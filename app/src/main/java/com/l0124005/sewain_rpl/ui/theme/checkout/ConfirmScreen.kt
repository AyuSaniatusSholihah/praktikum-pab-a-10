package com.l0124005.sewain_rpl.ui.theme.checkout

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.l0124005.sewain_rpl.network.CheckoutResponse
import com.l0124005.sewain_rpl.utils.CurrencyUtils

@Composable
fun PaymentConfirmedScreen(
    state: PaymentConfirmedState,
    onBackToHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(CkColors.Blue),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Payment Confirmed!", fontFamily = VolkhovFont, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = CkColors.Black)
        Text("Your order has been placed successfully", fontFamily = MonsterratFont, fontSize = 14.sp, color = CkColors.Gray)

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("ORDER RECEIPT", fontFamily = VolkhovFont, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CkColors.Dark, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(20.dp))

                ReceiptInfoRow("Order ID", state.orderId)
                ReceiptInfoRow("Date", state.date)
                
                Spacer(modifier = Modifier.height(16.dp))
                DashedLine()
                Spacer(modifier = Modifier.height(16.dp))

                state.items.forEach { item ->
                    ConfirmedProductRow(item)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))
                DashedLine()
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Amount", fontFamily = MonsterratFont, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CkColors.Black)
                    Text(CurrencyUtils.formatRupiah(state.totalAmount), fontFamily = MonsterratFont, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CkColors.Blue)
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onBackToHome,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CkColors.Blue)
        ) {
            Text("Back to Home", fontFamily = MonsterratFont, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
        }
    }
}

@Composable
private fun ReceiptInfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontFamily = MonsterratFont, fontSize = 13.sp, color = CkColors.Gray)
        Text(value, fontFamily = MonsterratFont, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = CkColors.Dark)
    }
}

@Composable
private fun ConfirmedProductRow(item: ConfirmedItem) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.name, fontFamily = MonsterratFont, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CkColors.Dark)
            Text("${item.qty} x ${CurrencyUtils.formatRupiah(item.price)}", fontFamily = MonsterratFont, fontSize = 12.sp, color = CkColors.Gray)
        }
        Text(CurrencyUtils.formatRupiah(item.subtotal), fontFamily = MonsterratFont, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CkColors.Dark)
    }
}

@Composable
private fun DashedLine() {
    Canvas(Modifier.fillMaxWidth().height(1.dp)) {
        drawLine(
            color = CkColors.Border,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )
    }
}

data class PaymentConfirmedState(val orderId: String, val date: String, val items: List<ConfirmedItem>, val totalAmount: Long)
data class ConfirmedItem(val name: String, val price: Long, val qty: Int, val subtotal: Long)

fun mapCheckoutResponseToState(response: CheckoutResponse): PaymentConfirmedState {
    val listTransaksi = response.data?.transaksi ?: emptyList()
    
    val items = listTransaksi.map { t ->
        ConfirmedItem(
            name = t.barang?.nama_barang ?: "Produk",
            price = t.barang?.harga_sewa?.toLong() ?: 0L,
            qty = t.jumlah,
            subtotal = t.total_harga.toLong()
        )
    }

    val firstT = listTransaksi.firstOrNull()
    val totalAll = listTransaksi.sumOf { it.total_harga.toLong() }

    return PaymentConfirmedState(
        orderId = "#${firstT?.id ?: "0000"}",
        date = firstT?.tanggal_sewa ?: "-",
        items = items,
        totalAmount = totalAll
    )
}

@Preview(showBackground = true)
@Composable
fun PaymentConfirmedScreenPreview() {
    val dummyState = PaymentConfirmedState(
        orderId = "#SW-2026-001",
        date = "30 July 2026",
        items = listOf(
            ConfirmedItem("Camera Canon EOS", 150000, 1, 150000),
            ConfirmedItem("Tripod GorillaPod", 50000, 1, 50000)
        ),
        totalAmount = 200000
    )
    MaterialTheme {
        PaymentConfirmedScreen(state = dummyState, onBackToHome = {})
    }
}
