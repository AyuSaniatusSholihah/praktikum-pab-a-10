package com.l0124005.sewain_rpl.repository

import com.l0124005.sewain_rpl.network.ApiClient
import com.l0124005.sewain_rpl.network.CatalogResponse
import com.l0124005.sewain_rpl.network.KatalogListResponse
import com.l0124005.sewain_rpl.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class KatalogRepository {
    private val apiService = ApiClient.instance

    fun getKatalogPublik(): Flow<Resource<KatalogListResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getKatalogPublik()
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message()))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun getKatalogDetail(token: String, id: Int): Flow<Resource<CatalogResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getKatalogDetail("Bearer $token", id)
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
