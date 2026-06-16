package com.l0124005.sewain_rpl.ui.theme.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.l0124005.sewain_rpl.ui.theme.katalog.formatRupiah
import com.l0124005.sewain_rpl.ui.theme.transaksi.TransaksiItem
import com.l0124005.sewain_rpl.utils.Resource
import com.l0124005.sewain_rpl.viewmodel.TransaksiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerDashboardScreen(
    viewModel: TransaksiViewModel,
    token: String,
    onManageKatalog: () -> Unit,
    onTransaksiDetail: (Int) -> Unit
) {
    val dashboardState by viewModel.ownerDashboard.observeAsState()

    LaunchedEffect(Unit) {
        viewModel.getOwnerDashboard(token)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Dashboard Pemilik", fontWeight = FontWeight.Bold) })
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onManageKatalog,
                icon = { Icon(Icons.Default.Inventory, contentDescription = null) },
                text = { Text("Kelola Katalog") },
                containerColor = Color(0xFF5D8AA8),
                contentColor = Color.White
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (dashboardState) {
                is Resource.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is Resource.Success -> {
                    val data = dashboardState?.data?.data
                    if (data != null) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    StatCard(
                                        title = "Pendapatan",
                                        value = "Rp ${formatRupiah(data.total_saldo_pendapatan)}",
                                        icon = Icons.Default.Payments,
                                        modifier = Modifier.weight(1f)
                                    )
                                    StatCard(
                                        title = "Total Barang",
                                        value = "${data.total_barang}",
                                        icon = Icons.Default.Inventory,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            item {
                                Text("Transaksi Terbaru", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }

                            if (data.daftar_transaksi.isEmpty()) {
                                item {
                                    Text("Belum ada transaksi masuk", color = Color.Gray)
                                }
                            } else {
                                items(data.daftar_transaksi) { transaksi ->
                                    TransaksiItem(transaksi = transaksi, onClick = onTransaksiDetail)
                                }
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    Text(text = dashboardState?.message ?: "Error", color = Color.Red, modifier = Modifier.align(Alignment.Center))
                }
                else -> {}
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4F8))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = Color(0xFF5D8AA8))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontSize = 12.sp, color = Color.Gray)
            Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}
