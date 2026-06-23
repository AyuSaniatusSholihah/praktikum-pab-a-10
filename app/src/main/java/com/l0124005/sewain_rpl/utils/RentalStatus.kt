package com.l0124005.sewain_rpl.utils

import androidx.compose.ui.graphics.Color
import com.l0124005.sewain_rpl.network.ApiClient
import com.l0124005.sewain_rpl.network.TransaksiData
import java.text.SimpleDateFormat
import java.util.*

// ── Warna badge status ──
private val StatusActiveBg    = Color(0xFFC3D4E9)
private val StatusActiveText  = Color(0xFF1e3a5f)
private val StatusUpcomingBg  = Color(0xFFFFF9C4)
private val StatusUpcomingText = Color(0xFF827717)
private val StatusCompletedBg  = Color(0xFFC8E6C9)
private val StatusCompletedText = Color(0xFF2E7D32)
private val StatusCanceledBg  = Color(0xFFffcdd2)
private val StatusCanceledText = Color(0xFFc62828)
private val StatusReturnBg   = Color(0xFFffe3c4)
private val StatusReturnText  = Color(0xFFa35a17)

enum class RentalStatus(
    val label: String,
    val badgeColor: Color,
    val badgeTextColor: Color
) {
    ACTIVE("Active Rent", StatusActiveBg, StatusActiveText),
    UPCOMING("Upcoming Rent", StatusUpcomingBg, StatusUpcomingText),
    COMPLETED("Completed Rent", StatusCompletedBg, StatusCompletedText),
    CANCELED("Canceled Rent", StatusCanceledBg, StatusCanceledText),
    RETURN("Return Rent", StatusReturnBg, StatusReturnText);

    companion object {
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
                    val sdf = SimpleDateFormat(fmt, Locale.getDefault())
                    if (fmt.contains("'Z'")) sdf.timeZone = TimeZone.getTimeZone("UTC")
                    val d = sdf.parse(dateStr)
                    if (d != null) return d
                } catch (e: Exception) {}
            }
            return null
        }

        fun fromTransaksi(item: TransaksiData): RentalStatus {
            val status = item.status.lowercase()
            val isDisewa = status in listOf("disewa", "aktif", "active")
            val hasBukti = !item.foto_buktipengembalian.isNullOrEmpty() && item.foto_buktipengembalian != "null"
            
            // 1. Prioritas Status Mutlak dari Backend
            if (status in listOf("dibatalkan", "expired", "canceled", "cancelled")) return CANCELED
            if (status in listOf("selesai", "completed", "done")) return COMPLETED
            
            // Logic khusus: Jika sudah ada bukti pengembalian atau status verifikasi, maka RETURN
            if (status in listOf("menunggu_verifikasi", "dikembalikan", "tunggu verifikasi pengembalian") || hasBukti) return RETURN
            
            // Jika belum bayar atau status menunggu, tetap UPCOMING
            if (status in listOf("menunggu_pembayaran", "menunggu pembayaran", "pending", "menunggu") && !isDisewa) return UPCOMING

            val tglSewa = parseFlexibleDate(item.tanggal_sewa)
            // val tglKembaliRencana = parseFlexibleDate(item.tanggal_kembali_rencana)
            // val now = Calendar.getInstance().time

            // 2. Logika Berdasarkan Status 'Disewa'
            // Walaupun sudah lewat tanggal kembali, jika belum dikembalikan (hasBukti = false), 
            // status tetap ACTIVE (sesuai permintaan user).
            return when {
                isDisewa -> ACTIVE
                tglSewa != null && Calendar.getInstance().time.before(tglSewa) -> UPCOMING
                else -> ACTIVE
            }
        }

        fun calculateFine(transaksi: TransaksiData, referenceTime: Date? = null): Double {
            val tglKembaliRencana = parseFlexibleDate(transaksi.tanggal_kembali_rencana)
            val tglKembaliAktual = parseFlexibleDate(transaksi.tanggal_kembali_aktual)
            
            val fineRate = transaksi.barang?.harga_denda_perjam ?: 0.0

            // 1. Jika sudah ada tanggal aktual (sudah dikembalikan), hitung denda berdasarkan itu
            if (tglKembaliAktual != null && tglKembaliRencana != null) {
                if (tglKembaliAktual.after(tglKembaliRencana)) {
                    val diffInMillis = tglKembaliAktual.time - tglKembaliRencana.time
                    val diffInHours = diffInMillis.toDouble() / (1000.0 * 60 * 60)
                    return (diffInHours * fineRate).coerceAtLeast(0.0)
                }
                return 0.0
            }

            // 2. Jika belum ada tanggal aktual, tapi statusnya ACTIVE atau RETURN (menunggu verifikasi), 
            // hitung estimasi denda sampai "sekarang" (referenceTime)
            val status = fromTransaksi(transaksi)
            if ((status == ACTIVE || status == RETURN) && tglKembaliRencana != null) {
                val now = referenceTime ?: Calendar.getInstance().time
                if (now.after(tglKembaliRencana)) {
                    val diffInMillis = now.time - tglKembaliRencana.time
                    val diffInHours = diffInMillis.toDouble() / (1000.0 * 60 * 60)
                    return (diffInHours * fineRate).coerceAtLeast(0.0)
                }
            }

            // 3. Fallback ke total_denda dari backend
            return transaksi.total_denda
        }

        fun calculateLateHours(transaksi: TransaksiData, referenceTime: Date? = null): Double {
            val tglKembaliRencana = parseFlexibleDate(transaksi.tanggal_kembali_rencana)
            val tglKembaliAktual = parseFlexibleDate(transaksi.tanggal_kembali_aktual)
            
            if (tglKembaliRencana == null) return 0.0

            val endTime = tglKembaliAktual ?: referenceTime ?: Calendar.getInstance().time
            if (endTime.after(tglKembaliRencana)) {
                val diffInMillis = endTime.time - tglKembaliRencana.time
                return diffInMillis.toDouble() / (1000.0 * 60 * 60)
            }
            return 0.0
        }

        fun buildPhotoUrl(foto: String?, name: String): String {
            return if (!foto.isNullOrEmpty()) {
                if (foto.startsWith("http")) foto
                else {
                    val path = if (foto.startsWith("profiles/")) foto else "profiles/$foto"
                    "${ApiClient.IMAGE_BASE_URL}$path"
                }
            } else {
                "https://ui-avatars.com/api/?name=$name"
            }
        }
    }
}
