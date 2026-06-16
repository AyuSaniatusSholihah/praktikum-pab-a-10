package com.l0124005.sewain_rpl.ui.theme.katalog

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage

// ============================================================
// DATA MODEL — Edit Item
// Sama strukturnya dengan AddItemFormState, tapi di-preload dari data existing
// ============================================================
data class EditItemFormState(
    val itemId: String = "",
    val itemName: String = "",
    val category: String = "Camping",
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
    // URI = gambar baru dipilih user; imageUrl = gambar lama dari server/storage
    val mainImageUri: Uri? = null,
    val mainImageUrl: String = "",
    val angle1Uri: Uri? = null,
    val angle1Url: String = "",
    val angle2Uri: Uri? = null,
    val angle2Url: String = "",
    val angle3Uri: Uri? = null,
    val angle3Url: String = "",
    val angle4Uri: Uri? = null,
    val angle4Url: String = "",
)

// ============================================================
// EDIT ITEM SCREEN
// ============================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditItemScreen(
    // Ini datang dari navigasi / ViewModel — preload dari item yang dipilih
    initialData: EditItemFormState = EditItemFormState(
        itemName    = "ALLTREK Tenda Camping Tentastic Outdoor 1 BedRoom + 1 Guest Room",
        category    = "Camping",
        price       = "Rp 450.000/hari",
        description = "Tenda yang cocok untuk kalian yang ingin mencoba camping bersama keluarga! " +
                "Tentastic merupakan tenda keluarga yang terdiri atas 2 ruangan (Living Room dan Bedroom)...",
        whatsApp    = "0853-9017-6483",
        lokasi      = "Jl. Ir. H. Juanda No. 50 (Dago), Bandung",
        additionalInfo = "Spesifikasi Produk\nKode : Tentastic PRO\nBrand : ALLTREK\n" +
                "Material Inner : 210D Oxford Waterproof PU 4500MM\nUkuran : 380 x 260 x 185 cm",
        jaminan     = "KTP, SIM, + setengah harga awal",
        denda       = "Rp 10.000/jam",
        stockQty    = 9,
        dateStart   = "30 Mei 2026",
        dateEnd     = "30 Mei 2027",
        mainImageUrl = "", // ganti dengan URL dari server
    ),
    onBack: () -> Unit = {},
    onSaveChanges: (EditItemFormState) -> Unit = {},
    onDeleteListing: () -> Unit = {}
) {
    var form by remember { mutableStateOf(initialData) }

    var showDateStartPicker  by remember { mutableStateOf(false) }
    var showDateEndPicker    by remember { mutableStateOf(false) }
    var showCategorySheet    by remember { mutableStateOf(false) }
    var showDeleteDialog     by remember { mutableStateOf(false) }

    // Image pickers
    val mainImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        it?.let { uri -> form = form.copy(mainImageUri = uri) }
    }
    val angle1Launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        it?.let { uri -> form = form.copy(angle1Uri = uri) }
    }
    val angle2Launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        it?.let { uri -> form = form.copy(angle2Uri = uri) }
    }
    val angle3Launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        it?.let { uri -> form = form.copy(angle3Uri = uri) }
    }
    val angle4Launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        it?.let { uri -> form = form.copy(angle4Uri = uri) }
    }

    Scaffold(
        topBar = { EditItemTopBar(onBack = onBack) },
        bottomBar = {
            SaveChangesBottomBar(onSave = { onSaveChanges(form) })
        },
        containerColor = White
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {

            // ---- HEADER ----
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "My Katalogs",
                    fontFamily = Volkhov,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = Black
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Home", fontFamily = FontFamily.Default, fontSize = 12.sp, color = TextMuted)
                    Text(" › ", fontFamily = FontFamily.Default, fontSize = 12.sp, color = TextMuted)
                    Text(
                        "My Katalogs",
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Black
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ---- PROFILE CARD + DELETE BUTTON ----
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                // Profile Card
                ProfileCard(
                    modifier = Modifier.fillMaxWidth(),
                    nama = "Camping Groups Bandung",
                    lokasi = "Jl. Ir. H. Juanda No. 50 (Dago), Bandung",
                    rating = 4.3f,
                    totalUlasan = 120,
                    jumlahKatalog = 9,
                    whatsapp = form.whatsApp
                )

                // Tombol Delete (pojok kanan atas kartu)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-4).dp, y = 4.dp)
                ) {
                    DeleteButton(onClick = { showDeleteDialog = true })
                }
            }

            Spacer(Modifier.height(24.dp))

            // ---- LABEL FOTO ----
            Text(
                text = "Foto Produk",
                fontFamily = Volkhov,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Black,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(12.dp))

            // ---- MAIN IMAGE (sudah ada foto dari existing item) ----
            EditUploadBoxMain(
                existingUrl = form.mainImageUrl,
                newUri      = form.mainImageUri,
                onClick     = { mainImageLauncher.launch("image/*") },
                modifier    = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .aspectRatio(4f / 3f)
            )

            Spacer(Modifier.height(12.dp))

            // ---- 4 ANGLE THUMBNAILS ----
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    EditUploadBoxSmall(
                        label      = "Angle 1",
                        existingUrl = form.angle1Url,
                        newUri     = form.angle1Uri,
                        onClick    = { angle1Launcher.launch("image/*") }
                    )
                    EditUploadBoxSmall(
                        label      = "Angle 3",
                        existingUrl = form.angle3Url,
                        newUri     = form.angle3Uri,
                        onClick    = { angle3Launcher.launch("image/*") }
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    EditUploadBoxSmall(
                        label      = "Angle 2",
                        existingUrl = form.angle2Url,
                        newUri     = form.angle2Uri,
                        onClick    = { angle2Launcher.launch("image/*") }
                    )
                    EditUploadBoxSmall(
                        label      = "Angle 4",
                        existingUrl = form.angle4Url,
                        newUri     = form.angle4Uri,
                        onClick    = { angle4Launcher.launch("image/*") }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ---- TANGGAL TERSEDIA ----
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DatePickerBox(
                    label   = "Tanggal Item\nMulai Tersedia",
                    value   = form.dateStart,
                    onClick = { showDateStartPicker = true },
                    modifier = Modifier.weight(1f)
                )
                DatePickerBox(
                    label   = "Tanggal Item\nTidak Tersedia",
                    value   = form.dateEnd,
                    onClick = { showDateEndPicker = true },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))

            // ---- ITEM DETAILS FORM (pre-filled) ----
            EditItemDetailsForm(
                form          = form,
                onFormChange  = { form = it },
                onCategoryClick = { showCategorySheet = true },
                modifier      = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )

            Spacer(Modifier.height(32.dp))
        }
    }

    // ---- DATE PICKER DIALOGS ----
    if (showDateStartPicker) {
        SewainDatePickerDialog(
            title = "Tanggal Mulai Tersedia",
            onDismiss = { showDateStartPicker = false },
            onDateSelected = { date ->
                form = form.copy(dateStart = date)
                showDateStartPicker = false
            }
        )
    }
    if (showDateEndPicker) {
        SewainDatePickerDialog(
            title = "Tanggal Tidak Tersedia",
            onDismiss = { showDateEndPicker = false },
            onDateSelected = { date ->
                form = form.copy(dateEnd = date)
                showDateEndPicker = false
            }
        )
    }

    // ---- CATEGORY BOTTOM SHEET ----
    if (showCategorySheet) {
        CategoryBottomSheet(
            selectedCategory = form.category,
            onSelect = { selected ->
                form = form.copy(category = selected)
                showCategorySheet = false
            },
            onDismiss = { showCategorySheet = false }
        )
    }

    // ---- DELETE CONFIRMATION DIALOG ----
    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            itemName  = form.itemName,
            onConfirm = {
                showDeleteDialog = false
                onDeleteListing()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

// ============================================================
// TOP BAR — Edit Item
// ============================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditItemTopBar(onBack: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = buildAnnotatedString {
                    append("SEWA")
                    withStyle(androidx.compose.ui.text.SpanStyle(color = Primary)) { append("IN") }
                },
                fontFamily = Volkhov,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Black
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Black)
            }
        },
        actions = {
            IconButton(onClick = {}) {
                Icon(Icons.Outlined.Person, contentDescription = "Profil", tint = Black)
            }
            BadgedBox(badge = { Badge { Text("3") } }) {
                IconButton(onClick = {}) {
                    Icon(Icons.Outlined.Notifications, contentDescription = "Notifikasi", tint = Black)
                }
            }
            BadgedBox(badge = { Badge { Text("2") } }) {
                IconButton(onClick = {}) {
                    Icon(Icons.Outlined.ShoppingCart, contentDescription = "Keranjang", tint = Black)
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
    )
}

// ============================================================
// DELETE BUTTON — pill shape, merah saat hover/pressed
// ============================================================
@Composable
fun DeleteButton(onClick: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }

    Button(
        onClick = onClick,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isPressed) Color(0xFFD8262C) else Primary,
            contentColor   = White
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        modifier = Modifier
            .pointerInput(Unit) {
                // Deteksi press untuk ubah warna (opsional)
            }
    ) {
        Icon(
            Icons.Default.Delete,
            contentDescription = "Hapus Listing",
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "Hapus",
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}

// ============================================================
// MAIN IMAGE UPLOAD BOX — Edit Mode
// Prioritas tampil: newUri > existingUrl > placeholder kosong
// ============================================================
@Composable
fun EditUploadBoxMain(
    existingUrl: String,
    newUri: Uri?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasImage = newUri != null || existingUrl.isNotEmpty()

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (hasImage) 1.dp else 2.dp,
                color = UploadBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .background(if (hasImage) Color.Transparent else UploadBg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        when {
            // Gambar baru dipilih user
            newUri != null -> {
                AsyncImage(
                    model = newUri,
                    contentDescription = "Foto utama baru",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                )
            }
            // Gambar lama dari server
            existingUrl.isNotEmpty() -> {
                AsyncImage(
                    model = existingUrl,
                    contentDescription = "Foto utama",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                )
            }
            // Kosong — tampilkan placeholder
            else -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Outlined.CameraAlt,
                        contentDescription = null,
                        tint = Black,
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        "Upload Foto / Image",
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextDark
                    )
                }
            }
        }

        // Edit overlay — selalu tampil di pojok kanan bawah
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
                .size(38.dp)
                .clip(CircleShape)
                .background(White.copy(alpha = 0.92f))
                .border(0.5.dp, UploadBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.Edit,
                contentDescription = "Ganti foto",
                tint = Primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ============================================================
// SMALL UPLOAD BOX — Edit Mode
// ============================================================
@Composable
fun EditUploadBoxSmall(
    label: String,
    existingUrl: String,
    newUri: Uri?,
    onClick: () -> Unit
) {
    val hasImage = newUri != null || existingUrl.isNotEmpty()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.1f)
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = if (hasImage) 1.dp else 2.dp,
                color = UploadBorder,
                shape = RoundedCornerShape(10.dp)
            )
            .background(if (hasImage) Color.Transparent else UploadBg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        when {
            newUri != null -> {
                AsyncImage(
                    model = newUri,
                    contentDescription = label,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp))
                )
            }
            existingUrl.isNotEmpty() -> {
                AsyncImage(
                    model = existingUrl,
                    contentDescription = label,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp))
                )
            }
            else -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Outlined.Image,
                        contentDescription = null,
                        tint = Black,
                        modifier = Modifier.size(26.dp)
                    )
                    Text(
                        text = label,
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = TextDark
                    )
                }
            }
        }

        // Edit overlay kecil
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(6.dp)
                .size(28.dp)
                .clip(CircleShape)
                .background(White.copy(alpha = 0.92f))
                .border(0.5.dp, UploadBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.Edit,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}

