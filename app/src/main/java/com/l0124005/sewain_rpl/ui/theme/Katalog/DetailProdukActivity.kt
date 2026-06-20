package com.l0124005.sewain_rpl.ui.theme.katalog

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import com.l0124005.sewain_rpl.ui.theme.Sewain_rplTheme
import com.l0124005.sewain_rpl.utils.CurrencyUtils
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

// ============================================================
// WARNA TEMA — SEWAIN
// ============================================================
object DetailColors {
    val Primary      = com.l0124005.sewain_rpl.ui.theme.katalog.Primary
    val Black        = com.l0124005.sewain_rpl.ui.theme.katalog.Black
    val White        = com.l0124005.sewain_rpl.ui.theme.katalog.White
    val Surface      = Color(0xFFF8F8F8)
    val Border       = Color(0xFFE4E4E4)
    val TextMuted    = com.l0124005.sewain_rpl.ui.theme.katalog.TextMuted
    val TextDark     = com.l0124005.sewain_rpl.ui.theme.katalog.TextDark
    val Star         = Color(0xFFF5B800)
    val StockRed     = Color(0xFFD8262C)
    val PriceOld     = Color(0xFF666666)
    val InfoBg       = Color(0xFFF5F5F5)
}

data class DetailSpecItem(val label: String, val value: String)

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
    onNavigateToTransactions: () -> Unit = {}
) {
    val context = LocalContext.current
    val detailState by katalogViewModel.myKatalogDetail.observeAsState()
    val keranjangState by keranjangViewModel.keranjang.observeAsState()
    val checkoutState by transaksiViewModel.checkoutState.observeAsState()

    var isRentNowClicked by remember { mutableStateOf(false) }
    var selectedThumb by remember { mutableStateOf(0) }
    var quantity      by remember { mutableStateOf(1) }
    var selectedTab   by remember { mutableStateOf(0) }

    val sdfDisplay = SimpleDateFormat("dd MMMM yyyy", Locale("id"))
    val sdfApi = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    var dateStartRaw by remember { mutableStateOf(Date()) }
    var dateEndRaw by remember { mutableStateOf(Date(System.currentTimeMillis() + 86400000)) }

    var showDatePickerStart by remember { mutableStateOf(false) }
    var showDatePickerEnd by remember { mutableStateOf(false) }

    var fallbackTried by remember { mutableStateOf(false) }

    LaunchedEffect(productId) {
        if (productId != -1) {
            fallbackTried = false
            katalogViewModel.resetStates()
            // Mencoba ambil secara publik dulu (umum)
            katalogViewModel.getKatalogPublikDetail(productId)
        }
    }

    // Listener tambahan untuk menangani 404 (Data baru yang belum muncul di publik)
    LaunchedEffect(detailState) {
        if (detailState is Resource.Error && detailState?.code == 404 && !fallbackTried) {
            // Jika 404 di publik, coba ambil dari endpoint privat (milik sendiri)
            fallbackTried = true
            katalogViewModel.getMyKatalogDetail(token, productId)
        }
    }

    LaunchedEffect(keranjangState) {
        if (keranjangState is Resource.Success) {
            if (isRentNowClicked) {
                transaksiViewModel.checkout(token)
            } else {
                Toast.makeText(context, "Berhasil ditambahkan ke keranjang!", Toast.LENGTH_SHORT).show()
                onNavigateToCart()
            }
            keranjangViewModel.resetStates()
        } else if (keranjangState is Resource.Error) {
            Toast.makeText(context, "Gagal: ${keranjangState?.message}", Toast.LENGTH_SHORT).show()
            keranjangViewModel.resetStates()
        }
    }

    LaunchedEffect(checkoutState) {
        if (checkoutState is Resource.Success) {
            Toast.makeText(context, "Sewa berhasil! Mengalihkan ke riwayat transaksi...", Toast.LENGTH_SHORT).show()
            onNavigateToTransactions()
            transaksiViewModel.resetStates()
        } else if (checkoutState is Resource.Error) {
            Toast.makeText(context, "Checkout gagal: ${checkoutState?.message}", Toast.LENGTH_SHORT).show()
            transaksiViewModel.resetStates()
        }
    }

    if (showDatePickerStart) {
        SewainDatePickerDialog(
            title = "Pilih Tanggal Mulai",
            onDismiss = { showDatePickerStart = false },
            onDateSelected = { dateStr ->
                sdfDisplay.parse(dateStr)?.let { dateStartRaw = it }
                showDatePickerStart = false
            }
        )
    }

    if (showDatePickerEnd) {
        SewainDatePickerDialog(
            title = "Pilih Tanggal Selesai",
            onDismiss = { showDatePickerEnd = false },
            onDateSelected = { dateStr ->
                sdfDisplay.parse(dateStr)?.let { dateEndRaw = it }
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
                val isLoading = keranjangState is Resource.Loading || checkoutState is Resource.Loading
                DetailBottomActionBar(
                    onAddToCart = {
                        isRentNowClicked = false
                        keranjangViewModel.addToKeranjang(
                            token, productId, quantity,
                            sdfApi.format(dateStartRaw),
                            sdfApi.format(dateEndRaw)
                        )
                    },
                    onRentNow = {
                        isRentNowClicked = true
                        keranjangViewModel.addToKeranjang(
                            token, productId, quantity,
                            sdfApi.format(dateStartRaw),
                            sdfApi.format(dateEndRaw)
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
                    val mainImage = if (product.foto_barang != null) "${ApiClient.IMAGE_BASE_URL}${product.foto_barang}" else null
                    val photos = if (mainImage != null) listOf(mainImage) else emptyList()
                    val tabs = listOf("Deskripsi", "Info Tambahan", "Ulasan")

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
                                fontFamily = Volkhov,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                letterSpacing = 2.sp,
                                color = DetailColors.TextMuted
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = product.nama_barang,
                                fontFamily = Volkhov,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = DetailColors.Black,
                                lineHeight = 30.sp
                            )

                            Spacer(Modifier.height(10.dp))

                            DetailRatingRow(rating = 4.5f, reviewCount = 0)

                            Spacer(Modifier.height(14.dp))

                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "Rp ${CurrencyUtils.formatRupiah(product.harga_sewa)}/hari",
                                    fontFamily = Volkhov,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                    color = DetailColors.Black
                                )
                            }

                            Spacer(Modifier.height(22.dp))

                            DetailDateSelectorRow(
                                dateStart = sdfDisplay.format(dateStartRaw),
                                dateEnd = sdfDisplay.format(dateEndRaw),
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
                                    fontFamily = FontFamily.Default, fontSize = 14.sp, color = DetailColors.Black, lineHeight = 22.sp, textAlign = TextAlign.Justify
                                )
                                1 -> DetailAdditionalInfoContent(specs = listOf(
                                    DetailSpecItem("Lokasi Barang", product.lokasi ?: "-"),
                                    DetailSpecItem("Uang Jaminan", "Rp ${CurrencyUtils.formatRupiah(product.harga_jaminan)}"),
                                    DetailSpecItem("Denda Keterlambatan", "Rp ${CurrencyUtils.formatRupiah(product.harga_denda_perjam)}/jam"),
                                    DetailSpecItem("Kategori", product.kategori?.nama_kategori ?: "Umum")
                                ), ownerName = "Owner #${product.user_id}", ownerDenda = "Rp ${CurrencyUtils.formatRupiah(product.harga_denda_perjam)}")
                                2 -> Text("Belum ada ulasan.", modifier = Modifier.padding(vertical = 16.dp), color = DetailColors.TextMuted)
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
    TopAppBar(
        title = {
            Text(
                text = buildAnnotatedString {
                    append("SEWA")
                    withStyle(SpanStyle(color = DetailColors.Primary)) { append("IN") }
                },
                fontFamily = Volkhov,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = DetailColors.Black
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = DetailColors.Black)
            }
        },
        actions = {
            IconButton(onClick = {}) { Icon(Icons.Outlined.Share, null, tint = DetailColors.Black) }
            IconButton(onClick = onCartClick) { Icon(Icons.Outlined.ShoppingCart, null, tint = DetailColors.Black) }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = DetailColors.White)
    )
}

@Composable
fun DetailMainImageSection(photos: List<String>, selectedIndex: Int, onThumbSelected: (Int) -> Unit) {
    Column {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1.2f).background(DetailColors.Surface)) {
            val imageToShow = photos.getOrNull(selectedIndex)
            if (imageToShow != null) {
                AsyncImage(model = imageToShow, contentDescription = "Foto produk", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            } else {
                Icon(Icons.Default.Image, null, Modifier.size(64.dp).align(Alignment.Center), tint = Color.LightGray)
            }
        }
        if (photos.size > 1) {
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                photos.forEachIndexed { i, url ->
                    val isActive = i == selectedIndex
                    Box(modifier = Modifier.size(72.dp).clip(RoundedCornerShape(6.dp)).border(width = if (isActive) 2.dp else 0.dp, color = if (isActive) DetailColors.Black else Color.Transparent, shape = RoundedCornerShape(6.dp)).clickable { onThumbSelected(i) }.background(DetailColors.Surface)) {
                        AsyncImage(model = url, contentDescription = "Thumbnail ${i + 1}", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
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
        Text(text = label, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = DetailColors.Black)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.CalendarToday, null, tint = DetailColors.Black, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(text = value, fontSize = 12.sp, color = DetailColors.Black)
        }
    }
}

@Composable
fun DetailStockSection(stock: Int) {
    Column {
        Text(text = buildAnnotatedString {
            append("Tersedia ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = DetailColors.TextDark)) { append("$stock") }
            append(" item")
        }, fontSize = 13.sp, color = DetailColors.PriceOld)
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
        DetailActionLinkItem(icon = Icons.AutoMirrored.Outlined.CompareArrows, label = "Bandingkan", onClick = {})
        DetailActionLinkItem(icon = Icons.AutoMirrored.Outlined.HelpOutline, label = "Tanya", onClick = {})
        DetailActionLinkItem(icon = Icons.Outlined.Share, label = "Bagikan", onClick = {})
    }
}

@Composable
fun DetailActionLinkItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Row(modifier = Modifier.clickable(onClick = onClick), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = label, tint = DetailColors.Black, modifier = Modifier.size(17.dp))
        Spacer(Modifier.width(5.dp))
        Text(text = label, fontSize = 13.sp, color = DetailColors.Black)
    }
}

@Composable
fun DetailInfoItems() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        DetailInfoRow(icon = Icons.Outlined.CalendarToday, boldText = "Ketentuan Pengembalian:", text = "Wajib dikembalikan dalam keadaan bersih & tepat waktu")
        DetailInfoRow(icon = Icons.Outlined.Shield, boldText = "Kebijakan Jaminan:", text = "Dokumen pendukung valid saat pengambilan")
        DetailInfoRow(icon = Icons.Outlined.Warning, boldText = "Denda keterlambatan sewa berlaku per jam", text = "")
    }
}

@Composable
fun DetailInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, boldText: String, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, null, tint = DetailColors.Black, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(text = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontFamily = Volkhov, fontSize = 13.sp)) { append(boldText) }
            if (text.isNotEmpty()) append(" $text")
        }, fontSize = 13.sp, color = DetailColors.Black)
    }
}

