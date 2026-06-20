package com.l0124005.sewain_rpl.ui.theme.transaksi

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.l0124005.sewain_rpl.network.ApiClient
import com.l0124005.sewain_rpl.network.TransaksiData
import com.l0124005.sewain_rpl.ui.theme.BluePrimary
import com.l0124005.sewain_rpl.ui.theme.NavyPrimary
import com.l0124005.sewain_rpl.ui.theme.SewainTopBar
import com.l0124005.sewain_rpl.ui.theme.katalog.formatRupiah
import com.l0124005.sewain_rpl.utils.Resource
import com.l0124005.sewain_rpl.viewmodel.TransaksiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailTransaksiScreen(
    viewModel: TransaksiViewModel,
    token: String,
    transaksiId: Int,
    onBack: () -> Unit,
    onPayClick: (List<Int>) -> Unit,
    onProductClick: (Int) -> Unit
) {
    val transaksiDetailState by viewModel.transaksiDetail.observeAsState()

    LaunchedEffect(transaksiId) {
        viewModel.getDetailTransaksi(token, transaksiId)
    }

    Scaffold(
        topBar = {
            SewainTopBar(
                onNavigationClick = onBack
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (transaksiDetailState) {
                is Resource.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is Resource.Success -> {
                    val transaksi = transaksiDetailState?.data?.data
                    if (transaksi != null) {
                        TransaksiDetailContent(transaksi, onPayClick, onProductClick)
                    } else {
                        Text("Data tidak ditemukan", modifier = Modifier.align(Alignment.Center))
                    }
                }
                is Resource.Error -> {
                    Text(
                        text = transaksiDetailState?.message ?: "Error",
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                null -> {
                    // Initial or reset state
                }
            }
        }
    }
}

@Composable
fun TransaksiDetailContent(
    transaksi: TransaksiData,
    onPayClick: (List<Int>) -> Unit,
    onProductClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = getStatusColor(transaksi.status).copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Status", fontSize = 14.sp, color = Color.Gray)
                    Text(
                        transaksi.status.uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = getStatusColor(transaksi.status)
                    )
                }
                if (!transaksi.barang?.foto_barang.isNullOrEmpty()) {
                    AsyncImage(
                        model = "${ApiClient.IMAGE_BASE_URL}${transaksi.barang?.foto_barang}",
                        contentDescription = null,
                        modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        // Information Sections
        InfoSection("Informasi Barang") {
            Card(
                modifier = Modifier.fillMaxWidth().clickable { 
                    transaksi.barang?.id?.let { onProductClick(it) }
                },
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    InfoRow("Nama Barang", transaksi.barang?.nama_barang ?: "-")
                    InfoRow("Jumlah", "${transaksi.jumlah} unit")
                    InfoRow("Harga Sewa", "Rp ${formatRupiah(transaksi.barang?.harga_sewa ?: 0.0)} / hari")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Lihat Detail Produk >", 
                        fontSize = 12.sp, 
                        color = NavyPrimary, 
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }

        InfoSection("Informasi Sewa") {
            InfoRow("Tanggal Sewa", transaksi.tanggal_sewa)
            InfoRow("Rencana Kembali", transaksi.tanggal_kembali_rencana)
            if (transaksi.tanggal_kembali_aktual != null) {
                InfoRow("Tanggal Kembali", transaksi.tanggal_kembali_aktual)
            }
        }

        InfoSection("Rincian Biaya") {
            InfoRow("Total Harga", "Rp ${formatRupiah(transaksi.total_harga)}")
            if (transaksi.total_denda > 0) {
                InfoRow("Total Denda", "Rp ${formatRupiah(transaksi.total_denda)}", color = Color.Red)
            }
        }

        if (transaksi.status.lowercase() == "menunggu pembayaran") {
            Button(
                onClick = { onPayClick(listOf(transaksi.id)) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text("Bayar Sekarang", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun InfoSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NavyPrimary)
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String, color: Color = Color.Black) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = Color.Gray)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = color)
    }
}
