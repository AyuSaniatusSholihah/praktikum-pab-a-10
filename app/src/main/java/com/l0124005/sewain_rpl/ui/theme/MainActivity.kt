package com.l0124005.sewain_rpl.ui.theme

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
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
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.l0124005.sewain_rpl.network.ApiClient
import com.l0124005.sewain_rpl.network.CatalogData
import com.l0124005.sewain_rpl.network.KatalogListResponse
import com.l0124005.sewain_rpl.repository.KatalogRepository
import com.l0124005.sewain_rpl.repository.KeranjangRepository
import com.l0124005.sewain_rpl.repository.ProfileRepository
import com.l0124005.sewain_rpl.repository.TransaksiRepository
import com.l0124005.sewain_rpl.ui.theme.auth.LoginActivity
import com.l0124005.sewain_rpl.ui.theme.katalog.*
import com.l0124005.sewain_rpl.ui.theme.landing.LandingActivity
import com.l0124005.sewain_rpl.ui.theme.profil.ProfileScreen
import com.l0124005.sewain_rpl.ui.theme.transaksi.RiwayatTransaksiScreen
import com.l0124005.sewain_rpl.ui.theme.transaksi.DetailTransaksiScreen
import com.l0124005.sewain_rpl.ui.theme.keranjang.KeranjangScreen
import com.l0124005.sewain_rpl.utils.Resource
import com.l0124005.sewain_rpl.utils.SessionManager
import com.l0124005.sewain_rpl.utils.CurrencyUtils
import com.l0124005.sewain_rpl.viewmodel.*

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
                val token = sessionManager.getToken()

                // ViewModels
                val katalogViewModel: KatalogViewModel = viewModel(factory = KatalogViewModelFactory(KatalogRepository()))
                val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(ProfileRepository()))
                val keranjangViewModel: KeranjangViewModel = viewModel(factory = KeranjangViewModelFactory(KeranjangRepository()))
                val transaksiViewModel: TransaksiViewModel = viewModel(factory = TransaksiViewModelFactory(TransaksiRepository()))

                MainContainer(
                    token = token,
                    katalogViewModel = katalogViewModel,
                    profileViewModel = profileViewModel,
                    keranjangViewModel = keranjangViewModel,
                    transaksiViewModel = transaksiViewModel,
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

enum class Screen {
    Home, Rental, Keranjang, MyKatalog, Profile, RiwayatTransaksi, AddItem, EditItem, DetailTransaksi, Pembayaran, MyRental, ProductDetail
}

@Composable
fun MainContainer(
    token: String,
    katalogViewModel: KatalogViewModel,
    profileViewModel: ProfileViewModel,
    keranjangViewModel: KeranjangViewModel,
    transaksiViewModel: TransaksiViewModel,
    onLogout: () -> Unit
) {
    var currentScreen by remember { mutableStateOf(Screen.Home) }
    var selectedBarangForEdit by remember { mutableStateOf<CatalogData?>(null) }
    var selectedProductId by remember { mutableStateOf<Int?>(null) }
    var selectedTransaksiId by remember { mutableStateOf<Int?>(null) }
    var selectedTransaksiIdsForPayment by remember { mutableStateOf<List<Int>>(emptyList()) }

    Scaffold(
        bottomBar = {
            if (currentScreen in listOf(Screen.Home, Screen.Rental, Screen.Keranjang, Screen.MyKatalog, Screen.Profile)) {
                HomeBottomNavigation(
                    currentScreen = currentScreen,
                    onScreenSelected = { currentScreen = it }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentScreen) {
                Screen.Home -> HomeScreenContent(
                    katalogViewModel = katalogViewModel,
                    onLogout = onLogout,
                    onSeeAllRentals = { currentScreen = Screen.Rental },
                    onProductClick = { id ->
                        selectedProductId = id
                        currentScreen = Screen.ProductDetail
                    }
                )
                Screen.Rental -> RentalsScreen(
                    viewModel = katalogViewModel,
                    keranjangViewModel = keranjangViewModel,
                    token = token,
                    onItemClick = { barang ->
                        selectedProductId = barang.id
                        currentScreen = Screen.ProductDetail
                    }
                )
                Screen.Keranjang -> KeranjangScreen(
                    token = token,
                    viewModel = keranjangViewModel,
                    onBack = { currentScreen = Screen.Home },
                    onCheckout = {
                        // Logika checkout bisa diarahkan ke pembayaran jika perlu
                        // Untuk sekarang tetap di keranjang atau ke riwayat
                        currentScreen = Screen.RiwayatTransaksi
                    }
                )
                Screen.ProductDetail -> selectedProductId?.let { id ->
                    DetailProdukScreen(
                        productId = id,
                        token = token,
                        katalogViewModel = katalogViewModel,
                        keranjangViewModel = keranjangViewModel,
                        onBack = { currentScreen = Screen.Rental }
                    )
                }
                Screen.Pembayaran -> {
                    com.l0124005.sewain_rpl.ui.theme.transaksi.PembayaranScreen(
                        viewModel = transaksiViewModel,
                        token = token,
                        transaksiIds = selectedTransaksiIdsForPayment,
                        onBack = { currentScreen = Screen.DetailTransaksi },
                        onPaymentSuccess = {
                            currentScreen = Screen.RiwayatTransaksi
                        }
                    )
                }
                Screen.AddItem -> AddItemScreen(
                    viewModel = katalogViewModel,
                    token = token,
                    onBack = { currentScreen = Screen.MyKatalog }
                )
                Screen.EditItem -> selectedBarangForEdit?.let {
                    EditItemScreen(
                        viewModel = katalogViewModel,
                        token = token,
                        itemId = it.id,
                        onBack = { currentScreen = Screen.MyKatalog },
                        onSuccess = { currentScreen = Screen.MyKatalog }
                    )
                }
                Screen.MyKatalog -> MyKatalogScreen(
                    viewModel = katalogViewModel,
                    token = token,
                    onBack = { currentScreen = Screen.Home },
                    onAddItem = { currentScreen = Screen.AddItem },
                    onEditItem = { barang ->
                        selectedBarangForEdit = barang
                        currentScreen = Screen.EditItem
                    }
                )
                Screen.Profile -> ProfileScreen(
                    viewModel = profileViewModel,
                    token = token,
                    onLogout = onLogout,
                    onEditProfile = { /* TODO */ }
                )
                Screen.RiwayatTransaksi -> RiwayatTransaksiScreen(
                    viewModel = transaksiViewModel,
                    token = token,
                    onBack = { currentScreen = Screen.Profile },
                    onDetailClick = { id ->
                        selectedTransaksiId = id
                        currentScreen = Screen.DetailTransaksi
                    }
                )
                Screen.DetailTransaksi -> selectedTransaksiId?.let { id ->
                    DetailTransaksiScreen(
                        transaksiId = id,
                        token = token,
                        viewModel = transaksiViewModel,
                        onBack = { currentScreen = Screen.RiwayatTransaksi },
                        onPayClick = { ids ->
                            selectedTransaksiIdsForPayment = ids
                            currentScreen = Screen.Pembayaran
                        }
                    )
                }
                Screen.MyRental -> {
                    // Fallback sementara jika MyRental dipilih
                    HomeScreenContent(
                        katalogViewModel = katalogViewModel,
                        onLogout = onLogout,
                        onSeeAllRentals = { currentScreen = Screen.Rental },
                        onProductClick = { id ->
                            selectedProductId = id
                            currentScreen = Screen.ProductDetail
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HomeScreenContent(
    katalogViewModel: KatalogViewModel,
    onLogout: () -> Unit,
    onSeeAllRentals: () -> Unit,
    onProductClick: (Int) -> Unit
) {
    val katalogState by katalogViewModel.katalogPublik.observeAsState(Resource.Loading())

    LaunchedEffect(Unit) {
        katalogViewModel.getKatalogPublik(kategoriId = null)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        HomeTopBar(onLogout = onLogout)
        HeroCollage(onSeeAllRentals)
        Spacer(modifier = Modifier.height(20.dp))
        SearchCard()
        Spacer(modifier = Modifier.height(28.dp))
        BrandLogosSection()
        Spacer(modifier = Modifier.height(32.dp))
        AboutUsSection()
        Spacer(modifier = Modifier.height(48.dp))
        CategoriesSection()
        Spacer(modifier = Modifier.height(48.dp))
        RentItemsSection(katalogState, onProductClick)
        Spacer(modifier = Modifier.height(48.dp))
        TestimonialsSection()
        Spacer(modifier = Modifier.height(100.dp))
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
private fun HeroCollage(onRentNow: () -> Unit) {
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
                    onClick = onRentNow,
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
private fun RentItemsSection(katalogState: Resource<KatalogListResponse>, onProductClick: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Rent Items", fontSize = 32.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
        Spacer(modifier = Modifier.height(12.dp))
        Text("Temukan berbagai barang pilihan yang siap mendukung aktivitasmu.", fontSize = 13.sp, color = Color.Gray, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(32.dp))

        when (katalogState) {
            is Resource.Loading -> {
                CircularProgressIndicator(color = PrimaryBlue)
            }
            is Resource.Success -> {
                val items = katalogState.data?.data?.take(4) ?: emptyList()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items.forEach { barang ->
                        HomeRentalCard(barang, onProductClick, Modifier.weight(1f))
                    }
                }
            }
            is Resource.Error -> {
                Text("Gagal memuat barang", color = Color.Red, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun HomeRentalCard(barang: CatalogData, onProductClick: (Int) -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.clickable { onProductClick(barang.id) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                if (!barang.foto_barang.isNullOrEmpty()) {
                    AsyncImage(
                        model = "${ApiClient.IMAGE_BASE_URL}${barang.foto_barang}",
                        contentDescription = barang.nama_barang,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text("No Image", color = Color.Gray, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(barang.nama_barang, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, Modifier.size(10.dp), tint = Color.Gray)
                Text(" ${barang.lokasi}", fontSize = 10.sp, color = Color.Gray)
            }
            Spacer(Modifier.height(4.dp))
            Text("Rp ${CurrencyUtils.formatRupiah(barang.harga_sewa)}/hari", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryBlue)
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
private fun HomeBottomNavigation(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFF4A4A4A),
        modifier = Modifier
            .navigationBarsPadding() // Memberikan jarak dari navigasi sistem HP
            .height(80.dp)
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
    ) {
        NavigationBarItem(
            selected = currentScreen == Screen.Home,
            onClick = { onScreenSelected(Screen.Home) },
            icon = { Icon(Icons.Default.Home, null) },
            label = { Text("Home", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.White, unselectedIconColor = Color.Gray, selectedTextColor = Color.White, unselectedTextColor = Color.Gray, indicatorColor = Color.Transparent)
        )
        NavigationBarItem(
            selected = currentScreen == Screen.Rental,
            onClick = { onScreenSelected(Screen.Rental) },
            icon = { Icon(Icons.Default.Search, null) },
            label = { Text("Rental", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.White, unselectedIconColor = Color.Gray, selectedTextColor = Color.White, unselectedTextColor = Color.Gray, indicatorColor = Color.Transparent)
        )
        NavigationBarItem(
            selected = currentScreen == Screen.Keranjang,
            onClick = { onScreenSelected(Screen.Keranjang) },
            icon = { Icon(Icons.Default.ShoppingCart, null) },
            label = { Text("Keranjang", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.White, unselectedIconColor = Color.Gray, selectedTextColor = Color.White, unselectedTextColor = Color.Gray, indicatorColor = Color.Transparent)
        )
        NavigationBarItem(
            selected = currentScreen == Screen.MyKatalog,
            onClick = { onScreenSelected(Screen.MyKatalog) },
            icon = { Icon(Icons.Default.Store, null) },
            label = { Text("My Katalog", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.White, unselectedIconColor = Color.Gray, selectedTextColor = Color.White, unselectedTextColor = Color.Gray, indicatorColor = Color.Transparent)
        )
        NavigationBarItem(
            selected = currentScreen == Screen.Profile || currentScreen == Screen.RiwayatTransaksi || currentScreen == Screen.DetailTransaksi || currentScreen == Screen.Pembayaran,
            onClick = { onScreenSelected(Screen.Profile) },
            icon = { Icon(Icons.Default.Person, null) },
            label = { Text("Me", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.White, unselectedIconColor = Color.Gray, selectedTextColor = Color.White, unselectedTextColor = Color.Gray, indicatorColor = Color.Transparent)
        )
    }
}