package com.l0124005.sewain_rpl.ui.theme.profil

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.l0124005.sewain_rpl.network.ApiClient
import com.l0124005.sewain_rpl.ui.theme.katalog.formatRupiah
import com.l0124005.sewain_rpl.utils.Resource
import com.l0124005.sewain_rpl.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    token: String,
    onLogout: () -> Unit,
    onEditProfile: () -> Unit,
    onMyRentalClick: () -> Unit,
    onRentalOwnerClick: () -> Unit,
    onTransaksiClick: () -> Unit
) {
    val profileState by viewModel.profile.observeAsState()

    LaunchedEffect(Unit) {
        viewModel.getProfile(token)
    }

    val primaryColor = Color(0xFF285473)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil Saya", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8FAFB)
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (profileState) {
                is Resource.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = primaryColor)
                }
                is Resource.Success -> {
                    val user = profileState?.data?.data
                    if (user != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Profile Header
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    AsyncImage(
                                        model = if (!user.foto_profil.isNullOrEmpty()) "${ApiClient.IMAGE_BASE_URL}profiles/${user.foto_profil}" else "https://ui-avatars.com/api/?name=${user.name}",
                                        contentDescription = "Profile Picture",
                                        modifier = Modifier.size(90.dp).clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(text = user.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    Text(text = user.email, fontSize = 14.sp, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        color = primaryColor.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(20.dp)
                                    ) {
                                        Text(
                                            text = "Saldo: Rp ${formatRupiah(user.saldo)}",
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                            color = primaryColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Menu Section
                            Text(
                                text = "Pengaturan Akun",
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                fontSize = 13.sp
                            )

                            ProfileMenuItem(
                                icon = Icons.Default.Person,
                                label = "Edit Profile",
                                onClick = onEditProfile
                            )
                            ProfileMenuItem(
                                icon = Icons.Default.ShoppingCart,
                                label = "My Rental",
                                onClick = onMyRentalClick
                            )
                            ProfileMenuItem(
                                icon = Icons.Default.Storefront,
                                label = "Rental Owner",
                                onClick = onRentalOwnerClick
                            )
                            ProfileMenuItem(
                                icon = Icons.Default.History,
                                label = "Transaksi",
                                onClick = onTransaksiClick
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            Button(
                                onClick = onLogout,
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE24B4A)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Keluar", fontWeight = FontWeight.Bold)
                            }
                            
                            Spacer(modifier = Modifier.height(40.dp))
                        }
                    }
                }
                is Resource.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = profileState?.message ?: "Error", color = Color.Red)
                        Button(onClick = { viewModel.getProfile(token) }) {
                            Text("Coba Lagi")
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color(0xFF285473), modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = label, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium, fontSize = 15.sp)
            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(14.dp))
        }
    }
}

