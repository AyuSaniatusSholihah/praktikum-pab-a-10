package com.l0124005.sewain_rpl.ui.theme.katalog

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import coil.compose.AsyncImage
import com.l0124005.sewain_rpl.network.ApiClient
import com.l0124005.sewain_rpl.network.KategoriListResponse
import com.l0124005.sewain_rpl.utils.Resource
import com.l0124005.sewain_rpl.viewmodel.KatalogViewModel
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
    val crudResult by viewModel.crudResult.observeAsState()
    val kategoriResult by viewModel.kategori.observeAsState()

    var itemName by remember { mutableStateOf("") }
    var categoryId by remember { mutableIntStateOf(1) }
    var categoryName by remember { mutableStateOf("Pilih Kategori") }
    var priceSewa by remember { mutableStateOf("") }
    var priceJaminan by remember { mutableStateOf("") }
    var priceDenda by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var additionalInfo by remember { mutableStateOf("") }
    var stock by remember { mutableIntStateOf(1) }
    var location by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var existingImageUrl by remember { mutableStateOf("") }

    var showCategorySheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.getKategori()
        viewModel.getMyKatalogDetail(token, itemId)
    }

    LaunchedEffect(itemDetailState, kategoriResult) {
        if (itemDetailState is Resource.Success) {
            val data = itemDetailState?.data?.data
            data?.let {
                itemName = it.nama_barang
                categoryId = it.kategori_id
                priceSewa = it.harga_sewa.toInt().toString()
                priceJaminan = it.harga_jaminan.toInt().toString()
                priceDenda = it.harga_denda_perjam.toInt().toString()
                description = it.deskripsi ?: ""
                additionalInfo = it.additional_information ?: ""
                stock = it.stok
                location = it.lokasi
                existingImageUrl = it.foto_barang ?: ""

                // Cari nama kategori berdasarkan id
                kategoriResult?.let { res ->
                    if (res is Resource.Success) {
                        val cats = res.data?.data ?: emptyList()
                        categoryName = cats.find { c -> c.id == it.kategori_id }?.nama_kategori ?: "Pilih Kategori"
                    }
                }
            }
        }
    }

    LaunchedEffect(crudResult) {
        if (crudResult is Resource.Success) {
            onSuccess()
        }
    }

    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        imageUri = it
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Black)
                }
                Text(
                    text = "Edit Katalog",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Black,
                    fontFamily = Volkhov,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        },
        containerColor = White
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // -- Foto Barang --
            SewainFormLabel(label = "Upload Foto Barang")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(UploadBg)
                    .border(1.dp, UploadBorder, RoundedCornerShape(14.dp))
                    .clickable { imageLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (existingImageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = "${ApiClient.IMAGE_BASE_URL}$existingImageUrl",
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.CameraAlt,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Ganti Foto", color = Primary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            SewainFormField(
                label = "Nama Barang",
                value = itemName,
                onValueChange = { itemName = it },
                placeholder = "Masukkan nama barang"
            )

            // Category Selector
            Column {
                SewainFormLabel(label = "Kategori")
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(FormBg)
                        .border(1.dp, BorderGray, RoundedCornerShape(6.dp))
                        .clickable { showCategorySheet = true }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = categoryName,
                            fontSize = 13.sp,
                            color = if (categoryName == "Pilih Kategori") TextMuted else TextDark
                        )
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = TextMuted)
                    }
                }
                Spacer(Modifier.height(14.dp))
            }

            SewainFormField(
                label = "Harga Sewa (per hari)",
                value = priceSewa,
                onValueChange = { priceSewa = it },
                placeholder = "Contoh: 50000",
                keyboardType = KeyboardType.Number
            )

            SewainFormField(
                label = "Harga Jaminan",
                value = priceJaminan,
                onValueChange = { priceJaminan = it },
                placeholder = "Contoh: 100000",
                keyboardType = KeyboardType.Number
            )

            SewainFormField(
                label = "Denda per Jam",
                value = priceDenda,
                onValueChange = { priceDenda = it },
                placeholder = "Contoh: 5000",
                keyboardType = KeyboardType.Number
            )

            Column {
                SewainFormLabel(label = "Stok Barang")
                Spacer(Modifier.height(8.dp))
                StockQtyStepper(
                    qty = stock,
                    onMinus = { if (stock > 1) stock-- },
                    onPlus = { stock++ }
                )
                Spacer(Modifier.height(14.dp))
            }

            SewainFormField(
                label = "Lokasi",
                value = location,
                onValueChange = { location = it },
                placeholder = "Contoh: Bandung, Jawa Barat"
            )

            SewainFormField(
                label = "Deskripsi",
                value = description,
                onValueChange = { description = it },
                placeholder = "Jelaskan kondisi barang...",
                singleLine = false,
                minLines = 3
            )

            SewainFormField(
                label = "Informasi Tambahan",
                value = additionalInfo,
                onValueChange = { additionalInfo = it },
                placeholder = "Aturan sewa, kelengkapan, dll...",
                singleLine = false,
                minLines = 2
            )

            Button(
                onClick = {
                    val namePart = itemName.toRequestBody("text/plain".toMediaTypeOrNull())
                    val catPart = categoryId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    val sewaPart = priceSewa.toRequestBody("text/plain".toMediaTypeOrNull())
                    val jaminanPart = priceJaminan.toRequestBody("text/plain".toMediaTypeOrNull())
                    val dendaPart = priceDenda.toRequestBody("text/plain".toMediaTypeOrNull())
                    val stokPart = stock.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    val locPart = location.toRequestBody("text/plain".toMediaTypeOrNull())
                    val descPart = description.toRequestBody("text/plain".toMediaTypeOrNull())
                    val addInfoPart = additionalInfo.toRequestBody("text/plain".toMediaTypeOrNull())

                    var imagePart: MultipartBody.Part? = null
                    imageUri?.let { uri ->
                        val file = File(context.cacheDir, "temp_image_edit.jpg")
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            file.outputStream().use { output -> input.copyTo(output) }
                        }
                        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                        imagePart = MultipartBody.Part.createFormData("foto_barang", file.name, requestFile)
                    }

                    viewModel.updateKatalog(
                        token, itemId, catPart, namePart, descPart, sewaPart, 
                        jaminanPart, dendaPart, stokPart, locPart, imagePart, 
                        additionalInformation = addInfoPart
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                enabled = crudResult !is Resource.Loading
            ) {
                if (crudResult is Resource.Loading) {
                    CircularProgressIndicator(color = White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        "Simpan Perubahan",
                        fontFamily = Volkhov,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showCategorySheet) {
        val categoriesList = if (kategoriResult is Resource.Success) {
            kategoriResult?.data?.data?.map { it.nama_kategori } ?: emptyList()
        } else {
            emptyList()
        }

        CategoryBottomSheet(
            selectedCategory = categoryName,
            categoriesList = categoriesList,
            onSelect = { selected ->
                categoryName = selected
                if (kategoriResult is Resource.Success) {
                    categoryId = kategoriResult?.data?.data?.find { it.nama_kategori == selected }?.id ?: categoryId
                }
                showCategorySheet = false
            },
            onDismiss = { showCategorySheet = false }
        )
    }
}

