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
                for (rowIndex in (sheet.firstRowNum + 1)..sheet.lastRowNum) {
                    val row = sheet.getRow(rowIndex) ?: continue
                    val id = cellText(row.getCell(columns.id), formatter)
                    val sku = cellText(row.getCell(columns.sku), formatter)
                    val description = cellText(row.getCell(columns.description), formatter)
                    val gtin = cellText(row.getCell(columns.gtin), formatter)
                    val location = cellText(row.getCell(columns.location), formatter)
                    if (id.isBlank() && sku.isBlank() && gtin.isBlank() && description.isBlank()) continue
                    if (id.isBlank()) continue
                    products += Product(id, sku, description, gtin, location)
                }
                require(products.isNotEmpty()) { "Não encontrei produtos com ID na primeira aba." }
                return Catalog(headers, sheet.sheetName, products)
            }
        }
    }

    fun export(headers: List<String>, sheetName: String, products: List<Product>, output: OutputStream) {
        require(headers.isNotEmpty()) { "Importe a planilha da Olist antes de exportar." }
        require(products.isNotEmpty()) { "Ainda não há produtos contados para exportar." }
        val columns = mapHeaders(headers)
        HSSFWorkbook().use { workbook ->
            val sheet = workbook.createSheet(sheetName.take(31).ifBlank { "Produtos" })
            val headerRow = sheet.createRow(0)
            headers.forEachIndexed { index, header -> headerRow.createCell(index).setCellValue(header) }

            products.forEachIndexed { index, product ->
                val row = sheet.createRow(index + 1)
                row.createCell(columns.id).setCellValue(product.id)
                row.createCell(columns.sku).setCellValue(product.sku)
                row.createCell(columns.gtin).setCellValue(product.gtin)
                row.createCell(columns.location).setCellValue(product.countedLocation)
                row.createCell(columns.stock).setCellValue(product.countedQuantity ?: 0.0)
            }
            workbook.write(output)
        }
    }

    private data class Columns(val id: Int, val sku: Int, val description: Int, val gtin: Int, val location: Int, val stock: Int)

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
            stock = find("Estoque"),
        )
    }

    private fun normalize(value: String): String = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "").lowercase()

    private fun cellText(cell: Cell?, formatter: DataFormatter): String = cell?.let {
        formatter.formatCellValue(it).trim().trimEnd(',')
    }.orEmpty()
}
