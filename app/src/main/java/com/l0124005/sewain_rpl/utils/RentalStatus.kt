package com.l0124005.sewain_rpl.utils

import androidx.compose.ui.graphics.Color
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
                "yyyy-MM-dd HH:mm:ss",
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
            val isDisewa = status == "disewa"
            
            // 1. Prioritas Status Mutlak dari Backend
            if (status in listOf("dibatalkan", "expired", "canceled", "cancelled")) return CANCELED
            if (status in listOf("selesai", "completed", "done", "menunggu_verifikasi", "dikembalikan")) return COMPLETED
            
            // Jika belum bayar, tetap UPCOMING meskipun tanggal sudah lewat (belum pegang barang)
            // Kecuali jika sudah status "disewa" (tangan user)
            if (status in listOf("menunggu_pembayaran", "pending", "menunggu") && !isDisewa) return UPCOMING

            val tglSewa = parseFlexibleDate(item.tanggal_sewa)
            val tglKembaliRencana = parseFlexibleDate(item.tanggal_kembali_rencana)
            val now = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            // 2. Logika Berdasarkan Tanggal (Mapping ke Upcoming/Active/Return)
            return when {
                tglKembaliRencana != null && now.after(tglKembaliRencana) -> RETURN
                isDisewa -> ACTIVE
                tglSewa != null && now.before(tglSewa) -> UPCOMING
                else -> ACTIVE
            }
        }

        fun calculateFine(transaksi: TransaksiData): Double {
            val status = fromTransaksi(transaksi)
            if (status == RETURN && transaksi.tanggal_kembali_aktual.isNullOrEmpty()) {
                val tglKembaliRencana = parseFlexibleDate(transaksi.tanggal_kembali_rencana)
                val now = Calendar.getInstance().time
                if (tglKembaliRencana != null && now.after(tglKembaliRencana)) {
                    val diffInMillis = now.time - tglKembaliRencana.time
                    val diffInHours = (diffInMillis / (1000 * 60 * 60)).toDouble()
                    val fineRate = transaksi.barang?.harga_denda_perjam ?: 0.0
                    return diffInHours.coerceAtLeast(0.0) * fineRate
                }
            }
            return transaksi.total_denda
        }
    }
}
