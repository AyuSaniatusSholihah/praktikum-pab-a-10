package com.l0124005.sewain_rpl.ui.theme

import android.content.Intent
import com.l0124005.sewain_rpl.ui.theme.katalog.DetailProdukActivity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.l0124005.sewain_rpl.network.ApiClient
import com.l0124005.sewain_rpl.network.CatalogData
import com.l0124005.sewain_rpl.repository.KatalogRepository
import com.l0124005.sewain_rpl.utils.Resource
import com.l0124005.sewain_rpl.viewmodel.KatalogViewModel
import com.l0124005.sewain_rpl.viewmodel.KatalogViewModelFactory
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.l0124005.sewain_rpl.repository.KeranjangRepository
import com.l0124005.sewain_rpl.utils.SessionManager
import com.l0124005.sewain_rpl.viewmodel.KeranjangViewModel
import com.l0124005.sewain_rpl.viewmodel.KeranjangViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ═══════════════════════════════════════
// ACTIVITY
// ═══════════════════════════════════════

class RentalsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sessionManager = SessionManager(this)
        val token = sessionManager.getToken()

        setContent {
            Sewain_rplTheme {
                val katalogViewModel: KatalogViewModel = viewModel(factory = KatalogViewModelFactory(KatalogRepository()))
                val keranjangViewModel: KeranjangViewModel = viewModel(factory = KeranjangViewModelFactory(KeranjangRepository()))

                RentalsScreen(
                    viewModel = katalogViewModel,
                    keranjangViewModel = keranjangViewModel,
                    token = token,
                    onItemClick = { barang ->
                        val intent = Intent(this@RentalsActivity, DetailProdukActivity::class.java).apply {
                            putExtra("EXTRA_PRODUCT_ID", barang.id)
                        }
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

// ═══════════════════════════════════════
// WARNA TEMA SEWAIN
// ═══════════════════════════════════════

private val HomeBluePrimary    = BluePrimary
private val HomeBlueLight      = Color(0xFFEEF3F7)
private val HomeNavyPrimary    = NavyPrimary
private val HomeBackgroundPage = Color(0xFFF8FAFB)
private val HomeCardWhite      = Color(0xFFFFFFFF)
private val HomeTextPrimary    = Color(0xFF1A1A1A)
private val HomeTextMuted      = Color(0xFF8A8A8A)
private val HomeBorderColor    = Color(0xFFE8E8E8)

// ═══════════════════════════════════════
// MAIN SCREEN
// ═══════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RentalsScreen(
    viewModel: KatalogViewModel,
    keranjangViewModel: KeranjangViewModel,
    token: String,
    onItemClick: (CatalogData) -> Unit
) {
    val context = LocalContext.current
    val addToCartState by keranjangViewModel.addToCartState.observeAsState()
    val kategoriResult by viewModel.kategori.observeAsState()

    LaunchedEffect(Unit) {
        viewModel.getKategori()
    }

    LaunchedEffect(addToCartState) {
        if (addToCartState is Resource.Success) {
            Toast.makeText(context, "Berhasil ditambahkan ke keranjang!", Toast.LENGTH_SHORT).show()
            keranjangViewModel.resetActionStates()
        } else if (addToCartState is Resource.Error) {
            Toast.makeText(context, "Gagal: ${addToCartState?.message}", Toast.LENGTH_SHORT).show()
            keranjangViewModel.resetActionStates()
        }
    }

    var searchQuery    by remember { mutableStateOf("") }
    var activeKategori by remember { mutableStateOf("Semua") }

    val katalogState by viewModel.katalogPublik.observeAsState(Resource.Loading())

    // Fetch ulang setiap kali filter berubah
    LaunchedEffect(searchQuery, activeKategori, kategoriResult) {
        val cats = (kategoriResult as? Resource.Success)?.data?.data ?: emptyList()
        val katId = if (activeKategori == "Semua") {
            null
        } else {
            cats.find { it.nama_kategori == activeKategori }?.id
        }
        val q = searchQuery.ifBlank { null }
        viewModel.getKatalogPublik(search = q, kategoriId = katId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HomeBackgroundPage)
    ) {
        // ── Top Bar ──
        com.l0124005.sewain_rpl.ui.theme.SewainTopBar()

        // ── Search Bar ──
        RentalsSearchBar(
            query    = searchQuery,
            onQueryChange = { searchQuery = it }
        )

        // ── Filter Kategori ──
        val availableCategories = (kategoriResult as? Resource.Success)?.data?.data?.map { it.nama_kategori } ?: emptyList()
        val fullCategories = listOf("Semua") + availableCategories

        KategoriFilterRow(
            categories = fullCategories,
            aktif        = activeKategori,
            onKategoriSelected = { activeKategori = it }
        )

        // ── Konten ──
        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = katalogState) {
                is Resource.Loading -> {
                    CircularProgressIndicator(
                        color    = HomeBluePrimary,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is Resource.Error -> {
                    Column(
                        modifier            = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text      = "😔 Gagal memuat data",
                            fontSize  = 14.sp,
                            color     = HomeTextMuted
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text     = state.message ?: "Terjadi kesalahan",
                            fontSize = 12.sp,
                            color    = HomeTextMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = {
                                viewModel.getKategori()
                                viewModel.getKatalogPublik()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = HomeBluePrimary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HomeBluePrimary)
                        ) {
                            Text("Coba Lagi")
                        }
                    }
                }

                is Resource.Success -> {
                    val list = state.data?.data ?: emptyList()
                    if (list.isEmpty()) {
                        EmptyState(modifier = Modifier.align(Alignment.Center))
                    } else {
                        RentalsGrid(
                            items       = list,
                            onItemClick = onItemClick,
                            onAddToCart = { barang ->
                                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                val today = sdf.format(Date())
                                val tomorrow = sdf.format(Date(System.currentTimeMillis() + 86400000))
                                keranjangViewModel.addToKeranjang(token, barang.id, 1, today, tomorrow)
                            }
                        )
                    }
                }

                else -> {}
            }
        }
    }
}

// ═══════════════════════════════════════
// TOP BAR
// ═══════════════════════════════════════

@Composable
private fun RentalsTopBar() {
    Row(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment   = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Logo
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text       = "SEWA",
                fontSize   = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color      = HomeTextPrimary,
                letterSpacing = 1.sp
            )
            Text(
                text       = "IN",
                fontSize   = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color      = HomeBluePrimary,
                letterSpacing = 1.sp
            )
        }

        // Judul halaman
        Text(
            text       = "Rentals",
            fontSize   = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color      = HomeTextPrimary
        )

        // Icon filter
        Icon(
            imageVector        = Icons.Default.Tune,
            contentDescription = "Filter",
            tint               = HomeNavyPrimary,
            modifier           = Modifier.size(22.dp)
        )
    }

    // Divider tipis
    HorizontalDivider(color = HomeBorderColor, thickness = 0.5.dp)
}

