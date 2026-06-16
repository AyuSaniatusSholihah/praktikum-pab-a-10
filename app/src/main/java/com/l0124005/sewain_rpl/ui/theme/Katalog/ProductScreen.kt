package com.l0124005.sewain_rpl.ui.theme.katalog

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.l0124005.sewain_rpl.R

// ============================================================
// WARNA TEMA — SEWAIN
// ============================================================
// Menggunakan warna dari KatalogComponents agar konsisten
object SewainColors {
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

// ============================================================
// DATA MODELS
// ============================================================
data class ReviewItem(
    val name: String,
    val date: String,
    val rating: Int,
    val text: String,
    val avatarUrl: String
)

data class SpecItem(val label: String, val value: String)

// ============================================================
// PRODUCT DETAIL SCREEN
// ============================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    onBack: () -> Unit = {},
    onAddToCart: () -> Unit = {},
    onRentNow: () -> Unit = {}
) {
    // ----- State -----
    var selectedThumb by remember { mutableStateOf(0) }
    var quantity      by remember { mutableStateOf(1) }
    var selectedTab   by remember { mutableStateOf(0) } // 0=Desc, 1=Info, 2=Reviews
    var dateStart     by remember { mutableStateOf("30 Mei 2026") }
    var dateEnd       by remember { mutableStateOf("30 Mei 2027") }
    var cartCount     by remember { mutableStateOf(2) }

    val maxStock = 9

    val photos = listOf(
        "https://source.unsplash.com/featured/?tent,camping",
        "https://source.unsplash.com/featured/?outdoor,tent",
        "https://source.unsplash.com/featured/?camping,nature",
        "https://source.unsplash.com/featured/?tent,forest",
        "https://source.unsplash.com/featured/?camping,family",
    )

    val reviews = listOf(
        ReviewItem("Andi R.", "21 Juli 2026", 5,
            "Tenda kualitas premium, set up gampang banget! Sangat direkomendasikan untuk camping keluarga.",
            "https://randomuser.me/api/portraits/men/32.jpg"),
        ReviewItem("Sari M.", "20 Juli 2026", 4,
            "Cukup luas untuk 4 orang, tahan hujan. Pemilik juga responsif dan ramah.",
            "https://randomuser.me/api/portraits/women/44.jpg"),
        ReviewItem("Reza P.", "15 Juli 2026", 5,
            "Worth it dengan harga sewanya. Highly recommended untuk kalian yang mau camping bareng keluarga!",
            "https://randomuser.me/api/portraits/men/67.jpg"),
    )

    val specs = listOf(
        SpecItem("Kode",     "Tentastic PRO"),
        SpecItem("Brand",    "ALLTREK"),
        SpecItem("Material", "210D Oxford Waterproof PU 4500MM"),
        SpecItem("Ukuran",   "380 x 260 x 185 cm"),
    )

    val tabs = listOf("Deskripsi", "Info Tambahan", "Ulasan [3]")

    // ----- Layout -----
    Scaffold(
        topBar = {
            TopBar(
                cartCount = cartCount,
                onBack = onBack
            )
        },
        bottomBar = {
            BottomActionBar(
                onAddToCart = {
                    cartCount++
                    onAddToCart()
                },
                onRentNow = onRentNow
            )
        },
        containerColor = SewainColors.White
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {

            // ---- 1. FOTO UTAMA ----
            MainImageSection(
                photos = photos,
                selectedIndex = selectedThumb,
                onThumbSelected = { selectedThumb = it }
            )

            Spacer(Modifier.height(20.dp))

            Column(
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {

                // ---- 2. BRAND + JUDUL ----
                Text(
                    text = "SEWAIN",
                    fontFamily = Volkhov,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    letterSpacing = 2.sp,
                    color = SewainColors.TextMuted
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "ALLTREK Tenda Camping Tentastic Outdoor 1 Bedroom + 1 Guest Room",
                    fontFamily = Volkhov,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = SewainColors.Black,
                    lineHeight = 30.sp
                )

                Spacer(Modifier.height(10.dp))

                // ---- 3. RATING ----
                RatingRow(rating = 4.7f, reviewCount = 3)

                Spacer(Modifier.height(6.dp))

                // ---- 4. VIEWING NOW ----
                ViewingNowRow(count = 24)

                Spacer(Modifier.height(14.dp))

                // ---- 5. HARGA ----
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "Rp 450.000/hari",
                        fontFamily = Volkhov,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = SewainColors.Black
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Rp 500.000",
                        fontFamily = FontFamily.Default,
                        fontSize = 14.sp,
                        color = SewainColors.PriceOld,
                        textDecoration = TextDecoration.LineThrough,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                Spacer(Modifier.height(22.dp))

                // ---- 6. TANGGAL SEWA ----
                DateSelectorRow(
                    dateStart = dateStart,
                    dateEnd = dateEnd,
                    onDateStartClick = { /* buka date picker */ },
                    onDateEndClick   = { /* buka date picker */ }
                )

                Spacer(Modifier.height(20.dp))

                // ---- 7. STOK ----
                StockSection(stock = maxStock, maxStock = 20)

                Spacer(Modifier.height(20.dp))

                // ---- 8. QUANTITY ----
                QuantitySection(
                    qty      = quantity,
                    maxStock = maxStock,
                    onMinus  = { if (quantity > 1) quantity-- },
                    onPlus   = { if (quantity < maxStock) quantity++ }
                )

                Spacer(Modifier.height(24.dp))

                // ---- 9. ACTION LINKS ----
                ActionLinks()

                Spacer(Modifier.height(20.dp))

                Divider(color = SewainColors.Border, thickness = 0.5.dp)
                Spacer(Modifier.height(16.dp))

                // ---- 10. INFO ITEM ----
                InfoItems()

                Spacer(Modifier.height(20.dp))

                // ---- 11. PAYMENT BOX ----
                PaymentBox()

                Spacer(Modifier.height(28.dp))

                // ---- 12. TABS ----
                TabSection(
                    tabs        = tabs,
                    selectedTab = selectedTab,
                    onTabChange = { selectedTab = it }
                )

                Spacer(Modifier.height(16.dp))

                // ---- 13. TAB CONTENT ----
                when (selectedTab) {
                    0 -> DescriptionContent()
                    1 -> AdditionalInfoContent(specs = specs)
                    2 -> ReviewsContent(reviews = reviews)
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

// ============================================================
// TOP BAR
// ============================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(cartCount: Int, onBack: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = buildAnnotatedString {
                    append("SEWA")
                    withStyle(SpanStyle(color = SewainColors.Primary)) { append("IN") }
                },
                fontFamily = Volkhov,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = SewainColors.Black
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Kembali",
                    tint = SewainColors.Black)
            }
        },
        actions = {
            IconButton(onClick = {}) {
                Icon(Icons.Outlined.Person, contentDescription = "Profil",
                    tint = SewainColors.Black)
            }
            IconButton(onClick = {}) {
                Icon(Icons.Outlined.Notifications, contentDescription = "Notifikasi",
                    tint = SewainColors.Black)
            }
            BadgedBox(badge = {
                Badge { Text("$cartCount") }
            }) {
                IconButton(onClick = {}) {
                    Icon(Icons.Outlined.ShoppingCart, contentDescription = "Keranjang",
                        tint = SewainColors.Black)
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = SewainColors.White
        )
    )
}

// ============================================================
// MAIN IMAGE + THUMBNAILS
// ============================================================
@Composable
fun MainImageSection(
    photos: List<String>,
    selectedIndex: Int,
    onThumbSelected: (Int) -> Unit
) {
    Column {
        // Foto besar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(SewainColors.Surface)
        ) {
            AsyncImage(
                model = photos.getOrElse(selectedIndex) { "" },
                contentDescription = "Foto produk",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(Modifier.height(12.dp))

        // Thumbnail row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            photos.forEachIndexed { i, url ->
                val isActive = i == selectedIndex
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .border(
                            width = if (isActive) 2.dp else 0.dp,
                            color = if (isActive) SewainColors.Black else Color.Transparent,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable { onThumbSelected(i) }
                        .background(SewainColors.Surface)
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

// ============================================================
// RATING ROW
// ============================================================
@Composable
fun RatingRow(rating: Float, reviewCount: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        val fullStars = rating.toInt()
        repeat(fullStars) {
            Text("★", color = SewainColors.Star, fontSize = 20.sp)
        }
        if (fullStars < 5) {
            Text("☆", color = SewainColors.Star, fontSize = 20.sp)
        }
        Spacer(Modifier.width(6.dp))
        Text(
            text = "($reviewCount)",
            fontFamily = FontFamily.Default,
            fontSize = 14.sp,
            color = SewainColors.TextDark
        )
    }
}

// ============================================================
// VIEWING NOW ROW
// ============================================================
@Composable
fun ViewingNowRow(count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Outlined.RemoveRedEye, contentDescription = null,
            tint = SewainColors.TextMuted, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(
            text = "$count orang sedang melihat ini",
            fontFamily = FontFamily.Default,
            fontSize = 12.sp,
            color = SewainColors.TextMuted
        )
    }
}

// ============================================================
// DATE SELECTOR
// ============================================================
@Composable
fun DateSelectorRow(
    dateStart: String,
    dateEnd: String,
    onDateStartClick: () -> Unit,
    onDateEndClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DateBox(
            label = "Tanggal Mulai",
            value = dateStart,
            onClick = onDateStartClick,
            modifier = Modifier.weight(1f)
        )
        DateBox(
            label = "Tanggal Selesai",
            value = dateEnd,
            onClick = onDateEndClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun DateBox(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(0.5.dp, SewainColors.Border, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = SewainColors.Black,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Outlined.CalendarToday, contentDescription = null,
                tint = SewainColors.Black,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = value,
                fontFamily = FontFamily.Default,
                fontSize = 12.sp,
                color = SewainColors.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ============================================================
// STOCK SECTION
// ============================================================
@Composable
fun StockSection(stock: Int, maxStock: Int) {
    val fraction = stock.toFloat() / maxStock.toFloat()
    Column {
        Text(
            text = buildAnnotatedString {
                append("Hanya ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = SewainColors.TextDark)) {
                    append("$stock")
                }
                append(" item tersisa!")
            },
            fontFamily = FontFamily.Default,
            fontSize = 13.sp,
            color = SewainColors.PriceOld
        )
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(SewainColors.Border)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction)
                    .clip(RoundedCornerShape(2.dp))
                    .background(SewainColors.StockRed)
            )
        }
    }
}

// ============================================================
// QUANTITY SECTION
// ============================================================
@Composable
fun QuantitySection(
    qty: Int,
    maxStock: Int,
    onMinus: () -> Unit,
    onPlus: () -> Unit
) {
    Column {
        Text(
            text = "Jumlah",
            fontFamily = Volkhov,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = SewainColors.Black
        )
        Spacer(Modifier.height(10.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Qty stepper
            Row(
                modifier = Modifier
                    .border(1.dp, SewainColors.Border, RoundedCornerShape(6.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onMinus, modifier = Modifier.size(40.dp)) {
                    Text("−", fontSize = 18.sp, color = SewainColors.TextDark)
                }
                Text(
                    text = "$qty",
                    modifier = Modifier.defaultMinSize(minWidth = 32.dp),
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.Default,
                    fontSize = 15.sp,
                    color = SewainColors.Black
                )
                IconButton(onClick = onPlus, modifier = Modifier.size(40.dp)) {
                    Text("+", fontSize = 18.sp, color = SewainColors.TextDark)
                }
            }

            // Teks stok sisa di samping stepper
            Text(
                text = "/ $maxStock tersedia",
                fontFamily = FontFamily.Default,
                fontSize = 12.sp,
                color = SewainColors.TextMuted
            )
        }
    }
}

// ============================================================
// ACTION LINKS
// ============================================================
@Composable
fun ActionLinks() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        ActionLinkItem(
            icon  = Icons.Outlined.CompareArrows,
            label = "Bandingkan",
            onClick = {}
        )
        ActionLinkItem(
            icon  = Icons.Outlined.HelpOutline,
            label = "Tanya",
            onClick = {}
        )
        ActionLinkItem(
            icon  = Icons.Outlined.Share,
            label = "Bagikan",
            onClick = {}
        )
    }
}

@Composable
fun ActionLinkItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = SewainColors.Black,
            modifier = Modifier.size(17.dp)
        )
        Spacer(Modifier.width(5.dp))
        Text(
            text = label,
            fontFamily = FontFamily.Default,
            fontSize = 13.sp,
            color = SewainColors.Black
        )
    }
}

// ============================================================
// INFO ITEMS
// ============================================================
@Composable
fun InfoItems() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        InfoRow(
            icon = Icons.Outlined.CalendarToday,
            boldText = "Tanggal Tersedia:",
            text = "30 Mei 2026 – 2027"
        )
        InfoRow(
            icon = Icons.Outlined.Shield,
            boldText = "Kebijakan Pengembalian:",
            text = "Wajib dikembalikan tepat waktu"
        )
        InfoRow(
            icon = Icons.Outlined.Warning,
            boldText = "Denda keterlambatan berlaku",
            text = ""
        )
    }
}

