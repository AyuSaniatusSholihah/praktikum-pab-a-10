package com.l0124005.sewain_rpl.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.l0124005.sewain_rpl.network.OwnerDashboardResponse
import com.l0124005.sewain_rpl.network.ProfileResponse
import com.l0124005.sewain_rpl.network.TransaksiListResponse
import com.l0124005.sewain_rpl.repository.ProfileRepository
import com.l0124005.sewain_rpl.utils.Resource
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class ProfileViewModel(private val repository: ProfileRepository) : ViewModel() {

    // State untuk MENAMPILKAN profil (GET)
    private val _profile = MutableLiveData<Resource<ProfileResponse>?>()
    val profile: LiveData<Resource<ProfileResponse>?> = _profile

    // State TERPISAH untuk aksi UPDATE — agar form tidak hilang saat loading
    private val _updateState = MutableLiveData<Resource<ProfileResponse>?>()
    val updateState: LiveData<Resource<ProfileResponse>?> = _updateState

    private val _ownerDashboard = MutableLiveData<Resource<OwnerDashboardResponse>?>()
    val ownerDashboard: LiveData<Resource<OwnerDashboardResponse>?> = _ownerDashboard

    // State untuk Informasi Rekening (Simulasi penyimpanan otomatis)
    private val _rekeningInfo = MutableLiveData<String>("Nama Rekening: BSI Syariah\nNo Rekening: 763098218847\nA/n: Camping Groups Bandung")
    val rekeningInfo: LiveData<String> = _rekeningInfo

    private val _transaksiList = MutableLiveData<Resource<TransaksiListResponse>?>()
    val transaksiList: LiveData<Resource<TransaksiListResponse>?> = _transaksiList

    fun updateRekening(token: String, newInfo: String) {
        _rekeningInfo.value = newInfo
        // Simpan ke profil (sementara dipetakan ke field alamat atau dihandle backend secara dinamis)
        val body = newInfo.toRequestBody("text/plain".toMediaTypeOrNull())
        updateProfile(token, alamat = body)
    }

    fun resetStates() {
        _profile.value = null
        _updateState.value = null
    }

    fun resetUpdateState() {
        _updateState.value = null
    }

    fun getProfile(token: String) {
        viewModelScope.launch {
            repository.getProfile(token).collect {
                _profile.value = it
            }
        }
    }

    fun getStats(token: String) {
        viewModelScope.launch {
            repository.getOwnerDashboard(token).collect {
                _ownerDashboard.value = it
            }
            repository.getRiwayatTransaksi(token).collect {
                _transaksiList.value = it
            }
        }
    }

    fun updateProfile(
        token: String,
        name: RequestBody? = null,
        username: RequestBody? = null,
        phoneNumber: RequestBody? = null,
        alamat: RequestBody? = null,
        tanggalLahir: RequestBody? = null,
        jenisKelamin: RequestBody? = null,
        fotoProfil: MultipartBody.Part? = null
    ) {
        viewModelScope.launch {
            // Gunakan _updateState bukan _profile agar tampilan tidak hilang saat loading
            repository.updateProfile(token, name, username, phoneNumber, alamat, tanggalLahir, jenisKelamin, fotoProfil).collect {
                _updateState.value = it
            }
        }
    }
}
