package br.com.inventario.olist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.inventario.olist.data.InventoryDatabase
import br.com.inventario.olist.data.Catalog
import br.com.inventario.olist.data.OlistWorkbook
import br.com.inventario.olist.data.Product
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import java.text.NumberFormat
import java.util.Locale

private val Green = Color(0xFF176B5B)
private val PaleGreen = Color(0xFFE8F4F0)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = InventoryDatabase(this)
        setContent {
            MaterialTheme(
                colorScheme = MaterialTheme.colorScheme.copy(
                    primary = Green,
                    secondary = Color(0xFF46796E),
                    surface = Color(0xFFF8FAF9),
                ),
            ) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
                    InventoryScreen(database, contentResolver)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InventoryScreen(database: InventoryDatabase, contentResolver: android.content.ContentResolver) {
    var revision by remember { mutableStateOf(0) }
    var status by remember { mutableStateOf("Importe a planilha de produtos da Olist para começar.") }
    var barcode by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var location by remember { mutableStateOf("") }
    var candidate by remember { mutableStateOf<Product?>(null) }
    var choices by remember { mutableStateOf<List<Product>>(emptyList()) }
    var showDuplicateDialog by remember { mutableStateOf(false) }
    var pendingCatalog by remember { mutableStateOf<Catalog?>(null) }
    var showCatalogResetDialog by remember { mutableStateOf(false) }

    fun installCatalog(catalog: Catalog) {
        database.replaceCatalog(catalog)
        candidate = null
        choices = emptyList()
        status = "Catálogo carregado: ${catalog.products.size} produtos."
        revision++
    }

    val barcodeScanner = rememberLauncherForActivityResult(ScanContract()) { result ->
        result.contents?.let { scanned ->
            barcode = scanned
            choices = database.findByBarcode(scanned)
            candidate = choices.singleOrNull()
            if (candidate != null) location = candidate?.location.orEmpty().take(10)
            status = when {
                choices.isEmpty() -> "Produto não encontrado para: $scanned"
                choices.size == 1 -> "Produto localizado."
                else -> "Há mais de um produto com esse código. Escolha o correto."
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching {
                val catalog = OlistWorkbook.import(contentResolver, uri)
                if (database.countedCount() > 0) {
                    pendingCatalog = catalog
                    showCatalogResetDialog = true
                } else installCatalog(catalog)
            }.onFailure { error -> status = error.message ?: "Não foi possível importar a planilha." }
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/vnd.ms-excel"),
    ) { uri ->
        if (uri != null) {
            runCatching {
                contentResolver.openOutputStream(uri)?.use { output ->
                    OlistWorkbook.export(database.headers(), database.sheetName(), database.countedProducts(), output)
                } ?: error("Não foi possível salvar a planilha.")
                status = "Planilha exportada com ${database.countedCount()} produtos contados."
                revision++
            }.onFailure { error -> status = error.message ?: "Não foi possível exportar a planilha." }
        }
    }

    val productCount = remember(revision) { database.productCount() }
    val countedCount = remember(revision) { database.countedCount() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventário Olist", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { Icon(Icons.Default.Inventory2, contentDescription = null, tint = Green, modifier = Modifier.padding(start = 16.dp, end = 10.dp)) },
            )
        },
    ) { insets ->
        Column(
            modifier = Modifier.fillMaxSize().padding(insets).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(2.dp))
            Text("Contagem de estoque", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("Carregue os produtos, conte pelo código de barras e exporte para atualizar a Olist.", color = Color(0xFF56615E))

            ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = PaleGreen)) {
                Row(Modifier.fillMaxWidth().padding(18.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Metric("Produtos", productCount.toString())
                    HorizontalDivider(Modifier.size(width = 1.dp, height = 44.dp), color = Color(0xFFBFD9D1))
                    Metric("Contados", countedCount.toString())
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = { importLauncher.launch(arrayOf("application/vnd.ms-excel", "application/octet-stream")) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("  Importar XLS")
                }
                OutlinedButton(
                    onClick = { exportLauncher.launch("inventario_olist.xls") },
                    enabled = countedCount > 0,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("  Exportar")
                }
            }

            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Registrar contagem", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it.take(10) },
                        label = { Text("Localização (até 10 caracteres)") },
                        supportingText = { Text("Ex.: A-01, RUA3, PRAT02") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = barcode,
                        onValueChange = {
                            barcode = it.trimStart()
                            candidate = null
                            choices = emptyList()
                        },
                        label = { Text("Código de barras ou SKU") },
                        placeholder = { Text("Leia com o leitor ou digite") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = {
                                if (barcode.isBlank()) {
                                    candidate = null
                                    choices = emptyList()
                                    status = "Informe ou leia um código."
                                } else {
                                    choices = database.findByBarcode(barcode.trim())
                                    candidate = choices.singleOrNull()
                                    if (candidate != null) location = candidate?.location.orEmpty().take(10)
                                    status = when {
                                        choices.isEmpty() -> "Produto não encontrado para: ${barcode.trim()}"
                                        choices.size == 1 -> "Produto localizado."
                                        else -> "Há mais de um produto com esse código. Escolha o correto."
                                    }
                                }
                            }) { Icon(Icons.Default.Search, contentDescription = "Localizar produto") }
                        },
                    )

                    OutlinedButton(
                        onClick = {
                            val options = ScanOptions()
                                .setDesiredBarcodeFormats(ScanOptions.ONE_D_CODE_TYPES)
                                .setPrompt("Aponte a câmera para o código de barras")
                                .setBeepEnabled(true)
                                .setOrientationLocked(false)
                            barcodeScanner.launch(options)
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("  Ler com a câmera")
                    }

                    if (choices.size > 1) {
                        Text("Selecione o produto", fontWeight = FontWeight.Medium)
                        choices.forEach { product ->
                            OutlinedButton(onClick = { candidate = product; choices = emptyList(); location = product.location.take(10) }, modifier = Modifier.fillMaxWidth()) {
                                Text("${product.description}\nSKU ${product.sku} · EAN ${product.gtin}")
                            }
                        }
                    }

                    candidate?.let { product ->
                        ProductCard(product)
                        OutlinedTextField(
                            value = quantity,
                            onValueChange = { value -> if (value.matches(Regex("^\\d{0,8}([,.]\\d{0,3})?$"))) quantity = value },
                            label = { Text("Quantidade contada") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Button(onClick = {
                            val counted = quantity.replace(',', '.').toDoubleOrNull()
                            if (counted == null || counted < 0.0) {
                                status = "Informe uma quantidade válida."
                            } else if (product.countedQuantity != null) {
                                showDuplicateDialog = true
                            } else {
                                database.saveCount(product.id, counted, location)
                                status = "Contagem registrada para ${product.description}."
                                candidate = database.findByBarcode(barcode.trim()).firstOrNull { it.id == product.id }
                                quantity = "1"
                                revision++
                            }
                        }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Text("  Registrar quantidade")
                        }
                    }
                }
            }

            Text(status, color = Green, fontWeight = FontWeight.Medium, modifier = Modifier.padding(vertical = 2.dp))
            Spacer(Modifier.height(14.dp))
        }
    }

    if (showDuplicateDialog) {
        val product = candidate
        val entered = quantity.replace(',', '.').toDoubleOrNull() ?: 0.0
        if (product != null) AlertDialog(
            onDismissRequest = { showDuplicateDialog = false },
            title = { Text("Produto já contado") },
            text = {
                Text("${product.description}\n\nQuantidade anterior: ${formatQuantity(product.countedQuantity ?: 0.0)}\nNova quantidade: ${formatQuantity(entered)}")
            },
            confirmButton = {
                Button(onClick = {
                    database.saveCount(product.id, (product.countedQuantity ?: 0.0) + entered, location)
                    showDuplicateDialog = false
                    status = "Quantidade somada para ${product.description}."
                    candidate = database.findByBarcode(barcode.trim()).firstOrNull { it.id == product.id }
                    quantity = "1"
                    revision++
                }) { Text("Somar") }
            },
            dismissButton = {
                Row {
                    OutlinedButton(onClick = {
                        database.saveCount(product.id, entered, location)
                        showDuplicateDialog = false
                        status = "Quantidade substituída para ${product.description}."
                        candidate = database.findByBarcode(barcode.trim()).firstOrNull { it.id == product.id }
                        quantity = "1"
                        revision++
                    }) { Text("Sobrescrever") }
                    IconButton(onClick = { showDuplicateDialog = false }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cancelar") }
                }
            },
        )
    }

    if (showCatalogResetDialog) {
        AlertDialog(
            onDismissRequest = { showCatalogResetDialog = false; pendingCatalog = null },
            title = { Text("Substituir catálogo?") },
            text = { Text("A contagem atual será apagada ao carregar outra planilha. Deseja continuar?") },
            confirmButton = {
                Button(onClick = {
                    pendingCatalog?.let(::installCatalog)
                    pendingCatalog = null
                    showCatalogResetDialog = false
                }) { Text("Substituir") }
            },
            dismissButton = {
                OutlinedButton(onClick = { pendingCatalog = null; showCatalogResetDialog = false }) { Text("Cancelar") }
            },
        )
    }
}

@Composable
private fun Metric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Green)
        Text(label, color = Color(0xFF56615E))
    }
}

@Composable
private fun ProductCard(product: Product) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F6F5))) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(product.description, fontWeight = FontWeight.SemiBold)
            Text("SKU: ${product.sku.ifBlank { "—" }}", color = Color(0xFF56615E))
            Text("GTIN/EAN: ${product.gtin.ifBlank { "—" }}", color = Color(0xFF56615E))
            if (product.countedQuantity != null) Text(
                "Já contado: ${formatQuantity(product.countedQuantity)}",
                color = Green,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

private fun formatQuantity(value: Double): String = NumberFormat.getNumberInstance(Locale.forLanguageTag("pt-BR")).format(value)


