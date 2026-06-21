package com.l0124005.sewain_rpl.ui.theme.profil

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.l0124005.sewain_rpl.network.ApiClient
import com.l0124005.sewain_rpl.network.TransaksiData
import com.l0124005.sewain_rpl.ui.theme.AbrilFatfaceFont
import com.l0124005.sewain_rpl.ui.theme.MontaguSlabFont
import com.l0124005.sewain_rpl.ui.theme.BluePrimary
import com.l0124005.sewain_rpl.ui.theme.DarkNavy
import com.l0124005.sewain_rpl.ui.theme.MidBlue
import com.l0124005.sewain_rpl.ui.theme.NavyPrimary
import com.l0124005.sewain_rpl.ui.theme.SewainTopBar
import com.l0124005.sewain_rpl.ui.theme.TextMuted
import com.l0124005.sewain_rpl.ui.theme.VolkhovFont
import com.l0124005.sewain_rpl.utils.CurrencyUtils.formatRupiah
import com.l0124005.sewain_rpl.utils.Resource
import com.l0124005.sewain_rpl.viewmodel.ProfileViewModel
import com.l0124005.sewain_rpl.viewmodel.TransaksiViewModel
import kotlinx.coroutines.launch

private val LocalMidBlue = Color(0xFF4D6674)
private val LocalDarkNavy = Color(0xFF21394F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RentalsOwnerScreen(
    viewModel: TransaksiViewModel,
    profileViewModel: ProfileViewModel,
    token: String,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onProfileClick: () -> Unit,
    onMyRentalClick: () -> Unit,
    onWalletClick: () -> Unit,
    onRentalClick: (TransaksiData) -> Unit,
    onSettingsClick: () -> Unit = {}
) {
    val dashboardState by viewModel.ownerDashboard.observeAsState()
    val profileState by profileViewModel.profile.observeAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.getOwnerDashboard(token)
        profileViewModel.getProfile(token)
    }

    val userName = (profileState as? Resource.Success)?.data?.data?.name ?: "User"
    val avatarUrl = (profileState as? Resource.Success)?.data?.data?.foto_profil

    val dashboardData = (dashboardState as? Resource.Success)?.data?.data
    val allTransactions = dashboardData?.daftar_transaksi ?: emptyList()

    val activeRentals = allTransactions.filter { 
        it.status !in listOf("selesai", "dibatalkan", "expired") 
    }
    val historyRentals = allTransactions.filter { 
        it.status in listOf("selesai", "dibatalkan", "expired") 
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ProfileDrawerContent(
                userName = userName,
                currentScreen = "Rentals Owner",
                onProfileClick = {
                    scope.launch { drawerState.close() }
                    onProfileClick()
                },
                onMyRentalsClick = {
                    scope.launch { drawerState.close() }
                    onMyRentalClick()
                },
                onRentalsOwnerClick = {
                    scope.launch { drawerState.close() }
                },
                onMyWalletClick = {
                    scope.launch { drawerState.close() }
                    onWalletClick()
                },
                onLogoutClick = {
                    scope.launch { drawerState.close() }
                    onLogout()
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
                when (dashboardState) {
                    is Resource.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = BluePrimary
                        )
                    }
                    is Resource.Error -> {
                        Text(
                            text = (dashboardState as Resource.Error).message ?: "Error",
                            color = Color.Red,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    else -> {
                        RentalsOwnerContent(
                            userName = userName,
                            avatarUrl = avatarUrl,
                            activeRentals = activeRentals,
                            historyRentals = historyRentals,
                            onMenuClick = { scope.launch { drawerState.open() } },
                            onRentalClick = onRentalClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RentalsOwnerContent(
    userName: String,
    avatarUrl: String?,
    activeRentals: List<TransaksiData>,
    historyRentals: List<TransaksiData>,
    onMenuClick: () -> Unit,
    onRentalClick: (TransaksiData) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        SewainTopBar(
            navigationIcon = Icons.Default.Menu,
            onNavigationClick = onMenuClick
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
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
                        .padding(20.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
                        AsyncImage(
                            model = if (!avatarUrl.isNullOrEmpty()) {
                                if (avatarUrl.startsWith("http")) avatarUrl 
                                else "${ApiClient.IMAGE_BASE_URL}profiles/$avatarUrl"
                            } else "https://ui-avatars.com/api/?name=$userName",
                            contentDescription = null,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Text(
                        text = "Rentals Owner",
                        fontFamily = VolkhovFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Barang-barang kamu yang sedang disewa",
                        fontFamily = MontaguSlabFont,
                        fontSize = 12.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    RentalGrid(items = activeRentals, onRentalClick = onRentalClick)

                    Spacer(Modifier.height(32.dp))

                    Text(
                        text = "Rentals Owner — History",
                        fontFamily = VolkhovFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Barang-barang kamu yang telah disewa",
                        fontFamily = MontaguSlabFont,
                        fontSize = 12.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    RentalGrid(items = historyRentals, onRentalClick = onRentalClick)
                }
            }
        }
    }
}

@Composable
private fun RentalGrid(items: List<TransaksiData>, onRentalClick: (TransaksiData) -> Unit) {
    if (items.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.04f))
                .padding(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Tidak ada data.",
                fontFamily = MontaguSlabFont,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.4f)
            )
        }
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rowItems.forEach { item ->
                    RentalCard(
                        item = item,
                        onClick = { onRentalClick(item) },
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

@Composable
private fun RentalCard(item: TransaksiData, onClick: () -> Unit, modifier: Modifier = Modifier) {
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
            .background(LocalDarkNavy)
            .clickable { onClick() }
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = barang?.nama_barang,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                contentScale = ContentScale.Crop
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
                    fontFamily = MontaguSlabFont,
                    fontSize = 10.sp,
                    color = status.badgeTextColor
                )
            }
        }

        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = barang?.nama_barang ?: "Produk",
                fontFamily = MontaguSlabFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = Color.White,
                maxLines = 2
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(11.dp)
                )
                Spacer(Modifier.width(3.dp))
                Text(
                    text = "Indonesia",
                    fontFamily = MontaguSlabFont,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Rp ${formatRupiah(barang?.harga_sewa ?: 0.0)}/hari",
                fontFamily = AbrilFatfaceFont,
                fontSize = 16.sp,
                color = Color.White
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Sewa: ${item.tanggal_sewa} - ${item.tanggal_kembali_rencana}",
                    fontFamily = MontaguSlabFont,
                    fontSize = 10.sp,
                    color = Color.White
                )
            }
            if (!item.tanggal_kembali_aktual.isNullOrEmpty()) {
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Return: ${item.tanggal_kembali_aktual}",
                        fontFamily = MontaguSlabFont,
                        fontSize = 10.sp,
                        color = Color.White
                    )
                }
            } else if (status == RentalStatus.ACTIVE || status == RentalStatus.UPCOMING) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Return: Belum dijadwalkan",
                    fontFamily = MontaguSlabFont,
                    fontSize = 10.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = Color.White.copy(alpha = 0.45f)
                )
            }
        }
    }
}