// ═══════════════════════════════════════
// SEARCH BAR
// ═══════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RentalsSearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value            = query,
        onValueChange    = onQueryChange,
        placeholder      = { Text("Cari barang sewa...", color = HomeTextMuted, fontSize = 13.sp) },
        leadingIcon      = {
            Icon(
                imageVector        = Icons.Default.Search,
                contentDescription = "Cari",
                tint               = HomeBluePrimary,
                modifier           = Modifier.size(20.dp)
            )
        },
        trailingIcon     = if (query.isNotEmpty()) {
            {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector        = Icons.Default.Close,
                        contentDescription = "Hapus",
                        tint               = HomeTextMuted,
                        modifier           = Modifier.size(18.dp)
                    )
                }
            }
        } else null,
        singleLine       = true,
        shape            = RoundedCornerShape(14.dp),
        modifier         = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .height(50.dp),
        colors           = OutlinedTextFieldDefaults.colors(
            focusedContainerColor   = HomeCardWhite,
            unfocusedContainerColor = HomeCardWhite,
            focusedBorderColor      = HomeBluePrimary,
            unfocusedBorderColor    = HomeBorderColor,
            cursorColor             = HomeBluePrimary
        ),
        textStyle = androidx.compose.ui.text.TextStyle(
            fontSize = 13.sp,
            color    = HomeTextPrimary
        )
    )
}

// ═══════════════════════════════════════
// FILTER KATEGORI ROW
// ═══════════════════════════════════════