@Composable
fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    boldText: String,
    text: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null,
            tint = SewainColors.Black, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontFamily = Volkhov, fontSize = 13.sp)) {
                    append(boldText)
                }
                if (text.isNotEmpty()) {
                    append(" $text")
                }
            },
            fontFamily = FontFamily.Default,
            fontSize = 13.sp,
            color = SewainColors.Black
        )
    }
}

// ============================================================
// PAYMENT BOX
// ============================================================
@Composable
fun PaymentBox() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(SewainColors.Surface)
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Payment method chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                listOf("VISA", "MASTERCARD", "BCA", "BNI", "Mandiri", "BRI", "GoPay", "OVO", "DANA").forEach { method ->
                    PaymentChip(method)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Pembayaran dijamin aman & terpercaya",
                fontFamily = Volkhov,
                fontSize = 12.sp,
                color = SewainColors.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun PaymentChip(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .border(0.5.dp, SewainColors.Border, RoundedCornerShape(4.dp))
            .background(SewainColors.White)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            color = SewainColors.TextDark
        )
    }
}

// ============================================================
// TABS
// ============================================================
@Composable
fun TabSection(
    tabs: List<String>,
    selectedTab: Int,
    onTabChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 0.5.dp,
                color = SewainColors.Border,
                shape = RoundedCornerShape(bottomStart = 0.dp, bottomEnd = 0.dp)
            ),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        tabs.forEachIndexed { i, tab ->
            val isActive = i == selectedTab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTabChange(i) }
                    .padding(vertical = 12.dp)
                    .drawBehind {
                        if (isActive) {
                            drawLine(
                                color = SewainColors.Black,
                                start = Offset(0f, size.height),
                                end = Offset(size.width, size.height),
                                strokeWidth = 2.dp.toPx()
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tab,
                    fontFamily = FontFamily.Default,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 13.sp,
                    color = if (isActive) SewainColors.Black else SewainColors.TextMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ============================================================
// TAB CONTENTS
// ============================================================
@Composable
fun DescriptionContent() {
    Text(
        text = "Tenda yang cocok untuk kalian yang ingin mencoba camping bersama keluarga! " +
                "Tentastic merupakan tenda keluarga yang terdiri atas 2 ruangan (Living Room dan Bedroom) " +
                "dan bedroomnya dapat dibagi jadi 2 bedroom terpisah (untuk varian large). " +
                "Tentastic memiliki material Oxford Waterproof PU 4500MM dan flooring Oxford 150D Seam Sealed, " +
                "sehingga camping tetap aman dan nyaman. Tingginya mencapai 180cm+ agar nyaman beraktivitas di dalam!",
        fontFamily = FontFamily.Default,
        fontSize = 14.sp,
        color = SewainColors.Black,
        lineHeight = 22.sp,
        textAlign = TextAlign.Justify
    )
}

@Composable
fun AdditionalInfoContent(specs: List<SpecItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        specs.forEachIndexed { i, spec ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
                    .let { if (i > 0) it.drawBehind {
                        drawLine(Color(0xFFF0F0F0), Offset(0f, 0f), Offset(size.width, 0f), 1.dp.toPx())
                    } else it }
            ) {
                Text(
                    text = spec.label,
                    modifier = Modifier.weight(1f),
                    fontFamily = FontFamily.Default,
                    fontSize = 13.sp,
                    color = SewainColors.Black
                )
                Text(
                    text = spec.value,
                    modifier = Modifier.weight(1f),
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = SewainColors.Black,
                    textAlign = TextAlign.End
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Owner info box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(SewainColors.InfoBg)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OwnerInfoRow("Pemilik", "Camping Groups Bandung")
                OwnerInfoRow("WhatsApp", "6285390176483")
                OwnerInfoRow("Jaminan", "KTP, SIM, + setengah harga asli")
                OwnerInfoRow("Denda", "Rp 15.000/jam")
                OwnerInfoRow("Lokasi", "Jl. Ir. H. Juanda No. 50 (Dago), Bandung")
            }
        }
    }
}

@Composable
fun OwnerInfoRow(label: String, value: String) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(fontFamily = Volkhov, fontWeight = FontWeight.Bold, fontSize = 14.sp)) {
                append("$label: ")
            }
            withStyle(SpanStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp)) {
                append(value)
            }
        },
        color = SewainColors.Black
    )
}

