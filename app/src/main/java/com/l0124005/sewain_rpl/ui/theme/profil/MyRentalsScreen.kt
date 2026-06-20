package com.l0124005.sewain_rpl.ui.theme.profil

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.l0124005.sewain_rpl.ui.theme.transaksi.TransaksiItem
import com.l0124005.sewain_rpl.utils.Resource
import com.l0124005.sewain_rpl.viewmodel.TransaksiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyRentalsScreen(
    viewModel: TransaksiViewModel,
    token: String,
    onBack: () -> Unit
) {
    val transaksiState by viewModel.transaksiList.observeAsState()

    LaunchedEffect(Unit) {
        viewModel.getRiwayatTransaksi(token)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Rentals", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = transaksiState) {
                is Resource.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is Resource.Success -> {
                    val list = state.data?.data ?: emptyList()
                    if (list.isEmpty()) {
                        Text("No rentals found", modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(list) { transaksi ->
                                TransaksiItem(transaksi = transaksi, onClick = {})
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    Text(
                        text = state.message ?: "Error loading rentals",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {}
            }
        }
    }
}
