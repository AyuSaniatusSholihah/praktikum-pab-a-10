package com.l0124005.sewain_rpl.ui.theme

import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PhoneCallback
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import coil.compose.AsyncImage
import com.l0124005.sewain_rpl.network.ApiClient
import com.l0124005.sewain_rpl.network.CatalogData
import com.l0124005.sewain_rpl.network.KatalogListResponse
import com.l0124005.sewain_rpl.network.KategoriListResponse
import com.l0124005.sewain_rpl.network.KeranjangItem
import com.l0124005.sewain_rpl.repository.KatalogRepository
import com.l0124005.sewain_rpl.repository.KeranjangRepository
import com.l0124005.sewain_rpl.repository.ProfileRepository
import com.l0124005.sewain_rpl.repository.TransaksiRepository
import com.l0124005.sewain_rpl.ui.theme.auth.LoginActivity
import com.l0124005.sewain_rpl.ui.theme.katalog.*
import com.l0124005.sewain_rpl.ui.theme.landing.LandingActivity
import com.l0124005.sewain_rpl.ui.theme.profil.ProfileScreen
import com.l0124005.sewain_rpl.ui.theme.profil.MyWalletScreen
import com.l0124005.sewain_rpl.ui.theme.profil.MyRentalsScreen
import com.l0124005.sewain_rpl.ui.theme.profil.RentalsOwnerScreen
import com.l0124005.sewain_rpl.ui.theme.admin.OwnerDashboardScreen
import com.l0124005.sewain_rpl.ui.theme.transaksi.RiwayatTransaksiScreen
import com.l0124005.sewain_rpl.ui.theme.transaksi.DetailTransaksiScreen
import com.l0124005.sewain_rpl.ui.theme.keranjang.KeranjangScreen
import com.l0124005.sewain_rpl.utils.Resource
import com.l0124005.sewain_rpl.utils.SessionManager
import com.l0124005.sewain_rpl.utils.CurrencyUtils
import com.l0124005.sewain_rpl.viewmodel.*
import kotlinx.coroutines.launch
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import com.l0124005.sewain_rpl.ui.theme.profil.ProfileDrawerContent
import com.l0124005.sewain_rpl.ui.theme.VidalokaFont
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.l0124005.sewain_rpl.R

data class ScreenTarget(val screen: Screen, val id: Long = System.currentTimeMillis(), val productId: Int? = null)

// ============================================================
// WARNA TEMA HOME -- disamain persis sama hex di home.css
// ============================================================
private val Volkhov = FontFamily.Default // Fallback jika tidak ada font custom

private object HomeColors {
    val Background     = Color(0xFFFFFFFF)
    val TextDark       = Color(0xFF1A1A1A) // --color-text
    val TextBody       = Color(0xFF484848) // warna teks judul produk, harga, dll
    val TextMuted      = Color(0xFF8A8A8A) // about-text, loc, reviews
    val Accent         = Color(0xFF6A87A1) // .site-logo span, hero "can", btn-search
    val AccentDeep     = Color(0xFF4D6674) // .btn-rent / .btn-view-more / floating-btn
    val IconStroke     = Color(0xFF55959E) // ikon lokasi/kalender di search bar
    val FeatureIcon    = Color(0xFF6C8EA1) // lingkaran ikon di About Us
    val SoftGray       = Color(0xFFE0E0E0) // background hero & categories slider
    val ImgPlaceholder = Color(0xFFEDEDED)
    val Border         = Color(0xFFE2E2E2) // --color-border
    val Star           = Color(0xFFF5B800) // --color-star
    val TestiBg        = Color(0xFFF8F8F8)
}

class MainActivity : ComponentActivity() {
    private val _initialScreen = mutableStateOf(ScreenTarget(Screen.Home))

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

        val screenStr = intent.getStringExtra("TARGET_SCREEN")
        val productId = if (intent.hasExtra("PRODUCT_ID")) intent.getIntExtra("PRODUCT_ID", -1) else null
        _initialScreen.value = ScreenTarget(getScreenFromStr(screenStr), productId = if (productId == -1) null else productId)

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
                    initialScreen = _initialScreen.value,
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val screenStr = intent.getStringExtra("TARGET_SCREEN")
        val productId = if (intent.hasExtra("PRODUCT_ID")) intent.getIntExtra("PRODUCT_ID", -1) else null
        if (screenStr != null) {
            _initialScreen.value = ScreenTarget(getScreenFromStr(screenStr), productId = if (productId == -1) null else productId)
        }
    }

    private fun getScreenFromStr(str: String?): Screen {
        return when (str) {
            "KERANJANG" -> Screen.Keranjang
            "TRANSAKSI" -> Screen.RiwayatTransaksi
            "CHECKOUT" -> Screen.CheckoutPayment
            else -> Screen.Home
        }
    }
}

