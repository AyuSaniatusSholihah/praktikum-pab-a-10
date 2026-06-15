package com.l0124005.sewain_rpl.ui.theme

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
import java.util.Locale

// ═══════════════════════════════════════
// ACTIVITY
// ═══════════════════════════════════════

class RentalsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Sewain_rplTheme {
                val factory = KatalogViewModelFactory(KatalogRepository())
                val viewModel: KatalogViewModel = viewModel(factory = factory)

                RentalsScreen(
                    viewModel = viewModel,
                    onItemClick = { _ ->
                        // TODO: navigasi ke detail
                    }
                )
            }
        }
    }
}

// ═══════════════════════════════════════
// WARNA TEMA SEWAIN
// ═══════════════════════════════════════

private val TealPrimary    = Color(0xFF1D9E75)
private val TealLight      = Color(0xFFE1F5EE)
private val NavyPrimary    = Color(0xFF285473)
private val BackgroundPage = Color(0xFFF8FAFB)
private val CardWhite      = Color(0xFFFFFFFF)
private val TextPrimary    = Color(0xFF1A1A1A)
private val TextMuted      = Color(0xFF8A8A8A)
private val BorderColor    = Color(0xFFE8E8E8)

// ═══════════════════════════════════════
// DAFTAR KATEGORI FILTER
// ═══════════════════════════════════════

private val kategoriList = listOf(
    "Semua", "Kamera", "Outdoor", "Elektronik", "Olahraga", "Fashion", "Lainnya"
)

// ═══════════════════════════════════════
// MAIN SCREEN
// ═══════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RentalsScreen(
    viewModel: KatalogViewModel,
    onItemClick: (CatalogData) -> Unit
) {
    var searchQuery    by remember { mutableStateOf("") }
    var activeKategori by remember { mutableStateOf("Semua") }

    val katalogState by viewModel.katalogPublik.observeAsState(Resource.Loading())

    // Fetch ulang setiap kali filter berubah
    LaunchedEffect(searchQuery, activeKategori) {
        val kat = if (activeKategori == "Semua") null else activeKategori.lowercase()
        val q   = searchQuery.ifBlank { null }
        viewModel.getKatalogPublik(search = q, kategori = kat)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPage)
    ) {
        // ── Top Bar ──
        RentalsTopBar()

        // ── Search Bar ──
        RentalsSearchBar(
            query    = searchQuery,
            onQueryChange = { searchQuery = it }
        )

        // ── Filter Kategori ──
        KategoriFilterRow(
            aktif        = activeKategori,
            onKategoriSelected = { activeKategori = it }
        )

        // ── Konten ──
        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = katalogState) {
                is Resource.Loading -> {
                    CircularProgressIndicator(
                        color    = TealPrimary,
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
                            color     = TextMuted
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text     = state.message ?: "",
                            fontSize = 12.sp,
                            color    = TextMuted
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = {
                                val kat = if (activeKategori == "Semua") null else activeKategori.lowercase()
                                viewModel.getKatalogPublik(kategori = kat)
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TealPrimary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, TealPrimary)
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
                            onItemClick = onItemClick
                        )
                    }
                }
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
                color      = TextPrimary,
                letterSpacing = 1.sp
            )
            Text(
                text       = "IN",
                fontSize   = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color      = TealPrimary,
                letterSpacing = 1.sp
            )
        }

        // Judul halaman
        Text(
            text       = "Rentals",
            fontSize   = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color      = TextPrimary
        )

        // Icon filter
        Icon(
            imageVector        = Icons.Default.Tune,
            contentDescription = "Filter",
            tint               = NavyPrimary,
            modifier           = Modifier.size(22.dp)
        )
    }

    // Divider tipis
    HorizontalDivider(color = BorderColor, thickness = 0.5.dp)
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
        placeholder      = { Text("Cari barang sewa...", color = TextMuted, fontSize = 13.sp) },
        leadingIcon      = {
            Icon(
                imageVector        = Icons.Default.Search,
                contentDescription = "Cari",
                tint               = TealPrimary,
                modifier           = Modifier.size(20.dp)
            )
        },
        trailingIcon     = if (query.isNotEmpty()) {
            {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector        = Icons.Default.Close,
                        contentDescription = "Hapus",
                        tint               = TextMuted,
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
            focusedContainerColor   = CardWhite,
            unfocusedContainerColor = CardWhite,
            focusedBorderColor      = TealPrimary,
            unfocusedBorderColor    = BorderColor,
            cursorColor             = TealPrimary
        ),
        textStyle = androidx.compose.ui.text.TextStyle(
            fontSize = 13.sp,
            color    = TextPrimary
        )
    )
}

// ═══════════════════════════════════════
// FILTER KATEGORI ROW
// ═══════════════════════════════════════

@Composable
private fun KategoriFilterRow(
    aktif: String,
    onKategoriSelected: (String) -> Unit
) {
    LazyRow(
        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(kategoriList) { kat ->
            val isActive = kat == aktif
            Box(
                contentAlignment = Alignment.Center,
                modifier         = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isActive) TealPrimary else CardWhite)
                    .let {
                        if (!isActive) it.border(0.5.dp, BorderColor, RoundedCornerShape(20.dp))
                        else it
                    }
                    .clickable { onKategoriSelected(kat) }
                    .padding(horizontal = 16.dp, vertical = 7.dp)
            ) {
                Text(
                    text       = kat,
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color      = if (isActive) Color.White else TextMuted
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
    onItemClick: (CatalogData) -> Unit
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
                onClick = { onItemClick(barang) }
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
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape    = RoundedCornerShape(14.dp),
        colors   = CardDefaults.cardColors(containerColor = CardWhite),
        border   = androidx.compose.foundation.BorderStroke(0.5.dp, BorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            // ── Gambar barang ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(TealLight)
            ) {
                if (!barang.foto_barang.isNullOrEmpty()) {
                    AsyncImage(
                        model               = "${ApiClient.IMAGE_BASE_URL}${barang.foto_barang}",
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
                            tint               = TealPrimary,
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
                            .background(TealPrimary)
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
                    color      = TextPrimary,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                // Lokasi
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector        = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint               = TealPrimary,
                        modifier           = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text     = barang.lokasi,
                        fontSize = 10.sp,
                        color    = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Harga
                Text(
                    text       = "Rp ${formatHarga(barang.harga_sewa)}",
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color      = TealPrimary
                )
                Text(
                    text     = "/hari",
                    fontSize = 9.sp,
                    color    = TextMuted
                )
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
            color      = TextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text     = "Coba ganti kata kunci\natau kategori lainnya",
            fontSize = 12.sp,
            color    = TextMuted,
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
