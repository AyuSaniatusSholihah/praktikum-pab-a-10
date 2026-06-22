package com.l0124005.sewain_rpl.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.l0124005.sewain_rpl.network.*
import com.l0124005.sewain_rpl.repository.KatalogRepository
import com.l0124005.sewain_rpl.utils.Resource
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

class KatalogViewModel(private val repository: KatalogRepository) : ViewModel() {

    private val _katalogPublik = MutableLiveData<Resource<KatalogListResponse>?>()
    val katalogPublik: LiveData<Resource<KatalogListResponse>?> = _katalogPublik

    private val _myKatalog = MutableLiveData<Resource<KatalogListResponse>?>()
    val myKatalog: LiveData<Resource<KatalogListResponse>?> = _myKatalog

    private val _myKatalogDetail = MutableLiveData<Resource<KatalogDetailResponse>?>()
    val myKatalogDetail: LiveData<Resource<KatalogDetailResponse>?> = _myKatalogDetail

    private val _crudResult = MutableLiveData<Resource<KatalogCrudResponse>?>()
    val crudResult: LiveData<Resource<KatalogCrudResponse>?> = _crudResult

    private val _deleteResult = MutableLiveData<Resource<GenericResponse>?>()
    val deleteResult: LiveData<Resource<GenericResponse>?> = _deleteResult

    private val _kategori = MutableLiveData<Resource<KategoriListResponse>?>()
    val kategori: LiveData<Resource<KategoriListResponse>?> = _kategori

    private val _extractedKategori = MutableLiveData<List<String>>(emptyList())
    val extractedKategori: LiveData<List<String>> = _extractedKategori

    fun resetStates() {
        _katalogPublik.value = null
        _myKatalog.value = null
        _myKatalogDetail.value = null
        _crudResult.value = null
        _deleteResult.value = null
        _kategori.value = null
    }

    // Hanya reset crudResult agar state katalog & kategori tidak hilang saat simpan
    fun resetCrudResult() {
        _crudResult.value = null
    }

    fun resetMyKatalogDetail() {
        _myKatalogDetail.value = null
    }

    fun getKategori() {
        viewModelScope.launch {
            repository.getKategori().collect {
                _kategori.value = it
            }
        }
    }

    fun getKatalogPublik(
        search: String? = null,
        kategoriId: Int? = null,
        lokasi: String? = null,
        minHarga: Double? = null,
        maxHarga: Double? = null
    ) {
        viewModelScope.launch {
            repository.getKatalogPublik(search, kategoriId, lokasi, minHarga, maxHarga).collect {
                _katalogPublik.value = it
            }
        }
    }

    fun getMyKatalog(token: String) {
        viewModelScope.launch {
            repository.getMyKatalog(token).collect {
                _myKatalog.value = it
            }
        }
    }

    fun getMyKatalogDetail(token: String, id: Int) {
        viewModelScope.launch {
            repository.getMyKatalogDetail(token, id).collect {
                _myKatalogDetail.value = it
            }
        }
    }

    fun getKatalogPublikDetail(id: Int) {
        viewModelScope.launch {
            repository.getKatalogPublikDetail(id).collect {
                _myKatalogDetail.value = it
            }
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
        whatsapp: RequestBody? = null,
        tanggalMulai: RequestBody? = null,
        tanggalAkhir: RequestBody? = null,
        additionalInformation: RequestBody?,
        fotoUtama: MultipartBody.Part?,
        fotoAngle1: MultipartBody.Part? = null,
        fotoAngle2: MultipartBody.Part? = null,
        fotoAngle3: MultipartBody.Part? = null,
        fotoAngle4: MultipartBody.Part? = null,
        status: RequestBody? = null
    ) {
        viewModelScope.launch {
            repository.createKatalog(
                token, kategoriId, namaBarang, deskripsi, hargaSewa,
                hargaJaminan, hargaDendaPerjam, stok, lokasi,
                whatsapp, tanggalMulai, tanggalAkhir,
                additionalInformation, fotoUtama, fotoAngle1, fotoAngle2, 
                fotoAngle3, fotoAngle4, status
            ).collect {
                _crudResult.value = it
            }
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
        whatsapp: RequestBody? = null,
        tanggalMulai: RequestBody? = null,
        tanggalAkhir: RequestBody? = null,
        additionalInformation: RequestBody? = null,
        fotoUtama: MultipartBody.Part? = null,
        fotoAngle1: MultipartBody.Part? = null,
        fotoAngle2: MultipartBody.Part? = null,
        fotoAngle3: MultipartBody.Part? = null,
        fotoAngle4: MultipartBody.Part? = null,
        status: RequestBody? = null
    ) {
        viewModelScope.launch {
            repository.updateKatalog(
                token, id, kategoriId, namaBarang, deskripsi, hargaSewa,
                hargaJaminan, hargaDendaPerjam, stok, lokasi,
                whatsapp, tanggalMulai, tanggalAkhir,
                additionalInformation, fotoUtama, fotoAngle1, fotoAngle2,
                fotoAngle3, fotoAngle4, status
            ).collect {
                _crudResult.value = it
            }
        }
    }

    fun deleteKatalog(token: String, id: Int) {
        viewModelScope.launch {
            repository.deleteKatalog(token, id).collect {
                _deleteResult.value = it
            }
        }
    }
}
