package com.l0124005.sewain_rpl.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.l0124005.sewain_rpl.network.AddToKeranjangRequest
import com.l0124005.sewain_rpl.network.KeranjangResponse
import com.l0124005.sewain_rpl.network.UpdateKeranjangItemRequest
import com.l0124005.sewain_rpl.repository.KeranjangRepository
import com.l0124005.sewain_rpl.utils.Resource
import kotlinx.coroutines.launch

class KeranjangViewModel(private val repository: KeranjangRepository) : ViewModel() {

    private val _keranjang = MutableLiveData<Resource<KeranjangResponse>>()
    val keranjang: LiveData<Resource<KeranjangResponse>> = _keranjang

    fun getKeranjang(token: String) {
        viewModelScope.launch {
            repository.getKeranjang(token).collect {
                _keranjang.value = it
            }
        }
    }

    fun addToKeranjang(token: String, productId: Int, startDate: String, endDate: String, quantity: Int) {
        viewModelScope.launch {
            repository.addToKeranjang(token, AddToKeranjangRequest(productId, startDate, endDate, quantity)).collect {
                _keranjang.value = it
            }
        }
    }

    fun updateKeranjangItem(token: String, id: Int, startDate: String?, endDate: String?, quantity: Int?) {
        viewModelScope.launch {
            repository.updateKeranjangItem(token, id, UpdateKeranjangItemRequest(startDate, endDate, quantity)).collect {
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
}
