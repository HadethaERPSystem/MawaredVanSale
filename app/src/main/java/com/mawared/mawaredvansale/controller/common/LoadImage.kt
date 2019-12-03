package com.mawared.mawaredvansale.controller.common

import android.graphics.Bitmap
import android.os.AsyncTask
import android.graphics.BitmapFactory
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL


class LoadImage: AsyncTask<String, Void, Bitmap>() {
    override fun doInBackground(vararg params: String): Bitmap? {
        try {
            val url = URL(params[0])
            val connection = url.openConnection() as HttpURLConnection
            connection.setDoInput(true)
            connection.connect()
            val input = connection.getInputStream()
            return BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            return null
        }

    }

    override fun onPostExecute(result: Bitmap) {
        //do what you want with your bitmap result on the UI thread
    }
}