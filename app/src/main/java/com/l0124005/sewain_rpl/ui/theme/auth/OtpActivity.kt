package com.l0124005.sewain_rpl.ui.theme.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.l0124005.sewain_rpl.repository.AuthRepository
import com.l0124005.sewain_rpl.ui.theme.Sewain_rplTheme
import com.l0124005.sewain_rpl.ui.theme.landing.VidalokaFont
import com.l0124005.sewain_rpl.ui.theme.landing.VolkhovFont
import com.l0124005.sewain_rpl.utils.Resource
import com.l0124005.sewain_rpl.utils.SessionManager
import com.l0124005.sewain_rpl.viewmodel.AuthViewModel
import com.l0124005.sewain_rpl.viewmodel.AuthViewModelFactory

// ── Warna konsisten dengan LoginActivity ──────────────────────
private val AuthBlue          = Color(0xFF6A87A1)
private val AuthBlack         = Color(0xFF000000)
private val AuthLogoDark      = Color(0xFF484848)
private val AuthTextMuted     = Color(0xFF838383)
private val AuthInputBorder   = Color(0xFF9D9D9D)
private val OtpBoxBg          = Color(0xFFE8E8E8)
private val OtpBoxFocusBg     = Color(0xFFD8E4ED)

// Gradient sama persis dengan LoginActivity
private val AuthBackgroundGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFE0E0E0),
        Color(0xFF6A87A1),
        Color(0xFF21394F)
    )
)

