package com.l0124005.sewain_rpl.ui.theme

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PhoneCallback
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
private val BorderGray = Color(0xFFE0E0E0)
private val ButtonGray = Color(0xFF6B8B9E)

@Composable
fun HomeScreen(onLogout: () -> Unit) {
    Scaffold(
        containerColor = Color.White,
        topBar = { HomeTopBar(onLogout = onLogout) },
        bottomBar = { HomeBottomNavigation() },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                FloatingActionButton(
                    onClick = { /* TODO */ },
                    containerColor = PrimaryBlue,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                }
                Spacer(modifier = Modifier.height(12.dp))
                FloatingActionButton(
                    onClick = { /* TODO */ },
                    containerColor = Color.White,
                    contentColor = PrimaryBlue,
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp).border(1.dp, BorderGray, CircleShape)
                ) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = "Top")
                }
            }
        }
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
            Spacer(modifier = Modifier.height(32.dp))
            AboutUsSection()
            Spacer(modifier = Modifier.height(40.dp))
            FeaturesSection()
            Spacer(modifier = Modifier.height(48.dp))
            CategoriesSection()
            Spacer(modifier = Modifier.height(48.dp))
            RentItemsSection()
            Spacer(modifier = Modifier.height(48.dp))
            TestimonialsSection()
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun HomeTopBar(onLogout: () -> Unit) {
    Surface(color = Color.White, shadowElevation = 1.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Menu, "Menu", Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "SEWAIN",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Sign\nin",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray,
                modifier = Modifier.clickable { onLogout() }.padding(end = 12.dp)
            )
            Button(
                onClick = { onLogout() },
                colors = ButtonDefaults.buttonColors(containerColor = ButtonGray),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("Sign Up", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
private fun HeroCollage() {
    val gray = Color(0xFFF1F1F1)
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(modifier = Modifier.weight(1f).height(170.dp).clip(RoundedCornerShape(12.dp)).background(gray))
            Column(modifier = Modifier.weight(1.2f), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.fillMaxWidth().height(80.dp).clip(RoundedCornerShape(12.dp)).background(gray))
                Spacer(Modifier.height(8.dp))
                Text("ALL YOU", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("can", fontSize = 12.sp, fontStyle = FontStyle.Italic, color = AccentBlue)
                Text("RENT", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFF4A7A9B), fontFamily = FontFamily.Serif)
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(30.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text("RENT NOW", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
            Box(modifier = Modifier.weight(1f).height(170.dp).clip(RoundedCornerShape(12.dp)).background(gray))
        }
        Spacer(Modifier.height(10.dp))
        Box(modifier = Modifier.fillMaxWidth().height(80.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF607D8B)))
    }
}

@Composable
private fun SearchCard() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            SearchFieldItem("Location", "Lokasi", Icons.Default.LocationOn)
            HorizontalDivider(color = BorderGray, thickness = 0.5.dp)
            SearchFieldItem("Find", "Cari barang...", Icons.Default.Search)
            HorizontalDivider(color = BorderGray, thickness = 0.5.dp)
            SearchFieldItem("Sewa Up", "17 July 2024", Icons.Default.CalendarMonth, true)
            HorizontalDivider(color = BorderGray, thickness = 0.5.dp)
            SearchFieldItem("Return", "20 July 2024", Icons.Default.CalendarMonth, true)
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ButtonGray)
            ) {
                Text("Search", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SearchFieldItem(label: String, value: String, icon: ImageVector, hasArrow: Boolean = false) {
    Column(modifier = Modifier.padding(vertical = 10.dp)) {
        Text(label, fontSize = 11.sp, color = Color.Gray)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, Modifier.size(16.dp), tint = AccentBlue)
            Spacer(Modifier.width(8.dp))
            Text(value, fontSize = 14.sp, modifier = Modifier.weight(1f))
            if (hasArrow) Icon(Icons.Default.KeyboardArrowDown, null, Modifier.size(18.dp), tint = Color.Gray)
        }
    }
}

