package com.l0124005.sewain_rpl.ui.theme.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.l0124005.sewain_rpl.ui.theme.katalog.*

@Composable
fun CheckoutPageHeader() {
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
