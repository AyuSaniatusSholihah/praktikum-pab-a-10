package com.l0124005.sewain_rpl.ui.theme.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

/* ────────────────────────────────────────────────────────────
 *  SUMMARY PRODUCT CARD
 *  Meniru blok `.summary-product` di HTML: foto + badge qty, nama, harga/hari,
 *  dua kotak tanggal (mulai/selesai), lalu baris-baris (durasi, subtotal, shipping,
 *  jaminan, total, catatan denda)
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
                        fontFamily = Volkhov,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Black
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${CurrencyUtils.formatRupiah(item.harga)}/hari",
                        fontFamily = FontFamily.Default,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Primary
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
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                color = Black,
                maxLines = 2
            )
        }
        Spacer(Modifier.height(3.dp))
        Text(
            value,
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 11.sp,
            color = Black
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
            fontFamily = FontFamily.Default,
            fontSize = 11.sp,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
            color = TextDark
        )
        Text(
            value,
            fontFamily = FontFamily.Default,
            fontSize = 11.sp,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium,
            fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
            color = TextDark
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