// ============================================================
// ACTIVITY
// ============================================================
class OtpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val email = intent.getStringExtra("EXTRA_EMAIL") ?: ""

        setContent {
            Sewain_rplTheme {
                val factory   = AuthViewModelFactory(AuthRepository(SessionManager(this)))
                val viewModel : AuthViewModel = viewModel(factory = factory)

                OtpScreen(
                    email     = email,
                    viewModel = viewModel,
                    onVerificationSuccess = {
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

// ============================================================
// STATEFUL WRAPPER
// ============================================================
@Composable
fun OtpScreen(
    email: String,
    viewModel: AuthViewModel,
    onVerificationSuccess: () -> Unit
) {
    val otpState by viewModel.otpState.observeAsState()
    val context  = LocalContext.current

    LaunchedEffect(otpState) {
        when (otpState) {
            is Resource.Success -> {
                Toast.makeText(context, "Verifikasi Berhasil! Silakan Login.", Toast.LENGTH_LONG).show()
                onVerificationSuccess()
            }
            is Resource.Error -> {
                Toast.makeText(context, otpState?.message ?: "OTP Salah", Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    OtpScreenContent(
        email       = email,
        otpState    = otpState,
        onVerify    = { code -> viewModel.verifyOtp(email, code) },
        onResendOtp = { viewModel.resendOtp(email) }
    )
}

// ============================================================
// STATELESS UI — tema sesuai LoginActivity
// ============================================================
@Composable
fun OtpScreenContent(
    email: String,
    otpState: Resource<String>?,
    onVerify: (String) -> Unit,
    onResendOtp: () -> Unit
) {
    // 4 kotak OTP terpisah, sama seperti HTML (id otp1–otp4)
    val digits       = remember { mutableStateListOf("", "", "", "") }
    val focusRequests = remember { List(4) { FocusRequester() } }
    val focusManager  = LocalFocusManager.current
    val context       = LocalContext.current

    // Gradient background — sama dengan Login
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AuthBackgroundGradient),
        contentAlignment = Alignment.Center
    ) {
        // Card putih di tengah — sama struktur dengan Login
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .background(Color.White, RoundedCornerShape(14.dp))
                .padding(horizontal = 28.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Logo "SEWAIN" ─────────────────────────────────────
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = AuthLogoDark)) { append("SEWA") }
                    withStyle(SpanStyle(color = AuthBlue))     { append("IN")   }
                },
                fontSize   = 30.sp,
                fontFamily = VidalokaFont,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(Modifier.height(6.dp))

            // ── Subtitle "Sign In To SEWAIN" — sama dengan Login ──
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontFamily = VolkhovFont, color = AuthBlack)) {
                        append("Sign In To ")
                    }
                    withStyle(SpanStyle(fontFamily = VidalokaFont, color = AuthLogoDark)) {
                        append("SEWA")
                    }
                    withStyle(SpanStyle(fontFamily = VidalokaFont, color = AuthBlue)) {
                        append("IN")
                    }
                },
                fontSize   = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(24.dp))

            // ── Ikon email (circle biru) ──────────────────────────
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(AuthBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Outlined.MarkEmailUnread,
                    contentDescription = "Email",
                    tint     = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(Modifier.height(14.dp))

            // ── Judul ─────────────────────────────────────────────
            Text(
                text       = "Enter The Confirmation Code",
                fontFamily = VolkhovFont,
                fontWeight = FontWeight.Bold,
                fontSize   = 15.sp,
                color      = AuthBlack,
                textAlign  = TextAlign.Center
            )

            Spacer(Modifier.height(6.dp))

            // ── Email hint ────────────────────────────────────────
            Text(
                text       = "We sent a 4-digit code to",
                fontFamily = MonsterratFont,
                fontSize   = 10.sp,
                color      = AuthTextMuted,
                textAlign  = TextAlign.Center
            )
            Text(
                text       = email,
                fontFamily = MonsterratFont,
                fontWeight = FontWeight.Bold,
                fontSize   = 11.sp,
                color      = AuthBlue,
                textAlign  = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            // ── 4 kotak OTP ───────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                digits.forEachIndexed { i, value ->
                    OtpBox(
                        value          = value,
                        focusRequester = focusRequests[i],
                        onValueChange  = { newVal ->
                            // Hanya terima 1 digit angka
                            val clean = newVal.filter { it.isDigit() }.take(1)
                            digits[i] = clean
                            // Auto-jump ke kotak berikutnya
                            if (clean.isNotEmpty() && i < 3) {
                                focusRequests[i + 1].requestFocus()
                            }
                            // Bila semua terisi, tutup keyboard
                            if (digits.all { it.isNotEmpty() }) {
                                focusManager.clearFocus()
                            }
                        },
                        onBackspace = {
                            // Mundur ke kotak sebelumnya saat backspace di kotak kosong
                            if (digits[i].isEmpty() && i > 0) {
                                digits[i - 1] = ""
                                focusRequests[i - 1].requestFocus()
                            }
                        }
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── Tombol "Verify Now" — gaya sama dengan "Sign In" ──
            Button(
                onClick = {
                    val code = digits.joinToString("")
                    if (code.length < 4) {
                        Toast.makeText(context, "Masukkan 4 digit kode OTP!", Toast.LENGTH_SHORT).show()
                    } else {
                        onVerify(code)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape  = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AuthBlue),
                enabled = otpState !is Resource.Loading
            ) {
                if (otpState is Resource.Loading) {
                    CircularProgressIndicator(
                        color    = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Text(
                        text       = "Verify Now",
                        color      = Color.White,
                        fontSize   = 14.sp,
                        fontFamily = MonsterratFont,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── Resend link ───────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text       = "Didn't receive a code? ",
                    fontFamily = MonsterratFont,
                    fontSize   = 10.sp,
                    color      = AuthBlack
                )
                Text(
                    text       = "Resend Now",
                    fontFamily = MonsterratFont,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 11.sp,
                    color      = AuthBlue,
                    modifier   = Modifier.clickable(onClick = onResendOtp)
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Footer ────────────────────────────────────────────
            Text(
                text      = "SEWAIN Terms & Conditions",
                fontSize  = 10.sp,
                fontFamily = MonsterratFont,
                fontWeight = FontWeight.SemiBold,
                color     = AuthBlack,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ============================================================
// COMPOSABLE: SATU KOTAK OTP
// Desain sama dengan CSS .otp-input (bg #E8E8E8, focus #D8E4ED + border biru)
// ============================================================
@Composable
fun OtpBox(
    value: String,
    focusRequester: FocusRequester,
    onValueChange: (String) -> Unit,
    onBackspace: () -> Unit
) {
    val isFocused = remember { mutableStateOf(false) }

    BasicTextField(
        value     = value,
        onValueChange = onValueChange,
        modifier  = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isFocused.value) OtpBoxFocusBg else OtpBoxBg)
            .border(
                width = if (isFocused.value) 2.dp else 0.dp,
                color = if (isFocused.value) AuthBlue else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .focusRequester(focusRequester)
            .onFocusChanged { isFocused.value = it.isFocused },
        singleLine   = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.NumberPassword,
            imeAction    = ImeAction.Next
        ),
        textStyle = TextStyle(
            fontFamily  = MonsterratFont,
            fontWeight  = FontWeight.Bold,
            fontSize    = 20.sp,
            color       = Color(0xFF333333),
            textAlign   = TextAlign.Center
        ),
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.Center) {
                innerTextField()
            }
        }
    )
}

// ============================================================
// PREVIEW
// ============================================================
@Preview(showBackground = true)
@Composable
fun OtpScreenPreview() {
    Sewain_rplTheme {
        OtpScreenContent(
            email       = "user@example.com",
            otpState    = null,
            onVerify    = {},
            onResendOtp = {}
        )
    }
}