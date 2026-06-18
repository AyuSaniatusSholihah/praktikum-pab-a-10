package com.l0124005.sewain_rpl.ui.theme.keranjang

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.l0124005.sewain_rpl.utils.Resource
import com.l0124005.sewain_rpl.viewmodel.KeranjangViewModel
import com.l0124005.sewain_rpl.network.KeranjangItem
import com.l0124005.sewain_rpl.network.CatalogData
import com.l0124005.sewain_rpl.utils.CurrencyUtils

object CartColors {
    val Blue = Color(0xFF6A87A1)
    val Black = Color(0xFF000000)
    val Dark = Color(0xFF484848)
    val Gray = Color(0xFF8A8A8A)
    val BorderLight = Color(0x26000000)
    val BorderRow = Color(0x63000000)
    val QtyBoxBorder = Color(0xFFDDDDDD)
}

object CartFonts {
    val Heading = FontFamily.Serif
    val Body = FontFamily.Default
}

@Composable
fun KeranjangScreen(
    token: String,
    viewModel: KeranjangViewModel,
    onBack: () -> Unit,
    onCheckout: () -> Unit = {}
) {
    val keranjangState by viewModel.keranjang.observeAsState()

    LaunchedEffect(Unit) {
        viewModel.getKeranjang(token)
    }

    Surface(color = Color.White, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Shopping Cart",
                    fontFamily = CartFonts.Heading,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            when (val state = keranjangState) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = CartColors.Blue)
                    }
                }
                is Resource.Success -> {
                    val data = state.data?.data
                    if (data == null || data.items.isEmpty()) {
                        EmptyCartSection(modifier = Modifier.weight(1f), onBrowseProducts = onBack)
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 20.dp)
                        ) {
                            item { CartTableHeader() }
                            items(data.items) { item ->
                                CartRow(
                                    item = item,
                                    onRemove = { viewModel.removeKeranjangItem(token, item.id) }
                                )
                                HorizontalDivider(color = CartColors.BorderRow, thickness = 1.dp)
                            }
                        }

                        CheckoutSection(
                            subtotal = data.total_estimasi.toLong(),
                            onCheckout = onCheckout
                        )
                    }
                }
                is Resource.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message ?: "Terjadi kesalahan", color = Color.Red)
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun CartTableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text("Product", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text("Price", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 13.sp, textAlign = TextAlign.Center)
        Text("Subtotal", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 13.sp, textAlign = TextAlign.Center)
    }
    HorizontalDivider(color = CartColors.BorderLight, thickness = 1.5.dp)
}

@Composable
private fun CartRow(
    item: KeranjangItem,
    onRemove: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0xFFF2F2F2), RoundedCornerShape(8.dp))
            ) {
                AsyncImage(
                    model = item.barang.foto_barang ?: "https://placehold.co/200",
                    contentDescription = item.barang.nama_barang,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.barang.nama_barang,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = CartColors.Black
                )
                Text(
                    text = "${CurrencyUtils.formatRupiah(item.barang.harga_sewa.toLong())} / hari",
                    fontSize = 12.sp,
                    color = CartColors.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Qty: ${item.jumlah}", fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = CartColors.Blue, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Text(
                text = CurrencyUtils.formatRupiah(item.subtotal.toLong()),
                modifier = Modifier.width(80.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
private fun EmptyCartSection(modifier: Modifier = Modifier, onBrowseProducts: () -> Unit) {
    Column(
        modifier = modifier.fillMaxWidth().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.ShoppingCart, null, tint = CartColors.Gray, modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Keranjang kamu kosong", fontSize = 18.sp, color = CartColors.Gray)
        Text(
            "Lihat Produk",
            color = CartColors.Blue,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable { onBrowseProducts() }.padding(8.dp)
        )
    }
}

@Composable
private fun CheckoutSection(subtotal: Long, onCheckout: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
        HorizontalDivider(color = Color(0xFFEEEEEE))
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total Estimasi", fontSize = 15.sp)
            Text(CurrencyUtils.formatRupiah(subtotal), fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onCheckout,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CartColors.Blue),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Checkout", color = Color.White)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun KeranjangScreenPreview() {
    Surface(color = Color.White) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            CartTableHeader()
            CartRow(
                item = KeranjangItem(
                    id = 1,
                    jumlah = 1,
                    tanggal_sewa = "2024-06-20",
                    tanggal_kembali_rencana = "2024-06-22",
                    subtotal = 450000.0,
                    barang = CatalogData(
                        id = 1,
                        user_id = 1,
                        kategori_id = 1,
                        nama_barang = "ALLTREK Tenda Camping",
                        deskripsi = "Tenda berkualitas",
                        harga_sewa = 450000.0,
                        harga_jaminan = 200000.0,
                        harga_denda_perjam = 15000.0,
                        stok = 5,
                        lokasi = "Bandung",
                        foto_barang = null,
                        status = "tersedia"
                    )
                ),
                onRemove = {}
            )
            CheckoutSection(subtotal = 450000, onCheckout = {})
        }
    }
}