package com.l0124005.sewain_rpl.ui.theme.transaksi

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.l0124005.sewain_rpl.network.BayarRequest
import com.l0124005.sewain_rpl.ui.theme.SewainTopBar
import com.l0124005.sewain_rpl.ui.theme.BluePrimary
import com.l0124005.sewain_rpl.ui.theme.katalog.formatRupiah
import com.l0124005.sewain_rpl.utils.Resource
import com.l0124005.sewain_rpl.viewmodel.PaymentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PembayaranScreen(
    viewModel: PaymentViewModel,
    token: String,
    transaksiIds: List<Int>,
    onBack: () -> Unit,
    onPaymentSuccess: () -> Unit
) {
    var selectedMetode by remember { mutableStateOf("Transfer Bank") }
    val pembayaranState by viewModel.pembayaranState.observeAsState()

    LaunchedEffect(pembayaranState) {
        if (pembayaranState is Resource.Success) {
            onPaymentSuccess()
        }
    }

    Scaffold(
        topBar = {
            SewainTopBar(
                onNavigationClick = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Pilih Metode Pembayaran", fontWeight = FontWeight.Bold, fontSize = 16.sp)

            val metodeList = listOf("Transfer Bank", "E-Wallet (Gopay/OVO)", "Saldo App")
            metodeList.forEach { metode ->
                MetodeItem(
                    metode = metode,
                    isSelected = selectedMetode == metode,
                    onClick = { selectedMetode = metode }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            if (pembayaranState is Resource.Error) {
                Text(pembayaranState?.message ?: "Terjadi kesalahan", color = Color.Red)
            }

            Button(
                onClick = {
                    val metodeRequest = when (selectedMetode) {
                        "Transfer Bank" -> "transfer bank"
                        "E-Wallet (Gopay/OVO)" -> "e-wallet"
                        "Saldo App" -> "qris" // Mapping ke qris karena backend hanya terima: transfer bank, e-wallet, qris
                        else -> selectedMetode.lowercase()
                    }
                    viewModel.bayar(
                        token,
                        BayarRequest(
                            transaksi_ids = transaksiIds,
                            metode = metodeRequest,
                            detail_metode = "Pembayaran via $selectedMetode"
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                enabled = pembayaranState !is Resource.Loading
            ) {
                if (pembayaranState is Resource.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Bayar Sekarang", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun MetodeItem(metode: String, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) BluePrimary else Color.LightGray,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(metode, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
            if (isSelected) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BluePrimary)
            }
        }
    }
}
