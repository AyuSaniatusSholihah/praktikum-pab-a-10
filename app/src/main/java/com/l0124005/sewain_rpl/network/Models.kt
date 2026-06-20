package com.l0124005.sewain_rpl.network

import com.google.gson.annotations.SerializedName

// ════════════════════════════════════════════════════════════
// DATA CLASSES (DTO)
// ════════════════════════════════════════════════════════════

// Auth DTOs
data class RegisterRequest(
    val first_name: String, 
    val last_name: String, 
    val email: String, 
    val phone: String?, 
    val password: String, 
    val password_confirmation: String
)

data class RegisterResponse(
    val success: Boolean, 
    val message: String, 
    val email: String?
)

data class VerifyOtpRequest(
    val email: String,
    val otp: String
)

data class ResendOtpRequest(
    val email: String
)

data class LoginRequest(
    val email: String, 
    val password: String
)

data class LoginResponse(
    val success: Boolean, 
    val message: String, 
    @SerializedName("access_token")
    val access_token: String?, 
    @SerializedName("token_type")
    val token_type: String?, 
    val user: UserData?,
    val is_verified: Boolean? = null,
    val email: String? = null
)

data class LogoutResponse(
    val success: Boolean, 
    val message: String
)

// User / Profile DTOs
data class UserData(
    val id: Int, 
    val name: String, 
    val username: String?,
    val email: String, 
    val phone_number: String?, 
    val alamat: String?,
    val saldo: Double,
    val foto_profil: String?,
    val is_banned: Boolean,
    val role: String,
    val email_verified_at: String?
)

data class ProfileResponse(
    val status: String, 
    val message: String?, 
    val data: UserData
)

// Kategori & Catalog DTOs
data class KategoriData(
    val id: Int,
    val nama_kategori: String,
    val deskripsi: String?
)

data class KategoriListResponse(
    val status: String,
    val data: List<KategoriData>
)

data class CatalogData(
    val id: Int, 
    val user_id: Int,
    val kategori_id: Int,
    val nama_barang: String, 
    val deskripsi: String?, 
    val additional_information: String? = null,
    val harga_sewa: Double, 
    val harga_jaminan: Double,
    val harga_denda_perjam: Double,
    val stok: Int,
    val lokasi: String,
    val foto_barang: String?,
    val status: String,
    val kategori: KategoriData? = null
)

data class KatalogListResponse(
    val status: String, 
    val data: List<CatalogData>
)

data class KatalogDetailResponse(
    val status: String, 
    val data: CatalogData
)

data class KatalogCrudResponse(
    val status: String, 
    val message: String?, 
    val data: CatalogData?
)

// Keranjang DTOs
data class AddToKeranjangRequest(
    val barang_id: Int, 
    val jumlah: Int,
    val tanggal_sewa: String, 
    val tanggal_kembali_rencana: String
)

data class UpdateKeranjangRequest(
    val jumlah: Int? = null,
    val tanggal_sewa: String? = null,
    val tanggal_kembali_rencana: String? = null
)

data class KeranjangResponse(
    val status: String, 
    val message: String?, 
    val data: KeranjangData?
)

data class KeranjangData(
    val items: List<KeranjangItem>,
    val total_estimasi: Double
)

data class KeranjangItem(
    val id: Int, 
    val barang: CatalogData, 
    val jumlah: Int, 
    val tanggal_sewa: String, 
    val tanggal_kembali_rencana: String, 
    val subtotal: Double
)

// Transaksi & Pembayaran DTOs
data class PembayaranData(
    val id: Int,
    val metode: String,
    val detail_metode: String?,
    val tanggal_bayar: String?,
    val jumlah_bayar: Double
)

data class ReviewData(
    val id: Int,
    val transaksi_id: Int,
    val user_id: Int,
    val barang_id: Int,
    val rating: Int,
    val komentar: String?,
    val foto_review: String?
)

data class TransaksiData(
    val id: Int, 
    val user_id: Int, 
    val barang_id: Int,
    val pembayaran_id: Int?,
    val jumlah: Int,
    val tanggal_sewa: String, 
    val tanggal_kembali_rencana: String,
    val tanggal_kembali_aktual: String?,
    val status: String, 
    val foto_buktipengembalian: String?,
    val tanggal_verifikasipengembalian: String?,
    val total_harga: Double,
    val jam_terlambat: Int,
    val total_denda: Double,
    val barang: CatalogData? = null,
    val user: UserData? = null,
    val pembayaran: PembayaranData? = null,
    val review: ReviewData? = null
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
    val message: String?, 
    val data: CheckoutData?
)

data class CheckoutData(
    val transaksi: List<TransaksiData>
)

data class BayarRequest(
    val transaksi_ids: List<Int>, 
    val metode: String, 
    val detail_metode: String
)

data class BayarResponse(
    val status: String, 
    val message: String?, 
    val data: BayarData?
)

data class BayarData(
    val pembayaran: PembayaranData,
    val transaksi: List<TransaksiData>
)

data class KembalikanResponse(
    val status: String,
    val message: String?,
    val data: KembalikanData?
)

data class KembalikanData(
    val transaksi: TransaksiData,
    val review: ReviewData
)

// Owner Dashboard DTOs
data class OwnerDashboardResponse(
    val status: String,
    val data: OwnerDashboardData
)

data class OwnerDashboardData(
    val owner_name: String,
    val saldo_user: Double,
    val total_saldo_pendapatan: Double,
    val total_barang: Int,
    val daftar_transaksi: List<TransaksiData>
)

data class VerifikasiPengembalianRequest(
    val status_kondisi: String,
    val denda_kerusakan: Double? = null
)

data class VerifikasiPengembalianResponse(
    val status: String,
    val message: String?,
    val data: VerifikasiPengembalianData?
)

data class VerifikasiPengembalianData(
    val transaksi: TransaksiData
)

data class GenericResponse(
    val status: String,
    val message: String?
)
