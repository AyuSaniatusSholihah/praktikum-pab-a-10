package com.l0124005.sewain_rpl.ui.theme.keranjang

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import com.l0124005.sewain_rpl.network.KeranjangItem
import com.l0124005.sewain_rpl.repository.TransaksiRepository
import com.l0124005.sewain_rpl.ui.theme.katalog.formatRupiah
import com.l0124005.sewain_rpl.utils.Resource
import com.l0124005.sewain_rpl.viewmodel.KeranjangViewModel
import com.l0124005.sewain_rpl.viewmodel.TransaksiViewModel
import com.l0124005.sewain_rpl.viewmodel.TransaksiViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeranjangScreen(
    token: String,
    viewModel: KeranjangViewModel,
    onBack: () -> Unit
) {
    val keranjangState by viewModel.keranjang.observeAsState()
    val transaksiViewModel: TransaksiViewModel = viewModel(
        factory = TransaksiViewModelFactory(TransaksiRepository())
    )
    val checkoutState by transaksiViewModel.checkoutState.observeAsState()

    LaunchedEffect(Unit) {
        viewModel.getKeranjang(token)
    }

    LaunchedEffect(checkoutState) {
        if (checkoutState is Resource.Success) {
            viewModel.getKeranjang(token)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Keranjang Saya", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            if (keranjangState is Resource.Success) {
                val keranjangData = keranjangState?.data?.data
                val totalBayar = keranjangData?.total_estimasi ?: 0.0

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Pembayaran", fontWeight = FontWeight.Medium)
                            Text(
                                "Rp ${formatRupiah(totalBayar)}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = Color(0xFF1E5276)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { transaksiViewModel.checkout(token) },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D8AA8)),
                            enabled = checkoutState !is Resource.Loading
                        ) {
                            if (checkoutState is Resource.Loading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text("Checkout", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        when (keranjangState) {
            is Resource.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF5D8AA8))
                }
            }
            is Resource.Success -> {
                val items = keranjangState?.data?.data?.items ?: emptyList()
                if (items.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Keranjang Anda kosong.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .background(Color(0xFFF8F9FA)),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(items) { item ->
                            KeranjangItemCard(
                                item = item,
                                onDelete = { viewModel.removeKeranjangItem(token, item.id) }
                            )
                        }
                    }
                }
            }
            is Resource.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Error: ${keranjangState?.message}", color = Color.Red)
                }
            }
            else -> {}
        }
    }
}

@Composable
fun KeranjangItemCard(item: KeranjangItem, onDelete: () -> Unit) {
    val imageUrl = if (item.barang.foto_barang != null) "${ApiClient.IMAGE_BASE_URL}products/${item.barang.foto_barang}" else null

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray)
            ) {
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = item.barang.nama_barang,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.barang.nama_barang,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "${item.jumlah} Unit x Rp ${formatRupiah(item.barang.harga_sewa)}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Rp ${formatRupiah(item.subtotal)}",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E5276),
                    fontSize = 14.sp
                )
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.Red.copy(alpha = 0.7f))
            }
        }
    }
}
