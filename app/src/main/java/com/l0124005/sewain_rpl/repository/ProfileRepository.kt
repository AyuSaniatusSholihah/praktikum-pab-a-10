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
            val response = apiService.getProfile("Bearer $token")
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
        fotoProfil: MultipartBody.Part? = null
    ): Flow<Resource<ProfileResponse>> = flow {
        emit(Resource.Loading())
        try {
            val authToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
            val response = apiService.updateProfile(authToken, "PUT", name, username, phoneNumber, alamat, fotoProfil)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message()))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }
}
