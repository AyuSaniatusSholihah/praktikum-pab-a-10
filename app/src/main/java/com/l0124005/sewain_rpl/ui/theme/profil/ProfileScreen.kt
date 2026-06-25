package com.l0124005.sewain_rpl.ui.theme.profil

import android.app.DatePickerDialog
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.l0124005.sewain_rpl.network.ApiClient
import com.l0124005.sewain_rpl.network.OwnerDashboardResponse
import com.l0124005.sewain_rpl.network.ProfileResponse
import com.l0124005.sewain_rpl.network.TransaksiListResponse
import com.l0124005.sewain_rpl.network.UserData
import com.l0124005.sewain_rpl.ui.theme.AbrilFatfaceFont
import com.l0124005.sewain_rpl.ui.theme.MontaguSlabFont
import com.l0124005.sewain_rpl.ui.theme.SewainTopBar
import com.l0124005.sewain_rpl.ui.theme.Sewain_rplTheme
import com.l0124005.sewain_rpl.ui.theme.VolkhovFont
import com.l0124005.sewain_rpl.utils.CurrencyUtils
import com.l0124005.sewain_rpl.utils.DateUtils
import com.l0124005.sewain_rpl.utils.ImageUtils
import com.l0124005.sewain_rpl.utils.RentalStatus
import com.l0124005.sewain_rpl.utils.Resource
import com.l0124005.sewain_rpl.viewmodel.ProfileViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar
import java.util.Locale

