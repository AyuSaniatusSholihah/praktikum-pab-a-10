package com.l0124005.sewain_rpl

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.l0124005.sewain_rpl.ui.theme.Sewain_rplTheme
import com.l0124005.sewain_rpl.ui.theme.auth.LoginActivity
import com.l0124005.sewain_rpl.ui.theme.landing.LandingActivity
import com.l0124005.sewain_rpl.utils.SessionManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val sessionManager = SessionManager(this)

        // 1. Cek Sesi: Jika belum login, arahkan ke LandingActivity
        if (!sessionManager.isLoggedIn()) {
            val intent = Intent(this, LandingActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        // 2. Jika sudah login, tampilkan layar utama sementara
        enableEdgeToEdge()
        setContent {
            Sewain_rplTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Selamat Datang!",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(text = "Anda berhasil masuk ke SEWAIN.")
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Button(onClick = {
                            sessionManager.clearSession()
                            val intent = Intent(this@MainActivity, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }) {
                            Text("Logout")
                        }
                    }
                }
            }
        }
    }
}
