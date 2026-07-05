package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Product
import com.example.ui.CartItem
import com.example.ui.KasirViewModel
import com.example.utils.QrCodeGenerator
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(viewModel: KasirViewModel) {
    val products by viewModel.products.collectAsStateWithLifecycle()
    val cart by viewModel.cart.collectAsStateWithLifecycle()
    val cartTotal by viewModel.cartTotal.collectAsStateWithLifecycle()
    val checkoutSuccess by viewModel.checkoutSuccess.collectAsStateWithLifecycle()
    
    var showCartBottomSheet by remember { mutableStateOf(false) }
    var showCheckoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.addDummyProductsIfNeeded()
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.ShoppingCart, contentDescription = "Logo", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    Column {
                        Text("KasirKu", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFF4CAF50)))
                            Text("Shift 1 • Kasir", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            if (cart.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { showCartBottomSheet = true },
                    icon = { Icon(Icons.Filled.ShoppingCart, contentDescription = "Keranjang") },
                    text = { Text("${cart.sumOf { it.quantity }} Item - ${formatRupiah(cartTotal)}") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (products.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(150.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(products) { product ->
                        ProductCard(
                            product = product,
                            onClick = { viewModel.addToCart(product) }
                        )
                    }
                }
            }
        }
    }

    if (showCartBottomSheet) {
        ModalBottomSheet(onDismissRequest = { showCartBottomSheet = false }) {
            CartContent(
                cart = cart,
                total = cartTotal,
                onAdd = { viewModel.addToCart(it) },
                onRemove = { viewModel.removeFromCart(it) },
                onCheckout = {
                    showCartBottomSheet = false
                    showCheckoutDialog = true
                },
                onClear = {
                    viewModel.clearCart()
                    showCartBottomSheet = false
                }
            )
        }
    }

    if (showCheckoutDialog) {
        CheckoutDialog(
            total = cartTotal,
            onDismiss = { showCheckoutDialog = false },
            onConfirm = { method ->
                viewModel.checkout(method)
                showCheckoutDialog = false
            }
        )
    }
    
    if (checkoutSuccess != null) {
        ReceiptDialog(
            receiptId = checkoutSuccess!!,
            total = cartTotal,
            onDismiss = { viewModel.clearCheckoutSuccess() }
        )
    }
}

@Composable
fun ProductCard(product: Product, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = product.name.take(2).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = product.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatRupiah(product.price),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CartContent(
    cart: List<CartItem>,
    total: Double,
    onAdd: (Product) -> Unit,
    onRemove: (Product) -> Unit,
    onCheckout: () -> Unit,
    onClear: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Keranjang (${cart.sumOf { it.quantity }} Item)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
                TextButton(onClick = onClear) {
                    Text("Hapus Semua", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                }
            }
            
            if (cart.isEmpty()) {
                Text("Keranjang kosong.", modifier = Modifier.padding(vertical = 32.dp))
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                ) {
                    items(cart) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.product.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text("${formatRupiah(item.product.price)} × ${item.quantity}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant).clickable { onRemove(item.product) }, contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.Remove, contentDescription = "Kurang", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                                }
                                Text(item.quantity.toString(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant).clickable { onAdd(item.product) }, contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.Add, contentDescription = "Tambah", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                        Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                    }
                }
            }
        }
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Pembayaran", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(formatRupiah(total), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
            Button(
                onClick = onCheckout,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = cart.isNotEmpty(),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("PROSES PEMBAYARAN", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CheckoutDialog(
    total: Double,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var selectedMethod by remember { mutableStateOf("QRIS") }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    
    LaunchedEffect(selectedMethod) {
        if (selectedMethod == "QRIS") {
            // Generate dummy QRIS payload
            val dummyQris = "00020101021226570011ID.CO.QRIS.WWW011893600915312345678902148412345678901230303UMI51440014ID.CO.QRIS.WWW0215ID12345678901230303UMI5204581253033605406${total.toLong()}5802ID5911KasirKu App6007Jakarta61051234562070703A0163045E6A"
            qrBitmap = QrCodeGenerator.generateQrCode(dummyQris, 500)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Pilih Pembayaran", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FilterChip(
                        selected = selectedMethod == "QRIS",
                        onClick = { selectedMethod = "QRIS" },
                        label = { Text("QRIS") }
                    )
                    FilterChip(
                        selected = selectedMethod == "TUNAI",
                        onClick = { selectedMethod = "TUNAI" },
                        label = { Text("TUNAI") }
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                if (selectedMethod == "QRIS" && qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap!!.asImageBitmap(),
                        contentDescription = "QRIS Code",
                        modifier = Modifier.size(200.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Scan QRIS untuk membayar", style = MaterialTheme.typography.bodyMedium)
                } else if (selectedMethod == "TUNAI") {
                    Icon(Icons.Filled.ShoppingCart, contentDescription = null, modifier = Modifier.size(100.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Terima uang tunai dari pelanggan", style = MaterialTheme.typography.bodyMedium)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                Text("Total Tagihan", style = MaterialTheme.typography.bodyLarge)
                Text(formatRupiah(total), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onConfirm(selectedMethod) }) {
                        Text("Konfirmasi Pembayaran")
                    }
                }
            }
        }
    }
}

@Composable
fun ReceiptDialog(
    receiptId: String,
    total: Double,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(0.dp), // Receipt style
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("KASIRKU", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.Black)
                Text("Jl. Contoh Alamat No. 123", style = MaterialTheme.typography.bodySmall, color = Color.Black)
                Spacer(modifier = Modifier.height(16.dp))
                Text("No: $receiptId", style = MaterialTheme.typography.bodySmall, color = Color.Black)
                Text("Lunas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Divider(color = Color.Black, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("TOTAL TRANSAKSI", fontWeight = FontWeight.Bold, color = Color.Black)
                    Text(formatRupiah(total), fontWeight = FontWeight.Bold, color = Color.Black)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color.Black, thickness = 1.dp)
                
                Spacer(modifier = Modifier.height(24.dp))
                var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
                LaunchedEffect(Unit) {
                    qrBitmap = QrCodeGenerator.generateQrCode(receiptId, 200)
                }
                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap!!.asImageBitmap(),
                        contentDescription = "Receipt QR",
                        modifier = Modifier.size(100.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Terima Kasih!", style = MaterialTheme.typography.bodyMedium, color = Color.Black)
                
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Tutup & Cetak")
                }
            }
        }
    }
}

fun formatRupiah(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return format.format(amount).replace("Rp", "Rp ")
}
