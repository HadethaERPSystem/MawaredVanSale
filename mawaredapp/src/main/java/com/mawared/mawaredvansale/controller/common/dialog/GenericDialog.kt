package com.mawared.mawaredvansale.controller.common.dialog

import android.content.Context
import androidx.appcompat.app.AlertDialog

object GenericDialog {
    fun <T> showDialog(context: Context, title: String, msg: String, baseEo: T, doRun: (obj: T) -> Unit) {
        AlertDialog.Builder(context).apply {
            setTitle(title)
            setMessage(msg)
            setPositiveButton("OK") { _, _ ->
                doRun(baseEo)
            }

            setNegativeButton("Cancel") { _, _ ->
                //pass
            }
        }.create().show()
    }
}