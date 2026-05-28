package com.l0124005.sewain_rpl.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ══ AUTH (tidak butuh token) ══════════════
    @POST("login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @POST("google-login")
    suspend fun googleLogin(@Body body: GoogleLoginRequest): Response<LoginResponse>

    @POST("register")
    suspend fun register(@Body body: RegisterRequest): Response<ApiResponse>

    @POST("verify-otp")
    suspend fun verifyOtp(@Body body: OtpRequest): Response<LoginResponse>

    @POST("forgot-password")
    suspend fun forgotPassword(@Body body: ForgotPasswordRequest): Response<ApiResponse>

    @POST("reset-password")
    suspend fun resetPassword(@Body body: ResetPasswordRequest): Response<ApiResponse>

    // ══ USER (butuh token) ════════════════════
    @GET("user")
    suspend fun getUser(
        @Header("Authorization") token: String
    ): Response<UserData>

    @POST("logout")
    suspend fun logout(
        @Header("Authorization") token: String
    ): Response<ApiResponse>

    @GET("profile")
    suspend fun getProfile(
        @Header("Authorization") token: String
    ): Response<UserData>

    @Multipart
    @POST("profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Part("first_name") firstName: RequestBody,
        @Part("last_name") lastName: RequestBody,
        @Part("phone") phone: RequestBody,
        @Part foto: MultipartBody.Part? = null
    ): Response<ApiResponse>
}