// ── Warna tema ──
val DarkNavy   = Color(0xFF21394F) // Menggunakan NavyPrimary
val MidBlue    = Color(0xFF4D6674) // Menggunakan BluePrimary
val InputBlue  = Color(0xFFB2C9DD)
val TextLight  = Color(0xFFE6E8EF)
val TextMuted  = Color(0xFFA1A2A7)
val AccentBlue = Color(0xFF6A87A1) // Menggunakan BluePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    token: String,
    onLogout: () -> Unit,
    onEditProfile: () -> Unit,
    onMyRentalClick: () -> Unit,
    onRentalOwnerClick: () -> Unit,
    onMyWalletClick: () -> Unit,
    onSettingsClick: () -> Unit = {}
) {
    val profileState by viewModel.profile.observeAsState()
    val updateState  by viewModel.updateState.observeAsState()
    val ownerDashboardState by viewModel.ownerDashboard.observeAsState()
    val transaksiListState by viewModel.transaksiList.observeAsState()
    val context = LocalContext.current

    // ── State buat drawer ──
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { 
        viewModel.getProfile(token)
        viewModel.getStats(token)
    }

    // Pantau hasil UPDATE (bukan profile load)
    LaunchedEffect(updateState) {
        when {
            isSaving && updateState is Resource.Success -> {
                Toast.makeText(context, "Profil berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                isSaving = false
                viewModel.resetUpdateState()
                viewModel.getProfile(token)   // refresh tampilan dengan data terbaru
            }
            isSaving && updateState is Resource.Error -> {
                Toast.makeText(context, "Gagal menyimpan: ${updateState?.message}", Toast.LENGTH_LONG).show()
                isSaving = false
                viewModel.resetUpdateState()
            }
        }
    }

    // ── Ambil data user buat ditampilin di drawer ──
    val user = (profileState as? Resource.Success<ProfileResponse>)?.data?.data
    val userName = user?.name ?: "User"
    val userPhoto = user?.foto_profil

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ProfileDrawerContent(
                userName = userName,
                userPhoto = userPhoto,
                currentScreen = "Profile",
                onProfileClick = { scope.launch { drawerState.close() } },
                onMyRentalsClick = {
                    scope.launch { drawerState.close() }
                    onMyRentalClick()
                },
                onRentalsOwnerClick = {
                    scope.launch { drawerState.close() }
                    onRentalOwnerClick()
                },
                onMyWalletClick = {
                    scope.launch { drawerState.close() }
                    onMyWalletClick()
                },
                onLogoutClick = {
                    scope.launch { drawerState.close() }
                    onLogout()
                },
                onSettingsClick = {
                    scope.launch { drawerState.close() }
                    onSettingsClick()
                }
            )
        }
    ) {
        // ── Konten utama ──
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            SewainTopBar(
                navigationIcon = Icons.Default.Menu,
                onNavigationClick = { scope.launch { drawerState.open() } }
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .background(Color.White)
            ) {
                when (profileState) {
                    is Resource.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(top = 100.dp),
                            color = AccentBlue
                        )
                    }
                    is Resource.Success -> {
                        val user = (profileState as Resource.Success<ProfileResponse>).data?.data
                        
                        // Refine stats: count all transactions except those canceled or just pending payment
                        val totalDisewa = (transaksiListState as? Resource.Success<TransaksiListResponse>)?.data?.data?.count { 
                            val status = RentalStatus.fromTransaksi(it)
                            status == RentalStatus.ACTIVE || status == RentalStatus.RETURN || status == RentalStatus.COMPLETED
                        } ?: 0
                        
                        val jumlahKatalog = (ownerDashboardState as? Resource.Success<OwnerDashboardResponse>)?.data?.data?.total_barang ?: 0
                        
                        if (user != null) {
                            ProfileContent(
                                user = user,
                                totalDisewa = totalDisewa,
                                jumlahKatalog = jumlahKatalog,
                                isSaving = isSaving,
                                onLogout = onLogout,
                                onSave = { updatedUser, imageUri ->
                                    val formattedToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
                                    
                                    // Menggunakan text/plain untuk field teks di multipart
                                    val namePart = updatedUser.name.toRequestBody("text/plain".toMediaTypeOrNull())
                                    val usernamePart = updatedUser.username?.toRequestBody("text/plain".toMediaTypeOrNull())
                                    val phonePart = updatedUser.phone_number?.toRequestBody("text/plain".toMediaTypeOrNull())
                                    val alamatPart = updatedUser.alamat?.toRequestBody("text/plain".toMediaTypeOrNull())
                                    
                                    // Kirim tanggal_lahir jika tidak kosong. 
                                    // Pastikan format yyyy-MM-dd (sudah dihandle oleh DatePickerDialog)
                                    val tanggalLahirPart = if (!updatedUser.tanggal_lahir.isNullOrBlank()) {
                                        updatedUser.tanggal_lahir.toRequestBody("text/plain".toMediaTypeOrNull())
                                    } else null

                                    // Gunakan nilai full "Laki-laki" / "Perempuan" sesuai dropdown
                                    // jika pemetaan ke "L"/"P" dianggap invalid oleh backend
                                    val genderValue = updatedUser.jenis_kelamin
                                    val jenisKelaminPart = if (!genderValue.isNullOrBlank()) {
                                        genderValue.toRequestBody("text/plain".toMediaTypeOrNull())
                                    } else null

                                    var imagePart: MultipartBody.Part? = null
                                    imageUri?.let { uri ->
                                        val file = ImageUtils.compressImage(context, uri)
                                        if (file != null) {
                                            val requestFile = file.readBytes().toRequestBody("image/*".toMediaTypeOrNull())
                                            imagePart = MultipartBody.Part.createFormData("foto_profil", file.name, requestFile)
                                        }
                                    }

                                    isSaving = true
                                    viewModel.updateProfile(formattedToken, namePart, usernamePart, phonePart, alamatPart, tanggalLahirPart, jenisKelaminPart, imagePart)
                                }
                            )
                        }
                    }
                    is Resource.Error -> {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(top = 100.dp)
                                .padding(horizontal = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Gagal memuat profil",
                                color = Color.Red,
                                fontFamily = VolkhovFont
                            )
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.getProfile(token) },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                            ) {
                                Text("Coba Lagi", color = Color.White)
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

// ── Komponen 1 item menu di drawer ──
@Composable
private fun DrawerMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean = false,
    tint: Color = Color.White,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(if (selected) AccentBlue.copy(alpha = 0.25f) else Color.Transparent)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(16.dp))
        Text(
            text = label,
            fontFamily = VolkhovFont,
            fontSize = 14.sp,
            color = tint
        )
    }
}

