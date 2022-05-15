package com.mawared.mawaredvansale.data.db.entities.dms

import android.graphics.Bitmap
import androidx.room.PrimaryKey

data class Document(
    var fileName: String?,
    var masterType: String?,
    var base64String: String?,
    var bmp: Bitmap?,
    var isNew: String?
) {
    @PrimaryKey(autoGenerate = false)
    var doc_id:  Int = 0
}