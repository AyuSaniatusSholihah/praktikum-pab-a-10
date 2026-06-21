package com.l0124005.sewain_rpl.repository

import com.l0124005.sewain_rpl.network.*
import com.l0124005.sewain_rpl.utils.Resource
import com.l0124005.sewain_rpl.utils.SessionManager

class AuthRepository(private val session: SessionManager) {

    private val api = ApiClient.instance

    suspend fun login(email: String, password: String): Resource<String> {
        return try {
            val response = api.login(LoginRequest(email, password))
            val body = response.body()
            
            if (response.isSuccessful && body != null) {
                if (body.success && body.access_token != null) {
                    session.saveToken(body.access_token)
                    // Ambil role asli dari user, jika tidak ada default ke "user"
                    val role = body.user?.role ?: "user"
                    session.saveRole(role)
                    Resource.Success(body.message)
                } else if (body.is_verified == false && body.email != null) {
                    // Kasus khusus: Akun ada tapi belum verifikasi OTP
                    Resource.Error("VERIFY_NEEDED|${body.message}|${body.email}")
                } else {
                    Resource.Error(body.message)
                }
            } else {
                // Ambil pesan error dari errorBody jika ada (format JSON)
                val errorMsg = response.errorBody()?.string()
                if (errorMsg?.contains("message") == true) {
                    // Parsing manual sederhana atau biarkan user melihat pesan sistem
                    Resource.Error("Gagal login: Kredensial tidak valid atau masalah server")
                } else {
                    Resource.Error("Login gagal (Code: ${response.code()})")
                }
            }
        } catch (e: Exception) {
            Resource.Error("Tidak bisa konek ke server: ${e.message}")
        }
    }

    suspend fun register(req: RegisterRequest): Resource<String> {
        return try {
            val response = api.register(req)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    Resource.Success(body.message)
                } else {
                    Resource.Error(body?.message ?: "Registrasi gagal")
                }
            } else {
                Resource.Error("Registrasi gagal: Data tidak valid atau sudah terdaftar")
            }
        } catch (e: Exception) {
            Resource.Error("Error: ${e.message}")
        }
    }

    suspend fun verifyOtp(email: String, otp: String): Resource<String> {
        return try {
            val response = api.verifyOtp(VerifyOtpRequest(email, otp))
            if (response.isSuccessful) {
                val body = response.body()!!
                if (body.success) {
                    // Kita tidak simpan token di sini agar user login manual setelah verifikasi
                    Resource.Success(body.message)
                } else {
                    Resource.Error(body.message)
                }
            } else {
                Resource.Error("Verifikasi gagal: Kode OTP salah atau kedaluwarsa")
            }
        } catch (e: Exception) {
            Resource.Error("Error: ${e.message}")
        }
    }

    suspend fun resendOtp(email: String): Resource<String> {
        return try {
            val response = api.resendOtp(ResendOtpRequest(email))
            if (response.isSuccessful) {
                Resource.Success(response.body()?.message ?: "OTP terkirim")
            } else {
                Resource.Error("Gagal mengirim ulang OTP")
            }
        } catch (e: Exception) {
            Resource.Error("Error: ${e.message}")
        }
    }

    suspend fun logout(): Resource<String> {
        return try {
            val token = session.getToken() // Sudah mengandung "Bearer " dari SessionManager
            api.logout(token)
            session.clearSession()
            Resource.Success("Logout berhasil")
        } catch (e: Exception) {
            session.clearSession()
            Resource.Success("Logout berhasil")
        }
    }
}
