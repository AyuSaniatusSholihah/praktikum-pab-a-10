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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.l0124005.sewain_rpl.network.ApiClient
import com.l0124005.sewain_rpl.network.TransaksiData
import com.l0124005.sewain_rpl.network.VerifikasiPengembalianRequest
import com.l0124005.sewain_rpl.ui.theme.BluePrimary
import com.l0124005.sewain_rpl.ui.theme.NavyPrimary
import com.l0124005.sewain_rpl.ui.theme.SewainTopBar
import com.l0124005.sewain_rpl.utils.CurrencyUtils
import com.l0124005.sewain_rpl.utils.DateUtils
import com.l0124005.sewain_rpl.utils.RentalStatus
import com.l0124005.sewain_rpl.utils.Resource
import com.l0124005.sewain_rpl.utils.SessionManager
import com.l0124005.sewain_rpl.viewmodel.TransaksiViewModel
import com.l0124005.sewain_rpl.viewmodel.VerificationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailTransaksiScreen(
    viewModel: TransaksiViewModel,
    verificationViewModel: VerificationViewModel,
    token: String,
    transaksiId: Int,
    isOwner: Boolean = false,
    onBack: () -> Unit,
    onPayClick: (List<Int>) -> Unit,
    onProductClick: (Int) -> Unit
) {
    val transaksiDetailState by viewModel.transaksiDetail.observeAsState()
    val verifikasiState by verificationViewModel.verifikasiState.observeAsState()
    val context = LocalContext.current

    LaunchedEffect(transaksiId, isOwner) {
        if (isOwner) {
            viewModel.getOwnerTransaksiDetail(token, transaksiId)
        } else {
            viewModel.getDetailTransaksi(token, transaksiId)
        }
    }

    LaunchedEffect(verifikasiState) {
        if (verifikasiState is Resource.Success) {
            android.widget.Toast.makeText(context, "Berhasil memverifikasi pengembalian!", android.widget.Toast.LENGTH_SHORT).show()
            viewModel.getDetailTransaksi(token, transaksiId)
            verificationViewModel.resetState()
        } else if (verifikasiState is Resource.Error) {
            android.widget.Toast.makeText(context, "Gagal: ${verifikasiState?.message}", android.widget.Toast.LENGTH_LONG).show()
            verificationViewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            SewainTopBar(
                onNavigationClick = onBack
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (transaksiDetailState) {
                is Resource.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is Resource.Success -> {
                    val transaksi = transaksiDetailState?.data?.data
                    if (transaksi != null) {
                        TransaksiDetailContent(
                            transaksi = transaksi,
                            verificationViewModel = verificationViewModel,
                            token = token,
                            onPayClick = onPayClick,
                            onProductClick = onProductClick,
                            isLoadingVerification = verifikasiState is Resource.Loading
                        )
                    } else {
                        Text("Data tidak ditemukan", modifier = Modifier.align(Alignment.Center))
                    }
                }
                is Resource.Error -> {
                    Text(
                        text = transaksiDetailState?.message ?: "Error",
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                null -> {
                    // Initial or reset state
                }
            }
        }
    }
}

@Composable
fun TransaksiDetailContent(
    transaksi: TransaksiData,
    verificationViewModel: VerificationViewModel,
    token: String,
    onPayClick: (List<Int>) -> Unit,
    onProductClick: (Int) -> Unit,
    isLoadingVerification: Boolean
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val currentUserId = sessionManager.getUserId()
    
    val isOwner = transaksi.barang?.user_id == currentUserId
    val rentalStatus = RentalStatus.fromTransaksi(transaksi)
    
    // Status is waiting for verification if status is "dikembalikan" or "menunggu_verifikasi"
    // or if the photo proof of return is uploaded but verification date is null.
    val isWaitingVerification = rentalStatus == RentalStatus.RETURN

    var showVerificationDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = rentalStatus.badgeColor.copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Status", fontSize = 14.sp, color = Color.Gray)
                    Text(
                        rentalStatus.label.uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = rentalStatus.badgeTextColor
                    )
                }
                val imageUrl = if (transaksi.barang != null && !transaksi.barang.foto_barang.isNullOrEmpty()) {
                    if (transaksi.barang.foto_barang.startsWith("http")) transaksi.barang.foto_barang
                    else "${ApiClient.IMAGE_BASE_URL}${transaksi.barang.foto_barang}"
                } else null
                if (!imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        // Information Sections
        InfoSection("Informasi Barang") {
            Card(
                modifier = Modifier.fillMaxWidth().clickable { 
                    transaksi.barang?.id?.let { onProductClick(it) }
                },
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    InfoRow("Nama Barang", transaksi.barang?.nama_barang ?: "-")
                    InfoRow("Jumlah", "${transaksi.jumlah} unit")
                    InfoRow("Harga Sewa", "Rp ${CurrencyUtils.formatRupiah(transaksi.barang?.harga_sewa ?: 0)} / hari")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Lihat Detail Produk >", 
                        fontSize = 12.sp, 
                        color = NavyPrimary, 
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }

        InfoSection("Informasi Sewa") {
            InfoRow("Tanggal Sewa", DateUtils.formatDateForUI(transaksi.tanggal_sewa))
            InfoRow("Rencana Kembali", DateUtils.formatDateForUI(transaksi.tanggal_kembali_rencana))
            if (transaksi.tanggal_kembali_aktual != null) {
                InfoRow("Tanggal Kembali", DateUtils.formatDateForUI(transaksi.tanggal_kembali_aktual))
            }
        }

        InfoSection("Rincian Biaya") {
            val dendaValue = RentalStatus.calculateFine(transaksi)
            InfoRow("Total Harga", "Rp ${CurrencyUtils.formatRupiah(transaksi.total_harga)}")
            if (dendaValue > 0) {
                InfoRow("Total Denda", "Rp ${CurrencyUtils.formatRupiah(dendaValue)}", color = Color.Red)
            }
        }

        // Action Buttons
        if (rentalStatus == RentalStatus.UPCOMING && transaksi.status.lowercase().contains("pembayaran")) {
            Button(
                onClick = { onPayClick(listOf(transaksi.id)) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text("Bayar Sekarang", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        if (isOwner && isWaitingVerification) {
            Button(
                onClick = { showVerificationDialog = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                enabled = !isLoadingVerification
            ) {
                if (isLoadingVerification) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Verifikasi Pengembalian", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }

    if (showVerificationDialog) {
        VerificationDialog(
            onDismiss = { showVerificationDialog = false },
            onSubmit = { statusKondisi, dendaKerusakan ->
                showVerificationDialog = false
                verificationViewModel.verifikasiPengembalian(
                    token,
                    transaksi.id,
                    VerifikasiPengembalianRequest(
                        status_kondisi = statusKondisi,
                        denda_kerusakan = dendaKerusakan
                    )
                )
            }
        )
    }
}

@Composable
fun InfoSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NavyPrimary)
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String, color: Color = Color.Black) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = Color.Gray)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = color)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationDialog(
    onDismiss: () -> Unit,
    onSubmit: (statusKondisi: String, dendaKerusakan: Double?) -> Unit
) {
    var statusKondisi by remember { mutableStateOf("ok") } // "ok" or "tolak"
    var dendaInput by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Verifikasi Pengembalian", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Pilih kondisi barang setelah pengembalian:")
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { statusKondisi = "ok" }
                            .border(
                                width = if (statusKondisi == "ok") 2.dp else 1.dp,
                                color = if (statusKondisi == "ok") BluePrimary else Color.LightGray,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Box(modifier = Modifier.padding(12.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("Kondisi Baik", fontWeight = if (statusKondisi == "ok") FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                    
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { statusKondisi = "tolak" }
                            .border(
                                width = if (statusKondisi == "tolak") 2.dp else 1.dp,
                                color = if (statusKondisi == "tolak") BluePrimary else Color.LightGray,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Box(modifier = Modifier.padding(12.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("Barang Rusak", fontWeight = if (statusKondisi == "tolak") FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
                
                if (statusKondisi == "tolak") {
                    OutlinedTextField(
                        value = dendaInput,
                        onValueChange = { dendaInput = it },
                        label = { Text("Denda Kerusakan (Rp)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        ),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val denda = if (statusKondisi == "tolak") dendaInput.toDoubleOrNull() else null
                    onSubmit(statusKondisi, denda)
                },
                enabled = statusKondisi == "ok" || (statusKondisi == "tolak" && dendaInput.toDoubleOrNull() != null),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text("Kirim", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = Color.Gray)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}
