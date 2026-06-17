package com.l0124005.sewain_rpl.ui.theme.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.l0124005.sewain_rpl.ui.theme.katalog.*
import com.l0124005.sewain_rpl.utils.CurrencyUtils

// ============================================================
// 1. TEMA WARNA & TIPOGRAFI — Menggunakan katalog shared
// ============================================================

val PaidGreen = Color(0xFF2E7D32) // border .paid-badge

// ============================================================
// 2. MODEL DATA (dummy dulu, nanti diisi dari API Laravel)
// ============================================================

data class RentedItem(
    val imageUrl: String,
    val name: String,
    val pricePerDay: Long,
    val qty: Int,
    val startDate: String,
    val endDate: String,
    val durationDays: Int,
    val subtotal: Long,
    val shipping: Long,
    val deposit: Long, // Jaminan
    val total: Long,
    val lateFeeNote: String // contoh: "Rp 15.000/jam"
)

data class CustomerInfo(
    val name: String,
    val address: String,
    val shippingMethod: String, // "Delivery" atau "COD — Ambil Sendiri"
    val paymentDate: String
)

data class PaymentConfirmedState(
    val orderNumber: String,
    val items: List<RentedItem>,
    val customer: CustomerInfo
)

// Dummy data sesuai contoh di HTML asli — nanti diganti dari response API
fun dummyPaymentConfirmedState() = PaymentConfirmedState(
    orderNumber = "5020",
    items = listOf(
        RentedItem(
            imageUrl = "https://placehold.co/200x220?text=Tenda",
            name = "ALLTREK Tenda Camping Tentastic Outdoor 1 Bedroom + 1 Guest Room",
            pricePerDay = 450_000,
            qty = 1,
            startDate = "30 Juli 2026",
            endDate = "1 Agustus 2026",
            durationDays = 2,
            subtotal = 900_000,
            shipping = 40_000,
            deposit = 810_000,
            total = 1_750_000,
            lateFeeNote = "Rp 15.000/jam"
        ),
        RentedItem(
            imageUrl = "https://placehold.co/200x220?text=Kebaya",
            name = "Kebaya Cream (1 Set)",
            pricePerDay = 630_000,
            qty = 1,
            startDate = "30 Juli 2026",
            endDate = "31 Juli 2026",
            durationDays = 1,
            subtotal = 630_000,
            shipping = 10_000,
            deposit = 315_000,
            total = 955_000,
            lateFeeNote = "Rp 15.000/jam"
        )
    ),
    customer = CustomerInfo(
        name = "Aprilia Afia",
        address = "Jl. Bondong, 45, Brigie",
        shippingMethod = "Delivery",
        paymentDate = "Kamis, 11 Maret 2026 — 05:46 AM"
    )
)

// ============================================================
// 3. SCREEN UTAMA
// ============================================================

@Composable
fun PaymentConfirmedScreen(
    state: PaymentConfirmedState,
    onBackToHome: () -> Unit = {},
    onPrintReceipt: () -> Unit = {}
) {
    Surface(color = Color.White, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            PageHeader()

            Spacer(modifier = Modifier.height(8.dp))

            ConfirmSection(
                orderNumber = state.orderNumber,
                onBackToHome = onBackToHome,
                onPrintReceipt = onPrintReceipt
            )

            Spacer(modifier = Modifier.height(28.dp))

            ReceiptCard(state = state)

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ============================================================
// 4. PAGE HEADER ("Checkout Payment" + breadcrumb)
// ============================================================

@Composable
private fun PageHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 28.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Checkout Payment",
            fontFamily = Volkhov,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = Black,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Home", color = TextMuted, fontSize = 12.sp, fontFamily = FontFamily.Default)
            Text("  ›  ", color = Color(0xFFCCCCCC), fontSize = 12.sp)
            Text("Rentals", color = TextMuted, fontSize = 12.sp, fontFamily = FontFamily.Default)
            Text("  ›  ", color = Color(0xFFCCCCCC), fontSize = 12.sp)
            Text(
                "Checkout Payment",
                color = Black,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                fontFamily = FontFamily.Default
            )
        }
    }
    HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)
}

// ============================================================
// 5. CONFIRM SECTION (lingkaran centang, judul, tombol)
// ============================================================

