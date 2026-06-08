package com.l0124005.sewain_rpl.network

import com.google.gson.annotations.SerializedName

// ═══════════════════════════════════════════
// REQUEST MODELS (DTO)
// ═══════════════════════════════════════════

data class RegisterRequest(
    val first_name: String,
    val last_name: String,
    val email: String,
    val phone: String?,
    val password: String,
    val password_confirmation: String
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

data class BayarRequest(
    val transaksi_ids: List<Int>,
    val jumlah_bayar: Double,
    val metode_pembayaran: String
)

// ═══════════════════════════════════════════
// RESPONSE MODELS (DTO)
// ═══════════════════════════════════════════

data class RegisterResponse(
    val success: Boolean,
    val message: String,
    val email: String?
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    @SerializedName("access_token")
    val accessToken: String?,
    @SerializedName("token_type")
    val tokenType: String?,
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
    @SerializedName("phone_number")
    val phoneNumber: String?,
    @SerializedName("email_verified_at")
    val emailVerifiedAt: String?
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
    val harga_jaminan: Double,
    val harga_denda_perjam: Double,
    val stok: Int,
    val lokasi: String,
    val foto_barang: String?,
    val status: String
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

data class CheckoutResponse(
    val status: String,
    val message: String,
    val transactions: List<TransaksiData>
)

data class BayarResponse(
    val status: String,
    val message: String
)
