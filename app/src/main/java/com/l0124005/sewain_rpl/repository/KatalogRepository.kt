package com.l0124005.sewain_rpl.repository

import com.l0124005.sewain_rpl.network.ApiClient
import com.l0124005.sewain_rpl.network.KatalogListResponse
import com.l0124005.sewain_rpl.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class KatalogRepository {
    private val apiService = ApiClient.instance

    fun getKatalogPublik(search: String? = null, kategori: String? = null): Flow<Resource<KatalogListResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getKatalogPublik(search, kategori)
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
