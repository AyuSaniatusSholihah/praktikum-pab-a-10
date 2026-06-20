package com.l0124005.sewain_rpl.ui.theme.checkout

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.l0124005.sewain_rpl.R
import com.l0124005.sewain_rpl.network.KeranjangResponse
import com.l0124005.sewain_rpl.network.ProfileResponse
import com.l0124005.sewain_rpl.network.UserData
import com.l0124005.sewain_rpl.utils.CurrencyUtils
import com.l0124005.sewain_rpl.utils.Resource
import com.l0124005.sewain_rpl.ui.theme.MonsterratFont
import com.l0124005.sewain_rpl.ui.theme.VolkhovFont
import com.l0124005.sewain_rpl.viewmodel.KeranjangViewModel
import com.l0124005.sewain_rpl.viewmodel.ProfileViewModel
import com.l0124005.sewain_rpl.viewmodel.TransaksiViewModel

object CkColors {
    val Blue  = Color(0xFF6A87A1)
    val Black = Color(0xFF000000)
    val Dark  = Color(0xFF484848)
    val Gray  = Color(0xFF8A8A8A)
    val Border = Color(0xFFE0E0E0)
    val PanelBg = Color(0xFFF5F5F5)
}

enum class ShippingMethod { COD, DELIVERY }
enum class PaymentType { CREDIT, QRIS, TRANSFER, EWALLET }

@Composable
fun CheckoutPaymentScreen(
    token: String,
    keranjangViewModel: KeranjangViewModel,
    profileViewModel: ProfileViewModel,
    transaksiViewModel: TransaksiViewModel,
    onEditProfile: () -> Unit
) {
    val keranjangState by keranjangViewModel.keranjang.observeAsState()
    val profileState by profileViewModel.profile.observeAsState()

    val items = remember(keranjangState) {
        val data = (keranjangState as? Resource.Success)?.data?.data?.items
        data?.map { it ->
            CartItem(
                id = it.id,
                nama = it.barang.nama_barang,
                harga = it.barang.harga_sewa.toLong(),
                qty = it.jumlah,
                imageUrl = it.barang.foto_barang ?: "",
                tglMulai = it.tanggal_sewa,
                tglSelesai = it.tanggal_kembali_rencana,
                jaminanBase = it.barang.harga_jaminan.toLong()
            )
        } ?: emptyList()
    }

    var firstName by remember { mutableStateOf("") }
    var lastName  by remember { mutableStateOf("") }
    var email     by remember { mutableStateOf("") }
    var phone     by remember { mutableStateOf("") }

    LaunchedEffect(profileState) {
        if (profileState is Resource.Success) {
            val p = (profileState as Resource.Success).data?.data
            if (p != null) {
                firstName = p.name.split(" ").firstOrNull() ?: ""
                lastName = p.name.split(" ").drop(1).joinToString(" ")
                email = p.email
                phone = p.phone_number ?: ""
            }
        }
    }

    var shipping by remember { mutableStateOf(ShippingMethod.COD) }
    var address by remember { mutableStateOf("") }
    var cityProvince by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }
    var paymentType by remember { mutableStateOf(PaymentType.CREDIT) }
    
    val shippingCost = if (shipping == ShippingMethod.DELIVERY) 40000L else 0L

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp).verticalScroll(rememberScrollState())) {
        Text("Checkout Payment", fontFamily = VolkhovFont, fontWeight = FontWeight.Bold, fontSize = 26.sp, color = CkColors.Black, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp))

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
        SectionTitle("Metode Pengiriman")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ShippingToggleButton("COD", shipping == ShippingMethod.COD, { shipping = ShippingMethod.COD }, Modifier.weight(1f))
            ShippingToggleButton("Delivery", shipping == ShippingMethod.DELIVERY, { shipping = ShippingMethod.DELIVERY }, Modifier.weight(1f))
        }

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
        SectionTitle("Payment Method")
        PaymentMethodSelector(paymentType, { paymentType = it })

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                transaksiViewModel.checkout(token)
            },
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CkColors.Blue),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Pay Now", color = Color.White, fontFamily = MonsterratFont, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        }

        Spacer(Modifier.height(24.dp))
        Text("Order Summary", fontFamily = VolkhovFont, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = CkColors.Dark)
        for (item in items) {
            SummaryProductCard(item = item, shippingCost = shippingCost)
            Spacer(Modifier.height(16.dp))
        }
        SummaryTotalRow(grandTotal = items.sumOf { (it.harga * it.qty) + it.jaminanBase } + shippingCost)
        Spacer(Modifier.height(40.dp))
    }
}

