package com.l0124005.sewain_rpl.ui.theme.Checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.l0124005.sewain_rpl.R
import com.l0124005.sewain_rpl.ui.theme.katalog.*
import com.l0124005.sewain_rpl.utils.CurrencyUtils
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.l0124005.sewain_rpl.ui.theme.Checkout.CartItem



val MonsterratFont = FontFamily(Font(R.font.montserrat))
val VolkhovFont = FontFamily(Font(R.font.volkhov_regular))

// ── Tema warna & font (sama dengan checkout.css) ──────────────
object CkColors {
    val Blue  = Color(0xFF6A87A1)
    val Black = Color(0xFF000000)
    val Dark  = Color(0xFF484848)
    val Gray  = Color(0xFF8A8A8A)
    val Border = Color(0xFFE0E0E0)
    val PanelBg = Color(0xFFF5F5F5)
}
val CkVolkhov = FontFamily.Serif   // Volkhov fallback
val CkBody    = FontFamily.Default // Poppins fallback

enum class ShippingMethod { COD, DELIVERY }
enum class PaymentType { CREDIT, QRIS, TRANSFER, EWALLET }


data class BankOption(val name: String, val rekening: String, val logoRes: Int? = null)
data class EwalletOption(val name: String, val nomor: String, val logoRes: Int? = null)

val bankOptions = listOf(
    BankOption("BCA", "0978 5634 21196", R.drawable.bca),
    BankOption("Mandiri", "4321 8765 4321", R.drawable.mandiri),
    BankOption("BRI", "8765 4321 8765", R.drawable.bri),
    BankOption("BNI", "1234 9999 0011", R.drawable.bni),
    BankOption("BSI", "1234 9999 0011", R.drawable.bsi)
)

val ewalletOptions = listOf(
    EwalletOption("GoPay", "0858 4619 7216", R.drawable.gopay),
    EwalletOption("OVO", "0858 4619 7216", R.drawable.ovo),
    EwalletOption("ShopeePay", "0858 4619 7216", R.drawable.spay),
    EwalletOption("Dana", "0858 4619 7216",  R.drawable.dana)
)