@Composable
private fun ConfirmSection(
    orderNumber: String,
    onBackToHome: () -> Unit,
    onPrintReceipt: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // check-circle
        Box(
            modifier = Modifier
                .padding(top = 24.dp)
                .size(90.dp)
                .background(Primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Payment confirmed",
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "PAYMENT CONFIRMED!",
            fontFamily = Volkhov,
            fontWeight = FontWeight.Medium,
            fontSize = 24.sp,
            color = TextDark,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row {
            Text(
                "ORDER ",
                color = Color(0xFF888888),
                fontSize = 14.sp,
                fontFamily = FontFamily.Default
            )
            Text(
                "#$orderNumber",
                color = Primary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                fontFamily = FontFamily.Default
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Thank you for buying Goodfeel. The system is grateful to you. " +
                    "Please check your e-mail, there will be a payment link that will be sent " +
                    "to your e-mail address according to your agreement.",
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = Color(0xFF818181),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onBackToHome,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(
                "Back to SEWAIN Home",
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Light,
                fontSize = 15.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = onPrintReceipt) {
            Text(
                "Print Receipt",
                color = Primary,
                fontFamily = FontFamily.Default,
                fontSize = 12.sp
            )
        }
    }
}

// ============================================================
// 6. RECEIPT CARD (daftar produk + customer info + logo)
// ============================================================

@Composable
private fun ReceiptCard(state: PaymentConfirmedState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(width = 1.dp, color = BorderGray, shape = RoundedCornerShape(8.dp))
            .background(UploadBg, RoundedCornerShape(8.dp))
    ) {
        state.items.forEach { item ->
            ReceiptProductItem(item = item)
            HorizontalDivider(color = BorderGray, thickness = 1.dp)
        }

        CustomerInfoSection(customer = state.customer)

        // receipt-logo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .border(width = 1.dp, color = Color(0xFFF0F0F0))
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row {
                Text(
                    "SEWA",
                    fontFamily = Volkhov,
                    fontSize = 28.sp,
                    color = TextDark
                )
                Text(
                    "IN",
                    fontFamily = Volkhov,
                    fontSize = 28.sp,
                    color = Primary
                )
            }
        }
    }
}

@Composable
private fun ReceiptProductItem(item: RentedItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(18.dp)
    ) {
        // Header: badge qty + gambar + info produk + badge PAID
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp, 110.dp)
                        .background(UploadBg, RoundedCornerShape(4.dp))
                ) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = item.name,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        fontFamily = Volkhov,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Black,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${CurrencyUtils.formatRupiah(item.pricePerDay)}/hari",
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = Primary
                    )
                }
            }

            // item-badge (qty), posisinya nempel pojok kiri atas gambar
            Box(
                modifier = Modifier
                    .offset(x = (-6).dp, y = (-6).dp)
                    .size(20.dp)
                    .background(Primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${item.qty}",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // PAID badge — rotate -15deg, border hijau, mirip stempel
        Box(
            modifier = Modifier
                .padding(vertical = 12.dp)
                .align(Alignment.CenterHorizontally)
                .rotate(-15f)
                .border(width = 3.dp, color = PaidGreen, shape = RoundedCornerShape(4.dp))
                .padding(horizontal = 15.dp, vertical = 6.dp)
        ) {
            Text(
                text = "PAID",
                color = PaidGreen,
                fontFamily = Volkhov,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                letterSpacing = 2.sp
            )
        }

        // Tanggal mulai & selesai
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            DateBlock(
                label = "Tanggal Mulai Penyewaan",
                value = item.startDate,
                modifier = Modifier.weight(1f)
            )
            DateBlock(
                label = "Tanggal Selesai Penyewaan",
                value = item.endDate,
                modifier = Modifier.weight(1f)
            )
        }

        // Rows: durasi, subtotal, shipping, jaminan, total, denda
        ReceiptRow(label = "Durasi Sewa", value = "${item.durationDays} Hari")
        ReceiptRow(label = "Subtotal", value = CurrencyUtils.formatRupiah(item.subtotal))
        ReceiptRow(
            label = "Shipping",
            value = if (item.shipping > 0) CurrencyUtils.formatRupiah(item.shipping) else "-"
        )
        ReceiptRow(label = "Jaminan", value = CurrencyUtils.formatRupiah(item.deposit))
        ReceiptRow(label = "Total", value = CurrencyUtils.formatRupiah(item.total), isBold = true)
        ReceiptRow(
            label = "#Catatan Denda Pengembalian",
            value = item.lateFeeNote,
            isItalic = true
        )
    }
}

@Composable
private fun DateBlock(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.DateRange,
                contentDescription = null,
                tint = Color(0xFF181A18),
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                color = Color(0xFF181A18),
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = value,
            fontFamily = FontFamily.Default,
            fontSize = 11.sp,
            color = Color(0xFF181A18),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ReceiptRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    isItalic: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Default,
            fontSize = 11.sp,
            fontStyle = if (isItalic) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = TextDark
        )
        Text(
            text = value,
            fontFamily = FontFamily.Default,
            fontSize = if (isBold) 12.sp else 11.sp,
            fontStyle = if (isItalic) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            color = TextDark
        )
    }
}

// ============================================================
// 7. CUSTOMER INFO SECTION
// ============================================================

@Composable
private fun CustomerInfoSection(customer: CustomerInfo) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(20.dp)
    ) {
        Text(
            text = "Informasi Customer",
            fontFamily = Volkhov,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Black,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp)
        )
        HorizontalDivider(color = BorderGray, thickness = 1.dp)
        Spacer(modifier = Modifier.height(14.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            CustomerField(
                label = "Nama Customer",
                value = customer.name,
                modifier = Modifier.weight(1f)
            )
            CustomerField(
                label = "Alamat",
                value = customer.address,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            CustomerField(
                label = "Metode Pengiriman",
                value = customer.shippingMethod,
                modifier = Modifier.weight(1f)
            )
            CustomerField(
                label = "Tanggal Pembayaran",
                value = customer.paymentDate,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CustomerField(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontFamily = FontFamily.Default,
            fontSize = 12.sp,
            color = TextDark
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = TextDark
        )
    }
}

// ============================================================
// 8. PREVIEW
// ============================================================

@Preview(showBackground = true, heightDp = 1400, name = "Payment Confirmed - Full Page")
@Composable
fun PaymentConfirmedScreenPreview() {
    MaterialTheme {
        PaymentConfirmedScreen(state = dummyPaymentConfirmedState())
    }
}
