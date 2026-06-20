package com.l0124005.sewain_rpl.ui.theme.keranjang

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.l0124005.sewain_rpl.R
import com.l0124005.sewain_rpl.utils.Resource
import com.l0124005.sewain_rpl.viewmodel.KeranjangViewModel
import com.l0124005.sewain_rpl.network.KeranjangItem
import com.l0124005.sewain_rpl.network.CatalogData
import com.l0124005.sewain_rpl.utils.CurrencyUtils
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily


// ── Tema warna sesuai CSS SEWAIN ──────────────────────────────
object CartColors {
    val Blue        = Color(0xFF6A87A1)
    val Black       = Color(0xFF000000)
    val Dark        = Color(0xFF484848)
    val Gray        = Color(0xFF8A8A8A)
    val BorderLight = Color(0x26000000)
    val BorderRow   = Color(0x63000000)
    val DividerRow  = Color(0xFFEDEDED)
    val ThumbBg     = Color(0xFFEDEDED)
    val DurBg       = Color(0xFFEEF3F7)
    val DurText     = Color(0xFF4A6E82)
    val FooterBg    = Color(0xFFFFFFFF)
}

object CartFonts {
    val Heading = FontFamily.Serif
    val MonsterratFont = FontFamily(Font(R.font.montserrat))
    val Body    = FontFamily.Default
}

// ── Helper ────────────────────────────────────────────────────
fun hitungDurasi(tglMulai: String, tglSelesai: String): Int {
    return try {
        val sdf  = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val d1   = sdf.parse(tglMulai)
        val d2   = sdf.parse(tglSelesai)
        val diff = ((d2?.time ?: 0L) - (d1?.time ?: 0L)) / (1000 * 60 * 60 * 24)
        if (diff > 0) diff.toInt() else 1
    } catch (e: Exception) { 1 }
}

fun formatTanggal(tgl: String): String {
    return try {
        val input  = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val output = SimpleDateFormat("dd MMM", Locale("id"))
        output.format(input.parse(tgl)!!)
    } catch (e: Exception) { tgl }
}

// ── Screen utama ──────────────────────────────────────────────
@Composable
fun KeranjangScreen(
    token: String,
    viewModel: KeranjangViewModel,
    onBack: () -> Unit,
    onCheckout: (List<KeranjangItem>) -> Unit = {}
) {
    val keranjangState by viewModel.keranjang.observeAsState()
    var checkedIds     by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var initialized    by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.getKeranjang(token) }

    LaunchedEffect(keranjangState) {
        val data = (keranjangState as? Resource.Success)?.data?.data
        if (data != null && !initialized) {
            checkedIds  = data.items.map { it.id }.toSet()
            initialized = true
        }
    }

    Surface(color = Color.White, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Header ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = CartColors.Black
                    )
                }
                Text(
                    text       = "Shopping Cart",
                    fontFamily = CartFonts.Heading,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 24.sp,
                    color      = CartColors.Black,
                    modifier   = Modifier.weight(1f),
                    textAlign  = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            HorizontalDivider(color = CartColors.DividerRow, thickness = 1.dp)

            when (val state = keranjangState) {
                is Resource.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = CartColors.Blue)
                    }
                }

                is Resource.Success -> {
                    val data = state.data?.data
                    if (data == null || data.items.isEmpty()) {
                        EmptyCartSection(modifier = Modifier.weight(1f), onBrowseProducts = onBack)
                    } else {
                        val allChecked = data.items.all { it.id in checkedIds }

                        // ── Pilih Semua ──
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 28.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = allChecked,
                                onCheckedChange = { checked ->
                                    checkedIds = if (checked) data.items.map { it.id }.toSet()
                                    else emptySet()
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor   = CartColors.Blue,
                                    uncheckedColor = CartColors.Gray
                                ),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text       = "Pilih semua",
                                fontSize   = 13.sp,
                                color      = CartColors.Gray,
                                fontFamily = CartFonts.Body
                            )
                        }

                        // ── Daftar item ──
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 28.dp)
                        ) {
                            items(data.items) { item ->
                                CartRow(
                                    item            = item,
                                    isChecked       = item.id in checkedIds,
                                    onCheckedChange = { checked ->
                                        checkedIds = if (checked) checkedIds + item.id
                                        else checkedIds - item.id
                                    },
                                    onRemove    = { viewModel.removeKeranjangItem(token, item.id) },
                                    onQtyChange = { /* tambahkan update qty ke viewModel jika ada */ }
                                )
                                HorizontalDivider(color = CartColors.DividerRow, thickness = 1.dp)
                            }
                        }

                        // ── Footer checkout ──
                        val selectedItems = data.items.filter { it.id in checkedIds }
                        val totalEstimasi = selectedItems.sumOf { it.subtotal }.toLong()

                        CheckoutSection(
                            total         = totalEstimasi,
                            selectedCount = selectedItems.size,
                            onCheckout    = {
                                if (selectedItems.isNotEmpty()) onCheckout(selectedItems)
                            }
                        )
                    }
                }

                is Resource.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message ?: "Terjadi kesalahan", color = Color.Red)
                    }
                }

                else -> {}
            }
        }
    }
}

