package com.l0124005.sewain_rpl.ui.theme.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.l0124005.sewain_rpl.R
import com.l0124005.sewain_rpl.utils.CurrencyUtils

// ── Data customer untuk struk ─────────────────────────────────
data class CustomerInfo(
    val nama: String,
    val metodePembayaran: String,
    val waktuPemesanan: String,
    val metodePengiriman: String, // "COD" atau "Delivery"
    val alamat: String,            // kosong/"-" kalau COD
    val tanggalPembayaran: String
)

val LatoFont = FontFamily(Font(R.font.lato_regular))
val AbrilFatfaceFont = FontFamily(Font(R.font.abrilfatface_regular))

// CATATAN: CkColors, CkVolkhov, CkBody, MonsterratFont, dan clickableNoRipple
// sudah didefinisikan di CheckoutPaymentScreen.kt dalam package yang sama.

// ── Screen utama ──────────────────────────────────────────────
@Composable
fun PaymentConfirmedScreen(
    orderNumber: String,
    items: List<CartItem>,
    shippingCost: Long,
    customer: CustomerInfo,
    onBackHome: () -> Unit,
    onPrintReceipt: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
    ) {

        Text(
            "Checkout Payment",
            fontFamily = CkVolkhov,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            color = CkColors.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp)
        )

        // ── Bagian kiri: Konfirmasi ──
        ConfirmationStatus(
            orderNumber = orderNumber,
            onBackHome = onBackHome,
            onPrintReceipt = onPrintReceipt
        )

        Spacer(Modifier.height(32.dp))

        // ── Bagian kanan: Struk ──
        ReceiptCard(
            items = items,
            shippingCost = shippingCost,
            customer = customer
        )

        Spacer(Modifier.height(32.dp))
    }
}

// ── Status konfirmasi (ikon centang + judul + pesan) ──────────
@Composable
private fun ConfirmationStatus(
    orderNumber: String,
    onBackHome: () -> Unit,
    onPrintReceipt: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(RoundedCornerShape(50))
                .background(CkColors.Blue),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = "Pembayaran berhasil",
                tint = Color.White,
                modifier = Modifier.size(50.dp)
            )
        }

        Spacer(Modifier.height(18.dp))

        Text(
            "PAYMENT CONFIRMED!",
            fontFamily = CkVolkhov,
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            color = CkColors.Dark,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(6.dp))

        Row {
            Text("ORDER ", fontSize = 15.sp, color = Color(0xFF888888), fontFamily = CkBody, fontWeight = FontWeight.SemiBold)
            Text(
                "#$orderNumber",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = CkColors.Blue,
                fontFamily = CkBody
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "Thank you for buying Goodfeel. The system is grateful to you. Please check your e-mail, there will be a payment link that will be sent to your e-mail address according to your agreement.",
            fontSize = 13.sp,
            fontFamily = AbrilFatfaceFont,
            color = Color(0xFF818181),
            lineHeight = 22.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = onBackHome,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CkColors.Blue),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Back to SEWAIN Home", color = Color.White, fontFamily = MonsterratFont, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(10.dp))

        Text(
            "Print Receipt",
            fontSize = 12.sp,
            color = CkColors.Blue,
            fontFamily = CkBody,
            fontWeight = FontWeight.SemiBold,
            textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
            modifier = Modifier.clickableNoRipple(onPrintReceipt).padding(6.dp)
        )
    }
}

// ── Kartu struk lengkap ────────────────────────────────────────
@Composable
private fun ReceiptCard(
    items: List<CartItem>,
    shippingCost: Long,
    customer: CustomerInfo
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, CkColors.Border, RoundedCornerShape(8.dp))
            .background(CkColors.PanelBg)
    ) {
        for (item in items) {
            ReceiptProductRow(item = item, shippingCost = shippingCost)
        }

        CustomerInfoSection(customer = customer)
    }
}

