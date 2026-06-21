package com.l0124005.sewain_rpl.ui.theme.transaksi

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.l0124005.sewain_rpl.ui.theme.AbrilFatfaceFont
import com.l0124005.sewain_rpl.ui.theme.SewainTopBar
import com.l0124005.sewain_rpl.ui.theme.VolkhovFont
import com.l0124005.sewain_rpl.ui.theme.profil.RentalDetailData

// ── Warna tema -- Konsisten dengan RentalDetailScreen.kt ──
private val DarkNavy    = Color(0xFF21394F)
private val MidBlue     = Color(0xFF4D6674)
private val LightBlueBg = Color(0xFFE8F4FB)
private val TextLight   = Color(0xFFE6E8EF)
private val TextMuted   = Color(0xFFA1A2A7)
private val PriceBlue   = Color(0xFF6A87A1)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormPengembalianScreen(
    detail: RentalDetailData,
    onBack: () -> Unit,
    onSubmit: (Uri?, Int, String) -> Unit
) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var rating by remember { mutableIntStateOf(5) }
    var komentar by remember { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    Scaffold(
        topBar = { 
            SewainTopBar(
                onNavigationClick = onBack,
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack
            ) 
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // ── Header Panel ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MidBlue)
                    .padding(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkNavy)
                        .padding(24.dp)
                ) {
                    Column {
                        Text(
                            text = "Form Pengembalian",
                            fontFamily = VolkhovFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Silakan lengkapi data pengembalian barang.",
                            fontFamily = VolkhovFont,
                            fontSize = 12.sp,
                            color = TextMuted
                        )

                        Spacer(Modifier.height(24.dp))

                        // ── Info Barang Ringkas ──
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = detail.itemImageUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = detail.itemName,
                                    fontFamily = AbrilFatfaceFont,
                                    fontSize = 16.sp,
                                    color = TextLight
                                )
                                Text(
                                    text = "ID: ${detail.idTransaksi}",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Spacer(Modifier.height(28.dp))

                        // ── Upload Foto Bukti ──
                        Text(
                            text = "Foto Bukti Pengembalian",
                            fontFamily = AbrilFatfaceFont,
                            fontSize = 16.sp,
                            color = TextLight
                        )
                        Spacer(Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(LightBlueBg)
                                .clickable { launcher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedImageUri != null) {
                                AsyncImage(
                                    model = selectedImageUri,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.CloudUpload,
                                        contentDescription = null,
                                        tint = DarkNavy,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Text(
                                        "Klik untuk unggah foto",
                                        color = DarkNavy,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        // ── Rating ──
                        Text(
                            text = "Rating Layanan",
                            fontFamily = AbrilFatfaceFont,
                            fontSize = 16.sp,
                            color = TextLight
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            repeat(5) { index ->
                                Icon(
                                    imageVector = if (index < rating) Icons.Default.Star else Icons.Outlined.StarBorder,
                                    contentDescription = null,
                                    tint = if (index < rating) Color(0xFFFFD700) else TextMuted,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clickable { rating = index + 1 }
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        // ── Komentar ──
                        Text(
                            text = "Komentar / Catatan",
                            fontFamily = AbrilFatfaceFont,
                            fontSize = 16.sp,
                            color = TextLight
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = komentar,
                            onValueChange = { komentar = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            placeholder = { Text("Tulis pengalamanmu...", color = TextMuted, fontSize = 14.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = LightBlueBg,
                                unfocusedContainerColor = LightBlueBg,
                                focusedBorderColor = PriceBlue,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(Modifier.height(32.dp))

                        // ── Tombol Submit ──
                        Button(
                            onClick = { onSubmit(selectedImageUri, rating, komentar) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PriceBlue),
                            enabled = selectedImageUri != null
                        ) {
                            Text(
                                text = "KIRIM PENGEMBALIAN",
                                fontFamily = AbrilFatfaceFont,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
