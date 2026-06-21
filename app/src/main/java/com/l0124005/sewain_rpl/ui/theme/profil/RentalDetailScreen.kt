package com.l0124005.sewain_rpl.ui.theme.profil

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import coil.compose.AsyncImage
import com.l0124005.sewain_rpl.ui.theme.AbrilFatfaceFont
import com.l0124005.sewain_rpl.ui.theme.SewainTopBar
import com.l0124005.sewain_rpl.ui.theme.VolkhovFont
import com.l0124005.sewain_rpl.ui.theme.DarkNavy
import com.l0124005.sewain_rpl.ui.theme.MidBlue
import com.l0124005.sewain_rpl.ui.theme.TextLight
import com.l0124005.sewain_rpl.ui.theme.TextMuted

// ── Warna tema tambahan ──
private val LightBlueBg = Color(0xFFE8F4FB) // .rd-field .rd-value background
private val ReceiptBg   = Color(0xFFF5F5F5) // .receipt-item / .receipt-product
private val ReceiptBorder = Color(0xFFE8E8E8)
private val ReceiptText = Color(0xFF484848) // .rec-row .lbl/.val
private val ReceiptDark = Color(0xFF181A18) // .rec-date-label, total-row
private val PriceBlue   = Color(0xFF6A87A1) // .rec-price
private val PaidGreen   = Color(0xFF2E7D32) // .paid-badge
private val DendaRed    = Color(0xFFD32F2F) // .rec-row.denda

// ── Status badge -- sesuai statusLabels & badgeBg di script detail-sewa.html ──
enum class RentalDetailStatus(val label: String, val bgColor: Color) {
    ACTIVE("Active Rent", Color(0xFF438CFA).copy(alpha = 0.35f)),
    UPCOMING("Upcoming Rent", Color(0xFFEBF530).copy(alpha = 0.35f)),
    COMPLETED("Completed Rent", Color(0xFF34ED4A).copy(alpha = 0.35f)),
    CANCELED("Canceled Rent", Color(0xFFF83220).copy(alpha = 0.35f)),
    RETURN("Return Rent", Color(0xFFFAA443).copy(alpha = 0.35f))
}

data class RentalDetailData(
    val itemName: String,
    val itemImageUrl: String,
    val status: RentalDetailStatus,
    val idTransaksi: String,
    val owner: String,
    val user: String,
    val denda: String,
    val date: String,
    // Receipt
    val receiptItemName: String,
    val receiptItemImageUrl: String,
    val pricePerDay: String,
    val isPaid: Boolean,
    val tanggalMulai: String,
    val tanggalSelesai: String,
    val durasiSewa: String,
    val subtotal: String,
    val shipping: String,
    val jaminan: String,
    val total: String,
    val catatanDenda: String
)

