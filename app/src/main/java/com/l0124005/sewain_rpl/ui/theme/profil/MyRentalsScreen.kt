package com.l0124005.sewain_rpl.ui.theme.profil

import androidx.compose.foundation.background
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
import coil.compose.AsyncImage
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
import com.l0124005.sewain_rpl.utils.CurrencyUtils.formatRupiah
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
private val StatusActiveBg    = Color(0xFFC3D4E9)
private val StatusActiveText  = Color(0xFF1e3a5f)
private val StatusUpcomingBg  = Color(0xFFe8f4ea)
private val StatusUpcomingText = Color(0xFF1f7a47)
private val StatusCompletedBg  = Color(0xFFd1f0dd)
private val StatusCompletedText = Color(0xFF1f7a47)
private val StatusCanceledBg  = Color(0xFFffcdd2)
private val StatusCanceledText = Color(0xFFc62828)
private val StatusReturnBg   = Color(0xFFffe3c4)
private val StatusReturnText  = Color(0xFFa35a17)

enum class RentalStatus(
    val label: String,
    val badgeColor: Color,
    val badgeTextColor: Color
) {
    ACTIVE("Active Rent", StatusActiveBg, StatusActiveText),
    UPCOMING("Upcoming Rent", StatusUpcomingBg, StatusUpcomingText),
    COMPLETED("Completed Rent", StatusCompletedBg, StatusCompletedText),
    CANCELED("Canceled Rent", StatusCanceledBg, StatusCanceledText),
    RETURN("Return Rent", StatusReturnBg, StatusReturnText);

    companion object {
        fun fromBackend(status: String): RentalStatus {
            return when (status.lowercase()) {
                "sedang_disewa", "menunggu_kembali" -> ACTIVE
                "menunggu_pembayaran", "sudah_dibayar" -> UPCOMING
                "selesai" -> COMPLETED
                "dibatalkan", "expired" -> CANCELED
                "menunggu_verifikasi" -> RETURN
                else -> COMPLETED
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyRentalsScreen(
    viewModel: TransaksiViewModel,
    profileViewModel: ProfileViewModel,
    token: String,
    onBack: () -> Unit,
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

    val allTransactions = (transaksiState as? Resource.Success)?.data?.data ?: emptyList()
    
    // Pisahkan Active (termasuk upcoming/return) dan History (selesai/batal)
    val activeTransactions = allTransactions.filter { 
        it.status !in listOf("selesai", "dibatalkan", "expired") 
    }
    val historyTransactions = allTransactions.filter { 
        it.status in listOf("selesai", "dibatalkan", "expired") 
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ProfileDrawerContent(
                userName = userName,
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
        Scaffold(
            containerColor = Color.White
        ) { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                when (val state = transaksiState) {
                    is Resource.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = BluePrimary
                        )
                    }
                    is Resource.Error -> {
                        Text(
                            text = state.message ?: "Error loading rentals",
                            color = Color.Red,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    else -> {
                        MyRentalsContent(
                            active = activeTransactions,
                            history = historyTransactions,
                            onMenuClick = { scope.launch { drawerState.open() } }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MyRentalsContent(
    active: List<TransaksiData>,
    history: List<TransaksiData>,
    onMenuClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        SewainTopBar(
            navigationIcon = Icons.Default.Menu,
            onNavigationClick = onMenuClick
        )

        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(LocalMidBlue)
                    .padding(20.dp)
            ) {
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
                    RentalCardsGrid(items = active)

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
                    RentalCardsGrid(items = history)
                }
            }
        }
    }
}

@Composable
private fun RentalCardsGrid(items: List<TransaksiData>) {
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
private fun RentalCard(item: TransaksiData, modifier: Modifier = Modifier) {
    val status = RentalStatus.fromBackend(item.status)
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
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
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
                    fontSize = 11.sp
                )
            }

            Text(
                text = "Rp ${formatRupiah(barang?.harga_sewa ?: 0.0)}/hari",
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
                    text = "Sewa: ${item.tanggal_sewa} - ${item.tanggal_kembali_rencana}",
                    color = Color.White,
                    fontSize = 10.sp
                )
            }

            if (!item.tanggal_kembali_aktual.isNullOrEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Return: ${item.tanggal_kembali_aktual}",
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }
            } else if (status == RentalStatus.ACTIVE || status == RentalStatus.UPCOMING) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.45f),
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Return: Belum dijadwalkan",
                        color = Color.White.copy(alpha = 0.45f),
                        fontSize = 10.sp,
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }
    }
}
