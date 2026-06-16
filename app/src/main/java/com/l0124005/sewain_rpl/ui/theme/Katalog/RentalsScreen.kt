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
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage

// Reuse colors and fonts from KatalogComponents
private val BackgroundPage = Color(0xFFF8FAFB)
private val CardWhite      = Color(0xFFFFFFFF)
private val BorderColor    = Color(0xFFE8E8E8)
// removed local TealPrimary and TealLight to use Primary from KatalogComponents

private val kategoriList = listOf(
    "Semua", "Kamera", "Outdoor", "Elektronik", "Olahraga", "Fashion", "Lainnya"
)

// Mock Data Model to keep it frontend-only and independent for now
data class MockCatalogItem(
    val id: Int,
    val name: String,
    val price: Double,
    val location: String,
    val imageRes: String = "",
    val status: String = "tersedia"
)

@Composable
fun RentalsScreen(
    onItemClick: (MockCatalogItem) -> Unit = {}
) {
    var searchQuery    by remember { mutableStateOf("") }
    var activeKategori by remember { mutableStateOf("Semua") }

    val mockItems = listOf(
        MockCatalogItem(1, "Tas Carrier 60L", 50000.0, "Bandung"),
        MockCatalogItem(2, "Tenda Dome 4P", 75000.0, "Jakarta"),
        MockCatalogItem(3, "Kamera Sony A6400", 150000.0, "Surabaya"),
        MockCatalogItem(4, "Sepeda Gunung", 100000.0, "Bandung"),
    )

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

        KategoriFilterRow(
            aktif = activeKategori,
            onKategoriSelected = { activeKategori = it }
        )

        RentalsGrid(
            items = mockItems,
            onItemClick = onItemClick
        )
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
        ),
        textStyle = androidx.compose.ui.text.TextStyle(
            fontSize = 13.sp,
            color = TextDark
        )
    )
}

@Composable
private fun KategoriFilterRow(
    aktif: String,
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
    items: List<MockCatalogItem>,
    onItemClick: (MockCatalogItem) -> Unit
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
                onClick = { onItemClick(item) }
            )
        }
    }
}

@Composable
private fun RentalCard(
    item: MockCatalogItem,
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
                Icon(
                    imageVector = Icons.Default.Inventory2,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(36.dp).align(Alignment.Center)
                )

                if (item.status == "tersedia") {
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
                    text = item.name,
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
                        text = item.location,
                        fontSize = 10.sp,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Rp ${String.format("%,.0f", item.price).replace(",", ".")}",
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

@Preview(showBackground = true)
@Composable
fun RentalsScreenPreview() {
    RentalsScreen()
}
