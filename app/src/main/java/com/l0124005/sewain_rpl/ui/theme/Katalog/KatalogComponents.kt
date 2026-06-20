package com.l0124005.sewain_rpl.ui.theme.katalog

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.l0124005.sewain_rpl.network.ApiClient
import com.l0124005.sewain_rpl.network.CatalogData
import com.l0124005.sewain_rpl.network.KategoriData
import java.text.SimpleDateFormat
import java.util.*

// ============================================================
// WARNA TEMA — KATALOG SHARED
// ============================================================
val UploadBg     = Color(0xFFDDE9F5)
val UploadBorder = Color(0xFFC3D4E9)
val FormBg       = Color(0xFFB2C9DD).copy(alpha = 0.5f)
val Primary      = Color(0xFF6A87A1)
val Black        = Color(0xFF181A18)
val White        = Color(0xFFFFFFFF)
val TextDark     = Color(0xFF484848)
val TextMuted    = Color(0xFF8A8A8A)
val BorderGray   = Color(0xFF8A8A8A)
val SectionBg    = Color(0xFFDDE9F5)
val SectionBorder= Color(0xFFC3D4E9)

// Font Fallback
val Volkhov = FontFamily.Default

// ============================================================
// TYPOGRAPHY — CK STANDARD
// ============================================================
val CkHeading = TextStyle(
    fontFamily = Volkhov,
    fontWeight = FontWeight.Bold,
    fontSize = 16.sp,
    color = Black
)

val CkBody = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.SemiBold,
    fontSize = 12.sp,
    color = TextMuted
)

fun formatRupiah(number: Double): String {
    val format = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("id", "ID"))
    return format.format(number).replace("Rp", "").trim()
}

@Composable
fun ProductCard(product: CatalogData, onClick: () -> Unit, onAddToCart: () -> Unit) {
    val imageUrl = if (product.foto_barang != null) {
        if (product.foto_barang.startsWith("http")) {
            product.foto_barang
        } else {
            "${ApiClient.IMAGE_BASE_URL}${product.foto_barang}"
        }
    } else null

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(0.5.dp, SectionBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .background(UploadBg)
            ) {
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = product.nama_barang,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Inventory2,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(40.dp).align(Alignment.Center)
                    )
                }
                
                if (product.stok > 0) {
                     Surface(
                        color = Primary,
                        shape = RoundedCornerShape(bottomStart = 8.dp),
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text(
                            text = "Tersedia",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 8.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = product.nama_barang,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Black
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.LocationOn, null, Modifier.size(12.dp), tint = TextMuted)
                            Spacer(Modifier.width(2.dp))
                            Text(
                                text = product.lokasi ?: "-",
                                fontSize = 10.sp,
                                color = TextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "Rp ${formatRupiah(product.harga_sewa)}",
                                color = Primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = " / hari",
                                color = TextMuted,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(bottom = 1.dp)
                            )
                        }
                    }

                    // Tombol Keranjang Cepat
                    IconButton(
                        onClick = { onAddToCart() },
                        modifier = Modifier
                            .size(32.dp)
                            .background(Primary, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ShoppingCart,
                            contentDescription = "Tambah ke Keranjang",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// PROFILE CARD
// ============================================================

data class AddItemFormState(
    val itemName: String = "",
    val category: String = "",
    val price: String = "",
    val description: String = "",
    val whatsApp: String = "",
    val lokasi: String = "",
    val additionalInfo: String = "",
    val jaminan: String = "",
    val denda: String = "",
    val stockQty: Int = 1,
    val dateStart: String = "",
    val dateEnd: String = "",
    val mainImageUri: Uri? = null,
    val categoryId: String = "",
    val angle1Uri: Uri? = null,
    val angle2Uri: Uri? = null,
    val angle3Uri: Uri? = null,
    val angle4Uri: Uri? = null,
)

@Composable
fun ProfileCard(
    modifier: Modifier = Modifier,
    nama: String = "Camping Groups Bandung",
    lokasi: String = "Kota Bandung",
    rating: Float = 4.3f,
    totalUlasan: Int = 120,
    jumlahKatalog: Int = 7,
    whatsapp: String = "0821-5620-9034"
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(0.5.dp, SectionBorder, RoundedCornerShape(12.dp))
            .background(SectionBg)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Avatar placeholder
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(UploadBorder),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.Person,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(32.dp)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(
                text = nama,
                fontFamily = Volkhov,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Black
            )
            ProfileInfoRow(
                icon = Icons.Outlined.LocationOn,
                label = lokasi
            )
            ProfileInfoRow(
                icon = Icons.Outlined.Star,
                label = "$rating  ($totalUlasan Ulasan)"
            )
            ProfileInfoRow(
                icon = Icons.Outlined.Inventory2,
                label = "$jumlahKatalog Barang"
            )
            ProfileInfoRow(
                icon = Icons.Outlined.Phone,
                label = whatsapp
            )
        }
    }
}

