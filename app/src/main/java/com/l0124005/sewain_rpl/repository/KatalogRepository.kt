package com.l0124005.sewain_rpl.repository

import com.l0124005.sewain_rpl.network.*
import com.l0124005.sewain_rpl.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import okhttp3.RequestBody

class KatalogRepository {
    private val apiService = ApiClient.instance

    fun getKatalogPublik(
        search: String? = null,
        kategoriId: Int? = null,
        lokasi: String? = null,
        minHarga: Double? = null,
        maxHarga: Double? = null
    ): Flow<Resource<KatalogListResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getKatalogPublik(search, kategoriId, lokasi, minHarga, maxHarga)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message()))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun getMyKatalog(token: String): Flow<Resource<KatalogListResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getMyKatalog(token)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message()))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun getMyKatalogDetail(token: String, id: Int): Flow<Resource<KatalogDetailResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getMyKatalogDetail(token, id)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message()))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun getKatalogPublikDetail(id: Int): Flow<Resource<KatalogDetailResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getKatalogPublikDetail(id)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message()))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun createKatalog(
        token: String,
        kategoriId: RequestBody,
        namaBarang: RequestBody,
        deskripsi: RequestBody?,
        hargaSewa: RequestBody,
        hargaJaminan: RequestBody,
        hargaDendaPerjam: RequestBody,
        stok: RequestBody,
        lokasi: RequestBody,
        additionalInformation: RequestBody?,
        fotoBarang: MultipartBody.Part?,
        status: RequestBody? = null
    ): Flow<Resource<KatalogCrudResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.createKatalog(
                token, kategoriId, namaBarang, deskripsi,
                hargaSewa, hargaJaminan, hargaDendaPerjam, stok, lokasi,
                additionalInformation, fotoBarang, status
            )
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message()))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun updateKatalog(
        token: String,
        id: Int,
        kategoriId: RequestBody? = null,
        namaBarang: RequestBody? = null,
        deskripsi: RequestBody? = null,
        hargaSewa: RequestBody? = null,
        hargaJaminan: RequestBody? = null,
        hargaDendaPerjam: RequestBody? = null,
        stok: RequestBody? = null,
        lokasi: RequestBody? = null,
        additionalInformation: RequestBody? = null,
        fotoBarang: MultipartBody.Part? = null,
        status: RequestBody? = null
    ): Flow<Resource<KatalogCrudResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.updateKatalog(
                token, id, "PUT", kategoriId, namaBarang, deskripsi,
                hargaSewa, hargaJaminan, hargaDendaPerjam, stok, lokasi,
                additionalInformation, fotoBarang, status
            )
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message()))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun deleteKatalog(token: String, id: Int): Flow<Resource<GenericResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.deleteKatalog(token, id)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message()))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    fun getKategori(): Flow<Resource<KategoriListResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getKategori()
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
