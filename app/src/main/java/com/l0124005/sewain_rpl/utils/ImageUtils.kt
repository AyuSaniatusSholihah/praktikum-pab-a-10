package com.l0124005.sewain_rpl.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object ImageUtils {
    fun compressImage(context: Context, uri: Uri, maxSizeKb: Int = 2048): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val file = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
            var quality = 100
            val byteArrayOutputStream = ByteArrayOutputStream()
            
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
            
            // Gradually reduce quality until it fits under maxSizeKb
            while (byteArrayOutputStream.toByteArray().size / 1024 > maxSizeKb && quality > 10) {
                byteArrayOutputStream.reset()
                quality -= 10
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
            }

            val fileOutputStream = FileOutputStream(file)
            fileOutputStream.write(byteArrayOutputStream.toByteArray())
            fileOutputStream.flush()
            fileOutputStream.close()
            
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