// ── Screen utama ──────────────────────────────────────────────
@Composable
fun CheckoutPaymentScreen(
    items: List<CartItem>,
    onPay: (CheckoutFormData) -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var lastName  by remember { mutableStateOf("") }
    var email     by remember { mutableStateOf("") }
    var phone     by remember { mutableStateOf("") }

    var shipping by remember { mutableStateOf(ShippingMethod.COD) }
    var address by remember { mutableStateOf("") }
    var cityProvince by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }

    var paymentType by remember { mutableStateOf(PaymentType.CREDIT) }
    var cardNumber by remember { mutableStateOf("") }
    var expDate by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var cardHolder by remember { mutableStateOf("") }

    val shippingCost = if (shipping == ShippingMethod.DELIVERY) 40000L else 0L

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {

        Text(
            "Checkout Payment",
            fontFamily = CkVolkhov,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            color = CkColors.Black,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp)
        )

        // ── Section: Contact ──
        SectionTitle("Contact")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CkTextField(firstName, { firstName = it }, "First Name", Modifier.weight(1f))
            CkTextField(lastName, { lastName = it }, "Last Name", Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        CkTextField(email, { email = it }, "Email Address", keyboardType = KeyboardType.Email)
        Spacer(Modifier.height(10.dp))
        CkTextField(phone, { phone = it }, "Phone Number", keyboardType = KeyboardType.Phone)

        Spacer(Modifier.height(24.dp))

        // ── Section: Metode Pengiriman ──
        SectionTitle("Metode Pengiriman")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ShippingToggleButton(
                label = "COD (Ambil Sendiri)",
                selected = shipping == ShippingMethod.COD,
                onClick = { shipping = ShippingMethod.COD },
                modifier = Modifier.weight(1f)
            )
            ShippingToggleButton(
                label = "Delivery",
                selected = shipping == ShippingMethod.DELIVERY,
                onClick = { shipping = ShippingMethod.DELIVERY },
                modifier = Modifier.weight(1f)
            )
        }

        // ── Delivery form (muncul jika Delivery dipilih) ──
        if (shipping == ShippingMethod.DELIVERY) {
            Spacer(Modifier.height(14.dp))
            CkTextField(address, { address = it }, "Address")
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CkTextField(cityProvince, { cityProvince = it }, "City & Province", Modifier.weight(1f))
                CkTextField(postalCode, { postalCode = it }, "Kode Pos", Modifier.weight(1f))
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── Section: Payment Method ──
        SectionTitle("Payment Method")
        Text(
            "Make your payment directly into our bank account. Please use your Order ID as the payment reference. Your order will not be shipped until the funds have cleared in our account.",
            fontSize = 11.sp,
            color = CkColors.Gray,
            fontFamily = CkBody,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        PaymentMethodSelector(
            selected = paymentType,
            onSelect = { paymentType = it }
        )

        Spacer(Modifier.height(10.dp))

        // ── Panel sesuai metode pembayaran ──
        when (paymentType) {
            PaymentType.CREDIT -> CreditCardPanel(
                cardNumber, { cardNumber = it },
                expDate, { expDate = it },
                cvv, { cvv = it },
                cardHolder, { cardHolder = it }
            )
            PaymentType.QRIS -> QrisPanel(nominal = totalAll(items, shippingCost))
            PaymentType.TRANSFER -> BankTransferPanel(nominal = totalAll(items, shippingCost))
            PaymentType.EWALLET -> EwalletPanel(nominal = totalAll(items, shippingCost))
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                onPay(
                    CheckoutFormData(
                        firstName, lastName, email, phone,
                        shipping, address, cityProvince, postalCode,
                        paymentType, cardNumber, expDate, cvv, cardHolder
                    )
                )
            },
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CkColors.Blue),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Pay Now", color = Color.White, fontFamily = MonsterratFont, fontSize = 14.sp)
        }

        Text(
            "© 2026 SEWAIN · All Rights Reserved · Secure Payment",
            fontSize = 10.sp,
            color = CkColors.Gray,
            fontFamily = CkBody,
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(Modifier.height(20.dp))

        // ── Order summary (LENGKAP — foto, tanggal, durasi, jaminan, dll) ──
        Text(
            "Order Summary",
            fontFamily = CkVolkhov,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,  // dari 16.sp jadi 20.sp, atur sesuai selera
            color = CkColors.Dark,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        for (item in items) {
            SummaryProductCard(item = item, shippingCost = shippingCost)
            Spacer(Modifier.height(16.dp))
        }
        SummaryTotalRow(grandTotal = totalAll(items, shippingCost))

        // Catatan privasi — sama seperti .summary-note di checkout.html
        Text(
            "Your personal data will be used to support your experience throughout this website.",
            fontSize = 11.sp,
            color = CkColors.Gray,
            fontFamily = CkBody,
            lineHeight = 17.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 4.dp)
        )
        Spacer(Modifier.height(32.dp))
    }
}

fun totalAll(items: List<CartItem>, shippingCost: Long): Long {
    var total: Long = 0
    for (item in items) {
        val durasi = item.durasiHari()
        total += item.subtotal(durasi) + item.jaminan()
    }
    return total + shippingCost
}

data class CheckoutFormData(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val shipping: ShippingMethod,
    val address: String,
    val cityProvince: String,
    val postalCode: String,
    val paymentType: PaymentType,
    val cardNumber: String,
    val expDate: String,
    val cvv: String,
    val cardHolder: String
)

// ── Komponen kecil form ──────────────────────────────────────

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        fontFamily = CkVolkhov,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = CkColors.Dark,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
private fun CkTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, fontSize = 12.sp, color = CkColors.Gray, fontFamily = CkBody) },
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = CkColors.Dark, fontFamily = CkBody),
        shape = RoundedCornerShape(0.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CkColors.Blue,
            unfocusedBorderColor = CkColors.Gray,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        modifier = modifier.fillMaxWidth().height(48.dp)
    )
}

@Composable
private fun ShippingToggleButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (selected) CkColors.Blue else CkColors.Border
    val bgColor = if (selected) Color(0xFFF0F5F9) else Color.White
    val textColor = if (selected) CkColors.Blue else CkColors.Dark

    Box(
        modifier = modifier
            .border(width = 1.5.dp, color = borderColor, shape = RoundedCornerShape(0.dp))
            .background(bgColor)
            .clickableNoRipple(onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = textColor,
            fontFamily = CkBody
        )
    }
}

