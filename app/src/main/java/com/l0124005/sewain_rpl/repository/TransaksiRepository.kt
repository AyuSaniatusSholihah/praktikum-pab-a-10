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
            val formattedToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
            android.util.Log.d("TransaksiRepository", "Checkout IDs: $keranjangIds")
            val response = apiService.checkout(formattedToken, CheckoutRequest(keranjangIds))
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message()
                android.util.Log.e("TransaksiRepository", "Checkout Error: $errorMsg")
                emit(Resource.Error(parseErrorMessage(errorMsg)))
            }
        } catch (e: Exception) {
            android.util.Log.e("TransaksiRepository", "Checkout Exception", e)
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun bayar(token: String, request: BayarRequest): Flow<Resource<BayarResponse>> = flow {
        emit(Resource.Loading())
        try {
            val formattedToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
            android.util.Log.d("TransaksiRepository", "Bayar Request: $request")
            val response = apiService.bayar(formattedToken, request)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message()
                android.util.Log.e("TransaksiRepository", "Bayar Error: $errorMsg")
                emit(Resource.Error(parseErrorMessage(errorMsg)))
            }
        } catch (e: Exception) {
            android.util.Log.e("TransaksiRepository", "Bayar Exception", e)
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun cancelTransaksi(token: String, id: Int): Flow<Resource<GenericResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.cancelTransaksi(token, id)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message()
                android.util.Log.e("TransaksiRepository", "Cancel Error Code: ${response.code()}")
                android.util.Log.e("TransaksiRepository", "Cancel Error Body: $errorMsg")
                
                val cleanMsg = when {
                    response.code() == 404 -> "Endpoint tidak ditemukan (404). Pastikan rute di backend adalah POST /api/transaksi/{id}/cancel"
                    response.code() == 405 -> "Metode tidak diizinkan (405). Coba ganti POST menjadi PATCH atau DELETE di ApiService."
                    errorMsg.contains("message") -> {
                        try {
                            val json = com.google.gson.Gson().fromJson(errorMsg, com.google.gson.JsonObject::class.java)
                            json.get("message")?.asString ?: errorMsg
                        } catch (e: Exception) { errorMsg }
                    }
                    errorMsg.contains("<html>", true) || errorMsg.contains("<!DOCTYPE", true) -> 
                        "Server Error (HTML). Kemungkinan crash di backend."
                    else -> errorMsg
                }
                
                emit(Resource.Error(cleanMsg))
            }
        } catch (e: Exception) {
            android.util.Log.e("TransaksiRepository", "Cancel Exception", e)
            emit(Resource.Error("Koneksi gagal: ${e.message}"))
        }
    }

    fun kembalikanBarang(
        token: String,
        id: Int,
        fotoBukti: MultipartBody.Part,
        rating: RequestBody,
        komentar: RequestBody? = null,
        tanggalKembali: RequestBody? = null,
        totalDenda: RequestBody? = null
    ): Flow<Resource<KembalikanResponse>> = flow {
        emit(Resource.Loading())
        try {
            val formattedToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
            val response = apiService.kembalikanBarang(formattedToken, id, fotoBukti, rating, komentar, tanggalKembali, totalDenda)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message()
                android.util.Log.e("TransaksiRepository", "Error kembalikanBarang: $errorMsg")
                emit(Resource.Error(parseErrorMessage(errorMsg)))
            }
        } catch (e: Exception) {
            android.util.Log.e("TransaksiRepository", "Exception kembalikanBarang", e)
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

    fun getOwnerTransaksiDetail(token: String, id: Int): Flow<Resource<TransaksiDetailResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getOwnerTransaksiDetail(token, id)
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
            val formattedToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
            val response = apiService.verifikasiPengembalian(formattedToken, id, request)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message()
                android.util.Log.e("TransaksiRepository", "Verifikasi Error Body: $errorMsg")
                emit(Resource.Error(parseErrorMessage(errorMsg)))
            }
        } catch (e: Exception) {
            android.util.Log.e("TransaksiRepository", "Verifikasi Exception", e)
            emit(Resource.Error("Koneksi gagal: ${e.message}"))
        }
    }

    private fun parseErrorMessage(errorMsg: String): String {
        return try {
            val json = com.google.gson.Gson().fromJson(errorMsg, com.google.gson.JsonObject::class.java)
            json.get("message")?.asString ?: errorMsg
        } catch (e: Exception) { errorMsg }
    }
}
