package com.l0124005.sewain_rpl.repository

import com.l0124005.sewain_rpl.network.*
import com.l0124005.sewain_rpl.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class KeranjangRepository {
    private val apiService = ApiClient.instance

    fun getKeranjang(token: String): Flow<Resource<KeranjangResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getKeranjang(token)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message()))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun addToKeranjang(token: String, request: AddToKeranjangRequest): Flow<Resource<KeranjangResponse>> = flow {
        emit(Resource.Loading())
        try {
            // Token sudah mengandung "Bearer " dari SessionManager, jadi tidak perlu ditambah lagi
            val response = apiService.addToKeranjang(token, request)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message()))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun updateKeranjangItem(token: String, id: Int, request: UpdateKeranjangRequest): Flow<Resource<KeranjangResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.updateKeranjangItem(token, id, request)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message()))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun removeKeranjangItem(token: String, id: Int): Flow<Resource<KeranjangResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.removeKeranjangItem(token, id)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message()))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun clearKeranjang(token: String): Flow<Resource<GenericResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.clearKeranjang(token)
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
