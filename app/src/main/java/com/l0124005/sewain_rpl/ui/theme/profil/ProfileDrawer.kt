package com.l0124005.sewain_rpl.ui.theme.profil

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.l0124005.sewain_rpl.ui.theme.*

import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.l0124005.sewain_rpl.network.ApiClient

private val LocalMidBlue = Color(0xFF4D6674)
private val LocalDarkNavy = Color(0xFF21394F)
private val ActiveMenuBlue = Color(0xFF78A5B5)

@Composable
fun ProfileDrawerContent(
    userName: String,
    userPhoto: String? = null,
    currentScreen: String,
    onProfileClick: () -> Unit,
    onMyRentalsClick: () -> Unit,
    onRentalsOwnerClick: () -> Unit,
    onMyWalletClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = LocalMidBlue,
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
                if (!userPhoto.isNullOrEmpty()) {
                    val fullPhotoUrl = if (userPhoto.startsWith("http")) userPhoto 
                                     else "${ApiClient.IMAGE_BASE_URL}${if (userPhoto.startsWith("profiles/")) userPhoto else "profiles/$userPhoto"}"
                    AsyncImage(
                        model = fullPhotoUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                }
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

        DrawerMenuItem(
            icon = Icons.Default.Person,
            label = "Profile",
            selected = currentScreen == "Profile",
            onClick = onProfileClick
        )
        DrawerMenuItem(
            icon = Icons.AutoMirrored.Filled.List,
            label = "My Rentals",
            selected = currentScreen == "My Rentals",
            onClick = onMyRentalsClick
        )
        DrawerMenuItem(
            icon = Icons.Default.Store,
            label = "Rentals Owner",
            selected = currentScreen == "Rentals Owner",
            onClick = onRentalsOwnerClick
        )
        DrawerMenuItem(
            icon = Icons.Default.AccountBalanceWallet,
            label = "My Wallet",
            selected = currentScreen == "My Wallet",
            onClick = onMyWalletClick
        )
        DrawerMenuItem(
            icon = Icons.Default.Settings,
            label = "Settings",
            selected = currentScreen == "Settings",
            onClick = onSettingsClick
        )

        Spacer(Modifier.weight(1f))
        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

        DrawerMenuItem(
            icon = Icons.AutoMirrored.Filled.Logout,
            label = "Log Out",
            tint = Color(0xFFE57373),
            onClick = onLogoutClick
        )
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun DrawerMenuItem(
    icon: ImageVector,
    label: String,
    selected: Boolean = false,
    tint: Color = Color.White,
    onClick: () -> Unit
) {
    val background = if (selected) ActiveMenuBlue else Color.Transparent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) LocalDarkNavy else tint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = label,
            color = if (selected) LocalDarkNavy else Color.White,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
