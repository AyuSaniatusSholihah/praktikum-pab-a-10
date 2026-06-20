package com.l0124005.sewain_rpl.ui.theme.katalog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.l0124005.sewain_rpl.network.CatalogData
import com.l0124005.sewain_rpl.utils.Resource
import com.l0124005.sewain_rpl.viewmodel.KatalogViewModel
import com.l0124005.sewain_rpl.ui.theme.BluePrimary
import com.l0124005.sewain_rpl.ui.theme.NavyPrimary
import com.l0124005.sewain_rpl.ui.theme.SewainTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KatalogScreen(
    viewModel: KatalogViewModel,
    onProductClick: (Int) -> Unit,
    onCartClick: () -> Unit
) {
    val katalogState by viewModel.katalogPublik.observeAsState()
    
    // Panggil data saat pertama kali screen dibuka
    LaunchedEffect(Unit) {
        viewModel.getKatalogPublik()
    }

    Scaffold(
        topBar = {
            SewainTopBar(
                onActionClick = onCartClick
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F9FA))
        ) {
            // Search Bar Placeholder
            OutlinedTextField(
                value = "",
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Cari barang yang ingin Anda sewa...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = BluePrimary) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = BluePrimary,
                    unfocusedBorderColor = Color(0xFFE8E8E8)
                )
            )

            when (katalogState) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BluePrimary)
                    }
                }
                is Resource.Success -> {
                    val products = katalogState?.data?.data ?: emptyList()
                    if (products.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Belum ada produk tersedia.")
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(products) { product ->
                                ProductCard(
                                    product = product, 
                                    onClick = { onProductClick(product.id) },
                                    onAddToCart = {
                                        // KatalogScreen tidak menangani cart, ini hanya placeholder
                                    }
                                )
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "Error: ${katalogState?.message}", color = Color.Red)
                    }
                }
                else -> {}
            }
        }
    }
}
