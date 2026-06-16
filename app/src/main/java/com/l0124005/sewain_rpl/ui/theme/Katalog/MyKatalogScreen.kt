package com.l0124005.sewain_rpl.ui.theme.katalog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.l0124005.sewain_rpl.ui.theme.Sewain_rplTheme

// ═══════════════════════════════════════
// ACTIVITY
// ═══════════════════════════════════════

class MyKatalogActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Sewain_rplTheme {
                MyKatalogScreen(
                    onAddItemClick   = { /* TODO: navigasi ke AddItemActivity */ },
                    onItemClick      = { /* TODO: navigasi ke detail */ },
                    onEditItemClick  = { /* TODO: navigasi ke EditItemActivity */ },
                    onDeleteItemClick = { /* TODO: hapus item */ }
                )
            }
        }
    }
}

// ═══════════════════════════════════════
// DATA CLASS (sementara, ganti dengan response API nanti)
// ═══════════════════════════════════════

data class MyKatalogItem(
    val id: Int,
    val namaBarang: String,
    val lokasi: String,
    val hargaSewa: Double,
    val stok: Int,
    val status: String,       // "tersedia" atau "disewa"
    val fotoBarang: String?,
    val totalUlasan: Int,
    val rating: Float
)

// ═══════════════════════════════════════
// DUMMY DATA (hapus setelah endpoint siap)
// ═══════════════════════════════════════

private val dummyItems = listOf(
    MyKatalogItem(1, "Soleil Set Alat Masak Camping Nesting Aluminium",  "Kota Bandung", 50000.0,  10, "tersedia", null, 40, 4.0f),
    MyKatalogItem(2, "ALLTREK Tenda Camping 1 Bedroom + 1 Guest Room",   "Kota Bandung", 450000.0, 1,  "disewa",   null, 20, 4.0f),
    MyKatalogItem(3, "ALLTREK Set Meja Kursi Camping Gear Folding",      "Kota Bandung", 70000.0,  5,  "tersedia", null, 52, 3.0f),
    MyKatalogItem(4, "ALLTREK Storage Cookwear Tas Penyimpanan Camping", "Kota Bandung", 750000.0, 3,  "tersedia", null, 40, 4.0f),
    MyKatalogItem(5, "Sunrei Lampu Camping Wraith Hike and Ride",        "Kota Bandung", 25000.0,  1,  "disewa",   null, 30, 5.0f),
)

// ═══════════════════════════════════════
// MAIN SCREEN
// ═══════════════════════════════════════

@Composable
fun MyKatalogScreen(
    onAddItemClick: () -> Unit,
    onItemClick: (MyKatalogItem) -> Unit,
    onEditItemClick: (MyKatalogItem) -> Unit,
    onDeleteItemClick: (MyKatalogItem) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    // TODO: Ganti dummyItems dengan state dari ViewModel setelah endpoint siap
    val allItems = dummyItems
    val filteredItems = remember(searchQuery, allItems) {
        if (searchQuery.isBlank()) allItems
        else allItems.filter {
            it.namaBarang.contains(searchQuery, ignoreCase = true) ||
                    it.lokasi.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
    ) {
        // ── Top Bar ──
        MyKatalogTopBar()

        LazyColumn(
            modifier            = Modifier.fillMaxSize(),
            contentPadding      = PaddingValues(bottom = 80.dp)
        ) {
            // ── Profile Card ──
            item {
                ProfileCard(
                    modifier = Modifier.padding(16.dp),
                    nama          = "Camping Groups Bandung",
                    lokasi        = "Kota Bandung",
                    rating        = 4.3f,
                    totalUlasan   = 120,
                    jumlahKatalog = allItems.size,
                    whatsapp      = "0821-5620-9034"
                )
            }

            // ── Search Bar ──
            item {
                MyKatalogSearchBar(
                    query         = searchQuery,
                    onQueryChange = { searchQuery = it }
                )
            }

            // ── Tombol Tambah ──
            item {
                AddItemButton(onClick = onAddItemClick)
            }

            // ── Label jumlah ──
            item {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text       = "${filteredItems.size} Barang",
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = Black,
                        fontFamily = Volkhov
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text     = "di katalogmu",
                        fontSize = 13.sp,
                        color    = TextMuted
                    )
                }
            }

            // ── List Katalog ──
            if (filteredItems.isEmpty()) {
                item { MyKatalogEmpty() }
            } else {
                items(filteredItems) { item ->
                    MyKatalogCard(
                        item            = item,
                        onClick         = { onItemClick(item) },
                        onEditClick     = { onEditItemClick(item) },
                        onDeleteClick   = { onDeleteItemClick(item) }
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════
// TOP BAR
// ═══════════════════════════════════════

@Composable
private fun MyKatalogTopBar() {
    Column {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text          = "SEWA",
                    fontSize      = 22.sp,
                    fontWeight    = FontWeight.Bold,
                    fontFamily    = Volkhov,
                    color         = Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text          = "IN",
                    fontSize      = 22.sp,
                    fontWeight    = FontWeight.Bold,
                    fontFamily    = Volkhov,
                    color         = Primary,
                    letterSpacing = 1.sp
                )
            }

            Text(
                text       = "My Katalog",
                fontSize   = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color      = Black,
                fontFamily = Volkhov
            )

            Icon(
                imageVector        = Icons.Outlined.MoreVert,
                contentDescription = "Menu",
                tint               = Black,
                modifier           = Modifier.size(22.dp)
            )
        }
        HorizontalDivider(color = UploadBorder, thickness = 0.5.dp)
    }
}

// ═══════════════════════════════════════
// SEARCH BAR
// ═══════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyKatalogSearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value         = query,
        onValueChange = onQueryChange,
        placeholder   = {
            Text(
                text     = "Search something here",
                color    = Primary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        },
        leadingIcon = {
            Icon(
                imageVector        = Icons.Outlined.Search,
                contentDescription = "Cari",
                tint               = Primary,
                modifier           = Modifier.size(20.dp)
            )
        },
        trailingIcon = if (query.isNotEmpty()) {
            {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector        = Icons.Outlined.Close,
                        contentDescription = "Hapus",
                        tint               = TextMuted,
                        modifier           = Modifier.size(18.dp)
                    )
                }
            }
        } else null,
        singleLine = true,
        shape      = RoundedCornerShape(999.dp),
        modifier   = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(50.dp),
        colors     = OutlinedTextFieldDefaults.colors(
            focusedContainerColor   = UploadBg,
            unfocusedContainerColor = UploadBg,
            focusedBorderColor      = UploadBorder,
            unfocusedBorderColor    = UploadBorder,
            cursorColor             = Primary
        ),
        textStyle = androidx.compose.ui.text.TextStyle(
            fontSize   = 13.sp,
            color      = Primary,
            fontWeight = FontWeight.SemiBold
        )
    )
}