@Composable
private fun BrandLogosSection() {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("FUJIFILM", fontSize = 28.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Text("BOSE", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("Naturehike", fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text("Honda", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(32.dp))
            Text("Apple", fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun AboutUsSection() {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("About us", fontSize = 26.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "SEWAIN adalah Platform digital yang menyediakan layanan penyewaan berbagai kebutuhan sehari-hari dengan mudah, cepat, dan terpercaya. Kami hadir untuk membantu pengguna menemukan dan menyewa barang tanpa harus membeli, sehingga lebih praktis dan hemat.",
            fontSize = 14.sp, color = Color.Gray, textAlign = TextAlign.Center, lineHeight = 22.sp
        )
    }
}

@Composable
private fun FeaturesSection() {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
        FeatureItem(Icons.Default.Category, "Wide Range of Rentals", "Various items available across multiple categories")
        FeatureItem(Icons.Default.EventAvailable, "Easy & Fast Booking", "Rent items in just a few simple steps")
        FeatureItem(Icons.AutoMirrored.Filled.PhoneCallback, "24/7 Support", "We're here to help anytime you need")
    }
}

@Composable
private fun FeatureItem(icon: ImageVector, title: String, desc: String) {
    Column(modifier = Modifier.width(100.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(ButtonGray.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = PrimaryBlue, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, lineHeight = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(desc, fontSize = 8.sp, color = Color.Gray, textAlign = TextAlign.Center, lineHeight = 10.sp)
    }
}

@Composable
private fun CategoriesSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Categories", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("Find what you are looking for", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontSize = 12.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(32.dp))
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            CategoryCard("Photography", Modifier.weight(1f))
            CategoryCard("Vehicles", Modifier.weight(1f))
            CategoryCard("Camping", Modifier.weight(1f))
        }
    }
}

@Composable
private fun CategoryCard(name: String, modifier: Modifier) {
    Column(modifier = modifier.shadow(2.dp, RoundedCornerShape(12.dp)).background(Color.White).padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(8.dp)).background(Color(0xFFF1F1F1)))
        Spacer(modifier = Modifier.height(8.dp))
        Text(name, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun RentItemsSection() {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Rent Items", fontSize = 32.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
        Spacer(modifier = Modifier.height(12.dp))
        Text("Temukan berbagai barang pilihan yang siap mendukung aktivitasmu.", fontSize = 13.sp, color = Color.Gray, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            modifier = Modifier.width(180.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Box(modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFE0E0E0)), contentAlignment = Alignment.Center) {
                    Text("No Image", color = Color.Gray, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text("Kamera DSLR Canon", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(5) { Icon(Icons.Default.Star, null, Modifier.size(12.dp), tint = Color(0xFFFFC107)) }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, Modifier.size(10.dp), tint = Color.Gray)
                    Text(" Surabaya", fontSize = 10.sp, color = Color.Gray)
                }
                Spacer(Modifier.height(4.dp))
                Text("Rp 50.000/hari", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryBlue)
            }
        }
    }
}

@Composable
private fun TestimonialsSection() {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("This Is What Our Customers Say", textAlign = TextAlign.Center, fontSize = 26.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
        Spacer(modifier = Modifier.height(24.dp))
        Card(
            modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(70.dp).clip(CircleShape).background(Color(0xFFE0E0E0)))
                Spacer(modifier = Modifier.height(16.dp))
                Text("“Saya selalu kesini buat sewa kamera. Thanks ya for making it easier untuk dapat barang dengan kualitas top.”", fontSize = 13.sp, textAlign = TextAlign.Center, fontStyle = FontStyle.Italic, lineHeight = 20.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Row { repeat(5) { Icon(Icons.Default.Star, null, Modifier.size(16.dp), tint = Color(0xFFFFC107)) } }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Karen W.", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("Fotografer", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
private fun HomeBottomNavigation() {
    NavigationBar(
        containerColor = Color(0xFF4A4A4A),
        modifier = Modifier.height(65.dp).clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
    ) {
        NavigationBarItem(selected = true, onClick = {}, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Home", fontSize = 10.sp) }, colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.White, unselectedIconColor = Color.Gray, selectedTextColor = Color.White, unselectedTextColor = Color.Gray, indicatorColor = Color.Transparent))
        NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Default.Search, null) }, label = { Text("Search", fontSize = 10.sp) }, colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.White, unselectedIconColor = Color.Gray, selectedTextColor = Color.White, unselectedTextColor = Color.Gray, indicatorColor = Color.Transparent))
        NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Default.Store, null) }, label = { Text("My Katalog", fontSize = 10.sp) }, colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.White, unselectedIconColor = Color.Gray, selectedTextColor = Color.White, unselectedTextColor = Color.Gray, indicatorColor = Color.Transparent))
        NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Default.Person, null) }, label = { Text("Me", fontSize = 10.sp) }, colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.White, unselectedIconColor = Color.Gray, selectedTextColor = Color.White, unselectedTextColor = Color.Gray, indicatorColor = Color.Transparent))
    }
}
