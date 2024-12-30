package com.mawared.mawaredvansale.utilities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MediaHelper {
    var fileName = ""
    var fileUri = Uri.parse("")
    val RC_CAMERA = 100

    fun getMyFileName(prefix: String = "MVS") : String{
        val enLang = Locale("en")
        val timeStamp = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", enLang).format(Date())
        var fn = "${prefix}_${timeStamp}.jpg"
        return fn
    }

    fun getRcCamera() : Int{
        return this.RC_CAMERA
    }

    fun getOutputMediaFile() : File?{
        val mediaStorageDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "mawaredVanSale")
        if(!mediaStorageDir.exists())
            if(!mediaStorageDir.mkdirs()){
                Log.e("mkdir", "")
            }
        val mediaFile = File(mediaStorageDir.path+File.separator + "${this.fileName}")
        return  mediaFile
    }

    fun getOutputMediaFileUri(prefix: String = "MVS") : Uri{
        val enLang = Locale("en")
        val timeStamp = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", enLang).format(Date())
        this.fileName = "${prefix}_${timeStamp}.jpg"
        this.fileUri = Uri.fromFile(getOutputMediaFile())
        return this.fileUri
    }

    fun getOutputMediaFileUri1(prefix: String, ctx: Context): Uri {
        val enLang = Locale("en")
        val mediaDir = File(ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "com.mawared.mawaredvansale")
        if (!mediaDir.exists()) {
            if (!mediaDir.mkdirs()) {
                Log.e("FileCreation", "Failed to create directory: ${mediaDir.absolutePath}")
                throw IOException("Failed to create directory")
            }
        }
        val timeStamp = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", enLang).format(Date())
        val file = File(mediaDir, "${prefix}_${timeStamp}.jpg")
        return FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", file)
    }

    fun getOutputMediaFileUri121(prefix: String, ctx: Context): Uri {
        val enLang = Locale("en")
        val mediaDir = File(ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "com.mawared.mawaredvansale")

        if (!mediaDir.exists() && !mediaDir.mkdirs()) {
            Log.e("FileCreation", "Failed to create directory: ${mediaDir.absolutePath}")
            throw IOException("Failed to create directory")
        }

        val timeStamp = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", enLang).format(Date())
        val file = File(mediaDir, "${prefix}_${timeStamp}.jpg")

        Log.d("FileCreation", "File path: ${file.path}")

        return FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", file)
    }

    fun getOutputMediaFileUri12(prefix: String, ctx: Context): Uri {
        val enLang = Locale("en")
        val mediaDir = File(ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "com.mawared.mawaredvansale")

        if (!mediaDir.exists() && !mediaDir.mkdirs()) {
            Log.e("FileCreation", "Failed to create directory: ${mediaDir.absolutePath}")
            throw IOException("Failed to create directory")
        }

        val timeStamp = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", enLang).format(Date())
        val file = File(mediaDir, "${prefix}_${timeStamp}.jpg")

        Log.d("FileCreation", "Generated file path: ${file.absolutePath}")

        return FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", file)
    }

    fun bitmapToString(bmp : Bitmap) : String{
        val outputStream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    fun getBitmapToString(imv: ImageView, uri: Uri) : String {
        var bmp = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        bmp = BitmapFactory.decodeFile(this.fileUri.path)
        val dim = 720
        if(bmp.height > bmp.width){
            bmp = Bitmap.createScaledBitmap(bmp, (bmp.width*dim).div(bmp.height), dim, true)
        }else{
            bmp = Bitmap.createScaledBitmap(bmp, dim ,(bmp.height*dim).div(bmp.width), true)
        }

        imv.setImageBitmap(bmp)
        return  bitmapToString(bmp)
    }


}