enum class Screen {
    Home, Rental, Keranjang, MyKatalog, Profile, RiwayatTransaksi, AddItem, EditItem, DetailTransaksi, Pembayaran, MyRental, RentalsOwner, CheckoutPayment, Confirm, MyWallet, Settings
}

@Composable
fun MainContainer(
    token: String,
    katalogViewModel: KatalogViewModel,
    profileViewModel: ProfileViewModel,
    keranjangViewModel: KeranjangViewModel,
    transaksiViewModel: TransaksiViewModel,
    initialScreen: ScreenTarget = ScreenTarget(Screen.Home),
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(initialScreen.screen) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var selectedItemsForCheckout by remember { mutableStateOf<List<KeranjangItem>>(emptyList()) }
    var selectedBarangForEdit by remember { mutableStateOf<CatalogData?>(null) }
    var selectedTransaksiId by remember { mutableStateOf<Int?>(null) }
    var selectedTransaksiIdsForPayment by remember { mutableStateOf<List<Int>>(emptyList()) }

    val keranjangState by keranjangViewModel.keranjang.observeAsState()
    val checkoutState by transaksiViewModel.checkoutState.observeAsState()

    LaunchedEffect(initialScreen) {
        currentScreen = initialScreen.screen
        if (initialScreen.screen == Screen.CheckoutPayment && initialScreen.productId != null) {
            keranjangViewModel.getKeranjang(token)
        } else if (initialScreen.screen == Screen.Keranjang) {
            keranjangViewModel.getKeranjang(token)
        }
    }

    LaunchedEffect(keranjangState) {
        if (currentScreen == Screen.CheckoutPayment && initialScreen.productId != null && keranjangState is Resource.Success) {
            val items = (keranjangState as Resource.Success<com.l0124005.sewain_rpl.network.KeranjangResponse>).data?.data?.items ?: emptyList()
            val filtered = items.filter { it.barang?.id == initialScreen.productId }
            if (filtered.isNotEmpty()) {
                selectedItemsForCheckout = filtered
            } else {
                Toast.makeText(context, "Produk tidak ditemukan di keranjang", Toast.LENGTH_SHORT).show()
                currentScreen = Screen.Home
            }
            keranjangViewModel.resetStates()
        } else if (keranjangState is Resource.Success) {
            Toast.makeText(context, "Berhasil ditambahkan ke keranjang!", Toast.LENGTH_SHORT).show()
            keranjangViewModel.resetStates()
        } else if (keranjangState is Resource.Error) {
            Toast.makeText(context, "Gagal: ${keranjangState?.message}", Toast.LENGTH_SHORT).show()
            keranjangViewModel.resetStates()
        }
    }

    LaunchedEffect(checkoutState) {
        if (checkoutState is Resource.Success) {
            Toast.makeText(context, "Sewa berhasil!", Toast.LENGTH_SHORT).show()
            keranjangViewModel.getKeranjang(token)
            currentScreen = Screen.Confirm
        } else if (checkoutState is Resource.Error) {
            Toast.makeText(context, "Checkout gagal: ${checkoutState?.message}", Toast.LENGTH_SHORT).show()
            transaksiViewModel.resetCheckoutState()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            val profileState by profileViewModel.profile.observeAsState()
            val userName = (profileState as? Resource.Success)?.data?.data?.name ?: "User"

            ProfileDrawerContent(
                userName = userName,
                currentScreen = currentScreen.name,
                onProfileClick = {
                    scope.launch { drawerState.close() }
                    currentScreen = Screen.Profile
                },
                onMyRentalsClick = {
                    scope.launch { drawerState.close() }
                    currentScreen = Screen.MyRental
                },
                onRentalsOwnerClick = {
                    scope.launch { drawerState.close() }
                    currentScreen = Screen.RentalsOwner
                },
                onMyWalletClick = {
                    scope.launch { drawerState.close() }
                    currentScreen = Screen.MyWallet
                },
                onLogoutClick = {
                    scope.launch { drawerState.close() }
                    onLogout()
                },
                onSettingsClick = {
                    scope.launch { drawerState.close() }
                    currentScreen = Screen.Settings
                }
            )
        },
        gesturesEnabled = currentScreen == Screen.Profile
    ) {
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
                        keranjangViewModel = keranjangViewModel,
                        token = token,
                        onLogout = onLogout,
                        onMenuClick = {
                            scope.launch { drawerState.open() }
                        },
                        onSeeAllRentals = { currentScreen = Screen.Rental },
                        onProductClick = { productId: Int ->
                            val intent = Intent(context, DetailProdukActivity::class.java).apply {
                                putExtra("EXTRA_PRODUCT_ID", productId)
                            }
                            context.startActivity(intent)
                        }
                    )
                    Screen.Rental -> ProductScreen(
                        token = token,
                        viewModel = katalogViewModel,
                        keranjangViewModel = keranjangViewModel,
                        onItemClick = { id ->
                            val intent = Intent(context, DetailProdukActivity::class.java).apply {
                                putExtra("EXTRA_PRODUCT_ID", id)
                            }
                            context.startActivity(intent)
                        }
                    )
                    Screen.Keranjang -> KeranjangScreen(
                        token = token,
                        viewModel = keranjangViewModel,
                        onBack = { currentScreen = Screen.Home },
                        onCheckout = { selectedItems ->
                            selectedItemsForCheckout = selectedItems
                            currentScreen = Screen.CheckoutPayment
                        }
                    )
                    Screen.CheckoutPayment -> {
                        com.l0124005.sewain_rpl.ui.theme.checkout.CheckoutPaymentScreen(
                            token = token,
                            keranjangViewModel = keranjangViewModel,
                            profileViewModel = profileViewModel,
                            transaksiViewModel = transaksiViewModel,
                            selectedItems = selectedItemsForCheckout,
                            onEditProfile = {
                                currentScreen = Screen.Profile
                            },
                            onBack = {
                                currentScreen = Screen.Keranjang
                            }
                        )
                    }
                    Screen.Confirm -> {
                        val checkoutResponse = (checkoutState as? Resource.Success)?.data
                        val confirmedState = checkoutResponse?.let { com.l0124005.sewain_rpl.ui.theme.checkout.mapCheckoutResponseToState(it) }

                        if (confirmedState != null) {
                            com.l0124005.sewain_rpl.ui.theme.checkout.PaymentConfirmedScreen(
                                orderNumber = confirmedState.orderNumber,
                                items = confirmedState.items,
                                shippingCost = confirmedState.shippingCost,
                                customer = confirmedState.customer,
                                onBackHome = {
                                    transaksiViewModel.resetCheckoutState()
                                    currentScreen = Screen.Home
                                }
                            )
                        } else {
                            LaunchedEffect(Unit) {
                                currentScreen = Screen.Home
                            }
                        }
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
                        profileViewModel = profileViewModel,
                        token = token,
                        onBack = { currentScreen = Screen.MyKatalog },
                        onSuccess = { currentScreen = Screen.MyKatalog }
                    )
                    Screen.EditItem -> selectedBarangForEdit?.let {
                        EditItemScreen(
                            viewModel = katalogViewModel,
                            token = token,
                            itemId = it.id,
                            onBack = { currentScreen = Screen.MyKatalog },
                            onSuccess = {
                                // Refresh daftar katalog dari server agar perubahan tampil
                                katalogViewModel.getMyKatalog(token)
                                currentScreen = Screen.MyKatalog
                            }
                        )
                    }
                    Screen.MyKatalog -> MyKatalogScreen(
                        viewModel = katalogViewModel,
                        profileViewModel = profileViewModel,
                        token = token,
                        onBack = { currentScreen = Screen.Home },
                        onAddItem = { currentScreen = Screen.AddItem },
                        onEditItem = { barang ->
                            selectedBarangForEdit = barang
                            currentScreen = Screen.EditItem
                        },
                        onItemClick = { barang ->
                            selectedBarangForEdit = barang
                            currentScreen = Screen.EditItem
                        }
                    )
                    Screen.Profile -> ProfileScreen(
                        viewModel = profileViewModel,
                        token = token,
                        onLogout = onLogout,
                        onEditProfile = { /* TODO */ },
                        onMyRentalClick = { currentScreen = Screen.MyRental },
                        onRentalOwnerClick = { currentScreen = Screen.RentalsOwner },
                        onMyWalletClick = { currentScreen = Screen.MyWallet },
                        onSettingsClick = { currentScreen = Screen.Settings }
                    )
                    Screen.MyRental -> {
                        MyRentalsScreen(
                            viewModel = transaksiViewModel,
                            profileViewModel = profileViewModel,
                            token = token,
                            onBack = { currentScreen = Screen.Profile },
                            onRentalsOwnerClick = { currentScreen = Screen.RentalsOwner },
                            onMyWalletClick = { currentScreen = Screen.MyWallet },
                            onLogoutClick = onLogout,
                            onSettingsClick = { currentScreen = Screen.Settings }
                        )
                    }
                    Screen.RentalsOwner -> {
                        RentalsOwnerScreen(
                            viewModel = transaksiViewModel,
                            profileViewModel = profileViewModel,
                            token = token,
                            onBack = { currentScreen = Screen.Profile },
                            onLogout = onLogout,
                            onProfileClick = { currentScreen = Screen.Profile },
                            onMyRentalClick = { currentScreen = Screen.MyRental },
                            onWalletClick = { currentScreen = Screen.MyWallet },
                            onRentalClick = { /* TODO */ },
                            onSettingsClick = { currentScreen = Screen.Settings }
                        )
                    }
                    Screen.RiwayatTransaksi -> RiwayatTransaksiScreen(
                        viewModel = transaksiViewModel,
                        token = token,
                        onBack = { currentScreen = Screen.Profile },
                        onDetailClick = { id ->
                            selectedTransaksiId = id
                            currentScreen = Screen.DetailTransaksi
                        }
                    )
                    Screen.MyWallet -> MyWalletScreen(
                        viewModel = profileViewModel,
                        transaksiViewModel = transaksiViewModel,
                        token = token,
                        onBack = { currentScreen = Screen.Profile },
                        onLogout = onLogout,
                        onMyRentalClick = { currentScreen = Screen.MyRental },
                        onRentalOwnerClick = { currentScreen = Screen.RentalsOwner },
                        onSettingsClick = { currentScreen = Screen.Settings }
                    )
                    Screen.Settings -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Settings Screen (Placeholder)")
                        }
                    }
                    Screen.DetailTransaksi -> selectedTransaksiId?.let { id ->
                        DetailTransaksiScreen(
                            transaksiId = id,
                            token = token,
                            viewModel = transaksiViewModel,
                            onBack = { currentScreen = Screen.RiwayatTransaksi },
                            onPayClick = { ids ->
                                selectedTransaksiIdsForPayment = ids
                                currentScreen = Screen.Pembayaran
                            },
                            onProductClick = { productId ->
                                val intent = Intent(context, DetailProdukActivity::class.java).apply {
                                    putExtra("EXTRA_PRODUCT_ID", productId)
                                }
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreenContent(
    katalogViewModel: KatalogViewModel,
    keranjangViewModel: KeranjangViewModel,
    token: String,
    onLogout: () -> Unit,
    onMenuClick: () -> Unit,
    onSeeAllRentals: () -> Unit,
    onProductClick: (Int) -> Unit
) {
    val katalogState by katalogViewModel.katalogPublik.observeAsState(Resource.Loading())
    val kategoriState by katalogViewModel.kategori.observeAsState(Resource.Loading())

    LaunchedEffect(Unit) {
        katalogViewModel.getKategori()
        katalogViewModel.getKatalogPublik(kategoriId = null)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HomeColors.Background)
            .verticalScroll(rememberScrollState())
    ) {
        HomeTopBar(onLogout = onLogout, onMenuClick = onMenuClick)
        HeroCollage(onSeeAllRentals)
        Spacer(modifier = Modifier.height(20.dp))
        SearchCard()
        Spacer(modifier = Modifier.height(36.dp))
        BrandLogosSection()
        Spacer(modifier = Modifier.height(36.dp))
        AboutUsSection()
        Spacer(modifier = Modifier.height(32.dp))
        // NOTE: sebelumnya FeaturesSection() didefinisikan tapi nggak pernah dipanggil di sini,
        // jadi 3 ikon "Wide Range / Easy & Fast Booking / 24/7 Support" hilang dari app.
        FeaturesSection()
        Spacer(modifier = Modifier.height(48.dp))
        CategoriesSection(kategoriState = kategoriState)
        Spacer(modifier = Modifier.height(48.dp))
        val context = LocalContext.current
        RentItemsSection(
            katalogState = katalogState,
            onProductClick = onProductClick,
            onSeeAllRentals = onSeeAllRentals,
            onAddToCart = { product ->
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val tglSewa = sdf.format(Calendar.getInstance().time)
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                val tglKembali = sdf.format(calendar.time)

                keranjangViewModel.addToKeranjang(
                    token = token,
                    barangId = product.id,
                    jumlah = 1,
                    tglSewa = tglSewa,
                    tglKembali = tglKembali
                )
            }
        )
        Spacer(modifier = Modifier.height(40.dp))
        TestimonialsSection()
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun HomeTopBar(onLogout: () -> Unit, onMenuClick: () -> Unit) {
    SewainTopBar()
}

@Composable
fun HeroCollage(onRentNow: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(330f / 680f)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.kamera), // ganti nama file
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(modifier = Modifier.weight(1.1f), horizontalAlignment = Alignment.CenterHorizontally) {
                // BOX ATAS TENGAH
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(402f / 155f)
                        .clip(RoundedCornerShape(6.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.camping), // ganti nama file
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text("ALL YOU", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = HomeColors.TextBody, letterSpacing = 0.5.sp)
                Text("can", fontSize = 16.sp, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Medium, color = HomeColors.Accent)
                Text(
                    text = "RENT",
                    style = TextStyle(
                        fontSize = 40.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp,
                        color = HomeColors.TextBody,
                        drawStyle = Stroke(width = 2.2f)
                    )
                )
                Spacer(Modifier.height(6.dp))
                Button(
                    onClick = onRentNow,
                    colors = ButtonDefaults.buttonColors(containerColor = HomeColors.AccentDeep),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Text("RENT NOW", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 0.5.sp)
                }
                Spacer(Modifier.height(8.dp))
                // BOX BAWAH TENGAH
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(402f / 155f)
                        .clip(RoundedCornerShape(6.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.motormobil), // ganti nama file
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(330f / 680f)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.produk_apple), // ganti nama file
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun SearchCard() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = HomeColors.Background),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            SearchFieldItem("Location", "Choose Location", Icons.Default.LocationOn)
            HorizontalDivider(color = HomeColors.Border, thickness = 0.5.dp)
            SearchFieldItem("Find", "Kamera", Icons.Default.Search)
            HorizontalDivider(color = HomeColors.Border, thickness = 0.5.dp)
            SearchFieldItem("Sewa Up", "17 July 2024", Icons.Default.CalendarMonth, true)
            HorizontalDivider(color = HomeColors.Border, thickness = 0.5.dp)
            SearchFieldItem("Return", "20 July 2024", Icons.Default.CalendarMonth, true)
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HomeColors.Accent)
            ) {
                Text("Search", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SearchFieldItem(label: String, value: String, icon: ImageVector, hasArrow: Boolean = false) {
    Column(modifier = Modifier.padding(vertical = 10.dp)) {
        Text(label, fontSize = 11.sp, color = HomeColors.TextMuted, fontWeight = FontWeight.Medium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, Modifier.size(16.dp), tint = HomeColors.IconStroke)
            Spacer(Modifier.width(8.dp))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = HomeColors.TextBody, modifier = Modifier.weight(1f))
            if (hasArrow) Icon(Icons.Default.KeyboardArrowDown, null, Modifier.size(18.dp), tint = HomeColors.TextBody)
        }
    }
}
@Composable
fun BrandLogosSection() {
    val brandLogos = listOf(
        Triple("FUJIFILM",   R.drawable.merk1, Pair(40.dp, 100.dp)),  // height, maxWidth
        Triple("BOSE",       R.drawable.merk2, Pair(100.dp, 200.dp)),
        Triple("Naturehike", R.drawable.merk3, Pair(100.dp, 200.dp)),
        Triple("Honda",      R.drawable.merk4, Pair(100.dp, 200.dp)),
        Triple("Apple",      R.drawable.merk5, Pair(100.dp, 200.dp)),
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        brandLogos.forEach { (name, resId, size) ->
            Image(
                painter = painterResource(id = resId),
                contentDescription = name,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .height(size.first)       // tinggi custom per logo
                    .widthIn(max = size.second) // lebar custom per logo
            )
        }
    }
}@Composable
fun AboutUsSection() {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("About us", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = HomeColors.TextDark, fontFamily = VolkhovFont)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "SEWAIN adalah Platform digital yang menyediakan layanan penyewaan berbagai kebutuhan sehari-hari dengan mudah, cepat, dan terpercaya. Kami hadir untuk membantu pengguna menemukan dan menyewa barang tanpa harus membeli, sehingga lebih praktis dan hemat.",
            fontSize = 13.sp, color = HomeColors.TextMuted, textAlign = TextAlign.Center, lineHeight = 21.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Dengan berbagai pilihan kategori seperti elektronik, kamera, perlengkapan outdoor, hingga kebutuhan event, SEWAIN menjadi solusi bagi siapa saja yang membutuhkan barang untuk penggunaan sementara.",
            fontSize = 13.sp, color = HomeColors.TextMuted, textAlign = TextAlign.Center, lineHeight = 21.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "SEWAIN — Sewa Mudah, Hidup Lebih Praktis.",
            fontSize = 13.sp, fontStyle = FontStyle.Italic, color = HomeColors.TextMuted, textAlign = TextAlign.Center
        )
    }
}

@Composable
fun FeaturesSection() {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
        FeatureItem(Icons.Default.Category, "Wide Range of Rentals", "Various items available across multiple categories")
        FeatureItem(Icons.Default.EventAvailable, "Easy & Fast Booking", "Rent items in just a few simple steps")
        FeatureItem(Icons.AutoMirrored.Filled.PhoneCallback, "24/7 Support", "We're here to help anytime you need")
    }
}

