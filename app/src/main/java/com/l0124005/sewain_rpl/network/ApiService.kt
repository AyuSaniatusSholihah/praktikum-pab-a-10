package com.l0124005.sewain_rpl.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ═══════════════════════════════════════
    // AUTHENTIKASI (Tanpa Google Login)
    // ═══════════════════════════════════════

    @POST("register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<RegisterResponse>

    @POST("verify-otp")
    suspend fun verifyOtp(
        @Body request: VerifyOtpRequest
    ): Response<LoginResponse>

    @POST("login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @POST("resend-otp")
    suspend fun resendOtp(
        @Body request: ResendOtpRequest
    ): Response<GenericResponse>

    @POST("logout")
    suspend fun logout(
        @Header("Authorization") token: String
    ): Response<LogoutResponse>


    // ═══════════════════════════════════════
    // PROFIL
    // ═══════════════════════════════════════

    @GET("profile")
    suspend fun getProfile(
        @Header("Authorization") token: String
    ): Response<ProfileResponse>

    @Multipart
    @POST("profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Query("_method") method: String = "PUT",
        @Part("name") name: RequestBody? = null,
        @Part("username") username: RequestBody? = null,
        @Part("phone_number") phoneNumber: RequestBody? = null,
        @Part("alamat") alamat: RequestBody? = null,
        @Part("tanggal_lahir") tanggalLahir: RequestBody? = null,
        @Part("jenis_kelamin") jenisKelamin: RequestBody? = null,
        @Part foto_profil: MultipartBody.Part? = null
    ): Response<ProfileResponse>


    // ═══════════════════════════════════════
    // PRODUK / KATALOG
    // ═══════════════════════════════════════

    @GET("katalog-publik")
    suspend fun getKatalogPublik(
        @Query("search") search: String? = null,
        @Query("kategori_id") kategoriId: Int? = null,
        @Query("lokasi") lokasi: String? = null,
        @Query("min_harga") minHarga: Double? = null,
        @Query("max_harga") maxHarga: Double? = null
    ): Response<KatalogListResponse>

    @GET("katalog-publik/{id}")
    suspend fun getKatalogPublikDetail(
        @Path("id") id: Int
    ): Response<KatalogDetailResponse>

    @GET("katalog")
    suspend fun getMyKatalog(
        @Header("Authorization") token: String
    ): Response<KatalogListResponse>

    @GET("katalog/{id}")
    suspend fun getMyKatalogDetail(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<KatalogDetailResponse>

    @Multipart
    @POST("katalog")
    suspend fun createKatalog(
        @Header("Authorization") token: String,
        @Part("kategori_id") kategoriId: RequestBody,
        @Part("nama_barang") namaBarang: RequestBody,
        @Part("deskripsi") deskripsi: RequestBody?,
        @Part("harga_sewa") hargaSewa: RequestBody,
        @Part("harga_jaminan") hargaJaminan: RequestBody,
        @Part("harga_denda_perjam") hargaDendaPerjam: RequestBody,
        @Part("stok") stok: RequestBody,
        @Part("lokasi") lokasi: RequestBody,
        @Part("whatsapp") whatsapp: RequestBody? = null,
        @Part("tanggal_mulai") tanggalMulai: RequestBody? = null,
        @Part("tanggal_akhir") tanggalAkhir: RequestBody? = null,
        @Part("additional_information") additionalInformation: RequestBody?,
        @Part foto_barang: MultipartBody.Part?,     // Nama field diambil dari Part.createFormData("foto_barang", ...)
        @Part fotoproduk1: MultipartBody.Part? = null,
        @Part fotoproduk2: MultipartBody.Part? = null,
        @Part fotoproduk3: MultipartBody.Part? = null,
        @Part fotoproduk4: MultipartBody.Part? = null,
        @Part("status") status: RequestBody? = null
    ): Response<KatalogCrudResponse>

    @Multipart
    @POST("katalog/{id}")
    suspend fun updateKatalog(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Query("_method") method: String = "PUT",
        @Part("kategori_id") kategoriId: RequestBody? = null,
        @Part("nama_barang") namaBarang: RequestBody? = null,
        @Part("deskripsi") deskripsi: RequestBody? = null,
        @Part("harga_sewa") hargaSewa: RequestBody? = null,
        @Part("harga_jaminan") hargaJaminan: RequestBody? = null,
        @Part("harga_denda_perjam") hargaDendaPerjam: RequestBody? = null,
        @Part("stok") stok: RequestBody? = null,
        @Part("lokasi") lokasi: RequestBody? = null,
        @Part("whatsapp") whatsapp: RequestBody? = null,
        @Part("tanggal_mulai") tanggalMulai: RequestBody? = null,
        @Part("tanggal_akhir") tanggalAkhir: RequestBody? = null,
        @Part("additional_information") additionalInformation: RequestBody? = null,
        @Part foto_barang: MultipartBody.Part? = null,
        @Part fotoproduk1: MultipartBody.Part? = null,
        @Part fotoproduk2: MultipartBody.Part? = null,
        @Part fotoproduk3: MultipartBody.Part? = null,
        @Part fotoproduk4: MultipartBody.Part? = null,
        @Part("status") status: RequestBody? = null
    ): Response<KatalogCrudResponse>

    @DELETE("katalog/{id}")
    suspend fun deleteKatalog(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<GenericResponse>

    @GET("kategori")
    suspend fun getKategori(): Response<KategoriListResponse>


    // ═══════════════════════════════════════
    // KERANJANG
    // ═══════════════════════════════════════

    @GET("keranjang")
    suspend fun getKeranjang(
        @Header("Authorization") token: String
    ): Response<KeranjangResponse>

    @POST("keranjang/items")
    suspend fun addToKeranjang(
        @Header("Authorization") token: String,
        @Body request: AddToKeranjangRequest
    ): Response<KeranjangResponse>

    @PATCH("keranjang/items/{id}")
    suspend fun updateKeranjangItem(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: UpdateKeranjangRequest
    ): Response<KeranjangResponse>

    @DELETE("keranjang/items/{id}")
    suspend fun removeKeranjangItem(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<KeranjangResponse>

    @DELETE("keranjang")
    suspend fun clearKeranjang(
        @Header("Authorization") token: String
    ): Response<GenericResponse>


    // ═══════════════════════════════════════
    // TRANSAKSI & PEMBAYARAN (Tenant / Penyewa)
    // ═══════════════════════════════════════

    @GET("transaksi")
    suspend fun getRiwayatTransaksi(
        @Header("Authorization") token: String
    ): Response<TransaksiListResponse>

    @GET("transaksi/{id}")
    suspend fun getDetailTransaksi(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<TransaksiDetailResponse>

    @POST("checkout")
    suspend fun checkout(
        @Header("Authorization") token: String,
        @Body request: CheckoutRequest
    ): Response<CheckoutResponse>

    @POST("transaksi/bayar")
    suspend fun bayar(
        @Header("Authorization") token: String,
        @Body request: BayarRequest
    ): Response<BayarResponse>

    @Multipart
    @POST("transaksi/{id}/kembalikan")
    suspend fun kembalikanBarang(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Part foto_buktipengembalian: MultipartBody.Part,
        @Part("rating") rating: RequestBody,
        @Part("komentar") komentar: RequestBody? = null
    ): Response<KembalikanResponse>


    // ═══════════════════════════════════════
    // TRANSAKSI & PEMBAYARAN (Owner / Pemilik)
    // ═══════════════════════════════════════

    @GET("owner/dashboard")
    suspend fun getOwnerDashboard(
        @Header("Authorization") token: String
    ): Response<OwnerDashboardResponse>

    @GET("owner/transaksi/{id}")
    suspend fun getOwnerTransaksiDetail(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<TransaksiDetailResponse>

    @GET("owner/pengembalian")
    suspend fun getOwnerListPengembalian(
        @Header("Authorization") token: String
    ): Response<TransaksiListResponse>

    @POST("transaksi/{id}/verifikasi-pengembalian")
    suspend fun verifikasiPengembalian(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: VerifikasiPengembalianRequest
    ): Response<VerifikasiPengembalianResponse>
}