@Composable
internal fun ProfileContent(
    user: UserData,
    totalDisewa: Int = 0,
    jumlahKatalog: Int = 0,
    isSaving: Boolean = false,
    onLogout: () -> Unit,
    onSave: (UserData, Uri?) -> Unit
) {
    var name          by remember { mutableStateOf(user.name) }
    var phone         by remember { mutableStateOf(user.phone_number ?: "") }
    var tanggalLahir  by remember { mutableStateOf(user.tanggal_lahir ?: "") }
    var jenisKelamin  by remember { 
        mutableStateOf(
            when(user.jenis_kelamin) {
                "L" -> "Laki-laki"
                "P" -> "Perempuan"
                else -> user.jenis_kelamin ?: "Laki-laki"
            }
        )
    }
    var alamat        by remember { mutableStateOf(user.alamat ?: "") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    
    // Inisialisasi calendar dari tanggalLahir jika ada
    LaunchedEffect(user.tanggal_lahir) {
        if (!user.tanggal_lahir.isNullOrBlank()) {
            DateUtils.backendStringToDate(user.tanggal_lahir)?.let {
                calendar.time = it
            }
        }
    }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
            }
            tanggalLahir = DateUtils.dateToBackendString(selectedDate.time)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    // ── Panel luar warna #4D6674 ──
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MidBlue)
            .padding(20.dp)
    ) {
        // ── Mini header: foto kecil + nama ──
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = RentalStatus.buildPhotoUrl(user.foto_profil, user.name),
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(6.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(14.dp))
            Text(
                text = user.name,
                fontFamily = VolkhovFont,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White
            )
        }

        Spacer(Modifier.height(20.dp))

        // ── Inner card warna #21394F ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(DarkNavy)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Avatar besar + tombol edit ──
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier.clickable { launcher.launch("image/*") }
            ) {
                AsyncImage(
                    model = selectedImageUri ?: RentalStatus.buildPhotoUrl(user.foto_profil, user.name),
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.White, CircleShape),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MidBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = Color.White
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── Greeting ──
            Text(
                text = "Hello, ${user.name}!",
                fontFamily = AbrilFatfaceFont,
                fontSize = 22.sp,
                color = TextLight
            )
            Text(
                text = "Here is your quick overview",
                fontFamily = VolkhovFont,
                fontSize = 13.sp,
                color = TextMuted
            )

            Spacer(Modifier.height(28.dp))

            // ====================================================
            // KOLOM KIRI (.profile-left di web), urutan sama persis:
            // Name -> Email -> Password -> Nomor Telepon ->
            // Tanggal Lahir -> Jenis Kelamin -> Alamat
            // ====================================================
            ProfileField(
                label = "Name Account User",
                value = name
            ) { name = it }

            ProfileField(
                label = "Email",
                value = user.email,
                enabled = false
            ) {}

            ProfileField(
                label = "Password",
                value = "••••••••",
                enabled = false
            ) {}

            ProfileField(
                label = "Nomor Telepon",
                value = phone
            ) { phone = it }

            ProfileField(
                label = "Tanggal Lahir",
                value = tanggalLahir,
                enabled = true,
                readOnly = true,
                placeholder = "YYYY-MM-DD",
                onClick = { datePickerDialog.show() }
            ) { /* Diupdate via DatePickerDialog */ }

            // Jenis Kelamin = <select> di web -> dropdown di Compose
            ProfileGenderField(
                label = "Jenis Kelamin",
                value = jenisKelamin,
                onValueChange = { jenisKelamin = it }
            )

            ProfileField(
                label = "Alamat",
                value = alamat,
                multiline = true
            ) { alamat = it }

            Spacer(Modifier.height(24.dp))

            // ====================================================
            // KOLOM KANAN (.profile-right di web), urutan sama persis:
            // Saldo card -> Total Disewa -> Jumlah Katalog Barang -> SAVE
            // (Status Akun & Role dihapus -- tidak ada di web)
            // ====================================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = InputBlue)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Badge "+2.3%" -- sesuai .saldo-badge di web (background putih transparan, teks hijau gelap)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.5f))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "+2.3%",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = Color(0xFF1D4734)
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Saldo",
                                fontFamily = VolkhovFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = DarkNavy
                            )
                            Text(
                                text = "Rp ${CurrencyUtils.formatRupiah(user.saldo)}",
                                fontFamily = AbrilFatfaceFont,
                                fontSize = 22.sp,
                                color = Color.Black
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AttachMoney,
                                contentDescription = null,
                                tint = DarkNavy,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Total Disewa & Jumlah Katalog Barang -- sesuai .profile-stat di web
            StatRow(label = "Total Disewa", value = "$totalDisewa Kali")
            Spacer(Modifier.height(10.dp))
            StatRow(label = "Jumlah Katalog Barang", value = "$jumlahKatalog Barang")

            Spacer(Modifier.height(32.dp))

            // ── Tombol SAVE -- warna #4D6674 sesuai .btn-save-profile di web ──
            Button(
                onClick = {
                    if (!isSaving) {
                        onSave(
                            user.copy(
                                name = name,
                                phone_number = phone,
                                tanggal_lahir = tanggalLahir,
                                jenis_kelamin = jenisKelamin,
                                alamat = alamat
                            ),
                            selectedImageUri
                        )
                    }
                },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MidBlue,
                    disabledContainerColor = MidBlue.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Menyimpan...",
                        fontFamily = MontaguSlabFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "SAVE",
                        fontFamily = MontaguSlabFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            // Logout DIHAPUS dari sini. Di web, Logout ada di sidebar (.dash-logout),
            // bukan di dalam card profil. Pasang tombol logout di composable sidebar kamu,
            // panggil onLogout() di sana.
        }
    }
}

