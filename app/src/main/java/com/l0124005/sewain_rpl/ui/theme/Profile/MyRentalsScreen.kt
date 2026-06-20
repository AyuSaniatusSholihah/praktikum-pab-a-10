package com.l0124005.sewain_rpl.ui.theme.profile

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
import com.l0124005.sewain_rpl.ui.theme.katalog.formatRupiah
import com.l0124005.sewain_rpl.utils.Resource
import com.l0124005.sewain_rpl.viewmodel.TransaksiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyRentalsScreen(
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
            TopAppBar(
                title = { Text("My Rentals", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
        ) {
            when (transaksiListState) {
                is Resource.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is Resource.Success -> {
                    val data = transaksiListState?.data?.data ?: emptyList()
                    if (data.isEmpty()) {
                        Text(
                            text = "Belum ada riwayat sewa",
                            modifier = Modifier.align(Alignment.Center),
                            color = Color.Gray
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(data) { transaksi ->
                                RentalItem(transaksi, onDetailClick)
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    Text(
                        text = transaksiListState?.message ?: "Terjadi kesalahan",
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                null -> {
                    // Initial state
                }
            }
        }
    }
}

@Composable
fun RentalItem(transaksi: TransaksiData, onClick: (Int) -> Unit) {
    Card(
        onClick = { onClick(transaksi.id) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!transaksi.barang?.foto_barang.isNullOrEmpty()) {
                    AsyncImage(
                        model = "${ApiClient.IMAGE_BASE_URL}${transaksi.barang?.foto_barang}",
                        contentDescription = null,
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.LightGray)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = transaksi.barang?.nama_barang ?: "Barang",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "ID #${transaksi.id}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = getStatusColor(transaksi.status).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = transaksi.status.uppercase(),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = getStatusColor(transaksi.status),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${transaksi.jumlah} unit × Rp ${formatRupiah(transaksi.barang?.harga_sewa ?: 0.0)}/hari",
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )
                    Text(
                        text = "${transaksi.tanggal_sewa} - ${transaksi.tanggal_kembali_rencana}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                color = Color(0xFFF0F0F0)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (transaksi.total_denda > 0) "Total + Denda" else "Total Pembayaran",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Rp ${formatRupiah(transaksi.total_harga + transaksi.total_denda)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2A33)
                )
            }
        }
    }
}

fun getStatusColor(status: String): Color {
    return when (status.lowercase()) {
        "pending" -> Color.Gray
        "menunggu pembayaran" -> Color(0xFFFFA500)
        "dibayar", "disewa" -> Color.Blue
        "dikembalikan" -> Color.Green
        "selesai" -> Color(0xFF006400)
        "dibatalkan" -> Color.Red
        else -> Color.Black
    }
}
