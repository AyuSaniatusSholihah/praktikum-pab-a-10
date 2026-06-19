package com.l0124005.sewain_rpl.ui.theme.katalog

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.l0124005.sewain_rpl.network.ApiClient
import com.l0124005.sewain_rpl.network.CatalogData
import com.l0124005.sewain_rpl.utils.Resource
import com.l0124005.sewain_rpl.utils.CurrencyUtils
import com.l0124005.sewain_rpl.viewmodel.KatalogViewModel

private val BackgroundPage = Color(0xFFF8FAFB)
private val CardWhite      = Color(0xFFFFFFFF)
private val BorderColor    = Color(0xFFE8E8E8)

@Composable
fun RentalsScreen(
    viewModel: KatalogViewModel,
    onItemClick: (Int) -> Unit
) {
    var searchQuery    by remember { mutableStateOf("") }
    var activeKategori by remember { mutableStateOf("Semua") }
    
    val katalogState by viewModel.katalogPublik.observeAsState()
    val kategoriState by viewModel.kategori.observeAsState()

    LaunchedEffect(Unit) {
        viewModel.getKategori()
    }

    LaunchedEffect(activeKategori, searchQuery, kategoriState) {
        var kategoriId: Int? = null
        if (activeKategori != "Semua" && kategoriState is Resource.Success) {
            val kategoriListApi = kategoriState?.data?.data
            kategoriId = kategoriListApi?.find { it.nama_kategori == activeKategori }?.id
        }
        viewModel.getKatalogPublik(
            search = if (searchQuery.isEmpty()) null else searchQuery, 
            kategoriId = kategoriId
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPage)
    ) {
        RentalsTopBar()

        RentalsSearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it }
        )

        val categoriesForFilter = mutableListOf("Semua")
        if (kategoriState is Resource.Success) {
            kategoriState?.data?.data?.map { it.nama_kategori }?.let {
                categoriesForFilter.addAll(it)
            }
        }

        KategoriFilterRow(
            aktif = activeKategori,
            kategoriList = categoriesForFilter,
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
                        RentalsGrid(
                            items = products,
                            onItemClick = onItemClick
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
private fun RentalsTopBar() {
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
private fun RentalsSearchBar(
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
    aktif: String,
    kategoriList: List<String>,
    onKategoriSelected: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(kategoriList) { kat ->
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
private fun RentalsGrid(
    items: List<CatalogData>,
    onItemClick: (Int) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(items) { item ->
            RentalCard(
                item = item,
                onClick = { onItemClick(item.id) }
            )
        }
    }
}

@Composable
private fun RentalCard(
    item: CatalogData,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        border = BorderStroke(0.5.dp, BorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(UploadBg)
            ) {
                if (!item.foto_barang.isNullOrEmpty()) {
                    AsyncImage(
                        model = "${ApiClient.IMAGE_BASE_URL}${item.foto_barang}",
                        contentDescription = item.nama_barang,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(36.dp).align(Alignment.Center)
                    )
                }

                if (item.stok > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Primary)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Tersedia",
                            fontSize = 8.sp,
                            color = White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                Text(
                    text = item.nama_barang,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = item.lokasi,
                        fontSize = 10.sp,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = CurrencyUtils.formatRupiah(item.harga_sewa),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
                Text(
                    text = "/hari",
                    fontSize = 9.sp,
                    color = TextMuted
                )
            }
        }
    }
}
