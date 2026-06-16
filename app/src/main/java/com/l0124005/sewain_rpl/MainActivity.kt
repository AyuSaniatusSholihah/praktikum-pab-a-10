package com.l0124005.sewain_rpl

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.l0124005.sewain_rpl.ui.theme.Sewain_rplTheme
import com.l0124005.sewain_rpl.ui.theme.auth.LoginActivity
import com.l0124005.sewain_rpl.ui.theme.landing.LandingActivity
import com.l0124005.sewain_rpl.utils.SessionManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionManager = SessionManager(this)

        if (!sessionManager.isLoggedIn()) {
            val intent = Intent(this, LandingActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        enableEdgeToEdge()
        setContent {
            Sewain_rplTheme {
                HomeScreen(
                    onLogout = {
                        sessionManager.clearSession()
                        val intent = Intent(this@MainActivity, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}

private val PrimaryBlue = Color(0xFF285473)
private val AccentBlue = Color(0xFF5D8AA8)
private val SoftGray = Color(0xFFF1F1F1)
private val BorderGray = Color(0xFFE0E0E0)

@Composable
fun HomeScreen(onLogout: () -> Unit) {
    Scaffold(
        containerColor = Color.White,
        topBar = { HomeTopBar(onLogout = onLogout) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            HeroCollage()
            Spacer(modifier = Modifier.height(20.dp))
            SearchCard()
            Spacer(modifier = Modifier.height(28.dp))
            BrandLogosSection()
            Spacer(modifier = Modifier.height(28.dp))
            AboutUsSection()
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun HomeTopBar(onLogout: () -> Unit) {
    Surface(color = Color.White, shadowElevation = 2.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu",
                tint = Color(0xFF2A2A2A),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "SEWA",
                    fontSize = 22.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2A2A2A)
                )
                Text(
                    text = "IN",
                    fontSize = 22.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = AccentBlue
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Sign\nin",
                fontSize = 11.sp,
                color = PrimaryBlue,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onLogout() }
            )
            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(PrimaryBlue)
                    .clickable { onLogout() }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Sign\nUp",
                    fontSize = 11.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun HeroCollage() {
    val imageGray1 = Color(0xFFB0B8BF)
    val imageGray2 = Color(0xFFCFD8DC)
    val imageGray3 = Color(0xFFD6CFC7)
    val imageDark  = Color(0xFF607D8B)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Baris atas: 3 kolom
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Kolom kiri — placeholder kamera 1
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(160.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(imageGray1)
            )

            // Kolom tengah — teks overlay + tombol RENT NOW
            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .height(160.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(imageGray3),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "ALL YOU",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2A2A2A)
                    )
                    Text(
                        text = "can",
                        fontSize = 12.sp,
                        color = Color(0xFF555555),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                    Text(
                        text = "RENT",
                        fontSize = 26.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4A7A9B)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF4A7A9B))
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "RENT NOW",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Kolom kanan — placeholder elektronik
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(160.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(imageGray2)
            )
        }

        // Baris bawah — placeholder motor (lebar penuh)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(imageDark)
        )
    }
}

@Composable
private fun SearchCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderGray),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SearchField(
                label = "Location",
                value = "Lokasi",
                leadingIcon = Icons.Default.LocationOn
            )
            DividerLine()

            SearchField(
                label = "Find",
                value = "Cari barang...",
                leadingIcon = Icons.Default.Search
            )
            DividerLine()

            SearchField(
                label = "Sewa Up",
                value = "17 July 2024",
                leadingIcon = Icons.Default.DateRange,
                trailingIcon = Icons.Default.KeyboardArrowDown
            )
            DividerLine()

            SearchField(
                label = "Return",
                value = "20 July 2024",
                leadingIcon = Icons.Default.DateRange,
                trailingIcon = Icons.Default.KeyboardArrowDown
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { /* TODO: search action */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(AccentBlue, PrimaryBlue)
                            ),
                            shape = RoundedCornerShape(22.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Search",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchField(
    label: String,
    value: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                color = Color(0xFF2A2A2A),
                modifier = Modifier.weight(1f)
            )
            if (trailingIcon != null) {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun DividerLine() {
    HorizontalDivider(thickness = 1.dp, color = BorderGray)
}

@Composable
private fun BrandLogosSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BrandLabel("FUJIFILM", FontWeight.Black)
            BrandLabel("BOSE", FontWeight.Bold)
            BrandLabel("Naturehike", FontWeight.Normal)
        }
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BrandLabel("Honda", FontWeight.Bold)
            BrandLabel("Apple", FontWeight.Medium)
        }
    }
}

@Composable
private fun BrandLabel(name: String, weight: FontWeight) {
    Text(
        text = name,
        fontSize = 14.sp,
        fontWeight = weight,
        color = Color(0xFF555555)
    )
}

@Composable
private fun AboutUsSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "About us",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2A2A2A),
            fontFamily = FontFamily.Serif
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "SEWAIN adalah platform digital yang menyediakan layanan penyewaan berbagai " +
                    "kebutuhan sehari-hari dengan mudah, cepat, dan terpercaya. Kami hadir untuk " +
                    "membantu pengguna menemukan dan menyewa barang tanpa harus membeli, sehingga lebih " +
                    "hemat dan efisien.",
            fontSize = 13.sp,
            color = Color(0xFF555555),
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
    }
}
