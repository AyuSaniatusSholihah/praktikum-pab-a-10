package com.l0124005.sewain_rpl.ui.theme.katalog

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.l0124005.sewain_rpl.network.ApiClient
import com.l0124005.sewain_rpl.repository.KatalogRepository
import com.l0124005.sewain_rpl.repository.KeranjangRepository
import com.l0124005.sewain_rpl.repository.TransaksiRepository
import com.l0124005.sewain_rpl.ui.theme.MainActivity
import com.l0124005.sewain_rpl.ui.theme.SewainTopBar
import com.l0124005.sewain_rpl.ui.theme.Sewain_rplTheme
import com.l0124005.sewain_rpl.utils.CurrencyUtils
import com.l0124005.sewain_rpl.utils.DateUtils
import com.l0124005.sewain_rpl.utils.Resource
import com.l0124005.sewain_rpl.utils.SessionManager
import com.l0124005.sewain_rpl.viewmodel.KatalogViewModel
import com.l0124005.sewain_rpl.viewmodel.KatalogViewModelFactory
import com.l0124005.sewain_rpl.viewmodel.KeranjangViewModel
import com.l0124005.sewain_rpl.viewmodel.KeranjangViewModelFactory
import com.l0124005.sewain_rpl.viewmodel.TransaksiViewModel
import com.l0124005.sewain_rpl.viewmodel.TransaksiViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch
import androidx.compose.ui.tooling.preview.Preview
import com.l0124005.sewain_rpl.network.CatalogData
import com.l0124005.sewain_rpl.network.KategoriData
import com.l0124005.sewain_rpl.ui.theme.VolkhovFont
import com.l0124005.sewain_rpl.ui.theme.MonsterratFont
import com.l0124005.sewain_rpl.ui.theme.LatoFont
import com.l0124005.sewain_rpl.ui.theme.JostFont
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.l0124005.sewain_rpl.R
// ============================================================
// WARNA TEMA — SEWAIN (disamakan dengan brand colors)
// ============================================================
object DetailColors {
    val Primary      = com.l0124005.sewain_rpl.ui.theme.BluePrimary
    val Black        = com.l0124005.sewain_rpl.ui.theme.katalog.Black
    val White        = com.l0124005.sewain_rpl.ui.theme.katalog.White
    val Surface      = Color(0xFFEDEDED)
    val Border       = Color(0xFFE4E4E4)
    val TextMuted    = com.l0124005.sewain_rpl.ui.theme.katalog.TextMuted
    val TextDark     = com.l0124005.sewain_rpl.ui.theme.katalog.TextDark
    val Star         = Color(0xFFF5B800)
    val StockRed     = Color(0xFFD8262C)
    val PriceOld     = Color(0xFF666666)
    val InfoBg       = Color(0xFFF5F5F5)
    val Accent       = Color(0xFF6A87A1)   // disamakan .btn-rent-now di product.css
}

data class DetailSpecItem(val label: String, val value: String)
data class DetailReview(val nama: String, val tanggal: String, val rating: Int, val komentar: String)

class DetailProdukActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val productId = intent.getIntExtra("EXTRA_PRODUCT_ID", -1)

        setContent {
            Sewain_rplTheme {
                val sessionManager = SessionManager(this)
                val token = sessionManager.getToken()

                val katalogViewModel: KatalogViewModel = viewModel(
                    factory = KatalogViewModelFactory(KatalogRepository())
                )
                val keranjangViewModel: KeranjangViewModel = viewModel(
                    factory = KeranjangViewModelFactory(KeranjangRepository())
                )
                val transaksiViewModel: TransaksiViewModel = viewModel(
                    factory = TransaksiViewModelFactory(TransaksiRepository())
                )

                ProductDetailScreen(
                    productId = productId,
                    token = token,
                    katalogViewModel = katalogViewModel,
                    keranjangViewModel = keranjangViewModel,
                    transaksiViewModel = transaksiViewModel,
                    onBack = { finish() },
                    onNavigateToCart = {
                        val intent = Intent(this, MainActivity::class.java).apply {
                            putExtra("TARGET_SCREEN", "KERANJANG")
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        }
                        startActivity(intent)
                        finish()
                    },
                    onNavigateToCheckout = { productIdToCheckout ->
                        val intent = Intent(this, MainActivity::class.java).apply {
                            putExtra("TARGET_SCREEN", "CHECKOUT")
                            putExtra("PRODUCT_ID", productIdToCheckout)
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        }
                        startActivity(intent)
                        finish()
                    },
                    onNavigateToTransactions = {
                        val intent = Intent(this, MainActivity::class.java).apply {
                            putExtra("TARGET_SCREEN", "TRANSAKSI")
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        }
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: Int,
    token: String,
    katalogViewModel: KatalogViewModel,
    keranjangViewModel: KeranjangViewModel,
    transaksiViewModel: TransaksiViewModel,
    onBack: () -> Unit = {},
    onNavigateToCart: () -> Unit = {},
    onNavigateToCheckout: (Int) -> Unit = {},
    onNavigateToTransactions: () -> Unit = {}
) {
    val context = LocalContext.current
    val detailState by katalogViewModel.myKatalogDetail.observeAsState()
    val addToCartState by keranjangViewModel.addToCartState.observeAsState()
    val checkoutState by transaksiViewModel.checkoutState.observeAsState()

    var isRentNowClicked by remember { mutableStateOf(false) }
    var selectedThumb by remember { mutableStateOf(0) }
    var quantity      by remember { mutableStateOf(1) }
    var selectedTab   by remember { mutableStateOf(0) }

    var dateStartRaw by remember { mutableStateOf(Date()) }
    var dateEndRaw by remember { mutableStateOf(Date(System.currentTimeMillis() + 86400000)) }

    var showDatePickerStart by remember { mutableStateOf(false) }
    var showDatePickerEnd by remember { mutableStateOf(false) }

    var fallbackTried by remember { mutableStateOf(false) }

    LaunchedEffect(productId) {
        if (productId != -1) {
            Log.d("DetailProduk", "Mencoba ambil detail ID: $productId melalui katalog-publik")
            fallbackTried = false
            katalogViewModel.resetStates()
            katalogViewModel.getKatalogPublikDetail(productId)
            // Reset selected thumbnail ke awal saat ganti produk
            selectedThumb = 0
        } else {
            Log.e("DetailProduk", "ID Produk tidak valid: $productId")
        }
    }

    // Listener untuk menangani 404 atau error lainnya
    LaunchedEffect(detailState) {
        val state = detailState
        if (state is Resource.Error) {
            Log.e("DetailProduk", "Error terdeteksi: Code=${state.code}, Message=${state.message}")
            
            // Jika 404 di publik, coba jalur privat (katalog/{id})
            if (state.code == 404 && !fallbackTried) {
                Log.d("DetailProduk", "404 Not Found di publik. Mencoba fallback ke getMyKatalogDetail dengan ID: $productId")
                fallbackTried = true
                katalogViewModel.getMyKatalogDetail(token, productId)
            }
        } else if (state is Resource.Success) {
            Log.d("DetailProduk", "Detail produk berhasil dimuat untuk ID: $productId")
            
            // Sync tanggal dari data backend ke state UI (dateStartRaw & dateEndRaw)
            state.data?.data?.let { p ->
                try {
                    p.tanggal_mulai?.let { startStr ->
                        DateUtils.backendStringToDate(startStr)?.let { dateStartRaw = it }
                    }
                    p.tanggal_akhir?.let { endStr ->
                        DateUtils.backendStringToDate(endStr)?.let { dateEndRaw = it }
                    }
                } catch (e: Exception) {
                    Log.e("DetailProduk", "Gagal parsing tanggal dari backend: ${e.message}")
                }
            }
        }
    }

    LaunchedEffect(addToCartState) {
        if (addToCartState is Resource.Success) {
            if (isRentNowClicked) {
                // Navigasi ke CheckoutPayment dengan productId agar hanya produk ini yang di-checkout
                isRentNowClicked = false
                keranjangViewModel.resetActionStates()
                onNavigateToCheckout(productId)
            } else {
                Toast.makeText(context, "Berhasil ditambahkan ke keranjang!", Toast.LENGTH_SHORT).show()
                keranjangViewModel.resetActionStates()
                onNavigateToCart()
            }
        } else if (addToCartState is Resource.Error) {
            Toast.makeText(context, "Gagal: ${addToCartState?.message}", Toast.LENGTH_SHORT).show()
            keranjangViewModel.resetActionStates()
        }
    }

    if (showDatePickerStart) {
        SewainDatePickerDialog(
            title = "Pilih Tanggal Mulai",
            onDismiss = { showDatePickerStart = false },
            onDateSelected = { dateStr ->
                DateUtils.backendStringToDate(DateUtils.formatDateForBackend(dateStr))?.let { dateStartRaw = it }
                showDatePickerStart = false
            }
        )
    }

    if (showDatePickerEnd) {
        SewainDatePickerDialog(
            title = "Pilih Tanggal Selesai",
            onDismiss = { showDatePickerEnd = false },
            onDateSelected = { dateStr ->
                DateUtils.backendStringToDate(DateUtils.formatDateForBackend(dateStr))?.let { dateEndRaw = it }
                showDatePickerEnd = false
            }
        )
    }

    Scaffold(
        topBar = {
            DetailTopBar(
                onBack = onBack,
                onCartClick = onNavigateToCart
            )
        },
        bottomBar = {
            if (detailState is Resource.Success) {
                val isLoading = addToCartState is Resource.Loading || checkoutState is Resource.Loading
                DetailBottomActionBar(
                    onAddToCart = {
                        isRentNowClicked = false
                        keranjangViewModel.addToKeranjang(
                            token, productId, quantity,
                            DateUtils.dateToBackendString(dateStartRaw),
                            DateUtils.dateToBackendString(dateEndRaw)
                        )
                    },
                    onRentNow = {
                        isRentNowClicked = true
                        keranjangViewModel.addToKeranjang(
                            token, productId, quantity,
                            DateUtils.dateToBackendString(dateStartRaw),
                            DateUtils.dateToBackendString(dateEndRaw)
                        )
                    },
                    isLoading = isLoading
                )
            }
        },
        containerColor = DetailColors.White
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (detailState) {
                is Resource.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = DetailColors.Primary)
                }
                is Resource.Success -> {
                    val product = detailState!!.data!!.data
                    
                    // Mengambil semua foto dari helper listFoto di model
                    val photos = remember(product) {
                        product.listFoto.map { product.getFullUrl(it) }
                    }

                    val tabs = listOf("Deskripsi", "Info Tambahan", "Ulasan")

                    // TODO: kalau backend kamu sudah punya field harga sebelum diskon
                    // (misalnya product.harga_lama), pakai itu di sini. Untuk sementara
                    // dihitung otomatis 10% di atas harga sewa, cuma biar tampilan harga
                    // coret-nya kebentuk -- bukan data asli, ganti begitu field-nya ada.
                    val hargaLama = (product.harga_sewa * 1.1).toLong()

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        DetailMainImageSection(
                            photos = photos,
                            selectedIndex = selectedThumb,
                            onThumbSelected = { selectedThumb = it }
                        )

                        Spacer(Modifier.height(20.dp))

                        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                            Text(
                                text = (product.kategori?.nama_kategori ?: "SEWAIN").uppercase(Locale.getDefault()),
                                fontFamily = VolkhovFont,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                letterSpacing = 2.sp,
                                color = DetailColors.TextMuted
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = product.nama_barang,
                                fontFamily = VolkhovFont,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 24.sp,
                                color = DetailColors.Black,
                                lineHeight = 32.sp
                            )

                            Spacer(Modifier.height(10.dp))

                            DetailRatingRow(rating = 4.5f, reviewCount = 0)

                            Spacer(Modifier.height(8.dp))

                            DetailViewingNowRow()

                            Spacer(Modifier.height(14.dp))

                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "Rp ${CurrencyUtils.formatRupiah(product.harga_sewa)}/hari",
                                    fontFamily = VolkhovFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                    color = DetailColors.Black
                                )
                                if (hargaLama > product.harga_sewa) {
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        text = "Rp ${CurrencyUtils.formatRupiah(hargaLama)}",
                                        fontSize = 14.sp,
                                        fontFamily = VolkhovFont,
                                        color = DetailColors.PriceOld,
                                        textDecoration = TextDecoration.LineThrough,
                                        modifier = Modifier.padding(bottom = 3.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.height(22.dp))

                            DetailDateSelectorRow(
                                dateStart = DateUtils.dateToUiString(dateStartRaw),
                                dateEnd = DateUtils.dateToUiString(dateEndRaw),
                                onDateStartClick = { showDatePickerStart = true },
                                onDateEndClick   = { showDatePickerEnd = true }
                            )

                            Spacer(Modifier.height(20.dp))

                            DetailStockSection(stock = product.stok)

                            Spacer(Modifier.height(20.dp))

                            DetailQuantitySection(
                                qty      = quantity,
                                maxStock = product.stok,
                                onMinus  = { if (quantity > 1) quantity-- },
                                onPlus   = { if (quantity < product.stok) quantity++ }
                            )

                            Spacer(Modifier.height(24.dp))

                            DetailActionLinks()

                            Spacer(Modifier.height(20.dp))

                            HorizontalDivider(color = DetailColors.Border, thickness = 0.5.dp)
                            Spacer(Modifier.height(16.dp))

                            DetailInfoItems()

                            Spacer(Modifier.height(20.dp))

                            DetailPaymentBox()

                            Spacer(Modifier.height(28.dp))

                            DetailTabSection(
                                tabs        = tabs,
                                selectedTab = selectedTab,
                                onTabChange = { selectedTab = it }
                            )

                            Spacer(Modifier.height(16.dp))

                            when (selectedTab) {
                                0 -> Text(
                                    text = product.deskripsi ?: "Tidak ada deskripsi.",
                                    fontFamily = LatoFont, fontSize = 14.sp, color = DetailColors.Black, lineHeight = 22.sp, textAlign = TextAlign.Justify
                                )
                                1 -> {
                                    val specs = mutableListOf(
                                        DetailSpecItem("Lokasi Barang", product.lokasi ?: "-"),
                                        DetailSpecItem("Uang Jaminan", "Rp ${CurrencyUtils.formatRupiah(product.harga_jaminan)}"),
                                        DetailSpecItem("Denda Keterlambatan", "Rp ${CurrencyUtils.formatRupiah(product.harga_denda_perjam)}/jam"),
                                        DetailSpecItem("Kategori", product.kategori?.nama_kategori ?: "Umum")
                                    )
                                    
                                    if (!product.additional_information.isNullOrBlank()) {
                                        specs.add(DetailSpecItem("Info Tambahan", product.additional_information))
                                    }

                                    if (!product.whatsapp.isNullOrBlank()) {
                                        specs.add(DetailSpecItem("WhatsApp", product.whatsapp))
                                    }

                                    DetailAdditionalInfoContent(
                                        specs = specs,
                                        ownerName = product.user?.name ?: "Owner #${product.user_id}",
                                        ownerDenda = "Rp ${CurrencyUtils.formatRupiah(product.harga_denda_perjam)}"
                                    )
                                }
                                2 -> DetailReviewSection(reviews = emptyList())
                            }

                            Spacer(Modifier.height(32.dp))
                        }
                    }
                }
                is Resource.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Error: ${detailState?.message}", color = Color.Red)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = {
                            if (productId != -1) {
                                fallbackTried = false
                                katalogViewModel.resetStates()
                                katalogViewModel.getKatalogPublikDetail(productId)
                            }
                        }) {
                            Text("Coba Lagi")
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailTopBar(onBack: () -> Unit, onCartClick: () -> Unit) {
    SewainTopBar(
        navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
        onNavigationClick = onBack,
        actionIcon = Icons.Outlined.ShoppingCart,
        onActionClick = onCartClick
    )
}

// ============================================================
// GALERI FOTO -- bisa di-swipe (HorizontalPager) + thumbnail.
// Butuh androidx.compose.foundation versi 1.6+ untuk HorizontalPager
// di package androidx.compose.foundation.pager (bukan accompanist).
// ============================================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DetailMainImageSection(photos: List<String>, selectedIndex: Int, onThumbSelected: (Int) -> Unit) {
    // Gunakan key(photos) agar PagerState di-reset total saat daftar foto berubah (dari kosong ke berisi)
    key(photos) {
        val pageCount = if (photos.isEmpty()) 1 else photos.size
        val pagerState = rememberPagerState(initialPage = selectedIndex.coerceIn(0, pageCount - 1)) { pageCount }
        val scope = rememberCoroutineScope()

        // Sync pager -> external state (saat swipe)
        LaunchedEffect(pagerState.currentPage) {
            if (photos.isNotEmpty() && pagerState.currentPage != selectedIndex) {
                onThumbSelected(pagerState.currentPage)
            }
        }

        // Sync external state -> pager (saat klik thumbnail)
        LaunchedEffect(selectedIndex) {
            if (photos.isNotEmpty() && pagerState.currentPage != selectedIndex) {
                pagerState.animateScrollToPage(selectedIndex)
            }
        }

        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DetailColors.Surface)
            ) {
                if (photos.isEmpty()) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp).align(Alignment.Center),
                        tint = Color.LightGray
                    )
                } else {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        pageSpacing = 16.dp
                    ) { page ->
                        AsyncImage(
                            model = photos[page],
                            contentDescription = "Foto produk ${page + 1}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    
                    // Dot Indicators
                    if (photos.size > 1) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 12.dp)
                                .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            photos.indices.forEach { i ->
                                val isActive = i == pagerState.currentPage
                                Box(
                                    modifier = Modifier
                                        .size(if (isActive) 8.dp else 6.dp)
                                        .clip(CircleShape)
                                        .background(if (isActive) Color.White else Color.White.copy(alpha = 0.5f))
                                )
                            }
                        }
                    }
                }
            }

            // Thumbnails (Horizontal Scroll)
            if (photos.size > 1) {
                Spacer(Modifier.height(12.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(photos) { i, url ->
                        val isActive = i == pagerState.currentPage
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = if (isActive) 2.dp else 1.dp,
                                    color = if (isActive) DetailColors.Primary else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    // Cukup update selectedIndex, LaunchedEffect di DetailMainImageSection akan handle animasinya
                                    onThumbSelected(i)
                                }
                                .background(DetailColors.Surface)
                        ) {
                            AsyncImage(
                                model = url,
                                contentDescription = "Thumbnail ${i + 1}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRatingRow(rating: Float, reviewCount: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        val fullStars = rating.toInt()
        repeat(fullStars) { Text("★", color = DetailColors.Star, fontSize = 20.sp) }
        if (fullStars < 5) { Text("☆", color = DetailColors.Star, fontSize = 20.sp) }
        Spacer(Modifier.width(6.dp))
        Text(text = "($reviewCount)", fontSize = 14.sp, color = DetailColors.TextDark)
    }
}

// "X orang sedang melihat ini" -- samain sama .viewing-now di product.css
@Composable
fun DetailViewingNowRow() {
    val viewerCount = remember { (5..24).random() }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Outlined.Visibility,
            contentDescription = null,
            tint = DetailColors.TextMuted,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(text = "$viewerCount orang sedang melihat ini", fontSize = 12.sp, color = DetailColors.TextMuted)
    }
}

@Composable
fun DetailDateSelectorRow(dateStart: String, dateEnd: String, onDateStartClick: () -> Unit, onDateEndClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        DetailDateBox(label = "Tanggal Mulai", value = dateStart, onClick = onDateStartClick, modifier = Modifier.weight(1f))
        DetailDateBox(label = "Tanggal Selesai", value = dateEnd, onClick = onDateEndClick, modifier = Modifier.weight(1f))
    }
}

