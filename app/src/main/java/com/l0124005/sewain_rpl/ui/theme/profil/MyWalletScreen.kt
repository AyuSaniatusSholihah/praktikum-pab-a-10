package com.l0124005.sewain_rpl.ui.theme.profil

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.delay
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
private val LocalMidBlue = Color(0xFF4D6674)
private val LocalDarkNavy = Color(0xFF21394F)
private val LocalAccentBlue = Color(0xFF6A87A1)

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
    val savedRekening by viewModel.rekeningInfo.observeAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.getProfile(token)
        transaksiViewModel.getOwnerDashboard(token)
        transaksiViewModel.getRiwayatTransaksi(token) // Ambil riwayat penyewa juga untuk "Pembayaran"
    }

    val ownerTxns = (transaksiViewModel.ownerDashboard.observeAsState().value as? Resource.Success)?.data?.data?.daftar_transaksi ?: emptyList()
    val tenantTxns = (transaksiViewModel.transaksiList.observeAsState().value as? Resource.Success)?.data?.data ?: emptyList()

    // Gabungkan data: Owner -> Pemasukan (Hijau), Tenant -> Pembayaran (Merah)
    val history = remember(ownerTxns, tenantTxns) {
        val income = ownerTxns.map { txn ->
            WalletTransaction(
                title = txn.barang?.nama_barang ?: "Produk",
                location = txn.barang?.lokasi ?: "Indonesia",
                amount = "+ Rp ${formatRupiah(txn.total_harga)}",
                dateStart = txn.tanggal_sewa,
                dateEnd = txn.tanggal_kembali_rencana,
                transactionDate = txn.tanggal_verifikasipengembalian ?: txn.tanggal_sewa,
                imageUrl = if (!txn.barang?.foto_barang.isNullOrEmpty()) "${ApiClient.IMAGE_BASE_URL}${txn.barang?.foto_barang}" else "https://picsum.photos/seed/${txn.id}/300/200",
                type = WalletTxnType.PEMASUKAN
            )
        }
        val expense = tenantTxns.map { txn ->
            WalletTransaction(
                title = txn.barang?.nama_barang ?: "Produk",
                location = txn.barang?.lokasi ?: "Indonesia",
                amount = "- Rp ${formatRupiah(txn.total_harga)}",
                dateStart = txn.tanggal_sewa,
                dateEnd = txn.tanggal_kembali_rencana,
                transactionDate = txn.pembayaran?.tanggal_bayar ?: txn.tanggal_sewa,
                imageUrl = if (!txn.barang?.foto_barang.isNullOrEmpty()) "${ApiClient.IMAGE_BASE_URL}${txn.barang?.foto_barang}" else "https://picsum.photos/seed/${txn.id}/300/200",
                type = WalletTxnType.PEMBAYARAN
            )
        }
        (income + expense).sortedByDescending { it.transactionDate }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            val user = (profileState as? Resource.Success)?.data?.data
            val userName = user?.name ?: "User"
            val userPhoto = user?.foto_profil
            ProfileDrawerContent(
                userName = userName,
                userPhoto = userPhoto,
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
                                userPhoto = user.foto_profil,
                                email = user.email,
                                saldo = user.saldo,
                                history = history,
                                initialRekening = savedRekening ?: "",
                                onRekeningSave = { viewModel.updateRekening(token, it) },
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
    userPhoto: String?,
    email: String,
    saldo: Double,
    history: List<WalletTransaction>,
    initialRekening: String,
    onRekeningSave: (String) -> Unit,
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
            // ── Panel luar warna #4D6674 (.dash-panel / .dash-content) ──
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
                        model = if (!userPhoto.isNullOrEmpty()) {
                            if (userPhoto.startsWith("http")) userPhoto
                            else "${ApiClient.IMAGE_BASE_URL}${if (userPhoto.startsWith("profiles/")) userPhoto else "profiles/$userPhoto"}"
                        } else "https://ui-avatars.com/api/?name=$name",
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(6.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(14.dp))
                    Text(
                        text = name,
                        fontFamily = VolkhovFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }

                Spacer(Modifier.height(20.dp))

                // ── Card konten dalam warna #21394F (.dash-content-card) ──
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(LocalDarkNavy)
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

                    // State lokal untuk informasi rekening agar bisa di-edit langsung
                    var rekeningState by rememberSaveable { 
                        mutableStateOf(initialRekening.ifEmpty { "Nama Rekening: BSI Syariah\nNo Rekening: 763098218847\nA/n: Camping Groups Bandung" }) 
                    }

                    // Logika "Otomatis Simpan": Menunggu user berhenti mengetik selama 1.5 detik
                    LaunchedEffect(rekeningState) {
                        if (rekeningState.isNotEmpty() && rekeningState != initialRekening) {
                            delay(1500) // Debounce delay
                            onRekeningSave(rekeningState)
                        }
                    }

                    // ── Saldo card di atas, lalu info akun di bawahnya ──
                    WalletProfileSection(
                        name = name,
                        email = email,
                        rekening = rekeningState,
                        onRekeningChange = { rekeningState = it },
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
                            // Dibagi 2 kolom manual (Chunked + Row)
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
    onRekeningChange: (String) -> Unit,
    saldo: Double
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // ── Saldo card paling atas ──
        WalletSaldoCard(saldo = saldo)

        Spacer(Modifier.height(20.dp))

        // ── Info akun di bawahnya ──
        WalletField(label = "Name Account User", value = name, readOnly = true)
        WalletField(label = "Email", value = email, readOnly = true)
        WalletField(
            label = "Informasi Rekening",
            value = rekening,
            onValueChange = onRekeningChange,
            multiline = true
        )
    }
}

// ── Field ala .profile-field di web (label Abril Fatface + input pill InputBlue) ──
@Composable
private fun WalletField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit = {},
    readOnly: Boolean = false,
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
        
        TextField(
            value = value,
            onValueChange = onValueChange,
            readOnly = readOnly,
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (multiline) Modifier.heightIn(min = 100.dp)
                    else Modifier.height(56.dp)
                ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = InputBlue,
                unfocusedContainerColor = InputBlue,
                disabledContainerColor = InputBlue,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                cursorColor = DarkNavy
            ),
            shape = if (multiline) RoundedCornerShape(14.dp) else RoundedCornerShape(999.dp),
            textStyle = LocalTextStyle.current.copy(
                fontSize = 14.sp,
                color = DarkNavy
            )
        )
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
            .background(LocalDarkNavy.copy(alpha = 0.001f)) // transparent placeholder, real bg dari Box foto + info di bawah
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(LocalDarkNavy)
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
                val badgeColor = if (item.type == WalletTxnType.PEMASUKAN) PemasukanGreen else PembayaranRed
                val badgeText = if (item.type == WalletTxnType.PEMASUKAN) "Pemasukan Sewa" else "Pembayaran Sewa"
                
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(badgeColor.copy(alpha = 0.9f))
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
                        text = item.location,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                Text(
                    text = item.amount,
                    fontFamily = AbrilFatfaceFont,
                    fontSize = 16.sp,
                    color = if (item.type == WalletTxnType.PEMASUKAN) PemasukanGreen else PembayaranRed
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
                    userPhoto = null,
                    email = dummyEmail,
                    saldo = dummySaldo,
                    history = dummyHistoryPreview,
                    initialRekening = "Nama Rekening: BSI Syariah\nNo Rekening: 763098218847\nA/n: Camping Groups Bandung",
                    onRekeningSave = {},
                    onMenuClick = {}
                )
            }
        }
    }
}