@Composable private fun SectionTitle(text: String) = Text(text, fontFamily = VolkhovFont, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CkColors.Dark, modifier = Modifier.padding(bottom = 12.dp))

@Composable
private fun CkTextField(value: String, onValueChange: (String) -> Unit, placeholder: String, modifier: Modifier = Modifier, keyboardType: KeyboardType = KeyboardType.Text) {
    OutlinedTextField(value = value, onValueChange = onValueChange, placeholder = { Text(placeholder, fontSize = 12.sp, color = CkColors.Gray) }, shape = RoundedCornerShape(0.dp), modifier = modifier.fillMaxWidth().height(52.dp), keyboardOptions = KeyboardOptions(keyboardType = keyboardType))
}

@Composable
private fun ShippingToggleButton(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.border(1.5.dp, if (selected) CkColors.Blue else CkColors.Border).background(if (selected) Color(0xFFF0F5F9) else Color.White).clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onClick() }.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (selected) CkColors.Blue else CkColors.Dark)
    }
}

@Composable
private fun PaymentMethodSelector(selected: PaymentType, onSelect: (PaymentType) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().border(1.5.dp, CkColors.Blue).background(Color(0xFFF5F5F5)).padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(selected.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CkColors.Dark)
        Icon(Icons.Default.KeyboardArrowDown, null)
    }
}

@Composable
fun SummaryProductCard(item: CartItem, shippingCost: Long) {
    val total = item.harga * item.qty + item.jaminanBase + shippingCost
    Column(modifier = Modifier.fillMaxWidth().border(1.dp, CkColors.Border).padding(18.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            AsyncImage(model = item.imageUrl, contentDescription = null, modifier = Modifier.size(80.dp).clip(RoundedCornerShape(4.dp)), contentScale = ContentScale.Crop)
            Column {
                Text(item.nama, fontFamily = VolkhovFont, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(CurrencyUtils.formatRupiah(item.harga)+"/hari", color = CkColors.Blue, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }
        Spacer(Modifier.height(12.dp))
        SummaryRow("Subtotal", CurrencyUtils.formatRupiah(item.harga * item.qty))
        SummaryRow("Total", CurrencyUtils.formatRupiah(total), true)
    }
}

@Composable
private fun SummaryRow(label: String, value: String, bold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 11.sp, fontWeight = if(bold) FontWeight.Bold else FontWeight.SemiBold)
        Text(value, fontSize = 11.sp, fontWeight = if(bold) FontWeight.Bold else FontWeight.SemiBold)
    }
}

@Composable
fun SummaryTotalRow(grandTotal: Long) {
    Row(modifier = Modifier.fillMaxWidth().background(CkColors.PanelBg).border(1.5.dp, CkColors.Border).padding(18.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Total", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Text(CurrencyUtils.formatRupiah(grandTotal), fontWeight = FontWeight.Bold, color = CkColors.Blue)
    }
}

data class CartItem(val id: Int, val nama: String, val harga: Long, val qty: Int, val imageUrl: String, val tglMulai: String, val tglSelesai: String, val jaminanBase: Long)

@Preview(showBackground = true)
@Composable
fun CheckoutUIPreview() {
    MaterialTheme {
        Text("UI Auth Preview")
    }
}
