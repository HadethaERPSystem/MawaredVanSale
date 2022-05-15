package com.mawared.mawaredvansale.utilities

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MediaHelper {
    var fileName = ""
    var fileUri = Uri.parse("")
    val RC_CAMERA = 100

    fun getMyFileName() : String{
        return this.fileName
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

    fun getOutputMediaFileUri() : Uri{
        val enLang = Locale("en")
        val timeStamp = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", enLang).format(Date())
        this.fileName = "MVS_${timeStamp}.jpg"
        this.fileUri = Uri.fromFile(getOutputMediaFile())
        return this.fileUri
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