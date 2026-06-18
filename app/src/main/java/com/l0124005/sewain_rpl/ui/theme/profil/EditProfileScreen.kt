package com.l0124005.sewain_rpl.ui.theme.profil

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.l0124005.sewain_rpl.network.ApiClient
import com.l0124005.sewain_rpl.utils.Resource
import com.l0124005.sewain_rpl.viewmodel.ProfileViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

private val DarkNavy = Color(0xFF1F2A33)
private val InputBlue = Color(0xFF8AA9BD)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: ProfileViewModel,
    token: String,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val profileState by viewModel.profile.observeAsState()
    
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var alamat by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    
    var hasInitialData by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    LaunchedEffect(profileState) {
        if (profileState is Resource.Success && !hasInitialData) {
            val user = profileState?.data?.data
            if (user != null) {
                name = user.name
                username = user.username ?: ""
                phoneNumber = user.phone_number ?: ""
                alamat = user.alamat ?: ""
                hasInitialData = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkNavy)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Picture Section
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color.LightGray)
                            .border(2.dp, DarkNavy, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageUri != null) {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            val user = (profileState as? Resource.Success)?.data?.data
                            if (user?.foto_profil != null) {
                                AsyncImage(
                                    model = "${ApiClient.IMAGE_BASE_URL}profiles/${user.foto_profil}",
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Default.Person, null, modifier = Modifier.size(60.dp), tint = DarkNavy)
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(InputBlue)
                            .border(1.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(16.dp), tint = DarkNavy)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                EditTextField("Name", name) { name = it }
                EditTextField("Username", username) { username = it }
                EditTextField("Phone Number", phoneNumber) { phoneNumber = it }
                EditTextField("Address", alamat, isSingleLine = false) { alamat = it }

                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = {
                        val namePart = name.toRequestBody("text/plain".toMediaTypeOrNull())
                        val usernamePart = username.toRequestBody("text/plain".toMediaTypeOrNull())
                        val phonePart = phoneNumber.toRequestBody("text/plain".toMediaTypeOrNull())
                        val alamatPart = alamat.toRequestBody("text/plain".toMediaTypeOrNull())

                        var imagePart: MultipartBody.Part? = null
                        imageUri?.let { uri ->
                            val file = uriToFile(uri, context)
                            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                            imagePart = MultipartBody.Part.createFormData("foto_profil", file.name, requestFile)
                        }

                        viewModel.updateProfile(token, namePart, usernamePart, phonePart, alamatPart, imagePart)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkNavy),
                    shape = RoundedCornerShape(25.dp),
                    enabled = profileState !is Resource.Loading
                ) {
                    if (profileState is Resource.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("UPDATE PROFILE", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                // Handle update response
                LaunchedEffect(profileState) {
                    if (profileState is Resource.Success && hasInitialData) {
                        Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                        onSuccess()
                    } else if (profileState is Resource.Error) {
                        Toast.makeText(context, profileState?.message ?: "Update failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}

@Composable
private fun EditTextField(
    label: String,
    value: String,
    isSingleLine: Boolean = true,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(label, color = DarkNavy, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = isSingleLine,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DarkNavy,
                unfocusedBorderColor = InputBlue,
                cursorColor = DarkNavy
            )
        )
    }
}

private fun uriToFile(uri: Uri, context: android.content.Context): File {
    val contentResolver = context.contentResolver
    val myFile = File.createTempFile("temp_image", ".jpg", context.cacheDir)
    val inputStream = contentResolver.openInputStream(uri)
    val outputStream = FileOutputStream(myFile)
    val buffer = ByteArray(1024)
    var length: Int
    while (inputStream?.read(buffer).also { length = it ?: -1 } != -1) {
        outputStream.write(buffer, 0, length)
    }
    outputStream.close()
    inputStream?.close()
    return myFile
}