// ── Item row ─────────────────────────────────────────────────
@Composable
private fun CartRow(
    item: KeranjangItem,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onRemove: () -> Unit,
    onQtyChange: (Int) -> Unit
) {
    val hari = hitungDurasi(item.tanggal_sewa, item.tanggal_kembali_rencana)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Checkbox
        Checkbox(
            checked         = isChecked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor   = CartColors.Blue,
                uncheckedColor = CartColors.Gray
            ),
            modifier = Modifier
                .size(20.dp)
                .padding(top = 28.dp)
        )

        Spacer(Modifier.width(10.dp))

        // Foto produk
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(CartColors.ThumbBg)
        ) {
            AsyncImage(
                model              = item.barang.foto_barang ?: "https://placehold.co/200",
                contentDescription = item.barang.nama_barang,
                modifier           = Modifier.fillMaxSize()
            )
        }

        Spacer(Modifier.width(14.dp))

        // Info produk
        Column(
            modifier            = Modifier.weight(1f),
            verticalArrangement = Arrangement.Top
        ) {
            // Nama + X bulat
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top
            ) {
                Text(
                    text       = item.barang.nama_barang,
                    fontFamily = CartFonts.Heading,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 15.sp,
                    color      = CartColors.Black,
                    lineHeight = 20.sp,
                    modifier   = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(CartColors.Dark.copy(alpha = 0.2f))
                        .clickable { onRemove() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = Icons.Default.Close,
                        contentDescription = "Hapus",
                        tint               = CartColors.Dark,
                        modifier           = Modifier.size(13.dp)
                    )
                }
            }

            Spacer(Modifier.height(3.dp))

            // Harga/hari
            Text(
                text       = "${CurrencyUtils.formatRupiah(item.barang.harga_sewa.toLong())}/hari",
                fontFamily = CartFonts.MonsterratFont,
                fontWeight = FontWeight.SemiBold,
                fontSize   = 11.sp,
                color      = CartColors.Dark
            )

            Spacer(Modifier.height(6.dp))

            // Badge durasi
            Surface(color = CartColors.DurBg, shape = RoundedCornerShape(4.dp)) {
                Text(
                    text       = "$hari hari sewa",
                    fontSize   = 12.sp,
                    color      = CartColors.DurText,
                    fontFamily = CartFonts.MonsterratFont,
                    fontWeight = FontWeight.SemiBold,
                    modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }

            Spacer(Modifier.height(4.dp))

            // Tanggal pill
            Row(verticalAlignment = Alignment.CenterVertically) {
                DatePill(formatTanggal(item.tanggal_sewa))
                Text("  →  ", fontSize = 20.sp, color = CartColors.Gray, fontWeight = FontWeight.ExtraBold)
                DatePill(formatTanggal(item.tanggal_kembali_rencana))
            }

            Spacer(Modifier.height(8.dp))

            // Qty −/+
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .border(1.dp, CartColors.DividerRow, RoundedCornerShape(4.dp))
                    .clip(RoundedCornerShape(4.dp))
            ) {
                IconButton(
                    onClick  = { if (item.jumlah > 1) onQtyChange(item.jumlah - 1) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Text("−", fontSize = 14.sp, color = CartColors.Dark)
                }
                Text(
                    text      = String.format("%02d", item.jumlah),
                    fontSize  = 13.sp,
                    color     = CartColors.Black,
                    modifier  = Modifier.widthIn(min = 32.dp),
                    textAlign = TextAlign.Center
                )
                IconButton(
                    onClick  = { onQtyChange(item.jumlah + 1) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Text("+", fontSize = 14.sp, color = CartColors.Dark)
                }
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text       = "Subtotal: ${CurrencyUtils.formatRupiah(item.subtotal.toLong())}",
                fontSize   = 11.sp,
                fontFamily = CartFonts.MonsterratFont,
                fontWeight = FontWeight.SemiBold,
                color      = CartColors.Gray
            )
        } // ← tutup Column info produk
    } // ← tutup Row CartRow
} // ← tutup fun CartRow  ✅ INI YANG HILANG DI VERSI LAMA

