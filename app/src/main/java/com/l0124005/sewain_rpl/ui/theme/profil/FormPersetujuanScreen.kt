package com.l0124005.sewain_rpl.ui.theme.profil

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.l0124005.sewain_rpl.network.VerifikasiPengembalianRequest
import com.l0124005.sewain_rpl.ui.theme.*
import com.l0124005.sewain_rpl.utils.DateUtils
import com.l0124005.sewain_rpl.utils.CurrencyUtils
import com.l0124005.sewain_rpl.utils.RentalStatus
import com.l0124005.sewain_rpl.utils.Resource
import com.l0124005.sewain_rpl.viewmodel.TransaksiViewModel

private val LightBlueBg  = Color(0xFFE8F4FB)
private val LightBlueLn  = Color(0xFFDFEAF2)
private val StarOn       = Color(0xFFF5B800)
private val StarOff      = Color(0xFFCCCCCC)
private val DendaRed     = Color(0xFFD32F2F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormPersetujuanScreen(
    transaksiId: Int,
    token: String,
    viewModel: TransaksiViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val detailState by viewModel.transaksiDetail.observeAsState()
    val verifikasiState by viewModel.verifikasiState.observeAsState()

    var isVerified by remember { mutableStateOf(false) }
    var statusKondisi by remember { mutableStateOf("Baik") }
    var dendaTambahan by remember { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(transaksiId) {
        viewModel.getOwnerTransaksiDetail(token, transaksiId)
    }

    LaunchedEffect(verifikasiState) {
        if (verifikasiState is Resource.Success) {
            isVerified = true
            // Jangan panggil resetStates() di sini jika itu menghapus transaksiDetail
            // viewModel.resetStates()
        } else if (verifikasiState is Resource.Error) {
            val msg = (verifikasiState as Resource.Error).message ?: "Gagal verifikasi"
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            android.util.Log.e("FormPersetujuan", "Error: $msg")
            viewModel.resetStates()
        }
    }

    val isLoading = verifikasiState is Resource.Loading

    Scaffold(
        topBar = {
            SewainTopBar(
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = {
                    if (isVerified) onSuccess() else onBack()
                }
            )
        },
        containerColor = Color.White
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)
        ) {
            when (val state = detailState) {
                is Resource.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is Resource.Error -> {
                    Text(state.message ?: "Gagal memuat detail", modifier = Modifier.align(Alignment.Center), color = Color.Red)
                }
                is Resource.Success -> {
                    state.data?.data?.let { transaksi ->
                        PersetujuanContent(
                            transaksi = transaksi,
                            token = token,
                            viewModel = viewModel,
                            statusKondisi = statusKondisi,
                            onStatusChange = { statusKondisi = it },
                            dendaTambahan = dendaTambahan,
                            onDendaChange = { dendaTambahan = it },
                            isLoading = isLoading,
                            isVerified = isVerified,
                            showConfirmDialog = showConfirmDialog,
                            onConfirmDialogChange = { showConfirmDialog = it },
                            onBack = onBack,
                            onSuccess = onSuccess
                        )
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun PersetujuanContent(
    transaksi: com.l0124005.sewain_rpl.network.TransaksiData,
    token: String,
    viewModel: TransaksiViewModel,
    statusKondisi: String,
    onStatusChange: (String) -> Unit,
    dendaTambahan: String,
    onDendaChange: (String) -> Unit,
    isLoading: Boolean,
    isVerified: Boolean,
    showConfirmDialog: Boolean,
    onConfirmDialogChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { onConfirmDialogChange(false) },
            title = { Text("Konfirmasi Verifikasi", fontWeight = FontWeight.Bold) },
            text = { Text("Pastikan Anda telah memeriksa barang dengan teliti. Lanjutkan verifikasi pengembalian?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirmDialogChange(false)
                        // Mapping status sesuai ekspektasi Laravel (ok / tolak)
                        val dendaVal = dendaTambahan.toDoubleOrNull() ?: 0.0
                        val request = VerifikasiPengembalianRequest(
                            status_kondisi = if (dendaVal > 0) "tolak" else "ok",
                            denda_kerusakan = dendaVal
                        )
                        viewModel.verifikasiPengembalian(token, transaksi.id, request)
                    }
                ) {
                    Text("Ya, Verifikasi", color = ProfileAccentBlue, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { onConfirmDialogChange(false) }) {
                    Text("Batal", color = Color.Gray)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    val fineAmount = RentalStatus.calculateFine(transaksi)
    val dendaDisplay = if (fineAmount > 0) {
        "Rp ${CurrencyUtils.formatRupiah(fineAmount)}"
    } else {
        "Tidak ada denda"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MidBlue)
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkNavy)
                    .padding(24.dp)
            ) {
                Text(
                    text = "Rentals Owner",
                    fontFamily = VolkhovFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color.White
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Verifikasi Pengembalian Barang",
                    fontFamily = VolkhovFont,
                    fontSize = 12.sp,
                    color = TextMuted
                )

                Spacer(Modifier.height(24.dp))

                val fineAmount = RentalStatus.calculateFine(transaksi)
                val dendaDisplay = if (fineAmount > 0) {
                    "Rp ${CurrencyUtils.formatRupiah(fineAmount)}"
                } else {
                    "Tidak ada denda"
                }

                // Item Detail Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(10.dp))
                    ) {
                        AsyncImage(
                            model = transaksi.barang?.mainFotoUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(Modifier.width(18.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = transaksi.barang?.nama_barang ?: "-",
                            fontFamily = VolkhovFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = TextLight,
                            lineHeight = 28.sp
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
                // ⬇️ TAMBAHIN INI — grid info sama kayak ReturnInfoGrid di FormPengembalianScreen
                PersetujuanInfoGrid(
                    fields = listOf(
                        "ID Transaksi" to "TRX-${transaksi.id}",
                        "Owner" to (transaksi.barang?.user?.name ?: "-"),
                        "User" to (transaksi.user?.name ?: "-"),
                        "Denda" to dendaDisplay,
                        "Tanggal Sewa" to DateUtils.formatDateForUI(transaksi.tanggal_sewa),
                        "Tanggal Pengembalian" to (DateUtils.formatFullDateForUI(transaksi.tanggal_kembali_aktual).ifEmpty { "Belum dikembalikan" })
                    )
                )

                Spacer(Modifier.height(24.dp))

                if (isVerified) {
                    Spacer(Modifier.height(8.dp))
                    ConfirmedBoxOwner(
                        onDone = { if (isVerified) onSuccess() else onBack() }
                    )
                } else {
                    Text(
                        text = "FORM PENGEMBALIAN",
                        fontFamily = AbrilFatfaceFont,
                        fontSize = 22.sp,
                        color = TextLight,
                        textAlign = TextAlign.Center,
                        letterSpacing = 1.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))
                    PenyewaSubmissionBox(
                        tanggalPengembalianAktual = DateUtils.formatFullDateForUI(transaksi.tanggal_kembali_aktual).ifEmpty { "Belum dikembalikan" },
                        dendaText = dendaDisplay,
                        fotoBuktiUrl = transaksi.buktiPengembalianUrl,
                        rating = transaksi.review?.rating ?: 0,
                        review = transaksi.review?.komentar ?: ""
                    )

                    Spacer(Modifier.height(32.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    Spacer(Modifier.height(24.dp))

                    // Verification Form
                    Text(
                        text = "HASIL PENGECEKAN OWNER",
                        fontFamily = AbrilFatfaceFont,
                        fontSize = 22.sp,
                        color = TextLight,
                        textAlign = TextAlign.Center,
                        letterSpacing = 1.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))

                    Text("Kondisi Barang", color = TextLight, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = statusKondisi,
                        onValueChange = onStatusChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Misal: Baik, Lecet sedikit, dll") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = DarkNavy,
                            unfocusedTextColor = DarkNavy
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(Modifier.height(16.dp))

                    Text("Denda Kerusakan (Opsional)", color = TextLight, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = dendaTambahan,
                        onValueChange = { if (it.all { char -> char.isDigit() }) onDendaChange(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("0") },
                        prefix = { Text("Rp ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = DarkNavy,
                            unfocusedTextColor = DarkNavy
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(Modifier.height(32.dp))

                    Button(
                        onClick = {
                            if (statusKondisi.isBlank()) {
                                Toast.makeText(context, "Kondisi barang harus diisi", Toast.LENGTH_SHORT).show()
                            } else {
                                onConfirmDialogChange(true)
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(170.dp)
                            .height(56.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ProfileAccentBlue,
                            disabledContainerColor = ProfileAccentBlue.copy(alpha = 0.5f)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "ACCEPT\nPENGEMBALIAN",
                                fontFamily = AbrilFatfaceFont,
                                fontSize = 14.sp,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfirmedBoxOwner(
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(LightBlueBg)
            .padding(32.dp, 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(ProfileAccentBlue),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color(0xFFC3D4E9),
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "ACCEPT RETURN CONFIRMED!",
            fontFamily = VolkhovFont,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = Color(0xFF484848),
            letterSpacing = 0.5.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Terima Kasih telah menggunakan Website SEWAIN sebagai platform penyewaan Anda!",
            fontFamily = MontaguSlabFont,
            fontWeight = FontWeight.Light,
            fontSize = 14.sp,
            color = Color(0xFFAAAAAA),
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(Modifier.height(14.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Status Return Rent Anda saat ini:",
                fontFamily = MontaguSlabFont,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = Color(0xFFAAAAAA),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "COMPLETED RENT ✓",
                fontFamily = AbrilFatfaceFont,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = Color(0xFF818181),
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onDone,
            modifier = Modifier
                .width(200.dp)
                .height(56.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ProfileAccentBlue)
        ) {
            Text(
                text = "Back to Rentals Owner",
                fontFamily = AbrilFatfaceFont,
                fontSize = 14.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}
// ── Grid info transaksi, tampilan sama persis ReturnInfoGrid di FormPengembalianScreen ──
@Composable
private fun PersetujuanInfoGrid(fields: List<Pair<String, String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        fields.chunked(2).forEach { rowFields ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowFields.forEach { (label, value) ->
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = label,
                            fontFamily = AbrilFatfaceFont,
                            fontSize = 16.sp,
                            color = TextLight
                        )
                        Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(LightBlueBg)
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(text = value, color = DarkNavy, fontSize = 14.sp)
                        }
                    }
                }
                if (rowFields.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}
// ── Kotak bukti pengembalian dari penyewa, tampilan sama persis ReturnBox
// di FormPengembalianScreen, tapi READ-ONLY (owner cuma liat, gak input) ──
@Composable
private fun PenyewaSubmissionBox(
    tanggalPengembalianAktual: String,
    dendaText: String,
    fotoBuktiUrl: String?,
    rating: Int,
    review: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LightBlueBg)
            .padding(20.dp)
    ) {
        // ── Baris atas: Tanggal Pengembalian + Denda ──
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Date Pengembalian Sewa",
                    fontFamily = AbrilFatfaceFont,
                    fontSize = 15.sp,
                    color = DarkNavy
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkNavy)
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tanggalPengembalianAktual,
                        color = LightBlueLn,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = LightBlueLn,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Denda",
                    fontFamily = AbrilFatfaceFont,
                    fontSize = 15.sp,
                    color = DarkNavy
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkNavy)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = dendaText,
                        color = DendaRed,
                        fontSize = 13.sp,
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── Baris bawah: Foto Bukti (read-only) + Review (read-only) ──
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Foto bukti -- read-only, tidak clickable
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Bukti Pengembalian",
                    fontFamily = AbrilFatfaceFont,
                    fontSize = 15.sp,
                    color = DarkNavy
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkNavy),
                    contentAlignment = Alignment.Center
                ) {
                    if (!fotoBuktiUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = fotoBuktiUrl,
                            contentDescription = "Bukti pengembalian",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = "Belum ada foto",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Review -- bintang ditampilkan apa adanya, textarea read-only
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Review",
                    fontFamily = AbrilFatfaceFont,
                    fontSize = 15.sp,
                    color = DarkNavy
                )
                Spacer(Modifier.height(6.dp))

                Row(modifier = Modifier.padding(bottom = 10.dp)) {
                    for (i in 1..5) {
                        Text(
                            text = "★",
                            fontSize = 26.sp,
                            color = if (i <= rating) StarOn else StarOff
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 140.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkNavy)
                        .padding(14.dp)
                ) {
                    Text(
                        text = review.ifBlank { "Tidak ada ulasan." },
                        color = LightBlueLn,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

