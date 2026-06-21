package com.l0124005.sewain_rpl.ui.theme.katalog

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.l0124005.sewain_rpl.network.ApiClient
import com.l0124005.sewain_rpl.network.CatalogData
import com.l0124005.sewain_rpl.network.KategoriData
import com.l0124005.sewain_rpl.utils.Resource
import com.l0124005.sewain_rpl.viewmodel.KatalogViewModel
import com.l0124005.sewain_rpl.viewmodel.KeranjangViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

import androidx.compose.ui.tooling.preview.Preview
import com.l0124005.sewain_rpl.ui.theme.*
import com.l0124005.sewain_rpl.network.KatalogListResponse
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import com.l0124005.sewain_rpl.ui.theme.VidalokaFont
private val BackgroundPage = Color(0xFFF8FAFB)
private val CardWhite      = Color(0xFFFFFFFF)
private val BorderColor    = Color(0xFFE8E8E8)

// Range harga -- disamakan persis dengan priceRanges di rentals.html
private data class HargaRange(val label: String, val min: Long, val max: Long)

private val hargaRanges = listOf(
    HargaRange("Rp 0 - Rp 100.000", 0, 100_000),
    HargaRange("Rp 100.000 - Rp 250.000", 100_000, 250_000),
    HargaRange("Rp 250.000 - Rp 500.000", 250_000, 500_000),
    HargaRange("Rp 500.000 - Rp 750.000", 500_000, 750_000),
    HargaRange("Rp 750.000 - Rp 1.000.000", 750_000, 1_000_000),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(
    token: String,
    viewModel: KatalogViewModel,
    keranjangViewModel: KeranjangViewModel,
    initialSearchQuery: String = "",
    initialCategory: String = "Semua",
    onItemClick: (Int) -> Unit
) {
    val context = LocalContext.current
    var searchQuery     by remember { mutableStateOf(initialSearchQuery) }
    var activeKategori  by remember { mutableStateOf(initialCategory) }
    var activeHarga     by remember { mutableStateOf<HargaRange?>(null) }
    var showFilterSheet by remember { mutableStateOf(false) }

    val katalogState by viewModel.katalogPublik.observeAsState()
    val kategoriResult by viewModel.kategori.observeAsState()

    val addToCartState by keranjangViewModel.addToCartState.observeAsState()

    // Sync searchQuery with initialSearchQuery when navigating from Home
    LaunchedEffect(initialSearchQuery) {
        searchQuery = initialSearchQuery
    }
    
    // Sync activeKategori with initialCategory when navigating from Home
    LaunchedEffect(initialCategory) {
        activeKategori = initialCategory
    }

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

    LaunchedEffect(activeKategori, searchQuery, kategoriResult) {
        val cats = (kategoriResult as? Resource.Success)?.data?.data ?: emptyList()
        val kategoriId = if (activeKategori == "Semua") {
            null
        } else {
            cats.find { it.nama_kategori == activeKategori }?.id
        }
        viewModel.getKatalogPublik(search = if (searchQuery.isEmpty()) null else searchQuery, kategoriId = kategoriId)
    }

    val availableCategories = (kategoriResult as? Resource.Success)?.data?.data?.map { it.nama_kategori } ?: emptyList()
    val fullCategories = listOf("Semua") + availableCategories

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPage)
    ) {
        ProductTopBar(onFilterClick = { showFilterSheet = true })

        ProductSearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it }
        )

        KategoriFilterRow(
            categories = fullCategories,
            aktif = activeKategori,
            onKategoriSelected = { activeKategori = it }
        )

        Box(modifier = Modifier.weight(1f)) {
            when (katalogState) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BluePrimary)
                    }
                }
                is Resource.Success -> {
                    val allProducts = katalogState?.data?.data ?: emptyList()

                    // Filter harga dilakukan di sisi klien (sama seperti getFiltered() di rentals.html).
                    // PENTING: ganti `item.harga` di bawah ini kalau nama field di CatalogData kamu
                    // bukan "harga" (misalnya harga_sewa / harga_per_hari).
                    val products = allProducts.filter { item ->
                        val harga = item.harga_sewa
                        activeHarga == null || (harga >= activeHarga!!.min && harga <= activeHarga!!.max)
                    }

                    if (products.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Tidak ada barang ditemukan", color = TextMuted)
                        }
                    } else {
                        ProductGrid(
                            items = products,
                            onItemClick = { id ->
                                Log.d("PRODUCT_SCREEN", "Clicking product ID: $id")
                                onItemClick(id)
                            },
                            onAddToCart = { product ->
                                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                val calendar = Calendar.getInstance()
                                val tglSewa = sdf.format(calendar.time)

                                calendar.add(Calendar.DAY_OF_YEAR, 1)
                                val tglKembali = sdf.format(calendar.time)

                                keranjangViewModel.addToKeranjang(
                                    token = token,
                                    barangId = product.id,
                                    jumlah = 1,
                                    tglSewa = tglSewa,
                                    tglKembali = tglKembali
                                )
                            }
                        )
                    }
                }
                is Resource.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Gagal memuat data: ${katalogState?.message}", color = Color.Red)
                    }
                }
                else -> {}
            }
        }
    }

    if (showFilterSheet) {
        FilterBottomSheet(
            categories = availableCategories,
            activeKategori = activeKategori,
            activeHarga = activeHarga,
            onKategoriSelected = { activeKategori = it },
            onHargaSelected = { activeHarga = it },
            onReset = {
                activeKategori = "Semua"
                activeHarga = null
            },
            onApply = { showFilterSheet = false },
            onDismiss = { showFilterSheet = false }
        )
    }
}

