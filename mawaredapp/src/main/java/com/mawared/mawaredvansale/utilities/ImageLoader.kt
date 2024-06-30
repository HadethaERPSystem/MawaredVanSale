package com.mawared.mawaredvansale.utilities

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.lang.Exception
import java.net.URL

class ImageLoader {
    fun LoadImageFromUrl(FileName: String?, loadImage: (Bitmap?) -> Unit) {

        try {
            var bmp: Bitmap? = null
            Coroutines.ioThenMain({
                //val bmp = BitmapFactory.decodeResource(ctx.resources, R.mipmap.ic_logo_black)
                val url = URL(FileName)
                val conn = url.openConnection()
                conn.connect()
                val l = conn.contentLength
                if (l > 0) {
                    val `in` = conn.getInputStream()
                    bmp = BitmapFactory.decodeStream(`in`)
                }
            }, {loadImage(bmp)})


        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}