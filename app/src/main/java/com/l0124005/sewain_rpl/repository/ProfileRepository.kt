package com.l0124005.sewain_rpl.repository

import com.l0124005.sewain_rpl.network.*
import com.l0124005.sewain_rpl.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

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

    fun getOwnerDashboard(token: String): Flow<Resource<OwnerDashboardResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getOwnerDashboard(token)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message()))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Gagal memuat data owner"))
        }
    }

    fun getRiwayatTransaksi(token: String): Flow<Resource<TransaksiListResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getRiwayatTransaksi(token)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message()))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Gagal memuat riwayat transaksi"))
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
            val methodPart = "PUT".toRequestBody("text/plain".toMediaTypeOrNull())
            val response = apiService.updateProfile(token, methodPart, name, username, phoneNumber, alamat, tanggalLahir, jenisKelamin, fotoProfil)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message()
                val parsedMsg = try {
                    val json = org.json.JSONObject(errorMsg)
                    val mainMsg = json.optString("message", "")
                    val errors = json.optJSONObject("errors")
                    if (errors != null) {
                        val sb = StringBuilder(mainMsg)
                        if (sb.isNotEmpty()) sb.append(": ")
                        val keys = errors.keys()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            val errorArray = errors.optJSONArray(key)
                            if (errorArray != null) {
                                for (i in 0 until errorArray.length()) {
                                    sb.append(errorArray.getString(i)).append(" ")
                                }
                            }
                        }
                        sb.toString().trim()
                    } else {
                        mainMsg.ifEmpty { response.message() }
                    }
                } catch (e: Exception) {
                    response.message()
                }
                emit(Resource.Error(parsedMsg))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Koneksi gagal, coba lagi"))
        }
    }
}