@Composable
fun RentalDetailScreen(
    detail: RentalDetailData,
    onAjukanPengembalian: () -> Unit = {}
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
                // ── Panel luar warna MidBlue (.dash-panel/.dash-content) ──
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(MidBlue)
                        .padding(20.dp)
                ) {
                    // ── Card konten dalam warna DarkNavy (.dash-content-card) ──
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(DarkNavy)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
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
                                text = "Detail transaksi penyewaan kamu.",
                                fontFamily = VolkhovFont,
                                fontSize = 12.sp,
                                color = TextMuted
                            )

                            Spacer(Modifier.height(20.dp))

                            // ── Header detail rental (.rental-detail-header) ──
                            RentalDetailHeaderBlock(detail = detail)

                            Spacer(Modifier.height(24.dp))

                            // ── Grid info transaksi (.rd-form-grid) ──
                            RentalDetailInfoGrid(detail = detail)

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
                            ReceiptCard(detail = detail)
                        }

                        // ── Tombol "AJUKAN PENGEMBALIAN" -- HANYA muncul kalau status Active Rent
                        if (detail.status == RentalDetailStatus.ACTIVE) {
                            Button(
                                onClick = onAjukanPengembalian,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 24.dp, end = 24.dp)
                                    .width(150.dp)
                                    .height(64.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PriceBlue),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "AJUKAN\nPENGEMBALIAN",
                                    fontFamily = AbrilFatfaceFont,
                                    fontSize = 13.sp,
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    letterSpacing = 0.3.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RentalDetailHeaderBlock(detail: RentalDetailData) {
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
private fun RentalDetailInfoGrid(detail: RentalDetailData) {
    val fields = listOf(
        "ID Transaksi" to detail.idTransaksi,
        "Owner" to detail.owner,
        "User" to detail.user,
        "Denda" to detail.denda,
        "Date" to detail.date,
        "Status" to detail.status.label
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

@Composable
private fun ReceiptCard(detail: RentalDetailData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, ReceiptBorder, RoundedCornerShape(8.dp))
            .background(ReceiptBg)
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(50))
                    .background(PriceBlue),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "1", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .size(width = 90.dp, height = 100.dp)
                    .clip(RoundedCornerShape(4.dp))
            ) {
                AsyncImage(
                    model = detail.receiptItemImageUrl,
                    contentDescription = detail.receiptItemName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = detail.receiptItemName,
                    fontFamily = VolkhovFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.Black,
                    lineHeight = 18.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = detail.pricePerDay,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PriceBlue
                )
            }

            if (detail.isPaid) {
                Box(
                    modifier = Modifier
                        .rotate(-15f)
                        .border(3.5.dp, PaidGreen.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "PAID",
                        color = PaidGreen.copy(alpha = 0.75f),
                        fontFamily = VolkhovFont,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        letterSpacing = 2.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            ReceiptDateItem(
                label = "Tanggal Mulai Penyewaan",
                value = detail.tanggalMulai,
                modifier = Modifier.weight(1f)
            )
            ReceiptDateItem(
                label = "Tanggal Selesai Penyewaan",
                value = detail.tanggalSelesai,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(14.dp))

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            ReceiptRow(label = "Durasi Sewa", value = detail.durasiSewa)
            ReceiptRow(label = "Subtotal", value = detail.subtotal)
            ReceiptRow(label = "Shipping", value = detail.shipping)
            ReceiptRow(label = "Jaminan", value = detail.jaminan)

            HorizontalDivider(color = Color(0xFFDFEAF2), thickness = 1.dp, modifier = Modifier.padding(vertical = 2.dp))

            ReceiptRow(label = "Total", value = detail.total, bold = true)

            ReceiptRow(
                label = "#Catatan Denda",
                value = detail.catatanDenda,
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

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, widthDp = 390, heightDp = 1600)
@Composable
fun RentalDetailScreenPreview() {
    val dummyDetail = RentalDetailData(
        itemName = "ALLTREK Tenda Camping 1 Bedroom - 1 Guest Room",
        itemImageUrl = "https://images.example.com/tent.png",
        status = RentalDetailStatus.ACTIVE,
        idTransaksi = "TRX-20260517-0042",
        owner = "Camping Groups Bandung",
        user = "Aprilia Alfa",
        denda = "-",
        date = "12 Maret 2015, 09.45 AM",
        receiptItemName = "ALLTREK Tenda Camping Tentastic Outdoor 1 Bedroom + 1 Guest Room",
        receiptItemImageUrl = "https://images.example.com/tentastic.png",
        pricePerDay = "Rp 450.000/hari",
        isPaid = true,
        tanggalMulai = "30 Juli 2026",
        tanggalSelesai = "1 Agustus 2026",
        durasiSewa = "2 Hari",
        subtotal = "Rp 900.000",
        shipping = "Rp 40.000",
        jaminan = "Rp 450.000",
        total = "Rp 1.750.000",
        catatanDenda = "Rp 15.000/jam"
    )

    RentalDetailScreen(
        detail = dummyDetail,
        onAjukanPengembalian = {}
    )
}