@Composable
private fun PaymentMethodSelector(
    selected: PaymentType,
    onSelect: (PaymentType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val (label, sub) = when (selected) {
        PaymentType.CREDIT -> "Credit Card" to "Visa, Mastercard, dll"
        PaymentType.QRIS -> "QRIS" to "Scan QR untuk bayar"
        PaymentType.TRANSFER -> "Transfer Bank" to "BCA, Mandiri, BRI, BNI"
        PaymentType.EWALLET -> "E-Wallet" to "GoPay, OVO, ShopeePay, Dana"
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, CkColors.Blue, RoundedCornerShape(0.dp))
                .background(Color(0xFFF5F5F5))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RadioButton(selected = true, onClick = {}, colors = RadioButtonDefaults.colors(selectedColor = CkColors.Blue))
                Column {
                    Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CkColors.Dark, fontFamily = CkBody)
                    Text(sub, fontSize = 11.sp, color = CkColors.Gray, fontFamily = CkBody)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) { // ← jarak diperbesar di sini
                PaymentLogoBox(
                    logoRes = when (selected) {
                        PaymentType.CREDIT -> R.drawable.visa   // atau logo kartu kredit kamu
                        PaymentType.QRIS -> R.drawable.logo_qris
                        PaymentType.TRANSFER -> R.drawable.logo_bank_tf
                        PaymentType.EWALLET -> R.drawable.logo_ewallet
                    }
                )
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Pilih metode lain", tint = CkColors.Dark)
                }
            }
        }

        if (expanded) {
            Spacer(Modifier.height(6.dp))
            PaymentType.entries.filter { it != selected }.forEach { type ->
                val (l, s) = when (type) {
                    PaymentType.CREDIT -> "Credit Card" to "Visa, Mastercard, dll"
                    PaymentType.QRIS -> "QRIS" to "Scan QR untuk bayar"
                    PaymentType.TRANSFER -> "Transfer Bank" to "BCA, Mandiri, BRI, BNI"
                    PaymentType.EWALLET -> "E-Wallet" to "GoPay, OVO, ShopeePay, Dana"
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, CkColors.Border, RoundedCornerShape(0.dp))
                        .background(Color.White)
                        .clickableNoRipple { onSelect(type); expanded = false }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = false,
                        onClick = { onSelect(type); expanded = false },
                        colors = RadioButtonDefaults.colors(unselectedColor = CkColors.Gray)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {  // ← tambah weight di sini
                        Text(l, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CkColors.Dark, fontFamily = CkBody)
                        Text(s, fontSize = 11.sp, color = CkColors.Gray, fontFamily = CkBody)
                    }
                    // ↓ INI YANG DITAMBAH
                    PaymentLogoBox(
                        logoRes = when (type) {
                            PaymentType.CREDIT -> R.drawable.visa
                            PaymentType.QRIS -> R.drawable.logo_qris
                            PaymentType.TRANSFER -> R.drawable.logo_bank_tf
                            PaymentType.EWALLET -> R.drawable.logo_ewallet
                        }
                    )
                }
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun CreditCardPanel(
    cardNumber: String, onCardNumber: (String) -> Unit,
    expDate: String, onExpDate: (String) -> Unit,
    cvv: String, onCvv: (String) -> Unit,
    cardHolder: String, onCardHolder: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CkColors.PanelBg)
            .border(1.dp, CkColors.Border)
            .padding(16.dp)
    ) {
        CkTextField(cardNumber, onCardNumber, "Card Number", keyboardType = KeyboardType.Number)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CkTextField(expDate, onExpDate, "Expiration Date (MM/YY)", Modifier.weight(1f))
            CkTextField(cvv, onCvv, "Security Code", Modifier.weight(1f), KeyboardType.Number)
        }
        Spacer(Modifier.height(10.dp))
        CkTextField(cardHolder, onCardHolder, "Card Holder Name")
        Spacer(Modifier.height(8.dp))
        SaveInfoCheckbox()
    }
}

