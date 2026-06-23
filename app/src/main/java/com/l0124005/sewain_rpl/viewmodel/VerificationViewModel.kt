package com.l0124005.sewain_rpl.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.l0124005.sewain_rpl.network.*
import com.l0124005.sewain_rpl.repository.ProfileRepository
import com.l0124005.sewain_rpl.repository.TransaksiRepository
import com.l0124005.sewain_rpl.utils.Resource
import kotlinx.coroutines.launch

class VerificationViewModel(
    private val transaksiRepository: TransaksiRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _verifikasiState = MutableLiveData<Resource<VerifikasiPengembalianResponse>?>()
    val verifikasiState: LiveData<Resource<VerifikasiPengembalianResponse>?> = _verifikasiState

    private val _ownerDashboardState = MutableLiveData<Resource<OwnerDashboardResponse>?>()
    val ownerDashboardState: LiveData<Resource<OwnerDashboardResponse>?> = _ownerDashboardState

    private val _profileState = MutableLiveData<Resource<ProfileResponse>?>()
    val profileState: LiveData<Resource<ProfileResponse>?> = _profileState

    fun verifikasiPengembalian(token: String, id: Int, request: VerifikasiPengembalianRequest) {
        viewModelScope.launch {
            transaksiRepository.verifikasiPengembalian(token, id, request).collect { resource ->
                _verifikasiState.value = resource
                if (resource is Resource.Success) {
                    // Sync profile and owner dashboard stats
                    launch {
                        profileRepository.getProfile(token).collect {
                            _profileState.value = it
                        }
                    }
                    launch {
                        transaksiRepository.getOwnerDashboard(token).collect {
                            _ownerDashboardState.value = it
                        }
                    }
                }
            }
        }
    }

    fun resetState() {
        _verifikasiState.value = null
        _ownerDashboardState.value = null
        _profileState.value = null
    }
}
