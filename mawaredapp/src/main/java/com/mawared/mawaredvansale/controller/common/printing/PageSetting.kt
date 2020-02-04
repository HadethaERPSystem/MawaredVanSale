package com.mawared.mawaredvansale.controller.common.printing

import android.graphics.Bitmap
import android.graphics.fonts.FontStyle
import com.itextpdf.text.*
import com.mawared.mawaredvansale.controller.common.LineType

class PageSetting {
    val marginRight = 0.5f
    val marginLeft = 0.5f
    val marginTop = 0.5f
    val marginBottom = 0.5f
    var page : Rectangle = PageSize.A4
}

class PageFilter(val lable: String, val value: String)

class RowHeader(
    val cellName: String,
    val fontSize: Float = 12f,
    val hasTotal: Boolean = false,
    val cellWidth: Int = 0,
    val summaryTitle: String = "Total",
    val direction: Int = 0,
    val beginOfBody: Float = 0f,
    val align: Int = Element.ALIGN_CENTER,
    val fontWeight: Int = Font.NORMAL,
    val fontName: String = "assets/fonts/arial.ttf"
)

class ReportFont(
    val font: Font,
    val fontSize: Float,
    val fontStyle: FontStyle,
    val baseColor: BaseColor,
    val align: Int
)

class TCell(
    val cellName: String,
    val fontSize: Float = 12f,
    val hasTotal: Boolean = false,
    val cellWidth: Float = 0f,
    val summaryTitle: String = "Total",
    val align: Int = 0,
    val border: Int = 0,
    val fontName: String = "assets/fonts/arial.ttf",
    val fontWeight: Int = Font.NORMAL
)
class CTable(
    val cells: HashMap<Int, TCell>
)
class HeaderFooterRow(
    val order: Int = 0,
    val rows: ArrayList<CTable>? = null,
    val text: String? = null,
    val fontSize: Float = 12F,
    val align: Int = Element.ALIGN_CENTER,
    val fontWeight: Int = Font.NORMAL,
    val fontName: String = "assets/fonts/arial.ttf",
    val cellsWidth: ArrayList<Int>? = null,
    val fontColor: BaseColor = BaseColor.BLACK,
    val bmp: Bitmap? = null,
    var xPos: Float = 0F,
    var yPos: Float = 0F
)

class RepLogo(val bmp: Bitmap? = null,
           var xPos: Float = 0F,
           var yPos: Float = 0F)

class ITextTicket(
    var order: Int = 0,
    var text: String? = null,
    var type: LineType = LineType.Text,
    var xPos: Float = 0F,
    var yPos: Float = 0F,
    var width: Float = 0F,
    var height: Float = 0F,
    var align: Int = Element.ALIGN_CENTER,
    var fontName: String? = "assets/fonts/arial.ttf",
    var color: BaseColor? = null,
    var bold: Int = Font.NORMAL,
    var size: Float = 12.0f,
    var bmp: Bitmap? = null
)