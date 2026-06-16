package com.l0124005.sewain_rpl

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.l0124005.sewain_rpl.repository.*
import com.l0124005.sewain_rpl.ui.theme.Sewain_rplTheme
import com.l0124005.sewain_rpl.ui.theme.auth.LoginActivity
import com.l0124005.sewain_rpl.ui.theme.RentalsScreen
import com.l0124005.sewain_rpl.ui.theme.katalog.DetailProdukActivity
import com.l0124005.sewain_rpl.ui.theme.keranjang.KeranjangScreen
import com.l0124005.sewain_rpl.ui.theme.profil.ProfileScreen
import com.l0124005.sewain_rpl.ui.theme.transaksi.RiwayatTransaksiScreen
import com.l0124005.sewain_rpl.ui.theme.transaksi.DetailTransaksiScreen
import com.l0124005.sewain_rpl.ui.theme.transaksi.PembayaranScreen
import com.l0124005.sewain_rpl.ui.theme.admin.OwnerDashboardScreen
import com.l0124005.sewain_rpl.ui.theme.katalog.AddItemScreen
import com.l0124005.sewain_rpl.ui.theme.katalog.EditItemScreen
import com.l0124005.sewain_rpl.ui.theme.katalog.MyKatalogScreen
import com.l0124005.sewain_rpl.utils.Resource
import com.l0124005.sewain_rpl.utils.SessionManager
import com.l0124005.sewain_rpl.viewmodel.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val sessionManager = SessionManager(this)

        if (!sessionManager.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        enableEdgeToEdge()
        setContent {
            Sewain_rplTheme {
                MainContainer(sessionManager)
            }
        }
    }

    @Composable
    fun MainContainer(sessionManager: SessionManager) {
        var selectedTab by remember { mutableIntStateOf(0) }
        val token = sessionManager.getToken() ?: ""

        val katalogViewModel: KatalogViewModel = viewModel(factory = KatalogViewModelFactory(KatalogRepository()))
        val keranjangViewModel: KeranjangViewModel = viewModel(factory = KeranjangViewModelFactory(KeranjangRepository()))
        val transaksiViewModel: TransaksiViewModel = viewModel(factory = TransaksiViewModelFactory(TransaksiRepository()))
        val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(ProfileRepository()))

        Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text("Home") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) },
                        label = { Text("Keranjang") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Default.History, contentDescription = null) },
                        label = { Text("Transaksi") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                        label = { Text("Owner") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        icon = { Icon(Icons.Default.Person, contentDescription = null) },
                        label = { Text("Profil") }
                    )
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                when (selectedTab) {
                    0 -> RentalsScreen(
                        viewModel = katalogViewModel,
                        keranjangViewModel = keranjangViewModel,
                        token = token,
                        onItemClick = { product ->
                            val intent = Intent(this@MainActivity, DetailProdukActivity::class.java)
                            intent.putExtra("EXTRA_PRODUCT_ID", product.id)
                            startActivity(intent)
                        }
                    )
                    1 -> {
                        var showCheckoutPayment by remember { mutableStateOf<List<Int>?>(null) }
                        
                        val checkoutState by transaksiViewModel.checkoutState.observeAsState()
                        
                        LaunchedEffect(checkoutState) {
                            if (checkoutState is Resource.Success) {
                                showCheckoutPayment = checkoutState?.data?.data?.transaksi?.map { it.id }
                            }
                        }

                        if (showCheckoutPayment != null) {
                            PembayaranScreen(
                                viewModel = transaksiViewModel,
                                token = token,
                                transaksiIds = showCheckoutPayment!!,
                                onBack = { showCheckoutPayment = null },
                                onPaymentSuccess = {
                                    showCheckoutPayment = null
                                    selectedTab = 2 // Go to history
                                }
                            )
                        } else {
                            KeranjangScreen(
                                viewModel = keranjangViewModel,
                                token = token,
                                onBack = { selectedTab = 0 }
                            )
                        }
                    }
                    2 -> {
                        var showDetail by remember { mutableStateOf<Int?>(null) }
                        var showPayment by remember { mutableStateOf<List<Int>?>(null) }

                        if (showPayment != null) {
                            PembayaranScreen(
                                viewModel = transaksiViewModel,
                                token = token,
                                transaksiIds = showPayment ?: emptyList(),
                                onBack = { showPayment = null },
                                onPaymentSuccess = {
                                    showPayment = null
                                    showDetail = null
                                    transaksiViewModel.getRiwayatTransaksi(token)
                                }
                            )
                        } else if (showDetail != null) {
                            DetailTransaksiScreen(
                                viewModel = transaksiViewModel,
                                token = token,
                                transaksiId = showDetail ?: 0,
                                onBack = { showDetail = null },
                                onPayClick = { ids: List<Int> -> showPayment = ids }
                            )
                        } else {
                            RiwayatTransaksiScreen(
                                viewModel = transaksiViewModel,
                                token = token,
                                onBack = { selectedTab = 0 },
                                onDetailClick = { id -> showDetail = id }
                            )
                        }
                    }
                    3 -> {
                        var showManageKatalog by remember { mutableStateOf(false) }
                        var showDetailId by remember { mutableStateOf<Int?>(null) }
                        var showAddItem by remember { mutableStateOf(false) }
                        var showEditId by remember { mutableStateOf<Int?>(null) }

                        if (showAddItem) {
                            AddItemScreen(
                                viewModel = katalogViewModel,
                                token = token,
                                onBack = { showAddItem = false },
                                onSuccess = {
                                    showAddItem = false
                                    katalogViewModel.getMyKatalog(token)
                                }
                            )
                        } else if (showEditId != null) {
                            EditItemScreen(
                                viewModel = katalogViewModel,
                                token = token,
                                itemId = showEditId!!,
                                onBack = { showEditId = null },
                                onSuccess = {
                                    showEditId = null
                                    katalogViewModel.getMyKatalog(token)
                                }
                            )
                        } else if (showManageKatalog) {
                            MyKatalogScreen(
                                viewModel = katalogViewModel,
                                token = token,
                                onBack = { showManageKatalog = false },
                                onEditItem = { item -> showEditId = item.id },
                                onAddItem = { showAddItem = true }
                            )
                        } else if (showDetailId != null) {
                            DetailTransaksiScreen(
                                viewModel = transaksiViewModel,
                                token = token,
                                transaksiId = showDetailId!!,
                                onBack = { showDetailId = null },
                                onPayClick = {}
                            )
                        } else {
                            OwnerDashboardScreen(
                                viewModel = transaksiViewModel,
                                token = token,
                                onManageKatalog = { showManageKatalog = true },
                                onTransaksiDetail = { id -> showDetailId = id }
                            )
                        }
                    }
                    4 -> ProfileScreen(
                        viewModel = profileViewModel,
                        token = token,
                        onLogout = {
                            sessionManager.clearSession()
                            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                            finish()
                        },
                        onEditProfile = { /* Navigate to edit profile */ }
                    )
                }
            }
        }
    }
}
