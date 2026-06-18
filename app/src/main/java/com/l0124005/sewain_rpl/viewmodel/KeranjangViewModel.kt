package com.l0124005.sewain_rpl.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.l0124005.sewain_rpl.network.*
import com.l0124005.sewain_rpl.repository.KeranjangRepository
import com.l0124005.sewain_rpl.utils.Resource
import kotlinx.coroutines.launch

class KeranjangViewModel(private val repository: KeranjangRepository) : ViewModel() {

    private val _keranjang = MutableLiveData<Resource<KeranjangResponse>>()
    val keranjang: LiveData<Resource<KeranjangResponse>> = _keranjang

    private val _clearResult = MutableLiveData<Resource<GenericResponse>>()
    val clearResult: LiveData<Resource<GenericResponse>> = _clearResult

    fun getKeranjang(token: String) {
        viewModelScope.launch {
            repository.getKeranjang(token).collect {
                _keranjang.value = it
            }
        }
    }

    fun addToKeranjang(token: String, barangId: Int, jumlah: Int, tglSewa: String, tglKembali: String) {
        viewModelScope.launch {
            repository.addToKeranjang(token, AddToKeranjangRequest(barangId, jumlah, tglSewa, tglKembali)).collect {
                _keranjang.value = it
            }
        }
    }

    fun updateKeranjangItem(token: String, id: Int, request: UpdateKeranjangRequest) {
        viewModelScope.launch {
            repository.updateKeranjangItem(token, id, request).collect {
                _keranjang.value = it
            }
        }
    }

    fun removeKeranjangItem(token: String, id: Int) {
        viewModelScope.launch {
            repository.removeKeranjangItem(token, id).collect {
                _keranjang.value = it
            }
        }
    }

    fun clearKeranjang(token: String) {
        viewModelScope.launch {
            repository.clearKeranjang(token).collect {
                _clearResult.value = it
            }
        }
    }
}
