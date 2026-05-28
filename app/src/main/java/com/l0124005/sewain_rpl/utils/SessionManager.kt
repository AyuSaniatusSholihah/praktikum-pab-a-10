package com.l0124005.sewain_rpl.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private var prefs: SharedPreferences =
        context.getSharedPreferences("sewain_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val USER_TOKEN = "user_token"
        private const val USER_ROLE = "user_role"
    }

    /**
     * Simpan token akses
     */
    fun saveToken(token: String) {
        val editor = prefs.edit()
        editor.putString(USER_TOKEN, "Bearer $token")
        editor.apply()
    }

    /**
     * Ambil token akses
     */
    fun getToken(): String {
        return prefs.getString(USER_TOKEN, "") ?: ""
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
