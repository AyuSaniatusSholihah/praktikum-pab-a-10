package com.l0124005.sewain_rpl.ui.theme.profil

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.l0124005.sewain_rpl.network.ApiClient
import com.l0124005.sewain_rpl.network.UserData
import com.l0124005.sewain_rpl.network.ProfileResponse
import com.l0124005.sewain_rpl.ui.theme.katalog.formatRupiah
import com.l0124005.sewain_rpl.utils.Resource
import com.l0124005.sewain_rpl.viewmodel.ProfileViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

private val DarkNavy = Color(0xFF1F2A33)
private val InputBlue = Color(0xFF8AA9BD)

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    token: String,
    onLogout: () -> Unit,
    onEditProfile: () -> Unit,
    onMyRentalClick: () -> Unit,
    onRentalOwnerClick: () -> Unit,
    onTransaksiClick: () -> Unit,
    onRiwayatTransaksiClick: () -> Unit
) {
    val profileState by viewModel.profile.observeAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.getProfile(token)
    }

    LaunchedEffect(profileState) {
        if (profileState is Resource.Success && (profileState as Resource.Success<ProfileResponse>).data?.message == "Profile updated successfully") {
            Toast.makeText(context, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
            viewModel.resetStates()
            viewModel.getProfile(token)
        } else if (profileState is Resource.Error) {
            Toast.makeText(context, "Gagal update: ${profileState?.message}", Toast.LENGTH_SHORT).show()
            viewModel.resetStates()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        when (profileState) {
            is Resource.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = DarkNavy)
            }
            is Resource.Success -> {
                val user = profileState?.data?.data
                if (user != null) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        ProfileHeaderSection(
                            name = user.name,
                            foto = user.foto_profil,
                            onMyRentalClick = onMyRentalClick,
                            onRentalOwnerClick = onRentalOwnerClick,
                            onWalletClick = onTransaksiClick
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        ProfileFormSection(
                            user = user,
                            onLogout = onLogout,
                            onEditPhoto = onEditProfile,
                            onSave = { updatedUser ->
                                val namePart = updatedUser.name.toRequestBody("text/plain".toMediaTypeOrNull())
                                val usernamePart = updatedUser.username?.toRequestBody("text/plain".toMediaTypeOrNull())
                                val phonePart = updatedUser.phone_number?.toRequestBody("text/plain".toMediaTypeOrNull())
                                val alamatPart = updatedUser.alamat?.toRequestBody("text/plain".toMediaTypeOrNull())
                                
                                viewModel.updateProfile(token, namePart, usernamePart, phonePart, alamatPart)
                            },
                            onWalletClick = onTransaksiClick
                        )
                    }
                }
            }
            is Resource.Error -> {
                Text(
                    text = "Error: ${profileState?.message}",
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            else -> {}
        }
    }
}