// ── Field dengan label Abril Fatface + input pill InputBlue ──
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileField(
    label: String,
    value: String,
    enabled: Boolean = true,
    multiline: Boolean = false,
    readOnly: Boolean = false,
    placeholder: String = "",
    onClick: (() -> Unit)? = null,
    onValueChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Text(
            text = label,
            fontFamily = AbrilFatfaceFont,
            fontSize = 18.sp,
            color = TextLight,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(6.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled && onClick == null,
            readOnly = readOnly || onClick != null,
            singleLine = !multiline,
            placeholder = { if (placeholder.isNotEmpty()) Text(placeholder, color = DarkNavy.copy(alpha = 0.5f)) },
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (multiline) Modifier.heightIn(min = 90.dp)
                    else Modifier.height(50.dp)
                )
                .clip(
                    if (multiline) RoundedCornerShape(14.dp)
                    else RoundedCornerShape(999.dp)
                )
                .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
            colors = TextFieldDefaults.colors(
                focusedContainerColor   = InputBlue,
                unfocusedContainerColor = InputBlue,
                disabledContainerColor  = InputBlue,
                focusedIndicatorColor   = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor  = Color.Transparent,
                focusedTextColor        = DarkNavy,
                unfocusedTextColor      = DarkNavy,
                disabledTextColor       = DarkNavy
            ),
            textStyle = TextStyle(
                fontFamily = MontaguSlabFont,
                fontSize = 14.sp,
                color = DarkNavy
            )
        )
    }
}

// ── Dropdown Jenis Kelamin -- versi Compose dari <select> di web ──
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileGenderField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("Laki-laki", "Perempuan")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontFamily = AbrilFatfaceFont,
            fontSize = 18.sp,
            color = TextLight,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(6.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(999.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor   = InputBlue,
                    unfocusedContainerColor = InputBlue,
                    focusedIndicatorColor   = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor        = DarkNavy,
                    unfocusedTextColor      = DarkNavy
                ),
                textStyle = TextStyle(
                    fontFamily = MontaguSlabFont,
                    fontSize = 14.sp,
                    color = DarkNavy
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, fontFamily = MontaguSlabFont) },
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

// ── Stat row dengan label Abril + value pill InputBlue ──
@Composable
private fun StatRow(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontFamily = AbrilFatfaceFont,
            fontSize = 18.sp,
            color = TextLight,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(40.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(InputBlue),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                fontFamily = MontaguSlabFont,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = DarkNavy
            )
        }
    }
}

fun uriToFile(context: android.content.Context, uri: Uri): File {
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

// ── Preview ──
@Preview(showBackground = true, widthDp = 390, heightDp = 2600)
@Composable
fun ProfileScreenPreview() {
    val dummyUser = UserData(
        id = 1,
        name = "Camping Groups Bandung",
        username = "campinggroups",
        email = "campinggroups.bandung@gmail.com",
        phone_number = "0853-9017-6483",
        alamat = "Jl. Ir. H. Juanda No. 50, Bandung",
        saldo = 1872000.0,
        foto_profil = null,
        is_banned = false,
        role = "user",
        email_verified_at = null,
        tanggal_lahir = "2000-01-01",
        jenis_kelamin = "Laki-laki",
        total_disewa = 5,
        jumlah_katalog = 10
    )

    Sewain_rplTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                SewainTopBar()
                ProfileContent(
                    user = dummyUser,
                    onLogout = {},
                    onSave = { _, _ -> }
                )
            }
        }
    }
}

// ── Preview khusus buat liat tampilan DRAWER ──
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, widthDp = 390, heightDp = 700, name = "Drawer Terbuka")
@Composable
fun ProfileDrawerPreview() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Open) // langsung kebuka
    val userName = "Camping Groups Bandung"

    Sewain_rplTheme {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = DarkNavy,
                    modifier = Modifier.width(260.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFB2B9B9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = userName,
                            fontFamily = VolkhovFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    Spacer(Modifier.height(8.dp))

                    DrawerMenuItem(icon = Icons.Default.Person, label = "Profile", selected = true) {}
                    DrawerMenuItem(icon = Icons.Default.List, label = "My Rentals") {}
                    DrawerMenuItem(icon = Icons.Default.Store, label = "Rentals Owner") {}
                    DrawerMenuItem(icon = Icons.Default.AccountBalanceWallet, label = "My Wallet") {}
                    DrawerMenuItem(icon = Icons.Default.Settings, label = "Settings") {}

                    Spacer(Modifier.weight(1f))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                    DrawerMenuItem(
                        icon = Icons.AutoMirrored.Filled.Logout,
                        label = "Log Out",
                        tint = Color(0xFFE57373)
                    ) {}

                    Spacer(Modifier.height(12.dp))
                }
            }
        ) {
            // Konten belakang drawer (boleh simple aja buat preview)
            Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkNavy)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        SewainTopBar()
                    }
                }
            }
        }
    }
}