// ============================================================
// ITEM DETAILS FORM — Pre-filled (sama struktur, value diisi)
// ============================================================
@Composable
fun EditItemDetailsForm(
    form: EditItemFormState,
    onFormChange: (EditItemFormState) -> Unit,
    onCategoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, SectionBorder, RoundedCornerShape(12.dp))
            .background(SectionBg)
            .padding(20.dp)
    ) {
        Text(
            text = "Item Details",
            fontFamily = Volkhov,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Black
        )
        Spacer(Modifier.height(16.dp))

        // Item Name
        SewainFormField(
            label = "Item Name",
            value = form.itemName,
            onValueChange = { onFormChange(form.copy(itemName = it)) },
            placeholder = "ex: Tas Carrier Geiser Adv 60 Liter"
        )

        // Category
        SewainFormLabel(label = "Category")
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .border(1.dp, BorderGray, RoundedCornerShape(6.dp))
                .background(FormBg)
                .clickable(onClick = onCategoryClick)
                .padding(horizontal = 14.dp, vertical = 11.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = form.category,
                    fontFamily = FontFamily.Default,
                    fontSize = 13.sp,
                    color = TextDark
                )
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = TextDark,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(Modifier.height(14.dp))

        // Harga
        SewainFormField(
            label = "Harga",
            value = form.price,
            onValueChange = { onFormChange(form.copy(price = it)) },
            placeholder = "ex: Rp 100.000/hari"
        )

        // Description
        SewainFormField(
            label = "Description",
            value = form.description,
            onValueChange = { onFormChange(form.copy(description = it)) },
            placeholder = "Deskripsi produk...",
            singleLine = false,
            minLines = 4
        )

        // WhatsApp
        SewainFormField(
            label = "WhatsApp",
            value = form.whatsApp,
            onValueChange = { onFormChange(form.copy(whatsApp = it)) },
            placeholder = "ex: No HP",
            keyboardType = KeyboardType.Phone
        )

        // Lokasi
        SewainFormField(
            label = "Lokasi",
            value = form.lokasi,
            onValueChange = { onFormChange(form.copy(lokasi = it)) },
            placeholder = "ex: Jl. Braga, Kab. Bandung"
        )

        HorizontalDivider(color = UploadBorder, thickness = 0.5.dp)
        Spacer(Modifier.height(14.dp))

        // Additional Info
        SewainFormField(
            label = "Additional Informations",
            value = form.additionalInfo,
            onValueChange = { onFormChange(form.copy(additionalInfo = it)) },
            placeholder = "Spesifikasi, fitur, dsb...",
            singleLine = false,
            minLines = 7
        )

        // Jaminan
        SewainFormField(
            label = "Jaminan",
            value = form.jaminan,
            onValueChange = { onFormChange(form.copy(jaminan = it)) },
            placeholder = "ex: KTP, SIM, + setengah harga awal"
        )

        // Denda
        SewainFormField(
            label = "Denda",
            value = form.denda,
            onValueChange = { onFormChange(form.copy(denda = it)) },
            placeholder = "ex: Rp 10.000/jam"
        )

        // Stock Quantity
        SewainFormLabel(label = "Stock Quantity")
        Spacer(Modifier.height(6.dp))
        StockQtyStepper(
            qty    = form.stockQty,
            onMinus = { if (form.stockQty > 1) onFormChange(form.copy(stockQty = form.stockQty - 1)) },
            onPlus  = { if (form.stockQty < 99) onFormChange(form.copy(stockQty = form.stockQty + 1)) }
        )
    }
}

