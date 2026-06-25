package com.l0124005.sewain_rpl.ui.theme.profil

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.l0124005.sewain_rpl.network.ApiClient
import com.l0124005.sewain_rpl.network.TransaksiData
import com.l0124005.sewain_rpl.utils.DateUtils
import com.l0124005.sewain_rpl.utils.CurrencyUtils
import com.l0124005.sewain_rpl.utils.RentalStatus
import com.l0124005.sewain_rpl.ui.theme.*
import com.l0124005.sewain_rpl.utils.Resource
import com.l0124005.sewain_rpl.viewmodel.TransaksiViewModel
import com.l0124005.sewain_rpl.viewmodel.ProfileViewModel

// ── Warna tema tambahan (unik untuk layar konfirmasi) ──
private val LightBlueBg   = Color(0xFFE8F4FB) 
private val CheckTint     = Color(0xFFC3D4E9) 
private val ConfirmedDark = Color(0xFF484848) 
private val ConfirmedGray = Color(0xFFAAAAAA)

enum class ReturnVerificationStatus(val label: String) {
    WAITING("MENUNGGU VERIFIKASI OWNER"),
    APPROVED("COMPLETED RENT ✓")
}

data class ReturnConfirmedData(
    val itemName: String,
    val itemImageUrl: String,
    val status: RentalStatus = RentalStatus.RETURN,
    val idTransaksi: String,
    val owner: String,
    val user: String,
    val denda: String,
    val tanggalSewa: String,
    val tanggalKembaliRencana: String,
    val tanggalKembaliAktual: String,
    val returnStatus: ReturnVerificationStatus = ReturnVerificationStatus.WAITING
)

@Composable
fun ReturnConfirmedScreen(
    transaksiId: Int,
    token: String,
    viewModel: TransaksiViewModel,
    profileViewModel: ProfileViewModel,
    onDone: () -> Unit
) {
    val detailState by viewModel.transaksiDetail.observeAsState()
    val profileState by profileViewModel.profile.observeAsState()

    LaunchedEffect(transaksiId) {
        viewModel.getDetailTransaksi(token, transaksiId)
        if (profileState == null) {
            profileViewModel.getProfile(token)
        }
    }

    Scaffold(
        topBar = {
            SewainTopBar(
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = onDone
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
                    Text(
                        text = state.message ?: "Gagal memuat detail",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Red
                    )
                }
                is Resource.Success -> {
                    state.data?.data?.let { transaksi ->
                        val detail = mapTransaksiToReturnConfirmed(transaksi, profileState)
                        ReturnConfirmedContent(detail = detail, onDone = onDone)
                    }
                }
                else -> {}
            }
        }
    }
}

private fun mapTransaksiToReturnConfirmed(
    transaksi: TransaksiData,
    profileState: Resource<com.l0124005.sewain_rpl.network.ProfileResponse>?
): ReturnConfirmedData {
    val barang = transaksi.barang
    val imageUrl = if (barang != null && !barang.foto_barang.isNullOrEmpty()) {
        if (barang.foto_barang.startsWith("http")) barang.foto_barang
        else "${ApiClient.IMAGE_BASE_URL}${barang.foto_barang}"
    } else ""

    val returnVerStatus = if (transaksi.status.lowercase() in listOf("selesai", "completed", "verified") || 
                             transaksi.tanggal_verifikasipengembalian != null) {
        ReturnVerificationStatus.APPROVED
    } else {
        ReturnVerificationStatus.WAITING
    }

    val dendaValue = RentalStatus.calculateFine(transaksi)
    val dendaText = if (dendaValue > 0) "Rp ${CurrencyUtils.formatRupiah(dendaValue)}" else "Tidak ada denda"

    val currentUserName = transaksi.user?.name 
        ?: (profileState as? Resource.Success)?.data?.data?.name 
        ?: "Customer"

    return ReturnConfirmedData(
        itemName = barang?.nama_barang ?: "-",
        itemImageUrl = imageUrl,
        status = RentalStatus.fromTransaksi(transaksi),
        idTransaksi = "TRX-${transaksi.id}",
        owner = barang?.user?.name ?: "-",
        user = currentUserName,
        denda = dendaText,
        tanggalSewa = DateUtils.formatDateForUI(transaksi.tanggal_sewa),
        tanggalKembaliRencana = DateUtils.formatDateForUI(transaksi.tanggal_kembali_rencana),
        tanggalKembaliAktual = DateUtils.formatFullDateForUI(transaksi.tanggal_kembali_aktual).ifBlank { "-" },
        returnStatus = returnVerStatus
    )
}

@Composable
private fun ReturnConfirmedContent(
    detail: ReturnConfirmedData,
    onDone: () -> Unit
) {
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
                    text = "Status pengembalian barang kamu.",
                    fontFamily = VolkhovFont,
                    fontSize = 12.sp,
                    color = TextMuted
                )

                Spacer(Modifier.height(20.dp))

                ReturnDetailHeaderBlock(detail = detail)

                Spacer(Modifier.height(24.dp))

                ReturnDetailInfoGrid(detail = detail)

                Spacer(Modifier.height(24.dp))

                ConfirmedBox(
                    returnStatus = detail.returnStatus,
                    onBackToProfile = onDone
                )
            }
        }
    }
}

@Composable
private fun ReturnDetailHeaderBlock(detail: ReturnConfirmedData) {
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
                    .background(detail.status.badgeColor)
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
private fun ReturnDetailInfoGrid(detail: ReturnConfirmedData) {
    val fields = listOf(
        "ID Transaksi" to detail.idTransaksi,
        "Owner" to detail.owner,
        "User" to detail.user,
        "Denda" to detail.denda,
        "Tanggal Sewa" to detail.tanggalSewa,
        "Rencana Kembali" to detail.tanggalKembaliRencana,
        "Tanggal Kembali" to detail.tanggalKembaliAktual
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
private fun ConfirmedBox(
    returnStatus: ReturnVerificationStatus,
    onBackToProfile: () -> Unit
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
                Icons.Default.Check,
                contentDescription = null,
                tint = CheckTint,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = if (returnStatus == ReturnVerificationStatus.APPROVED) "ACCEPT RETURN CONFIRMED!" else "RETURN CONFIRMED!",
            fontFamily = VolkhovFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            color = ConfirmedDark,
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
            color = ConfirmedGray,
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
                color = ConfirmedGray,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = returnStatus.label,
                fontFamily = AbrilFatfaceFont,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = Color(0xFF818181),
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onBackToProfile,
            modifier = Modifier
                .width(200.dp)
                .height(56.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ProfileAccentBlue)
        ) {
            Text(
                text = "Back to SEWAIN Profile",
                fontFamily = AbrilFatfaceFont,
                fontSize = 14.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}
