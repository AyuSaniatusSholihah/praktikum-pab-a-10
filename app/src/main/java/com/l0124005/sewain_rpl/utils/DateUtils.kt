package com.l0124005.sewain_rpl.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    private const val UI_FORMAT = "dd MMMM yyyy"
    private const val BACKEND_FORMAT = "yyyy-MM-dd"
    private val localeId = Locale.forLanguageTag("id-ID")

    /**
     * Mengonversi format tanggal dari yyyy-MM-dd (Backend) ke dd MMMM yyyy (UI Indonesia)
     */
    fun formatDateForUI(dateStr: String?): String {
        if (dateStr.isNullOrEmpty()) return ""
        return try {
            val inputFormat = SimpleDateFormat(BACKEND_FORMAT, Locale.US)
            val outputFormat = SimpleDateFormat(UI_FORMAT, localeId)
            val date = inputFormat.parse(dateStr)
            date?.let { outputFormat.format(it) } ?: ""
        } catch (e: Exception) {
            dateStr
        }
    }

    /**
     * Mengonversi format tanggal dari dd MMMM yyyy (UI Indonesia) ke yyyy-MM-dd (Backend)
     */
    fun formatDateForBackend(dateStr: String?): String {
        if (dateStr.isNullOrEmpty()) return ""
        return try {
            val inputFormat = SimpleDateFormat(UI_FORMAT, localeId)
            val outputFormat = SimpleDateFormat(BACKEND_FORMAT, Locale.US)
            val date = inputFormat.parse(dateStr)
            date?.let { outputFormat.format(it) } ?: ""
        } catch (e: Exception) {
            dateStr
        }
    }

    /**
     * Format dari Date object ke UI String
     */
    fun dateToUiString(date: Date?): String {
        if (date == null) return ""
        return try {
            SimpleDateFormat(UI_FORMAT, localeId).format(date)
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Format dari Date object ke Backend String
     */
    fun dateToBackendString(date: Date?): String {
        if (date == null) return ""
        return try {
            SimpleDateFormat(BACKEND_FORMAT, Locale.US).format(date)
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Parse dari Backend String ke Date object
     */
    fun backendStringToDate(dateStr: String?): Date? {
        if (dateStr.isNullOrEmpty()) return null
        return try {
            SimpleDateFormat(BACKEND_FORMAT, Locale.US).parse(dateStr)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Format dari Millis ke UI String
     */
    fun millisToUiString(millis: Long?): String {
        if (millis == null) return ""
        return try {
            SimpleDateFormat(UI_FORMAT, localeId).format(Date(millis))
        } catch (e: Exception) {
            ""
        }
    }
}
