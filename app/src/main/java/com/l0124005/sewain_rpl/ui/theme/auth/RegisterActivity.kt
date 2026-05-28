package com.l0124005.sewain_rpl.ui.theme.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.l0124005.sewain_rpl.MainActivity
  import com.l0124005.sewain_rpl.network.RegisterRequest
import com.l0124005.sewain_rpl.repository.AuthRepository
import com.l0124005.sewain_rpl.ui.theme.Sewain_rplTheme
import com.l0124005.sewain_rpl.utils.Resource
import com.l0124005.sewain_rpl.utils.SessionManager
import com.l0124005.sewain_rpl.viewmodel.AuthViewModel
import com.l0124005.sewain_rpl.viewmodel.AuthViewModelFactory

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
                    onRegisterSuccess = {
                        startActivity(Intent(this, MainActivity::class.java))
                        finishAffinity()
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
    onRegisterSuccess: () -> Unit
) {
    // State untuk menyimpan input teks pengguna
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val registerState by viewModel.registerState.observeAsState()
    val loginState by viewModel.loginState.observeAsState()
    val context = LocalContext.current

    // Konfigurasi Google Sign In
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("YOUR_WEB_CLIENT_ID.apps.googleusercontent.com") // GANTI DENGAN WEB CLIENT ID ANDA
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                viewModel.googleLogin(idToken)
            }
        } catch (e: ApiException) {
            Log.e("GoogleSignIn", "Sign in failed: ${e.statusCode}")
            Toast.makeText(context, "Google Sign In Failed", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(registerState, loginState) {
        val state = registerState ?: loginState
        when (state) {
            is Resource.Success<*> -> {
                Toast.makeText(context, state.data?.toString() ?: "Berhasil!", Toast.LENGTH_SHORT).show()
                onRegisterSuccess()
            }
            is Resource.Error<*> -> {
                Toast.makeText(context, state.message ?: "Error", Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    // Warna kustom berdasarkan desain
    val darkBlueBorder = Color(0xFF285473)
    val textGray = Color(0xFF8C8C8C)
    val linkBlue = Color(0xFF1E5276)

    // Latar belakang gradient
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFE4EDF5), // Biru sangat muda di atas
            Color(0xFFFFFFFF)  // Putih di bawah
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(64.dp))

            // --- Header & Logo ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "SEWA",
                    fontSize = 38.sp,
                    fontFamily = FontFamily.Serif,
                    color = Color(0xFF2A2A2A)
                )
                Text(
                    text = "IN",
                    fontSize = 38.sp,
                    fontFamily = FontFamily.Serif,
                    color = Color(0xFF5D8AA8) // Warna biru pudar untuk "IN"
                )
            }
            Text(
                text = "Sign UP To SEWAIN",
                fontSize = 14.sp,
                fontFamily = FontFamily.Serif,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- Tombol Sign up with Google ---
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clickable {
                        launcher.launch(googleSignInClient.signInIntent)
                    },
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(1.dp, darkBlueBorder)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("G", color = Color(0xFF4285F4), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Sign up with Google",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- Form Input Grid ---
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CustomTextField(
                    label = "FIRST NAME",
                    value = firstName,
                    onValueChange = { firstName = it },
                    placeholder = "Tes",
                    modifier = Modifier.weight(1f)
                )
                CustomTextField(
                    label = "LAST NAME",
                    value = lastName,
                    onValueChange = { lastName = it },
                    placeholder = "Tes",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CustomTextField(
                    label = "EMAIL",
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "ayu.saniatus@gmail.com",
                    modifier = Modifier.weight(1f)
                )
                CustomTextField(
                    label = "PHONE",
                    value = phone,
                    onValueChange = { phone = it },
                    placeholder = "09957042947XX",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CustomTextField(
                    label = "PASSWORD",
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "••••••••",
                    isPassword = true,
                    modifier = Modifier.weight(1f)
                )
                CustomTextField(
                    label = "CONFIRM PASSWORD",
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = "••••••••",
                    isPassword = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // --- Tombol Sign Up (Gradient) ---
            val buttonGradient = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFD3DCE3), // Abu-abu terang atas
                    Color(0xFF6B8B9E)  // Biru keabu-abuan bawah
                )
            )
            Button(
                onClick = {
                    if (password != confirmPassword) {
                        Toast.makeText(context, "Password tidak cocok!", Toast.LENGTH_SHORT).show()
                    } else if (firstName.isNotEmpty() && lastName.isNotEmpty() && email.isNotEmpty() && phone.isNotEmpty() && password.isNotEmpty()) {
                        viewModel.register(
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
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(),
                enabled = registerState !is Resource.Loading<*> && loginState !is Resource.Loading<*>
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(buttonGradient, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (registerState is Resource.Loading<*> || loginState is Resource.Loading<*>) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = "Sign Up",
                            color = Color.Black,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- Teks Login ---
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color.Black)) {
                        append("Already have an account? ")
                    }
                    withStyle(style = SpanStyle(color = linkBlue, fontWeight = FontWeight.Bold)) {
                        append("Login")
                    }
                },
                fontSize = 14.sp,
                modifier = Modifier.clickable { onNavigateToLogin() }
            )

            Spacer(modifier = Modifier.weight(1f))

            // --- Teks Syarat & Ketentuan ---
            Text(
                text = "SEWAIN Terms & Conditions",
                color = textGray,
                fontSize = 12.sp,
                textDecoration = TextDecoration.Underline,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .padding(bottom = 32.dp, top = 24.dp)
                    .clickable { /* Handle buka T&C */ }
            )
        }
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
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF8C8C8C),
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
            },
            singleLine = true,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color(0xFF285473),
                unfocusedBorderColor = Color(0xFF285473),
                cursorColor = Color.Black
            ),
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
        )
    }
}
