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
    // PRODUK / KATALOG
    // ═══════════════════════════════════════

    @GET("katalog-publik")
    suspend fun getKatalogPublik(
        @Query("search") search: String? = null,
        @Query("kategori") kategori: String? = null
    ): Response<KatalogListResponse>


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
    suspend fun bayar(
        @Header("Authorization") token: String,
        @Body request: BayarRequest
    ): Response<BayarResponse>


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
