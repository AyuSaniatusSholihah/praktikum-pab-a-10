package com.l0124005.sewain_rpl.ui.theme.transaksi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.l0124005.sewain_rpl.network.ApiClient
import com.l0124005.sewain_rpl.network.TransaksiData
import com.l0124005.sewain_rpl.ui.theme.BluePrimary
import com.l0124005.sewain_rpl.ui.theme.NavyPrimary
import com.l0124005.sewain_rpl.ui.theme.katalog.formatRupiah
import com.l0124005.sewain_rpl.utils.Resource
import com.l0124005.sewain_rpl.viewmodel.TransaksiViewModel

import com.l0124005.sewain_rpl.utils.RentalStatus
import java.text.SimpleDateFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiwayatTransaksiScreen(
    viewModel: TransaksiViewModel,
    token: String,
    onBack: () -> Unit,
    onDetailClick: (Int) -> Unit
) {
    val transaksiListState by viewModel.transaksiList.observeAsState()

    LaunchedEffect(Unit) {
        viewModel.getRiwayatTransaksi(token)
    }

    Scaffold(
        topBar = {
            com.l0124005.sewain_rpl.ui.theme.SewainTopBar(
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = onBack
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (transaksiListState) {
                is Resource.Loading -> {
                    CircularProgressIndicator(
                        color = BluePrimary,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is Resource.Success -> {
                    val data = transaksiListState?.data?.data ?: emptyList()
                    if (data.isEmpty()) {
                        Text("Belum ada transaksi", modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(data) { transaksi ->
                                TransaksiItem(transaksi, onDetailClick)
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    Text(
                        text = transaksiListState?.message ?: "Error",
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                null -> {
                    // State is null (e.g., before loading or after reset)
                }
            }
        }
    }
}

@Composable
fun TransaksiItem(transaksi: TransaksiData, onClick: (Int) -> Unit) {
    val rentalStatus = RentalStatus.fromTransaksi(transaksi)
    Card(
        onClick = { onClick(transaksi.id) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val barang = transaksi.barang
            val imageUrl = if (!barang?.foto_barang.isNullOrEmpty()) {
                if (barang?.foto_barang?.startsWith("http") == true) barang.foto_barang
                else "${ApiClient.IMAGE_BASE_URL}${barang?.foto_barang}"
            } else null

            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier.size(70.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(modifier = Modifier.size(70.dp).clip(RoundedCornerShape(8.dp)).background(Color.LightGray))
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = transaksi.barang?.nama_barang ?: "Barang", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = rentalStatus.badgeColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = rentalStatus.label.uppercase(),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = rentalStatus.badgeTextColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Total: Rp ${formatRupiah(transaksi.total_harga)}", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                
                val outputSdf = SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                val tglSewa = RentalStatus.parseFlexibleDate(transaksi.tanggal_sewa)
                val tglDisplay = if (tglSewa != null) outputSdf.format(tglSewa) else transaksi.tanggal_sewa
                Text(text = "$tglDisplay", fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}

fun getStatusColor(status: String): Color {
    return when (status.lowercase()) {
        "pending" -> Color.Gray
        "menunggu pembayaran" -> Color(0xFFFFA500)
        "dibayar", "disewa" -> NavyPrimary
        "dikembalikan" -> BluePrimary
        "selesai" -> BluePrimary
        "dibatalkan" -> Color.Red
        else -> Color.Black
    }
}
