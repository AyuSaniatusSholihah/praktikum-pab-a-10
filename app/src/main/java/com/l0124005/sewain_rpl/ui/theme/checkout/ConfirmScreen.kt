package com.l0124005.sewain_rpl.ui.theme.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector
import coil.compose.AsyncImage
import com.l0124005.sewain_rpl.R
import com.l0124005.sewain_rpl.network.ApiClient
import com.l0124005.sewain_rpl.ui.theme.AbrilFatfaceFont
import com.l0124005.sewain_rpl.ui.theme.LatoFont
import com.l0124005.sewain_rpl.ui.theme.MonsterratFont
import com.l0124005.sewain_rpl.ui.theme.VolkhovFont
import com.l0124005.sewain_rpl.utils.CurrencyUtils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.l0124005.sewain_rpl.ui.theme.checkout.CheckoutFormData
import com.l0124005.sewain_rpl.ui.theme.checkout.ShippingMethod

// ── Tema warna & font (Sync dengan CheckoutPaymentScreen) ─────
// Already defined in CheckoutPaymentScreen.kt in the same package

// ── Data customer untuk struk ─────────────────────────────────
data class CustomerInfo(
    val nama: String,
    val metodePembayaran: String,
    val metodePengiriman: String,
    val alamat: String,
    val tanggalPembayaran: String
)

fun mapCheckoutResponseToState(
    response: com.l0124005.sewain_rpl.network.CheckoutResponse,
    formData: CheckoutFormData? = null
): PaymentConfirmedState? {
    val data = response.data ?: return null
    val firstTx = data.transaksi.firstOrNull() ?: return null
    
    val displayFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())

    val items = data.transaksi.map { tx ->
        val startDate = com.l0124005.sewain_rpl.utils.RentalStatus.parseFlexibleDate(tx.tanggal_sewa)
        val endDate = com.l0124005.sewain_rpl.utils.RentalStatus.parseFlexibleDate(tx.tanggal_kembali_rencana)

        CartItem(
            id = tx.id,
            nama = tx.barang?.nama_barang ?: "Produk",
            harga = tx.barang?.harga_sewa?.toLong() ?: 0L,
            qty = tx.jumlah,
            imageUrl = if (tx.barang?.foto_barang != null) "${ApiClient.IMAGE_BASE_URL}${tx.barang.foto_barang}" else "",
            tglMulai = startDate?.let { displayFormat.format(it) } ?: tx.tanggal_sewa,
            tglSelesai = endDate?.let { displayFormat.format(it) } ?: tx.tanggal_kembali_rencana,
            dendaPerJam = tx.barang?.harga_denda_perjam?.toLong() ?: 0L,
            jaminanBase = tx.barang?.harga_jaminan?.toLong() ?: 0L
        )
    }

    val dateFormat = java.text.SimpleDateFormat("EEEE, d MMMM yyyy HH:mm", java.util.Locale("id", "ID"))
    val currentDateTime = dateFormat.format(java.util.Date())

    val customer = CustomerInfo(
        nama = formData?.let { "${it.firstName} ${it.lastName}" } ?: firstTx.user?.name ?: "Customer",
        metodePembayaran = formData?.paymentDetail ?: firstTx.pembayaran?.metode ?: "-",
        metodePengiriman = formData?.shipping?.name ?: (if (firstTx.user?.alamat.isNullOrBlank()) "COD" else "Delivery"),
        alamat = if (formData?.shipping == ShippingMethod.DELIVERY) {
            "${formData.address}, ${formData.cityProvince} ${formData.postalCode}"
        } else {
            firstTx.user?.alamat ?: "-"
        },
        tanggalPembayaran = currentDateTime
    )

    // Hitung total ongkir jika ada
    val totalShipping = if (formData?.shipping == ShippingMethod.DELIVERY) 40000L else 0L

    return PaymentConfirmedState(
        orderNumber = firstTx.pembayaran_id?.toString() ?: firstTx.id.toString(),
        items = items,
        shippingCost = totalShipping,
        customer = customer
    )
}

