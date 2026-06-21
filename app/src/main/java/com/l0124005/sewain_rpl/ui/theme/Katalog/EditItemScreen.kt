package com.l0124005.sewain_rpl.ui.theme.katalog

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.l0124005.sewain_rpl.network.ApiClient
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
    val kategoriResult by viewModel.kategori.observeAsState()
    val crudResult by viewModel.crudResult.observeAsState()

    var itemName by remember { mutableStateOf("") }
    var categoryId by remember { mutableIntStateOf(1) }
    var categoryName by remember { mutableStateOf("") }
    var priceSewa by remember { mutableStateOf("") }
    var priceJaminan by remember { mutableStateOf("") }
    var priceDenda by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var additionalInformation by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("1") }
    var location by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("tersedia") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var existingImageUrl by remember { mutableStateOf("") }
    var showCategorySheet by remember { mutableStateOf(false) }

    // Snackbar untuk menampilkan pesan error / sukses
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.getMyKatalogDetail(token, itemId)
        viewModel.getKategori()
    }

    // Isi form dengan data dari server saat detail berhasil dimuat
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
                additionalInformation = it.additional_information ?: ""
                stock = it.stok.toString()
                location = it.lokasi
                status = it.status
                existingImageUrl = it.foto_barang ?: ""

                if (kategoriResult is Resource.Success) {
                    val cats = (kategoriResult as Resource.Success).data?.data
                    categoryName = cats?.find { c -> c.id == it.kategori_id }?.nama_kategori ?: ""
                }
            }
        }
    }

    // Observer hasil CRUD: tampilkan snackbar error atau navigasi jika sukses
    LaunchedEffect(crudResult) {
        when (val result = crudResult) {
            is Resource.Success -> {
                snackbarHostState.showSnackbar("Berhasil menyimpan perubahan!")
                onSuccess()
            }
            is Resource.Error -> {
                val msg = result.message ?: "Terjadi kesalahan, coba lagi."
                snackbarHostState.showSnackbar("Gagal menyimpan: $msg")
            }
            else -> {}
        }
    }

    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        imageUri = it
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Katalog") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // Tampilkan error saat fetch detail
            if (itemDetailState is Resource.Error) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Gagal memuat data: ${(itemDetailState as Resource.Error).message}",
                        color = Color(0xFFB71C1C),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Loading saat memuat detail
            if (itemDetailState is Resource.Loading) {
                Box(modifier = Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            // Foto Barang
            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(8.dp)).background(Color.LightGray).clickable { imageLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(model = imageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else if (existingImageUrl.isNotEmpty()) {
                    AsyncImage(model = "${ApiClient.IMAGE_BASE_URL}$existingImageUrl", contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.CameraAlt, contentDescription = null, modifier = Modifier.size(48.dp))
                        Text("Ketuk untuk ganti foto", color = Color.DarkGray)
                    }
                }
            }

            OutlinedTextField(value = itemName, onValueChange = { itemName = it }, label = { Text("Nama Barang") }, modifier = Modifier.fillMaxWidth(), isError = itemName.isBlank())
            if (itemName.isBlank()) {
                Text("Nama barang wajib diisi", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            
            // Kategori
            OutlinedTextField(
                value = categoryName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Kategori") },
                trailingIcon = { IconButton(onClick = { showCategorySheet = true }) { Icon(Icons.Default.KeyboardArrowDown, null) } },
                modifier = Modifier.fillMaxWidth().clickable { showCategorySheet = true },
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color.Black,
                    disabledBorderColor = Color.Gray,
                    disabledLabelColor = Color.Gray,
                    disabledTrailingIconColor = Color.Gray
                )
            )

            OutlinedTextField(value = priceSewa, onValueChange = { priceSewa = it.filter { c -> c.isDigit() } }, label = { Text("Harga Sewa / Hari") }, modifier = Modifier.fillMaxWidth(), isError = priceSewa.isBlank())
            OutlinedTextField(value = priceJaminan, onValueChange = { priceJaminan = it.filter { c -> c.isDigit() } }, label = { Text("Harga Jaminan") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = priceDenda, onValueChange = { priceDenda = it.filter { c -> c.isDigit() } }, label = { Text("Denda / Jam") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = stock, onValueChange = { stock = it.filter { c -> c.isDigit() } }, label = { Text("Stok") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Lokasi") }, modifier = Modifier.fillMaxWidth(), isError = location.isBlank())
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Deskripsi") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            OutlinedTextField(value = additionalInformation, onValueChange = { additionalInformation = it }, label = { Text("Informasi Tambahan") }, modifier = Modifier.fillMaxWidth(), minLines = 3)

            // Status Dropdown
            var expanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = status,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Status") },
                    trailingIcon = { IconButton(onClick = { expanded = true }) { Icon(Icons.Default.KeyboardArrowDown, null) } },
                    modifier = Modifier.fillMaxWidth()
                )
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(text = { Text("Tersedia") }, onClick = { status = "tersedia"; expanded = false })
                    DropdownMenuItem(text = { Text("Disewa") }, onClick = { status = "disewa"; expanded = false })
                    DropdownMenuItem(text = { Text("Habis") }, onClick = { status = "habis"; expanded = false })
                }
            }

            // Tombol Simpan
            val isFormValid = itemName.isNotBlank() && priceSewa.isNotBlank() && location.isNotBlank()
            val isSaving = crudResult is Resource.Loading

            Button(
                onClick = {
                    // Validasi input sebelum mengirim ke server
                    if (!isFormValid) {
                        return@Button
                    }

                    // Reset state lama agar tidak memicu onSuccess() dari hasil sebelumnya
                    viewModel.resetStates()

                    val namePart = itemName.toRequestBody("text/plain".toMediaTypeOrNull())
                    val catPart = categoryId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    
                    val cleanSewa = priceSewa.ifEmpty { "0" }
                    val cleanJaminan = priceJaminan.ifEmpty { "0" }
                    val cleanDenda = priceDenda.ifEmpty { "0" }
                    val cleanStock = stock.ifEmpty { "1" }

                    val sewaPart = cleanSewa.toRequestBody("text/plain".toMediaTypeOrNull())
                    val jaminanPart = cleanJaminan.toRequestBody("text/plain".toMediaTypeOrNull())
                    val dendaPart = cleanDenda.toRequestBody("text/plain".toMediaTypeOrNull())
                    val stokPart = cleanStock.toRequestBody("text/plain".toMediaTypeOrNull())
                    val locPart = location.toRequestBody("text/plain".toMediaTypeOrNull())
                    val descPart = description.toRequestBody("text/plain".toMediaTypeOrNull())
                    val addInfoPart = additionalInformation.toRequestBody("text/plain".toMediaTypeOrNull())
                    val statusPart = status.toRequestBody("text/plain".toMediaTypeOrNull())

                    var imagePart: MultipartBody.Part? = null
                    imageUri?.let { uri ->
                        val file = File(context.cacheDir, "temp_image_edit.jpg")
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            file.outputStream().use { output -> input.copyTo(output) }
                        }
                        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                        imagePart = MultipartBody.Part.createFormData("foto_barang", file.name, requestFile)
                    }

                    viewModel.updateKatalog(token, itemId, catPart, namePart, descPart, sewaPart, jaminanPart, dendaPart, stokPart, locPart, addInfoPart, imagePart, statusPart)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving && isFormValid
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Simpan Perubahan")
                }
            }

            // Pesan jika form belum valid
            if (!isFormValid) {
                Text(
                    text = "Lengkapi field yang wajib diisi (Nama, Harga Sewa, Lokasi)",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }

    if (showCategorySheet) {
        val availableCategories = (kategoriResult as? Resource.Success)?.data?.data ?: emptyList()
        CategoryBottomSheetDynamic(
            categories = availableCategories,
            selectedCategoryId = categoryId.toString(),
            onSelect = { id, name ->
                categoryId = id
                categoryName = name
                showCategorySheet = false
            },
            onDismiss = { showCategorySheet = false }
        )
    }
}
