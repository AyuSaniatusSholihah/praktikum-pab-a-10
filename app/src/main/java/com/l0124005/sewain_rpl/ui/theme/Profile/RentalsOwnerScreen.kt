package com.l0124005.sewain_rpl.ui.theme.profile

import androidx.compose.foundation.background
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
fun RentalsOwnerScreen(
    viewModel: TransaksiViewModel,
    token: String,
    onBack: () -> Unit
) {
    val dashboardState by viewModel.ownerDashboard.observeAsState()

    LaunchedEffect(Unit) {
        viewModel.getOwnerDashboard(token)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rentals Owner", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
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
            when (dashboardState) {
                is Resource.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is Resource.Success -> {
                    val list = dashboardState?.data?.data?.daftar_transaksi ?: emptyList()
                    if (list.isEmpty()) {
                        Text("Belum ada penyewaan barang Anda", modifier = Modifier.align(Alignment.Center), color = Color.Gray)
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(list) { transaksi ->
                                RentalOwnerItemCard(transaksi)
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    Text("Gagal memuat data: ${dashboardState?.message}", color = Color.Red, modifier = Modifier.align(Alignment.Center))
                }
                else -> {}
            }
        }
    }
}

@Composable
fun RentalOwnerItemCard(transaksi: TransaksiData) {
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
                    text = "ID: #${transaksi.id} - ${transaksi.user?.name ?: "Customer"}",
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
                        "selesai" -> Color(0xFFE8F5E9)
                        "disewa" -> Color(0xFFE3F2FD)
                        "menunggu_pembayaran" -> Color(0xFFFFF3E0)
                        else -> Color(0xFFF5F5F5)
                    },
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = transaksi.status.replace("_", " ").uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when(transaksi.status.lowercase()) {
                            "selesai" -> Color(0xFF2E7D32)
                            "disewa" -> Color(0xFF1976D2)
                            "menunggu_pembayaran" -> Color(0xFFEF6C00)
                            else -> Color.Gray
                        }
                    )
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
        }
    }
}
