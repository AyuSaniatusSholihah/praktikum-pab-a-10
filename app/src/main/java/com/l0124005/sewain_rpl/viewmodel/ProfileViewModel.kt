package com.l0124005.sewain_rpl.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.l0124005.sewain_rpl.network.ProfileResponse
import com.l0124005.sewain_rpl.repository.ProfileRepository
import com.l0124005.sewain_rpl.utils.Resource
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

class ProfileViewModel(private val repository: ProfileRepository) : ViewModel() {

    // State untuk MENAMPILKAN profil (GET)
    private val _profile = MutableLiveData<Resource<ProfileResponse>?>()
    val profile: LiveData<Resource<ProfileResponse>?> = _profile

    // State TERPISAH untuk aksi UPDATE — agar form tidak hilang saat loading
    private val _updateState = MutableLiveData<Resource<ProfileResponse>?>()
    val updateState: LiveData<Resource<ProfileResponse>?> = _updateState

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
