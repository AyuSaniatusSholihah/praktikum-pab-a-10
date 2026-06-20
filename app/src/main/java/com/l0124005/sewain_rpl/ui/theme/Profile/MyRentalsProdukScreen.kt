package com.l0124005.sewain_rpl.ui.theme.profile

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.l0124005.sewain_rpl.network.ApiClient
import com.l0124005.sewain_rpl.network.TransaksiData
import com.l0124005.sewain_rpl.ui.theme.katalog.formatRupiah
import com.l0124005.sewain_rpl.ui.theme.transaksi.getStatusColor
import com.l0124005.sewain_rpl.utils.Resource
import com.l0124005.sewain_rpl.viewmodel.TransaksiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyRentalsProdukScreen(
    viewModel: TransaksiViewModel,
    token: String,
    transaksiId: Int,
    onBack: () -> Unit,
    onProductClick: (Int) -> Unit
) {
    val transaksiDetailState by viewModel.transaksiDetail.observeAsState()

    LaunchedEffect(transaksiId) {
        viewModel.getDetailTransaksi(token, transaksiId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Rental", fontWeight = FontWeight.Bold) },
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
                        MyRentalDetailContent(transaksi, onProductClick)
                    } else {
                        Text("Data tidak ditemukan", modifier = Modifier.align(Alignment.Center))
                    }
                }
                is Resource.Error -> {
                    Text(
                        text = transaksiDetailState?.message ?: "Terjadi kesalahan",
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                null -> {}
            }
        }
    }
}

@Composable
fun MyRentalDetailContent(
    transaksi: TransaksiData,
    onProductClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status Badge
        Surface(
            color = getStatusColor(transaksi.status).copy(alpha = 0.1f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Status Rental", fontSize = 12.sp, color = Color.Gray)
                Text(
                    transaksi.status.uppercase(),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = getStatusColor(transaksi.status)
                )
            }
        }

        // Product Summary Card
        Card(
            modifier = Modifier.fillMaxWidth().clickable {
                transaksi.barang?.id?.let { onProductClick(it) }
            },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                if (!transaksi.barang?.foto_barang.isNullOrEmpty()) {
                    AsyncImage(
                        model = "${ApiClient.IMAGE_BASE_URL}${transaksi.barang?.foto_barang}",
                        contentDescription = null,
                        modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)).background(Color.LightGray))
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(transaksi.barang?.nama_barang ?: "Barang", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("${transaksi.jumlah} unit", fontSize = 14.sp, color = Color.Gray)
                    Text("Rp ${formatRupiah(transaksi.barang?.harga_sewa ?: 0.0)} / hari", fontSize = 14.sp, color = Color(0xFF1E5276))
                }
            }
        }

        // Rental Period
        RentalInfoSection("Periode Sewa") {
            RentalInfoRow("Mulai", transaksi.tanggal_sewa)
            RentalInfoRow("Rencana Kembali", transaksi.tanggal_kembali_rencana)
            if (transaksi.tanggal_kembali_aktual != null) {
                RentalInfoRow("Tanggal Kembali", transaksi.tanggal_kembali_aktual)
            }
        }

        // Payment Details
        RentalInfoSection("Rincian Pembayaran") {
            RentalInfoRow("ID Transaksi", "#${transaksi.id}")
            RentalInfoRow("Biaya Sewa", "Rp ${formatRupiah(transaksi.total_harga)}")
            if (transaksi.total_denda > 0) {
                RentalInfoRow("Denda", "Rp ${formatRupiah(transaksi.total_denda)}", color = Color.Red)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray.copy(alpha = 0.3f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Dibayar", fontWeight = FontWeight.Bold)
                Text("Rp ${formatRupiah(transaksi.total_harga + transaksi.total_denda)}", fontWeight = FontWeight.Bold, color = Color(0xFF1F2A33))
            }
        }
    }
}

@Composable
fun RentalInfoSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
fun RentalInfoRow(label: String, value: String, color: Color = Color.Black) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = Color.Gray)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = color)
    }
}
