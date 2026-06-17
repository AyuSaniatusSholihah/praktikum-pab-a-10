package com.l0124005.sewain_rpl.utils

import java.text.NumberFormat
import java.util.Locale

object CurrencyUtils {
    fun formatRupiah(amount: Number): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return formatter.format(amount).replace("Rp", "Rp ").trim()
    }

    // Simple format without currency symbol
    fun formatHarga(amount: Number): String {
        val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
        return formatter.format(amount)
    }
}