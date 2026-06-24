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

class PaymentViewModel(
    private val transaksiRepository: TransaksiRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _pembayaranState = MutableLiveData<Resource<BayarResponse>?>()
    val pembayaranState: LiveData<Resource<BayarResponse>?> = _pembayaranState

    private val _profileState = MutableLiveData<Resource<ProfileResponse>?>()
    val profileState: LiveData<Resource<ProfileResponse>?> = _profileState

    fun bayar(token: String, request: BayarRequest) {
        viewModelScope.launch {
            transaksiRepository.bayar(token, request).collect { resource ->
                _pembayaranState.value = resource
                if (resource is Resource.Success) {
                    // Sync renter profile balance
                    profileRepository.getProfile(token).collect { profileResource ->
                        _profileState.value = profileResource
                    }
                }
            }
        }
    }

    fun resetState() {
        _pembayaranState.value = null
        _profileState.value = null
    }
}
