package com.l0124005.sewain_rpl.ui.theme.profil

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
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
    onEditProfile: () -> Unit
) {
    val profileState by viewModel.profile.observeAsState()

    LaunchedEffect(Unit) {
        viewModel.getProfile(token)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil Saya") },
                actions = {
                    IconButton(onClick = onEditProfile) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (profileState) {
                is Resource.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is Resource.Success -> {
                    val user = profileState?.data?.data
                    if (user != null) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AsyncImage(
                                model = if (user.foto_profil != null) "${ApiClient.IMAGE_BASE_URL}profiles/${user.foto_profil}" else "https://ui-avatars.com/api/?name=${user.name}",
                                contentDescription = "Profile Picture",
                                modifier = Modifier.size(100.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = user.name, style = MaterialTheme.typography.headlineSmall)
                            Text(text = user.email, style = MaterialTheme.typography.bodyMedium)
                            Text(text = "Saldo: Rp ${formatRupiah(user.saldo)}", style = MaterialTheme.typography.bodyLarge)
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            OutlinedButton(
                                onClick = onLogout,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Keluar")
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    Text(text = profileState?.message ?: "Error", modifier = Modifier.align(Alignment.Center))
                }
                else -> {}
            }
        }
    }
}
