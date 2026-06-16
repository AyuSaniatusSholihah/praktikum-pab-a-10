package com.l0124005.sewain_rpl.ui.theme.transaksi

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.l0124005.sewain_rpl.utils.Resource
import com.l0124005.sewain_rpl.viewmodel.TransaksiViewModel

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
            TopAppBar(title = { Text("Riwayat Transaksi") })
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (transaksiListState) {
                is Resource.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
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
                else -> {}
            }
        }
    }
}

@Composable
fun TransaksiItem(transaksi: TransaksiData, onClick: (Int) -> Unit) {
    Card(
        onClick = { onClick(transaksi.id) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = transaksi.barang?.nama_barang ?: "Barang", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(text = "Status: ${transaksi.status}", color = getStatusColor(transaksi.status))
            Text(text = "Total: Rp ${transaksi.total_harga}")
            Text(text = "Sewa: ${transaksi.tanggal_sewa} s/d ${transaksi.tanggal_kembali_rencana}")
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
