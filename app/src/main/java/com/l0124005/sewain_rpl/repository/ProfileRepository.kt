package com.l0124005.sewain_rpl.repository

import com.l0124005.sewain_rpl.network.*
import com.l0124005.sewain_rpl.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import okhttp3.RequestBody

class ProfileRepository {
    private val apiService = ApiClient.instance

    fun getProfile(token: String): Flow<Resource<ProfileResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getProfile(token)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message()))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun updateProfile(
        token: String,
        name: RequestBody? = null,
        username: RequestBody? = null,
        phoneNumber: RequestBody? = null,
        alamat: RequestBody? = null,
        tanggalLahir: RequestBody? = null,
        jenisKelamin: RequestBody? = null,
        fotoProfil: MultipartBody.Part? = null
    ): Flow<Resource<ProfileResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.updateProfile(token, "PUT", name, username, phoneNumber, alamat, tanggalLahir, jenisKelamin, fotoProfil)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                val errMsg = try {
                    response.errorBody()?.string()?.let {
                        org.json.JSONObject(it).optString("message", response.message())
                    } ?: response.message()
                } catch (e: Exception) { response.message() }
                emit(Resource.Error(errMsg ?: "Gagal memperbarui profil"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Koneksi gagal, coba lagi"))
        }
    }
}
