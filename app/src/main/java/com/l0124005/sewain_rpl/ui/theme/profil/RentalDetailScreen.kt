package com.l0124005.sewain_rpl.ui.theme.profil

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.l0124005.sewain_rpl.network.*
import com.l0124005.sewain_rpl.utils.RentalStatus
import com.l0124005.sewain_rpl.ui.theme.*
import com.l0124005.sewain_rpl.utils.CurrencyUtils
import com.l0124005.sewain_rpl.utils.DateUtils
import com.l0124005.sewain_rpl.utils.Resource
import com.l0124005.sewain_rpl.viewmodel.TransaksiViewModel

// ── Kartu Receipt (.receipt-item) / .paid-badge ──
private val LightBlueBg   = Color(0xFFE8F4FB) // .rd-field .rd-value background
private val ReceiptBg     = Color(0xFFF5F5F5) // .receipt-item / .receipt-product
private val ReceiptBorder = Color(0xFFE8E8E8)
private val ReceiptText   = Color(0xFF484848) // .rec-row .lbl/.val
private val ReceiptDark   = Color(0xFF181A18) // .rec-date-label, total-row
private val PaidGreen     = Color(0xFF2E7D32) // .paid-badge
private val DendaRed      = Color(0xFFD32F2F) // .rec-row.denda

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RentalDetailScreen(
    transaksiId: Int,
    token: String,
    viewModel: TransaksiViewModel,
    profileViewModel: com.l0124005.sewain_rpl.viewmodel.ProfileViewModel,
    isOwner: Boolean = false,
    onBack: () -> Unit,
    onReturnClick: (Int) -> Unit,
    onVerifyClick: (Int) -> Unit = {}
) {
    val detailState by viewModel.transaksiDetail.observeAsState()
    val profileState by profileViewModel.profile.observeAsState()

    LaunchedEffect(transaksiId) {
        if (isOwner) {
            viewModel.getOwnerTransaksiDetail(token, transaksiId)
        } else {
            viewModel.getDetailTransaksi(token, transaksiId)
        }
        if (profileState == null) {
            profileViewModel.getProfile(token)
        }
    }

    Scaffold(
        topBar = {
            SewainTopBar(
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = onBack
            )
        },
        containerColor = Color.White
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)
        ) {
            when (val state = detailState) {
                is Resource.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = ProfileAccentBlue
                    )
                }
                is Resource.Error -> {
                    Text(
                        text = state.message ?: "Error loading detail",
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is Resource.Success -> {
                    state.data?.data?.let { transaksi ->
                        RentalDetailContent(
                            transaksi = transaksi,
                            token = token,
                            viewModel = viewModel,
                            profileState = profileState,
                            isOwner = isOwner,
                            onAjukanPengembalian = { onReturnClick(transaksi.id) },
                            onVerifyClick = { onVerifyClick(transaksi.id) },
                            onCancelSuccess = onBack
                        )
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun RentalDetailContent(
    transaksi: TransaksiData,
    token: String,
    viewModel: TransaksiViewModel,
    profileState: Resource<com.l0124005.sewain_rpl.network.ProfileResponse>?,
    isOwner: Boolean = false,
    onAjukanPengembalian: () -> Unit,
    onVerifyClick: () -> Unit = {},
    onCancelSuccess: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val cancelState by viewModel.cancelStatus.observeAsState()

    LaunchedEffect(cancelState) {
        if (cancelState is Resource.Success) {
            android.widget.Toast.makeText(context, "Penyewaan berhasil dibatalkan", android.widget.Toast.LENGTH_SHORT).show()
            // Setelah batal, kita refresh detail agar UI update ke status CANCELED
            if (isOwner) viewModel.getOwnerTransaksiDetail(token, transaksi.id)
            else viewModel.getDetailTransaksi(token, transaksi.id)
            viewModel.resetStates()
        } else if (cancelState is Resource.Error) {
            val msg = (cancelState as Resource.Error).message ?: "Gagal membatalkan"
            val displayMsg = when {
                msg.contains("<html>", ignoreCase = true) -> "Gagal: Endpoint tidak ditemukan di server (404)."
                msg.contains("404") -> "Gagal: Transaksi tidak ditemukan atau rute salah (404)."
                else -> msg
            }
            android.widget.Toast.makeText(context, displayMsg, android.widget.Toast.LENGTH_LONG).show()
            viewModel.resetStates()
        }
    }

    val status = RentalStatus.fromTransaksi(transaksi)
    val barang = transaksi.barang
    val imageUrl = if (barang != null && !barang.foto_barang.isNullOrEmpty()) {
        if (barang.foto_barang.startsWith("http")) barang.foto_barang
        else "${ApiClient.IMAGE_BASE_URL}${barang.foto_barang}"
    } else {
        "https://picsum.photos/seed/${barang?.id ?: 0}/600/400"
    }

    val formatTgl = { dateStr: String? ->
        DateUtils.formatDateForUI(dateStr).ifEmpty { "N/A" }
    }
    val formatTglFull = { dateStr: String? ->
        DateUtils.formatFullDateForUI(dateStr).ifEmpty { "N/A" }
    }

    // ── Hitung Denda Dinamis ──
    val displayDendaVal = RentalStatus.calculateFine(transaksi)
    val rentalStatus = RentalStatus.fromTransaksi(transaksi)
    
    val rawStatus = transaksi.status.lowercase()
    val isDisewa = rawStatus in listOf("disewa", "aktif", "active")
    val hasSubmittedReturn = rentalStatus == RentalStatus.RETURN || rentalStatus == RentalStatus.COMPLETED

    val durasiSewa = "${DateUtils.calculateDaysBetween(transaksi.tanggal_sewa, transaksi.tanggal_kembali_rencana)} Hari"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // ── Panel luar warna #4D6674 (.dash-panel/.dash-content) ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MidBlue)
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
                // ── Judul "My Rentals" + Tombol Ajukan (Row) ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isOwner) "Owner History" else "My Rentals",
                            fontFamily = VolkhovFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = Color.White
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (isOwner) "Detail transaksi dari sisi owner." else "Detail transaksi penyewaan kamu.",
                            fontFamily = VolkhovFont,
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }

                    // ── Tombol "AJUKAN PENGEMBALIAN" ──
                    // Izinkan pengembalian untuk status ACTIVE atau UPCOMING yang sudah berstatus "disewa" atau "aktif"
                    // Jangan tampilkan jika sudah ada bukti pengembalian (status RETURN)
                    if (!isOwner && !hasSubmittedReturn && (status == RentalStatus.ACTIVE || (status == RentalStatus.UPCOMING && isDisewa))) {
                        Button(
                            onClick = onAjukanPengembalian,
                            enabled = true,
                            modifier = Modifier
                                .width(140.dp)
                                .height(56.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ProfileAccentBlue,
                                disabledContainerColor = ProfileAccentBlue.copy(alpha = 0.5f)
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "AJUKAN\nPENGEMBALIAN",
                                fontFamily = AbrilFatfaceFont,
                                fontSize = 11.sp,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                letterSpacing = 0.3.sp,
                                lineHeight = 14.sp
                            )
                        }
                    }

                    // ── Tombol "VERIFIKASI PENGEMBALIAN" (Hanya Owner) ──
                    if (isOwner && rawStatus == "menunggu_verifikasi") {
                        Button(
                            onClick = onVerifyClick,
                            modifier = Modifier
                                .width(140.dp)
                                .height(56.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ProfileAccentBlue
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "VERIFIKASI\nPENGEMBALIAN",
                                fontFamily = AbrilFatfaceFont,
                                fontSize = 11.sp,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                letterSpacing = 0.3.sp,
                                lineHeight = 14.sp
                            )
                        }
                    }

                    // ── Tombol "BATALKAN SEWA" (Hanya untuk Upcoming) ──
                    // Muncul jika status UPCOMING dan belum dalam status "disewa"
                    val isCanceled = rawStatus in listOf("dibatalkan", "expired", "canceled", "cancelled")
                    if (!isOwner && status == RentalStatus.UPCOMING && !isDisewa && !isCanceled) {
                        Button(
                            onClick = { viewModel.cancelTransaksi(token, transaksi.id) },
                            enabled = cancelState !is Resource.Loading,
                            modifier = Modifier
                                .width(140.dp)
                                .height(56.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF9F5556), 
                                disabledContainerColor = Color(0xFF9F5556).copy(alpha = 0.5f)
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            if (cancelState is Resource.Loading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text(
                                    text = "BATALKAN\nPENYEWAAN",
                                    fontFamily = AbrilFatfaceFont,
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    letterSpacing = 0.3.sp,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ── Header detail rental (.rental-detail-header) ──
                RentalDetailHeaderBlock(
                    itemName = barang?.nama_barang ?: "Product Detail",
                    itemImageUrl = imageUrl,
                    status = status
                )

                Spacer(Modifier.height(24.dp))

                // Ambil user dari transaksi, jika null fallback ke profile
                val currentUser = transaksi.user?.name 
                    ?: (profileState as? Resource.Success)?.data?.data?.name 
                    ?: "Customer"

                val dendaText = if (displayDendaVal > 0) "Rp ${CurrencyUtils.formatRupiah(displayDendaVal)}" else "Tidak ada denda"

                val tglKembaliDisplay = if (transaksi.tanggal_kembali_aktual != null && transaksi.tanggal_kembali_aktual != "null") {
                    formatTglFull(transaksi.tanggal_kembali_aktual)
                } else {
                    formatTgl(transaksi.tanggal_kembali_rencana)
                }

                // ── Grid info transaksi (.rd-form-grid) ──
                RentalDetailInfoGrid(
                    idTransaksi = "TRX-${transaksi.id}",
                    owner = transaksi.barang?.user?.name ?: "Owner",
                    user = currentUser,
                    denda = dendaText,
                    tanggalSewa = formatTgl(transaksi.tanggal_sewa),
                    tanggalPengembalian = tglKembaliDisplay
                )

                Spacer(Modifier.height(28.dp))

                // ── Judul "Receipt" (.reviews-header h3) ──
                Text(
                    text = "Receipt",
                    fontFamily = AbrilFatfaceFont,
                    fontSize = 20.sp,
                    color = TextLight,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // ── Kartu Receipt (.receipt-item) ──
                val paymentMethod = transaksi.pembayaran?.metode ?: "-"
                ReceiptCard(
                    receiptItemName = barang?.nama_barang ?: "-",
                    receiptItemImageUrl = imageUrl,
                    pricePerDay = "Rp ${CurrencyUtils.formatRupiah(barang?.harga_sewa ?: 0.0)}/hari",
                    isPaid = transaksi.status.lowercase() != "menunggu_pembayaran",
                    paymentMethod = paymentMethod,
                    tanggalMulai = formatTgl(transaksi.tanggal_sewa),
                    tanggalSelesai = "${formatTgl(transaksi.tanggal_kembali_rencana)} 23:59 WIB",
                    durasiSewa = durasiSewa,
                    subtotal = "Rp ${CurrencyUtils.formatRupiah(transaksi.total_harga)}",
                    shipping = "Free",
                    jaminan = "Rp ${CurrencyUtils.formatRupiah(barang?.harga_jaminan ?: 0.0)}",
                    total = "Rp ${CurrencyUtils.formatRupiah(transaksi.total_harga + (barang?.harga_jaminan ?: 0.0) + displayDendaVal)}",
                    catatanDenda = if (displayDendaVal > 0) "Denda: Rp ${CurrencyUtils.formatRupiah(displayDendaVal)}" else "-"
                )

                // ── Form Confirmation (Status Info) ──
                // Muncul jika sudah mengajukan pengembalian (ada bukti atau status verifikasi)
                if (hasSubmittedReturn) {
                    Spacer(Modifier.height(28.dp))
                    FormConfirmationStatus(statusStr = rawStatus)
                }
            }
        }
    }
}

@Composable
private fun FormConfirmationStatus(statusStr: String) {
    val isCompleted = statusStr == "selesai" || statusStr == "completed" || statusStr == "done"
    val headerText = if (isCompleted) "ACCEPT RETURN CONFIRMED!" else "RETURN CONFIRMED!"
    val verificationStatus = if (isCompleted) {
        "COMPLETED RENT ✓"
    } else {
        "MENUNGGU VERIFIKASI OWNER"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFE8F4FB)) // LightBlueBg
            .padding(32.dp, 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(ProfileAccentBlue),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color(0xFFC3D4E9), // CheckTint
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = headerText,
            fontFamily = VolkhovFont,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = Color(0xFF484848), // ConfirmedDark
            letterSpacing = 0.5.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Terima Kasih telah menggunakan Website SEWAIN sebagai platform penyewaan Anda!",
            fontFamily = MontaguSlabFont,
            fontWeight = FontWeight.Light,
            fontSize = 14.sp,
            color = Color(0xFFAAAAAA),
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(Modifier.height(14.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Status Return Rent Anda saat ini:",
                fontFamily = MontaguSlabFont,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = Color(0xFFAAAAAA),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = verificationStatus,
                fontFamily = AbrilFatfaceFont,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = Color(0xFF818181),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ── Header: foto thumbnail + badge status + nama barang ──
@Composable
private fun RentalDetailHeaderBlock(
    itemName: String,
    itemImageUrl: String,
    status: RentalStatus
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(10.dp))
        ) {
            AsyncImage(
                model = itemImageUrl,
                contentDescription = itemName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Badge status
            Box(
                modifier = Modifier
                    .padding(6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(status.badgeColor.copy(alpha = 0.7f))
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
        Spacer(Modifier.width(18.dp))
        Text(
            text = itemName,
            fontFamily = AbrilFatfaceFont,
            fontSize = 22.sp,
            color = TextLight,
            lineHeight = 28.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

// ── Grid info transaksi: ID Transaksi, Owner, User, Denda, Tanggal Sewa, Tanggal Pengembalian ──
@Composable
private fun RentalDetailInfoGrid(
    idTransaksi: String,
    owner: String,
    user: String,
    denda: String,
    tanggalSewa: String,
    tanggalPengembalian: String
) {
    val fields = listOf(
        "ID Transaksi" to idTransaksi,
        "Owner" to owner,
        "User" to user,
        "Denda" to denda,
        "Tanggal Sewa" to tanggalSewa,
        "Tanggal Pengembalian" to tanggalPengembalian
    )

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        fields.chunked(2).forEach { rowFields ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowFields.forEach { (label, value) ->
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = label,
                            fontFamily = AbrilFatfaceFont,
                            fontSize = 16.sp,
                            color = TextLight
                        )
                        Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(LightBlueBg)
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = value,
                                color = DarkNavy,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                if (rowFields.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

// ── Kartu Receipt (.receipt-item) -- background terang, override tema gelap ──
@Composable
private fun ReceiptCard(
    receiptItemName: String,
    receiptItemImageUrl: String,
    pricePerDay: String,
    isPaid: Boolean,
    paymentMethod: String,
    tanggalMulai: String,
    tanggalSelesai: String,
    durasiSewa: String,
    subtotal: String,
    shipping: String,
    jaminan: String,
    total: String,
    catatanDenda: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, ReceiptBorder, RoundedCornerShape(8.dp))
            .background(Color.White)
            .padding(18.dp)
    ) {
        // ── Header produk: foto + nama & harga (Sesuai layout ConfirmScreen) ──
        Box {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 100.dp, height = 110.dp)
                        .clip(RoundedCornerShape(4.dp))
                ) {
                    AsyncImage(
                        model = receiptItemImageUrl,
                        contentDescription = receiptItemName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = receiptItemName,
                        fontFamily = VolkhovFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.Black,
                        lineHeight = 18.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = pricePerDay,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ProfileAccentBlue
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Via: $paymentMethod",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        fontStyle = FontStyle.Italic
                    )
                }
            }
            // Badge angka "1" (offset)
            Box(
                modifier = Modifier
                    .offset(x = (-6).dp, y = (-6).dp)
                    .size(20.dp)
                    .clip(RoundedCornerShape(50))
                    .background(ProfileAccentBlue),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "1", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(8.dp))

        // PAID badge -- centered dan miring (Sesuai layout ConfirmScreen)
        if (isPaid) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .border(width = 3.5.dp, color = PaidGreen.copy(alpha = 0.75f), shape = RoundedCornerShape(4.dp))
                    .rotate(-15f)
                    .padding(horizontal = 15.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "PAID",
                    color = PaidGreen.copy(alpha = 0.75f),
                    fontFamily = VolkhovFont,
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                    letterSpacing = 2.sp
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // ── Tanggal mulai & selesai (.rec-dates) ──
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            ReceiptDateItem(
                label = "Tanggal Mulai Penyewaan",
                value = tanggalMulai,
                modifier = Modifier.weight(1f)
            )
            ReceiptDateItem(
                label = "Tanggal Selesai Penyewaan",
                value = tanggalSelesai,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(14.dp))

        // ── Rows rincian biaya (.rec-rows) ──
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            ReceiptRow(label = "Durasi Sewa", value = durasiSewa)
            ReceiptRow(label = "Subtotal", value = subtotal)
            ReceiptRow(label = "Shipping", value = shipping)
            ReceiptRow(label = "Jaminan", value = jaminan)

            HorizontalDivider(color = Color(0xFFDFEAF2), thickness = 1.dp, modifier = Modifier.padding(vertical = 2.dp))

            ReceiptRow(label = "Total", value = total, bold = true)

            ReceiptRow(
                label = "#Catatan Denda",
                value = catatanDenda,
                isDenda = true
            )
        }
    }
}

@Composable
private fun ReceiptDateItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                Icons.Default.CalendarToday,
                contentDescription = null,
                tint = ReceiptDark,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = ReceiptDark,
                textAlign = TextAlign.Center
            )
        }
        Spacer(Modifier.height(3.dp))
        Text(
            text = value,
            fontSize = 11.sp,
            color = ReceiptDark
        )
    }
}

@Composable
private fun ReceiptRow(
    label: String,
    value: String,
    bold: Boolean = false,
    isDenda: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = if (bold) 12.sp else 11.sp,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            fontStyle = if (isDenda) FontStyle.Italic else FontStyle.Normal,
            color = if (isDenda) DendaRed else if (bold) ReceiptDark else ReceiptText
        )
        Text(
            text = value,
            fontSize = if (bold) 12.sp else 11.sp,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium,
            fontStyle = if (isDenda) FontStyle.Italic else FontStyle.Normal,
            color = if (isDenda) DendaRed else if (bold) ReceiptDark else ReceiptText
        )
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
fun RentalDetailContentPreview() {
    val dummyBarang = CatalogData(
        id = 1,
        user_id = 2,
        kategori_id = 1,
        nama_barang = "Sony Alpha A7 III Mirrorless Camera with 28-70mm Lens",
        deskripsi = "Camera body with lens",
        additional_information = null,
        harga_sewa = 250000.0,
        harga_jaminan = 500000.0,
        harga_denda_perjam = 10000.0,
        stok = 1,
        lokasi = "Solo",
        whatsapp = "08123456789",
        tanggal_mulai = null,
        tanggal_akhir = null,
        foto_barang = null,
        fotoproduk1 = null,
        fotoproduk2 = null,
        fotoproduk3 = null,
        fotoproduk4 = null,
        status = "aktif",
        kategori = null,
        user = UserData(
            id = 2, 
            name = "Owner Name", 
            username = "owner", 
            email = "owner@example.com", 
            phone_number = "123", 
            alamat = "Solo", 
            saldo = 0.0, 
            foto_profil = null, 
            is_banned = false, 
            role = "user", 
            email_verified_at = null
        )
    )
    val dummyTransaksi = TransaksiData(
        id = 123,
        user_id = 1,
        barang_id = 1,
        pembayaran_id = 1,
        jumlah = 1,
        tanggal_sewa = "2023-10-01",
        tanggal_kembali_rencana = "2023-10-03",
        tanggal_kembali_aktual = null,
        status = "aktif",
        foto_buktipengembalian = null,
        tanggal_verifikasipengembalian = null,
        total_harga = 500000.0,
        jam_terlambat = 0,
        total_denda = 0.0,
        barang = dummyBarang,
        pembayaran = PembayaranData(
            id = 1, 
            metode = "Transfer BCA", 
            detail_metode = "BCA", 
            tanggal_bayar = "2023-10-01", 
            jumlah_bayar = 1000000.0
        ),
        user = UserData(
            id = 1, 
            name = "John Doe", 
            username = "johndoe", 
            email = "john@example.com", 
            phone_number = "123", 
            alamat = "Solo", 
            saldo = 0.0, 
            foto_profil = null, 
            is_banned = false, 
            role = "user", 
            email_verified_at = null
        )
    )

    Sewain_rplTheme {
        RentalDetailContent(
            transaksi = dummyTransaksi,
            token = "dummy",
            viewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
            profileState = null,
            onAjukanPengembalian = {},
            onCancelSuccess = {}
        )
    }
}
