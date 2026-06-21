package com.l0124005.sewain_rpl.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.l0124005.sewain_rpl.network.*
import com.l0124005.sewain_rpl.repository.TransaksiRepository
import com.l0124005.sewain_rpl.utils.Resource
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

class TransaksiViewModel(private val repository: TransaksiRepository) : ViewModel() {

    private val _checkoutState = MutableLiveData<Resource<CheckoutResponse>?>()
    val checkoutState: LiveData<Resource<CheckoutResponse>?> = _checkoutState

    private val _pembayaranState = MutableLiveData<Resource<BayarResponse>?>()
    val pembayaranState: LiveData<Resource<BayarResponse>?> = _pembayaranState

    private val _transaksiList = MutableLiveData<Resource<TransaksiListResponse>?>()
    val transaksiList: LiveData<Resource<TransaksiListResponse>?> = _transaksiList

    private val _transaksiDetail = MutableLiveData<Resource<TransaksiDetailResponse>?>()
    val transaksiDetail: LiveData<Resource<TransaksiDetailResponse>?> = _transaksiDetail

    private val _kembalikanState = MutableLiveData<Resource<KembalikanResponse>?>()
    val kembalikanState: LiveData<Resource<KembalikanResponse>?> = _kembalikanState

    // Owner Side
    private val _ownerDashboard = MutableLiveData<Resource<OwnerDashboardResponse>?>()
    val ownerDashboard: LiveData<Resource<OwnerDashboardResponse>?> = _ownerDashboard

    private val _verifikasiState = MutableLiveData<Resource<VerifikasiPengembalianResponse>?>()
    val verifikasiState: LiveData<Resource<VerifikasiPengembalianResponse>?> = _verifikasiState

    fun resetCheckoutState() {
        _checkoutState.value = null
    }

    fun resetStates() {
        _checkoutState.value = null
        _pembayaranState.value = null
        _transaksiList.value = null
        _transaksiDetail.value = null
        _kembalikanState.value = null
        _ownerDashboard.value = null
        _verifikasiState.value = null
    }

    fun getRiwayatTransaksi(token: String) {
        viewModelScope.launch {
            repository.getRiwayatTransaksi(token).collect {
                _transaksiList.value = it
            }
        }
    }

    fun getDetailTransaksi(token: String, id: Int) {
        viewModelScope.launch {
            repository.getDetailTransaksi(token, id).collect {
                _transaksiDetail.value = it
            }
        }
    }

    fun checkout(token: String, keranjangIds: List<Int>? = null) {
        viewModelScope.launch {
            repository.checkout(token, keranjangIds).collect {
                _checkoutState.value = it
            }
        }
    }

    fun bayar(token: String, request: BayarRequest) {
        viewModelScope.launch {
            repository.bayar(token, request).collect {
                _pembayaranState.value = it
            }
        }
    }

    fun kembalikanBarang(
        token: String,
        id: Int,
        fotoBukti: MultipartBody.Part,
        rating: RequestBody,
        komentar: RequestBody? = null
    ) {
        viewModelScope.launch {
            repository.kembalikanBarang(token, id, fotoBukti, rating, komentar).collect {
                _kembalikanState.value = it
            }
        }
    }

    // Owner Methods
    fun getOwnerDashboard(token: String) {
        viewModelScope.launch {
            repository.getOwnerDashboard(token).collect {
                _ownerDashboard.value = it
            }
        }
    }

    fun verifikasiPengembalian(token: String, id: Int, request: VerifikasiPengembalianRequest) {
        viewModelScope.launch {
            repository.verifikasiPengembalian(token, id, request).collect {
                _verifikasiState.value = it
            }
        }
    }
}
