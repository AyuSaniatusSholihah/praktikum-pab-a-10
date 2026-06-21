package com.l0124005.sewain_rpl.repository

import com.l0124005.sewain_rpl.network.*
import com.l0124005.sewain_rpl.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import okhttp3.RequestBody

class TransaksiRepository {
    private val apiService = ApiClient.instance

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
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun getDetailTransaksi(token: String, id: Int): Flow<Resource<TransaksiDetailResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getDetailTransaksi(token, id)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message()))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun checkout(token: String, keranjangIds: List<Int>? = null): Flow<Resource<CheckoutResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.checkout(token, CheckoutRequest(keranjangIds))
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
            val response = apiService.bayar(token, request)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message()))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun kembalikanBarang(
        token: String,
        id: Int,
        fotoBukti: MultipartBody.Part,
        rating: RequestBody,
        komentar: RequestBody? = null
    ): Flow<Resource<KembalikanResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.kembalikanBarang(token, id, fotoBukti, rating, komentar)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message()))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    // Owner Side
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
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun verifikasiPengembalian(
        token: String,
        id: Int,
        request: VerifikasiPengembalianRequest
    ): Flow<Resource<VerifikasiPengembalianResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.verifikasiPengembalian(token, id, request)
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