@Composable
fun DetailDateBox(label: String, value: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.clip(RoundedCornerShape(8.dp)).border(0.5.dp, DetailColors.Border, RoundedCornerShape(8.dp)).clickable(onClick = onClick).padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontWeight = FontWeight.Bold, fontFamily = LatoFont, fontSize = 12.sp, color = DetailColors.Black)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.CalendarToday, null, tint = DetailColors.Black, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(text = value, fontFamily = LatoFont, fontSize = 12.sp, color = DetailColors.Black)
        }
    }
}

@Composable
fun DetailStockSection(stock: Int) {
    Column {
        Text(text = buildAnnotatedString {
            append("Only ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("$stock") }
            append(" item(s) left in stock!")
        }, fontFamily = JostFont, fontSize = 14.sp, color = DetailColors.PriceOld)
        Spacer(Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(DetailColors.Border)) {
            val fraction = if (stock > 0) 1f else 0f
            Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(fraction).clip(RoundedCornerShape(2.dp)).background(if (stock < 3) DetailColors.StockRed else DetailColors.Primary))
        }
    }
}

@Composable
fun DetailQuantitySection(qty: Int, maxStock: Int, onMinus: () -> Unit, onPlus: () -> Unit) {
    Column {
        Text(text = "Jumlah", fontFamily = Volkhov, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = DetailColors.Black)
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.border(1.dp, DetailColors.Border, RoundedCornerShape(6.dp)), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onMinus, modifier = Modifier.size(40.dp)) { Text("−", fontSize = 18.sp, color = DetailColors.TextDark) }
                Text(text = "$qty", modifier = Modifier.defaultMinSize(minWidth = 32.dp), textAlign = TextAlign.Center, fontSize = 15.sp, color = DetailColors.Black)
                IconButton(onClick = onPlus, modifier = Modifier.size(40.dp)) { Text("+", fontSize = 18.sp, color = DetailColors.TextDark) }
            }
            Text(text = "/ $maxStock tersedia", fontSize = 12.sp, color = DetailColors.TextMuted)
        }
    }
}

