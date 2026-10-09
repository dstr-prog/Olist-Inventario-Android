package br.com.inventario.olist.data

import java.io.OutputStream
import java.nio.charset.StandardCharsets

data class InventoryTxtExportSummary(
    val productCount: Int,
    val unitCount: Long,
    val sizeBytes: Long,
)

object OlistInventoryTxt {
    private const val MAX_FILE_SIZE_BYTES = 2_000_000L

    fun export(products: List<Product>, output: OutputStream): InventoryTxtExportSummary {
        require(products.isNotEmpty()) { "Ainda não há produtos contados para exportar." }

        val lines = mutableListOf<Pair<ByteArray, Int>>()
        var unitCount = 0L
        var sizeBytes = 0L

        products.forEach { product ->
            val quantity = product.countedQuantity ?: return@forEach
            require(quantity.isFinite() && quantity >= 0.0 && quantity % 1.0 == 0.0) {
                "A quantidade de '${product.description}' precisa ser um número inteiro para gerar o TXT da Olist."
            }
            require(quantity <= MAX_FILE_SIZE_BYTES.toDouble()) {
                "O TXT ultrapassa o limite de 2 MB da Olist."
            }

            val units = quantity.toInt()
            if (units == 0) return@forEach

            val code = product.gtin.ifBlank { product.sku }.trim()
            require(code.isNotBlank()) {
                "O produto '${product.description}' não tem GTIN/EAN nem SKU para exportar."
            }
            val codeBytes = code.toByteArray(StandardCharsets.UTF_8)
            val lineSizeBytes = codeBytes.size + 2L // Código mais quebra de linha CRLF.
            sizeBytes += lineSizeBytes * units
            require(sizeBytes <= MAX_FILE_SIZE_BYTES) {
                "O TXT ultrapassa o limite de 2 MB da Olist. Reduza as quantidades ou exporte em arquivos menores."
            }

            lines += codeBytes to units
            unitCount += units
        }

        lines.forEach { (codeBytes, units) ->
            repeat(units) {
                output.write(codeBytes)
                output.write('\r'.code)
                output.write('\n'.code)
            }
        }

        return InventoryTxtExportSummary(
            productCount = products.size,
            unitCount = unitCount,
            sizeBytes = sizeBytes,
        )
    }
}
