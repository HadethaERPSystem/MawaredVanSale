package com.mawared.mawaredvansale.controller.common

import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Rect
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.regex.Pattern

/**
 * Created by NO on 2017/9/14.
 */

object Utility {
    fun checkBlueboothPermission(context: Activity, permission: String, requestPermissions: Array<String>, callback: Callback) {
        if (Build.VERSION.SDK_INT >= 23) {
            //校验是否已具有模糊定位权限
            if (ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) !== PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(context, requestPermissions, 100)
            } else {
                //具有权限
                callback.permit()
                return
            }
        } else {
            //系统不高于6.0直接执行
            callback.pass()
        }
    }

    interface Callback {
        /**
         * API>=23 允许权限
         */
        fun permit()


        /**
         * API<23 无需授予权限
         */
        fun pass()
    }

    @Throws(IOException::class)
    fun writeBytesToFile(iStream: InputStream, file: File) {
        var fos: FileOutputStream? = null
        try {

            val buffer = ByteArray(2048)
            fos = FileOutputStream(file)
            iStream.use { input ->
                while (true){
                    val length = input.read(buffer)
                    if(length <= 0){
                        break
                    }
                    fos.write(buffer, 0, length)
                }
            }

        } catch (ex: Exception) {
        } finally {
            fos?.flush()
            fos?.close()
        }
    }

    fun Tobitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {

        val target = Bitmap.createBitmap(width, height, bitmap.config)
        val canvas = Canvas(target)
        canvas.drawBitmap(bitmap, null, Rect(0, 0, target.width, target.height), null)
        return target
    }

    //width：目标宽度，pageWidthPoint：初始宽度，pageHeightPoint：初始高度
    fun getHeight(width: Int, pageWidthPoint: Int, pageHeightPoint: Int): Int {
        val bili = width / pageWidthPoint.toDouble()
        return (pageHeightPoint * bili).toInt()
    }

    fun Tobitmap90(bitmap: Bitmap): Bitmap {
        var bitmap = bitmap
        val matrix = Matrix()
        // 设置旋转角度
        matrix.setRotate(90f)
        // 重新绘制Bitmap
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        return bitmap
    }

    //判断是否是整数
    fun isInteger(str: String): Boolean {
        val pattern = Pattern.compile("^[-\\+]?[\\d]*$")
        return pattern.matcher(str).matches()
    }
}
