package com.mawared.mawaredvansale.controller.base

import android.util.Log
import androidx.core.text.TextUtilsCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModel
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

abstract class BaseViewModel() : ViewModel() {

    fun returnDateString(isoString: String) : String{
        // 2017-09-11T01:16:13.858Z converted to below
        // Monday 4:35 PM format "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        val isLeftToRight = TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == ViewCompat.LAYOUT_DIRECTION_LTR
        val pattern: String = if(isLeftToRight) "dd-MM-yyyy" else "yyyy-MM-dd"
        val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        isoFormatter.timeZone = TimeZone.getTimeZone("UTC")
        var convertedDate = Date()
        try {
            convertedDate = isoFormatter.parse(isoString)
        }catch (e: ParseException){
            Log.d("PARSE", "Cannot parse date")
        }

        val outDateString = SimpleDateFormat(pattern, Locale.getDefault())
        return  outDateString.format(convertedDate)
    }
}