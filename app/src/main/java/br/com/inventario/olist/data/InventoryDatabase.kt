package br.com.inventario.olist.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class Product(
    val id: String,
    val sku: String,
    val description: String,
    val gtin: String,
    val location: String,
    val countedQuantity: Double? = null,
    val countedLocation: String = "",
)

data class Catalog(
    val headers: List<String>,
    val sheetName: String,
    val products: List<Product>,
)

data class CatalogImportSummary(val added: Int, val updated: Int)

class InventoryDatabase(context: Context) : SQLiteOpenHelper(context, "inventory_olist.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""CREATE TABLE products (
            id TEXT PRIMARY KEY NOT NULL,
            sku TEXT NOT NULL,
            description TEXT NOT NULL,
            gtin TEXT NOT NULL,
            location TEXT NOT NULL,
            counted_quantity REAL,
            counted_location TEXT NOT NULL DEFAULT ''
        )""".trimIndent())
        db.execSQL("CREATE TABLE catalog_meta (meta_key TEXT PRIMARY KEY NOT NULL, meta_value TEXT NOT NULL)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit

    fun mergeCatalog(catalog: Catalog): CatalogImportSummary {
        val currentHeaders = headers()
        require(currentHeaders.isEmpty() || currentHeaders == catalog.headers) {
            "A planilha tem colunas diferentes da primeira importada. Use arquivos exportados no mesmo formato."
        }
        var added = 0
        var updated = 0
        writableDatabase.beginTransaction()
        try {
            catalog.products.forEach { product ->
                val values = ContentValues().apply {
                    put("sku", product.sku)
                    put("description", product.description)
                    put("gtin", product.gtin)
                    put("location", product.location)
                }
                val changed = writableDatabase.update("products", values, "id = ?", arrayOf(product.id))
                if (changed == 0) {
                    writableDatabase.insertOrThrow("products", null, ContentValues(values).apply {
                        put("id", product.id)
                    })
                    added++
                } else {
                    updated++
                }
            }
            if (currentHeaders.isEmpty()) {
                saveMeta("headers", catalog.headers.joinToString("\u001f"))
                saveMeta("sheet", catalog.sheetName)
            }
            writableDatabase.setTransactionSuccessful()
        } finally {
            writableDatabase.endTransaction()
        }
        return CatalogImportSummary(added, updated)
    }

    fun headers(): List<String> = getMeta("headers")?.split("\u001f") ?: emptyList()
    fun sheetName(): String = getMeta("sheet") ?: "Produtos"

    fun productCount(): Int = readableDatabase.rawQuery("SELECT COUNT(*) FROM products", null).use {
        if (it.moveToFirst()) it.getInt(0) else 0
    }

    fun countedCount(): Int = readableDatabase.rawQuery(
        "SELECT COUNT(*) FROM products WHERE counted_quantity IS NOT NULL", null
    ).use { if (it.moveToFirst()) it.getInt(0) else 0 }

    fun findByBarcode(barcode: String): List<Product> = queryProducts(
        "SELECT id, sku, description, gtin, location, counted_quantity, counted_location " +
            "FROM products WHERE gtin = ? OR sku = ? ORDER BY CASE WHEN gtin = ? THEN 0 ELSE 1 END, description",
        arrayOf(barcode, barcode, barcode),
    )

    fun countedProducts(): List<Product> = queryProducts(
        "SELECT id, sku, description, gtin, location, counted_quantity, counted_location " +
            "FROM products WHERE counted_quantity IS NOT NULL ORDER BY description",
        emptyArray(),
    )

    fun saveCount(productId: String, quantity: Double, location: String) {
        writableDatabase.update("products", ContentValues().apply {
            put("counted_quantity", quantity)
            put("counted_location", location)
        }, "id = ?", arrayOf(productId))
    }

    private fun queryProducts(sql: String, args: Array<String>): List<Product> {
        val products = mutableListOf<Product>()
        readableDatabase.rawQuery(sql, args).use { cursor ->
            while (cursor.moveToNext()) products += Product(
                id = cursor.getString(0),
                sku = cursor.getString(1),
                description = cursor.getString(2),
                gtin = cursor.getString(3),
                location = cursor.getString(4),
                countedQuantity = if (cursor.isNull(5)) null else cursor.getDouble(5),
                countedLocation = cursor.getString(6),
            )
        }
        return products
    }

    private fun saveMeta(key: String, value: String) {
        writableDatabase.insertWithOnConflict("catalog_meta", null, ContentValues().apply {
            put("meta_key", key)
            put("meta_value", value)
        }, SQLiteDatabase.CONFLICT_REPLACE)
    }

    private fun getMeta(key: String): String? = readableDatabase.query(
        "catalog_meta", arrayOf("meta_value"), "meta_key = ?", arrayOf(key), null, null, null,
    ).use { if (it.moveToFirst()) it.getString(0) else null }
}
