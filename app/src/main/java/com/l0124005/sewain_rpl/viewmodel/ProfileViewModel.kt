package com.l0124005.sewain_rpl.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.l0124005.sewain_rpl.network.ProfileResponse
import com.l0124005.sewain_rpl.network.UpdateProfileRequest
import com.l0124005.sewain_rpl.repository.ProfileRepository
import com.l0124005.sewain_rpl.utils.Resource
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: ProfileRepository) : ViewModel() {

    private val _profile = MutableLiveData<Resource<ProfileResponse>>()
    val profile: LiveData<Resource<ProfileResponse>> = _profile

    fun getProfile(token: String) {
        viewModelScope.launch {
            repository.getProfile(token).collect {
                _profile.value = it
            }
        }
    }

    fun updateProfile(token: String, name: String?, phone: String?, address: String?) {
        viewModelScope.launch {
            repository.updateProfile(token, UpdateProfileRequest(name, phone, address)).collect {
                _profile.value = it
            }
        }
    }
}