@Composable
private fun QrisPanel(nominal: Long) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CkColors.PanelBg)
            .border(1.dp, CkColors.Border)
            .padding(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color(0xFF333333))
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                PanelInfoRow("A/n", "SEWAIN")
                PanelInfoRow("Nominal", currencyOrFallback(nominal))
            }
        }
        Spacer(Modifier.height(12.dp))
        SaveInfoCheckbox()
    }
}

@Composable
private fun BankTransferPanel(nominal: Long) {
    var selectedBank by remember { mutableStateOf(bankOptions.first()) }
    var dropdownOpen by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CkColors.PanelBg)
            .border(1.dp, CkColors.Border)
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CkColors.Border)
                    .background(Color.White)
                    .clickableNoRipple { dropdownOpen = !dropdownOpen }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(selectedBank.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CkColors.Dark, fontFamily = CkBody)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PaymentLogoBox(selectedBank.logoRes)
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = CkColors.Dark)
                }
            }
            if (dropdownOpen) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CkColors.Border)
                        .background(Color.White)
                ) {
                    bankOptions.filter { it != selectedBank }.forEach { opt ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickableNoRipple { selectedBank = opt; dropdownOpen = false }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(opt.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CkColors.Dark, fontFamily = CkBody)
                            PaymentLogoBox(opt.logoRes)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        PanelInfoRow("Nomor Rekening", selectedBank.rekening)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(modifier = Modifier.weight(1f)) { PanelInfoRow("A/n", "SEWAIN") }
            Box(modifier = Modifier.weight(1f)) { PanelInfoRow("Nominal", currencyOrFallback(nominal)) }
        }
        Spacer(Modifier.height(8.dp))
        SaveInfoCheckbox()
    }
}

@Composable
private fun EwalletPanel(nominal: Long) {
    var selectedEwallet by remember { mutableStateOf(ewalletOptions.first()) }
    var dropdownOpen by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CkColors.PanelBg)
            .border(1.dp, CkColors.Border)
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CkColors.Border)
                    .background(Color.White)
                    .clickableNoRipple { dropdownOpen = !dropdownOpen }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(selectedEwallet.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CkColors.Dark, fontFamily = CkBody)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PaymentLogoBox(selectedEwallet.logoRes)
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = CkColors.Dark)
                }
            }
            if (dropdownOpen) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CkColors.Border)
                        .background(Color.White)
                ) {
                    ewalletOptions.filter { it != selectedEwallet }.forEach { opt ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickableNoRipple { selectedEwallet = opt; dropdownOpen = false }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(opt.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CkColors.Dark, fontFamily = CkBody)
                            PaymentLogoBox(opt.logoRes)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        PanelInfoRow("Nomor E-Wallet", selectedEwallet.nomor)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(modifier = Modifier.weight(1f)) { PanelInfoRow("A/n", "SEWAIN") }
            Box(modifier = Modifier.weight(1f)) { PanelInfoRow("Nominal", currencyOrFallback(nominal)) }
        }
        Spacer(Modifier.height(8.dp))
        SaveInfoCheckbox()
    }
}

@Composable
private fun PaymentLogoBox(logoRes: Int? = null) {
    Box(
        modifier = Modifier
            .size(width = 40.dp, height = 25.dp)
            .border(1.dp, CkColors.Border, RoundedCornerShape(4.dp))
            .background(Color.White, RoundedCornerShape(4.dp))
    ) {
        if (logoRes != null) {
            Image(
                painter = painterResource(id = logoRes),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
            )
        }
    }
}
@Composable
private fun PanelInfoRow(label: String, value: String) {
    Column {
        Text(label, fontSize = 10.sp, color = CkColors.Gray, fontFamily = CkBody)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF9F9F9))
                .border(1.dp, Color(0xFFE8E8E8))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CkColors.Gray, fontFamily = CkBody)
        }
    }
}

@Composable
private fun SaveInfoCheckbox() {
    var checked by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = checked,
            onCheckedChange = { checked = it },
            colors = CheckboxDefaults.colors(checkedColor = CkColors.Blue),
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text("Save This Info For Future", fontSize = 11.sp, color = CkColors.Gray, fontFamily = CkBody)
    }
}

private fun currencyOrFallback(value: Long): String =
    CurrencyUtils.formatRupiah(value)

fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier = composed {
    this.then(
        Modifier.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() },
            onClick = onClick
        )
    )
}

