package br.com.inventario.olist.data

import android.content.ContentResolver
import android.net.Uri
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.Row
import java.io.OutputStream
import java.text.Normalizer

object OlistWorkbook {
    fun import(contentResolver: ContentResolver, uri: Uri): Catalog {
        val input = contentResolver.openInputStream(uri) ?: error("Não foi possível abrir a planilha.")
        input.use { stream ->
            HSSFWorkbook(stream).use { workbook ->
                val sheet = workbook.getSheetAt(0)
                val headerRow = sheet.getRow(sheet.firstRowNum) ?: error("A planilha está vazia.")
                val formatter = DataFormatter()
                val headers = (0 until headerRow.lastCellNum.toInt()).map { index ->
                    formatter.formatCellValue(headerRow.getCell(index)).trim()
                }
                val columns = mapHeaders(headers)
                val products = mutableListOf<Product>()
                val excludedProductIds = mutableSetOf<String>()
                for (rowIndex in (sheet.firstRowNum + 1)..sheet.lastRowNum) {
                    val row = sheet.getRow(rowIndex) ?: continue
                    val id = cellText(row.getCell(columns.id), formatter)
                    if (id.isBlank()) continue
                    val productType = cellText(row.getCell(columns.productType), formatter)
                    if (!productType.equals("S", ignoreCase = true)) {
                        excludedProductIds += id
                        continue
                    }
                    val sku = cellText(row.getCell(columns.sku), formatter)
                    val description = cellText(row.getCell(columns.description), formatter)
                    val gtin = cellText(row.getCell(columns.gtin), formatter)
                    val location = cellText(row.getCell(columns.location), formatter)
                    products += Product(id, sku, description, gtin, location)
                }
                require(products.isNotEmpty() || excludedProductIds.isNotEmpty()) {
                    "Não encontrei produtos com ID na primeira aba."
                }
                return Catalog(headers, sheet.sheetName, products, excludedProductIds.toList())
            }
        }
    }

    fun exportInventory(products: List<Product>, output: OutputStream) {
        require(products.isNotEmpty()) { "Ainda não há produtos contados para exportar." }
        val headers = listOf("ID*", "Produto", "Código (SKU)*", "GTIN/EAN", "Localização", "Saldo em estoque")
        HSSFWorkbook().use { workbook ->
            val sheet = workbook.createSheet("Inventário de Estoque")
            val headerRow = sheet.createRow(0)
            headers.forEachIndexed { index, header -> headerRow.createCell(index).setCellValue(header) }

            products.forEachIndexed { index, product ->
                require(product.id.isNotBlank()) {
                    "O produto '${product.description}' está sem ID e não pode ser exportado."
                }
                require(product.sku.isNotBlank()) {
                    "O produto '${product.description}' está sem SKU, que é obrigatório na planilha de inventário."
                }
                val row = sheet.createRow(index + 1)
                row.createCell(0).setCellValue(product.id)
                row.createCell(1).setCellValue(product.description)
                row.createCell(2).setCellValue(product.sku)
                row.createCell(3).setCellValue(product.gtin)
                row.createCell(4).setCellValue(product.countedLocation.ifBlank { product.location })
                row.createCell(5).setCellValue(product.countedQuantity ?: 0.0)
            }
            workbook.write(output)
        }
    }

    private data class Columns(
        val id: Int,
        val sku: Int,
        val description: Int,
        val gtin: Int,
        val location: Int,
        val productType: Int,
    )

    private fun mapHeaders(headers: List<String>): Columns {
        fun find(vararg candidates: String): Int {
            val normalized = candidates.map(::normalize)
            return headers.indexOfFirst { normalize(it) in normalized }.takeIf { it >= 0 }
                ?: error("A planilha não contém a coluna esperada: ${candidates.first()}.")
        }
        return Columns(
            id = find("ID"),
            sku = find("Código (SKU)", "SKU"),
            description = find("Descrição", "Descricao"),
            gtin = find("GTIN/EAN", "GTIN", "EAN"),
            location = find("Localização", "Localizacao"),
            productType = find("Tipo do produto"),
        )
    }

    private fun normalize(value: String): String = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "").lowercase()

    private fun cellText(cell: Cell?, formatter: DataFormatter): String = cell?.let {
        formatter.formatCellValue(it).trim().trimEnd(',')
    }.orEmpty()
}