// ═══════════════════════════════════════
// TOMBOL TAMBAH ITEM
// ═══════════════════════════════════════

@Composable
private fun AddItemButton(onClick: () -> Unit) {
    Button(
        onClick   = onClick,
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(50.dp),
        shape     = RoundedCornerShape(14.dp),
        colors    = ButtonDefaults.buttonColors(containerColor = Primary)
    ) {
        Icon(
            imageVector        = Icons.Outlined.Add,
            contentDescription = null,
            tint               = Color.White,
            modifier           = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text       = "Tambah Barang Baru",
            fontSize   = 14.sp,
            fontWeight = FontWeight.Bold,
            color      = Color.White,
            fontFamily = Volkhov
        )
    }
}

// ═══════════════════════════════════════
// CARD ITEM KATALOG
// ═══════════════════════════════════════

@Composable
private fun MyKatalogCard(
    item: MyKatalogItem,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val isDisewa  = item.status == "disewa"
    val badgeBg   = if (isDisewa) Primary else UploadBorder
    val badgeText = if (isDisewa) "ACTIVE RENTAL" else "AVAILABLE"
    val badgeTextColor = if (isDisewa) Color.White else Primary

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = White),
        border    = BorderStroke(0.5.dp, UploadBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier          = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Gambar barang ──
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(UploadBg)
            ) {
                if (!item.fotoBarang.isNullOrEmpty()) {
                    AsyncImage(
                        model              = item.fotoBarang,
                        contentDescription = item.namaBarang,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector        = Icons.Outlined.Inventory2,
                        contentDescription = null,
                        tint               = Primary,
                        modifier           = Modifier
                            .size(32.dp)
                            .align(Alignment.Center)
                    )
                }
            }

            // ── Info ──
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Badge status
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(badgeBg)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text       = badgeText,
                        fontSize   = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color      = badgeTextColor,
                        letterSpacing = 0.5.sp
                    )
                }

                // Nama barang
                Text(
                    text       = item.namaBarang,
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = Black,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis,
                    fontFamily = Volkhov
                )

                // Lokasi
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector        = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint               = Primary,
                        modifier           = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(text = item.lokasi, fontSize = 10.sp, color = TextMuted)
                }

                // Harga
                Text(
                    text       = "Rp ${formatHarga(item.hargaSewa)}/hari",
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Primary,
                    fontFamily = Volkhov
                )

                // Stok
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector        = Icons.Outlined.Inventory,
                        contentDescription = null,
                        tint               = TextMuted,
                        modifier           = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text     = "${item.stok} unit in stock",
                        fontSize = 10.sp,
                        color    = TextMuted
                    )
                }
            }

            // ── Tombol aksi ──
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // Tombol Edit
                IconButton(
                    onClick  = onEditClick,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(0.5.dp, UploadBorder, RoundedCornerShape(8.dp))
                        .background(White)
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.Edit,
                        contentDescription = "Edit",
                        tint               = Primary,
                        modifier           = Modifier.size(16.dp)
                    )
                }

                // Tombol Hapus
                IconButton(
                    onClick  = onDeleteClick,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(0.5.dp, UploadBorder, RoundedCornerShape(8.dp))
                        .background(White)
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.Delete,
                        contentDescription = "Hapus",
                        tint               = Color(0xFFE24B4A),
                        modifier           = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════
// EMPTY STATE
// ═══════════════════════════════════════

@Composable
private fun MyKatalogEmpty() {
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier         = Modifier
                .size(80.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(UploadBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = Icons.Outlined.Inventory2,
                contentDescription = null,
                tint               = TextMuted,
                modifier           = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text       = "Belum ada barang",
            fontSize   = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color      = Black,
            fontFamily = Volkhov
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text      = "Tambahkan barangmu untuk mulai menyewakan",
            fontSize  = 12.sp,
            color     = TextMuted,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

// ═══════════════════════════════════════
// HELPER
// ═══════════════════════════════════════

private fun formatHarga(harga: Double): String =
    String.format("%,.0f", harga).replace(",", ".")

@Preview(showBackground = true)
@Composable
fun MyKatalogScreenPreview() {
    MyKatalogScreen(
        onAddItemClick = {},
        onItemClick = {},
        onEditItemClick = {},
        onDeleteItemClick = {}
    )
}
