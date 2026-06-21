package com.l0124005.sewain_rpl.ui.theme

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.l0124005.sewain_rpl.network.CatalogData
import com.l0124005.sewain_rpl.network.KeranjangItem
import com.l0124005.sewain_rpl.repository.AuthRepository
import com.l0124005.sewain_rpl.repository.KatalogRepository
import com.l0124005.sewain_rpl.repository.KeranjangRepository
import com.l0124005.sewain_rpl.repository.ProfileRepository
import com.l0124005.sewain_rpl.repository.TransaksiRepository
import com.l0124005.sewain_rpl.ui.theme.auth.LoginScreen
import com.l0124005.sewain_rpl.ui.theme.checkout.CheckoutPaymentScreen
import com.l0124005.sewain_rpl.ui.theme.checkout.PaymentConfirmedScreen
import com.l0124005.sewain_rpl.ui.theme.checkout.CustomerInfo
import com.l0124005.sewain_rpl.ui.theme.katalog.AddItemScreen
import com.l0124005.sewain_rpl.ui.theme.katalog.DetailProdukActivity
import com.l0124005.sewain_rpl.ui.theme.katalog.EditItemScreen
import com.l0124005.sewain_rpl.ui.theme.katalog.KatalogScreen
import com.l0124005.sewain_rpl.ui.theme.katalog.MyKatalogScreen
import com.l0124005.sewain_rpl.ui.theme.keranjang.KeranjangScreen
import com.l0124005.sewain_rpl.ui.theme.profil.MyRentalsScreen
import com.l0124005.sewain_rpl.ui.theme.profil.MyWalletScreen
import com.l0124005.sewain_rpl.ui.theme.profil.ProfileScreen
import com.l0124005.sewain_rpl.ui.theme.profil.RentalsOwnerScreen
import com.l0124005.sewain_rpl.ui.theme.transaksi.DetailTransaksiScreen
import com.l0124005.sewain_rpl.ui.theme.transaksi.PembayaranScreen
import com.l0124005.sewain_rpl.utils.SessionManager
import com.l0124005.sewain_rpl.utils.Resource
import com.l0124005.sewain_rpl.viewmodel.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

enum class Screen {
    Home, Rental, Keranjang, MyKatalog, Profile, RiwayatTransaksi, AddItem, EditItem, DetailTransaksi, Pembayaran, MyRental, RentalsOwner, CheckoutPayment, Confirm, MyWallet, Settings, RentalDetail, FormPengembalian
}

