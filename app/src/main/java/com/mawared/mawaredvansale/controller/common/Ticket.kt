package com.mawared.mawaredvansale.controller.common

import android.graphics.Bitmap
import java.io.Serializable

class Ticket(
    var text: String? = null,
    var type: LineType = LineType.Text,
    var align: AlignText = AlignText.LEFT,
    var attribute: Attribute = Attribute.LARGE_FONT_NO_BOLD_NO_UNDERLINE_NO_HIGHLIGHT,
    var textSize: Int = 0,
    val bmp: Bitmap? = null
): Serializable

enum class LineType{
    Text, Image, Barcode
}

enum class AlignText{
    LEFT,
    CENTER,
    RIGHT


}

enum class Attribute(){
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