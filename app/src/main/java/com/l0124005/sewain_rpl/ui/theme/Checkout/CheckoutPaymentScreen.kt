package com.l0124005.sewain_rpl.ui.theme.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.l0124005.sewain_rpl.ui.theme.katalog.*
import com.l0124005.sewain_rpl.utils.CurrencyUtils

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import com.l0124005.sewain_rpl.network.ApiClient
import com.l0124005.sewain_rpl.utils.Resource
import com.l0124005.sewain_rpl.viewmodel.KeranjangViewModel
import com.l0124005.sewain_rpl.viewmodel.ProfileViewModel
import com.l0124005.sewain_rpl.viewmodel.TransaksiViewModel

@Composable
fun CheckoutPaymentScreen(
    token: String,
    keranjangViewModel: KeranjangViewModel,
    profileViewModel: ProfileViewModel,
    transaksiViewModel: TransaksiViewModel,
    onEditProfile: () -> Unit
) {
    val keranjangData by keranjangViewModel.keranjang.observeAsState()
    val profileState by profileViewModel.profile.observeAsState()
    val checkoutState by transaksiViewModel.checkoutState.observeAsState()

    val isLoading = checkoutState is Resource.Loading

    LaunchedEffect(Unit) {
        if (profileState == null) {
            profileViewModel.getProfile(token)
        }
    }

    val items = (keranjangData as? Resource.Success)?.data?.data?.items ?: emptyList()
    val total = (keranjangData as? Resource.Success)?.data?.data?.total_estimasi?.toLong() ?: 0L

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color.White)
    ) {
        CheckoutPageHeader()

        // Shipping Address Section
        when (val state = profileState) {
            is Resource.Loading -> {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            }
            is Resource.Success -> {
                val user = state.data?.data
                if (user != null) {
                    ShippingAddressSection(
                        name = user.name,
                        phone = user.phone_number ?: "-",
                        address = user.alamat ?: "Alamat belum diatur",
                        onEditClick = onEditProfile
                    )
                }
            }
            is Resource.Error -> {
                Text(
                    "Gagal memuat alamat: ${state.message}",
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 12.sp
                )
            }
            else -> {}
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Product Summary Section
        items.forEach { item ->
            val cartItem = CartItem(
                id = item.id,
                nama = item.barang.nama_barang,
                harga = item.barang.harga_sewa.toLong(),
                qty = item.jumlah,
                imageUrl = if (item.barang.foto_barang != null) "${ApiClient.IMAGE_BASE_URL}${item.barang.foto_barang}" else "https://placehold.co/200",
                tglMulai = item.tanggal_sewa,
                tglSelesai = item.tanggal_kembali_rencana,
                dendaPerJam = item.barang.harga_denda_perjam.toLong(),
                jaminanBase = item.barang.harga_jaminan.toLong()
            )
            SummaryProductCard(
                item = cartItem,
                shippingCost = 0,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(16.dp))
        }

        SummaryTotalRow(grandTotal = total, modifier = Modifier.padding(horizontal = 16.dp))

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (!isLoading) {
                    transaksiViewModel.checkout(token)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(50.dp),
            enabled = !isLoading,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Bayar Sekarang", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun ShippingAddressSection(
    name: String,
    phone: String,
    address: String,
    onEditClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(1.dp, SectionBorder, RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Alamat Pengiriman",
                fontFamily = Volkhov,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Black
            )
            Text(
                "Ubah",
                color = Primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onEditClick() }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "$name | $phone",
                    style = CkHeading.copy(fontSize = 13.sp)
                )
                Text(
                    text = address,
                    style = CkBody,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

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
            .border(width = 1.dp, color = SectionBorder)
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
                        style = CkHeading.copy(fontSize = 13.sp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${CurrencyUtils.formatRupiah(item.harga)}/hari",
                        style = CkBody.copy(fontSize = 11.sp, color = Primary)
                    )
                }
            }
            // Badge jumlah item, posisinya overlap di pojok kiri-atas foto
            Box(
                modifier = Modifier
                    .offset(x = (-6).dp, y = (-6).dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${item.qty}",
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = White
                )
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
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SummaryRow("Durasi Sewa", "$durasi Hari")
            SummaryRow("Subtotal", CurrencyUtils.formatRupiah(subtotal))
            SummaryRow("Shipping", if (shippingCost > 0) CurrencyUtils.formatRupiah(shippingCost) else "-")
            SummaryRow("Jaminan", CurrencyUtils.formatRupiah(jaminan))
            SummaryRow("Total", CurrencyUtils.formatRupiah(total), bold = true)
            SummaryRow("#Catatan Denda", "${CurrencyUtils.formatRupiah(item.dendaPerJam)}/jam", italic = true)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SummaryProductCardPreview() {
    val dummyItem = CartItem(
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
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            SummaryProductCard(item = dummyItem, shippingCost = 40000)
            Spacer(Modifier.height(16.dp))
            SummaryTotalRow(grandTotal = 1750000)
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
                tint = Black,
                modifier = Modifier.size(13.dp)
            )
            Text(
                label,
                style = CkHeading.copy(fontSize = 11.sp, fontWeight = FontWeight.Black),
                maxLines = 2
            )
        }
        Spacer(Modifier.height(3.dp))
        Text(
            value,
            style = CkBody.copy(fontSize = 11.sp, color = Black)
        )
    }
}

@Composable
private fun SummaryRow(label: String, value: String, bold: Boolean = false, italic: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = CkBody.copy(
                fontSize = 11.sp,
                fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold,
                fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
                color = TextDark
            )
        )
        Text(
            value,
            style = CkBody.copy(
                fontSize = 11.sp,
                fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold,
                fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
                color = TextDark
            )
        )
    }
}

@Composable
fun SummaryTotalRow(grandTotal: Long, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(FormBg)
            .border(width = 1.5.dp, color = SectionBorder)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Total",
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = TextDark
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Primary)
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                CurrencyUtils.formatRupiah(grandTotal),
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = White
            )
        }
    }
}