@Composable
private fun KategoriFilterRow(
    categories: List<String>,
    aktif: String,
    onKategoriSelected: (String) -> Unit
) {
    LazyRow(
        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { kat ->
            val isActive = kat == aktif
            Box(
                contentAlignment = Alignment.Center,
                modifier         = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isActive) HomeBluePrimary else HomeCardWhite)
                    .let {
                        if (!isActive) it.border(0.5.dp, HomeBorderColor, RoundedCornerShape(20.dp))
                        else it
                    }
                    .clickable { onKategoriSelected(kat) }
                    .padding(horizontal = 16.dp, vertical = 7.dp)
            ) {
                Text(
                    text       = kat,
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color      = if (isActive) Color.White else HomeTextMuted
                )
            }
        }
    }
}

// ═══════════════════════════════════════
// GRID PRODUK
// ═══════════════════════════════════════

@Composable
private fun RentalsGrid(
    items: List<CatalogData>,
    onItemClick: (CatalogData) -> Unit,
    onAddToCart: (CatalogData) -> Unit
) {
    LazyVerticalGrid(
        columns             = GridCells.Fixed(2),
        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement   = Arrangement.spacedBy(10.dp),
        modifier              = Modifier.fillMaxSize()
    ) {
        items(items) { barang ->
            RentalCard(
                barang  = barang,
                onClick = { onItemClick(barang) },
                onAddToCart = { onAddToCart(barang) }
            )
        }
    }
}

// ═══════════════════════════════════════
// CARD BARANG
// ═══════════════════════════════════════

@Composable
private fun RentalCard(
    barang: CatalogData,
    onClick: () -> Unit,
    onAddToCart: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape    = RoundedCornerShape(14.dp),
        colors   = CardDefaults.cardColors(containerColor = HomeCardWhite),
        border   = androidx.compose.foundation.BorderStroke(0.5.dp, HomeBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            // ── Gambar barang ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(HomeBlueLight)
            ) {
                if (!barang.foto_barang.isNullOrEmpty()) {
                    val imageUrl = if (barang.foto_barang.startsWith("http")) {
                        barang.foto_barang
                    } else {
                        "${ApiClient.IMAGE_BASE_URL}${barang.foto_barang}"
                    }
                    AsyncImage(
                        model               = imageUrl,
                        contentDescription  = barang.nama_barang,
                        contentScale        = ContentScale.Crop,
                        modifier            = Modifier.fillMaxSize()
                    )
                } else {
                    // Placeholder jika tidak ada foto
                    Box(
                        modifier            = Modifier.fillMaxSize(),
                        contentAlignment    = Alignment.Center
                    ) {
                        Icon(
                            imageVector        = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint               = HomeBluePrimary,
                            modifier           = Modifier.size(36.dp)
                        )
                    }
                }

                // Badge status
                if (barang.status == "tersedia") {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(HomeBluePrimary)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text     = "Tersedia",
                            fontSize = 8.sp,
                            color    = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // ── Info barang ──
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                Text(
                    text       = barang.nama_barang,
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = HomeTextPrimary,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                // Lokasi
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector        = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint               = HomeBluePrimary,
                        modifier           = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text     = barang.lokasi,
                        fontSize = 10.sp,
                        color    = HomeTextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Harga & Action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text       = "Rp ${formatHarga(barang.harga_sewa)}",
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color      = HomeBluePrimary
                        )
                        Text(
                            text     = "/hari",
                            fontSize = 9.sp,
                            color    = HomeTextMuted
                        )
                    }

                    IconButton(
                        onClick = onAddToCart,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(HomeBluePrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddShoppingCart,
                            contentDescription = "Tambah ke Keranjang",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════
// EMPTY STATE
// ═══════════════════════════════════════

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier            = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "😔", fontSize = 40.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text       = "Belum ada barang",
            fontSize   = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color      = HomeTextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text     = "Coba ganti kata kunci\natau kategori lainnya",
            fontSize = 12.sp,
            color    = HomeTextMuted,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

// ═══════════════════════════════════════
// HELPER
// ═══════════════════════════════════════

private fun formatHarga(harga: Double): String {
    return String.format(Locale.getDefault(), "%,.0f", harga).replace(",", ".")
}
