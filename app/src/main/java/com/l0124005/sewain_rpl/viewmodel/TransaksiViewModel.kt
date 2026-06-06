package com.l0124005.sewain_rpl.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.l0124005.sewain_rpl.network.*
import com.l0124005.sewain_rpl.repository.TransaksiRepository
import com.l0124005.sewain_rpl.utils.Resource
import kotlinx.coroutines.launch

class TransaksiViewModel(private val repository: TransaksiRepository) : ViewModel() {

    private val _checkoutState = MutableLiveData<Resource<CheckoutResponse>>()
    val checkoutState: LiveData<Resource<CheckoutResponse>> = _checkoutState

    private val _pembayaranState = MutableLiveData<Resource<BayarMassalResponse>>()
    val pembayaranState: LiveData<Resource<BayarMassalResponse>> = _pembayaranState

    private val _transaksiList = MutableLiveData<Resource<TransaksiListResponse>>()
    val transaksiList: LiveData<Resource<TransaksiListResponse>> = _transaksiList

    private val _transaksiDetail = MutableLiveData<Resource<TransaksiDetailResponse>>()
    val transaksiDetail: LiveData<Resource<TransaksiDetailResponse>> = _transaksiDetail

    private val _kembalikanState = MutableLiveData<Resource<KembalikanResponse>>()
    val kembalikanState: LiveData<Resource<KembalikanResponse>> = _kembalikanState

    fun checkout(token: String) {
        viewModelScope.launch {
            repository.checkout(token).collect {
                _checkoutState.value = it
            }
        }
    }

    fun bayarMassal(token: String, ids: List<Int>, total: Double, method: String) {
        viewModelScope.launch {
            repository.bayarMassal(token, BayarMassalRequest(ids, total, method)).collect {
                _pembayaranState.value = it
            }
        }
    }

    fun getTransaksi(token: String) {
        viewModelScope.launch {
            repository.getTransaksi(token).collect {
                _transaksiList.value = it
            }
        }
    }

    fun getTransaksiDetail(token: String, id: Int) {
        viewModelScope.launch {
            repository.getTransaksiDetail(token, id).collect {
                _transaksiDetail.value = it
            }
        }
    }

    fun kembalikanBarang(token: String, id: Int) {
        viewModelScope.launch {
            repository.kembalikanBarang(token, id).collect {
                _kembalikanState.value = it
            }
        }
    }
}
