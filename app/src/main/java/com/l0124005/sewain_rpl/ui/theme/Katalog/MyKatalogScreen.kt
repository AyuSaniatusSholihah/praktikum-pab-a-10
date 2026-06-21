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
import androidx.compose.runtime.livedata.observeAsState
import com.l0124005.sewain_rpl.network.ApiClient
import com.l0124005.sewain_rpl.network.CatalogData
import com.l0124005.sewain_rpl.ui.theme.SewainTopBar
import com.l0124005.sewain_rpl.ui.theme.Sewain_rplTheme
import com.l0124005.sewain_rpl.ui.theme.BluePrimary
import com.l0124005.sewain_rpl.utils.Resource
import com.l0124005.sewain_rpl.viewmodel.KatalogViewModel
import com.l0124005.sewain_rpl.viewmodel.ProfileViewModel
import com.l0124005.sewain_rpl.ui.theme.katalog.formatRupiah
import com.l0124005.sewain_rpl.ui.theme.katalog.ProfileCard
import com.l0124005.sewain_rpl.ui.theme.katalog.Volkhov
import com.l0124005.sewain_rpl.ui.theme.katalog.Primary
import com.l0124005.sewain_rpl.ui.theme.katalog.White
import com.l0124005.sewain_rpl.ui.theme.katalog.Black
import com.l0124005.sewain_rpl.ui.theme.katalog.TextMuted
import com.l0124005.sewain_rpl.ui.theme.katalog.UploadBg
import com.l0124005.sewain_rpl.ui.theme.katalog.UploadBorder

// ═══════════════════════════════════════
// ACTIVITY
// ═══════════════════════════════════════

class MyKatalogActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Sewain_rplTheme {
                // Mencoba memanggil dengan parameter yang benar jika ini untuk testing mandiri
                // Namun MyKatalogActivity sepertinya tidak benar-benar digunakan jika navigasi via MainActivity
                Text("Activity ini butuh ViewModel & Token")
            }
        }
    }
}

// ═══════════════════════════════════════
// MAIN SCREEN
// ═══════════════════════════════════════

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
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.getMyKatalog(token)
        profileViewModel.getProfile(token)
    }

    // Refresh list setelah delete berhasil
    LaunchedEffect(deleteResult) {
        when (deleteResult) {
            is Resource.Success -> {
                snackbarHostState.showSnackbar("Barang berhasil dihapus")
                viewModel.getMyKatalog(token)
            }
            is Resource.Error -> {
                snackbarHostState.showSnackbar("Gagal menghapus: ${(deleteResult as Resource.Error).message}")
            }
            else -> {}
        }
    }

    // Dialog konfirmasi hapus
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

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(White)
        ) {
            // ── Top Bar ──
            SewainTopBar()

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

                    LazyColumn(
                        modifier            = Modifier.fillMaxSize(),
                        contentPadding      = PaddingValues(bottom = 80.dp)
                    ) {
                        // ── Profile Card ──
                        item {
                            val user = (profileState as? Resource.Success)?.data?.data
                            ProfileCard(
                                modifier = Modifier.padding(16.dp),
                                nama          = user?.name ?: "Loading...",
                                lokasi        = user?.alamat ?: (if (allItems.isNotEmpty()) allItems[0].lokasi else "Unknown"),
                                rating        = 4.5f,
                                totalUlasan   = 12,
                                jumlahKatalog = allItems.size,
                                whatsapp      = user?.phone_number ?: ""
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
                            AddItemButton(onClick = onAddItem)
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
    }
}

// ═══════════════════════════════════════
// TOP BAR
// ═══════════════════════════════════════

@Composable
private fun MyKatalogTopBar(onBack: () -> Unit) {
    Column {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Kembali",
                    tint = Black
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
    item: CatalogData,
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
                if (!item.foto_barang.isNullOrEmpty()) {
                    val imageUrl = if (item.foto_barang.startsWith("http")) {
                        item.foto_barang
                    } else {
                        "${ApiClient.IMAGE_BASE_URL}${item.foto_barang}"
                    }
                    AsyncImage(
                        model              = imageUrl,
                        contentDescription = item.nama_barang,
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
                    text       = item.nama_barang,
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
                    text       = "Rp ${formatRupiah(item.harga_sewa)}/hari",
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

@Preview(showBackground = true)
@Composable
fun MyKatalogScreenPreview() {
    // MyKatalogScreen(...)
}
