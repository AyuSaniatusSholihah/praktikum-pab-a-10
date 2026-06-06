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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
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
                        // Arahkan ke Login setelah sukses daftar karena kita tidak pakai OTP
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
    onRegisterSuccess: () -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val registerState by viewModel.registerState.observeAsState()
    val context = LocalContext.current

    LaunchedEffect(registerState) {
        when (registerState) {
            is Resource.Success -> {
                Toast.makeText(context, registerState?.data ?: "Akun berhasil terdaftar!", Toast.LENGTH_LONG).show()
                onRegisterSuccess()
            }
            is Resource.Error -> {
                Toast.makeText(context, registerState?.message ?: "Terjadi kesalahan", Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    val textGray = Color(0xFF8C8C8C)
    val linkBlue = Color(0xFF1E5276)

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFE4EDF5),
            Color(0xFFFFFFFF)
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

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "SEWA", fontSize = 38.sp, fontFamily = FontFamily.Serif, color = Color(0xFF2A2A2A))
                Text(text = "IN", fontSize = 38.sp, fontFamily = FontFamily.Serif, color = Color(0xFF5D8AA8))
            }
            Text(
                text = "Sign UP To SEWAIN",
                fontSize = 14.sp,
                fontFamily = FontFamily.Serif,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                CustomTextField(
                    label = "FIRST NAME",
                    value = firstName,
                    onValueChange = { firstName = it },
                    placeholder = "First",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                CustomTextField(
                    label = "LAST NAME",
                    value = lastName,
                    onValueChange = { lastName = it },
                    placeholder = "Last",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            CustomTextField(
                label = "EMAIL",
                value = email,
                onValueChange = { email = it },
                placeholder = "yourname@email.com"
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomTextField(
                label = "PHONE NUMBER",
                value = phone,
                onValueChange = { phone = it },
                placeholder = "08123456789"
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomTextField(
                label = "PASSWORD",
                value = password,
                onValueChange = { password = it },
                placeholder = "••••••••",
                isPassword = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomTextField(
                label = "CONFIRM PASSWORD",
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = "••••••••",
                isPassword = true
            )

            Spacer(modifier = Modifier.height(48.dp))

            val buttonGradient = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFD3DCE3),
                    Color(0xFF6B8B9E)
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
                enabled = registerState !is Resource.Loading<*>
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(buttonGradient, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (registerState is Resource.Loading<*>) {
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

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "SEWAIN Terms & Conditions",
                color = textGray,
                fontSize = 12.sp,
                textDecoration = TextDecoration.Underline,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .padding(bottom = 32.dp)
                    .clickable { /* Handle T&C */ }
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
