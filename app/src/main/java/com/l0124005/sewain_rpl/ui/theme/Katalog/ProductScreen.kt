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

private val BackgroundPage = Color(0xFFF8FAFB)
private val CardWhite      = Color(0xFFFFFFFF)
private val BorderColor    = Color(0xFFE8E8E8)

@Composable
fun ProductScreen(
    token: String,
    viewModel: KatalogViewModel,
    keranjangViewModel: KeranjangViewModel,
    onItemClick: (Int) -> Unit
) {
    val context = LocalContext.current
    var searchQuery    by remember { mutableStateOf("") }
    var activeKategori by remember { mutableStateOf("Semua") }
    
    val katalogState by viewModel.katalogPublik.observeAsState()
    val kategoriResult by viewModel.kategori.observeAsState()

    LaunchedEffect(Unit) {
        viewModel.getKategori()
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPage)
    ) {
        ProductTopBar()

        ProductSearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it }
        )

        val availableCategories = (kategoriResult as? Resource.Success)?.data?.data?.map { it.nama_kategori } ?: emptyList()
        val fullCategories = listOf("Semua") + availableCategories

        KategoriFilterRow(
            categories = fullCategories,
            aktif = activeKategori,
            onKategoriSelected = { activeKategori = it }
        )

        Box(modifier = Modifier.weight(1f)) {
            when (katalogState) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Primary)
                    }
                }
                is Resource.Success -> {
                    val products = katalogState?.data?.data ?: emptyList()
                    if (products.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Tidak ada barang ditemukan")
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
                                Toast.makeText(context, "${product.nama_barang} ditambahkan ke keranjang", Toast.LENGTH_SHORT).show()
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
}

@Composable
private fun ProductTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "SEWA",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Volkhov,
                color = Black,
                letterSpacing = 1.sp
            )
            Text(
                text = "IN",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Volkhov,
                color = Primary,
                letterSpacing = 1.sp
            )
        }

        Text(
            text = "Rentals",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Black
        )

        Icon(
            imageVector = Icons.Default.Tune,
            contentDescription = "Filter",
            tint = Primary,
            modifier = Modifier.size(22.dp)
        )
    }
    HorizontalDivider(color = BorderColor, thickness = 0.5.dp)
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
                tint = Primary,
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
            focusedBorderColor = Primary,
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
                    .background(if (isActive) Primary else CardWhite)
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
