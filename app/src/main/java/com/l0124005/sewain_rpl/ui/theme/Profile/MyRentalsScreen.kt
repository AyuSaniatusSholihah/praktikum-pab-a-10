package com.l0124005.sewain_rpl.ui.theme.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
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
import com.l0124005.sewain_rpl.utils.CurrencyUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyRentalsScreen(
    viewModel: TransaksiViewModel,
    token: String,
    onBack: () -> Unit
) {
    val transaksiState by viewModel.transaksiList.observeAsState()

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
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8F9FA))
        ) {
            when (transaksiState) {
                is Resource.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is Resource.Success -> {
                    val list = transaksiState?.data?.data ?: emptyList()
                    if (list.isEmpty()) {
                        Text("Belum ada riwayat sewa", modifier = Modifier.align(Alignment.Center), color = Color.Gray)
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(list) { transaksi ->
                                RentalItemCard(transaksi)
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    Text("Gagal memuat data: ${transaksiState?.message}", color = Color.Red, modifier = Modifier.align(Alignment.Center))
                }
                else -> {}
            }
        }
    }
}

@Composable
fun RentalItemCard(transaksi: TransaksiData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ID: #${transaksi.id}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Total: Rp ${CurrencyUtils.formatRupiah(transaksi.total_harga)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2A33)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = when(transaksi.status.lowercase()) {
                        "lunas" -> Color(0xFFE8F5E9)
                        "pending" -> Color(0xFFFFF3E0)
                        else -> Color(0xFFF5F5F5)
                    },
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = transaksi.status.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when(transaksi.status.lowercase()) {
                            "lunas" -> Color(0xFF2E7D32)
                            "pending" -> Color(0xFFEF6C00)
                            else -> Color.Gray
                        }
                    )
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
        }
    }
}
