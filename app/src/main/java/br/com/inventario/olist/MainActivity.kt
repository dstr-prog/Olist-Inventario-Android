package br.com.inventario.olist

import android.os.Bundle
import android.media.AudioManager
import android.media.ToneGenerator
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import kotlinx.coroutines.delay

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

@Composable
private fun SplashScreen(version: String, onFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2_000)
        onFinished()
    }

    val mint = Color(0xFF8CE0C3)
    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(
                colors = listOf(Color(0xFF102B27), Color(0xFF176B5B), Color(0xFF0D3933)),
            ),
        ),
    ) {
        Box(
            Modifier.align(Alignment.TopEnd).offset(x = 86.dp, y = (-78).dp).size(260.dp)
                .clip(CircleShape).background(Color.White.copy(alpha = 0.035f)),
        )
        Box(
            Modifier.align(Alignment.BottomStart).offset(x = (-104).dp, y = 100.dp).size(280.dp)
                .clip(CircleShape).background(mint.copy(alpha = 0.055f)),
        )

        Column(
            modifier = Modifier.align(Alignment.Center).fillMaxWidth().padding(horizontal = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier.size(116.dp).clip(RoundedCornerShape(34.dp))
                    .background(Color.White.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Inventory2,
                    contentDescription = null,
                    tint = mint,
                    modifier = Modifier.size(62.dp),
                )
            }
            Spacer(Modifier.height(28.dp))
            Text("Inventário", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold)
            Text(
                "OLIST",
                color = mint,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 6.sp,
            )
            Spacer(Modifier.height(28.dp))
            Spacer(Modifier.width(48.dp).height(1.dp).background(mint.copy(alpha = 0.7f)))
            Spacer(Modifier.height(18.dp))
            Text(
                "CONTAGEM DE ESTOQUE",
                color = Color.White.copy(alpha = 0.78f),
                fontSize = 12.sp,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center,
            )
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 42.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Dstr Tech", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text("Versão $version", color = Color.White.copy(alpha = 0.72f), fontSize = 12.sp)
        }
    }
}

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = MaterialTheme.colorScheme.copy(
                    primary = Green,
                    secondary = Color(0xFF46796E),
                    surface = Color(0xFFF8FAF9),
                ),
            ) {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF102B27)) {
                    SplashScreen(BuildConfig.VERSION_NAME) {
                        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                        finish()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InventoryScreen(database: InventoryDatabase, contentResolver: android.content.ContentResolver) {
    var revision by remember { mutableStateOf(0) }
    var status by remember { mutableStateOf("") }
    var countingError by remember { mutableStateOf("") }
    var showCountingScreen by rememberSaveable { mutableStateOf(false) }
    var barcode by rememberSaveable { mutableStateOf("") }
    var quantity by rememberSaveable { mutableStateOf("0") }
    var location by rememberSaveable { mutableStateOf("") }
    var candidate by remember { mutableStateOf<Product?>(null) }
    var choices by remember { mutableStateOf<List<Product>>(emptyList()) }
    var showDuplicateDialog by remember { mutableStateOf(false) }
    var showZeroQuantityDialog by remember { mutableStateOf(false) }
    var showAdjustDialog by remember { mutableStateOf(false) }
    var showImportRequiredDialog by remember { mutableStateOf(false) }
    var adjustedQuantity by remember { mutableStateOf("") }
    var notFoundBarcode by remember { mutableStateOf<String?>(null) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var editQuantity by remember { mutableStateOf("") }
    var editLocation by remember { mutableStateOf("") }
    var pendingDeleteProduct by remember { mutableStateOf<Product?>(null) }
    var refocusBarcode by remember { mutableStateOf(false) }
    val barcodeFocusRequester = remember { FocusRequester() }
    val quantityFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(refocusBarcode) {
        if (refocusBarcode) {
            barcodeFocusRequester.requestFocus()
            keyboardController?.show()
            refocusBarcode = false
        }
    }

    LaunchedEffect(candidate?.id, showCountingScreen) {
        if (candidate != null && showCountingScreen) quantityFocusRequester.requestFocus()
    }

    LaunchedEffect(notFoundBarcode) {
        if (notFoundBarcode != null) {
            val tone = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
            try {
                tone.startTone(ToneGenerator.TONE_PROP_NACK, 350)
                delay(400)
            } finally {
                tone.release()
            }
        }
    }

    fun enterCounting() {
        if (database.productCount() == 0) {
            showImportRequiredDialog = true
        } else {
            countingError = ""
            showCountingScreen = true
        }
    }

    fun reportProductNotFound(code: String) {
        countingError = ""
        notFoundBarcode = code
    }

    fun importCatalog(catalog: Catalog) {
        val summary = database.mergeCatalog(catalog)
        candidate = null
        choices = emptyList()
        status = "Importação concluída: ${summary.added} novos, ${summary.updated} atualizados e ${summary.removed} de outros tipos removidos. Contagens anteriores preservadas."
        revision++
    }

    fun selectProduct(product: Product) {
        candidate = product
        choices = emptyList()
    }

    fun finishCount(product: Product, counted: Double, message: String, countLocation: String = location) {
        database.saveCount(product.id, counted, countLocation)
        status = message
        location = formatLocationInput(countLocation)
        countingError = ""
        candidate = null
        choices = emptyList()
        barcode = ""
        quantity = "0"
        revision++
        refocusBarcode = true
    }

    val barcodeScanner = rememberLauncherForActivityResult(ScanContract()) { result ->
        result.contents?.let { scanned ->
            if (database.productCount() == 0) {
                showImportRequiredDialog = true
                return@let
            }
            showCountingScreen = true
            barcode = scanned
            val matches = database.findByBarcode(scanned)
            choices = matches
            candidate = matches.singleOrNull()
            candidate?.let(::selectProduct)
            status = when {
                matches.isEmpty() -> {
                    reportProductNotFound(scanned)
                    ""
                }
                matches.size == 1 -> "Produto localizado."
                else -> "Há mais de um produto com esse código. Escolha o correto."
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching {
                val catalog = OlistWorkbook.import(contentResolver, uri)
                importCatalog(catalog)
            }.onFailure { error -> status = error.message ?: "Não foi possível importar a planilha." }
        }
    }

    val spreadsheetExportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/vnd.ms-excel"),
    ) { uri ->
        if (uri != null) {
            runCatching {
                contentResolver.openOutputStream(uri)?.use { output ->
                    OlistWorkbook.exportInventory(database.countedProducts(), output)
                } ?: error("Não foi possível salvar a planilha.")
                status = "Planilha de inventário exportada com ${database.countedCount()} produtos."
                revision++
            }.onFailure { error -> status = error.message ?: "Não foi possível exportar a planilha." }
        }
    }

    val productCount = remember(revision) { database.productCount() }
    val countedCount = remember(revision) { database.countedCount() }
    val countedProducts = remember(revision) { database.countedProducts() }

    LaunchedEffect(productCount, showCountingScreen) {
        if (productCount == 0 && showCountingScreen) {
            showCountingScreen = false
            showImportRequiredDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventário Olist | Dstr-Tech", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { Icon(Icons.Default.Inventory2, contentDescription = null, tint = Green, modifier = Modifier.padding(start = 16.dp, end = 10.dp)) },
            )
        },
    ) { insets ->
        Column(
            modifier = Modifier.fillMaxSize().padding(insets).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(2.dp))
            if (!showCountingScreen) {
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
                    Button(
                        onClick = { importLauncher.launch(arrayOf("application/vnd.ms-excel", "application/octet-stream")) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("  Importar XLS")
                    }
                    OutlinedButton(
                        onClick = { spreadsheetExportLauncher.launch("estoque_inventario.xls") },
                        enabled = countedCount > 0,
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("  Exportar XLS")
                    }
                }

                Button(onClick = ::enterCounting, modifier = Modifier.fillMaxWidth()) {
                    Text("Contagem")
                }

                if (status.isNotBlank()) {
                    Text(status, color = Green, fontWeight = FontWeight.Medium, modifier = Modifier.padding(vertical = 2.dp))
                }
            } else {
                Text("Registrar contagem", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                OutlinedButton(onClick = { showCountingScreen = false }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("  Voltar")
                }

                Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = formatLocationInput(it) },
                        label = { Text("Localização (opcional)") },
                        supportingText = { Text("Use o formato: D.RR.PP.N (Depósito, Rua, Prédio e Nível)") },
                        isError = location.isNotBlank() && !isValidLocation(location),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = barcode,
                        onValueChange = {
                            barcode = it.trimStart()
                            candidate = null
                            choices = emptyList()
                            countingError = ""
                        },
                        label = { Text("Código de barras ou SKU") },
                        placeholder = { Text("Leia com o leitor ou digite") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().focusRequester(barcodeFocusRequester),
                        trailingIcon = {
                            IconButton(onClick = {
                                if (barcode.isBlank()) {
                                    candidate = null
                                    choices = emptyList()
                                    countingError = "Informe ou leia um código."
                                } else {
                                    val matches = database.findByBarcode(barcode.trim())
                                    choices = matches
                                    candidate = matches.singleOrNull()
                                    candidate?.let(::selectProduct)
                                    countingError = ""
                                    if (matches.isEmpty()) {
                                        reportProductNotFound(barcode.trim())
                                    } else {
                                        status = when {
                                            matches.size == 1 -> "Produto localizado."
                                            else -> "Há mais de um produto com esse código. Escolha o correto."
                                        }
                                    }
                                }
                            }) { Icon(Icons.Default.Search, contentDescription = "Localizar produto") }
                        },
                    )

                    OutlinedButton(
                        onClick = {
                            if (productCount == 0) {
                                showImportRequiredDialog = true
                            } else {
                                val options = ScanOptions()
                                    .setDesiredBarcodeFormats(ScanOptions.ONE_D_CODE_TYPES)
                                    .setPrompt("Aponte a câmera para o código de barras")
                                    .setBeepEnabled(true)
                                    .setOrientationLocked(false)
                                barcodeScanner.launch(options)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("  Ler com a câmera")
                    }

                    if (choices.size > 1) {
                        Text("Selecione o produto", fontWeight = FontWeight.Medium)
                        choices.forEach { product ->
                            OutlinedButton(onClick = { selectProduct(product) }, modifier = Modifier.fillMaxWidth()) {
                                Text("${product.description}\nSKU ${product.sku} · EAN ${product.gtin}")
                            }
                        }
                    }

                    candidate?.let { product ->
                        ProductCard(product)
                        if (product.countedQuantity != null) {
                            TextButton(onClick = {
                                adjustedQuantity = product.countedQuantity.toString()
                                showAdjustDialog = true
                            }, modifier = Modifier.align(Alignment.End)) {
                                Text("Ajustar quantidade")
                            }
                        }
                        OutlinedTextField(
                            value = quantity,
                            onValueChange = { value -> if (value.matches(Regex("^\\d{0,8}([,.]\\d{0,3})?$"))) quantity = value },
                            label = { Text("Quantidade contada") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth().focusRequester(quantityFocusRequester),
                        )
                        Button(onClick = {
                            val counted = quantity.replace(',', '.').toDoubleOrNull()
                            if (location.isNotBlank() && !isValidLocation(location)) {
                                countingError = "Use o formato: D.RR.PP.N (Depósito, Rua, Prédio e Nível)"
                            } else if (counted == null || counted < 0.0) {
                                countingError = "Informe uma quantidade válida."
                            } else if (product.countedQuantity != null) {
                                countingError = ""
                                showDuplicateDialog = true
                            } else if (counted == 0.0) {
                                countingError = ""
                                showZeroQuantityDialog = true
                            } else {
                                finishCount(product, counted, "Contagem registrada para ${product.description}.")
                            }
                        }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Text("  Registrar quantidade")
                        }
                    }
                }
                }

                if (countingError.isNotBlank()) {
                    Text(countingError, color = Color.Red, fontWeight = FontWeight.Medium)
                }

                Text("Produtos contados (${countedProducts.size})", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Segure um item para editar. Deslize para a direita para excluir a contagem.", color = Color(0xFF56615E))
                if (countedProducts.isEmpty()) {
                    Text("Os produtos registrados aparecerão aqui.", color = Color(0xFF56615E))
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(countedProducts, key = { it.id }) { countedProduct ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .pointerInput(countedProduct.id) {
                                        var horizontalDistance = 0f
                                        detectHorizontalDragGestures(
                                            onDragStart = { horizontalDistance = 0f },
                                            onDragEnd = {
                                                if (horizontalDistance > 100.dp.toPx()) pendingDeleteProduct = countedProduct
                                            },
                                            onDragCancel = { horizontalDistance = 0f },
                                            onHorizontalDrag = { change, dragAmount ->
                                                horizontalDistance += dragAmount
                                                change.consume()
                                            },
                                        )
                                    }
                                    .combinedClickable(
                                        onClick = {},
                                        onLongClick = {
                                            editingProduct = countedProduct
                                            editQuantity = countedProduct.countedQuantity?.toString().orEmpty()
                                            editLocation = countedProduct.countedLocation
                                                .ifBlank { countedProduct.location }
                                                .takeIf(::isValidLocation)
                                                .orEmpty()
                                        },
                                    ),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(countedProduct.description, fontWeight = FontWeight.SemiBold)
                                        Text("SKU: ${countedProduct.sku.ifBlank { "—" }}", color = Color(0xFF56615E))
                                        Text(
                                            "Localização: ${countedProduct.countedLocation.ifBlank { countedProduct.location }.ifBlank { "—" }}",
                                            color = Color(0xFF56615E),
                                        )
                                    }
                                    Text(
                                        formatQuantity(countedProduct.countedQuantity ?: 0.0),
                                        color = Green,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
        }
    }

    if (showImportRequiredDialog) {
        AlertDialog(
            onDismissRequest = { showImportRequiredDialog = false },
            title = { Text("Importe os produtos") },
            text = { Text("Importe a planilha de produtos da Olist para começar a contagem.") },
            confirmButton = {
                Button(onClick = { showImportRequiredDialog = false }) { Text("OK") }
            },
        )
    }

    if (notFoundBarcode != null) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Produto não encontrado") },
            text = {
                Text(
                    "O código ${notFoundBarcode.orEmpty()} não foi localizado na planilha importada.",
                    color = Color.Red,
                )
            },
            confirmButton = {
                Button(onClick = {
                    notFoundBarcode = null
                    barcode = ""
                    candidate = null
                    choices = emptyList()
                    refocusBarcode = true
                }) { Text("OK") }
            },
        )
    }

    editingProduct?.let { product ->
        val parsedQuantity = editQuantity.replace(',', '.').toDoubleOrNull()
        val locationValid = editLocation.isBlank() || isValidLocation(editLocation)
        AlertDialog(
            onDismissRequest = { editingProduct = null },
            title = { Text("Editar contagem") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(product.description, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = editQuantity,
                        onValueChange = { value ->
                            if (value.matches(Regex("^\\d{0,8}([,.]\\d{0,3})?$"))) editQuantity = value
                        },
                        label = { Text("Quantidade") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    )
                    OutlinedTextField(
                        value = editLocation,
                        onValueChange = { editLocation = formatLocationInput(it) },
                        label = { Text("Localização (opcional)") },
                        supportingText = { Text("Use o formato: D.RR.PP.N (Depósito, Rua, Prédio e Nível)") },
                        isError = editLocation.isNotBlank() && !locationValid,
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = parsedQuantity != null && parsedQuantity >= 0.0 && locationValid,
                    onClick = {
                        database.saveCount(product.id, parsedQuantity!!, editLocation)
                        editingProduct = null
                        status = "Contagem atualizada para ${product.description}."
                        revision++
                    },
                ) { Text("Salvar") }
            },
            dismissButton = {
                TextButton(onClick = { editingProduct = null }) { Text("Cancelar") }
            },
        )
    }

    pendingDeleteProduct?.let { product ->
        AlertDialog(
            onDismissRequest = { pendingDeleteProduct = null },
            title = { Text("Excluir contagem?") },
            text = { Text("A contagem de ${product.description} será removida. O produto continuará no catálogo.") },
            confirmButton = {
                Button(onClick = {
                    database.clearCount(product.id)
                    pendingDeleteProduct = null
                    status = "Contagem removida para ${product.description}."
                    revision++
                }) { Text("Excluir") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteProduct = null }) { Text("Cancelar") }
            },
        )
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
                    showDuplicateDialog = false
                    finishCount(product, (product.countedQuantity ?: 0.0) + entered, "Quantidade somada para ${product.description}.")
                }) { Text("Somar") }
            },
            dismissButton = {
                Row {
                    OutlinedButton(onClick = {
                        showDuplicateDialog = false
                        finishCount(product, entered, "Quantidade substituída para ${product.description}.")
                    }) { Text("Sobrescrever") }
                    IconButton(onClick = { showDuplicateDialog = false }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cancelar") }
                }
            },
        )
    }

    if (showZeroQuantityDialog) {
        val product = candidate
        if (product != null) AlertDialog(
            onDismissRequest = { showZeroQuantityDialog = false },
            title = { Text("Confirmar quantidade zero") },
            text = { Text("Deseja registrar zero unidades para ${product.description}?") },
            confirmButton = {
                Button(onClick = {
                    showZeroQuantityDialog = false
                    finishCount(product, 0.0, "Contagem zero registrada para ${product.description}.")
                }) { Text("Registrar zero") }
            },
            dismissButton = {
                TextButton(onClick = { showZeroQuantityDialog = false }) { Text("Cancelar") }
            },
        )
    }

    if (showAdjustDialog) {
        val product = candidate
        val corrected = adjustedQuantity.replace(',', '.').toDoubleOrNull()
        if (product != null) AlertDialog(
            onDismissRequest = { showAdjustDialog = false },
            title = { Text("Ajustar quantidade") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Contagem salva: ${formatQuantity(product.countedQuantity ?: 0.0)}. Informe o total correto para substituir esse valor.")
                    OutlinedTextField(
                        value = adjustedQuantity,
                        onValueChange = { value ->
                            if (value.matches(Regex("^\\d{0,8}([,.]\\d{0,3})?$"))) adjustedQuantity = value
                        },
                        label = { Text("Nova quantidade total") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = corrected != null && corrected >= 0.0,
                    onClick = {
                        val newLocation = location.ifBlank { product.countedLocation }
                        showAdjustDialog = false
                        finishCount(product, corrected!!, "Contagem corrigida para ${product.description}.", newLocation)
                    },
                ) { Text("Salvar correção") }
            },
            dismissButton = {
                TextButton(onClick = { showAdjustDialog = false }) { Text("Cancelar") }
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

private fun formatLocationInput(value: String): String {
    val normalized = value.uppercase(Locale.ROOT).filter { it.isLetterOrDigit() || it == '.' }
    val deposit = normalized.takeWhile(Char::isLetter).take(2)
    val digits = normalized.dropWhile { it.isLetter() || it == '.' }.filter(Char::isDigit).take(5)
    val separatorCount = normalized.count { it == '.' }

    return buildString {
        append(deposit)
        if (deposit.isNotEmpty() && (digits.isNotEmpty() || separatorCount > 0)) append('.')
        append(digits.take(2))
        if (digits.length > 2 || separatorCount >= 2) {
            append('.')
            append(digits.drop(2).take(2))
        }
        if (digits.length > 4 || separatorCount >= 3) {
            append('.')
            append(digits.drop(4).take(1))
        }
    }
}

private fun isValidLocation(value: String): Boolean =
    Regex("^[A-Z]{1,2}\\.\\d{2}\\.\\d{2}\\.\\d$").matches(value)


