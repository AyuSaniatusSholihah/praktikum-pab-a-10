package com.l0124005.sewain_rpl.repository

import com.l0124005.sewain_rpl.network.*
import com.l0124005.sewain_rpl.ui.theme.auth.LoginActivity
import com.l0124005.sewain_rpl.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TransaksiRepository {
    private val apiService = ApiClient.instance

    fun checkout(token: String): Flow<Resource<CheckoutResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.checkout("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message()))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun bayar(token: String, request: BayarRequest): Flow<Resource<BayarResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.bayar("Bearer $token", request)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message()))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun bayarMassal(token: String, request: BayarMassalRequest): Flow<Resource<BayarMassalResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.bayarMassal("Bearer $token", request)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message()))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun getTransaksi(token: String): Flow<Resource<TransaksiListResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getTransaksi("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message()))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun getTransaksiDetail(token: String, id: Int): Flow<Resource<TransaksiDetailResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getTransaksiDetail("Bearer $token", id)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message()))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun kembalikanBarang(token: String, id: Int): Flow<Resource<KembalikanResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.kembalikanBarang("Bearer $token", id)
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
