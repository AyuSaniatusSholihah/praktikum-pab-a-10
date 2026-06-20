package com.l0124005.sewain_rpl.ui.theme.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.l0124005.sewain_rpl.repository.AuthRepository
import com.l0124005.sewain_rpl.ui.theme.Sewain_rplTheme
import com.l0124005.sewain_rpl.ui.theme.VidalokaFont
import com.l0124005.sewain_rpl.ui.theme.VolkhovFont
import com.l0124005.sewain_rpl.ui.theme.MonsterratFont
import com.l0124005.sewain_rpl.R
import com.l0124005.sewain_rpl.utils.Resource
import com.l0124005.sewain_rpl.utils.SessionManager
import com.l0124005.sewain_rpl.viewmodel.AuthViewModel
import com.l0124005.sewain_rpl.viewmodel.AuthViewModelFactory
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.FocusRequester

// Warna sama seperti Login/Register, diambil dari auth.css
private val AuthBlue = Color(0xFF6A87A1)
private val AuthBlack = Color(0xFF000000)

// Layer 1: vignette biru tua dari sisi kiri & kanan
private fun sideVignetteBrush(): Brush {
    return Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF21394F).copy(alpha = 0.35f),
            Color(0xFFFFFFFF).copy(alpha = 0f),
            Color(0xFF21394F).copy(alpha = 0.35f)
        )
    )
}

// Layer 2: fade vertical kompleks sesuai rbranch auth
private val AuthVerticalFadeBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFFFFFF),
        Color(0xFFE0E0E0),
        Color(0xFF6A87A1),
        Color(0xFF21394F)
    )
)

private val AuthTextMutedOtp = Color(0xFF838383)
private val AuthLogoDark = Color(0xFF484848)

class OtpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val email = intent.getStringExtra("EXTRA_EMAIL") ?: ""

        setContent {
            Sewain_rplTheme {
                val factory = AuthViewModelFactory(AuthRepository(SessionManager(this)))
                val viewModel: AuthViewModel = viewModel(factory = factory)

                OtpScreen(
                    email = email,
                    viewModel = viewModel,
                    onVerificationSuccess = {
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun OtpScreen(
    email: String,
    viewModel: AuthViewModel,
    onVerificationSuccess: () -> Unit
) {
    val otpState by viewModel.otpState.observeAsState()
    val context = LocalContext.current

    LaunchedEffect(otpState) {
        when (otpState) {
            is Resource.Success -> {
                Toast.makeText(context, "Verifikasi Berhasil! Silakan Login.", Toast.LENGTH_LONG)
                    .show()
                onVerificationSuccess()
            }

            is Resource.Error -> {
                Toast.makeText(context, otpState?.message ?: "OTP Salah", Toast.LENGTH_SHORT).show()
            }

            else -> {}
        }
    }

    OtpScreenContent(
        email = email,
        otpState = otpState,
        onVerify = { code -> viewModel.verifyOtp(email, code) },
        onResendOtp = { viewModel.resendOtp(email) }
    )
}

@Composable
fun OtpScreenContent(
    email: String,
    otpState: Resource<String>?,
    onVerify: (String) -> Unit,
    onResendOtp: () -> Unit
) {
    // 4 digit OTP terpisah, sama seperti .otp-row di web
    var otp1 by remember { mutableStateOf("") }
    var otp2 by remember { mutableStateOf("") }
    var otp3 by remember { mutableStateOf("") }
    var otp4 by remember { mutableStateOf("") }
    val context = LocalContext.current

    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val focusRequester2 = remember { androidx.compose.ui.focus.FocusRequester() }
    val focusRequester3 = remember { androidx.compose.ui.focus.FocusRequester() }
    val focusRequester4 = remember { androidx.compose.ui.focus.FocusRequester() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .background(sideVignetteBrush())
            .background(AuthVerticalFadeBrush),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .background(Color.White, RoundedCornerShape(14.dp))
                .padding(horizontal = 28.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo "SEWAIN" — sama seperti landing page, login, register
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = AuthLogoDark)) { append("SEWA") }
                    withStyle(style = SpanStyle(color = AuthBlue)) { append("IN") }
                },
                fontSize = 30.sp,
                fontFamily = VidalokaFont,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Ikon email bulat — meniru .email-icon-wrap .icon-circle
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(AuthBlue, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Email,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Judul — meniru .auth-title, font Volkhov
            Text(
                text = "Enter The Confirmation Code",
                fontSize = 15.sp,
                fontFamily = VolkhovFont,
                fontWeight = FontWeight.Bold,
                color = AuthBlack,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Subtitle — meniru .auth-subtitle
            Text(
                text = "We sent a 4-digit code to\n$email",
                fontSize = 11.sp,
                fontFamily = MonsterratFont,
                fontWeight = FontWeight.SemiBold,
                color = AuthTextMutedOtp,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 4 kotak OTP — meniru .otp-row .otp-input
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OtpDigitBox(
                    value = otp1,
                    onValueChange = {
                        if (it.length <= 1) {
                            otp1 = it
                            if (it.isNotEmpty()) focusRequester2.requestFocus()
                        }
                    }
                )
                OtpDigitBox(
                    value = otp2,
                    onValueChange = {
                        if (it.length <= 1) {
                            otp2 = it
                            if (it.isNotEmpty()) focusRequester3.requestFocus()
                        }
                    },
                    focusRequester = focusRequester2
                )
                OtpDigitBox(
                    value = otp3,
                    onValueChange = {
                        if (it.length <= 1) {
                            otp3 = it
                            if (it.isNotEmpty()) focusRequester4.requestFocus()
                        }
                    },
                    focusRequester = focusRequester3
                )
                OtpDigitBox(
                    value = otp4,
                    onValueChange = { if (it.length <= 1) otp4 = it },
                    focusRequester = focusRequester4
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tombol Verify Now — solid biru, sama seperti Sign In / Create Account
            Button(
                onClick = {
                    val code = otp1 + otp2 + otp3 + otp4
                    if (code.length == 4) {
                        onVerify(code)
                    } else {
                        Toast.makeText(
                            context,
                            "Masukkan kode OTP dengan lengkap!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AuthBlue),
                enabled = otpState !is Resource.Loading
            ) {
                if (otpState is Resource.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                } else {
                    Text(
                        text = "Verify Now",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontFamily = MonsterratFont,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // "Didn't receive a code? Resend Now" — meniru .auth-links
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = AuthBlack, fontWeight = FontWeight.Medium)) {
                        append("Didn't receive a code? ")
                    }
                    withStyle(style = SpanStyle(color = AuthBlue, fontWeight = FontWeight.Bold)) {
                        append("Resend Now")
                    }
                },
                fontSize = 13.sp,
                fontFamily = MonsterratFont,
                modifier = Modifier.clickable { onResendOtp() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Footer kecil — meniru .auth-footer
            Text(
                text = "SEWAIN Terms & Conditions",
                color = AuthBlack,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.5.sp,
                fontFamily = MonsterratFont
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OtpDigitBox(
    value: String,
    onValueChange: (String) -> Unit,
    focusRequester: FocusRequester? = null
) {
    Box(
        modifier = if (focusRequester != null) {
            Modifier
                .size(48.dp)
                .focusRequester(focusRequester)
        } else {
            Modifier.size(48.dp)
        },
        contentAlignment = Alignment.Center
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxSize(),
            singleLine = true,
            shape = RoundedCornerShape(6.dp),
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
                fontFamily = MonsterratFont,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF21394F)
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFD8E4ED),
                unfocusedContainerColor = Color(0xFFE8E8E8),
                focusedBorderColor = AuthBlue,
                unfocusedBorderColor = Color.Transparent
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OtpScreenPreview() {
    Sewain_rplTheme {
        OtpScreenContent(
            email = "user@example.com",
            otpState = null,
            onVerify = {},
            onResendOtp = {}
        )
    }
}