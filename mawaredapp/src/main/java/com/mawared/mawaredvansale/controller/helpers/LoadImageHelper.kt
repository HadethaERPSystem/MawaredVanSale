package com.mawared.mawaredvansale.controller.helpers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.mawared.mawaredvansale.utilities.Coroutines
import com.mawared.mawaredvansale.utilities.URL_GET_IMAGE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.URL

suspend fun loadImage(context: Context, url: String): Bitmap? = withContext(Dispatchers.IO) {
    try {
        return@withContext Glide.with(context)
            .asBitmap()
            .load(url)
            .submit()
            .get()
    } catch (e: GlideException) {
        e.logRootCauses("GlideException")
        Log.e("loadImage", "Failed to load image: ${e.message}", e)
        null
    } catch (e: Exception) {
        Log.e("loadImage", "Unexpected error: ${e.message}", e)
        null
    }
}

suspend fun loadLogo(url: String?) : Bitmap? = withContext(Dispatchers.IO) {
    var bitmap: Bitmap? = null
    try {
        if (!url.isNullOrEmpty()) {

            val conn = URL(url).openConnection()

            conn.connect()

            val length = conn.contentLength

            if (length > 0) {
                val `is`: InputStream = conn.getInputStream()

                bitmap = BitmapFactory.decodeStream(`is`)
            }
            return@withContext bitmap

        } else {
            null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
