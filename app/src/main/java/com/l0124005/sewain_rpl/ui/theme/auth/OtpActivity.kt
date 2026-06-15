package com.l0124005.sewain_rpl.ui.theme.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.l0124005.sewain_rpl.MainActivity
import com.l0124005.sewain_rpl.repository.AuthRepository
import com.l0124005.sewain_rpl.ui.theme.Sewain_rplTheme
import com.l0124005.sewain_rpl.utils.Resource
import com.l0124005.sewain_rpl.utils.SessionManager
import com.l0124005.sewain_rpl.viewmodel.AuthViewModel
import com.l0124005.sewain_rpl.viewmodel.AuthViewModelFactory

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
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
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
    var otpCode by remember { mutableStateOf("") }
    val context = LocalContext.current

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFE4EDF5), Color(0xFFFFFFFF))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "SEWA", fontSize = 38.sp, fontFamily = FontFamily.Serif, color = Color(0xFF2A2A2A))
                Text(text = "IN", fontSize = 38.sp, fontFamily = FontFamily.Serif, color = Color(0xFF5D8AA8))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Verifikasi Email",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2A2A2A)
            )
            
            Text(
                text = "Masukkan kode OTP yang dikirim ke\n$email",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            OutlinedTextField(
                value = otpCode,
                onValueChange = { if (it.length <= 6) otpCode = it },
                label = { Text("KODE OTP") },
                placeholder = { Text("123456") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    textAlign = TextAlign.Center,
                    letterSpacing = 8.sp,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF285473),
                    unfocusedBorderColor = Color(0xFF285473)
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            val buttonGradient = Brush.verticalGradient(
                colors = listOf(Color(0xFFD3DCE3), Color(0xFF6B8B9E))
            )
            
            Button(
                onClick = { 
                    if (otpCode.length >= 4) {
                        onVerify(otpCode)
                    } else {
                        Toast.makeText(context, "Masukkan kode OTP dengan lengkap!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(),
                enabled = otpState !is Resource.Loading
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().background(buttonGradient, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (otpState is Resource.Loading) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                    } else {
                        Text(text = "Verifikasi Sekarang", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(onClick = onResendOtp) {
                Text(text = "Kirim Ulang OTP", color = Color(0xFF1E5276), fontWeight = FontWeight.Bold)
            }
        }
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
