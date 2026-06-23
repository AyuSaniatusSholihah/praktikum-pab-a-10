package com.l0124005.sewain_rpl.ui.theme.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.l0124005.sewain_rpl.repository.AuthRepository
import com.l0124005.sewain_rpl.ui.theme.MainActivity
import com.l0124005.sewain_rpl.ui.theme.Sewain_rplTheme
import com.l0124005.sewain_rpl.ui.theme.VidalokaFont
import com.l0124005.sewain_rpl.ui.theme.VolkhovFont
import com.l0124005.sewain_rpl.ui.theme.MonsterratFont
import com.l0124005.sewain_rpl.utils.Resource
import com.l0124005.sewain_rpl.utils.SessionManager
import com.l0124005.sewain_rpl.viewmodel.AuthViewModel
import com.l0124005.sewain_rpl.viewmodel.AuthViewModelFactory
import androidx.compose.ui.graphics.Brush

// Warna diambil dari auth.css ( --blue: #6A87A1; --black: #000000; --bg: #f4f4f4; )
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

private val AuthTextMuted = Color(0xFF838383)
private val AuthInputBorder = Color(0xFF9D9D9D)
private val AuthLogoDark = Color(0xFF484848) 

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Sewain_rplTheme {
                val factory = AuthViewModelFactory(AuthRepository(SessionManager(this)))
                val viewModel: AuthViewModel = viewModel(factory = factory)

                LoginScreen(
                    viewModel = viewModel,
                    onNavigateToRegister = {
                        startActivity(Intent(this, RegisterActivity::class.java))
                    },
                    onLoginSuccess = {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val loginState by viewModel.loginState.observeAsState()
    val context = LocalContext.current
    var lastTypedEmail by remember { mutableStateOf("") }

    LaunchedEffect(loginState) {
        when (loginState) {
            is Resource.Success<*> -> {
                Toast.makeText(context, "Login Berhasil!", Toast.LENGTH_SHORT).show()
                onLoginSuccess()
            }
            is Resource.Error<*> -> {
                val errorMsg = (loginState as Resource.Error<*>).message ?: "Error"
                if (errorMsg.contains("403")) {
                    Toast.makeText(context, "Akun belum terverifikasi. Silakan verifikasi OTP.", Toast.LENGTH_LONG).show()
                    val intent = Intent(context, OtpLoginActivity::class.java)
                    intent.putExtra("EXTRA_EMAIL", lastTypedEmail)
                    context.startActivity(intent)
                } else {
                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                }
            }
            else -> {}
        }
    }

    LoginScreenContent(
        loginState = loginState,
        onLogin = { email, password -> 
            lastTypedEmail = email
            viewModel.login(email, password) 
        },
        onNavigateToRegister = onNavigateToRegister
    )
}

@Composable
fun LoginScreenContent(
    loginState: Resource<String>?,
    onLogin: (String, String) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .background(sideVignetteBrush())
            .background(AuthVerticalFadeBrush),
        contentAlignment = Alignment.Center
    ) {
        // Card putih di tengah, meniru .auth-card pada versi mobile (sidebar foto disembunyikan)
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .background(Color.White, RoundedCornerShape(14.dp))
                .padding(horizontal = 28.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo "SEWAIN" — sama seperti landing page (font Vidaloka, "SEWA" abu gelap + "IN" aksen biru)
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

            Spacer(modifier = Modifier.height(6.dp))

            // Judul "Sign In To SEWAIN" — meniru .auth-title (font Volkhov)
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontFamily = VolkhovFont, color = AuthBlack)) {
                        append("Sign In To ")
                    }
                    withStyle(style = SpanStyle(fontFamily = VidalokaFont, color = AuthLogoDark)) {
                        append("SEWA")
                    }
                    withStyle(style = SpanStyle(fontFamily = VidalokaFont, color = AuthBlue)) {
                        append("IN")
                    }
                },
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(28.dp))

            CustomTextField(
                label = "EMAIL",
                value = email,
                onValueChange = { email = it },
                placeholder = "yourname@email.com"
            )

            Spacer(modifier = Modifier.height(14.dp))

            CustomTextField(
                label = "PASSWORD",
                value = password,
                onValueChange = { password = it },
                placeholder = "••••••••",
                isPassword = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Tombol Sign In — meniru .btn-auth (solid biru, pill-ish radius 7px di web -> 10dp di sini)
            Button(
                onClick = {
                    if (email.isNotEmpty() && password.isNotEmpty()) {
                        onLogin(email, password)
                    } else {
                        Toast.makeText(context, "Isi semua field!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AuthBlue),
                enabled = loginState !is Resource.Loading<*>
            ) {
                if (loginState is Resource.Loading<*>) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                } else {
                    Text(
                        text = "Sign In",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontFamily = MonsterratFont,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Register Now — meniru .btn-auth-outline (outline biru)
            OutlinedButton(
                onClick = onNavigateToRegister,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, AuthBlue),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AuthBlue)
            ) {
                Text(
                    text = "Register Now",
                    fontSize = 14.sp,
                    fontFamily = MonsterratFont,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Footer kecil — meniru .auth-footer
            Text(
                text = "SEWAIN Terms & Conditions",
                fontSize = 10.sp,
                fontFamily = MonsterratFont,
                fontWeight = FontWeight.SemiBold,
                color = AuthBlack,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    Sewain_rplTheme {
        LoginScreenContent(
            loginState = null,
            onLogin = { _, _ -> },
            onNavigateToRegister = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontFamily = MonsterratFont,
            fontWeight = FontWeight.Bold,
            color = AuthTextMuted,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    color = Color(0xFF6B6B6B),
                    fontFamily = MonsterratFont,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            androidx.compose.foundation.text.BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 14.sp,
                    fontFamily = MonsterratFont,
                    color = Color(0xFF21394F)
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .background(AuthInputBorder)
        )
    }
}