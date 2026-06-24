package com.l0124005.sewain_rpl.ui.theme.katalog

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.l0124005.sewain_rpl.utils.DateUtils
import com.l0124005.sewain_rpl.utils.ImageUtils
import com.l0124005.sewain_rpl.utils.Resource
import com.l0124005.sewain_rpl.viewmodel.KatalogViewModel
import com.l0124005.sewain_rpl.viewmodel.ProfileViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import com.l0124005.sewain_rpl.ui.theme.MonsterratFont
import com.l0124005.sewain_rpl.ui.theme.SewainTopBar
import com.l0124005.sewain_rpl.ui.theme.VolkhovFont

// ============================================================
// ADD ITEM SCREEN
// ============================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemScreen(
    viewModel: KatalogViewModel,
    profileViewModel: ProfileViewModel,
    token: String,
    onBack: () -> Unit = {},
    onSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    var form by remember { mutableStateOf(AddItemFormState()) }
    var showDateStartPicker by remember { mutableStateOf(false) }
    var showDateEndPicker   by remember { mutableStateOf(false) }
    var showCategorySheet   by remember { mutableStateOf(false) }

    val crudResult by viewModel.crudResult.observeAsState()
    val kategoriResult by viewModel.kategori.observeAsState()
    val profileState by profileViewModel.profile.observeAsState()

    LaunchedEffect(Unit) {
        profileViewModel.getProfile(token)
        viewModel.getKategori()
    }

    LaunchedEffect(profileState) {
        if (profileState is Resource.Success) {
            val user = profileState?.data?.data
            if (user != null) {
                // Auto-fill WhatsApp jika masih kosong
                if (form.whatsApp.isEmpty() && !user.phone_number.isNullOrEmpty()) {
                    form = form.copy(whatsApp = user.phone_number)
                }
                // Auto-fill Lokasi jika masih kosong
                if (form.lokasi.isEmpty() && !user.alamat.isNullOrEmpty()) {
                    form = form.copy(lokasi = user.alamat)
                }
            }
        }
    }

    LaunchedEffect(crudResult) {
        when (crudResult) {
            is Resource.Success -> {
                Toast.makeText(context, "Katalog berhasil dipublikasikan!", Toast.LENGTH_SHORT).show()
                onSuccess()
                viewModel.resetStates()
            }
            is Resource.Error -> {
                Toast.makeText(context, "Gagal publish: ${crudResult?.message}", Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

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
        topBar = {
            SewainTopBar(
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = onBack
            )
        },
        bottomBar = {
            PublishBottomBar(
                onPublish = {
                    if (form.itemName.isEmpty() || form.categoryId.isEmpty() || form.mainImageUri == null) {
                        Toast.makeText(context, "Nama, Kategori, dan Foto Utama wajib diisi", Toast.LENGTH_SHORT).show()
                        return@PublishBottomBar
                    }

                    val namePart = form.itemName.toRequestBody("text/plain".toMediaTypeOrNull())
                    val catPart  = form.categoryId.toRequestBody("text/plain".toMediaTypeOrNull())
                    
                    val cleanPrice = form.price.filter { it.isDigit() }.ifEmpty { "0" }
                    val cleanJaminan = form.jaminan.filter { it.isDigit() }.ifEmpty { "0" }
                    val cleanDenda = form.denda.filter { it.isDigit() }.ifEmpty { "0" }

                    val sewaPart    = cleanPrice.toRequestBody("text/plain".toMediaTypeOrNull())
                    val jaminanPart = cleanJaminan.toRequestBody("text/plain".toMediaTypeOrNull())
                    val dendaPart   = cleanDenda.toRequestBody("text/plain".toMediaTypeOrNull())
                    val stokPart    = form.stockQty.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    val locPart     = form.lokasi.toRequestBody("text/plain".toMediaTypeOrNull())
                    val statusPart  = "tersedia".toRequestBody("text/plain".toMediaTypeOrNull())

                    // Gunakan null jika field opsional kosong agar tidak error di backend
                    val waPart      = if (form.whatsApp.isNotBlank()) form.whatsApp.toRequestBody("text/plain".toMediaTypeOrNull()) else null
                    val descPart    = if (form.description.isNotBlank()) form.description.toRequestBody("text/plain".toMediaTypeOrNull()) else null
                    val addInfoPart = if (form.additionalInfo.isNotBlank()) form.additionalInfo.toRequestBody("text/plain".toMediaTypeOrNull()) else null
                    
                    val dateStartBackend = DateUtils.formatDateForBackend(form.dateStart)
                    val dateEndBackend   = DateUtils.formatDateForBackend(form.dateEnd)
                    
                    val dStartPart = if (dateStartBackend.isNotBlank()) dateStartBackend.toRequestBody("text/plain".toMediaTypeOrNull()) else null
                    val dEndPart   = if (dateEndBackend.isNotBlank()) dateEndBackend.toRequestBody("text/plain".toMediaTypeOrNull()) else null

                    // Helper function to create MultipartBody.Part for a specific field name
                    fun createPart(uri: Uri?, fieldName: String): MultipartBody.Part? {
                        if (uri == null) return null
                        return try {
                            val file = ImageUtils.compressImage(context, uri, 1024) ?: return null
                            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                            MultipartBody.Part.createFormData(fieldName, file.name, requestFile)
                        } catch (e: Exception) {
                            Log.e("AddItem", "Gagal memproses $fieldName: ${e.message}")
                            null
                        }
                    }

                    val partMain   = createPart(form.mainImageUri, "foto_barang")
                    val partAngle1 = createPart(form.angle1Uri, "fotoproduk1")
                    val partAngle2 = createPart(form.angle2Uri, "fotoproduk2")
                    val partAngle3 = createPart(form.angle3Uri, "fotoproduk3")
                    val partAngle4 = createPart(form.angle4Uri, "fotoproduk4")

                    Log.d("AddItem", "Publishing: main=${partMain!=null}, a1=${partAngle1!=null}, a2=${partAngle2!=null}, a3=${partAngle3!=null}, a4=${partAngle4!=null}")

                    viewModel.createKatalog(
                        token, catPart, namePart, descPart, sewaPart, jaminanPart, 
                        dendaPart, stokPart, locPart, waPart, dStartPart, dEndPart,
                        addInfoPart,
                        partMain, partAngle1, partAngle2, partAngle3, partAngle4, 
                        statusPart
                    )
                },
                isLoading = crudResult is Resource.Loading
            )
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
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "My Katalogs",
                    fontFamily = VolkhovFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp,          // disamakan ukuran besar web (36px desktop, scaled ke mobile)
                    color = Black
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Text("My Katalog", fontSize = 12.sp, color = TextMuted)
                    Text(" › ", fontSize = 12.sp, color = TextMuted)
                    Text("Add Item", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Black)
                }
            }

            Spacer(Modifier.height(24.dp))

            // ---- PROFILE CARD ----
            val user = (profileState as? Resource.Success)?.data?.data
            ProfileCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                nama = user?.name ?: "Loading...",
                lokasi = user?.alamat ?: "Unknown",
                rating = 4.5f,
                totalUlasan = 12,
                jumlahKatalog = 0,
                whatsapp = user?.phone_number ?: "",
                fotoProfil = user?.foto_profil
            )

            Spacer(Modifier.height(24.dp))

            // ---- UPLOAD SECTION LABEL ----
            Text(
                text = "Foto Produk",
                fontFamily = VolkhovFont,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Black,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(12.dp))

            // ---- MAIN IMAGE UPLOAD ----
            UploadBoxMain(
                uri = form.mainImageUri,
                onClick = { mainImageLauncher.launch("image/*") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .aspectRatio(4f / 3f)
            )

            Spacer(Modifier.height(12.dp))

            // ---- 4 ANGLE THUMBNAILS (2x2 grid) ----
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
                    UploadBoxSmall(
                        uri = form.angle1Uri,
                        label = "Angle 1",
                        onClick = { angle1Launcher.launch("image/*") }
                    )
                    UploadBoxSmall(
                        uri = form.angle3Uri,
                        label = "Angle 3",
                        onClick = { angle3Launcher.launch("image/*") }
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    UploadBoxSmall(
                        uri = form.angle2Uri,
                        label = "Angle 2",
                        onClick = { angle2Launcher.launch("image/*") }
                    )
                    UploadBoxSmall(
                        uri = form.angle4Uri,
                        label = "Angle 4",
                        onClick = { angle4Launcher.launch("image/*") }
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
                    label = "Tanggal Item\nMulai Tersedia",
                    value = form.dateStart,
                    onClick = { showDateStartPicker = true },
                    modifier = Modifier.weight(1f)
                )
                DatePickerBox(
                    label = "Tanggal Item\nTidak Tersedia",
                    value = form.dateEnd,
                    onClick = { showDateEndPicker = true },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))

            // ---- ITEM DETAILS FORM ----
            ItemDetailsForm(
                form = form,
                onFormChange = { form = it },
                onCategoryClick = {
                    if (kategoriResult !is Resource.Success) {
                        viewModel.getKategori()
                    }
                    showCategorySheet = true
                },
                modifier = Modifier
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
        when (kategoriResult) {
            is Resource.Success -> {
                val list = kategoriResult?.data?.data ?: emptyList()
                CategoryBottomSheetDynamic(
                    categories = list,
                    selectedCategoryId = form.categoryId,
                    onSelect = { selectedId, selectedName ->
                        form = form.copy(
                            category = selectedName,
                            categoryId = selectedId.toString()
                        )
                        showCategorySheet = false
                    },
                    onDismiss = { showCategorySheet = false }
                )
            }
            is Resource.Loading -> {
                // Bisa tambahkan loading indicator jika perlu
            }
            is Resource.Error -> {
                Toast.makeText(context, "Gagal memuat kategori: ${kategoriResult?.message}", Toast.LENGTH_SHORT).show()
                showCategorySheet = false
            }
            else -> {
                // Jika null, panggil lagi
                viewModel.getKategori()
            }
        }
    }
}

// ============================================================
// TOP BAR
// ============================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemTopBar(onBack: () -> Unit) {
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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Black)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
    )
}

