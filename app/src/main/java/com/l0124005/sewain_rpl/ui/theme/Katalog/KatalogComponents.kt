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
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.remember
import java.text.SimpleDateFormat
import java.util.*
import com.l0124005.sewain_rpl.ui.theme.MonsterratFont
import com.l0124005.sewain_rpl.ui.theme.VolkhovFont
import com.l0124005.sewain_rpl.ui.theme.LatoFont
import com.l0124005.sewain_rpl.ui.theme.JostFont

import com.l0124005.sewain_rpl.utils.DateUtils

// ============================================================
// WARNA TEMA — KATALOG SHARED
// ============================================================
val UploadBg     = Color(0xFFEEF3F7)
val UploadBorder = Color(0xFFD1DDE7)
val FormBg       = Color(0xFFB2C9DD).copy(alpha = 0.3f)
val Primary      = com.l0124005.sewain_rpl.ui.theme.BluePrimary
val Black        = Color(0xFF181A18)
val White        = Color(0xFFFFFFFF)
val TextDark     = Color(0xFF484848)
val TextMuted    = Color(0xFF8A8A8A)
val BorderGray   = Color(0xFF8A8A8A)
val SectionBg    = Color(0xFFEEF3F7)
val SectionBorder= Color(0xFFD1DDE7)

// Tambahan warna khusus Add Item, biar sama persis web (#DDE9F5, #C3D4E9, dst)
val AddItemSectionBg     = Color(0xFFDDE9F5)
val AddItemSectionBorder = Color(0xFFC3D4E9)
val AddItemAccent        = Color(0xFF6A87A1)
val AddItemSectionBg30 = Color(0xFFDDE9F5).copy(alpha = 0.3f)
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
    val firstFoto = product.listFoto.firstOrNull()
    val imageUrl = if (firstFoto != null) {
        if (firstFoto.startsWith("http")) firstFoto
        else "${ApiClient.IMAGE_BASE_URL}$firstFoto"
    } else null

    val rating = 4
    val reviewCount = remember(product.id) { (10..50).random() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(386f / 240f)
                    .clip(RoundedCornerShape(8.dp))
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
                        modifier = Modifier.size(32.dp).align(Alignment.Center)
                    )
                }

                IconButton(
                    onClick = { onAddToCart() },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .size(28.dp)
                        .background(Primary, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ShoppingCart,
                        contentDescription = "Tambah ke Keranjang",
                        tint = White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = product.nama_barang,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 15.sp,
                    color = TextDark,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(4.dp))
                Text("★".repeat(rating) + "☆".repeat(5 - rating), color = Color(0xFFF5B800), fontSize = 10.sp)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.LocationOn, null, Modifier.size(10.dp), tint = Color(0xFF55959E))
                Spacer(Modifier.width(3.dp))
                Text(
                    text = product.lokasi ?: "-",
                    fontSize = 9.5.sp,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text("($reviewCount) Customer Reviews", fontSize = 9.5.sp, color = TextDark)

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Rp ${formatRupiah(product.harga_sewa)}/hari",
                fontWeight = FontWeight.Bold,
                fontSize = 12.5.sp,
                color = TextDark
            )
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
    nama: String = "User",
    lokasi: String = "Lokasi",
    rating: Float = 4.5f,
    totalUlasan: Int = 0,
    jumlahKatalog: Int = 0,
    whatsapp: String = "-",
    fotoProfil: String? = null
) {
    val photoUrl = remember(fotoProfil, nama) {
        if (!fotoProfil.isNullOrEmpty()) {
            if (fotoProfil.startsWith("http")) fotoProfil
            else {
                val cleanPath = if (fotoProfil.startsWith("profiles/")) fotoProfil else "profiles/$fotoProfil"
                "${ApiClient.IMAGE_BASE_URL}$cleanPath"
            }
        } else {
            "https://ui-avatars.com/api/?name=$nama&background=4D6674&color=fff"
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(0.5.dp, SectionBorder, RoundedCornerShape(12.dp))
            .background(Color(0xFFE0E0E0))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Avatar
        AsyncImage(
            model = photoUrl,
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(UploadBorder),
            contentScale = ContentScale.Crop
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = nama,
                fontFamily = VolkhovFont,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Black
            )
            ProfileInfoRow(
                icon = Icons.Outlined.LocationOn,
                label = lokasi,
                tint = TextMuted
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = Color(0xFFF5B800)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "$rating  ($totalUlasan Ulasan)",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProfileInfoRow(
                    icon = Icons.Outlined.Inventory2,
                    label = "$jumlahKatalog Barang",
                    tint = TextMuted
                )
            }
        }
    }
}

@Composable
fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    tint: Color = TextMuted
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF55959E), modifier = Modifier.size(13.dp))
        Text(
            text = label,
            fontFamily = MonsterratFont,
            fontWeight = FontWeight.Bold,
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
            fontFamily = LatoFont,
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
                fontFamily = LatoFont,
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
        fontFamily = VolkhovFont,
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
                    fontFamily = JostFont,
                    fontSize = 13.sp,
                    color = TextMuted
                )
            },
            singleLine = singleLine,
            minLines = minLines,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            textStyle = LocalTextStyle.current.copy(
                fontFamily = JostFont,
                fontSize = 13.sp,
                color = Color(0xFF484848)
            ),
            shape = RoundedCornerShape(6.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = AddItemAccent,
                unfocusedBorderColor = BorderGray,
                unfocusedContainerColor = Color(0xFFB2C9DD).copy(alpha = 0.5f),
                focusedContainerColor   = Color(0xFFB2C9DD).copy(alpha = 0.5f),
                cursorColor = AddItemAccent
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
            fontFamily = JostFont,
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
                    val date = DateUtils.millisToUiString(millis)
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