@Composable
private fun ProductTopBar(onFilterClick: () -> Unit) {
    SewainTopBar(
        actionIcon = Icons.Default.Tune,
        actionTint = NavyPrimary,
        onActionClick = { onFilterClick() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductSearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Cari barang sewa...", color = TextMuted, fontSize = 13.sp) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Cari",
                tint = BluePrimary,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = if (query.isNotEmpty()) {
            {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Hapus",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        } else null,
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .height(50.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = CardWhite,
            unfocusedContainerColor = CardWhite,
            focusedBorderColor = BluePrimary,
            unfocusedBorderColor = BorderColor,
        )
    )
}

@Composable
private fun KategoriFilterRow(
    categories: List<String>,
    aktif: String,
    onKategoriSelected: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { kat ->
            val isActive = kat == aktif
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isActive) BluePrimary else CardWhite)
                    .clickable { onKategoriSelected(kat) }
                    .border(
                        width = if (isActive) 0.dp else 0.5.dp,
                        color = if (isActive) Color.Transparent else BorderColor,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 7.dp)
            ) {
                Text(
                    text = kat,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isActive) White else TextMuted
                )
            }
        }
    }
}

@Composable
private fun ProductGrid(
    items: List<CatalogData>,
    onItemClick: (Int) -> Unit,
    onAddToCart: (CatalogData) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(items) { item ->
            ProductCard(
                product = item,
                onClick = { onItemClick(item.id) },
                onAddToCart = { onAddToCart(item) }
            )
        }
    }
}

/**
 * Bottom sheet filter -- konten & gaya disamakan dengan section ".filters-section"
 * di rentals.html (judul Volkhov bold, daftar harga, grid kategori).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterBottomSheet(
    categories: List<String>,
    activeKategori: String,
    activeHarga: HargaRange?,
    onKategoriSelected: (String) -> Unit,
    onHargaSelected: (HargaRange?) -> Unit,
    onReset: () -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CardWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            Text(
                text = "Filters",
                fontFamily = Volkhov,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Black,
                modifier = Modifier.padding(bottom = 18.dp)
            )

            // ---- Harga ----
            Text(
                text = "Harga",
                fontFamily = Volkhov,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            hargaRanges.forEach { range ->
                val isActive = range == activeHarga
                Text(
                    text = range.label,
                    fontSize = 14.sp,
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isActive) Black else TextMuted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onHargaSelected(if (isActive) null else range) }
                        .padding(vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ---- Kategori ----
            Text(
                text = "Kategori",
                fontFamily = Volkhov,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            categories.chunked(3).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    row.forEach { kat ->
                        val isActive = kat == activeKategori
                        Text(
                            text = kat,
                            fontSize = 14.sp,
                            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isActive) Black else TextMuted,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onKategoriSelected(if (isActive) "Semua" else kat) }
                                .padding(vertical = 6.dp)
                        )
                    }
                    repeat(3 - row.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, BorderColor),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Black)
                ) {
                    Text("Reset")
                }
                Button(
                    onClick = onApply,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary, contentColor = White)
                ) {
                    Text("Terapkan")
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProductScreenFullPreview() {
    Sewain_rplTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundPage)
        ) {
            // Rentals Top Bar
            ProductTopBar(onFilterClick = {})

            // Search Bar
            ProductSearchBar(
                query = "",
                onQueryChange = {}
            )

            // Category Filter
            val dummyCats = listOf("Semua", "Kamera", "Tenda", "Musik", "Elektronik")
            KategoriFilterRow(
                categories = dummyCats,
                aktif = "Semua",
                onKategoriSelected = {}
            )

            // Dummy Data untuk Grid
            val dummyItems = listOf(
                CatalogData(
                    id = 1,
                    user_id = 1,
                    kategori_id = 1,
                    nama_barang = "Kamera Sony A7 II",
                    deskripsi = "Kamera Mirrorless Full Frame",
                    harga_sewa = 150000.0,
                    harga_jaminan = 500000.0,
                    harga_denda_perjam = 20000.0,
                    stok = 1,
                    lokasi = "Surakarta",
                    foto_barang = null,
                    status = "tersedia"
                ),
                CatalogData(
                    id = 2,
                    user_id = 1,
                    kategori_id = 2,
                    nama_barang = "Tenda Camping 4P",
                    deskripsi = "Tenda kapasitas 4 orang anti badai",
                    harga_sewa = 75000.0,
                    harga_jaminan = 200000.0,
                    harga_denda_perjam = 10000.0,
                    stok = 2,
                    lokasi = "Solo",
                    foto_barang = null,
                    status = "tersedia"
                ),
                CatalogData(
                    id = 3,
                    user_id = 1,
                    kategori_id = 1,
                    nama_barang = "Lensa Sony 50mm",
                    deskripsi = "Lensa fix bokeh",
                    harga_sewa = 50000.0,
                    harga_jaminan = 150000.0,
                    harga_denda_perjam = 5000.0,
                    stok = 3,
                    lokasi = "Klaten",
                    foto_barang = null,
                    status = "tersedia"
                ),
                CatalogData(
                    id = 4,
                    user_id = 1,
                    kategori_id = 3,
                    nama_barang = "Gitar Akustik Yamaha",
                    deskripsi = "Gitar akustik suara jernih",
                    harga_sewa = 40000.0,
                    harga_jaminan = 100000.0,
                    harga_denda_perjam = 4000.0,
                    stok = 1,
                    lokasi = "Boyolali",
                    foto_barang = null,
                    status = "tersedia"
                )
            )

            // Product Grid
            ProductGrid(
                items = dummyItems,
                onItemClick = {},
                onAddToCart = {}
            )
        }
    }
}