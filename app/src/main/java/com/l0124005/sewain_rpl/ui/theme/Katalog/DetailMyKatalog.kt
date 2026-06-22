package com.l0124005.sewain_rpl.ui.theme.katalog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.l0124005.sewain_rpl.network.ApiClient
import com.l0124005.sewain_rpl.repository.KatalogRepository
import com.l0124005.sewain_rpl.ui.theme.SewainTopBar
import com.l0124005.sewain_rpl.ui.theme.Sewain_rplTheme
import com.l0124005.sewain_rpl.utils.CurrencyUtils
import com.l0124005.sewain_rpl.utils.Resource
import com.l0124005.sewain_rpl.utils.SessionManager
import com.l0124005.sewain_rpl.viewmodel.KatalogViewModel
import com.l0124005.sewain_rpl.viewmodel.KatalogViewModelFactory
import java.text.SimpleDateFormat
import java.util.*
import com.l0124005.sewain_rpl.ui.theme.katalog.DetailMainImageSection
import com.l0124005.sewain_rpl.ui.theme.katalog.DetailRatingRow
import com.l0124005.sewain_rpl.ui.theme.katalog.DetailViewingNowRow
import com.l0124005.sewain_rpl.ui.theme.katalog.DetailStockSection
import com.l0124005.sewain_rpl.ui.theme.katalog.DetailActionLinks
import com.l0124005.sewain_rpl.ui.theme.katalog.DetailInfoItems
import com.l0124005.sewain_rpl.ui.theme.katalog.DetailPaymentBox
import com.l0124005.sewain_rpl.ui.theme.katalog.DetailTabSection
import com.l0124005.sewain_rpl.ui.theme.katalog.DetailAdditionalInfoContent
import com.l0124005.sewain_rpl.ui.theme.katalog.DetailReviewSection
import com.l0124005.sewain_rpl.ui.theme.katalog.DetailSpecItem
import com.l0124005.sewain_rpl.ui.theme.katalog.DetailColors
import com.l0124005.sewain_rpl.ui.theme.VolkhovFont
import com.l0124005.sewain_rpl.ui.theme.LatoFont
import com.l0124005.sewain_rpl.network.CatalogData
import com.l0124005.sewain_rpl.R

// ═══════════════════════════════════════
// ACTIVITY
// ═══════════════════════════════════════

class DetailMyKatalogActivity : ComponentActivity() {
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

                DetailMyKatalogScreen(
                    productId = productId,
                    token = token,
                    katalogViewModel = katalogViewModel,
                    onBack = { finish() },
                    onEditKatalog = { item ->
                        val intent = android.content.Intent(this, com.l0124005.sewain_rpl.ui.theme.MainActivity::class.java).apply {
                            putExtra("TARGET_SCREEN", "EDIT_ITEM")
                            putExtra("PRODUCT_ID", item.id)
                            flags = android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}

// ═══════════════════════════════════════
// MAIN SCREEN
// ═══════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailMyKatalogScreen(
    productId: Int,
    token: String,
    katalogViewModel: KatalogViewModel,
    onBack: () -> Unit = {},
    onEditKatalog: (CatalogData) -> Unit = {}
) {
    val detailState by katalogViewModel.myKatalogDetail.observeAsState()

    var selectedThumb by remember { mutableIntStateOf(0) }
    var selectedTab   by remember { mutableIntStateOf(0) }

    var fallbackTried by remember { mutableStateOf(false) }

    // Reset selectedThumb saat produk berubah
    LaunchedEffect(productId) {
        selectedThumb = 0
        if (productId != -1) {
            fallbackTried = false
            katalogViewModel.resetStates()
            katalogViewModel.getMyKatalogDetail(token, productId)
        }
    }

    LaunchedEffect(detailState) {
        val state = detailState
        if (state is Resource.Error && state.code == 404 && !fallbackTried) {
            fallbackTried = true
            katalogViewModel.getKatalogPublikDetail(productId)
        }
    }

    Scaffold(
        topBar = {
            SewainTopBar(
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = onBack
            )
        },
        bottomBar = {
            if (detailState is Resource.Success) {
                val product = (detailState as Resource.Success).data?.data
                Surface(shadowElevation = 8.dp, color = DetailColors.White) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 14.dp)
                    ) {
                        Button(
                            onClick = { product?.let { onEditKatalog(it) } },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DetailColors.Accent,
                                contentColor = DetailColors.Black
                            ),
                            border = BorderStroke(0.5.dp, DetailColors.Black)
                        ) {
                            Text(
                                text = "Edit Katalog",
                                fontFamily = VolkhovFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        },
        containerColor = DetailColors.White
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (detailState) {
                is Resource.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = DetailColors.Primary
                    )
                }
                is Resource.Success -> {
                    val product = detailState!!.data!!.data
                    
                    // Mengambil semua foto dari helper listFoto
                    val photos = product.listFoto.map { product.getFullUrl(it) }

                    val tabs = listOf("Deskripsi", "Info Tambahan", "Ulasan")
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

                            Spacer(Modifier.height(20.dp))
                            DetailStockSection(stock = product.stok)
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
                                tabs = tabs,
                                selectedTab = selectedTab,
                                onTabChange = { selectedTab = it }
                            )

                            Spacer(Modifier.height(16.dp))

                            when (selectedTab) {
                                0 -> Text(
                                    text = product.deskripsi ?: "Tidak ada deskripsi.",
                                    fontFamily = LatoFont,
                                    fontSize = 14.sp,
                                    color = DetailColors.Black,
                                    lineHeight = 22.sp,
                                    textAlign = TextAlign.Justify
                                )
                                1 -> DetailAdditionalInfoContent(
                                    specs = mutableListOf(
                                        DetailSpecItem("Lokasi Barang", product.lokasi ?: "-"),
                                        DetailSpecItem("Uang Jaminan", "Rp ${CurrencyUtils.formatRupiah(product.harga_jaminan)}"),
                                        DetailSpecItem("Denda Keterlambatan", "Rp ${CurrencyUtils.formatRupiah(product.harga_denda_perjam)}/jam"),
                                        DetailSpecItem("Kategori", product.kategori?.nama_kategori ?: "Umum")
                                    ).apply {
                                        if (!product.additional_information.isNullOrBlank()) {
                                            add(DetailSpecItem("Info Tambahan", product.additional_information))
                                        }
                                    },
                                    ownerName = "Owner #${product.user_id}",
                                    ownerDenda = "Rp ${CurrencyUtils.formatRupiah(product.harga_denda_perjam)}"
                                )
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
                                katalogViewModel.getMyKatalogDetail(token, productId)
                            }
                        }) { Text("Coba Lagi") }
                    }
                }
                else -> {}
            }
        }
    }
}