@Composable
private fun ReceiptProductRow(item: CartItem, shippingCost: Long) {
    val durasi = item.durasiHari()
    val subtotal = item.subtotal(durasi)
    val jaminan = item.jaminan()
    val total = subtotal + shippingCost + jaminan

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(width = 0.5.dp, color = CkColors.Border)
            .padding(18.dp)
    ) {
        // Header: badge qty + foto + nama + harga + badge PAID
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
                        model = item.imageUrl,
                        contentDescription = item.nama,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.nama,
                        fontFamily = CkVolkhov,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = CkColors.Black,
                        lineHeight = 18.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${CurrencyUtils.formatRupiah(item.harga)}/hari",
                        fontFamily = CkBody,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CkColors.Blue
                    )
                }
            }
            // Badge qty di pojok kiri-atas foto
            Box(
                modifier = Modifier
                    .offset(x = (-6).dp, y = (-6).dp)
                    .size(20.dp)
                    .clip(RoundedCornerShape(50))
                    .background(CkColors.Blue),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${item.qty}",
                    fontFamily = CkBody,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = Color.White
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Badge PAID, miring, di tengah
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .border(width = 3.5.dp, color = Color(0xFF2E7D32), shape = RoundedCornerShape(4.dp))
                .rotate(-15f)
                .padding(horizontal = 15.dp, vertical = 8.dp)
        ) {
            Text(
                "PAID",
                fontFamily = CkVolkhov,
                fontWeight = FontWeight.Black,
                fontSize = 17.sp,
                color = Color(0xFF2E7D32).copy(alpha = 0.75f),
                letterSpacing = 2.sp
            )
        }

        Spacer(Modifier.height(14.dp))

        // Dua kotak tanggal
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 14.dp)
        ) {
            ReceiptDateBox("Tanggal Mulai Penyewaan", item.tglMulai, Modifier.weight(1f))
            ReceiptDateBox("Tanggal Selesai Penyewaan", item.tglSelesai, Modifier.weight(1f))
        }

        // Rincian harga — rata kiri sesuai .rec-row
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ReceiptRow("Durasi Sewa", "$durasi Hari")
            ReceiptRow("Subtotal", CurrencyUtils.formatRupiah(subtotal))
            ReceiptRow("Shipping", if (shippingCost > 0) CurrencyUtils.formatRupiah(shippingCost) else "-")
            ReceiptRow("Jaminan", CurrencyUtils.formatRupiah(jaminan))
            ReceiptRow("Total", CurrencyUtils.formatRupiah(total), bold = true)
            ReceiptRow("#Catatan Denda", "${CurrencyUtils.formatRupiah(item.dendaPerJam)}/jam", italic = true)
        }
    }
}

