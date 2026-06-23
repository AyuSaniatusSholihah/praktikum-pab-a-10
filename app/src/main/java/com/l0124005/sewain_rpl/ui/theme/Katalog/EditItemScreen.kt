package com.l0124005.sewain_rpl.ui.theme.katalog

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.l0124005.sewain_rpl.network.ApiClient
import com.l0124005.sewain_rpl.network.CatalogData
import com.l0124005.sewain_rpl.utils.DateUtils
import com.l0124005.sewain_rpl.utils.ImageUtils
import com.l0124005.sewain_rpl.utils.Resource
import com.l0124005.sewain_rpl.viewmodel.KatalogViewModel
import com.l0124005.sewain_rpl.ui.theme.SewainTopBar
import com.l0124005.sewain_rpl.ui.theme.VolkhovFont
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditItemScreen(
    viewModel: KatalogViewModel,
    token: String,
    itemId: Int,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val itemDetailState by viewModel.myKatalogDetail.observeAsState()
    val kategoriResult  by viewModel.kategori.observeAsState()
    val crudResult      by viewModel.crudResult.observeAsState()
    val deleteResult    by viewModel.deleteResult.observeAsState()

    // ── Form state ──
    var itemName            by remember { mutableStateOf("") }
    var categoryId          by remember { mutableIntStateOf(1) }
    var categoryName        by remember { mutableStateOf("") }
    var priceSewa           by remember { mutableStateOf("") }
    var priceJaminan        by remember { mutableStateOf("") }
    var priceDenda          by remember { mutableStateOf("") }
    var description         by remember { mutableStateOf("") }
    var additionalInformation by remember { mutableStateOf("") }
    var stock               by remember { mutableStateOf("1") }
    var location            by remember { mutableStateOf("") }
    var whatsApp            by remember { mutableStateOf("") }
    var dateStart           by remember { mutableStateOf("") }
    var dateEnd             by remember { mutableStateOf("") }

    // ── Foto state ──
    var imageUri            by remember { mutableStateOf<Uri?>(null) }
    var existingImageUrl    by remember { mutableStateOf("") }
    var angle1Uri           by remember { mutableStateOf<Uri?>(null) }
    var angle2Uri           by remember { mutableStateOf<Uri?>(null) }
    var angle3Uri           by remember { mutableStateOf<Uri?>(null) }
    var angle4Uri           by remember { mutableStateOf<Uri?>(null) }
    var existingAngle1Url   by remember { mutableStateOf("") }
    var existingAngle2Url   by remember { mutableStateOf("") }
    var existingAngle3Url   by remember { mutableStateOf("") }
    var existingAngle4Url   by remember { mutableStateOf("") }

    // ── UI state ──
    var showCategorySheet   by remember { mutableStateOf(false) }
    var showDeleteDialog    by remember { mutableStateOf(false) }
    var showDateStartPicker by remember { mutableStateOf(false) }
    var showDateEndPicker   by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    // ── Launchers ──
    val imageLauncher  = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { imageUri = it }
    val angle1Launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { angle1Uri = it }
    val angle2Launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { angle2Uri = it }
    val angle3Launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { angle3Uri = it }
    val angle4Launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { angle4Uri = it }

    // ── Load detail ──
    LaunchedEffect(itemId) {
        viewModel.resetCrudResult()
        viewModel.resetMyKatalogDetail()
        viewModel.getMyKatalogDetail(token, itemId)
        viewModel.getKategori()
    }

    // ── Isi form dari server ──
    LaunchedEffect(itemDetailState) {
        if (itemDetailState is Resource.Success) {
            val data = (itemDetailState as Resource.Success).data?.data
            data?.let {
                itemName              = it.nama_barang ?: ""
                categoryId            = it.kategori_id
                priceSewa             = it.harga_sewa.toLong().toString()
                priceJaminan          = it.harga_jaminan.toLong().toString()
                priceDenda            = it.harga_denda_perjam.toLong().toString()
                description           = it.deskripsi ?: ""
                additionalInformation = it.additional_information ?: ""
                stock                 = it.stok.toString()
                location              = it.lokasi ?: ""
                whatsApp              = it.whatsapp ?: ""
                
                // Konversi format tanggal dari yyyy-MM-dd ke dd MMMM yyyy untuk UI
                dateStart             = DateUtils.formatDateForUI(it.tanggal_mulai)
                dateEnd               = DateUtils.formatDateForUI(it.tanggal_akhir)
                
                existingImageUrl      = it.foto_barang ?: ""
                existingAngle1Url     = it.fotoproduk1 ?: ""
                existingAngle2Url     = it.fotoproduk2 ?: ""
                existingAngle3Url     = it.fotoproduk3 ?: ""
                existingAngle4Url     = it.fotoproduk4 ?: ""

                val cats = (kategoriResult as? Resource.Success)?.data?.data
                categoryName = cats?.find { c -> c.id == it.kategori_id }?.nama_kategori ?: ""
                
                Log.d("EditItem", "Loaded Data: WA=$whatsApp, Info=$additionalInformation, Start=$dateStart, End=$dateEnd")
            }
        }
    }

    // ── Sinkron nama kategori ──
    LaunchedEffect(kategoriResult) {
        val cats = (kategoriResult as? Resource.Success)?.data?.data
        if (cats != null && categoryName.isEmpty()) {
            categoryName = cats.find { c -> c.id == categoryId }?.nama_kategori ?: ""
        }
    }

    // ── Observer CRUD ──
    LaunchedEffect(crudResult) {
        when (val r = crudResult) {
            is Resource.Success -> {
                snackbarHostState.showSnackbar("Berhasil menyimpan perubahan!")
                onSuccess()
            }
            is Resource.Error -> {
                snackbarHostState.showSnackbar("Gagal menyimpan: ${r.message ?: "Terjadi kesalahan"}")
            }
            else -> {}
        }
    }

    // ── Observer Delete ──
    LaunchedEffect(deleteResult) {
        when (val r = deleteResult) {
            is Resource.Success -> {
                snackbarHostState.showSnackbar("Listing berhasil dihapus")
                onSuccess()
            }
            is Resource.Error -> {
                snackbarHostState.showSnackbar("Gagal menghapus: ${r.message ?: "Terjadi kesalahan"}")
            }
            else -> {}
        }
    }

    // ── Dialog Hapus ──
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    "Hapus Listing",
                    fontFamily = VolkhovFont,
                    fontWeight = FontWeight.Bold,
                    color = Black
                )
            },
            text = { Text("Yakin mau hapus listing ini? Aksi ini tidak bisa dibatalkan.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteKatalog(token, itemId)
                    showDeleteDialog = false
                }) {
                    Text("Hapus", color = Color(0xFFD8262C), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal", color = Primary)
                }
            }
        )
    }

    // ── Date Pickers ──
    if (showDateStartPicker) {
        SewainDatePickerDialog(
            title = "Tanggal Mulai Tersedia",
            onDismiss = { showDateStartPicker = false },
            onDateSelected = { date -> dateStart = date; showDateStartPicker = false }
        )
    }
    if (showDateEndPicker) {
        SewainDatePickerDialog(
            title = "Tanggal Tidak Tersedia",
            onDismiss = { showDateEndPicker = false },
            onDateSelected = { date -> dateEnd = date; showDateEndPicker = false }
        )
    }

    // ── Category Bottom Sheet ──
    if (showCategorySheet) {
        val availableCategories = (kategoriResult as? Resource.Success)?.data?.data ?: emptyList()
        CategoryBottomSheetDynamic(
            categories = availableCategories,
            selectedCategoryId = categoryId.toString(),
            onSelect = { id, name ->
                categoryId   = id
                categoryName = name
                showCategorySheet = false
            },
            onDismiss = { showCategorySheet = false }
        )
    }

    // ══════════════════════════════════════
    // SCAFFOLD
    // ══════════════════════════════════════
    Scaffold(
        topBar = {
            SewainTopBar(
                navigationIcon    = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = onBack
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = White
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            // ── Header: Judul + Breadcrumb + Tombol Hapus ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, start = 20.dp, end = 20.dp)
            ) {
                Column(
                    modifier            = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text       = "My Katalogs",
                        fontFamily = VolkhovFont,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 30.sp,
                        color      = Black
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("My Katalog", fontSize = 12.sp, color = TextMuted)
                        Text(" › ", fontSize = 12.sp, color = TextMuted)
                        Text(
                            "Edit Item",
                            fontWeight = FontWeight.Bold,
                            fontSize   = 12.sp,
                            color      = Black
                        )
                    }
                }

                // Tombol hapus di pojok kanan
                IconButton(
                    onClick  = { showDeleteDialog = true },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD8262C))
                ) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = "Hapus Listing",
                        tint               = White,
                        modifier           = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Error / Loading indicator ──
            if (itemDetailState is Resource.Error) {
                Card(
                    colors   = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                ) {
                    Text(
                        text     = "Gagal memuat data: ${(itemDetailState as Resource.Error).message}",
                        color    = Color(0xFFB71C1C),
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
            if (itemDetailState is Resource.Loading) {
                Box(
                    modifier         = Modifier.fillMaxWidth().height(60.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = Primary) }
                Spacer(Modifier.height(16.dp))
            }

            // ── Label Foto ──
            Text(
                text       = "Foto Produk",
                fontFamily = VolkhovFont,
                fontWeight = FontWeight.Bold,
                fontSize   = 16.sp,
                color      = Black,
                modifier   = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(12.dp))

            // ── Foto Utama (pakai UploadBoxMainEdit agar existing foto tampil) ──
            UploadBoxMainEdit(
                uri         = imageUri,
                existingUrl = CatalogData.EMPTY.getFullUrl(existingImageUrl),
                onClick     = { imageLauncher.launch("image/*") },
                modifier    = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .aspectRatio(4f / 3f)
            )

            Spacer(Modifier.height(12.dp))

            // ── 4 Angle Thumbnails ──
            Row(
                modifier              = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(
                    modifier            = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    UploadBoxSmallEdit(uri = angle1Uri, existingUrl = CatalogData.EMPTY.getFullUrl(existingAngle1Url), label = "Angle 1", onClick = { angle1Launcher.launch("image/*") })
                    UploadBoxSmallEdit(uri = angle3Uri, existingUrl = CatalogData.EMPTY.getFullUrl(existingAngle3Url), label = "Angle 3", onClick = { angle3Launcher.launch("image/*") })
                }
                Column(
                    modifier            = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    UploadBoxSmallEdit(uri = angle2Uri, existingUrl = CatalogData.EMPTY.getFullUrl(existingAngle2Url), label = "Angle 2", onClick = { angle2Launcher.launch("image/*") })
                    UploadBoxSmallEdit(uri = angle4Uri, existingUrl = CatalogData.EMPTY.getFullUrl(existingAngle4Url), label = "Angle 4", onClick = { angle4Launcher.launch("image/*") })
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Tanggal Tersedia ──
            Row(
                modifier              = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DatePickerBox(
                    label   = "Tanggal Item\nMulai Tersedia",
                    value   = dateStart,
                    onClick = { showDateStartPicker = true },
                    modifier = Modifier.weight(1f)
                )
                DatePickerBox(
                    label   = "Tanggal Item\nTidak Tersedia",
                    value   = dateEnd,
                    onClick = { showDateEndPicker = true },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Item Details Form ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, AddItemSectionBorder, RoundedCornerShape(12.dp))
                    .background(AddItemSectionBg30)
                    .padding(20.dp)
            ) {
                Text(
                    text       = "Item Details",
                    fontFamily = VolkhovFont,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 22.sp,
                    color      = Black
                )
                Spacer(Modifier.height(16.dp))

                // Item Name
                SewainFormField(
                    label         = "Item Name",
                    value         = itemName,
                    onValueChange = { itemName = it },
                    placeholder   = "ex: Tas Carrier Geiser Adv 60 Liter"
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
                        .clickable { showCategorySheet = true }
                        .padding(horizontal = 14.dp, vertical = 11.dp)
                ) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            text       = categoryName.ifEmpty { "Pilih kategori" },
                            fontFamily = FontFamily.Default,
                            fontSize   = 13.sp,
                            color      = if (categoryName.isEmpty()) TextMuted else TextDark
                        )
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint               = TextDark,
                            modifier           = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))

                // Harga
                SewainFormField(
                    label         = "Harga",
                    value         = priceSewa,
                    onValueChange = { priceSewa = it.filter { c -> c.isDigit() } },
                    placeholder   = "ex: Rp 100.000/hari",
                    keyboardType  = KeyboardType.Number
                )

                // Description
                SewainFormField(
                    label         = "Description",
                    value         = description,
                    onValueChange = { description = it },
                    placeholder   = "ex: Tas Camping yang memiliki bahan anti air...",
                    singleLine    = false,
                    minLines      = 3
                )

                // WhatsApp
                SewainFormField(
                    label         = "WhatsApp",
                    value         = whatsApp,
                    onValueChange = { whatsApp = it },
                    placeholder   = "ex: No HP",
                    keyboardType  = KeyboardType.Phone
                )

                // Lokasi
                SewainFormField(
                    label         = "Lokasi",
                    value         = location,
                    onValueChange = { location = it },
                    placeholder   = "ex: Jl. Braga, Kab. Bandung"
                )

                HorizontalDivider(color = UploadBorder, thickness = 0.5.dp)
                Spacer(Modifier.height(14.dp))

                // Additional Information
                SewainFormField(
                    label         = "Additional Informations",
                    value         = additionalInformation,
                    onValueChange = { additionalInformation = it },
                    placeholder   = "ex: Spesifikasi :\n- Bahan : Nylon + Polyester\n- Ukuran : 58 x 30 x 20 cm",
                    singleLine    = false,
                    minLines      = 6
                )

                // Jaminan
                SewainFormField(
                    label         = "Jaminan",
                    value         = priceJaminan,
                    onValueChange = { priceJaminan = it.filter { c -> c.isDigit() } },
                    placeholder   = "ex: KTP, SIM, + setengah harga awal",
                    keyboardType  = KeyboardType.Number
                )

                // Denda
                SewainFormField(
                    label         = "Denda",
                    value         = priceDenda,
                    onValueChange = { priceDenda = it.filter { c -> c.isDigit() } },
                    placeholder   = "ex: Rp 10.000/jam",
                    keyboardType  = KeyboardType.Number
                )

                // Stock
                SewainFormLabel(label = "Stock Quantity")
                Spacer(Modifier.height(6.dp))
                StockQtyStepper(
                    qty     = stock.toIntOrNull() ?: 1,
                    onMinus = { val v = stock.toIntOrNull() ?: 1; if (v > 1) stock = (v - 1).toString() },
                    onPlus  = { val v = stock.toIntOrNull() ?: 1; if (v < 99) stock = (v + 1).toString() }
                )
                Spacer(Modifier.height(16.dp))
            }

            Spacer(Modifier.height(24.dp))

            // ── Tombol Save Changes ──
            val isFormValid = itemName.isNotBlank() && priceSewa.isNotBlank() && location.isNotBlank()
            val isSaving    = crudResult is Resource.Loading

            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                Button(
                    onClick = {
                        if (!isFormValid) return@Button
                        viewModel.resetCrudResult()

                        val namePart     = itemName.toRequestBody("text/plain".toMediaTypeOrNull())
                        val catPart      = categoryId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                        val sewaPart     = priceSewa.ifEmpty { "0" }.toRequestBody("text/plain".toMediaTypeOrNull())
                        val jaminanPart  = priceJaminan.ifEmpty { "0" }.toRequestBody("text/plain".toMediaTypeOrNull())
                        val dendaPart    = priceDenda.ifEmpty { "0" }.toRequestBody("text/plain".toMediaTypeOrNull())
                        val stokPart     = stock.ifEmpty { "1" }.toRequestBody("text/plain".toMediaTypeOrNull())
                        val locPart      = location.toRequestBody("text/plain".toMediaTypeOrNull())
                        val statusPart   = "tersedia".toRequestBody("text/plain".toMediaTypeOrNull())

                        // Gunakan null jika kosong agar server tidak menerima string kosong
                        val waPart       = if (whatsApp.isNotBlank()) whatsApp.toRequestBody("text/plain".toMediaTypeOrNull()) else null
                        val descPart     = if (description.isNotBlank()) description.toRequestBody("text/plain".toMediaTypeOrNull()) else null
                        val addInfoPart  = if (additionalInformation.isNotBlank()) additionalInformation.toRequestBody("text/plain".toMediaTypeOrNull()) else null
                        
                        val startBackend = DateUtils.formatDateForBackend(dateStart)
                        val endBackend   = DateUtils.formatDateForBackend(dateEnd)
                        val startPart    = if (startBackend.isNotBlank()) startBackend.toRequestBody("text/plain".toMediaTypeOrNull()) else null
                        val endPart      = if (endBackend.isNotBlank()) endBackend.toRequestBody("text/plain".toMediaTypeOrNull()) else null
                        
                        Log.d("EditItem", "Saving: Start=$startBackend, End=$endBackend")

                        fun prepareImagePart(uri: Uri?, fieldName: String): MultipartBody.Part? {
                            if (uri == null) return null
                            return try {
                                // Kurangi ukuran ke 1024KB (1MB) agar tidak timeout saat upload banyak foto
                                val file = ImageUtils.compressImage(context, uri, 1024) ?: return null
                                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                                MultipartBody.Part.createFormData(fieldName, file.name, requestFile)
                            } catch (e: Exception) {
                                Log.e("EditItem", "Gagal memproses $fieldName: ${e.message}")
                                null
                            }
                        }

                        val partMain = prepareImagePart(imageUri, "foto_barang")
                        val part1    = prepareImagePart(angle1Uri, "fotoproduk1")
                        val part2    = prepareImagePart(angle2Uri, "fotoproduk2")
                        val part3    = prepareImagePart(angle3Uri, "fotoproduk3")
                        val part4    = prepareImagePart(angle4Uri, "fotoproduk4")

                        viewModel.updateKatalog(
                            token = token,
                            id = itemId,
                            kategoriId = catPart,
                            namaBarang = namePart,
                            deskripsi = descPart,
                            hargaSewa = sewaPart,
                            hargaJaminan = jaminanPart,
                            hargaDendaPerjam = dendaPart,
                            stok = stokPart,
                            lokasi = locPart,
                            whatsapp = waPart,
                            tanggalMulai = startPart,
                            tanggalAkhir = endPart,
                            additionalInformation = addInfoPart,
                            fotoUtama = partMain,
                            fotoAngle1 = part1,
                            fotoAngle2 = part2,
                            fotoAngle3 = part3,
                            fotoAngle4 = part4,
                            status = statusPart
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 50.dp),
                    shape   = RoundedCornerShape(8.dp),
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = AddItemAccent,
                        contentColor   = White
                    ),
                    enabled = !isSaving && isFormValid
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text        = "SAVE CHANGES",
                            fontFamily  = FontFamily.Default,
                            fontWeight  = FontWeight.ExtraBold,
                            fontSize    = 14.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            if (!isFormValid) {
                Text(
                    text     = "Lengkapi field yang wajib diisi (Nama, Harga Sewa, Lokasi)",
                    color    = MaterialTheme.colorScheme.error,
                    style    = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ══════════════════════════════════════
// UPLOAD BOX MAIN EDIT
// (sama dengan UploadBoxMain tapi support existing URL)
// ══════════════════════════════════════
@Composable
fun UploadBoxMainEdit(
    uri: Uri?,
    existingUrl: String,
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
        when {
            uri != null -> {
                AsyncImage(
                    model          = uri,
                    contentDescription = null,
                    contentScale   = ContentScale.Crop,
                    modifier       = Modifier.fillMaxSize()
                )
            }
            existingUrl.isNotEmpty() -> {
                AsyncImage(
                    model          = CatalogData.EMPTY.getFullUrl(existingUrl),
                    contentDescription = null,
                    contentScale   = ContentScale.Crop,
                    modifier       = Modifier.fillMaxSize()
                )
            }
            else -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Outlined.CameraAlt,
                        contentDescription = null,
                        tint     = Color(0xFF1E1E1E),
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text       = "Upload Foto/Image",
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 14.sp,
                        color      = Color(0xFF484848)
                    )
                }
            }
        }

        // Overlay edit icon jika sudah ada foto
        if (uri != null || existingUrl.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(White.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Edit,
                    contentDescription = "Ganti foto",
                    tint     = AddItemAccent,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun UploadBoxSmallEdit(
    uri: Uri?,
    existingUrl: String,
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
        when {
            uri != null -> {
                AsyncImage(
                    model = uri,
                    contentDescription = label,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            existingUrl.isNotEmpty() -> {
                AsyncImage(
                    model = CatalogData.EMPTY.getFullUrl(existingUrl),
                    contentDescription = label,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.CameraAlt,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(label, fontSize = 10.sp, color = TextMuted)
                }
            }
        }

        if (uri != null || existingUrl.isNotEmpty()) {
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
        }
    }
}
