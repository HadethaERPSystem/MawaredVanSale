package com.mawared.mawaredvansale.controller.common

import android.graphics.Bitmap
import java.io.Serializable

class Ticket(
    var text: String? = null,
    var type: LineType = LineType.Text,
    var align: AlignText = AlignText.LEFT,
    var attribute: Attribute = Attribute.LARGE_FONT_NO_BOLD_NO_UNDERLINE_NO_HIGHLIGHT,
    var textSize: Int = 0,
    val bmp: Bitmap? = null,
    var xPos: Int? = null,
    var yPos: Int? = null,
    var exPos: Int? = null,
    var eyPos: Int? = null
): Serializable

enum class LineType{
    Text, Image, Barcode, Column, LineSeparator, NewLine, Line, Rectangle
}

 enum class AlignText{
    LEFT,
    CENTER,
    RIGHT


}

class SunmiTicket(
    var type: LineType = LineType.Text,
    var cols : Array<String>?,
    var width: IntArray?,
    var align: IntArray?,
    var isBold: Boolean,
    var text: String?,
    var textAlign: Int?,
    var fontSize: Int? = 12,
    var bmp: Bitmap?
)

enum class Attribute {
    LARGE_FONT_NO_BOLD_NO_UNDERLINE_NO_HIGHLIGHT,
    SMALL_FONT_NO_BOLD_NO_UNDERLINE_NO_HIGHLIGHT,
    LARGE_FONT_BOLD_NO_UNDERLINE_NO_HIGHLIGHT,
    SMALL_FONT_BOLD_NO_UNDERLINE_NO_HIGHLIGHT,
    LARGE_FONT_NO_BOLD_UNDERLINE_NO_HIGHLIGHT,
    SMALL_FONT_NO_BOLD_UNDERLINE_NO_HIGHLIGHT,
    LARGE_FONT_BOLD_UNDERLINE_NO_HIGHLIGHT,
    SMALL_FONT_BOLD_UNDERLINE_NO_HIGHLIGHT,
    LARGE_FONT_NO_BOLD_NO_UNDERLINE_HIGHLIGHT,
    SMALL_FONT_NO_BOLD_NO_UNDERLINE_HIGHLIGHT,
    LARGE_FONT_BOLD_NO_UNDERLINE_HIGHLIGHT,
    SMALL_FONT_BOLD_NO_UNDERLINE_HIGHLIGHT,
    LARGE_FONT_NO_BOLD_UNDERLINE_HIGHLIGHT,
    SMALL_FONT_NO_BOLD_UNDERLINE_HIGHLIGHT,
    LARGE_FONT_BOLD_UNDERLINE_HIGHLIGHT,
    SMALL_FONT_BOLD_UNDERLINE_HIGHLIGHT;

}