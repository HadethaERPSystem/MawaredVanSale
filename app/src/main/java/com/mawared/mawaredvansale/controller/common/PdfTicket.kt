package com.mawared.mawaredvansale.controller.common

import android.graphics.Bitmap
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import java.io.Serializable

class PdfTicket(
    var text: String? = null,
    var rows: ArrayList<HashMap<Int, Column>>? = null,
    var type: LineType = LineType.Text,
    var align: Int = Element.ALIGN_CENTER,
    var fontName: String? = "assets/fonts/brandon_medium.otf",
    //var baseFont: BaseColor? = null,
    var fontWeight: Int = Font.NORMAL,
    var fontSize: Float = 12.0f,
    var width: Float = 0f,
    @Transient var bmp: Bitmap? = null,
    var styleType: String? = "L"  // T means Label , V means Value
): Serializable

class PdfHeader(
    var header: HashMap<Int, Column>? = null
): Serializable

class PdfFooter(
    var header: HashMap<Int, Column>? = null
): Serializable

class PdfBody(
    var rows: ArrayList<HashMap<Int, Column>>? = null
): Serializable

class Template(): Serializable{
    var header: ArrayList<PdfHeader>? = arrayListOf()
    var body: PdfBody? = null
    var footer: ArrayList<PdfFooter>? = arrayListOf()
}