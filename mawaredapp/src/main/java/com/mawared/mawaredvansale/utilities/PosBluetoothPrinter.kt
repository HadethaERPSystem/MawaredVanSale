package com.mawared.mawaredvansale.utilities

import android.bluetooth.BluetoothDevice
import android.graphics.*
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import java.io.IOException
import java.io.OutputStream
import java.util.ArrayList

class PosBluetoothPrinter(val device: BluetoothDevice) {

    private var btOutputStream: OutputStream? = null

    fun printText(text: String): Boolean {
        try {
            btOutputStream!!.write(encodeNonAscii(text).toByteArray())
            return true
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
    }

    fun printUnicode(data: ByteArray?): Boolean {
        try {
            btOutputStream?.write(data)
            return true
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
    }

    fun addNewLine(): Boolean {
        return printUnicode(NEW_LINE)
    }

    fun setLineSpacing(lineSpacing: Int) {
        val cmd = byteArrayOf(0x1B, 0x33, lineSpacing.toByte())
        printUnicode(cmd)
    }

    fun setBold(bold: Boolean) {
        val cmd = byteArrayOf(0x1B, 0x45, if (bold) 1.toByte() else 0)
        printUnicode(cmd)
    }

    fun feedPaper() {
        addNewLine()
        addNewLine()
        addNewLine()
        addNewLine()
    }

    fun printImage(bitmap: Bitmap): Boolean {
        val command = decodeBitmap(bitmap)
        return printUnicode(command)
    }


    @JvmOverloads
    fun printMultiLangText(outputStream: OutputStream, stringData: String, align: Paint.Align,
        textSize: Float, typeface: Typeface? = null): Boolean {
        btOutputStream=outputStream
        return printImage(getMultiLangTextAsImage(stringData, align, textSize, typeface))
    }

    @Throws(WriterException::class)
    fun printBarCode(message: String?, format: BarcodeFormat?, width: Int, height: Int): Boolean {
        return printImage(createBarCode(message, format, width, height))
    }

    fun getMultiLangTextAsImage(text: String, align: Paint.Align, textSize: Float, typeface: Typeface?): Bitmap {
        val paint = Paint()

        paint.isAntiAlias = true
        paint.color = Color.BLACK
        paint.textSize = textSize
        if (typeface != null) paint.setTypeface(typeface)
        // A real printlabel width (pixel)
        val xWidth = 385f

        // A height per text line (pixel)
        var xHeight = textSize + 8

        // it can be changed if the align's value is CENTER or RIGHT
        var xPos = 6f

        // If the original string data's length is over the width of print label,
        // or '\n' character included,
        // it will be increased per line gerneating.
        var yPos = 27f

        // If the original string data's length is over the width of print label,
        // or '\n' character included,
        // each lines splitted from the original string are added in this list
        // 'PrintData' class has 3 members, x, y, and splitted string data.
        val printDataList: MutableList<PrintData> = ArrayList()

        // if '\n' character included in the original string
        val tmpSplitList = text.split("\\n".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        for (i in 0..tmpSplitList.size - 1) {
            val tmpString = tmpSplitList[i]

            // calculate a width in each split string item.
            var widthOfString = paint.measureText(tmpString)

            // If the each split string item's length is over the width of print label,
            if (widthOfString > xWidth) {
                var lastString = tmpString
                while (!lastString.isEmpty()) {
                    var tmpSubString = ""

                    // retrieve repeatedly until each split string item's length is
                    // under the width of print label
                    while (widthOfString > xWidth) {
                        tmpSubString =
                            if (tmpSubString.isEmpty()) lastString.substring(
                                0,
                                lastString.length - 1
                            )
                            else tmpSubString.substring(0, tmpSubString.length - 1)

                        widthOfString = paint.measureText(tmpSubString)
                    }

                    // this each split string item is finally done.
                    if (tmpSubString.isEmpty()) {
                        // this last string to print is need to adjust align
                        if (align == Paint.Align.CENTER) {
                            if (widthOfString < xWidth) {
                                xPos = ((xWidth - widthOfString) / 2)
                            }
                        } else if (align == Paint.Align.RIGHT) {
                            if (widthOfString < xWidth) {
                                xPos = xWidth - widthOfString
                            }
                        }
                        printDataList.add(PrintData(xPos, yPos, lastString, paint))
                        lastString = ""
                    } else {
                        // When this logic is reached out here, it means,
                        // it's not necessary to calculate the x position
                        // 'cause this string line's width is almost the same
                        // with the width of print label
                        printDataList.add(PrintData(0f, yPos, tmpSubString, paint))

                        // It means line is needed to increase
                        yPos += 27f
                        xHeight += 30f

                        lastString = lastString.replaceFirst(tmpSubString.toRegex(), "")
                        widthOfString = paint.measureText(lastString)
                    }
                }
            } else {
                // This split string item's length is
                // under the width of print label already at first.
                if (align == Paint.Align.CENTER) {
                    if (widthOfString < xWidth) {
                        xPos = ((xWidth - widthOfString) / 2)
                    }
                } else if (align == Paint.Align.RIGHT) {
                    if (widthOfString < xWidth) {
                        xPos = xWidth - widthOfString
                    }
                }
                printDataList.add(PrintData(xPos, yPos, tmpString, paint))
            }

            if (i != tmpSplitList.size - 1) {
                // It means the line is needed to increase
                yPos += 27f
                xHeight += 30f
            }
        }

        // If you want to print the text bold
        //paint.setTypeface(Typeface.create(null as String?, Typeface.BOLD))

        // create bitmap by calculated width and height as upper.
        val bm = Bitmap.createBitmap(xWidth.toInt(), xHeight.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bm)
        canvas.drawColor(Color.WHITE)

        for (tmpItem in printDataList) canvas.drawText(
            tmpItem.text,
            tmpItem.xPos,
            tmpItem.yPos,
            paint
        )


        return bm
    }

    internal class PrintData(var xPos: Float, var yPos: Float, var text: String, var paint: Paint) {
        fun getxPos(): Float {
            return xPos
        }

        fun setxPos(xPos: Float) {
            this.xPos = xPos
        }

        fun getyPos(): Float {
            return yPos
        }

        fun setyPos(yPos: Float) {
            this.yPos = yPos
        }

        fun getmPaint(): Paint{
            return  paint
        }
        fun setmPaint(mPaint: Paint){
            this.paint = mPaint
        }
    }

    @Throws(WriterException::class)
    fun createBarCode(message: String?, format: BarcodeFormat?, width: Int, height: Int): Bitmap {
        val bitMatrix = MultiFormatWriter().encode(message, format, width, height)

        val matrixWidth = bitMatrix.width
        val matrixHeight = bitMatrix.height
        val pixels = IntArray(matrixWidth * matrixHeight)
        for (i in 0 until matrixHeight) {
            for (j in 0 until matrixWidth) {
                if (bitMatrix[j, i]) {
                    pixels[i * matrixWidth + j] = -0x1000000
                } else {
                    pixels[i * matrixWidth + j] = -0x1
                }
            }
        }
        val bitmap = Bitmap.createBitmap(matrixWidth, matrixHeight, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, matrixWidth, 0, 0, matrixWidth, matrixHeight)
        return bitmap
    }


    companion object {
        const val ALIGN_CENTER: Int = 100
        const val ALIGN_RIGHT: Int = 101
        const val ALIGN_LEFT: Int = 102

        private val NEW_LINE = byteArrayOf(10)
        private val ESC_ALIGN_CENTER = byteArrayOf(0x1b, 'a'.toByte(), 0x01)
        private val ESC_ALIGN_RIGHT = byteArrayOf(0x1b, 'a'.toByte(), 0x02)
        private val ESC_ALIGN_LEFT = byteArrayOf(0x1b, 'a'.toByte(), 0x00)

        private fun encodeNonAscii(text: String): String {
            return text.replace('á', 'a')
                .replace('č', 'c')
                .replace('ď', 'd')
                .replace('é', 'e')
                .replace('ě', 'e')
                .replace('í', 'i')
                .replace('ň', 'n')
                .replace('ó', 'o')
                .replace('ř', 'r')
                .replace('š', 's')
                .replace('ť', 't')
                .replace('ú', 'u')
                .replace('ů', 'u')
                .replace('ý', 'y')
                .replace('ž', 'z')
                .replace('Á', 'A')
                .replace('Č', 'C')
                .replace('Ď', 'D')
                .replace('É', 'E')
                .replace('Ě', 'E')
                .replace('Í', 'I')
                .replace('Ň', 'N')
                .replace('Ó', 'O')
                .replace('Ř', 'R')
                .replace('Š', 'S')
                .replace('Ť', 'T')
                .replace('Ú', 'U')
                .replace('Ů', 'U')
                .replace('Ý', 'Y')
                .replace('Ž', 'Z')
        }

        fun decodeBitmap(bmp: Bitmap): ByteArray? {
            val bmpWidth = bmp.width
            val bmpHeight = bmp.height

            val list: MutableList<String> = ArrayList()
            var sb: StringBuffer
            val zeroCount = bmpWidth % 8
            var zeroStr = ""
            if (zeroCount > 0) {
                for (i in 0 until (8 - zeroCount)) zeroStr = zeroStr + "0"
            }

            for (i in 0 until bmpHeight) {
                sb = StringBuffer()
                for (j in 0 until bmpWidth) {
                    val color = bmp.getPixel(j, i)
                    val r = (color shr 16) and 0xff
                    val g = (color shr 8) and 0xff
                    val b = color and 0xff
                    if (r > 160 && g > 160 && b > 160) sb.append("0")
                    else sb.append("1")
                }
                if (zeroCount > 0) sb.append(zeroStr)
                list.add(sb.toString())
            }

            val bmpHexList = binaryListToHexStringList(list)
            val commandHexString = "1D763000"
            var widthHexString = Integer
                .toHexString(if (bmpWidth % 8 == 0) bmpWidth / 8 else (bmpWidth / 8 + 1))
            if (widthHexString.length > 2) {
                return null
            } else if (widthHexString.length == 1) {
                widthHexString = "0$widthHexString"
            }
            widthHexString = widthHexString + "00"

            var heightHexString = Integer.toHexString(bmpHeight)
            if (heightHexString.length > 2) {
                return null
            } else if (heightHexString.length == 1) {
                heightHexString = "0$heightHexString"
            }
            heightHexString = heightHexString + "00"

            val commandList: MutableList<String> = ArrayList()
            commandList.add(commandHexString + widthHexString + heightHexString)
            commandList.addAll(bmpHexList)

            return hexList2Byte(commandList)
        }

        private fun binaryListToHexStringList(list: kotlin.collections.List<String>): kotlin.collections.List<String> {
            val hexList: MutableList<String> = ArrayList()
            for (binaryStr in list) {
                val sb = StringBuilder()
                var i = 0
                while (i < binaryStr.length) {
                    val str = binaryStr.substring(i, i + 8)
                    val hexString = strToHexString(str)
                    sb.append(hexString)
                    i += 8
                }
                hexList.add(sb.toString())
            }
            return hexList
        }

        private const val hexStr = "0123456789ABCDEF"
        private val binaryArray = arrayOf(
            "0000", "0001", "0010", "0011",
            "0100", "0101", "0110", "0111", "1000", "1001", "1010", "1011",
            "1100", "1101", "1110", "1111"
        )

        private fun strToHexString(binaryStr: String): String {
            var hex = ""
            val f4 = binaryStr.substring(0, 4)
            val b4 = binaryStr.substring(4, 8)
            for (i in binaryArray.indices) {
                if (f4 == binaryArray[i]) hex += hexStr.substring(i, i + 1)
            }
            for (i in binaryArray.indices) {
                if (b4 == binaryArray[i]) hex += hexStr.substring(i, i + 1)
            }

            return hex
        }

        private fun hexList2Byte(list: List<String>): ByteArray {
            val commandList: MutableList<ByteArray?> = ArrayList()
            for (hexStr in list) commandList.add(hexStringToBytes(hexStr))
            return sysCopy(commandList)
        }

        private fun hexStringToBytes(hexString: String): ByteArray? {
            var hexString: String? = hexString
            if (hexString == null || hexString == "") return null
            hexString = hexString.toUpperCase()
            val length = hexString.length / 2
            val hexChars = hexString.toCharArray()
            val d = ByteArray(length)
            for (i in 0 until length) {
                val pos = i * 2
                d[i] = (charToByte(hexChars[pos]).toInt() shl 4 or charToByte(
                    hexChars[pos + 1]
                ).toInt()).toByte()
            }
            return d
        }

        private fun sysCopy(srcArrays: List<ByteArray?>): ByteArray {
            var len = 0
            for (srcArray in srcArrays) {
                len += srcArray!!.size
            }
            val destArray = ByteArray(len)
            var destLen = 0
            for (srcArray in srcArrays) {
                System.arraycopy(srcArray, 0, destArray, destLen, srcArray!!.size)
                destLen += srcArray.size
            }
            return destArray
        }

        private fun charToByte(c: Char): Byte {
            return "0123456789ABCDEF".indexOf(c).toByte()
        }
    }
}