@Composable
fun ProfileInfoRow(
    icon: ImageVector,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icon, contentDescription = null, tint = TextMuted, modifier = Modifier.size(13.dp))
        Text(
            text = label,
            fontFamily = FontFamily.Default,
            fontSize = 12.sp,
            color = TextDark
        )
    }
}

// ============================================================
// DATE PICKER BOX
// ============================================================
@Composable
fun DatePickerBox(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, UploadBorder, RoundedCornerShape(10.dp))
            .background(UploadBg)
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = Black,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                Icons.Outlined.CalendarToday,
                contentDescription = null,
                tint = Black,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = if (value.isEmpty()) "Pilih tanggal" else value,
                fontFamily = FontFamily.Default,
                fontSize = 12.sp,
                color = if (value.isEmpty()) TextMuted else Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ============================================================
// REUSABLE FORM COMPONENTS
// ============================================================
@Composable
fun SewainFormLabel(label: String) {
    Text(
        text = label,
        fontFamily = Volkhov,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        color = TextDark
    )
}

@Composable
fun SewainFormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SewainFormLabel(label = label)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = placeholder,
                    fontFamily = FontFamily.Default,
                    fontSize = 13.sp,
                    color = TextMuted
                )
            },
            singleLine = singleLine,
            minLines = minLines,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            textStyle = LocalTextStyle.current.copy(
                fontFamily = FontFamily.Default,
                fontSize = 13.sp,
                color = TextDark
            ),
            shape = RoundedCornerShape(6.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = Primary,
                unfocusedBorderColor = BorderGray,
                unfocusedContainerColor = FormBg,
                focusedContainerColor   = FormBg,
            )
        )
        Spacer(Modifier.height(14.dp))
    }
}

@Composable
fun StockQtyStepper(
    qty: Int,
    onMinus: () -> Unit,
    onPlus: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .border(1.dp, UploadBorder, RoundedCornerShape(6.dp))
            .background(White),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onMinus,
            modifier = Modifier.size(40.dp)
        ) {
            Text("−", fontSize = 18.sp, color = TextDark)
        }
        Text(
            text = qty.toString().padStart(2, '0'),
            modifier = Modifier.defaultMinSize(minWidth = 44.dp),
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            color = Black
        )
        IconButton(
            onClick = onPlus,
            modifier = Modifier.size(40.dp)
        ) {
            Text("+", fontSize = 18.sp, color = TextDark)
        }
    }
}

// ============================================================
// DYNAMIC CATEGORY BOTTOM SHEET
// ============================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryBottomSheetDynamic(
    categories: List<KategoriData>,
    selectedCategoryId: String,
    onSelect: (Int, String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = White,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(modifier = Modifier.padding(bottom = 32.dp)) {
            Text(
                text = "Pilih Kategori",
                fontFamily = Volkhov,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Black,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
            HorizontalDivider(color = SectionBorder)
            categories.forEach { cat ->
                val isSelected = cat.id.toString() == selectedCategoryId
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(cat.id, cat.nama_kategori) }
                        .background(if (isSelected) UploadBg else White)
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = cat.nama_kategori,
                        fontFamily = FontFamily.Default,
                        fontSize = 15.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Primary else TextDark
                    )
                    if (isSelected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                HorizontalDivider(color = SectionBorder.copy(alpha = 0.5f))
            }
        }
    }
}

// ============================================================
// DATE PICKER DIALOG
// ============================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SewainDatePickerDialog(
    title: String,
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                    val date = java.text.SimpleDateFormat("dd MMMM yyyy", java.util.Locale("id")).format(java.util.Date(millis))
                    onDateSelected(date)
                }
            }) {
                Text("OK", color = Primary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = TextMuted)
            }
        },
        colors = DatePickerDefaults.colors(
            containerColor = White,
            titleContentColor = Black,
            headlineContentColor = Black,
            weekdayContentColor = TextMuted,
            selectedDayContainerColor = Primary,
            todayDateBorderColor = Primary,
            todayContentColor = Primary,
        )
    ) {
        DatePicker(
            state = datePickerState,
            title = {
                Text(
                    text = title,
                    modifier = Modifier.padding(start = 24.dp, top = 16.dp),
                    fontFamily = Volkhov,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Black
                )
            }
        )
    }
}