@Composable
private fun FeatureItem(icon: ImageVector, title: String, desc: String) {
    Column(modifier = Modifier.width(100.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(HomeColors.FeatureIcon), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(26.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(title, fontFamily = VolkhovFont, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = HomeColors.TextDark, textAlign = TextAlign.Center, lineHeight = 14.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(desc, fontFamily = RobotoFont, fontSize = 9.sp, color = HomeColors.TextBody, textAlign = TextAlign.Center, lineHeight = 12.sp)
    }
}

@Composable
fun CategoriesSection(kategoriState: Resource<KategoriListResponse>?) {
    val categories = (kategoriState as? Resource.Success)?.data?.data ?: emptyList()

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Categories", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = HomeColors.TextDark, fontFamily = VolkhovFont)
        Text("Find what you are looking for", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontSize = 12.sp, color = HomeColors.TextDark)
        Spacer(modifier = Modifier.height(24.dp))

        if (kategoriState is Resource.Loading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(30.dp), strokeWidth = 2.dp, color = HomeColors.Accent)
            }
        } else {
            Box(modifier = Modifier.fillMaxWidth().background(HomeColors.SoftGray)) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 28.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(categories) { category ->
                        CategoryCard(category.nama_kategori, Modifier.width(160.dp))
                    }
                }
            }
        }
    }
}

