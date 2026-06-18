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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.l0124005.sewain_rpl.R
import com.l0124005.sewain_rpl.ui.theme.Sewain_rplTheme
import com.l0124005.sewain_rpl.ui.theme.auth.LoginActivity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

// TODO: pastikan file font sudah ditaruh di res/font dengan nama "vidaloka"
// (contoh: res/font/vidaloka.ttf), lalu uncomment baris di bawah ini
// dan ganti FontFamily.Serif pada VidalokaFont jadi FontFamily(Font(R.font.vidaloka))
val VidalokaFont = FontFamily(Font(R.font.vidaloka_regular))

// Warna-warna ini diambil dari home.css agar konsisten dengan tema web
private val ColorBgSection = Color(0xFFF5F5F5)   // --color-bg-soft / --color-bg-section
private val ColorTextDark = Color(0xFF1A1A1A)    // --color-text
private val ColorTextMuted = Color(0xFF6B6B6B)   // --color-text-muted
private val ColorAccent = Color(0xFF6A87A1)      // warna span ".site-logo span" / brand accent
private val ColorBtn = Color(0xFF4D6674)         // --color-icon-bg / tombol (.btn-rent / .nav-signup base)

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBgSection),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
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
                    containerColor = ColorBtn,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(48.dp)
            ) {
                Text(
                    text = "Get Started",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

/**
 * "SEWA" warna gelap (--color-text), "IN" warna aksen (--color-icon-bg / span),
 * meniru pola ".site-logo span" pada CSS web.
 */
@Composable
private fun buildLogoStyledText(): AnnotatedString {
    return buildAnnotatedString {
        withStyle(style = SpanStyle(color = ColorTextDark)) {
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