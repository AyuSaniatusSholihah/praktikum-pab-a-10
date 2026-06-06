package com.l0124005.sewain_rpl.network

import com.google.gson.annotations.SerializedName

// ═══════════════════════════════════════════
// REQUEST MODELS
// ═══════════════════════════════════════════

data class RegisterRequest(
    val name: String,
    val email: String,
    val nomor_telepon: String,
    val password: String? = null // Menambahkan password jika diperlukan backend
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class UpdateProfileRequest(
    val name: String?,
    val nomor_telepon: String?,
    val alamat: String?
)

data class AddToKeranjangRequest(
    val barang_id: Int,
    val tanggal_mulai: String,
    val tanggal_selesai: String,
    val jumlah: Int
)

data class UpdateKeranjangItemRequest(
    val tanggal_mulai: String?,
    val tanggal_selesai: String?,
    val jumlah: Int?
)

data class BayarMassalRequest(
    val transaksi_ids: List<Int>,
    val jumlah_bayar: Double,
    val metode_pembayaran: String
)

// ═══════════════════════════════════════════
// RESPONSE MODELS
// ═══════════════════════════════════════════

data class RegisterResponse(
    val success: Boolean,
    val message: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val token: String?,
    val user: UserData?
)

data class LogoutResponse(
    val success: Boolean,
    val message: String
)

data class UserData(
    val id: Int,
    val name: String,
    val email: String,
    val nomor_telepon: String
)

data class ProfileResponse(
    val success: Boolean,
    val message: String?,
    val user: UserData
)

data class CatalogData(
    val id: Int,
    val nama_barang: String,
    val deskripsi: String?,
    val harga_sewa: Double,
    val status: String
)

data class CatalogResponse(
    val status: String,
    val data: CatalogData
)

data class KatalogListResponse(
    val status: String,
    val data: List<CatalogData>
)

data class KeranjangResponse(
    val status: String,
    val message: String?,
    val data: KeranjangData?
)

data class KeranjangData(
    val id: Int,
    val user_id: Int,
    val items: List<KeranjangItem>
)

data class KeranjangItem(
    val id: Int,
    val barang_id: Int,
    val barang: CatalogData,
    val tanggal_mulai: String,
    val tanggal_selesai: String,
    val jumlah: Int,
    val total_harga: Double
)

data class TransaksiData(
    val id: Int,
    val user_id: Int,
    val barang_id: Int,
    val status: String,
    val tanggal_sewa: String,
    val tanggal_kembali: String?,
    val total_harga: Double
)

data class TransaksiListResponse(
    val status: String,
    val data: List<TransaksiData>
)

data class TransaksiDetailResponse(
    val status: String,
    val data: TransaksiData
)

data class CheckoutResponse(
    val status: String,
    val message: String,
    val transactions: List<TransaksiData>
)

data class BayarMassalResponse(
    val status: String,
    val message: String
)

data class KembalikanResponse(
    val status: String,
    val message: String
)

data class ApiResponse(
    val success: Boolean,
    val message: String
)