class MainActivity : ComponentActivity() {
    private val profileViewModel: ProfileViewModel by viewModels { ProfileViewModelFactory(ProfileRepository()) }
    private val katalogViewModel: KatalogViewModel by viewModels { KatalogViewModelFactory(KatalogRepository()) }
    private val transaksiViewModel: TransaksiViewModel by viewModels { TransaksiViewModelFactory(TransaksiRepository()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Sewain_rplTheme {
                val sessionManager = remember { SessionManager(this) }
                val token = sessionManager.getToken()

                if (token.isEmpty()) {
                    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(AuthRepository(sessionManager)))
                    LoginScreen(
                        viewModel = authViewModel,
                        onLoginSuccess = { recreate() },
                        onNavigateToRegister = { /* Handle navigation to register */ }
                    )
                } else {
                    MainContainer(
                        token = token,
                        profileViewModel = profileViewModel,
                        katalogViewModel = katalogViewModel,
                        transaksiViewModel = transaksiViewModel,
                        onLogout = {
                            sessionManager.clearSession()
                            recreate()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MainContainer(
    token: String,
    profileViewModel: ProfileViewModel,
    katalogViewModel: KatalogViewModel,
    transaksiViewModel: TransaksiViewModel,
    onLogout: () -> Unit
) {
    var currentScreen by remember { mutableStateOf(Screen.Home) }
    val context = LocalContext.current

    var selectedBarangForEdit by remember { mutableStateOf<CatalogData?>(null) }
    var selectedTransaksiId by remember { mutableStateOf<Int?>(null) }
    var selectedRentalDetailData by remember { mutableStateOf<com.l0124005.sewain_rpl.ui.theme.profil.RentalDetailData?>(null) }
    var selectedItemsForCheckout by remember { mutableStateOf<List<KeranjangItem>>(emptyList()) }
    
    val keranjangViewModel: KeranjangViewModel = viewModel(factory = KeranjangViewModelFactory(KeranjangRepository()))
    val kembalikanState by transaksiViewModel.kembalikanState.observeAsState()

    LaunchedEffect(kembalikanState) {
        when (kembalikanState) {
            is Resource.Success -> {
                Toast.makeText(context, "Pengembalian berhasil diajukan!", Toast.LENGTH_SHORT).show()
                transaksiViewModel.resetStates()
                currentScreen = Screen.MyRental
            }
            is Resource.Error -> {
                Toast.makeText(context, "Gagal: ${kembalikanState?.message}", Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    Scaffold(
        bottomBar = {
            if (currentScreen in listOf(Screen.Home, Screen.Rental, Screen.Keranjang, Screen.MyKatalog, Screen.Profile)) {
                BottomNavigationBar(
                    currentScreen = currentScreen,
                    onScreenSelected = { currentScreen = it }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (currentScreen) {
                Screen.Home -> RentalsScreen(
                    viewModel = katalogViewModel,
                    keranjangViewModel = keranjangViewModel,
                    token = token,
                    onItemClick = { barang ->
                        val intent = Intent(context, DetailProdukActivity::class.java).apply {
                            putExtra("EXTRA_PRODUCT_ID", barang.id)
                        }
                        context.startActivity(intent)
                    }
                )
                Screen.Rental -> KatalogScreen(
                    viewModel = katalogViewModel,
                    onProductClick = { productId ->
                        val intent = Intent(context, DetailProdukActivity::class.java).apply {
                            putExtra("EXTRA_PRODUCT_ID", productId)
                        }
                        context.startActivity(intent)
                    },
                    onCartClick = { currentScreen = Screen.Keranjang }
                )
                Screen.Keranjang -> KeranjangScreen(
                    token = token,
                    viewModel = keranjangViewModel,
                    onBack = { currentScreen = Screen.Home },
                    onCheckout = { items ->
                        selectedItemsForCheckout = items
                        currentScreen = Screen.CheckoutPayment
                    }
                )
                Screen.CheckoutPayment -> CheckoutPaymentScreen(
                    token = token,
                    keranjangViewModel = keranjangViewModel,
                    profileViewModel = profileViewModel,
                    transaksiViewModel = transaksiViewModel,
                    selectedItems = selectedItemsForCheckout,
                    onEditProfile = { },
                    onBack = { currentScreen = Screen.Keranjang },
                    onCheckoutStarted = { /* Handle logic */ }
                )
                Screen.Confirm -> PaymentConfirmedScreen(
                    orderNumber = "ORD-SUCCESS",
                    items = emptyList(),
                    shippingCost = 0,
                    customer = CustomerInfo("", "", "", "", ""),
                    onBackHome = { currentScreen = Screen.Home },
                    onPrintReceipt = { }
                )
                Screen.MyKatalog -> MyKatalogScreen(
                    viewModel = katalogViewModel,
                    profileViewModel = profileViewModel,
                    token = token,
                    onBack = { currentScreen = Screen.Profile },
                    onAddItem = { currentScreen = Screen.AddItem },
                    onEditItem = { barang ->
                        selectedBarangForEdit = barang
                        currentScreen = Screen.EditItem
                    }
                )
                Screen.AddItem -> AddItemScreen(
                    viewModel = katalogViewModel,
                    profileViewModel = profileViewModel,
                    token = token,
                    onBack = { currentScreen = Screen.MyKatalog },
                    onSuccess = { currentScreen = Screen.MyKatalog }
                )
                Screen.EditItem -> selectedBarangForEdit?.let { barang ->
                    EditItemScreen(
                        viewModel = katalogViewModel,
                        token = token,
                        itemId = barang.id,
                        onBack = { currentScreen = Screen.MyKatalog },
                        onSuccess = { currentScreen = Screen.MyKatalog }
                    )
                }
                Screen.Profile -> ProfileScreen(
                    viewModel = profileViewModel,
                    token = token,
                    onLogout = onLogout,
                    onEditProfile = { },
                    onMyRentalClick = { currentScreen = Screen.MyRental },
                    onRentalOwnerClick = { currentScreen = Screen.RentalsOwner },
                    onMyWalletClick = { currentScreen = Screen.MyWallet },
                    onSettingsClick = { Toast.makeText(context, "Settings soon", Toast.LENGTH_SHORT).show() }
                )
                Screen.MyRental -> MyRentalsScreen(
                    viewModel = transaksiViewModel,
                    profileViewModel = profileViewModel,
                    token = token,
                    onBack = { currentScreen = Screen.Profile },
                    onRentalsOwnerClick = { currentScreen = Screen.RentalsOwner },
                    onMyWalletClick = { currentScreen = Screen.MyWallet },
                    onLogoutClick = onLogout,
                    onSettingsClick = { },
                    onItemClick = { transaksi ->
                        selectedTransaksiId = transaksi.id
                        val barang = transaksi.barang
                        val statusEnum = when (com.l0124005.sewain_rpl.ui.theme.profil.RentalStatus.fromBackend(transaksi.status)) {
                            com.l0124005.sewain_rpl.ui.theme.profil.RentalStatus.ACTIVE -> com.l0124005.sewain_rpl.ui.theme.profil.RentalDetailStatus.ACTIVE
                            com.l0124005.sewain_rpl.ui.theme.profil.RentalStatus.RETURN -> com.l0124005.sewain_rpl.ui.theme.profil.RentalDetailStatus.RETURN
                            else -> com.l0124005.sewain_rpl.ui.theme.profil.RentalDetailStatus.COMPLETED
                        }
                        val imgBase = com.l0124005.sewain_rpl.network.ApiClient.IMAGE_BASE_URL
                        val productImg = if (!barang?.foto_barang.isNullOrEmpty()) "${imgBase}products/${barang?.foto_barang}" else ""

                        selectedRentalDetailData = com.l0124005.sewain_rpl.ui.theme.profil.RentalDetailData(
                            itemName = barang?.nama_barang ?: "-",
                            itemImageUrl = productImg,
                            status = statusEnum,
                            idTransaksi = "TRX-${transaksi.id}",
                            owner = "Owner",
                            user = "User",
                            denda = "-",
                            date = transaksi.tanggal_sewa,
                            receiptItemName = barang?.nama_barang ?: "-",
                            receiptItemImageUrl = productImg,
                            pricePerDay = "Rp ${transaksi.total_harga}",
                            isPaid = true,
                            tanggalMulai = transaksi.tanggal_sewa,
                            tanggalSelesai = transaksi.tanggal_kembali_rencana,
                            durasiSewa = "1 Hari",
                            subtotal = "Rp ${transaksi.total_harga}",
                            shipping = "Rp 0",
                            jaminan = "Rp 0",
                            total = "Rp ${transaksi.total_harga}",
                            catatanDenda = "-"
                        )
                        currentScreen = Screen.RentalDetail
                    }
                )
                Screen.RentalDetail -> selectedRentalDetailData?.let { detail ->
                    com.l0124005.sewain_rpl.ui.theme.profil.RentalDetailScreen(
                        detail = detail,
                        onAjukanPengembalian = { currentScreen = Screen.FormPengembalian }
                    )
                }
                Screen.FormPengembalian -> selectedRentalDetailData?.let { detail ->
                    com.l0124005.sewain_rpl.ui.theme.transaksi.FormPengembalianScreen(
                        detail = detail,
                        onBack = { currentScreen = Screen.RentalDetail },
                        onSubmit = { uri, rating, komentar ->
                            val id = selectedTransaksiId ?: return@FormPengembalianScreen
                            val ratingBody = rating.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                            val komentarBody = komentar.toRequestBody("text/plain".toMediaTypeOrNull())
                            
                            var fotoBukti: MultipartBody.Part? = null
                            uri?.let {
                                try {
                                    val file = File(context.cacheDir, "return_${id}_${System.currentTimeMillis()}.jpg")
                                    context.contentResolver.openInputStream(it)?.use { input ->
                                        file.outputStream().use { output -> input.copyTo(output) }
                                    }
                                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                                    fotoBukti = MultipartBody.Part.createFormData("foto_bukti", file.name, requestFile)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Gagal memproses gambar", Toast.LENGTH_SHORT).show()
                                }
                            }

                            fotoBukti?.let {
                                transaksiViewModel.kembalikanBarang(token, id, it, ratingBody, komentarBody)
                            } ?: Toast.makeText(context, "Foto bukti wajib diunggah", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                Screen.RentalsOwner -> RentalsOwnerScreen(
                    viewModel = transaksiViewModel,
                    profileViewModel = profileViewModel,
                    token = token,
                    onBack = { currentScreen = Screen.Profile },
                    onLogout = onLogout,
                    onProfileClick = { currentScreen = Screen.Profile },
                    onMyRentalClick = { currentScreen = Screen.MyRental },
                    onWalletClick = { currentScreen = Screen.MyWallet },
                    onRentalClick = { },
                    onSettingsClick = { }
                )
                Screen.MyWallet -> MyWalletScreen(
                    viewModel = profileViewModel,
                    transaksiViewModel = transaksiViewModel,
                    token = token,
                    onBack = { currentScreen = Screen.Profile },
                    onLogout = onLogout,
                    onMyRentalClick = { currentScreen = Screen.MyRental },
                    onRentalOwnerClick = { currentScreen = Screen.RentalsOwner },
                    onSettingsClick = { }
                )
                Screen.RiwayatTransaksi -> DetailTransaksiScreen(
                    viewModel = transaksiViewModel,
                    token = token,
                    transaksiId = selectedTransaksiId ?: 0,
                    onBack = { currentScreen = Screen.Profile },
                    onPayClick = { ids -> 
                        currentScreen = Screen.Pembayaran 
                    },
                    onProductClick = { id -> 
                        val intent = Intent(context, DetailProdukActivity::class.java).apply {
                            putExtra("EXTRA_PRODUCT_ID", id)
                        }
                        context.startActivity(intent)
                    }
                )
                Screen.Pembayaran -> PembayaranScreen(
                    viewModel = transaksiViewModel,
                    token = token,
                    transaksiIds = emptyList(),
                    onBack = { currentScreen = Screen.RiwayatTransaksi },
                    onPaymentSuccess = { currentScreen = Screen.RiwayatTransaksi }
                )
                else -> {}
            }
        }
    }
}

@Composable
fun BottomNavigationBar(currentScreen: Screen, onScreenSelected: (Screen) -> Unit) {
    NavigationBar(containerColor = Color.White) {
        val items = listOf(
            Triple(Screen.Home, "Home", Icons.Default.Home),
            Triple(Screen.Rental, "Rental", Icons.Default.Search),
            Triple(Screen.Keranjang, "Keranjang", Icons.Default.ShoppingCart),
            Triple(Screen.MyKatalog, "Katalog", Icons.AutoMirrored.Filled.List),
            Triple(Screen.Profile, "Profile", Icons.Default.Person)
        )
        items.forEach { (screen, label, icon) ->
            NavigationBarItem(
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label) },
                selected = currentScreen == screen,
                onClick = { onScreenSelected(screen) }
            )
        }
    }
}