// ── Date pill ─────────────────────────────────────────────────
@Composable
private fun DatePill(label: String) {
    Surface(
        color = CartColors.DurBg,
        shape = RoundedCornerShape(3.dp)
    ) {
        Text(
            text     = label,
            fontSize = 10.sp,
            color    = CartColors.DurText,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

// ── Empty state ───────────────────────────────────────────────
@Composable
private fun EmptyCartSection(modifier: Modifier = Modifier, onBrowseProducts: () -> Unit) {
    Column(
        modifier            = modifier.fillMaxWidth().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.ShoppingCart,
            contentDescription = null,
            tint               = CartColors.Gray,
            modifier           = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text       = "Keranjang kamu kosong",
            fontSize   = 20.sp,
            fontFamily = CartFonts.Heading,
            color      = CartColors.Gray
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text           = "Lihat Produk",
            color          = CartColors.Blue,
            fontSize       = 14.sp,
            fontFamily     = CartFonts.Body,
            fontWeight     = FontWeight.Bold,
            textDecoration = TextDecoration.Underline,
            modifier       = Modifier
                .clickable { onBrowseProducts() }
                .padding(8.dp)
        )
    }
}

// ── Footer checkout ───────────────────────────────────────────
@Composable
private fun CheckoutSection(
    total: Long,
    selectedCount: Int,
    onCheckout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CartColors.FooterBg)
    ) {
        HorizontalDivider(color = CartColors.DividerRow, thickness = 1.dp)
        Column(modifier = Modifier.padding(horizontal = 28.dp, vertical = 18.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text       = "Total Estimasi",
                    fontFamily = CartFonts.Heading,
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 18.sp,
                    color      = CartColors.Black
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text       = CurrencyUtils.formatRupiah(total),
                        fontFamily = CartFonts.Heading,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 20.sp,
                        color      = CartColors.Black
                    )
                    Text(
                        text       = "$selectedCount item dipilih",
                        fontSize   = 11.sp,
                        color      = CartColors.Gray,
                        fontFamily = CartFonts.MonsterratFont,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Button(
                onClick  = onCheckout,
                enabled  = selectedCount > 0,
                shape    = RoundedCornerShape(6.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = CartColors.Blue,
                    disabledContainerColor = CartColors.Blue.copy(alpha = 0.4f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text       = "Checkout",
                    color      = Color.White,
                    fontFamily = CartFonts.MonsterratFont,
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 15.sp
                )
            }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────
@Preview(showBackground = true)
@Composable
fun KeranjangScreenPreview() {
    val dummyItem = KeranjangItem(
        id                      = 1,
        jumlah                  = 1,
        tanggal_sewa            = "2024-06-20",
        tanggal_kembali_rencana = "2024-06-22",
        subtotal                = 900000.0,
        barang = CatalogData(
            id                 = 1,
            user_id            = 1,
            kategori_id        = 1,
            nama_barang        = "ALLTREK Tenda Camping Tentastic Outdoor",
            deskripsi          = "Tenda berkualitas",
            harga_sewa         = 450000.0,
            harga_jaminan      = 200000.0,
            harga_denda_perjam = 15000.0,
            stok               = 5,
            lokasi             = "Bandung",
            foto_barang        = null,
            status             = "tersedia"
        )
    )

    Surface(color = Color.White) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header preview
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(Modifier.width(48.dp))
                Text(
                    text       = "Shopping Cart",
                    fontFamily = CartFonts.Heading,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 24.sp,
                    color      = CartColors.Black,
                    modifier   = Modifier.weight(1f),
                    textAlign  = TextAlign.Center
                )
                Spacer(Modifier.width(48.dp))
            }
            HorizontalDivider(color = CartColors.DividerRow)
            Row(
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked         = true,
                    onCheckedChange = {},
                    colors          = CheckboxDefaults.colors(checkedColor = CartColors.Blue),
                    modifier        = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text("Pilih semua", fontSize = 13.sp, color = CartColors.Gray)
            }
            HorizontalDivider(color = CartColors.DividerRow)
            Column(modifier = Modifier.padding(horizontal = 28.dp)) {
                CartRow(
                    item            = dummyItem,
                    isChecked       = true,
                    onCheckedChange = {},
                    onRemove        = {},
                    onQtyChange     = {}
                )
                HorizontalDivider(color = CartColors.DividerRow)
            }
            CheckoutSection(total = 900000L, selectedCount = 1, onCheckout = {})
        }
    }
}