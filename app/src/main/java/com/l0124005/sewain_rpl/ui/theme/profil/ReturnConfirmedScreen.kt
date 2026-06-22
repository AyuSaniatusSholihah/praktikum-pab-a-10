package com.l0124005.sewain_rpl.ui.theme.profil

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.l0124005.sewain_rpl.network.ApiClient
import com.l0124005.sewain_rpl.network.TransaksiData
import com.l0124005.sewain_rpl.utils.RentalStatus
import com.l0124005.sewain_rpl.ui.theme.*
import com.l0124005.sewain_rpl.utils.Resource
import com.l0124005.sewain_rpl.viewmodel.TransaksiViewModel

// ── Warna tema tambahan (unik untuk layar konfirmasi) ──
private val LightBlueBg   = Color(0xFFE8F4FB) // .rd-field .rd-value & .confirmed-box background
private val CheckTint     = Color(0xFFC3D4E9) // warna centang biru muda (.confirmed-box .check svg)
private val ConfirmedDark = Color(0xFF484848) // .confirmed-box h3
private val ConfirmedGray = Color(0xFF818181) // .confirmed-box p

// ── Status verifikasi pengembalian -- sesuai returnStatusLabels di script ──
enum class ReturnVerificationStatus(val label: String) {
    WAITING("MENUNGGU VERIFIKASI OWNER"),
    APPROVED("PENGEMBALIAN DISETUJUI ✓")
}

/**
 * Data ringkas untuk menampilkan header & grid info transaksi di screen konfirmasi pengembalian.
 * Isi dari data transaksi/pengembalian asli (hasil response setelah kirim form pengembalian)
 * sebelum ditampilkan ke screen ini.
 */
data class ReturnConfirmedData(
    val itemName: String,
    val itemImageUrl: String,
    val status: RentalStatus = RentalStatus.RETURN,
    val idTransaksi: String,
    val owner: String,
    val tanggalMulai: String,
    val tanggalSelesai: String,
    val metodePembayaran: String,
    val lokasiPengambilan: String,
    val returnStatus: ReturnVerificationStatus = ReturnVerificationStatus.WAITING
)

@Composable
fun ReturnConfirmedScreen(
    transaksiId: Int,
    token: String,
    viewModel: TransaksiViewModel,
    onDone: () -> Unit
) {
    val detailState by viewModel.transaksiDetail.observeAsState()

    LaunchedEffect(transaksiId) {
        viewModel.getDetailTransaksi(token, transaksiId)
    }

    Scaffold(
        topBar = { SewainTopBar() },
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
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is Resource.Error -> {
                    Text(
                        text = state.message ?: "Gagal memuat detail",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Red
                    )
                }
                is Resource.Success -> {
                    state.data?.data?.let { transaksi ->
                        val detail = mapTransaksiToReturnConfirmed(transaksi)
                        ReturnConfirmedContent(detail = detail, onDone = onDone)
                    }
                }
                else -> {}
            }
        }
    }
}

private fun mapTransaksiToReturnConfirmed(transaksi: TransaksiData): ReturnConfirmedData {
    val barang = transaksi.barang
    val imageUrl = if (barang != null && !barang.foto_barang.isNullOrEmpty()) {
        if (barang.foto_barang.startsWith("http")) barang.foto_barang
        else "${ApiClient.IMAGE_BASE_URL}${barang.foto_barang}"
    } else ""

    // Logika Status Verifikasi
    val returnVerStatus = if (transaksi.status.lowercase() == "selesai" || 
                             transaksi.status.lowercase() == "completed" ||
                             transaksi.tanggal_verifikasipengembalian != null) {
        ReturnVerificationStatus.APPROVED
    } else {
        ReturnVerificationStatus.WAITING
    }

    val outputSdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
    val formatTgl = { dateStr: String? ->
        val parsed = RentalStatus.parseFlexibleDate(dateStr)
        if (parsed != null) outputSdf.format(parsed) else dateStr ?: "-"
    }

    return ReturnConfirmedData(
        itemName = barang?.nama_barang ?: "-",
        itemImageUrl = imageUrl,
        status = RentalStatus.fromTransaksi(transaksi),
        idTransaksi = "TRX-${transaksi.id}",
        owner = barang?.user?.name ?: "-",
        tanggalMulai = formatTgl(transaksi.tanggal_sewa),
        tanggalSelesai = formatTgl(transaksi.tanggal_kembali_rencana),
        metodePembayaran = transaksi.pembayaran?.metode ?: "Saldo SEWAIN",
        lokasiPengambilan = barang?.lokasi ?: "-",
        returnStatus = returnVerStatus
    )
}

