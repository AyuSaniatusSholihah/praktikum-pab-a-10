package com.l0124005.sewain_rpl.repository

import com.l0124005.sewain_rpl.network.ApiClient
import com.l0124005.sewain_rpl.network.LoginRequest
import com.l0124005.sewain_rpl.network.RegisterRequest
import com.l0124005.sewain_rpl.network.OtpRequest
import com.l0124005.sewain_rpl.network.GoogleLoginRequest
import com.l0124005.sewain_rpl.utils.Resource
import com.l0124005.sewain_rpl.utils.SessionManager



class AuthRepository(private val session: SessionManager) {

    private val api = ApiClient.instance

    suspend fun login(email: String, password: String): Resource<String> {
        return try {
            val response = api.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val body = response.body()!!
                if (body.success && body.access_token != null) {
                    session.saveToken(body.access_token)
                    session.saveRole(body.user?.role ?: "user")
                    Resource.Success(body.message)
                } else {
                    Resource.Error(body.message)
                }
            } else {
                Resource.Error("Login gagal: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error("Tidak bisa konek ke server: ${e.message}")
        }
    }

    suspend fun register(req: RegisterRequest): Resource<String> {
        return try {
            val response = api.register(req)
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()!!.message)
            } else {
                Resource.Error(response.body()?.message ?: "Registrasi gagal")
            }
        } catch (e: Exception) {
            Resource.Error("Error: ${e.message}")
        }
    }

    suspend fun verifyOtp(email: String, otp: String): Resource<String> {
        return try {
            val response = api.verifyOtp(OtpRequest(email, otp))
            if (response.isSuccessful) {
                val body = response.body()!!
                if (body.success && body.access_token != null) {
                    session.saveToken(body.access_token)
                    Resource.Success("OTP berhasil")
                } else {
                    Resource.Error(body.message)
                }
            } else {
                Resource.Error("OTP tidak valid")
            }
        } catch (e: Exception) {
            Resource.Error("Error: ${e.message}")
        }
    }

    suspend fun logout(): Resource<String> {
        return try {
            api.logout(session.getToken() ?: "")
            session.clearSession()
            Resource.Success("Logout berhasil")
        } catch (e: Exception) {
            session.clearSession()
            Resource.Success("Logout berhasil")
        }
    }

    suspend fun googleLogin(idToken: String): Resource<String> {
        return try {
            val response = api.googleLogin(GoogleLoginRequest(idToken))
            if (response.isSuccessful) {
                val body = response.body()!!
                if (body.success && body.access_token != null) {
                    session.saveToken(body.access_token)
                    session.saveRole(body.user?.role ?: "user")
                    Resource.Success(body.message)
                } else {
                    Resource.Error(body.message)
                }
            } else {
                Resource.Error("Login Google gagal: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error("Gagal menghubungkan ke server: ${e.message}")
        }
    }
}