// ============================================================
// UPLOAD BOXES
// ============================================================
@Composable
fun UploadBoxMain(
    uri: Uri?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, AddItemSectionBorder, RoundedCornerShape(12.dp))
            .background(AddItemSectionBg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (uri != null) {
            AsyncImage(model = uri, contentDescription = "Foto utama", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            Box(
                modifier = Modifier.align(Alignment.BottomEnd).padding(10.dp).size(34.dp).clip(CircleShape).background(White.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Edit, contentDescription = "Ganti foto", tint = AddItemAccent, modifier = Modifier.size(18.dp))
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Outlined.CameraAlt, contentDescription = null, tint = Color(0xFF1E1E1E), modifier = Modifier.size(40.dp))
                Text(
                    text = "Upload Foto/Image",
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF484848)
                )
            }
        }
    }
}
@Composable
fun UploadBoxSmall(
    uri: Uri?,
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.1f)
            .clip(RoundedCornerShape(10.dp))
            .border(2.dp, UploadBorder, RoundedCornerShape(10.dp))
            .background(UploadBg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (uri != null) {
            AsyncImage(
                model = uri,
                contentDescription = label,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(White.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Edit, contentDescription = null,
                    tint = Primary, modifier = Modifier.size(13.dp)
                )
            }
        } else {
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
}

// ============================================================
// ITEM DETAILS FORM
// ============================================================
@Composable
fun ItemDetailsForm(
    form: AddItemFormState,
    onFormChange: (AddItemFormState) -> Unit,
    onCategoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, AddItemSectionBorder, RoundedCornerShape(12.dp))
            .background(AddItemSectionBg30)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Text(
            text = "Item Details",
            fontFamily = VolkhovFont,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
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

        // Category (tap to open sheet)
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
                    color = if (form.category.isEmpty()) TextMuted else TextDark
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
            placeholder = "ex: Rp 100.000/hari",
            keyboardType = KeyboardType.Number
        )

        // Description
        SewainFormField(
            label = "Description",
            value = form.description,
            onValueChange = { onFormChange(form.copy(description = it)) },
            placeholder = "ex: Tas Camping yang memiliki bahan anti air...",
            singleLine = false,
            minLines = 3
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

        // Additional Information
        SewainFormField(
            label = "Additional Informations",
            value = form.additionalInfo,
            onValueChange = { onFormChange(form.copy(additionalInfo = it)) },
            placeholder = "ex: Spesifikasi :\n- Bahan : Nylon + Polyester\n- Ukuran : 58 x 30 x 20 cm",
            singleLine = false,
            minLines = 6
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
            qty = form.stockQty,
            onMinus = { if (form.stockQty > 1) onFormChange(form.copy(stockQty = form.stockQty - 1)) },
            onPlus  = { if (form.stockQty < 99) onFormChange(form.copy(stockQty = form.stockQty + 1)) }
        )
    }
}

// ============================================================
// PUBLISH BOTTOM BAR
// ============================================================
@Composable
fun PublishBottomBar(onPublish: () -> Unit, isLoading: Boolean) {
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
                onClick = onPublish,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AddItemAccent,
                    contentColor   = White
                ),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = White, modifier = Modifier.size(24.dp))
                } else {
                    Icon(
                        Icons.Outlined.Publish,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "PUBLISH ITEM",
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddItemScreenPreview() {
    // AddItemScreen()
}
