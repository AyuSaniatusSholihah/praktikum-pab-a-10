package com.l0124005.sewain_rpl.ui.theme.profil

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.em
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.l0124005.sewain_rpl.ui.theme.*
import com.l0124005.sewain_rpl.viewmodel.TransaksiViewModel
import com.l0124005.sewain_rpl.network.ApiClient
import com.l0124005.sewain_rpl.utils.CurrencyUtils.formatRupiah
import com.l0124005.sewain_rpl.utils.Resource
import com.l0124005.sewain_rpl.utils.RentalStatus
import com.l0124005.sewain_rpl.viewmodel.ProfileViewModel
import java.util.Date
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ── Warna tema -- Konsisten dengan ProfileScreen ──
private val PemasukanGreen = Color(0xFF34ED4A)
private val PembayaranRed  = Color(0xFFF83220)
private val LocalMidBlue = Color(0xFF4D6674)
private val LocalDarkNavy = Color(0xFF21394F)

// ── Tipe transaksi wallet ──
enum class WalletTxnType { PEMASUKAN, PEMBAYARAN }

data class WalletTransaction(
    val title: String,
    val location: String,
    val amount: String,
    val dateStart: String,
    val dateEnd: String,
    val dateReturn: String,
    val isReturned: Boolean,
    val transactionDate: String,
    val rawDate: Date?,
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
        transaksiViewModel.getRiwayatTransaksi(token)
    }

    val ownerTxns = (transaksiViewModel.ownerDashboard.observeAsState().value as? Resource.Success)?.data?.data?.daftar_transaksi ?: emptyList()
    val tenantTxns = (transaksiViewModel.transaksiList.observeAsState().value as? Resource.Success)?.data?.data ?: emptyList()

    val outputSdf = remember { java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()) }
    val formatTgl = { dateStr: String? ->
        val parsed = RentalStatus.parseFlexibleDate(dateStr)
        if (parsed != null) outputSdf.format(parsed) else dateStr ?: "-"
    }

    // Gabungkan data: Owner -> Pemasukan, Tenant -> Pembayaran
    val history = remember(ownerTxns, tenantTxns) {
        val income = ownerTxns.map { txn ->
            val rDate = RentalStatus.parseFlexibleDate(txn.tanggal_verifikasipengembalian ?: txn.tanggal_sewa)
            val isReturned = !txn.tanggal_kembali_aktual.isNullOrEmpty() && !txn.tanggal_kembali_aktual.startsWith("0000")
            val returnDateStr = if (isReturned) txn.tanggal_kembali_aktual else txn.tanggal_kembali_rencana

            WalletTransaction(
                title = txn.barang?.nama_barang ?: "Produk",
                location = txn.barang?.lokasi ?: "Indonesia",
                amount = "+ Rp ${formatRupiah(txn.total_harga)}",
                dateStart = formatTgl(txn.tanggal_sewa),
                dateEnd = formatTgl(txn.tanggal_kembali_rencana),
                dateReturn = formatTgl(returnDateStr),
                isReturned = isReturned,
                transactionDate = formatTgl(txn.tanggal_verifikasipengembalian ?: txn.tanggal_sewa),
                rawDate = rDate,
                imageUrl = txn.barang?.mainFotoUrl?.ifEmpty { "https://picsum.photos/seed/${txn.id}/300/200" } ?: "https://picsum.photos/seed/${txn.id}/300/200",
                type = WalletTxnType.PEMASUKAN
            )
        }
        val expense = tenantTxns.map { txn ->
            val rDate = RentalStatus.parseFlexibleDate(txn.pembayaran?.tanggal_bayar ?: txn.tanggal_sewa)
            val isReturned = !txn.tanggal_kembali_aktual.isNullOrEmpty() && !txn.tanggal_kembali_aktual.startsWith("0000")
            val returnDateStr = if (isReturned) txn.tanggal_kembali_aktual else txn.tanggal_kembali_rencana

            WalletTransaction(
                title = txn.barang?.nama_barang ?: "Produk",
                location = txn.barang?.lokasi ?: "Indonesia",
                amount = "- Rp ${formatRupiah(txn.total_harga)}",
                dateStart = formatTgl(txn.tanggal_sewa),
                dateEnd = formatTgl(txn.tanggal_kembali_rencana),
                dateReturn = formatTgl(returnDateStr),
                isReturned = isReturned,
                transactionDate = formatTgl(txn.pembayaran?.tanggal_bayar ?: txn.tanggal_sewa),
                rawDate = rDate,
                imageUrl = txn.barang?.mainFotoUrl?.ifEmpty { "https://picsum.photos/seed/${txn.id}/300/200" } ?: "https://picsum.photos/seed/${txn.id}/300/200",
                type = WalletTxnType.PEMBAYARAN
            )
        }
        (income + expense).sortedByDescending { it.rawDate ?: Date(0) }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            val user = (profileState as? Resource.Success)?.data?.data
            ProfileDrawerContent(
                userName = user?.name ?: "User",
                userPhoto = user?.foto_profil,
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(LocalMidBlue)
                    .padding(20.dp)
            ) {
                // Header Profil Kecil
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

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(LocalDarkNavy)
                        .padding(24.dp)
                ) {
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

                    var rekeningState by rememberSaveable { 
                        mutableStateOf(initialRekening.ifEmpty { "Nama Rekening: BSI Syariah\nNo Rekening: 763098218847\nA/n: Camping Groups Bandung" }) 
                    }

                    LaunchedEffect(rekeningState) {
                        if (rekeningState.isNotEmpty() && rekeningState != initialRekening) {
                            delay(1500)
                            onRekeningSave(rekeningState)
                        }
                    }

                    WalletProfileSection(
                        name = name,
                        email = email,
                        rekening = rekeningState,
                        onRekeningChange = { rekeningState = it },
                        saldo = saldo
                    )

                    Spacer(Modifier.height(36.dp))

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

                    if (history.isEmpty()) {
                        Text(
                            text = "Belum ada riwayat transaksi.",
                            fontSize = 13.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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

@Composable
private fun WalletProfileSection(
    name: String,
    email: String,
    rekening: String,
    onRekeningChange: (String) -> Unit,
    saldo: Double
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        WalletSaldoCard(saldo = saldo)
        Spacer(Modifier.height(20.dp))
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
            textStyle = TextStyle(
                fontFamily = VolkhovFont,
                fontSize = 14.sp,
                color = DarkNavy
            )
        )
    }
}

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
                    fontFamily = VolkhovFont,
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

@Composable
private fun WalletTransactionCard(item: WalletTransaction, modifier: Modifier = Modifier) {
    val amountColor = if (item.type == WalletTxnType.PEMASUKAN) PemasukanGreen else PembayaranRed

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Transparent)
    ) {
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
                    .clip(RoundedCornerShape(16.dp))
            )

            val badgeText = if (item.type == WalletTxnType.PEMASUKAN) "Pemasukan Sewa" else "Pembayaran Sewa"
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(amountColor.copy(alpha = 0.35f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = badgeText,
                    fontFamily = VolkhovFont,
                    color = Color.White,
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
                text = item.title,
                fontFamily = VolkhovFont,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = TextLight,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(Modifier.width(3.dp))
                Text(
                    text = item.location,
                    fontFamily = VolkhovFont,
                    color = TextLight,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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
                    Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Sewa: ${item.dateStart} s.d ${item.dateEnd}",
                    fontFamily = VolkhovFont,
                    color = Color.White,
                    fontSize = 10.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = if (item.isReturned) Color.White else Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(11.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Return item: ${item.dateReturn} 23:59 WIB",
                    fontFamily = VolkhovFont,
                    color = if (item.isReturned) Color.White else Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
fun MyWalletScreenPreview() {
    Sewain_rplTheme {
        MyWalletContent(
            name = "Camping Groups Bandung",
            userPhoto = null,
            email = "camping@gmail.com",
            saldo = 1872000.0,
            history = emptyList(),
            initialRekening = "",
            onRekeningSave = {},
            onMenuClick = {}
        )
    }
}