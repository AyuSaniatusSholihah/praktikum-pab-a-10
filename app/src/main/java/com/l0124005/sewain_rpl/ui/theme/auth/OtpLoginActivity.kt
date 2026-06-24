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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.l0124005.sewain_rpl.repository.AuthRepository
import com.l0124005.sewain_rpl.ui.theme.Sewain_rplTheme
import com.l0124005.sewain_rpl.ui.theme.VidalokaFont
import com.l0124005.sewain_rpl.ui.theme.VolkhovFont
import com.l0124005.sewain_rpl.ui.theme.MonsterratFont
import com.l0124005.sewain_rpl.utils.Resource
import com.l0124005.sewain_rpl.utils.SessionManager
import com.l0124005.sewain_rpl.viewmodel.AuthViewModel
import com.l0124005.sewain_rpl.viewmodel.AuthViewModelFactory

private val AuthBlue = Color(0xFF6A87A1)
private val AuthBlack = Color(0xFF000000)
private val AuthTextMutedOtp = Color(0xFF838383)
private val AuthLogoDark = Color(0xFF484848)

private fun sideVignetteBrush(): Brush {
    return Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF21394F).copy(alpha = 0.35f),
            Color(0xFFFFFFFF).copy(alpha = 0f),
            Color(0xFF21394F).copy(alpha = 0.35f)
        )
    )
}

private val AuthVerticalFadeBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFFFFFF),
        Color(0xFFE0E0E0),
        Color(0xFF6A87A1),
        Color(0xFF21394F)
    )
)

class OtpLoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val email = intent.getStringExtra("EXTRA_EMAIL") ?: ""

        setContent {
            Sewain_rplTheme {
                val factory = AuthViewModelFactory(AuthRepository(SessionManager(this)))
                val viewModel: AuthViewModel = viewModel(factory = factory)

                OtpLoginScreen(
                    email = email,
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

@Composable
fun OtpLoginScreen(
    email: String,
    viewModel: AuthViewModel,
    onVerificationSuccess: () -> Unit
) {
    val otpState by viewModel.otpState.observeAsState()
    val context = LocalContext.current

    LaunchedEffect(otpState) {
        when (otpState) {
            is Resource.Success -> {
                Toast.makeText(context, "Akun Berhasil Diverifikasi! Silakan Login Kembali.", Toast.LENGTH_LONG).show()
                onVerificationSuccess()
            }
            is Resource.Error -> {
                Toast.makeText(context, (otpState as Resource.Error).message ?: "OTP Salah", Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    OtpLoginScreenContent(
        email = email,
        otpState = otpState,
        onVerify = { code -> viewModel.verifyOtp(email, code) },
        onResendOtp = { viewModel.resendOtp(email) }
    )
}

@Composable
fun OtpLoginScreenContent(
    email: String,
    otpState: Resource<String>?,
    onVerify: (String) -> Unit,
    onResendOtp: () -> Unit
) {
    var otp1 by remember { mutableStateOf("") }
    var otp2 by remember { mutableStateOf("") }
    var otp3 by remember { mutableStateOf("") }
    var otp4 by remember { mutableStateOf("") }
    val context = LocalContext.current

    val focusRequester2 = remember { FocusRequester() }
    val focusRequester3 = remember { FocusRequester() }
    val focusRequester4 = remember { FocusRequester() }

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

            Text(
                text = "Verification Required",
                fontSize = 15.sp,
                fontFamily = VolkhovFont,
                fontWeight = FontWeight.Bold,
                color = AuthBlack,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Your account is not verified yet.\nPlease enter the 4-digit code sent to\n$email",
                fontSize = 11.sp,
                fontFamily = MonsterratFont,
                fontWeight = FontWeight.SemiBold,
                color = AuthTextMutedOtp,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OtpDigitBox(value = otp1, onValueChange = {
                    if (it.length <= 1) {
                        otp1 = it
                        if (it.isNotEmpty()) focusRequester2.requestFocus()
                    }
                })
                OtpDigitBox(value = otp2, onValueChange = {
                    if (it.length <= 1) {
                        otp2 = it
                        if (it.isNotEmpty()) focusRequester3.requestFocus()
                    }
                }, focusRequester = focusRequester2)
                OtpDigitBox(value = otp3, onValueChange = {
                    if (it.length <= 1) {
                        otp3 = it
                        if (it.isNotEmpty()) focusRequester4.requestFocus()
                    }
                }, focusRequester = focusRequester3)
                OtpDigitBox(value = otp4, onValueChange = { if (it.length <= 1) otp4 = it }, focusRequester = focusRequester4)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val code = otp1 + otp2 + otp3 + otp4
                    if (code.length == 4) onVerify(code)
                    else Toast.makeText(context, "Please enter all 4 digits", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AuthBlue),
                enabled = otpState !is Resource.Loading
            ) {
                if (otpState is Resource.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                } else {
                    Text("Verify & Continue", color = Color.White, fontSize = 14.sp, fontFamily = MonsterratFont, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = AuthBlack, fontWeight = FontWeight.SemiBold)) { append("Didn't receive a code? ") }
                    withStyle(style = SpanStyle(color = AuthBlue, fontWeight = FontWeight.Bold)) { append("Resend Now") }
                },
                fontSize = 13.sp,
                fontFamily = MonsterratFont,
                modifier = Modifier.clickable { onResendOtp() }
            )
        }
    }
}

@Composable
private fun OtpDigitBox(
    value: String,
    onValueChange: (String) -> Unit,
    focusRequester: FocusRequester? = null
) {
    var isFocused by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(48.dp)
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
            .onFocusChanged { isFocused = it.isFocused }
            .background(
                color = if (isFocused) Color(0xFFD8E4ED) else Color(0xFFE8E8E8),
                shape = RoundedCornerShape(6.dp)
            )
            .border(
                width = if (isFocused) 1.5.dp else 0.dp,
                color = if (isFocused) AuthBlue else Color.Transparent,
                shape = RoundedCornerShape(6.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = value,
            onValueChange = { if (it.length <= 1) onValueChange(it) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            cursorBrush = SolidColor(AuthBlue),
            textStyle = TextStyle(
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                fontFamily = MonsterratFont,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF21394F)
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    innerTextField()
                }
            }
        )
    }
}
