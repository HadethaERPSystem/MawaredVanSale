package com.mawared.mawaredvansale.controller.common.printing

import android.graphics.fonts.FontStyle
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Font
import com.itextpdf.text.PageSize
import com.itextpdf.text.Rectangle

class PageSetting {
    val marginRight = 0.5f
    val marginLeft = 0.5f
    val marginTop = 0.5f
    val marginBottom = 0.5f
    val page : Rectangle = PageSize.A4
}

class PageFilter(val lable: String, val value: String)

class RowHeader(
    val cellName: String,
    val fontSize: Float = 12f,
    val hasTotal: Boolean = false,
    val cellWidth: Float = 0f,
    val summaryTitle: String = "Total",
    val direction: Int = 0,
    val beginOfBody: Float = 0f
)

class ReportFont(
    val font: Font,
    val fontSize: Float,
    val fontStyle: FontStyle,
    val baseColor: BaseColor,
    val align: Int
)
