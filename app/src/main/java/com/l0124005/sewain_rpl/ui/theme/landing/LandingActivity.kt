package com.l0124005.sewain_rpl.ui.theme.landing

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.l0124005.sewain_rpl.ui.theme.Sewain_rplTheme
import com.l0124005.sewain_rpl.ui.theme.auth.LoginActivity
import kotlinx.coroutines.delay

class LandingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Sewain_rplTheme {
                LandingScreen(
                    onTimeout = {
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun LandingScreen(onTimeout: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2000)
        onTimeout()
    }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFFFFFF),
            Color(0xFFE4EDF5),
            Color(0xFF9FB5C4)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient),
        contentAlignment = Alignment.Center
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            // Diamond (rhombus) di belakang teks
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .rotate(45f)
                    .background(Color(0xFF8AA9BD))
            )

            // Teks SEWAIN di atas diamond
            Text(
                text = "SEWAIN",
                fontSize = 40.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2A33),
                letterSpacing = 2.sp
            )
        }
    }
}