private fun categoryDescription(name: String): String = when (name.trim().lowercase()) {
    "photography" -> "Abadikan setiap momen indahmu dengan kamera berkualitas"
    "vehicles" -> "Sewa kendaraan impian untuk perjalanan tak terlupakan"
    "camping" -> "Petualangan outdoor jadi lebih mudah dengan perlengkapan camping"
    "fashion" -> "Temukan gaya terbaru dengan menyewa pakaian impian"
    "tools" -> "Peralatan berkualitas untuk kebutuhan sehari-hari"
    "electronics" -> "Alat elektronik berkualitas untuk kebutuhan sehari-hari"
    else -> "Temukan barang sewa terbaik di kategori ini"
}

private fun categoryIcon(name: String): ImageVector = when (name.trim().lowercase()) {
    "photography" -> Icons.Default.PhotoCamera
    "vehicles" -> Icons.Default.DirectionsCar
    "camping" -> Icons.Default.Cabin
    "fashion" -> Icons.Default.Checkroom
    "tools" -> Icons.Default.Build
    "electronics" -> Icons.Default.Devices
    else -> Icons.Default.Category
}
private fun categoryImage(name: String): Int = when (name.trim().lowercase()) {
    "photography" -> R.drawable.kategori_photography
    "vehicles"    -> R.drawable.kategori_vehicles
    "camping"     -> R.drawable.kategori_camping
    "fashion"     -> R.drawable.kategori_fashion
    "tools"       -> R.drawable.kategori_tools
    "electronics" -> R.drawable.kategori_electronic
    else          -> R.drawable.produk_apple
}
@Composable
private fun CategoryCard(name: String, modifier: Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(352f / 551f)
                .clip(RoundedCornerShape(8.dp))
        ) {
            Image(
                painter = painterResource(id = categoryImage(name)),
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = HomeColors.TextDark)
        Spacer(modifier = Modifier.height(4.dp))
        Text(categoryDescription(name), fontSize = 11.sp, color = HomeColors.TextDark, textAlign = TextAlign.Center, lineHeight = 15.sp)
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedButton(
            onClick = {},
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(1.dp, Color(0xFFD8D8D8)),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = HomeColors.TextBody),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text("Explore →", fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun RentItemsSection(
    katalogState: Resource<KatalogListResponse>?,
    onProductClick: (Int) -> Unit,
    onSeeAllRentals: () -> Unit,
    onAddToCart: (CatalogData) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Rent Items", fontFamily = VolkhovFont, fontSize = 26.sp, fontWeight = FontWeight.SemiBold, color = HomeColors.TextBody)
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            "Temukan berbagai barang pilihan yang siap mendukung aktivitasmu.\nProses sewa mudah, barang berkualitas, dan siap antar kapan saja.",
            fontSize = 12.sp, color = HomeColors.TextMuted, textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))

        when (katalogState) {
            is Resource.Loading -> {
                CircularProgressIndicator(color = HomeColors.Accent)
            }
            is Resource.Success -> {
                val itemsList = katalogState.data?.data?.take(6) ?: emptyList()
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(itemsList) { barang ->
                        HomeRentalCard(
                            barang = barang,
                            onProductClick = onProductClick,
                            onAddToCart = onAddToCart,
                            modifier = Modifier.width(220.dp)
                        )
                    }
                }
            }
            is Resource.Error -> {
                Text("Gagal memuat barang", color = Color.Red, fontSize = 12.sp)
            }
            null -> {}
        }

        Spacer(modifier = Modifier.height(28.dp))
        Button(
            onClick = onSeeAllRentals,
            colors = ButtonDefaults.buttonColors(containerColor = HomeColors.AccentDeep),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp)
        ) {
            Text("View More", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun HomeRentalCard(
    barang: CatalogData,
    onProductClick: (Int) -> Unit,
    onAddToCart: (CatalogData) -> Unit,
    modifier: Modifier = Modifier
) {
    val rating = 4
    val reviewCount = remember(barang.id) { (10..50).random() }

    Card(
        modifier = modifier.clickable { onProductClick(barang.id) },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = HomeColors.Background),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(386f / 240f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(HomeColors.ImgPlaceholder),
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
                    Icon(Icons.Default.Image, null, tint = Color.LightGray, modifier = Modifier.size(32.dp))
                }

                IconButton(
                    onClick = { onAddToCart(barang) },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .size(30.dp)
                        .background(HomeColors.Accent, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddShoppingCart,
                        contentDescription = "Add to Cart",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {

                Text(
                    barang.nama_barang,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = HomeColors.TextBody,
                    maxLines = 2,
                    lineHeight = 16.sp,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(4.dp))
                Text("★".repeat(rating) + "☆".repeat(5 - rating), color = HomeColors.Star, fontSize = 12.sp)
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, Modifier.size(11.dp), tint = HomeColors.IconStroke)
                Spacer(Modifier.width(3.dp))
                Text(barang.lokasi ?: "-", fontSize = 10.5.sp, color = HomeColors.TextMuted)
            }
            Text("($reviewCount) Customer Reviews", fontSize = 10.5.sp, color = HomeColors.TextBody)
            Spacer(Modifier.height(6.dp))
            Text("Rp ${CurrencyUtils.formatRupiah(barang.harga_sewa)}/hari", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = HomeColors.TextBody)
        }
    }
}

private data class HomeTestimonial(val name: String, val role: String, val stars: Int, val photoUrl: String, val text: String)

private val homeTestimonials = listOf(
    HomeTestimonial("Karen W.", "Fotografer", 5, "https://images.unsplash.com/photo-1531746020798-e6953c6e8e04?w=160&h=160&fit=crop", "Saya selalu kesini buat sewa kamera. Thanks ya for making it easier untuk dapat barang dengan kualitas top."),
    HomeTestimonial("James C.", "Mahasiswa", 5, "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=160&h=160&fit=crop", "Penyewaan ini sangatlah terpercaya dan aman, mulai dari pembayaran hingga barang sewaannya."),
    HomeTestimonial("Salwa C.", "Karyawan", 5, "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=160&h=160&fit=crop", "Prosesnya cepat dan barangnya sesuai foto. Akan sewa lagi pasti!"),
    HomeTestimonial("Dimas R.", "Event Organizer", 5, "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=160&h=160&fit=crop", "Saya pakai untuk acara kantor, semua peralatan tersedia. Sangat membantu untuk yang butuh dadakan."),
    HomeTestimonial("Aulia P.", "Content Creator", 4, "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=160&h=160&fit=crop", "Pertama kali nyobain, ternyata sangat user-friendly. Pemilik barangnya juga responsif lewat WhatsApp."),
    HomeTestimonial("Reza M.", "Pendaki", 5, "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=160&h=160&fit=crop", "Penyewaan tenda untuk camping akhir pekan kemarin lancar jaya. Harga juga lebih bersahabat dari toko sebelah.")
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TestimonialsSection() {
    val pagerState = rememberPagerState { homeTestimonials.size }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(HomeColors.TestiBg)
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "This Is What Our Customers Say",
            textAlign = TextAlign.Center, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, fontFamily = VolkhovFont,
            color = HomeColors.TextBody, modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Ratusan orang telah mempercayakan website SEWAIN sebagai tempat\npenyewaan terpercaya, praktis, & aman.",
            fontSize = 12.sp, color = HomeColors.TextMuted, textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 32.dp),
            pageSpacing = 12.dp
        ) { page ->
            val t = homeTestimonials[page]
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = HomeColors.Background),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(72.dp).clip(RoundedCornerShape(6.dp)).background(HomeColors.ImgPlaceholder)) {
                        AsyncImage(model = t.photoUrl, contentDescription = t.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("\"${t.text}\"", fontSize = 12.5.sp, textAlign = TextAlign.Center, fontStyle = FontStyle.Italic, lineHeight = 19.sp, color = HomeColors.TextBody)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("★".repeat(t.stars) + "☆".repeat(5 - t.stars), color = HomeColors.Star, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = HomeColors.Border, thickness = 0.5.dp, modifier = Modifier.fillMaxWidth(0.5f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(t.name, fontFamily = VolkhovFont, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = HomeColors.TextBody, )
                    Text(t.role, fontSize = 11.sp, color = HomeColors.TextBody)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            homeTestimonials.indices.forEach { i ->
                Box(
                    modifier = Modifier
                        .size(if (i == pagerState.currentPage) 7.dp else 5.dp)
                        .clip(CircleShape)
                        .background(if (i == pagerState.currentPage) HomeColors.Accent else HomeColors.Border)
                )
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
            .navigationBarsPadding()
            .height(80.dp)
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
    ) {
        NavigationBarItem(
            selected = currentScreen == Screen.Home,
            onClick = { onScreenSelected(Screen.Home) },
            icon = { Icon(Icons.Default.Home, null) },
            label = { Text("Home", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = HomeColors.Accent, unselectedIconColor = Color.Gray, selectedTextColor = HomeColors.Accent, unselectedTextColor = Color.Gray, indicatorColor = Color.Transparent)
        )
        NavigationBarItem(
            selected = currentScreen == Screen.Rental,
            onClick = { onScreenSelected(Screen.Rental) },
            icon = { Icon(Icons.Default.Search, null) },
            label = { Text("Rental", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = HomeColors.Accent, unselectedIconColor = Color.Gray, selectedTextColor = HomeColors.Accent, unselectedTextColor = Color.Gray, indicatorColor = Color.Transparent)
        )
        NavigationBarItem(
            selected = currentScreen == Screen.Keranjang,
            onClick = { onScreenSelected(Screen.Keranjang) },
            icon = { Icon(Icons.Default.ShoppingCart, null) },
            label = { Text("Keranjang", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = HomeColors.Accent, unselectedIconColor = Color.Gray, selectedTextColor = HomeColors.Accent, unselectedTextColor = Color.Gray, indicatorColor = Color.Transparent)
        )
        NavigationBarItem(
            selected = currentScreen == Screen.MyKatalog,
            onClick = { onScreenSelected(Screen.MyKatalog) },
            icon = { Icon(Icons.Default.Store, null) },
            label = { Text("My Katalog", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = HomeColors.Accent, unselectedIconColor = Color.Gray, selectedTextColor = HomeColors.Accent, unselectedTextColor = Color.Gray, indicatorColor = Color.Transparent)
        )
        NavigationBarItem(
            selected = currentScreen == Screen.Profile || currentScreen == Screen.RiwayatTransaksi || currentScreen == Screen.DetailTransaksi || currentScreen == Screen.Pembayaran,
            onClick = { onScreenSelected(Screen.Profile) },
            icon = { Icon(Icons.Default.Person, null) },
            label = { Text("Me", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = HomeColors.Accent, unselectedIconColor = Color.Gray, selectedTextColor = HomeColors.Accent, unselectedTextColor = Color.Gray, indicatorColor = Color.Transparent)
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 3000)
@Composable
fun HomeScreenPreview() {
    Sewain_rplTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(HomeColors.Background)
                .verticalScroll(rememberScrollState())
        ) {
            HomeTopBar(onLogout = {}, onMenuClick = {})
            HeroCollage(onRentNow = {})
            Spacer(modifier = Modifier.height(20.dp))
            SearchCard()
            Spacer(modifier = Modifier.height(36.dp))
            BrandLogosSection()
            Spacer(modifier = Modifier.height(36.dp))
            AboutUsSection()
            Spacer(modifier = Modifier.height(32.dp))
            FeaturesSection()
            Spacer(modifier = Modifier.height(48.dp))

            // ✅ Categories pakai dummy data
            val dummyKategori = Resource.Success(
                KategoriListResponse(
                    status = "success",
                    data = listOf(
                        com.l0124005.sewain_rpl.network.KategoriData(id = 1, nama_kategori = "Photography", deskripsi = null),
                        com.l0124005.sewain_rpl.network.KategoriData(id = 2, nama_kategori = "Vehicles", deskripsi = null),
                        com.l0124005.sewain_rpl.network.KategoriData(id = 3, nama_kategori = "Camping", deskripsi = null),
                        com.l0124005.sewain_rpl.network.KategoriData(id = 4, nama_kategori = "Electronics", deskripsi = null),
                    )
                )
            )
            CategoriesSection(kategoriState = dummyKategori)

            Spacer(modifier = Modifier.height(48.dp))

            // ✅ Rent Items pakai dummy data
            val dummyKatalog = Resource.Success(
                KatalogListResponse(
                    status = "success",
                    data = listOf(
                        CatalogData(id = 1, user_id = 1, kategori_id = 1, nama_barang = "Kamera Canon EOS", deskripsi = "", harga_sewa = 150000.0, harga_jaminan = 0.0, harga_denda_perjam = 0.0, stok = 1, lokasi = "Surakarta", foto_barang = null, status = "tersedia"),
                        CatalogData(id = 2, user_id = 1, kategori_id = 3, nama_barang = "Tenda Camping", deskripsi = "", harga_sewa = 80000.0, harga_jaminan = 0.0, harga_denda_perjam = 0.0, stok = 2, lokasi = "Solo", foto_barang = null, status = "tersedia"),
                        CatalogData(id = 3, user_id = 1, kategori_id = 2, nama_barang = "Motor Matic", deskripsi = "", harga_sewa = 100000.0, harga_jaminan = 0.0, harga_denda_perjam = 0.0, stok = 1, lokasi = "Surakarta", foto_barang = null, status = "tersedia"),
                    )
                )
            )
            RentItemsSection(
                katalogState = dummyKatalog,
                onProductClick = {},
                onSeeAllRentals = {},
                onAddToCart = {}
            )

            Spacer(modifier = Modifier.height(40.dp))
            TestimonialsSection()
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
