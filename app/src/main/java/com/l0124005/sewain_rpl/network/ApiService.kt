package com.l0124005.sewain_rpl.network

import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ═══════════════════════════════════════
    // AUTHENTIKASI
    // ═══════════════════════════════════════

    @POST("register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<RegisterResponse>

    @POST("login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @POST("logout")
    suspend fun logout(
        @Header("Authorization") token: String
    ): Response<LogoutResponse>


    // ═══════════════════════════════════════
    // PRODUK
    // ═══════════════════════════════════════

    @GET("katalog-publik")
    suspend fun getKatalogPublik(): Response<KatalogListResponse>

    @GET("katalog/{id}")
    suspend fun getKatalogDetail(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<CatalogResponse>


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
        @Body request: UpdateKeranjangItemRequest
    ): Response<KeranjangResponse>

    @DELETE("keranjang/items/{id}")
    suspend fun removeKeranjangItem(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<KeranjangResponse>


    // ═══════════════════════════════════════
    // TRANSAKSI & PEMBAYARAN
    // ═══════════════════════════════════════

    @POST("checkout")
    suspend fun checkout(
        @Header("Authorization") token: String
    ): Response<CheckoutResponse>

    @POST("transaksi/bayar")
    suspend fun bayarMassal(
        @Header("Authorization") token: String,
        @Body request: BayarMassalRequest
    ): Response<BayarMassalResponse>

    @GET("transaksi")
    suspend fun getTransaksi(
        @Header("Authorization") token: String
    ): Response<TransaksiListResponse>

    @GET("transaksi/{id}")
    suspend fun getTransaksiDetail(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<TransaksiDetailResponse>

    @POST("transaksi/{id}/kembalikan")
    suspend fun kembalikanBarang(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<KembalikanResponse>


    // ═══════════════════════════════════════
    // PROFIL
    // ═══════════════════════════════════════

    @GET("profile")
    suspend fun getProfile(
        @Header("Authorization") token: String
    ): Response<ProfileResponse>

    @POST("profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): Response<ProfileResponse>
}
