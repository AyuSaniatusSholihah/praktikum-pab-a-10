package com.l0124005.sewain_rpl.ui.theme.katalog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.runtime.livedata.observeAsState
import com.l0124005.sewain_rpl.network.ApiClient
import com.l0124005.sewain_rpl.network.CatalogData
import com.l0124005.sewain_rpl.ui.theme.SewainTopBar
import com.l0124005.sewain_rpl.ui.theme.Sewain_rplTheme
import com.l0124005.sewain_rpl.ui.theme.BluePrimary
import com.l0124005.sewain_rpl.utils.Resource
import com.l0124005.sewain_rpl.viewmodel.KatalogViewModel
import com.l0124005.sewain_rpl.viewmodel.ProfileViewModel
import com.l0124005.sewain_rpl.ui.theme.VolkhovFont

// ═══════════════════════════════════════
// ACTIVITY
// ═══════════════════════════════════════

class MyKatalogActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Sewain_rplTheme {
                Text("Activity ini butuh ViewModel & Token")
            }
        }
    }
}

// ═══════════════════════════════════════
// MAIN SCREEN
// ═══════════════════════════════════════
@Composable
private fun MyKatalogTitleHeader() {
    Text(
        text = "My Katalogs",
        fontFamily = VolkhovFont,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        color = Black,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 0.dp, bottom = 4.dp)
    )
}

@Composable
fun MyKatalogScreen(
    viewModel: KatalogViewModel,
    profileViewModel: ProfileViewModel,
    token: String,
    onBack: () -> Unit,
    onAddItem: () -> Unit,
    onEditItem: (CatalogData) -> Unit,
    onItemClick: (CatalogData) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var itemToDelete by remember { mutableStateOf<CatalogData?>(null) }

    val myKatalogState by viewModel.myKatalog.observeAsState(Resource.Loading())
    val profileState by profileViewModel.profile.observeAsState()
    val deleteResult by viewModel.deleteResult.observeAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.getMyKatalog(token)
        profileViewModel.getProfile(token)
    }

    LaunchedEffect(deleteResult) {
        when (deleteResult) {
            is Resource.Success -> {
                snackbarHostState.showSnackbar("Barang berhasil dihapus")
                viewModel.getMyKatalog(token)
                viewModel.resetStates()
            }
            is Resource.Error -> {
                snackbarHostState.showSnackbar("Gagal menghapus: ${(deleteResult as Resource.Error).message}")
                viewModel.resetStates()
            }
            else -> {}
        }
    }

    itemToDelete?.let { barang ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Hapus Barang", fontWeight = FontWeight.Bold, color = Black) },
            text = { Text("Yakin ingin menghapus \"${barang.nama_barang}\"? Tindakan ini tidak bisa dibatalkan.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteKatalog(token, barang.id)
                        itemToDelete = null
                    }
                ) {
                    Text("Hapus", color = Color(0xFFE24B4A), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Batal", color = Primary)
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(White)
        ) {
            // Gunakan SewainTopBar tanpa navigation icon (panah)
            SewainTopBar(
                navigationIcon = null
            )
            
            MyKatalogTitleHeader()

            when (val state = myKatalogState) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BluePrimary)
                    }
                }
                is Resource.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message ?: "Terjadi kesalahan", color = Color.Red)
                    }
                }
                is Resource.Success -> {
                    val allItems = state.data?.data ?: emptyList()
                    val filteredItems = remember(searchQuery, allItems) {
                        if (searchQuery.isBlank()) allItems
                        else allItems.filter {
                            it.nama_barang.contains(searchQuery, ignoreCase = true) ||
                                    it.lokasi.contains(searchQuery, ignoreCase = true)
                        }
                    }

                    LazyVerticalGrid(
                        columns             = GridCells.Fixed(2),
                        modifier            = Modifier.fillMaxSize(),
                        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement   = Arrangement.spacedBy(10.dp)
                    ) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            val user = (profileState as? Resource.Success)?.data?.data
                            ProfileCard(
                                modifier = Modifier.padding(bottom = 10.dp),
                                nama          = user?.name ?: "User",
                                lokasi        = user?.alamat ?: (if (allItems.isNotEmpty()) allItems[0].lokasi else "Lokasi"),
                                rating        = 4.5f,
                                totalUlasan   = 12,
                                jumlahKatalog = allItems.size,
                                whatsapp      = user?.phone_number ?: "",
                                fotoProfil    = user?.foto_profil
                            )
                        }

                        item (span = { GridItemSpan(maxLineSpan) }) {
                            MyKatalogSearchBar(
                                query         = searchQuery,
                                onQueryChange = { searchQuery = it }
                            )
                        }

                        item (span = { GridItemSpan(maxLineSpan) }) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text       = "${filteredItems.size} Barang",
                                    fontSize   = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color      = Black,
                                    fontFamily = VolkhovFont
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text     = "di katalogmu",
                                    fontSize = 13.sp,
                                    color    = TextMuted
                                )
                            }
                        }

                        if (filteredItems.isEmpty()) {
                            item { AddItemButton(onClick = onAddItem) }
                            item (span = { GridItemSpan(maxLineSpan) })  { MyKatalogEmpty() }
                        } else {
                            item { AddItemButton(onClick = onAddItem) }
                            items(filteredItems) { item ->
                                MyKatalogCard(
                                    item            = item,
                                    onClick         = { onItemClick(item) },
                                    onEditClick     = { onEditItem(item) },
                                    onDeleteClick   = { itemToDelete = item }
                                )
                            }
                        }
                    }
                }
                else -> {}
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
        )
    }
}

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
                color    = Color(0xFF3D5278),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        },
        leadingIcon = {
            Icon(
                imageVector        = Icons.Outlined.Search,
                contentDescription = "Cari",
                tint               = Color(0xFF3D5278),
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
            .padding(vertical = 8.dp)
            .height(50.dp),
        colors     = OutlinedTextFieldDefaults.colors(
            focusedContainerColor   = Color(0xFFE0E0E0),
            unfocusedContainerColor = Color(0xFFE0E0E0),
            focusedBorderColor      = Color(0xFFC3D4E9),
            unfocusedBorderColor    = Color(0xFFC3D4E9),
            cursorColor             = Primary
        ),
        textStyle = androidx.compose.ui.text.TextStyle(
            fontSize   = 13.sp,
            color      = Primary,
            fontWeight = FontWeight.SemiBold
        )
    )
}

