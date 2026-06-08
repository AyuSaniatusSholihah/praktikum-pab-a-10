package com.l0124005.sewain_rpl.repository

import com.l0124005.sewain_rpl.network.ApiClient
import com.l0124005.sewain_rpl.network.BayarRequest
import com.l0124005.sewain_rpl.network.BayarResponse
import com.l0124005.sewain_rpl.network.CheckoutResponse
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
}
