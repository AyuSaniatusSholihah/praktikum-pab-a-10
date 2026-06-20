package com.l0124005.sewain_rpl.ui.theme.checkout

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

data class CartItem(
    val id: Int,
    val nama: String,
    val harga: Long,
    val qty: Int,
    val imageUrl: String,
    val tglMulai: String,
    val tglSelesai: String,
    val dendaPerJam: Long,
    val jaminanBase: Long
) {
    fun durasiHari(): Int {
        return try {
            val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id-ID"))
            val start = sdf.parse(tglMulai)
            val end = sdf.parse(tglSelesai)
            if (start != null && end != null) {
                val diff = end.time - start.time
                val days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS).toInt()
                if (days <= 0) 1 else days
            } else {
                1
            }
        } catch (e: Exception) {
            1
        }
    }

    fun subtotal(durasi: Int): Long = harga * qty * durasi
    fun jaminan(): Long = jaminanBase * qty
}
