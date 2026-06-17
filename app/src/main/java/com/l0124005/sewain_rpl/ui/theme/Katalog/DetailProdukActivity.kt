package com.l0124005.sewain_rpl.ui.theme.katalog

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.l0124005.sewain_rpl.repository.KatalogRepository
import com.l0124005.sewain_rpl.repository.KeranjangRepository
import com.l0124005.sewain_rpl.ui.theme.Sewain_rplTheme
import com.l0124005.sewain_rpl.utils.Resource
import com.l0124005.sewain_rpl.utils.SessionManager
import com.l0124005.sewain_rpl.viewmodel.KatalogViewModel
import com.l0124005.sewain_rpl.viewmodel.KatalogViewModelFactory
import com.l0124005.sewain_rpl.viewmodel.KeranjangViewModel
import com.l0124005.sewain_rpl.viewmodel.KeranjangViewModelFactory

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetailProdukActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val productId = intent.getIntExtra("EXTRA_PRODUCT_ID", -1)
        
        setContent {
            Sewain_rplTheme {
                val sessionManager = SessionManager(this)
                val token = sessionManager.getToken()
                
                val katalogViewModel: KatalogViewModel = viewModel(
                    factory = KatalogViewModelFactory(KatalogRepository())
                )
                val keranjangViewModel: KeranjangViewModel = viewModel(
                    factory = KeranjangViewModelFactory(KeranjangRepository())
                )
                
                DetailProdukScreen(
                    productId = productId,
                    token = token,
                    katalogViewModel = katalogViewModel,
                    keranjangViewModel = keranjangViewModel,
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailProdukScreen(
    productId: Int,
    token: String,
    katalogViewModel: KatalogViewModel,
    keranjangViewModel: KeranjangViewModel,
    onBack: () -> Unit
) {
    // Menggunakan myKatalogDetail sesuai dengan property di KatalogViewModel
    val detailState by katalogViewModel.myKatalogDetail.observeAsState()
    val keranjangState by keranjangViewModel.keranjang.observeAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (productId != -1) {
            // Menggunakan getMyKatalogDetail sesuai dengan method di KatalogViewModel
            katalogViewModel.getMyKatalogDetail(token, productId)
        }
    }
    
    // Observasi status tambah ke keranjang
    LaunchedEffect(keranjangState) {
        if (keranjangState is Resource.Success) {
            Toast.makeText(context, "Berhasil ditambahkan ke keranjang!", Toast.LENGTH_SHORT).show()
        } else if (keranjangState is Resource.Error) {
            Toast.makeText(context, "Gagal: ${keranjangState?.message}", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Produk", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            if (detailState is Resource.Success) {
                BottomAppBar(
                    containerColor = Color.White,
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Button(
                        onClick = {
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val today = sdf.format(Date())
                            val tomorrow = sdf.format(Date(System.currentTimeMillis() + 86400000))
                            keranjangViewModel.addToKeranjang(token, productId, 1, today, tomorrow)
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D8AA8)),
                        enabled = keranjangState !is Resource.Loading
                    ) {
                        if (keranjangState is Resource.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Sewa Sekarang", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    ) { padding ->
        when (detailState) {
            is Resource.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF5D8AA8))
                }
            }
            is Resource.Success -> {
                val product = detailState!!.data!!.data
                val baseUrl = com.l0124005.sewain_rpl.network.ApiClient.IMAGE_BASE_URL
                val imageUrl = if (product.foto_barang != null) baseUrl + product.foto_barang else null

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Image Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .background(Color.LightGray)
                    ) {
                        if (imageUrl != null) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = product.nama_barang,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = product.nama_barang,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2A2A2A)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Rp ${formatRupiah(product.harga_sewa)} / hari",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E5276)
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        // Info Section
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = product.lokasi, color = Color.Gray, fontSize = 14.sp)
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Surface(
                                color = Color(0xFFE3F2FD),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "Stok: ${product.stok}",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 12.sp,
                                    color = Color(0xFF1976D2),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(text = "Deskripsi Produk", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = product.deskripsi ?: "Tidak ada deskripsi untuk produk ini.",
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = Color.DarkGray
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Additional Info
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F4F9)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = "Informasi Tambahan", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = "• Jaminan: Rp ${formatRupiah(product.harga_jaminan)}", fontSize = 13.sp)
                                Text(text = "• Denda Keterlambatan: Rp ${formatRupiah(product.harga_denda_perjam)} / jam", fontSize = 13.sp)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
            is Resource.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Gagal memuat detail produk: ${detailState?.message}", color = Color.Red)
                }
            }
            else -> {}
        }
    }
}