@Composable
fun ReviewsContent(reviews: List<ReviewItem>) {
    Column {
        reviews.forEachIndexed { i, review ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp)
                    .let {
                        if (i < reviews.lastIndex) it.drawBehind {
                            drawLine(
                                Color(0xFFF0F0F0),
                                Offset(0f, size.height),
                                Offset(size.width, size.height),
                                1.dp.toPx()
                            )
                        } else it
                    },
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(SewainColors.Border)
                ) {
                    AsyncImage(
                        model = review.avatarUrl,
                        contentDescription = review.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = review.name,
                                fontFamily = Volkhov,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = SewainColors.Black
                            )
                            Text(
                                text = "Penyewa Terverifikasi",
                                fontFamily = FontFamily.Default,
                                fontSize = 11.sp,
                                color = SewainColors.TextMuted
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = review.date,
                                fontFamily = FontFamily.Default,
                                fontSize = 11.sp,
                                color = SewainColors.TextMuted
                            )
                            Row {
                                repeat(review.rating) {
                                    Text("★", color = SewainColors.Star, fontSize = 14.sp)
                                }
                                repeat(5 - review.rating) {
                                    Text("☆", color = SewainColors.Star, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = review.text,
                        fontFamily = FontFamily.Default,
                        fontSize = 13.sp,
                        color = SewainColors.Black,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

// ============================================================
// BOTTOM ACTION BAR
// ============================================================
@Composable
fun BottomActionBar(
    onAddToCart: () -> Unit,
    onRentNow: () -> Unit
) {
    Surface(
        shadowElevation = 8.dp,
        color = SewainColors.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Tombol Add to Cart
            OutlinedButton(
                onClick = onAddToCart,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, SewainColors.Black),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = SewainColors.TextDark
                )
            ) {
                Text(
                    text = "Keranjang",
                    fontFamily = Volkhov,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }

            // Tombol Rent Now
            Button(
                onClick = onRentNow,
                modifier = Modifier
                    .weight(2f)
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SewainColors.Primary,
                    contentColor   = SewainColors.Black
                ),
                border = BorderStroke(0.5.dp, SewainColors.Black)
            ) {
                Text(
                    text = "Sewa Sekarang!",
                    fontFamily = Volkhov,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProductDetailScreenPreview() {
    ProductDetailScreen()
}