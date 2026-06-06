package com.l0124005.sewain_rpl.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.l0124005.sewain_rpl.network.CatalogResponse
import com.l0124005.sewain_rpl.network.KatalogListResponse
import com.l0124005.sewain_rpl.repository.KatalogRepository
import com.l0124005.sewain_rpl.utils.Resource
import kotlinx.coroutines.launch

class KatalogViewModel(private val repository: KatalogRepository) : ViewModel() {

    private val _katalogPublik = MutableLiveData<Resource<KatalogListResponse>>()
    val katalogPublik: LiveData<Resource<KatalogListResponse>> = _katalogPublik

    private val _katalogDetail = MutableLiveData<Resource<CatalogResponse>>()
    val katalogDetail: LiveData<Resource<CatalogResponse>> = _katalogDetail

    fun getKatalogPublik() {
        viewModelScope.launch {
            repository.getKatalogPublik().collect {
                _katalogPublik.value = it
            }
        }
    }

    fun getKatalogDetail(token: String, id: Int) {
        viewModelScope.launch {
            repository.getKatalogDetail(token, id).collect {
                _katalogDetail.value = it
            }
        }
    }
}