@Composable
private fun ReturnConfirmedContent(
    detail: ReturnConfirmedData,
    onDone: () -> Unit
) {
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
                // ── Judul "My Rentals" + subjudul ──
                Text(
                    text = "My Rentals",
                    fontFamily = VolkhovFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color.White
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Status pengembalian barang kamu.",
                    fontFamily = VolkhovFont,
                    fontSize = 12.sp,
                    color = TextMuted
                )

                Spacer(Modifier.height(20.dp))

                // ── Header detail rental (.rental-detail-header) ──
                ReturnDetailHeaderBlock(detail = detail)

                Spacer(Modifier.height(24.dp))

                // ── Grid info transaksi (.rd-form-grid) ──
                ReturnDetailInfoGrid(detail = detail)

                Spacer(Modifier.height(24.dp))

                // ── Kotak konfirmasi (.confirmed-box) ──
                ConfirmedBox(
                    returnStatus = detail.returnStatus,
                    onBackToProfile = onDone
                )
            }
        }
    }
}

// ── Header: foto thumbnail + badge status + nama barang ──
@Composable
private fun ReturnDetailHeaderBlock(detail: ReturnConfirmedData) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(10.dp))
        ) {
            AsyncImage(
                model = detail.itemImageUrl,
                contentDescription = detail.itemName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .padding(6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(detail.status.badgeColor)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = detail.status.label,
                    color = detail.status.badgeTextColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(Modifier.width(18.dp))
        Text(
            text = detail.itemName,
            fontFamily = AbrilFatfaceFont,
            fontSize = 22.sp,
            color = TextLight,
            lineHeight = 28.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

// ── Grid info transaksi: ID Transaksi, Owner, Tanggal Mulai, Tanggal Selesai,
// Metode Pembayaran, Lokasi Pengambilan ──
@Composable
private fun ReturnDetailInfoGrid(detail: ReturnConfirmedData) {
    val fields = listOf(
        "ID Transaksi" to detail.idTransaksi,
        "Owner" to detail.owner,
        "Tanggal Mulai" to detail.tanggalMulai,
        "Tanggal Selesai" to detail.tanggalSelesai,
        "Metode Pembayaran" to detail.metodePembayaran,
        "Lokasi Pengambilan" to detail.lokasiPengambilan
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

// ── Kotak konfirmasi (.confirmed-box) -- background terang, ikon centang bulat,
// judul "RETURN CONFIRMED!", paragraf ucapan terima kasih, status verifikasi,
// dan tombol "Back to SEWAIN Profile" ──
@Composable
private fun ConfirmedBox(
    returnStatus: ReturnVerificationStatus,
    onBackToProfile: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(LightBlueBg)
            .padding(32.dp, 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Lingkaran centang ──
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(ProfileAccentBlue),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = CheckTint,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        // ── Judul "RETURN CONFIRMED!" ──
        Text(
            text = "RETURN CONFIRMED!",
            fontFamily = VolkhovFont,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = ConfirmedDark,
            letterSpacing = 0.5.sp
        )

        Spacer(Modifier.height(12.dp))

        // ── Paragraf ucapan terima kasih ──
        Text(
            text = "Terima Kasih telah menggunakan Website SEWAIN sebagai platform penyewaan Anda!",
            fontSize = 14.sp,
            color = ConfirmedGray,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(Modifier.height(14.dp))

        // ── Status verifikasi pengembalian ──
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Status Return Rent Anda saat ini:",
                fontSize = 14.sp,
                color = ConfirmedGray,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = returnStatus.label,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 17.sp,
                color = ConfirmedGray,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(24.dp))

        // ── Tombol "Back to SEWAIN Profile" ──
        Button(
            onClick = onBackToProfile,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ProfileAccentBlue)
        ) {
            Text(
                text = "Back to SEWAIN Profile",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
    }
}

// ── Preview ──
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, widthDp = 390, heightDp = 1500)
@Composable
private fun ReturnConfirmedScreenPreview() {
    val dummyDetail = ReturnConfirmedData(
        itemName = "ALLTREK Tenda Camping 1 Bedroom - 1 Guest Room",
        itemImageUrl = "https://images.example.com/tent.png",
        status = RentalStatus.COMPLETED,
        idTransaksi = "TRX-20260517-0042",
        owner = "Camping Groups Bandung",
        tanggalMulai = "17/05/2026",
        tanggalSelesai = "17/06/2026",
        metodePembayaran = "Saldo SEWAIN Wallet",
        lokasiPengambilan = "Jl. Ir. H. Juanda No. 50 (Dago), Bandung",
        returnStatus = ReturnVerificationStatus.WAITING
    )

    Sewain_rplTheme {
        ReturnConfirmedContent(
            detail = dummyDetail,
            onDone = {}
        )
    }
}