package com.l0124005.sewain_rpl.ui.theme.transaksi

import androidx.compose.foundation.background
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
import com.l0124005.sewain_rpl.network.TransaksiData
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
    onPayClick: (List<Int>) -> Unit
) {
    val transaksiDetailState by viewModel.transaksiDetail.observeAsState()

    LaunchedEffect(transaksiId) {
        viewModel.getDetailTransaksi(token, transaksiId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Transaksi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
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
                        TransaksiDetailContent(transaksi, onPayClick)
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
                else -> {}
            }
        }
    }
}

@Composable
fun TransaksiDetailContent(
    transaksi: TransaksiData,
    onPayClick: (List<Int>) -> Unit
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
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Status", fontSize = 14.sp, color = Color.Gray)
                Text(
                    transaksi.status.uppercase(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = getStatusColor(transaksi.status)
                )
            }
        }

        // Information Sections
        InfoSection("Informasi Barang") {
            InfoRow("Nama Barang", transaksi.barang?.nama_barang ?: "-")
            InfoRow("Jumlah", "${transaksi.jumlah} unit")
            InfoRow("Harga Sewa", "Rp ${formatRupiah(transaksi.barang?.harga_sewa ?: 0.0)} / hari")
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
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D8AA8))
            ) {
                Text("Bayar Sekarang", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun InfoSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E5276))
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