@Composable
private fun ProfileHeaderSection(
    name: String,
    foto: String?,
    onMyRentalClick: () -> Unit,
    onRentalOwnerClick: () -> Unit,
    onWalletClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(DarkNavy)
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val profileImageUrl = remember(foto) {
                if (!foto.isNullOrEmpty()) {
                    if (foto.startsWith("http")) {
                        foto
                    } else {
                        // Jika path tidak diawali profiles/ dan bukan URL lengkap
                        val path = if (foto.startsWith("profiles/")) foto else "profiles/$foto"
                        "${ApiClient.IMAGE_BASE_URL}$path"
                    }
                } else {
                    "https://ui-avatars.com/api/?name=$name"
                }
            }

            AsyncImage(
                model = profileImageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.White, CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            
            // Hamburger Menu
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = Color.White
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    DropdownMenuItem(
                        text = { Text("My Rentals") },
                        onClick = { 
                            expanded = false
                            onMyRentalClick() 
                        },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.ListAlt, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Rentals Owner") },
                        onClick = { 
                            expanded = false
                            onRentalOwnerClick() 
                        },
                        leadingIcon = { Icon(Icons.Default.Storefront, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("My Wallet") },
                        onClick = { 
                            expanded = false
                            onWalletClick() 
                        },
                        leadingIcon = { Icon(Icons.Default.AccountBalanceWallet, null) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileFormSection(
    user: UserData,
    onLogout: () -> Unit,
    onEditPhoto: () -> Unit,
    onSave: (UserData) -> Unit,
    onWalletClick: () -> Unit
) {
    var name by remember { mutableStateOf(user.name) }
    var username by remember { mutableStateOf(user.username ?: "") }
    var phone by remember { mutableStateOf(user.phone_number ?: "") }
    var alamat by remember { mutableStateOf(user.alamat ?: "") }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            .background(DarkNavy)
            .padding(24.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Profile Picture Center
            Box(contentAlignment = Alignment.BottomEnd) {
                val profileImageUrl = remember(user.foto_profil) {
                    if (!user.foto_profil.isNullOrEmpty()) {
                        if (user.foto_profil.startsWith("http")) {
                            user.foto_profil
                        } else {
                            val path = if (user.foto_profil.startsWith("profiles/")) user.foto_profil else "profiles/${user.foto_profil}"
                            "${ApiClient.IMAGE_BASE_URL}$path"
                        }
                    } else {
                        "https://ui-avatars.com/api/?name=${user.name}"
                    }
                }

                AsyncImage(
                    model = profileImageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(2.dp, Color.White, CircleShape),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.Gray.copy(alpha = 0.8f))
                        .clickable { onEditPhoto() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Edit, null, Modifier.size(14.dp), tint = Color.White)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("Hello, ${user.name}!", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
            Text("Here is your quick overview", color = Color.Gray, fontSize = 12.sp)
            
            Spacer(modifier = Modifier.height(28.dp))
            
            ProfileTextField("Name Account", name) { name = it }
            ProfileTextField("Email", user.email, enabled = false) {}
            ProfileTextField("Nomor Telepon", phone) { phone = it }
            ProfileTextField("Username", username) { username = it }
            ProfileTextField("Alamat", alamat, isSingleLine = false) { alamat = it }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Wallet Card
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onWalletClick() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = InputBlue.copy(alpha = 0.2f))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Saldo", color = Color.White, fontSize = 12.sp)
                        Text("Rp ${formatRupiah(user.saldo)}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    }
                    Box(modifier = Modifier.size(32.dp).background(Color.White.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.AttachMoney, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            StatItem("Status Akun", if (user.is_banned) "Banned" else "Aktif")
            StatItem("Role", user.role.uppercase())
            
            Spacer(modifier = Modifier.height(40.dp))
            
            Button(
                onClick = { 
                    onSave(user.copy(name = name, username = username, phone_number = phone, alamat = alamat))
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = InputBlue),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("SAVE", fontWeight = FontWeight.Bold, color = DarkNavy)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("Logout", color = Color.Red.copy(alpha = 0.8f), modifier = Modifier.clickable { onLogout() }.padding(8.dp))
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileTextField(
    label: String, 
    value: String, 
    enabled: Boolean = true,
    isSingleLine: Boolean = true,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = if (isSingleLine) 48.dp else 100.dp)
                .clip(RoundedCornerShape(24.dp)),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = InputBlue,
                unfocusedContainerColor = InputBlue,
                disabledContainerColor = InputBlue.copy(alpha = 0.5f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedTextColor = DarkNavy,
                unfocusedTextColor = DarkNavy,
                disabledTextColor = DarkNavy.copy(alpha = 0.6f)
            ),
            singleLine = isSingleLine,
            textStyle = TextStyle(fontSize = 14.sp)
        )
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(label, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .width(110.dp)
                .height(36.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(InputBlue),
            contentAlignment = Alignment.Center
        ) {
            Text(value, color = DarkNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}
