package com.mawared.mawaredvansale.controller.common

import android.content.Context
import com.mawared.mawaredvansale.R
import java.io.File

object Common {
    fun getAppPath(context: Context): String {
        val dir = File(
            android.os.Environment.getExternalStorageDirectory().toString()
                    + File.separator
                    + context.resources.getString(R.string.app_name)
                    + File.separator
//                    + "assets"
//                    + File.separator
        )

        if (!dir.exists()) {
            dir.mkdir()
        }
        //"${dir.absolutePath}/example.pdf"
        return dir.absolutePath + File.separator

    }
}