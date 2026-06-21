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
import com.l0124005.sewain_rpl.ui.theme.AbrilFatfaceFont
import com.l0124005.sewain_rpl.ui.theme.SewainTopBar
import com.l0124005.sewain_rpl.ui.theme.VolkhovFont

private val DarkNavy     = Color(0xFF21394F)
private val MidBlue      = Color(0xFF4D6674)
private val LightBlueBg  = Color(0xFFE8F4FB)
private val TextLight    = Color(0xFFE6E8EF)
private val TextMuted    = Color(0xFFA1A2A7)
private val AccentBlue   = Color(0xFF6A87A1)
private val CheckTint    = Color(0xFFC3D4E9)
private val ConfirmedDark = Color(0xFF484848)
private val ConfirmedGray = Color(0xFF818181)

enum class ReturnRentalStatus(val label: String, val bgColor: Color) {
    ACTIVE("Active Rent",    Color(0xFF438CFA).copy(alpha = 0.35f)),
    UPCOMING("Upcoming Rent", Color(0xFFEBF530).copy(alpha = 0.35f)),
    COMPLETED("Completed Rent", Color(0xFF34ED4A).copy(alpha = 0.35f)),
    CANCELED("Canceled Rent", Color(0xFFF83220).copy(alpha = 0.35f)),
    RETURN("Return Rent",    Color(0xFFFAA443).copy(alpha = 0.35f))
}

enum class ReturnVerificationStatus(val label: String) {
    WAITING("MENUNGGU VERIFIKASI OWNER"),
    APPROVED("PENGEMBALIAN DISETUJUI ✓")
}

data class ReturnConfirmedData(
    val itemName: String,
    val itemImageUrl: String,
    val status: ReturnRentalStatus,
    val idTransaksi: String,
    val owner: String,
    val tanggalMulai: String,
    val tanggalSelesai: String,
    val metodePembayaran: String,
    val lokasiPengambilan: String,
    val returnStatus: ReturnVerificationStatus
)

@Composable
fun ReturnConfirmedScreen(
    detail: ReturnConfirmedData,
    onBackToProfile: () -> Unit = {}
) {
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
                        .background(MidBlue)
                        .padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(DarkNavy)
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
                            text = "Status pengembalian barang kamu.",
                            fontFamily = VolkhovFont,
                            fontSize = 12.sp,
                            color = TextMuted
                        )

                        Spacer(Modifier.height(20.dp))
                        ReturnDetailHeaderBlock(detail = detail)
                        Spacer(Modifier.height(24.dp))
                        ReturnDetailInfoGrid(detail = detail)
                        Spacer(Modifier.height(24.dp))
                        ConfirmedBox(
                            returnStatus = detail.returnStatus,
                            onBackToProfile = onBackToProfile
                        )
                    }
                }
            }
        }
    }
}

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
                    .background(detail.status.bgColor)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = detail.status.label,
                    color = Color.White,
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

@Composable
private fun ReturnDetailInfoGrid(detail: ReturnConfirmedData) {
    val fields = listOf(
        "ID Transaksi"       to detail.idTransaksi,
        "Owner"              to detail.owner,
        "Tanggal Mulai"      to detail.tanggalMulai,
        "Tanggal Selesai"    to detail.tanggalSelesai,
        "Metode Pembayaran"  to detail.metodePembayaran,
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
                            Text(text = value, color = DarkNavy, fontSize = 14.sp)
                        }
                    }
                }
                if (rowFields.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

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
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(AccentBlue),
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

        Text(
            text = "RETURN CONFIRMED!",
            fontFamily = VolkhovFont,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = ConfirmedDark,
            letterSpacing = 0.5.sp
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Terima Kasih telah menggunakan Website SEWAIN sebagai platform penyewaan Anda!",
            fontSize = 14.sp,
            color = ConfirmedGray,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(Modifier.height(14.dp))

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

        Button(
            onClick = onBackToProfile,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
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

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, widthDp = 390, heightDp = 1500)
@Composable
fun ReturnConfirmedScreenPreview() {
    val dummyDetail = ReturnConfirmedData(
        itemName = "ALLTREK Tenda Camping 1 Bedroom - 1 Guest Room",
        itemImageUrl = "https://images.example.com/tent.png",
        status = ReturnRentalStatus.COMPLETED,
        idTransaksi = "TRX-20260517-0042",
        owner = "Camping Groups Bandung",
        tanggalMulai = "17 May 2026, 09:00 WIB",
        tanggalSelesai = "17 Jun 2026, 09:00 WIB",
        metodePembayaran = "Saldo SEWAIN Wallet",
        lokasiPengambilan = "Jl. Ir. H. Juanda No. 50 (Dago), Bandung",
        returnStatus = ReturnVerificationStatus.WAITING
    )
    com.l0124005.sewain_rpl.ui.theme.Sewain_rplTheme {
        ReturnConfirmedScreen(detail = dummyDetail, onBackToProfile = {})
    }
}