@Composable
fun DetailPaymentBox() {
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(DetailColors.Surface).padding(14.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                listOf("VISA", "MASTERCARD", "BCA", "BNI", "MANDIRI", "BRI", "GOPAY", "OVO", "DANA").forEach { method ->
                    Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).border(0.5.dp, DetailColors.Border, RoundedCornerShape(4.dp)).background(DetailColors.White).padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Text(text = method, fontWeight = FontWeight.Bold, fontSize = 9.sp, color = DetailColors.TextDark)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(text = "Pembayaran dijamin aman & terpercaya", fontFamily = Volkhov, fontSize = 12.sp, color = DetailColors.Black, textAlign = TextAlign.Center)
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
                Text(text = tab, fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp, color = if (isActive) DetailColors.Black else DetailColors.TextMuted, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun DetailAdditionalInfoContent(specs: List<DetailSpecItem>, ownerName: String, ownerDenda: String) {
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        specs.forEachIndexed { i, spec ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp).let { if (i > 0) it.drawBehind { drawLine(Color(0xFFF0F0F0), Offset(0f, 0f), Offset(size.width, 0f), 1.dp.toPx()) } else it }) {
                Text(text = spec.label, modifier = Modifier.weight(1f), fontSize = 13.sp, color = DetailColors.Black)
                Text(text = spec.value, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium, fontSize = 13.sp, color = DetailColors.Black, textAlign = TextAlign.End)
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

@Composable
fun DetailBottomActionBar(onAddToCart: () -> Unit, onRentNow: () -> Unit, isLoading: Boolean) {
    Surface(shadowElevation = 8.dp, color = DetailColors.White) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onAddToCart, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, DetailColors.Black), colors = ButtonDefaults.outlinedButtonColors(contentColor = DetailColors.TextDark), enabled = !isLoading) {
                Text(text = "Keranjang", fontFamily = Volkhov, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
            Button(onClick = onRentNow, modifier = Modifier.weight(2f).height(48.dp), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = DetailColors.Primary, contentColor = DetailColors.Black), border = BorderStroke(0.5.dp, DetailColors.Black), enabled = !isLoading) {
                if (isLoading) { CircularProgressIndicator(modifier = Modifier.size(24.dp), color = DetailColors.Black) } else { Text(text = "Sewa Sekarang!", fontFamily = Volkhov, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
            }
        }
    }
}