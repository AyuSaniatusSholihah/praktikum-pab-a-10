package com.l0124005.sewain_rpl.ui.theme.katalog

import android.net.Uri
import android.widget.Toast
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
    val extractedKategori by viewModel.extractedKategori.observeAsState(emptyList())
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

    LaunchedEffect(Unit) {
        viewModel.getMyKatalogDetail(token, itemId)
        viewModel.getKategori()
        viewModel.getKatalogPublik()
    }

    LaunchedEffect(itemDetailState, kategoriResult, extractedKategori) {
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

                val cats = if (kategoriResult is Resource.Success) {
                    (kategoriResult as Resource.Success).data?.data
                } else {
                    extractedKategori
                }
                categoryName = cats?.find { c -> c.id == it.kategori_id }?.nama_kategori ?: ""
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
            TopAppBar(
                title = { Text("Edit Katalog") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(8.dp)).background(Color.LightGray).clickable { imageLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(model = imageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else if (existingImageUrl.isNotEmpty()) {
                    AsyncImage(model = "${ApiClient.IMAGE_BASE_URL}$existingImageUrl", contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Outlined.CameraAlt, contentDescription = null, modifier = Modifier.size(48.dp))
                }
            }

            OutlinedTextField(value = itemName, onValueChange = { itemName = it }, label = { Text("Nama Barang") }, modifier = Modifier.fillMaxWidth())
            
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

            OutlinedTextField(value = priceSewa, onValueChange = { priceSewa = it }, label = { Text("Harga Sewa / Hari") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = priceJaminan, onValueChange = { priceJaminan = it }, label = { Text("Harga Jaminan") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = priceDenda, onValueChange = { priceDenda = it }, label = { Text("Denda / Jam") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Stok") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Lokasi") }, modifier = Modifier.fillMaxWidth())
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

            Button(
                onClick = {
                    val namePart = itemName.toRequestBody("text/plain".toMediaTypeOrNull())
                    val catPart = categoryId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    
                    val cleanSewa = priceSewa.filter { it.isDigit() }.ifEmpty { "0" }
                    val cleanJaminan = priceJaminan.filter { it.isDigit() }.ifEmpty { "0" }
                    val cleanDenda = priceDenda.filter { it.isDigit() }.ifEmpty { "0" }
                    val cleanStock = stock.filter { it.isDigit() }.ifEmpty { "1" }

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
                enabled = crudResult !is Resource.Loading
            ) {
                if (crudResult is Resource.Loading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("Simpan Perubahan")
            }
        }
    }

    if (showCategorySheet) {
        val availableCategories = if (kategoriResult is Resource.Success) {
            kategoriResult?.data?.data ?: emptyList()
        } else {
            extractedKategori
        }

        if (availableCategories.isNotEmpty()) {
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
        } else {
            if (kategoriResult !is Resource.Loading) {
                Toast.makeText(context, "Gagal memuat kategori. Pastikan Anda terhubung ke internet.", Toast.LENGTH_SHORT).show()
                showCategorySheet = false
            }
        }
    }
}
