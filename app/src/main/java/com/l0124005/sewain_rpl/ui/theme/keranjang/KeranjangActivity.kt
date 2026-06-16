package com.l0124005.sewain_rpl.ui.theme.keranjang

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.l0124005.sewain_rpl.repository.KeranjangRepository
import com.l0124005.sewain_rpl.ui.theme.Sewain_rplTheme
import com.l0124005.sewain_rpl.utils.SessionManager
import com.l0124005.sewain_rpl.viewmodel.KeranjangViewModel
import com.l0124005.sewain_rpl.viewmodel.KeranjangViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel

class KeranjangActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Sewain_rplTheme {
                val sessionManager = SessionManager(this)
                val token = sessionManager.getToken() ?: ""
                val viewModel: KeranjangViewModel = viewModel(
                    factory = KeranjangViewModelFactory(KeranjangRepository())
                )
                
                KeranjangScreen(
                    token = token,
                    viewModel = viewModel,
                    onBack = { finish() }
                )
            }
        }
    }
}
