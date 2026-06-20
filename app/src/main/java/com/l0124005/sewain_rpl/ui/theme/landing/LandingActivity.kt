package com.l0124005.sewain_rpl.ui.theme.landing

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.l0124005.sewain_rpl.R
import com.l0124005.sewain_rpl.ui.theme.Sewain_rplTheme
import com.l0124005.sewain_rpl.ui.theme.VidalokaFont
import com.l0124005.sewain_rpl.ui.theme.MontaguSlabFont
import com.l0124005.sewain_rpl.ui.theme.VolkhovFont
import com.l0124005.sewain_rpl.ui.theme.auth.LoginActivity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush

// Layer 1: vignette biru tua dari sisi kiri & kanan (radial, biar "nongol sedikit" di pinggir)
// Layer 1: vignette biru tua dari sisi kiri & kanan
private fun sideVignetteBrush(width: Float, height: Float): Brush {
    return Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF21394F).copy(alpha = 0.35f), // biru tua redup di kiri
            Color(0xFFFFFFFF).copy(alpha = 0f),    // transparan di tengah (gak nabrak teks)
            Color(0xFF21394F).copy(alpha = 0.35f)  // biru tua redup di kanan
        )
    )
}

// Layer 2: fade vertical, full gradasi dari atas sampai bawah, gak ada zona kosong
private val verticalFadeBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFFFFFF), // putih di paling atas
        Color(0xFFE0E0E0), // abu muda
        Color(0xFF4A7A9B), // BluePrimary (Brand Blue)
        Color(0xFF285473)  // NavyPrimary
    )
)

// Warna-warna ini diambil dari home.css agar konsisten dengan tema web
private val ColorBgSection = Color(0xFFF5F5F5)   // --color-bg-soft / --color-bg-section
private val ColorTextDark = Color(0xFF1A1A1A)    // --color-text
private val ColorTextMuted = Color(0xFF6B6B6B)   // --color-text-muted
private val ColorAccent = Color(0xFF4A7A9B)      // warna span ".site-logo span" / brand accent (Biru)
private val ColorBtn = Color(0xFF4A7A9B)         // Menggunakan Brand Blue untuk tombol

class LandingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Sewain_rplTheme {
                LandingScreen(
                    onGetStarted = {
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun LandingScreen(onGetStarted: () -> Unit) {

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White) // dasar putih
                .background(sideVignetteBrush(widthPx, heightPx)) // vignette kiri-kanan
                .background(verticalFadeBrush) // fade biru-abu di bawah
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo "SEWAIN" — font serif Vidaloka, tanpa diamond
                Text(
                    text = buildLogoStyledText(),
                    fontSize = 44.sp,
                    fontFamily = VidalokaFont,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 1.5.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Deskripsi singkat, mirip tagline website
                Text(
                    text = "Website penyewaan barang yang mudah, cepat, dan terpercaya untuk semua kebutuhanmu.",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = VolkhovFont,
                    color = ColorTextMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Tombol Get Started — warna sesuai .btn-rent / .nav-signup di CSS
                Button(
                    onClick = onGetStarted,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorAccent,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(50.dp)
                ) {
                    Text(
                        text = "Get Started",
                        fontSize = 16.sp,
                        fontFamily = MontaguSlabFont,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            } // tutup Column
        } // tutup Box
    } // tutup BoxWithConstraints
} // tutup fun LandingScreen

/**
 * "SEWA" warna gelap (--color-text), "IN" warna aksen (--color-icon-bg / span),
 * meniru pola ".site-logo span" pada CSS web.
 */
@Composable
private fun buildLogoStyledText(): AnnotatedString {
    return buildAnnotatedString {
        withStyle(style = SpanStyle(color = Color(0xFF484848))) {
            append("SEWA")
        }
        withStyle(style = SpanStyle(color = ColorAccent)) {
            append("IN")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LandingScreenPreview() {
    Sewain_rplTheme {
        LandingScreen(onGetStarted = {})
    }
}