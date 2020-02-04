package com.mawared.mawaredvansale.controller.common

import com.itextpdf.text.Font
import java.io.Serializable

class ReportColumn(
    val text: String?,
    val align: Int?,
    val width: Int?,
    val fontName: String? = "assets/fonts/brandon_medium.otf",
    //val baseColor: BaseColor? = null,
    var fontWeight: Int = Font.NORMAL,
    var fontSize: Float = 12.0f,
    var styleType: String? = "L",  // T means Label , V means Value
   var boderWidth: Float = 0f
): Serializable

