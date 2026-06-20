package com.l0124005.sewain_rpl.ui.theme.profil

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
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
import com.l0124005.sewain_rpl.ui.theme.*
import com.l0124005.sewain_rpl.viewmodel.TransaksiViewModel
import com.l0124005.sewain_rpl.network.TransaksiData
import com.l0124005.sewain_rpl.network.ApiClient
import com.l0124005.sewain_rpl.utils.CurrencyUtils.formatRupiah
import com.l0124005.sewain_rpl.utils.Resource
import com.l0124005.sewain_rpl.viewmodel.ProfileViewModel
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.ui.tooling.preview.Preview
import com.l0124005.sewain_rpl.ui.theme.Sewain_rplTheme
import kotlinx.coroutines.launch

// ── Warna tema -- SAMA PERSIS dengan ProfileScreen.kt biar konsisten ──
private val PemasukanGreen = Color(0xFF34ED4A) // sesuai .pemasukan-badge di web (rgba 52,237,74)
private val PembayaranRed  = Color(0xFFF83220) // sesuai .pembayaran-badge di web (rgba 248,50,32)

// ── Tipe transaksi wallet ──
enum class WalletTxnType { PEMASUKAN, PEMBAYARAN }

// TODO: ganti dengan model asli dari backend kalau endpoint riwayat wallet sudah tersedia.
data class WalletTransaction(
    val title: String,
    val location: String,
    val amount: String,
    val dateStart: String,
    val dateEnd: String,
    val transactionDate: String,
    val imageUrl: String,
    val type: WalletTxnType
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyWalletScreen(
    viewModel: ProfileViewModel,
    transaksiViewModel: TransaksiViewModel,
    token: String,
    onBack: () -> Unit,
    onLogout: () -> Unit = {},
    onMyRentalClick: () -> Unit = {},
    onRentalOwnerClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val profileState by viewModel.profile.observeAsState()
    val dashboardState by transaksiViewModel.ownerDashboard.observeAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.getProfile(token)
        transaksiViewModel.getOwnerDashboard(token)
    }

    val history = (dashboardState as? Resource.Success)?.data?.data?.daftar_transaksi?.map { txn ->
        val isPemasukan = txn.status == "selesai" // Sederhananya, jika selesai berarti uang masuk
        WalletTransaction(
            title = txn.barang?.nama_barang ?: "Produk",
            location = txn.barang?.lokasi ?: "Indonesia",
            amount = "Rp ${formatRupiah(txn.total_harga)}",
            dateStart = txn.tanggal_sewa,
            dateEnd = txn.tanggal_kembali_rencana,
            transactionDate = txn.tanggal_verifikasipengembalian ?: txn.tanggal_sewa,
            imageUrl = if (!txn.barang?.foto_barang.isNullOrEmpty()) "${ApiClient.IMAGE_BASE_URL}products/${txn.barang?.foto_barang}" else "https://picsum.photos/seed/${txn.id}/300/200",
            type = if (isPemasukan) WalletTxnType.PEMASUKAN else WalletTxnType.PEMBAYARAN
        )
    } ?: emptyList()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            val userName = profileState?.data?.data?.name ?: "User"
            ProfileDrawerContent(
                userName = userName,
                currentScreen = "My Wallet",
                onProfileClick = {
                    scope.launch { drawerState.close() }
                    onBack()
                },
                onMyRentalsClick = {
                    scope.launch { drawerState.close() }
                    onMyRentalClick()
                },
                onRentalsOwnerClick = {
                    scope.launch { drawerState.close() }
                    onRentalOwnerClick()
                },
                onMyWalletClick = {
                    scope.launch { drawerState.close() }
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
                when (profileState) {
                    is Resource.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = BluePrimary
                        )
                    }
                    is Resource.Success -> {
                        val user = profileState?.data?.data
                        if (user != null) {
                            MyWalletContent(
                                name = user.name,
                                email = user.email,
                                saldo = user.saldo,
                                history = history,
                                onMenuClick = { scope.launch { drawerState.open() } }
                            )
                        }
                    }
                    is Resource.Error -> {
                        Text(
                            text = profileState?.message ?: "Error",
                            color = Color.Red,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}


// ====================================================
// KONTEN UTAMA -- sesuai struktur .dash-content-card
// di profile-wallet.html: header My Wallet -> saldo card
// -> info akun -> section "My Wallet — History" -> grid txn card
// ====================================================
@Composable
private fun MyWalletContent(
    name: String,
    email: String,
    saldo: Double,
    history: List<WalletTransaction>,
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
        
        Column(modifier = Modifier.padding(16.dp)) {
        // ── Panel luar warna #4D6674 (.dash-panel / .dash-content) ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(NavyPrimary)
                .padding(20.dp)
        ) {
            // ── Card konten dalam warna #21394F (.dash-content-card) ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkNavy)
                    .padding(24.dp)
            ) {
                // ── Judul "My Wallet" + subjudul ──
                Text(
                    text = "My Wallet",
                    fontFamily = VolkhovFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color.White
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Informasi Keuangan User",
                    fontFamily = VolkhovFont,
                    fontSize = 12.sp,
                    color = TextMuted
                )

                Spacer(Modifier.height(20.dp))

                // ── Saldo card di atas, lalu info akun di bawahnya (1 kolom, bukan 2 kolom) ──
                WalletProfileSection(
                    name = name,
                    email = email,
                    rekening = "Nama Rekening: BSI Syariah\nNo Rekening: 763098218847\nA/n: Camping Groups Bandung", // TODO: field ini belum ada di backend/UserData
                    saldo = saldo
                )

                Spacer(Modifier.height(36.dp))

                // ── Judul "My Wallet — History" + subjudul ──
                Text(
                    text = "My Wallet — History",
                    fontFamily = VolkhovFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color.White
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Riwayat penyewaan barangmu oleh penyewa.",
                    fontFamily = VolkhovFont,
                    fontSize = 12.sp,
                    color = TextMuted
                )

                Spacer(Modifier.height(16.dp))

                // ── Grid kartu transaksi (.txn-grid) ──
                if (history.isEmpty()) {
                    Text(
                        text = "Belum ada riwayat transaksi.",
                        fontSize = 13.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Dibagi 2 kolom manual (LazyVerticalGrid nested-scroll ribet di dalam
                        // verticalScroll luar, jadi pakai chunked + Row biasa -- aman & simpel)
                        history.chunked(2).forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                rowItems.forEach { item ->
                                    WalletTransactionCard(
                                        item = item,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                // biar layout tetap rapi kalau jumlah ganjil
                                if (rowItems.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}

// ── Section "Form Profile" Wallet -- versi 1 kolom vertikal:
// Saldo card paling atas, baru di bawahnya Name, Email, Informasi Rekening.
// (BUKAN 2 kolom samping-sampingan)
@Composable
private fun WalletProfileSection(
    name: String,
    email: String,
    rekening: String,
    saldo: Double
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // ── Saldo card paling atas ──
        WalletSaldoCard(saldo = saldo)

        Spacer(Modifier.height(20.dp))

        // ── Info akun di bawahnya ──
        WalletField(label = "Name Account User", value = name)
        WalletField(label = "Email", value = email)
        WalletField(
            label = "Informasi Rekening",
            value = rekening,
            multiline = true
        )
    }
}

// ── Field read-only ala .profile-field di web (label Abril Fatface + input pill InputBlue) ──
@Composable
private fun WalletField(
    label: String,
    value: String,
    multiline: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontFamily = AbrilFatfaceFont,
            fontSize = 18.sp,
            color = TextLight,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (multiline) Modifier.heightIn(min = 90.dp)
                    else Modifier.height(50.dp)
                )
                .clip(
                    if (multiline) RoundedCornerShape(14.dp)
                    else RoundedCornerShape(999.dp)
                )
                .background(InputBlue)
                .padding(horizontal = 16.dp, vertical = if (multiline) 12.dp else 0.dp),
            contentAlignment = if (multiline) Alignment.TopStart else Alignment.CenterStart
        ) {
            Text(
                text = value,
                fontSize = 14.sp,
                color = DarkNavy
            )
        }
    }
}

// ── Kartu Saldo -- sesuai .saldo-card di web (background biru muda, badge persen, nominal besar) ──
@Composable
private fun WalletSaldoCard(saldo: Double) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(InputBlue)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Badge "+2.3%" -- sesuai .saldo-badge di web
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White.copy(alpha = 0.5f))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "+2.3%",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = Color(0xFF1D4734)
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Saldo",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = DarkNavy
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Rp ${formatRupiah(saldo)}",
                    fontFamily = AbrilFatfaceFont,
                    fontSize = 24.sp,
                    color = Color.Black
                )
            }

            // ── Ikon bulat semi-transparan (.saldo-icon-btn) ──
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AttachMoney,
                    contentDescription = null,
                    tint = DarkNavy,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

// ── Kartu transaksi history -- sesuai .txn-card di web ──
// Badge hijau "Pemasukan Sewa" (uang masuk dari penyewa) atau
// badge merah "Pembayaran Sewa" (uang keluar untuk membayar sewa).
@Composable
private fun WalletTransactionCard(item: WalletTransaction, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(DarkNavy.copy(alpha = 0.001f)) // transparent placeholder, real bg dari Box foto + info di bawah
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF2D4A61))
        ) {
            // ── Foto + badge ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 11f)
            ) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                )

                // Badge "Pemasukan Sewa" / "Pembayaran Sewa"
                val (badgeColor, badgeText) = if (item.type == WalletTxnType.PEMASUKAN) {
                    PemasukanGreen.copy(alpha = 0.55f) to "Pemasukan Sewa"
                } else {
                    PembayaranRed.copy(alpha = 0.55f) to "Pembayaran Sewa"
                }
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(badgeColor)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // ── Info teks ──
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.title,
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
                        text = item.location,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                Text(
                    text = item.amount,
                    fontFamily = AbrilFatfaceFont,
                    fontSize = 16.sp,
                    color = Color.White
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Sewa: ${item.dateStart} s.d ${item.dateEnd}",
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Transaksi: ${item.transactionDate}",
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

// ── Preview ──
@Preview(showBackground = true, widthDp = 390, heightDp = 2400)
@Composable
fun MyWalletScreenPreview() {
    val dummyName = "Camping Groups Bandung"
    val dummyEmail = "campinggroups.bandung@gmail.com"
    val dummySaldo = 1872000.0

    val dummyHistoryPreview = listOf(
        WalletTransaction(
            title = "ALLTREK Tenda Camping 1 Bedroom + 1 Guest Room",
            location = "Kota Bandung, Indonesia",
            amount = "Rp 450.000",
            dateStart = "17/05/2026",
            dateEnd = "17/06/2026",
            transactionDate = "17/05/2026",
            imageUrl = "https://images.example.com/tent.png",
            type = WalletTxnType.PEMASUKAN
        ),
        WalletTransaction(
            title = "iPhone 17 Air (Hijau)",
            location = "Kota Surakarta, Indonesia",
            amount = "Rp 300.000",
            dateStart = "14/12/2025",
            dateEnd = "14/12/2025",
            transactionDate = "14/12/2025",
            imageUrl = "https://images.example.com/iphone.webp",
            type = WalletTxnType.PEMBAYARAN
        )
    )

    Sewain_rplTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                MyWalletContent(
                    name = dummyName,
                    email = dummyEmail,
                    saldo = dummySaldo,
                    history = dummyHistoryPreview,
                    onMenuClick = {}
                )
            }
        }
    }
}