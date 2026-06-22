package com.l0124005.sewain_rpl.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private var prefs: SharedPreferences =
        context.getSharedPreferences("sewain_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val USER_TOKEN = "user_token"
        private const val USER_ROLE = "user_role"
        private const val USER_ID = "user_id"
    }

    /**
     * Simpan token akses (Raw Token)
     */
    fun saveToken(token: String) {
        val editor = prefs.edit()
        editor.putString(USER_TOKEN, token) // Simpan raw token saja
        editor.apply()
    }

    fun saveUserId(userId: Int) {
        val editor = prefs.edit()
        editor.putInt(USER_ID, userId)
        editor.apply()
    }

    fun getUserId(): Int {
        return prefs.getInt(USER_ID, -1)
    }

    /**
     * Ambil token akses dengan prefix Bearer
     */
    fun getToken(): String {
        val token = prefs.getString(USER_TOKEN, "") ?: ""
        return if (token.isNotEmpty() && !token.startsWith("Bearer ")) {
            "Bearer $token"
        } else {
            token
        }
    }

    /**
     * Simpan role user
     */
    fun saveRole(role: String) {
        val editor = prefs.edit()
        editor.putString(USER_ROLE, role)
        editor.apply()
    }

    /**
     * Ambil role user
     */
    fun getRole(): String {
        return prefs.getString(USER_ROLE, "user") ?: "user"
    }

    /**
     * Cek apakah user sudah login
     */
    fun isLoggedIn(): Boolean {
        return getToken().isNotEmpty()
    }

    /**
     * Hapus sesi (logout)
     */
    fun clearSession() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}
