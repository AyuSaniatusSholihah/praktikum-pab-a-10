package com.l0124005.sewain_rpl.ui.theme.katalog

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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

    var itemName by remember { mutableStateOf("") }
    var categoryId by remember { mutableStateOf(1) }
    var priceSewa by remember { mutableStateOf("") }
    var priceJaminan by remember { mutableStateOf("") }
    var priceDenda by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("1") }
    var location by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var existingImageUrl by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.getMyKatalogDetail(token, itemId)
    }

    LaunchedEffect(itemDetailState) {
        if (itemDetailState is Resource.Success) {
            val data = itemDetailState?.data?.data
            data?.let {
                itemName = it.nama_barang
                categoryId = it.kategori_id
                priceSewa = it.harga_sewa.toInt().toString()
                priceJaminan = it.harga_jaminan.toInt().toString()
                priceDenda = it.harga_denda_perjam.toInt().toString()
                description = it.deskripsi ?: ""
                stock = it.stok.toString()
                location = it.lokasi
                existingImageUrl = it.foto_barang ?: ""
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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                    AsyncImage(model = "${ApiClient.IMAGE_BASE_URL}products/$existingImageUrl", contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Outlined.CameraAlt, contentDescription = null, modifier = Modifier.size(48.dp))
                }
            }

            OutlinedTextField(value = itemName, onValueChange = { itemName = it }, label = { Text("Nama Barang") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = priceSewa, onValueChange = { priceSewa = it }, label = { Text("Harga Sewa / Hari") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = priceJaminan, onValueChange = { priceJaminan = it }, label = { Text("Harga Jaminan") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = priceDenda, onValueChange = { priceDenda = it }, label = { Text("Denda / Jam") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Stok") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Lokasi") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Deskripsi") }, modifier = Modifier.fillMaxWidth(), minLines = 3)

            Button(
                onClick = {
                    val namePart = itemName.toRequestBody("text/plain".toMediaTypeOrNull())
                    val catPart = categoryId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    val sewaPart = priceSewa.toRequestBody("text/plain".toMediaTypeOrNull())
                    val jaminanPart = priceJaminan.toRequestBody("text/plain".toMediaTypeOrNull())
                    val dendaPart = priceDenda.toRequestBody("text/plain".toMediaTypeOrNull())
                    val stokPart = stock.toRequestBody("text/plain".toMediaTypeOrNull())
                    val locPart = location.toRequestBody("text/plain".toMediaTypeOrNull())
                    val descPart = description.toRequestBody("text/plain".toMediaTypeOrNull())

                    var imagePart: MultipartBody.Part? = null
                    imageUri?.let { uri ->
                        val file = File(context.cacheDir, "temp_image_edit.jpg")
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            file.outputStream().use { output -> input.copyTo(output) }
                        }
                        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                        imagePart = MultipartBody.Part.createFormData("foto_barang", file.name, requestFile)
                    }

                    viewModel.updateKatalog(token, itemId, catPart, namePart, descPart, sewaPart, jaminanPart, dendaPart, stokPart, locPart, imagePart)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = crudResult !is Resource.Loading
            ) {
                if (crudResult is Resource.Loading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("Simpan Perubahan")
            }
        }
    }
}
