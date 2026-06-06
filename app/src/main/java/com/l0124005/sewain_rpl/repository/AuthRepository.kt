package com.l0124005.sewain_rpl.repository

import com.l0124005.sewain_rpl.network.ApiClient
import com.l0124005.sewain_rpl.network.LoginRequest
import com.l0124005.sewain_rpl.network.RegisterRequest
import com.l0124005.sewain_rpl.utils.Resource
import com.l0124005.sewain_rpl.utils.SessionManager

class AuthRepository(private val session: SessionManager) {

    private val api = ApiClient.instance

    suspend fun login(email: String, password: String): Resource<String> {
        return try {
            val response = api.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val body = response.body()!!
                if (body.success && body.token != null) {
                    session.saveToken(body.token)
                    // Jika role tidak ada di model baru, kita default ke "user"
                    session.saveRole("user") 
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

    suspend fun logout(): Resource<String> {
        return try {
            val token = "Bearer ${session.getToken()}"
            api.logout(token)
            session.clearSession()
            Resource.Success("Logout berhasil")
        } catch (e: Exception) {
            session.clearSession()
            Resource.Success("Logout berhasil")
        }
    }
}
