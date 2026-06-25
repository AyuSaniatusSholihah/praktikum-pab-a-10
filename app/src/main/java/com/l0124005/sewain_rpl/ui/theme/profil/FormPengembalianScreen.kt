package com.l0124005.sewain_rpl.ui.theme.profil

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import java.util.Date
import com.l0124005.sewain_rpl.network.ApiClient
import com.l0124005.sewain_rpl.ui.theme.*
import com.l0124005.sewain_rpl.utils.*
import com.l0124005.sewain_rpl.viewmodel.TransaksiViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

// ── Warna tema tambahan (jika belum ada di Color.kt) ──
private val LightBlueBg  = Color(0xFFE8F4FB)
private val LightBlueLn  = Color(0xFFDFEAF2)
private val StarOn       = Color(0xFFF5B800)
private val StarOff      = Color(0xFFCCCCCC)
private val DendaRed      = Color(0xFFD32F2F)

data class ReturnFormDetail(
    val itemName: String,
    val itemImageUrl: String,
    val status: RentalStatus = RentalStatus.RETURN,
    val idTransaksi: String,
    val owner: String,
    val user: String,
    val infoDenda: String,
    val tanggalSewa: String,
    val tanggalPengambilan: String,
    val tanggalPengembalian: String,
    val tanggalPengembalianAktual: String,
    val dendaText: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormPengembalianScreen(
    transaksiId: Int,
    token: String,
    viewModel: TransaksiViewModel,
    profileViewModel: com.l0124005.sewain_rpl.viewmodel.ProfileViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val detailState by viewModel.transaksiDetail.observeAsState()
    val profileState by profileViewModel.profile.observeAsState()

    var fotoUri by remember { mutableStateOf<Uri?>(null) }
    var rating by remember { mutableIntStateOf(3) }
    var review by remember { mutableStateOf("") }

    val kembalikanState by viewModel.kembalikanState.observeAsState()
    var showConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(transaksiId) {
        viewModel.getDetailTransaksi(token, transaksiId)
        if (profileState == null) {
            profileViewModel.getProfile(token)
        }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> fotoUri = uri }

    LaunchedEffect(kembalikanState) {
        if (kembalikanState is Resource.Success) {
            Toast.makeText(context, "Pengembalian berhasil diajukan!", Toast.LENGTH_SHORT).show()
            onSuccess()
            viewModel.resetStates()
        } else if (kembalikanState is Resource.Error) {
            val msg = kembalikanState?.message ?: "Gagal mengajukan pengembalian"
            val displayMsg = if (msg.contains("<html>", ignoreCase = true) || msg.contains("<!DOCTYPE", ignoreCase = true)) {
                "Gagal mengajukan: Terjadi kesalahan pada server."
            } else {
                msg
            }
            Toast.makeText(context, displayMsg, Toast.LENGTH_LONG).show()
            viewModel.resetStates()
        }
    }

    val isLoading = kembalikanState is Resource.Loading

    Scaffold(
        topBar = {
            SewainTopBar(
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = onBack
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
                        val status = RentalStatus.fromTransaksi(transaksi)
                        val currentUser = transaksi.user?.name 
                            ?: (profileState as? Resource.Success)?.data?.data?.name 
                            ?: "Customer"

                        // ── Kalkulasi Tanggal & Denda Dinamis Menggunakan Utilities ──
                        val now = Date()
                        val currentDateDisplay = DateUtils.dateToFullUiString(now)
                        val currentDateIso = DateUtils.dateToFullBackendString(now)

                        val calculatedFine = RentalStatus.calculateFine(transaksi)
                        val dendaInt = calculatedFine.toInt().coerceAtLeast(0)

                        val detail = ReturnFormDetail(
                            itemName = transaksi.barang?.nama_barang ?: "-",
                            itemImageUrl = transaksi.barang?.mainFotoUrl ?: "",
                            status = status,
                            idTransaksi = "TRX-${transaksi.id}",
                            owner = transaksi.barang?.user?.name ?: "-",
                            user = currentUser,
                            infoDenda = "Rp ${CurrencyUtils.formatRupiah(transaksi.barang?.harga_denda_perjam ?: 0.0)}/jam",
                            tanggalSewa = DateUtils.formatDateForUI(transaksi.tanggal_sewa),
                            tanggalPengambilan = DateUtils.formatDateForUI(transaksi.tanggal_sewa),
                            tanggalPengembalian = DateUtils.formatDateForUI(transaksi.tanggal_kembali_rencana),
                            tanggalPengembalianAktual = currentDateDisplay,
                            dendaText = if (dendaInt > 0) "Rp ${CurrencyUtils.formatRupiah(dendaInt.toDouble())}" else "Tidak ada denda"
                        )

                        FormContent(
                            transaksiId = transaksiId,
                            token = token,
                            detail = detail,
                            viewModel = viewModel,
                            fotoUri = fotoUri,
                            onPickFoto = { pickImageLauncher.launch("image/*") },
                            rating = rating,
                            onRatingChange = { rating = it },
                            review = review,
                            onReviewChange = { review = it },
                            isLoading = isLoading,
                            calculatedDendaValue = dendaInt.toDouble(),
                            currentDateIso = currentDateIso,
                            showConfirmDialog = showConfirmDialog,
                            onConfirmDialogChange = { showConfirmDialog = it }
                        )
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun FormContent(
    transaksiId: Int,
    token: String,
    detail: ReturnFormDetail,
    viewModel: TransaksiViewModel,
    fotoUri: Uri?,
    onPickFoto: () -> Unit,
    rating: Int,
    onRatingChange: (Int) -> Unit,
    review: String,
    onReviewChange: (String) -> Unit,
    isLoading: Boolean,
    calculatedDendaValue: Double,
    currentDateIso: String,
    showConfirmDialog: Boolean,
    onConfirmDialogChange: (Boolean) -> Unit
) {
    val context = LocalContext.current

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { onConfirmDialogChange(false) },
            title = { Text("Konfirmasi Pengembalian", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin data yang diisi sudah benar? Pengajuan pengembalian tidak dapat dibatalkan.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirmDialogChange(false)
                        val file = if (fotoUri != null) ImageUtils.compressImage(context, fotoUri) else null
                        
                        val body = if (file != null) {
                            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                            MultipartBody.Part.createFormData(
                                "foto_buktipengembalian", file.name, requestFile
                            )
                        } else null

                        if (body == null) {
                            Toast.makeText(context, "Harap upload bukti pengembalian", Toast.LENGTH_SHORT).show()
                            return@TextButton
                        }

                        val ratingBody = rating.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                        val komentarBody = review.toRequestBody("text/plain".toMediaTypeOrNull())
                        val tglBody = currentDateIso.toRequestBody("text/plain".toMediaTypeOrNull())
                        val dendaInt = calculatedDendaValue.toInt().coerceAtLeast(0)
                        val dendaBody = dendaInt.toString().toRequestBody("text/plain".toMediaTypeOrNull())

                        viewModel.kembalikanBarang(token, transaksiId, body, ratingBody, komentarBody, tglBody, dendaBody)
                    }
                ) {
                    Text("Ya, Kirim", color = ProfileAccentBlue, fontWeight = FontWeight.Bold)
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
                    text = "My Rentals",
                    fontFamily = VolkhovFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color.White
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Form Pengembalian Barang",
                    fontFamily = VolkhovFont,
                    fontSize = 12.sp,
                    color = TextMuted
                )

                Spacer(Modifier.height(20.dp))

                RentalDetailHeader(detail = detail)

                Spacer(Modifier.height(24.dp))

                ReturnInfoGrid(detail = detail)

                Spacer(Modifier.height(8.dp))

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

                ReturnBox(
                    detail = detail,
                    fotoUri = fotoUri,
                    onPickFoto = onPickFoto,
                    rating = rating,
                    onRatingChange = onRatingChange,
                    review = review,
                    onReviewChange = onReviewChange
                )

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = { onConfirmDialogChange(true) },
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
                            text = "KIRIM\nPENGEMBALIAN",
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

@Composable
private fun RentalDetailHeader(detail: ReturnFormDetail) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(10.dp))
        ) {
            AsyncImage(
                model = detail.itemImageUrl,
                contentDescription = detail.itemName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .padding(6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(detail.status.badgeColor.copy(alpha = 0.7f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = detail.status.label,
                    color = detail.status.badgeTextColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(Modifier.width(18.dp))
        Text(
            text = detail.itemName,
            fontFamily = VolkhovFont,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = TextLight,
            lineHeight = 28.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ReturnInfoGrid(detail: ReturnFormDetail) {
    val fields = listOf(
        "ID Transaksi" to detail.idTransaksi,
        "Owner" to detail.owner,
        "User" to detail.user,
        "Denda" to detail.dendaText,
        "Tanggal Sewa" to detail.tanggalSewa,
        "Rencana Kembali" to detail.tanggalPengembalian
    )

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
                            Text(
                                text = value,
                                color = DarkNavy,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                if (rowFields.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ReturnBox(
    detail: ReturnFormDetail,
    fotoUri: Uri?,
    onPickFoto: () -> Unit,
    rating: Int,
    onRatingChange: (Int) -> Unit,
    review: String,
    onReviewChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LightBlueBg)
            .padding(20.dp)
    ) {
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
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = detail.tanggalPengembalianAktual,
                        color = LightBlueLn,
                        fontSize = 11.sp
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
                        text = detail.dendaText,
                        color = DendaRed,
                        fontSize = 13.sp,
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Upload Foto",
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
                        .background(DarkNavy)
                        .clickable { onPickFoto() },
                    contentAlignment = Alignment.Center
                ) {
                    if (fotoUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(model = fotoUri),
                            contentDescription = "Foto pengembalian",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "Klik untuk upload foto",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

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
                            color = if (i <= rating) StarOn else StarOff,
                            modifier = Modifier.clickable { onRatingChange(i) }
                        )
                    }
                }

                OutlinedTextField(
                    value = review,
                    onValueChange = onReviewChange,
                    placeholder = {
                        Text(
                            text = "Tulis review kamu di sini...",
                            color = LightBlueLn.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 140.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor   = DarkNavy,
                        unfocusedContainerColor = DarkNavy,
                        focusedBorderColor      = LightBlueLn,
                        unfocusedBorderColor    = LightBlueLn,
                        focusedTextColor        = LightBlueLn,
                        unfocusedTextColor      = LightBlueLn
                    ),
                    shape = RoundedCornerShape(10.dp),
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
    }
}
