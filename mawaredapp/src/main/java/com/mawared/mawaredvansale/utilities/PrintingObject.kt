package com.mawared.mawaredvansale.utilities
import android.content.Context
import android.graphics.*
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.mawared.mawaredvansale.controller.common.PrinterCommands
import com.mawared.mawaredvansale.controller.common.printing.BluetoothPrinter
import com.mawared.mawaredvansale.utils.Utils
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream

class PrintingObject(private val context: Context) {

    private var btOutputStream: OutputStream? = null

    fun print(bitmap: Bitmap){
        try {

            val printer = BluetoothPrinter(context)
            val socket = printer.connect()
            btOutputStream = socket?.outputStream
            printBitmap(bitmap)
            feedPaper()
            socket?.close()
        }catch (e: Exception){
            e.printStackTrace()
        }
        finally {
            btOutputStream?.close()
        }
    }

    private fun printUnicode(data: ByteArray?): Boolean {
        try {
            btOutputStream?.write(data)
            return true
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
    }

    private fun printImage(bitmap: Bitmap): Boolean {
        val command = Utils.decodeBitmap(bitmap)
        //val command = bitmapToByteArray(bitmap)
        return printUnicode(command)
    }

    private fun addNewLine(): Boolean {
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

    private fun feedPaper() {
        addNewLine()
        addNewLine()
        addNewLine()
        addNewLine()
    }

    @Throws(WriterException::class)
    fun printBarCode(message: String?, format: BarcodeFormat?, width: Int, height: Int): Boolean {
        return printImage(createBarCode(message, format, width, height))
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

    fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

    private fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray {
        val width = bitmap.width
        val height = bitmap.height
        val monochromeBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(monochromeBitmap)
        val paint = Paint()
        paint.isAntiAlias = true
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        val byteArrayOutputStream = ByteArrayOutputStream()
        monochromeBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

    fun printBitmap(bitmap: Bitmap) {

        val command = convertBitmapToEscPos(bitmap)

        try {
            //val command: ByteArray = Utils.decodeBitmap(bitmap)
            //btOutputStream?.write(PrinterCommands.ESC_ALIGN_CENTER)
            btOutputStream?.write(command)
            btOutputStream?.flush()

        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                //btOutputStream?.close()

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun decodeBitmap1(bitmap: Bitmap): ByteArray {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val bytes = ByteArrayOutputStream()
        val command = byteArrayOf(0x1B, 0x33, 0x00) // ESC 3 n

        bytes.write(command)
        for (y in 0 until height step 24) {
            bytes.write(byteArrayOf(0x1B, 0x2A, 0x21, (width % 256).toByte(), (width / 256).toByte())) // ESC * m nL nH
            for (x in 0 until width) {
                for (k in 0..2) {
                    var slice = 0
                    for (b in 0..7) {
                        val yy = y + k * 8 + b
                        if (yy >= height) continue
                        val pixel = pixels[yy * width + x]
                        val luminance = (0.299 * (pixel shr 16 and 0xFF) + 0.587 * (pixel shr 8 and 0xFF) + 0.114 * (pixel and 0xFF)).toInt()
                        slice = slice or (if (luminance < 128) 1 shl (7 - b) else 0)
                    }
                    bytes.write(slice)
                }
            }
            bytes.write(0x0A) // New line
        }
        return bytes.toByteArray()
    }

    fun convertBitmapToMonochrome(bitmap: Bitmap): ByteArray {
        val width = bitmap.width
        val height = bitmap.height
        val monochromeBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val red = (pixel shr 16) and 0xFF
            val green = (pixel shr 8) and 0xFF
            val blue = pixel and 0xFF
            val gray = (0.299 * red + 0.587 * green + 0.114 * blue).toInt()
            val monochromePixel = if (gray < 128) 0x000000 else 0xFFFFFF
            pixels[i] = monochromePixel
        }
        monochromeBitmap.setPixels(pixels, 0, width, 0, 0, width, height)

        val byteArrayOutputStream = ByteArrayOutputStream()
        val command = byteArrayOf(0x1B, 0x33, 0x00)
        byteArrayOutputStream.write(command)
        for (y in 0 until height step 24) {
            byteArrayOutputStream.write(byteArrayOf(0x1B, 0x2A, 0x21, (width % 256).toByte(), (width / 256).toByte()))
            for (x in 0 until width) {
                for (k in 0..2) {
                    var slice = 0
                    for (b in 0..7) {
                        val yy = y + k * 8 + b
                        if (yy >= height) continue
                        val pixel = monochromeBitmap.getPixel(x, yy)
                        val monochromePixel = if (pixel == 0xFF000000.toInt()) 1 shl (7 - b) else 0
                        slice = slice or monochromePixel
                    }
                    byteArrayOutputStream.write(slice)
                }
            }
            byteArrayOutputStream.write(0x0A)
        }
        return byteArrayOutputStream.toByteArray()
    }

    fun convertBitmapToEscPos(bitmap: Bitmap): ByteArray {
        val width = bitmap.width
        val height = bitmap.height
        val threshold = 127
        val baos = ByteArrayOutputStream()
        baos.write(byteArrayOf(0x1B, 0x40)) // Initialize the printer

        // Set line spacing to 24 dots
        baos.write(byteArrayOf(0x1B, 0x33, 24))

        for (y in 0 until height step 24) {
            baos.write(byteArrayOf(0x1B, 0x2A, 33, (width % 256).toByte(), (width / 256).toByte())) // Set print mode to 24-dot

            for (x in 0 until width) {
                for (k in 0 until 3) {
                    var slice = 0
                    for (b in 0 until 8) {
                        val yy = y + k * 8 + b
                        if (yy < height) {
                            val pixel = bitmap.getPixel(x, yy)
                            val gray = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3
                            if (gray < threshold) {
                                slice = slice or (1 shl (7 - b))
                            }
                        }
                    }
                    baos.write(slice)
                }
            }
            baos.write(byteArrayOf(0x0A)) // Line feed
        }
        baos.write(byteArrayOf(0x1B, 0x64, 4)) // Feed four lines
        baos.write(byteArrayOf(0x1B, 0x69)) // Cut the paper

        return baos.toByteArray()
    }

    fun convertBitmapToByteArray1(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
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

            val list: MutableList<String> = java.util.ArrayList()
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
            if (heightHexString.length > 3) {
                return null
            } else if (heightHexString.length == 1) {
                heightHexString = "0$heightHexString"
            }
            heightHexString = heightHexString + "00"

            val commandList: MutableList<String> = java.util.ArrayList()
            commandList.add(commandHexString + widthHexString + heightHexString)
            commandList.addAll(bmpHexList)

            return hexList2Byte(commandList)
        }

        private fun binaryListToHexStringList(list: kotlin.collections.List<String>): kotlin.collections.List<String> {
            val hexList: MutableList<String> = java.util.ArrayList()
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
            val commandList: MutableList<ByteArray?> = java.util.ArrayList()
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