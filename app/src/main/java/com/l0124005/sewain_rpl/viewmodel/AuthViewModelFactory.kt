package com.l0124005.sewain_rpl.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.l0124005.sewain_rpl.repository.*

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

// Factory untuk KeranjangViewModel
class KeranjangViewModelFactory(private val repo: KeranjangRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(KeranjangViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return KeranjangViewModel(repo) as T
        }
        throw IllegalArgumentException("ViewModel tidak dikenali")
    }
}

// Factory untuk TransaksiViewModel
class TransaksiViewModelFactory(private val repo: TransaksiRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransaksiViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransaksiViewModel(repo) as T
        }
        throw IllegalArgumentException("ViewModel tidak dikenali")
    }
}

// Factory untuk ProfileViewModel
class ProfileViewModelFactory(private val repo: ProfileRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(repo) as T
        }
        throw IllegalArgumentException("ViewModel tidak dikenali")
    }
}

// Factory untuk PaymentViewModel
class PaymentViewModelFactory(
    private val transaksiRepo: TransaksiRepository,
    private val profileRepo: ProfileRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PaymentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PaymentViewModel(transaksiRepo, profileRepo) as T
        }
        throw IllegalArgumentException("ViewModel tidak dikenali")
    }
}

// Factory untuk VerificationViewModel
class VerificationViewModelFactory(
    private val transaksiRepo: TransaksiRepository,
    private val profileRepo: ProfileRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VerificationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VerificationViewModel(transaksiRepo, profileRepo) as T
        }
        throw IllegalArgumentException("ViewModel tidak dikenali")
    }
}
