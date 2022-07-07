package com.mawared.mawaredvansale.controller.helpers.extension

import android.annotation.SuppressLint
import android.content.Context
import android.text.format.DateFormat
import android.util.Log
import androidx.core.text.TextUtilsCompat
import androidx.core.view.ViewCompat
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.controller.helpers.LocaleHelper
import java.math.BigDecimal
import java.text.*
import java.util.*
import java.util.Locale




@SuppressLint("SimpleDateFormat")
fun String.toArray(): Array<Int> {
    val split = this.split(":")
    val hour = split[0].toInt()
    return arrayOf(if (hour >= 12) hour - 12 else hour, split[1].toInt(), if (hour >= 12) 1 else 0)
}

@SuppressLint("SimpleDateFormat")
fun String.getFormattedDate(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss")
    dateFormat.timeZone = TimeZone.getTimeZone("UTC")
    val date = dateFormat.parse(this)
    val smsTimeInMilis = date?.time ?: return ""

    val smsTime = Calendar.getInstance()
    smsTime.timeInMillis = smsTimeInMilis
    val now = Calendar.getInstance()
    val timeFormatString = "hh:mm aa"
    val dateTimeFormatString = "EEEE, MMMM d, h:mm aa"
    val yearTimeFormatString = "MMMM dd yyyy, h:mm aa"
    val timeText = DateFormat.format(timeFormatString, smsTime)
    return when {
        now[Calendar.DATE] == smsTime[Calendar.DATE] -> "Today ( $timeText )"
        now[Calendar.DATE] - smsTime[Calendar.DATE] == 1 -> "Yesterday ( $timeText )"
        now[Calendar.YEAR] == smsTime[Calendar.YEAR] -> DateFormat.format(dateTimeFormatString, smsTime).toString()
        else -> DateFormat.format(yearTimeFormatString, smsTime).toString()
    }
}

@SuppressLint("SimpleDateFormat")
fun String.removeZeros(): String {
    val decimal = BigDecimal(this.toDouble())
    return DecimalFormat("0.##").format(decimal)
}

@SuppressLint("SimpleDateFormat")
fun String.toServerDate(context: Context?): String {
    if (this.isEmpty()) return ""

    Locale.setDefault(Locale.ENGLISH)
    val dateFormat = SimpleDateFormat("MM/dd/yyyy")
    val date = dateFormat.parse(this)
    val ret = DateFormat.format("yyyy-MM-dd", date).toString()

    LocaleHelper.setLocale(context, App.prefs.systemLanguage ?: LocaleHelper.DEFAULT_LANGUAGE)

    return ret
}

@SuppressLint("SimpleDateFormat")
fun String.toClientDate(): String {
    if (this.isEmpty()) return ""

    val dateFormat = SimpleDateFormat("yyyy-MM-dd")
    val date = dateFormat.parse(this)
    return DateFormat.format("MM/dd/yyyy", date).toString()
}

@SuppressLint("SimpleDateFormat")
fun String.returnDateString() : String{
    if (this.isEmpty()) return ""
    try {
        // 2017-09-11T01:16:13.858Z converted to below
        // Monday 4:35 PM format "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        val enLang = Locale("en")
        val isLeftToRight = TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == ViewCompat.LAYOUT_DIRECTION_LTR
        val pattern: String = if(isLeftToRight) "dd-MM-yyyy" else "yyyy-MM-dd"
        val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        isoFormatter.timeZone = TimeZone.getTimeZone("UTC")
        var convertedDate = Date()
        try {
            convertedDate = isoFormatter.parse(this)!!
        }catch (e: ParseException){
            Log.d("PARSE", "Cannot parse date")
        }

        val outDateString = SimpleDateFormat(pattern, enLang)
        return  outDateString.format(convertedDate)
    }catch (e: Exception){
        Log.i("Exc", "Error in BaseViewModel returnDateString($this)")
        return ""
    }
}

fun String.toPhoneFormat(): String {
    val text = this.replace("+", "")

    if (text.length < 9)
        return text

//    val newText = text.substring(text.length - 4, text.length)
//    val newText1 = text.substring(text.length - 7, text.length - 4)
    val newText2 = text.substring(0, text.length - 9)

    return "${newText2}XX-XXXX-XXX"
}

fun String.toHours(): String {
    val times = split(":")
    var am = "AM"
    if (times.size > 1) {
        var hour = times[0].toInt()
        if (hour >= 12) {
            am = "PM"
            hour -= 12
        }
        return String.format("%02d:", hour) + times[1] + " $am"
    }
    return this
}

fun String.toServerHours(): String {
    val time = split(" ")
    val times = time[0].split(":")
    var am = "AM"
    if (time.size > 1) {
        val hour = if (time[1] == "PM") times[0].toInt() + 12
        else times[0].toInt()

        return String.format("%02d:", hour) + times[1]
    }
    return this
}

@SuppressLint("SimpleDateFormat")
fun String.getTimeLeft(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss")
    dateFormat.timeZone = TimeZone.getTimeZone("UTC")
    val date = dateFormat.parse(this)

    val calendar = Calendar.getInstance()
    date?.let { calendar.time = date }
    calendar.add(Calendar.DAY_OF_YEAR, 2)

    val endDate = calendar.time
    val nowDate = Date()

    var different = endDate.time - nowDate.time

    val secondsInMilli = 1000
    val minutesInMilli = secondsInMilli * 60
    val hoursInMilli = minutesInMilli * 60

    val elapsedHours = different / hoursInMilli
    different %= hoursInMilli

    val elapsedMinutes = different / minutesInMilli

    return String.format("%02d:%02d left", elapsedHours, elapsedMinutes)
}

fun String.toTimeHours(): Int {
    val split = split(":")
    if (split.size > 1) {
        return split[0].toInt()
    }
    return 0
}

fun Double?.toFormatNumber() : String?{
    try {
        val enLang = Locale("en")
        val symbolsEN_US: DecimalFormatSymbols = DecimalFormatSymbols.getInstance(enLang)
        val value = this
        val dec = DecimalFormat("#,###.##", symbolsEN_US)
        if(value == null) return "0"
        val formatedNumber = dec.format(value)// NumberFormat.getInstance(enLang).format(value)

        return formatedNumber
    }catch (e: Exception){
        e.printStackTrace()
    }
    return "${this}"
}