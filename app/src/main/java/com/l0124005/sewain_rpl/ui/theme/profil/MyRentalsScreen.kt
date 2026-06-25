package com.l0124005.sewain_rpl.ui.theme.profil

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import com.l0124005.sewain_rpl.utils.RentalStatus
import com.l0124005.sewain_rpl.network.ApiClient
import com.l0124005.sewain_rpl.network.TransaksiData
import com.l0124005.sewain_rpl.ui.theme.AbrilFatfaceFont
import com.l0124005.sewain_rpl.ui.theme.BluePrimary
import com.l0124005.sewain_rpl.ui.theme.DarkNavy
import com.l0124005.sewain_rpl.ui.theme.MidBlue
import com.l0124005.sewain_rpl.ui.theme.NavyPrimary
import com.l0124005.sewain_rpl.ui.theme.SewainTopBar
import com.l0124005.sewain_rpl.ui.theme.Sewain_rplTheme
import com.l0124005.sewain_rpl.ui.theme.TextMuted
import com.l0124005.sewain_rpl.ui.theme.VolkhovFont
import com.l0124005.sewain_rpl.utils.CurrencyUtils
import com.l0124005.sewain_rpl.utils.DateUtils
import com.l0124005.sewain_rpl.utils.Resource
import com.l0124005.sewain_rpl.viewmodel.ProfileViewModel
import com.l0124005.sewain_rpl.viewmodel.TransaksiViewModel
import kotlinx.coroutines.launch

// ── Warna tema -- SAMA PERSIS dengan ProfileScreen.kt / MyWalletScreen.kt biar konsisten ──
private val CardBlue   = Color(0xFF21394F)
private val LocalMidBlue = Color(0xFF4D6674)
private val LocalDarkNavy = Color(0xFF21394F)

// ── Warna badge status -- sesuai .dc-badge di CSS ──
// Background soft, text dark sesuai .dc-badge.active / .paid / .return dll.


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyRentalsScreen(
    viewModel: TransaksiViewModel,
    profileViewModel: ProfileViewModel,
    token: String,
    onBack: () -> Unit,
    onRentalClick: (Int) -> Unit,
    onRentalsOwnerClick: () -> Unit,
    onMyWalletClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onSettingsClick: () -> Unit = {}
) {
    val transaksiState by viewModel.transaksiList.observeAsState()
    val profileState by profileViewModel.profile.observeAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.getRiwayatTransaksi(token)
        profileViewModel.getProfile(token)
    }

    val userName = (profileState as? Resource.Success)?.data?.data?.name ?: "User"
    val userPhoto = (profileState as? Resource.Success)?.data?.data?.foto_profil

    val allTransactions = (transaksiState as? Resource.Success)?.data?.data ?: emptyList()
    
    // Pisahkan menggunakan logika RentalStatus yang baru
    val activeTransactions = allTransactions.filter {
        val s = RentalStatus.fromTransaksi(it)
        s == RentalStatus.ACTIVE || s == RentalStatus.UPCOMING || s == RentalStatus.RETURN
    }
    val historyTransactions = allTransactions.filter { 
        val s = RentalStatus.fromTransaksi(it)
        s == RentalStatus.COMPLETED || s == RentalStatus.CANCELED
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ProfileDrawerContent(
                userName = userName,
                userPhoto = userPhoto,
                currentScreen = "My Rentals",
                onProfileClick = {
                    scope.launch { drawerState.close() }
                    onBack()
                },
                onMyRentalsClick = {
                    scope.launch { drawerState.close() }
                },
                onRentalsOwnerClick = {
                    scope.launch { drawerState.close() }
                    onRentalsOwnerClick()
                },
                onMyWalletClick = {
                    scope.launch { drawerState.close() }
                    onMyWalletClick()
                },
                onLogoutClick = {
                    scope.launch { drawerState.close() }
                    onLogoutClick()
                },
                onSettingsClick = {
                    scope.launch { drawerState.close() }
                    onSettingsClick()
                }
            )
        }
    ) {
// SESUDAH — sama persis struktur ProfileScreen
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            when (val state = transaksiState) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = BluePrimary
                        )
                    }
                }
                is Resource.Error -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = state.message ?: "Error loading rentals",
                            color = Color.Red,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
                else -> {
                    MyRentalsContent(
                        userName = userName,
                        userPhoto = userPhoto,
                        active = activeTransactions,
                        history = historyTransactions,
                        onRentalClick = onRentalClick,
                        onMenuClick = { scope.launch { drawerState.open() } }
                    )
                }
            }
        }
    }
}

