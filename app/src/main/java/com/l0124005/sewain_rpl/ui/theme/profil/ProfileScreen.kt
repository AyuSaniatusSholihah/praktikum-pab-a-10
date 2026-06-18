package com.l0124005.sewain_rpl.ui.theme.profil

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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.l0124005.sewain_rpl.network.ApiClient
import com.l0124005.sewain_rpl.network.UserData
import com.l0124005.sewain_rpl.utils.CurrencyUtils
import com.l0124005.sewain_rpl.utils.Resource
import com.l0124005.sewain_rpl.viewmodel.ProfileViewModel

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

    LaunchedEffect(Unit) {
        viewModel.getProfile(token)
    }

    Scaffold(
        bottomBar = { ProfileBottomNavigation() }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                                onRentalOwnerClick = onRentalOwnerClick
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            ProfileFormSection(
                                user = user,
                                onLogout = onLogout,
                                onEditProfile = onEditProfile,
                                onRiwayatTransaksiClick = onRiwayatTransaksiClick
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
}

@Composable
private fun ProfileHeaderSection(
    name: String,
    foto: String?,
    onMyRentalClick: () -> Unit,
    onRentalOwnerClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(DarkNavy)
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = if (!foto.isNullOrEmpty()) "${ApiClient.IMAGE_BASE_URL}profiles/$foto" else "https://ui-avatars.com/api/?name=$name",
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
            
            // Sidebar Menu Mockup
            Column(horizontalAlignment = Alignment.End) {
                HeaderMenuItem(Icons.Default.GridView, "Profile", true, {})
                HeaderMenuItem(Icons.AutoMirrored.Filled.ListAlt, "My Rentals", false, onMyRentalClick)
                HeaderMenuItem(Icons.Default.Storefront, "Rentals Owner", false, onRentalOwnerClick)
                HeaderMenuItem(Icons.Default.AccountBalanceWallet, "My Wallet", false, {})
            }
        }
    }
}

@Composable
private fun HeaderMenuItem(icon: ImageVector, label: String, isSelected: Boolean = false, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(vertical = 2.dp)
            .clickable { onClick() }
    ) {
        Icon(icon, null, Modifier.size(12.dp), tint = if (isSelected) Color.White else Color.Gray)
        Spacer(Modifier.width(6.dp))
        Text(label, color = if (isSelected) Color.White else Color.Gray, fontSize = 9.sp)
    }
}

@Composable
private fun ProfileFormSection(
    user: UserData,
    onLogout: () -> Unit,
    onEditProfile: () -> Unit,
    onRiwayatTransaksiClick: () -> Unit
) {
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
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, null, Modifier.size(50.dp), tint = DarkNavy)
                }
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.Gray.copy(alpha = 0.8f))
                        .clickable { /* TODO: Edit Photo */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Edit, null, Modifier.size(14.dp), tint = Color.White)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("Hello, ${user.name}!", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
            Text("Here is your quick overview", color = Color.Gray, fontSize = 12.sp)
            
            Spacer(modifier = Modifier.height(28.dp))
            
            ProfileTextField("Name Account", user.name)
            ProfileTextField("Email", user.email)
            ProfileTextField("Password Baru", "********")
            ProfileTextField("Konfirmasi Password Baru", "********")
            ProfileTextField("Nomor Telepon", user.phone_number ?: "085890423763")
            ProfileTextField("Username", user.username ?: user.name.lowercase().replace(" ", ""))
            ProfileTextField("Alamat", user.alamat ?: "Alamat belum diatur", isSingleLine = false)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Wallet Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = InputBlue.copy(alpha = 0.2f))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Saldo", color = Color.White, fontSize = 12.sp)
                        Text("Rp ${CurrencyUtils.formatRupiah(user.saldo)}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    }
                    Box(modifier = Modifier.size(32.dp).background(Color.White.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.AttachMoney, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            StatItem("Total Transaksi Penyewaan", "1 Kali")
            StatItem("Jumlah Katalog Barang", "0 Barang")
            
            Spacer(modifier = Modifier.height(40.dp))
            
            Button(
                onClick = { onEditProfile() },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = InputBlue),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("EDIT PROFILE", fontWeight = FontWeight.Bold, color = DarkNavy)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("Logout", color = Color.Red.copy(alpha = 0.8f), modifier = Modifier.clickable { onLogout() }.padding(8.dp))
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ProfileTextField(label: String, value: String, isSingleLine: Boolean = true) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isSingleLine) 48.dp else 100.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(InputBlue)
                .padding(horizontal = 20.dp),
            contentAlignment = if (isSingleLine) Alignment.CenterStart else Alignment.TopStart
        ) {
            Text(
                text = value,
                color = DarkNavy,
                fontSize = 14.sp,
                modifier = if (isSingleLine) Modifier else Modifier.padding(vertical = 12.dp)
            )
        }
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

@Composable
private fun ProfileBottomNavigation() {
    NavigationBar(
        containerColor = Color(0xFF4A4A4A),
        modifier = Modifier.height(65.dp).clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
    ) {
        NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Home", fontSize = 10.sp) }, colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.White, unselectedIconColor = Color.Gray, selectedTextColor = Color.White, unselectedTextColor = Color.Gray, indicatorColor = Color.Transparent))
        NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Default.Search, null) }, label = { Text("Search", fontSize = 10.sp) }, colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.White, unselectedIconColor = Color.Gray, selectedTextColor = Color.White, unselectedTextColor = Color.Gray, indicatorColor = Color.Transparent))
        NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Default.Store, null) }, label = { Text("My Katalog", fontSize = 10.sp) }, colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.White, unselectedIconColor = Color.Gray, selectedTextColor = Color.White, unselectedTextColor = Color.Gray, indicatorColor = Color.Transparent))
        NavigationBarItem(selected = true, onClick = {}, icon = { Icon(Icons.Default.Person, null) }, label = { Text("Me", fontSize = 10.sp) }, colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.White, unselectedIconColor = Color.Gray, selectedTextColor = Color.White, unselectedTextColor = Color.Gray, indicatorColor = Color.Transparent))
    }
}