@Composable
private fun AddItemButton(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, Color(0xFFEDEDED))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f / 1.6f),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(Color(0xFFE0E0E0))
                        .border(BorderStroke(2.dp, Color(0xFF484848)), androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null,
                        tint = Color(0xFF484848),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "ADD NEW ITEM",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF484848),
                    fontFamily = VolkhovFont,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
private fun MyKatalogCard(
    item: CatalogData,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val isDisewa  = item.status == "disewa"
    val badgeBg   = if (isDisewa) Color(0xFF1E3A5F) else Color(0xFFC3D4E9)
    val badgeText = if (isDisewa) "ACTIVE RENTAL" else "AVAILABLE"
    val badgeTextColor = if (isDisewa) Color.White else Color(0xFF1E3A5F)
    val rating = 4
    val reviewCount = remember(item.id) { (10..50).random() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(386f / 240f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(UploadBg)
            ) {
                if (item.mainFotoUrl.isNotEmpty()) {
                    AsyncImage(
                        model = item.mainFotoUrl,
                        contentDescription = item.nama_barang,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Outlined.Inventory2,
                        null,
                        tint = Primary,
                        modifier = Modifier.size(32.dp).align(Alignment.Center)
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(badgeBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(text = badgeText, fontFamily = VolkhovFont, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = badgeTextColor, letterSpacing = 0.8.sp)
                }

                Row(
                    modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(modifier = Modifier.size(20.dp).clip(RoundedCornerShape(4.dp)).background(White).clickable { onEditClick() }, contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Edit, "Edit", tint = Primary, modifier = Modifier.size(12.dp))
                    }
                    Box(modifier = Modifier.size(20.dp).clip(RoundedCornerShape(4.dp)).background(White).clickable { onDeleteClick() }, contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Delete, "Hapus", tint = Color(0xFFE24B4A), modifier = Modifier.size(12.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(text = item.nama_barang, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Black, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 15.sp, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(4.dp))
                Text("★".repeat(rating) + "☆".repeat(5 - rating), color = Color(0xFFF5B800), fontSize = 10.sp)
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.LocationOn, null, Modifier.size(10.dp), tint = Color(0xFF55959E))
                Spacer(Modifier.width(3.dp))
                Text(item.lokasi, fontSize = 9.5.sp, color = TextMuted)
            }
            Text("($reviewCount) Customer Reviews", fontSize = 9.5.sp, color = Color(0xFF484848))
            Spacer(Modifier.height(4.dp))
            Text(text = "Rp ${formatRupiah(item.harga_sewa)}/hari", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = Black)
            Spacer(Modifier.height(6.dp))
            HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.Inventory, null, Modifier.size(12.dp), tint = TextMuted)
                Spacer(Modifier.width(5.dp))
                Text(text = "${item.stok} Units in Stock", fontSize = 10.sp, color = Color(0xFF484848))
            }
        }
    }
}

@Composable
private fun MyKatalogEmpty() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.size(80.dp).clip(androidx.compose.foundation.shape.CircleShape).background(UploadBg), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.Inventory2, null, tint = TextMuted, modifier = Modifier.size(36.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Belum ada barang", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Black, fontFamily = VolkhovFont)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Tambahkan barangmu untuk mulai menyewakan", fontSize = 12.sp, color = TextMuted, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Preview(showBackground = true)
@Composable
fun MyKatalogScreenPreview() {
    // Preview
}