@Composable
private fun MyRentalsContent(
    userName: String,
    userPhoto: String?,
    active: List<TransaksiData>,
    history: List<TransaksiData>,
    onRentalClick: (Int) -> Unit,
    onMenuClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        SewainTopBar(
            navigationIcon = Icons.Default.Menu,
            onNavigationClick = onMenuClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(LocalMidBlue)
                    .padding(20.dp)
            ) {
                // ── Mini header: foto kecil + nama ──
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = RentalStatus.buildPhotoUrl(userPhoto, userName),
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(6.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(14.dp))
                    Text(
                        text = userName,
                        fontFamily = VolkhovFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }

                Spacer(Modifier.height(20.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(LocalDarkNavy)
                        .padding(24.dp)
                ) {
                    Text(
                        text = "My Rentals",
                        fontFamily = VolkhovFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = Color.White
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Barang yang sedang kamu sewa saat ini.",
                        fontFamily = VolkhovFont,
                        fontSize = 12.sp,
                        color = TextMuted
                    )

                    Spacer(Modifier.height(16.dp))
                    RentalCardsGrid(items = active, onItemClick = onRentalClick)

                    Spacer(Modifier.height(36.dp))

                    Text(
                        text = "My Rentals — History",
                        fontFamily = VolkhovFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = Color.White
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Riwayat penyewaan yang sudah selesai.",
                        fontFamily = VolkhovFont,
                        fontSize = 12.sp,
                        color = TextMuted
                    )

                    Spacer(Modifier.height(16.dp))
                    RentalCardsGrid(items = history, onItemClick = onRentalClick)
                }
            }
        }
    }
}

@Composable
private fun RentalCardsGrid(items: List<TransaksiData>, onItemClick: (Int) -> Unit) {
    if (items.isEmpty()) {
        Text(
            text = "Tidak ada data.",
            fontSize = 13.sp,
            color = TextMuted,
            modifier = Modifier.padding(vertical = 16.dp)
        )
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    rowItems.forEach { item ->
                        RentalCard(
                            item = item,
                            onItemClick = onItemClick,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun RentalCard(
    item: TransaksiData,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val status = RentalStatus.fromTransaksi(item)
    
    val formatTgl = { dateStr: String? ->
        DateUtils.formatDateForUI(dateStr)
    }
    
    val formatTglFull = { dateStr: String? ->
        DateUtils.formatFullDateForUI(dateStr)
    }

    val barang = item.barang
    val imageUrl = if (!barang?.foto_barang.isNullOrEmpty()) {
        if (barang?.foto_barang?.startsWith("http") == true) {
            barang.foto_barang
        } else {
            "${ApiClient.IMAGE_BASE_URL}${barang?.foto_barang}"
        }
    } else {
        "https://picsum.photos/seed/${barang?.id ?: 0}/300/200"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBlue)
            .clickable { onItemClick(item.id) }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 11f)
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = barang?.nama_barang,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            )

            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(status.badgeColor)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = status.label,
                    color = status.badgeTextColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = barang?.nama_barang ?: "Produk",
                fontFamily = VolkhovFont,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 2
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(Modifier.width(3.dp))
                Text(
                    text = barang?.lokasi ?: "Indonesia",
                    color = TextMuted,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = "Rp ${CurrencyUtils.formatRupiah(barang?.harga_sewa ?: 0.0)}/hari",
                fontFamily = AbrilFatfaceFont,
                fontSize = 16.sp,
                color = Color.White
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Sewa: ${formatTgl(item.tanggal_sewa)} s.d ${formatTgl(item.tanggal_kembali_rencana)}",
                    color = Color.White,
                    fontSize = 10.sp
                )
            }

            // Tampilkan info Return (Selalu tampilkan sesuai permintaan)
            val isReturned = !item.tanggal_kembali_aktual.isNullOrEmpty() && !item.tanggal_kembali_aktual.startsWith("0000")
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = if (isReturned) Color.White else Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(11.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = if (isReturned) {
                        "Return item: ${formatTglFull(item.tanggal_kembali_aktual)} WIB"
                    } else {
                        "Return item: ${formatTgl(item.tanggal_kembali_rencana)} 23:59 WIB"
                    },
                    color = if (isReturned) Color.White else Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp
                )
            }

            // Tambahan indikator urgensi jika status sudah RETURN tapi belum dikembalikan
            if (status == RentalStatus.RETURN && !isReturned) {
                Text(
                    text = "⚠ Segera kembalikan!",
                    color = Color(0xFFFFCC00),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.padding(start = 15.dp)
                )
            }
        }
    }
}
