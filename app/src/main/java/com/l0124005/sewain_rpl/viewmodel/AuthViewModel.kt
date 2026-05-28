package com.l0124005.sewain_rpl.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.l0124005.sewain_rpl.network.RegisterRequest
import com.l0124005.sewain_rpl.repository.AuthRepository
import com.l0124005.sewain_rpl.utils.Resource
import kotlinx.coroutines.launch

class AuthViewModel(private val repo: AuthRepository) : ViewModel() {

    // LiveData dipantau oleh Activity
    private val _loginState = MutableLiveData<Resource<String>>()
    val loginState: LiveData<Resource<String>> = _loginState

    private val _registerState = MutableLiveData<Resource<String>>()
    val registerState: LiveData<Resource<String>> = _registerState

    private val _otpState = MutableLiveData<Resource<String>>()
    val otpState: LiveData<Resource<String>> = _otpState

    fun login(email: String, password: String) {
        _loginState.value = Resource.Loading()
        viewModelScope.launch {
            _loginState.value = repo.login(email, password)
        }
    }

    fun googleLogin(idToken: String) {
        _loginState.value = Resource.Loading()
        viewModelScope.launch {
            _loginState.value = repo.googleLogin(idToken)
        }
    }

    fun register(req: RegisterRequest) {
        _registerState.value = Resource.Loading()
        viewModelScope.launch {
            _registerState.value = repo.register(req)
        }
    }

    fun verifyOtp(email: String, otp: String) {
        _otpState.value = Resource.Loading()
        viewModelScope.launch {
            _otpState.value = repo.verifyOtp(email, otp)
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            repo.logout()
            onDone()
        }
    }
}