@Composable
fun DetailActionLinks() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        DetailActionLinkItem(icon = Icons.AutoMirrored.Outlined.CompareArrows, label = "Compare", onClick = {})
        DetailActionLinkItem(icon = Icons.AutoMirrored.Outlined.HelpOutline, label = "Ask a Question", onClick = {})
        DetailActionLinkItem(icon = Icons.Outlined.Share, label = "Share", onClick = {})
    }
}

@Composable
fun DetailActionLinkItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Row(modifier = Modifier.clickable(onClick = onClick), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = label, tint = DetailColors.Black, modifier = Modifier.size(17.dp))
        Spacer(Modifier.width(5.dp))
        Text(text = label, fontSize = 13.5.sp, color = DetailColors.Black)
    }
}

@Composable
fun DetailInfoItems() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        DetailInfoRow(icon = Icons.Outlined.CalendarToday, boldText = "Return Terms:", text = "Wajib dikembalikan dalam keadaan bersih & tepat waktu")
        DetailInfoRow(icon = Icons.Outlined.Shield, boldText = "Guarantee Policy:", text = "Dokumen pendukung valid saat pengambilan")
        DetailInfoRow(icon = Icons.Outlined.Warning, boldText = "Late Fee Applied for Delays", text = "")
    }
}

@Composable
fun DetailInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, boldText: String, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, null, tint = DetailColors.Black, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(text = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold, fontFamily = VolkhovFont, fontSize = 14.sp)) { append(boldText) }
            if (text.isNotEmpty()) {
                withStyle(SpanStyle(fontFamily = JostFont)) { append(" $text") }
            }
        }, fontSize = 13.sp, color = DetailColors.Black)
    }
}
@Composable
fun DetailPaymentBox() {
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(DetailColors.Surface).padding(14.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            FlowRowPayment(
                methods = listOf(
                    R.drawable.visa,
                    R.drawable.jcb,
                    R.drawable.american,
                    R.drawable.dinner,
                    R.drawable.master,
                    R.drawable.mandiri,
                    R.drawable.bca,
                    R.drawable.bri,
                    R.drawable.bni,
                    R.drawable.bsi,
                    R.drawable.gopay,
                    R.drawable.ovo,
                    R.drawable.dana,
                    R.drawable.spay,

                )
            )
            Spacer(Modifier.height(8.dp))
            Text(text = "Guarantee safe & secure checkout", fontFamily = VolkhovFont, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = DetailColors.Black, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun FlowRowPayment(methods: List<Int>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        methods.chunked(5).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { logoRes ->
                    Box(
                        modifier = Modifier
                            .size(width = 48.dp, height = 28.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .border(0.5.dp, DetailColors.Border, RoundedCornerShape(4.dp))
                            .background(DetailColors.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = logoRes),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize().padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailTabSection(tabs: List<String>, selectedTab: Int, onTabChange: (Int) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().border(width = 0.5.dp, color = DetailColors.Border, shape = RoundedCornerShape(0.dp)), horizontalArrangement = Arrangement.SpaceEvenly) {
        tabs.forEachIndexed { i, tab ->
            val isActive = i == selectedTab
            Box(modifier = Modifier.weight(1f).clickable { onTabChange(i) }.padding(vertical = 12.dp).drawBehind {
                if (isActive) { drawLine(color = DetailColors.Black, start = Offset(0f, size.height), end = Offset(size.width, size.height), strokeWidth = 2.dp.toPx()) }
            }, contentAlignment = Alignment.Center) {
                // Di product.css semua tab bold (700), yang beda cuma warnanya -- bukan ketebalannya
                Text(
                    text = tab,
                    fontFamily = MonsterratFont,
                    fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Normal,
                    fontSize = 14.sp,
                    color = if (isActive) DetailColors.Black else DetailColors.TextMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun DetailAdditionalInfoContent(specs: List<DetailSpecItem>, ownerName: String, ownerDenda: String) {
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        specs.forEachIndexed { i, spec ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
                    .let { 
                        if (i > 0) it.drawBehind { 
                            drawLine(Color(0xFFF0F0F0), Offset(0f, 0f), Offset(size.width, 0f), 1.dp.toPx()) 
                        } else it 
                    }
            ) {
                Text(text = spec.label, fontSize = 12.sp, color = DetailColors.TextMuted, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = spec.value, 
                    fontWeight = FontWeight.Medium, 
                    fontSize = 14.sp, 
                    color = DetailColors.Black,
                    lineHeight = 20.sp
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(DetailColors.InfoBg).padding(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = buildAnnotatedString { withStyle(SpanStyle(fontFamily = Volkhov, fontWeight = FontWeight.Bold, fontSize = 14.sp)) { append("Pemilik: ") }; append(ownerName) }, color = DetailColors.Black)
                Text(text = buildAnnotatedString { withStyle(SpanStyle(fontFamily = Volkhov, fontWeight = FontWeight.Bold, fontSize = 14.sp)) { append("Denda Per Jam: ") }; append(ownerDenda) }, color = DetailColors.Black)
            }
        }
    }
}

// Tab "Ulasan" -- gaya kartu ulasan disamakan dengan tabs.rev di product_detail.html
// (avatar bulat, nama, tanggal, bintang, komentar). Diisi list kosong dulu karena
// belum ada endpoint review; begitu API-nya ada tinggal isi `reviews`.
@Composable
fun DetailReviewSection(reviews: List<DetailReview>) {
    if (reviews.isEmpty()) {
        Text("Belum ada ulasan.", modifier = Modifier.padding(vertical = 16.dp), color = DetailColors.TextMuted)
        return
    }
    Column {
        reviews.forEachIndexed { i, review ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp)
                    .let {
                        if (i > 0) it.drawBehind {
                            drawLine(Color(0xFFF0F0F0), Offset(0f, 0f), Offset(size.width, 0f), 1.dp.toPx())
                        } else it
                    }
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(DetailColors.Surface),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = review.nama.take(1).uppercase(),
                        fontFamily = Volkhov,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = DetailColors.TextDark
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = review.nama, fontFamily = Volkhov, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = DetailColors.Black)
                            Text(text = "Penyewa Terverifikasi", fontSize = 11.sp, color = DetailColors.TextMuted)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = review.tanggal, fontSize = 11.sp, color = DetailColors.TextMuted)
                            Row {
                                repeat(review.rating) { Text("★", color = DetailColors.Star, fontSize = 13.sp) }
                                repeat(5 - review.rating) { Text("☆", color = DetailColors.Star, fontSize = 13.sp) }
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(text = review.komentar, fontSize = 12.5.sp, color = DetailColors.Black, lineHeight = 18.sp)
                }
            }
        }
    }
}
@Composable
fun DetailBottomActionBar(onAddToCart: () -> Unit, onRentNow: () -> Unit, isLoading: Boolean) {
    Surface(shadowElevation = 8.dp, color = DetailColors.White) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onAddToCart,
                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, DetailColors.Black),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DetailColors.TextDark),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                enabled = !isLoading
            ) {
                Text(
                    text = "Add To Cart",
                    fontFamily = VolkhovFont,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Visible,
                    textAlign = TextAlign.Center
                )
            }
            Button(
                onClick = onRentNow,
                modifier = Modifier.weight(2f).heightIn(min = 48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DetailColors.Accent,
                    contentColor = DetailColors.Black
                ),
                border = BorderStroke(0.5.dp, DetailColors.Black),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = DetailColors.Black)
                } else {
                    Text(text = "Rent Now!", fontFamily = VolkhovFont, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, heightDp = 100000, widthDp = 480)
@Composable
fun DetailProdukPreview() {
    // Dummy foto biar galeri swipe-nya kelihatan di preview (URL contoh, bukan data asli)
    val dummyPhotos = listOf(
        "https://images.example.com/tenda-1.jpg",
        "https://images.example.com/tenda-2.jpg",
        "https://images.example.com/tenda-3.jpg"
    )
    var selectedThumb by remember { mutableStateOf(0) }

    Sewain_rplTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(DetailColors.White)
        ) {
            // TOP BAR
            DetailTopBar(onBack = {}, onCartClick = {})

            // GAMBAR UTAMA -- bisa di-swipe + indikator titik + thumbnail
            DetailMainImageSection(
                photos = dummyPhotos,
                selectedIndex = selectedThumb,
                onThumbSelected = { selectedThumb = it }
            )

            Spacer(Modifier.height(20.dp))

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                // Kategori & Nama
                Text(
                    text = "CAMPING",
                    fontFamily = Volkhov,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    letterSpacing = 2.sp,
                    color = DetailColors.TextMuted
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "ALLTREK Tenda Camping Tentastic Outdoor 1 Bedroom + 1 Guest Room",
                    fontFamily = Volkhov,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp,
                    color = DetailColors.Black,
                    lineHeight = 32.sp
                )

                Spacer(Modifier.height(10.dp))

                DetailRatingRow(rating = 4f, reviewCount = 4)

                Spacer(Modifier.height(8.dp))

                DetailViewingNowRow()

                Spacer(Modifier.height(14.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "Rp 450.000/hari",
                        fontFamily = Volkhov,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = DetailColors.Black
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Rp 500.000",
                        fontSize = 14.sp,
                        color = DetailColors.PriceOld,
                        textDecoration = TextDecoration.LineThrough,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }

                Spacer(Modifier.height(22.dp))

                DetailDateSelectorRow(
                    dateStart = "30 Mei 2026",
                    dateEnd = "30 Mei 2027",
                    onDateStartClick = {},
                    onDateEndClick = {}
                )

                Spacer(Modifier.height(20.dp))

                DetailStockSection(stock = 9)

                Spacer(Modifier.height(20.dp))

                DetailQuantitySection(
                    qty = 1,
                    maxStock = 9,
                    onMinus = {},
                    onPlus = {}
                )

                Spacer(Modifier.height(24.dp))

                DetailActionLinks()

                Spacer(Modifier.height(20.dp))

                HorizontalDivider(color = DetailColors.Border, thickness = 0.5.dp)
                Spacer(Modifier.height(16.dp))

                DetailInfoItems()

                Spacer(Modifier.height(20.dp))

                DetailPaymentBox()

                Spacer(Modifier.height(28.dp))

                DetailTabSection(
                    tabs = listOf("Description", "Additional Information", "Reviews"),
                    selectedTab = 0,
                    onTabChange = {}
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Tenda yang cocok untuk kalian yang ingin mencoba camping bersama keluarga! Tentastic merupakan tenda keluarga yang terdiri atas 2 ruangan (Living Room dan Bedroom).",
                    fontSize = 14.sp,
                    color = DetailColors.Black,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Justify
                )

                Spacer(Modifier.height(32.dp))
            }

            // BOTTOM BAR
            DetailBottomActionBar(
                onAddToCart = {},
                onRentNow = {},
                isLoading = false
            )
        }
    }
}