// ============================================================
// SAVE CHANGES BOTTOM BAR
// ============================================================
@Composable
fun SaveChangesBottomBar(onSave: () -> Unit) {
    Surface(
        shadowElevation = 8.dp,
        color = White
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4D6674),
                    contentColor   = White
                )
            ) {
                Icon(
                    Icons.Outlined.Save,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "SAVE CHANGES",
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

// ============================================================
// DELETE CONFIRMATION DIALOG
// ============================================================
@Composable
fun DeleteConfirmationDialog(
    itemName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = White,
        shape = RoundedCornerShape(16.dp),
        icon = {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFEBEB)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color(0xFFD8262C),
                    modifier = Modifier.size(26.dp)
                )
            }
        },
        title = {
            Text(
                text = "Hapus Listing?",
                fontFamily = Volkhov,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                text = "Kamu yakin mau menghapus\n\"${itemName.take(40)}${if (itemName.length > 40) "..." else ""}\"?\n\nAksi ini tidak dapat dibatalkan.",
                fontFamily = FontFamily.Default,
                fontSize = 14.sp,
                color = TextDark,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.fillMaxWidth()
            )
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, BorderGray),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextDark),
                modifier = Modifier.fillMaxWidth(0.48f)
            ) {
                Text(
                    "Batal",
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD8262C),
                    contentColor   = White
                ),
                modifier = Modifier.fillMaxWidth(0.48f)
            ) {
                Text(
                    "Hapus",
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    )
}

// ============================================================
// CATATAN PENGGUNAAN BERSAMA
// ============================================================
//
// File ini REUSE komponen dari KatalogComponents.kt
// ============================================================

// NAVIGASI — contoh penggunaan di NavGraph:
//
//   composable("edit_item/{itemId}") { backStackEntry ->
//       val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
//       val viewModel: EditItemViewModel = hiltViewModel()
//       val initialData by viewModel.getItemById(itemId).collectAsState()
//
//       EditItemScreen(
//           initialData      = initialData,
//           onBack           = { navController.popBackStack() },
//           onSaveChanges    = { updated -> viewModel.save(updated) },
//           onDeleteListing  = { viewModel.delete(itemId) }
//       )
//   }

@Preview(showBackground = true)
@Composable
fun EditItemScreenPreview() {
    EditItemScreen()
}