/* ────────────────────────────────────────────────────────────
 *  SUMMARY PRODUCT CARD — versi LENGKAP (foto, badge qty, tanggal,
 *  durasi, subtotal, shipping, jaminan, total, catatan denda)
 *  Meniru blok `.summary-product` di checkout.html
 * ──────────────────────────────────────────────────────────── */
@Composable
fun SummaryProductCard(
    item: CartItem,
    shippingCost: Long,
    modifier: Modifier = Modifier
) {
    val durasi = item.durasiHari()
    val subtotal = item.subtotal(durasi)
    val jaminan = item.jaminan()
    val total = subtotal + shippingCost + jaminan

    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = CkColors.Border)
            .padding(18.dp)
    ) {
        // Header: gambar + badge qty + nama + harga
        Box {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top,
                modifier = Modifier.padding(bottom = 14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
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
                        color = CkColors.Black
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
            // Badge jumlah item, overlap di pojok kiri-atas foto
            Box(
                modifier = Modifier
                    .offset(x = (-6).dp, y = (-6).dp)
                    .size(20.dp)
                    .clip(RoundedCornerShape(50)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CkColors.Blue, RoundedCornerShape(50)),
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
        }

        // Dua kotak tanggal: mulai & selesai
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 14.dp)
        ) {
            SummaryDateBox("Tanggal Mulai Penyewaan", item.tglMulai, Modifier.weight(1f))
            SummaryDateBox("Tanggal Selesai Penyewaan", item.tglSelesai, Modifier.weight(1f))
        }

        // Baris-baris rincian harga
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp))
        {
            SummaryRow("Durasi Sewa", "$durasi Hari")
            SummaryRow("Subtotal", CurrencyUtils.formatRupiah(subtotal))
            SummaryRow("Shipping", if (shippingCost > 0) CurrencyUtils.formatRupiah(shippingCost) else "-")
            SummaryRow("Jaminan", CurrencyUtils.formatRupiah(jaminan))
            SummaryRow("Total", CurrencyUtils.formatRupiah(total), bold = true)
            SummaryRow("#Catatan Denda", "${CurrencyUtils.formatRupiah(item.dendaPerJam)}/jam", italic = true)
        }
    }
}

@Composable
private fun SummaryDateBox(label: String, value: String, modifier: Modifier = Modifier) {
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
                fontFamily = CkBody,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                color = CkColors.Black,
                maxLines = 2
            )
        }
        Spacer(Modifier.height(3.dp))
        Text(
            value,
            fontFamily = CkBody,
            fontWeight = FontWeight.Normal,
            fontSize = 11.sp,
            color = CkColors.Black
        )
    }
}

@Composable
private fun SummaryRow(label: String, value: String, bold: Boolean = false, italic: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,  // ← ganti spacedBy jadi SpaceBetween
        verticalAlignment = Alignment.CenterVertically      // ← tambah ini
    ) {
        Text(
            label,
            fontFamily = CkBody,
            fontSize = 11.sp,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
            color = CkColors.Dark,
            modifier = Modifier.weight(2f)   // ← ganti width(110.dp) jadi weight(1f)
        )
        Text(
            value,
            fontFamily = CkBody,
            fontSize = 11.sp,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium,
            fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
            color = CkColors.Dark,
            textAlign = androidx.compose.ui.text.style.TextAlign.Start,  // ← tambah ini
            modifier = Modifier.weight(1f)   // ← tambah weight(1f)
        )
    }
}

/* ────────────────────────────────────────────────────────────
 *  SUMMARY TOTAL — meniru .summary-total (background abu, label kiri,
 *  pill biru di kanan menampilkan grand total)
 * ──────────────────────────────────────────────────────────── */
@Composable
fun SummaryTotalRow(grandTotal: Long, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(CkColors.PanelBg)
            .border(width = 1.5.dp, color = CkColors.Border)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Total",
            fontFamily = MonsterratFont,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = CkColors.Dark
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(CkColors.Blue)
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                CurrencyUtils.formatRupiah(grandTotal),
                fontFamily = MonsterratFont,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 1600)
@Composable
fun CheckoutPaymentScreenPreview() {
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
            CheckoutPaymentScreen(
                items = dummyItems,
                onPay = {}
            )
        }
    }
}