@Composable
private fun ReceiptDateBox(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(vertical = 8.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(
                Icons.Filled.DateRange,
                contentDescription = null,
                tint = CkColors.Black,
                modifier = Modifier.size(13.dp)
            )
            Text(
                label,
                fontFamily = LatoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = CkColors.Black,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
        Spacer(Modifier.height(3.dp))
        Text(
            value,
            fontFamily = LatoFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            color = CkColors.Black
        )
    }
}

@Composable
private fun ReceiptRow(label: String, value: String, bold: Boolean = false, italic: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            fontFamily = CkBody,
            fontSize = if (bold) 12.sp else 11.sp,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold,
            fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
            color = CkColors.Dark,
            modifier = Modifier.weight(2f)
        )
        Text(
            value,
            fontFamily = CkBody,
            fontSize = if (bold) 12.sp else 11.sp,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold,
            fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
            color = CkColors.Dark,
            textAlign = TextAlign.Start,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CustomerInfoSection(customer: CustomerInfo) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(width = 0.5.dp, color = CkColors.Border)
            .padding(horizontal = 18.dp, vertical = 20.dp)
    ) {
        Text(
            "Informasi Customer",
            fontFamily = CkVolkhov,
            fontWeight = FontWeight.Bold,
            fontSize = 19.sp,
            color = CkColors.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)
        )
        HorizontalDivider(color = CkColors.Border, thickness = 1.dp)
        Spacer(Modifier.height(14.dp))

        // 1. Nama Customer
        CustomerInfoItem("Nama Customer", customer.nama, Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))

        // 2. Metode Pembayaran
        CustomerInfoItem("Metode Pembayaran", customer.metodePembayaran, Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))

        // 3. Waktu Pemesanan
        CustomerInfoItem("Waktu Pemesanan", customer.waktuPemesanan, Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))

        // 4. Metode Pengiriman — COD: catatan ambil sendiri | Delivery: "-" lalu Alamat
        CustomerInfoItem("Metode Pengiriman", customer.metodePengiriman, Modifier.fillMaxWidth())
        if (customer.metodePengiriman.contains("COD", ignoreCase = true)) {
            Spacer(Modifier.height(4.dp))
        Text(
            "Catatan: Ambil sendiri di lokasi SEWAIN",
            fontSize = 11.sp,
            fontStyle = FontStyle.Italic,
            color = CkColors.Gray,
            fontFamily = CkBody,
            fontWeight = FontWeight.SemiBold
        )
        } else {
            Spacer(Modifier.height(12.dp))
            CustomerInfoItem("Alamat", customer.alamat.ifBlank { "-" }, Modifier.fillMaxWidth())
        }

        Spacer(Modifier.height(12.dp))

        // Tanggal Pembayaran tetap di bawah
        CustomerInfoItem("Tanggal Pembayaran", customer.tanggalPembayaran, Modifier.fillMaxWidth())

        Spacer(Modifier.height(20.dp))
        HorizontalDivider(color = CkColors.Border, thickness = 1.dp)

        // Logo SEWAIN di dalam card customer info (ikut ke-print)
        Box(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row {
                Text("SEWA", fontFamily = CkVolkhov, fontSize = 26.sp, color = CkColors.Dark)
                Text("IN", fontFamily = CkVolkhov, fontSize = 26.sp, color = CkColors.Blue)
            }
        }
    }
}

@Composable
private fun CustomerInfoItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, fontSize = 12.sp, color = CkColors.Dark, fontFamily = CkBody, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(2.dp))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CkColors.Dark, fontFamily = CkBody)
    }
}

@Preview(showBackground = true, heightDp = 1800)
@Composable
fun PaymentConfirmedScreenPreview() {
    val dummyItems = listOf(
        CartItem(
            id = 1,
            nama = "ALLTREK Tenda Camping Tentastic Outdoor 1 Bedroom + 1 Guest Room",
            harga = 450000,
            qty = 1,
            imageUrl = "https://placehold.co/200",
            tglMulai = "30 Juli 2026",
            tglSelesai = "1 Agustus 2026",
            dendaPerJam = 15000,
            jaminanBase = 810000
        ),
        CartItem(
            id = 2,
            nama = "Kebaya Cream (1 Set)",
            harga = 630000,
            qty = 1,
            imageUrl = "https://placehold.co/200",
            tglMulai = "30 Juli 2026",
            tglSelesai = "31 Juli 2026",
            dendaPerJam = 15000,
            jaminanBase = 315000
        )
    )

    MaterialTheme {
        Surface(color = Color.White, modifier = Modifier.fillMaxSize()) {
            PaymentConfirmedScreen(
                orderNumber = "5020",
                items = dummyItems,
                shippingCost = 40000L,
                customer = CustomerInfo(
                    nama = "Aprilia Afia",
                    metodePembayaran = "Transfer Bank — BCA",
                    waktuPemesanan = "Kamis, 11 Maret 2026 — 05:40 AM",
                    metodePengiriman = "Delivery",
                    alamat = "Jl. Bondong, 45, Brigie",
                    tanggalPembayaran = "Kamis, 11 Maret 2026 — 05:46 AM"
                ),
                onBackHome = {}
            )
        }
    }
}
