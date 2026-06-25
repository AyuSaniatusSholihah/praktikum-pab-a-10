package com.l0124005.sewain_rpl.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {
    private const val UI_FORMAT = "dd MMMM yyyy"
    private const val UI_FULL_FORMAT = "dd MMMM yyyy HH:mm 'WIB'"
    private const val BACKEND_FORMAT = "yyyy-MM-dd"
    private const val BACKEND_FULL_FORMAT = "yyyy-MM-dd HH:mm:ss"
    private val localeId = Locale.forLanguageTag("id-ID")

    /**
     * Mengonversi format tanggal dari yyyy-MM-dd (Backend) ke dd MMMM yyyy (UI Indonesia)
     */
    fun formatDateForUI(dateStr: String?): String {
        if (dateStr.isNullOrEmpty()) return ""
        return try {
            val date = parseFlexibleDate(dateStr)
            date?.let { SimpleDateFormat(UI_FORMAT, localeId).format(it) } ?: dateStr
        } catch (e: Exception) {
            dateStr
        }
    }

    /**
     * Mengonversi format tanggal ke format lengkap UI dengan jam
     */
    fun formatFullDateForUI(dateStr: String?): String {
        if (dateStr.isNullOrEmpty()) return ""
        return try {
            val date = parseFlexibleDate(dateStr)
            date?.let { SimpleDateFormat(UI_FULL_FORMAT, localeId).format(it) } ?: dateStr
        } catch (e: Exception) {
            dateStr
        }
    }

    /**
     * Menghitung selisih hari antara dua string tanggal
     */
    fun calculateDaysBetween(start: String?, end: String?): Int {
        if (start.isNullOrEmpty() || end.isNullOrEmpty()) return 0
        return try {
            val d1 = parseFlexibleDate(start)
            val d2 = parseFlexibleDate(end)
            if (d1 != null && d2 != null) {
                val diff = (d2.time - d1.time) / (1000 * 60 * 60 * 24)
                if (diff > 0) diff.toInt() else 1
            } else 1
        } catch (e: Exception) {
            1
        }
    }

    fun parseFlexibleDate(dateStr: String?): Date? {
        if (dateStr.isNullOrEmpty() || dateStr.startsWith("0000")) return null
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm",
            "yyyy-MM-dd"
        )
        for (fmt in formats) {
            try {
                val sdf = SimpleDateFormat(fmt, Locale.US)
                if (fmt.contains("'Z'")) sdf.timeZone = TimeZone.getTimeZone("UTC")
                val d = sdf.parse(dateStr)
                if (d != null) return d
            } catch (e: Exception) {}
        }
        return null
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
     * Format dari Date object ke UI String Lengkap (dengan jam & WIB)
     */
    fun dateToFullUiString(date: Date?): String {
        if (date == null) return ""
        return try {
            SimpleDateFormat(UI_FULL_FORMAT, localeId).format(date)
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
     * Format dari Date object ke Backend String Lengkap (dengan jam)
     */
    fun dateToFullBackendString(date: Date?): String {
        if (date == null) return ""
        return try {
            SimpleDateFormat(BACKEND_FULL_FORMAT, Locale.US).format(date)
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Mengambil tanggal hari ini dalam format Backend
     */
    fun getCurrentDateBackend(): String {
        return try {
            SimpleDateFormat(BACKEND_FORMAT, Locale.US).format(Date())
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Mengambil tanggal besok dalam format Backend
     */
    fun getTomorrowDateBackend(): String {
        return try {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            SimpleDateFormat(BACKEND_FORMAT, Locale.US).format(calendar.time)
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Parse dari Backend String ke Date object (mendukung berbagai format ISO & Standar)
     */
    fun backendStringToDate(dateStr: String?): Date? {
        return parseFlexibleDate(dateStr)
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
