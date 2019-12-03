package com.mawared.mawaredvansale.utilities


import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import java.util.*

object LocalDateTimeEx {
    /**an alternative of LocalDateTime.now(), as it requires initialization using AndroidThreeTen.init(context), which takes a bit time (loads a file)*/
    @JvmStatic
    fun getNow(): LocalDateTime = Calendar.getInstance().toLocalDateTime()
}

private fun Calendar.toLocalDateTime(): LocalDateTime = LocalDateTime.of(get(Calendar.YEAR), get(Calendar.MONTH) + 1, get(Calendar.DAY_OF_MONTH), get(Calendar.HOUR_OF_DAY), get(Calendar.MINUTE), get(Calendar.SECOND),
    get(Calendar.MILLISECOND) * 1000000)

object LocalTimeEx {
    /**an alternative of LocalDateTime.now(), as it requires initialization using AndroidThreeTen.init(context), which takes a bit time (loads a file)*/
    @JvmStatic
    fun getNow(): LocalTime = Calendar.getInstance().toLocalTime()
}



fun Calendar.toLocalTime(): LocalTime = LocalTime.of(get(Calendar.HOUR_OF_DAY), get(Calendar.MINUTE), get(Calendar.SECOND), get(Calendar.MILLISECOND) * 1000000)