data class PaymentConfirmedState(
    val orderNumber: String,
    val items: List<CartItem>,
    val shippingCost: Long,
    val customer: CustomerInfo
)

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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
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

        // ── Konfirmasi Status ──
        ConfirmationStatus(
            orderNumber = orderNumber,
            onBackHome = onBackHome,
            onPrintReceipt = onPrintReceipt
        )

        Spacer(Modifier.height(32.dp))

        // ── Struk Belanja ──
        ReceiptCard(
            items = items,
            shippingCost = shippingCost,
            customer = customer
        )

        Spacer(Modifier.height(32.dp))
    }
}

// ── Status konfirmasi ──
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
            fontWeight = FontWeight.ExtraBold,
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
                fontWeight = FontWeight.SemiBold,
                color = CkColors.Blue,
                fontFamily = CkBody,
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "Thank you for renting with SEWAIN. Your transaction has been successfully processed. Please check your email for the detailed receipt and rental agreement.",
            fontSize = 13.sp,
            color = Color(0xFF818181),
            fontFamily = AbrilFatfaceFont,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onBackHome,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CkColors.Blue),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Back to SEWAIN Home", color = Color.White, fontFamily = CkBody, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
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

// ── Kartu struk lengkap ──
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
        items.forEach { item ->
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
                        placeholder = painterResource(id = R.drawable.ic_launcher_background),
                        error = painterResource(id = R.drawable.ic_launcher_background),
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
            // Badge qty
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
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp,
                    color = Color.White
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Badge PAID
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

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 14.dp)
        ) {
            ReceiptDateBox("Tanggal Mulai Penyewaan", item.tglMulai, Modifier.weight(1f))
            ReceiptDateBox("Tanggal Selesai Penyewaan", item.tglSelesai, Modifier.weight(1f))
        }

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
                fontWeight = FontWeight.Black,
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
            fontWeight = FontWeight.Normal,
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
            fontWeight = FontWeight.SemiBold,
            fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
            color = CkColors.Dark,
            modifier = Modifier.weight(2f)
        )
        Text(
            value,
            fontFamily = CkBody,
            fontSize = if (bold) 12.sp else 11.sp,
            fontWeight = FontWeight.SemiBold,
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

        CustomerInfoItem("Nama Customer", customer.nama)
        Spacer(Modifier.height(12.dp))
        CustomerInfoItem("Metode Pembayaran", customer.metodePembayaran)
        Spacer(Modifier.height(12.dp))

        val pengirimanValue = if (customer.metodePengiriman.contains("COD", ignoreCase = true)) {
            "${customer.metodePengiriman} - Ambil sendiri di lokasi SEWAIN"
        } else {
            "${customer.metodePengiriman} - ${customer.alamat.ifBlank { "-" }}"
        }
        CustomerInfoItem("Metode Pengiriman", pengirimanValue)

        Spacer(Modifier.height(12.dp))
        CustomerInfoItem("Tanggal Pembayaran", customer.tanggalPembayaran)

        Spacer(Modifier.height(20.dp))
        HorizontalDivider(color = CkColors.Border, thickness = 1.dp)

        Box(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row {
                Text("SEWA", fontFamily = CkVolkhov, fontSize = 26.sp, color = CkColors.Dark)
                Text("IN", fontFamily = CkVolkhov, fontSize = 26.sp, color = Color(0xFF6A87A1))
            }
        }
    }
}

@Composable
private fun CustomerInfoItem(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
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
        )
    )

    MaterialTheme {
        Surface(color = Color.White, modifier = Modifier.fillMaxSize()) {
            PaymentConfirmedScreen(
                orderNumber = "0000",
                items = dummyItems,
                shippingCost = 40000L,
                customer = CustomerInfo(
                    nama = "Aprilia Afia",
                    metodePembayaran = "Transfer Bank — BCA",
                    metodePengiriman = "Delivery",
                    alamat = "Jl. Bondong, 45, Brigie",
                    tanggalPembayaran = "Kamis, 11 Maret 2026 — 05:46 AM"
                ),
                onBackHome = {}
            )
        }
    }
}
