package com.l0124005.sewain_rpl.ui.theme.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.l0124005.sewain_rpl.network.RegisterRequest
import com.l0124005.sewain_rpl.repository.AuthRepository
import com.l0124005.sewain_rpl.ui.theme.Sewain_rplTheme
import com.l0124005.sewain_rpl.ui.theme.VidalokaFont
import com.l0124005.sewain_rpl.ui.theme.VolkhovFont
import com.l0124005.sewain_rpl.ui.theme.MonsterratFont
import com.l0124005.sewain_rpl.utils.Resource
import com.l0124005.sewain_rpl.utils.SessionManager
import com.l0124005.sewain_rpl.viewmodel.AuthViewModel
import com.l0124005.sewain_rpl.viewmodel.AuthViewModelFactory

// Warna sama seperti LoginActivity, diambil dari auth.css
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

private val AuthTextMutedRegister = Color(0xFF838383)
private val AuthInputBorderRegister = Color(0xFF9D9D9D)
private val AuthLogoDark = Color(0xFF484848)

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Sewain_rplTheme {
                val factory = AuthViewModelFactory(AuthRepository(SessionManager(this)))
                val viewModel: AuthViewModel = viewModel(factory = factory)
                SignUpScreen(
                    viewModel = viewModel,
                    onNavigateToLogin = {
                        finish()
                    },
                    onRegisterSuccess = { email ->
                        val intent = Intent(this, OtpActivity::class.java)
                        intent.putExtra("EXTRA_EMAIL", email)
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun SignUpScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: (String) -> Unit
) {
    val registerState by viewModel.registerState.observeAsState()
    val context = LocalContext.current

    LaunchedEffect(registerState) {
        when (registerState) {
            is Resource.Success -> {
                Toast.makeText(context, registerState?.data ?: "Akun berhasil terdaftar!", Toast.LENGTH_LONG).show()
            }
            is Resource.Error -> {
                Toast.makeText(context, registerState?.message ?: "Terjadi kesalahan", Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    SignUpScreenContent(
        registerState = registerState,
        onRegister = { req -> viewModel.register(req) },
        onNavigateToLogin = onNavigateToLogin,
        onSuccessRedirect = { email -> onRegisterSuccess(email) }
    )
}

@Composable
fun SignUpScreenContent(
    registerState: Resource<String>?,
    onRegister: (RegisterRequest) -> Unit,
    onNavigateToLogin: () -> Unit,
    onSuccessRedirect: (String) -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val context = LocalContext.current

    LaunchedEffect(registerState) {
        if (registerState is Resource.Success) {
            onSuccessRedirect(email)
        }
    }

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
                .padding(horizontal = 28.dp, vertical = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo "SEWAIN" — sama seperti landing page & login
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

            // Judul "Sign Up To SEWAIN" — meniru pola .auth-title, "SEWAIN" pakai font logo
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontFamily = VolkhovFont, color = AuthBlack)) {
                        append("Sign Up To ")
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

            Row(modifier = Modifier.fillMaxWidth()) {
                CustomRegisterTextField(
                    label = "FIRST NAME",
                    value = firstName,
                    onValueChange = { firstName = it },
                    placeholder = "First",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                CustomRegisterTextField(
                    label = "LAST NAME",
                    value = lastName,
                    onValueChange = { lastName = it },
                    placeholder = "Last",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            CustomRegisterTextField(
                label = "EMAIL",
                value = email,
                onValueChange = { email = it },
                placeholder = "yourname@email.com"
            )

            Spacer(modifier = Modifier.height(14.dp))

            CustomRegisterTextField(
                label = "PHONE NUMBER",
                value = phone,
                onValueChange = { phone = it },
                placeholder = "08123456789"
            )

            Spacer(modifier = Modifier.height(14.dp))

            CustomRegisterTextField(
                label = "PASSWORD",
                value = password,
                onValueChange = { password = it },
                placeholder = "••••••••",
                isPassword = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            CustomRegisterTextField(
                label = "CONFIRM PASSWORD",
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = "••••••••",
                isPassword = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Tombol Create Account
            Button(
                onClick = {
                    if (password != confirmPassword) {
                        Toast.makeText(context, "Password tidak cocok!", Toast.LENGTH_SHORT).show()
                    } else if (firstName.isNotEmpty() && lastName.isNotEmpty() && email.isNotEmpty() && phone.isNotEmpty() && password.isNotEmpty()) {
                        onRegister(
                            RegisterRequest(
                                first_name = firstName,
                                last_name = lastName,
                                email = email,
                                phone = phone,
                                password = password,
                                password_confirmation = confirmPassword
                            )
                        )
                    } else {
                        Toast.makeText(context, "Isi semua field!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AuthBlue),
                enabled = registerState !is Resource.Loading<*>
            ) {
                if (registerState is Resource.Loading<*>) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                } else {
                    Text(
                        text = "Create Account",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontFamily = MonsterratFont,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // "Already have an account? Login"
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = AuthBlack, fontWeight = FontWeight.Normal)) {
                        append("Already have an account? ")
                    }
                    withStyle(style = SpanStyle(color = AuthBlue, fontWeight = FontWeight.Bold)) {
                        append("Login")
                    }
                },
                fontSize = 13.sp,
                fontFamily = MonsterratFont,
                modifier = Modifier.clickable { onNavigateToLogin() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Footer kecil
            Text(
                text = "SEWAIN Terms & Conditions",
                color = AuthBlack,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.5.sp,
                fontFamily = MonsterratFont,
                modifier = Modifier.clickable { /* Handle T&C */ }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    Sewain_rplTheme {
        SignUpScreenContent(
            registerState = null,
            onRegister = {},
            onNavigateToLogin = {},
            onSuccessRedirect = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomRegisterTextField(
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
            color = AuthTextMutedRegister,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
        )
        
        // Menggunakan Box untuk membungkus BasicTextField agar bisa meniru OutlinedTextField 
        // tanpa menggunakan parameter yang menyebabkan error, tapi tetap dengan gaya underline.
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
        
        // Garis bawah (Underline) khas rbranch auth
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .background(AuthInputBorderRegister)
        )
    }
}
