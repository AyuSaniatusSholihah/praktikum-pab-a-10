package com.l0124005.sewain_rpl.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.l0124005.sewain_rpl.repository.AuthRepository
import com.l0124005.sewain_rpl.repository.KatalogRepository

// Factory untuk AuthViewModel
class AuthViewModelFactory(private val repo: AuthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repo) as T
        }
        throw IllegalArgumentException("ViewModel tidak dikenali")
    }
}

// Factory untuk KatalogViewModel
class KatalogViewModelFactory(private val repo: KatalogRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(KatalogViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return KatalogViewModel(repo) as T
        }
        throw IllegalArgumentException("ViewModel tidak dikenali")
    }
}
