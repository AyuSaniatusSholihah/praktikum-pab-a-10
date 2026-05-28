package com.l0124005.sewain_rpl.network

class Models {
}

data class LoginRequest(val email: String, val password: String)

data class RegisterRequest(
    val first_name: String,
    val last_name: String,
    val email: String,
    val phone: String,
    val password: String,
    val password_confirmation: String
)

data class OtpRequest(val email: String, val otp: String)

data class ForgotPasswordRequest(val email: String)

data class ResetPasswordRequest(
    val email: String,
    val otp: String,
    val password: String,
    val password_confirmation: String
)

data class GoogleLoginRequest(
    val idToken: String
)

// ═══════════════════════════════════════════
// RESPONSE MODELS (diterima dari API)
// ═══════════════════════════════════════════

// Dipakai untuk response login & OTP verify
data class LoginResponse(
    val success: Boolean,
    val message: String,
    val access_token: String?,
    val token_type: String?,
    val user: UserData?
)

data class UserData(
    val id: Int,
    val first_name: String,
    val last_name: String,
    val email: String,
    val phone: String?,
    val role: String?      // 'user' atau 'admin'
)

// Response generic (sukses/gagal saja)
data class ApiResponse(
    val success: Boolean,
    val message: String
)


