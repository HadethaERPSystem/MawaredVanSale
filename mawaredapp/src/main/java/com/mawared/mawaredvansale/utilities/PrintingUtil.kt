package com.mawared.mawaredvansale.utilities

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Align
import android.graphics.Typeface
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.nio.file.Files
import java.util.*

object PrintingUtil {

    val PRINTER_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")


    @RequiresApi(Build.VERSION_CODES.O)
    fun printPdfToBluetoothDevice(context: Context, bluetoothAddress: String) {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            println("Bluetooth is not available on this device.")
            return
        }

        if (!bluetoothAdapter.isEnabled) {
            println("Bluetooth is not enabled.")
            return
        }

        val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice(bluetoothAddress)
        var bluetoothSocket: BluetoothSocket? = null
        var socket:BluetoothSocket?=null
        try {

            val printer = BluetoothPrinter(context, device)

            socket=  printer.connect()
//        printPdf(pdfFile, printer)
            val outputStream =socket!!.outputStream
//
            val util= PosBluetoothPrinter(device);
            val typeface = Typeface.createFromAsset(context.assets, "fonts/droid_kufi_bold.ttf")
            util.printMultiLangText(outputStream,"السلام عليكم ورحمة             الله وبركاته", Align.CENTER, 32f, typeface)

            util.feedPaper()

        } catch (e: IOException) {
            e.printStackTrace()
            println("Failed to print PDF: ${e.message}")
            // Ensure to handle exceptions appropriately or rethrow as needed
        } finally {
            try {
//            socket?.close()
            } catch (closeException: IOException) {
                closeException.printStackTrace()
            }
        }
    }

    class BluetoothPrinter(private val context: Context, private val device: BluetoothDevice) {

        private var socket: BluetoothSocket? = null

        @Throws(IOException::class)
        fun connect(): BluetoothSocket? {
            try {
                val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {

                }
                socket = device.createRfcommSocketToServiceRecord(uuid)
                socket?.connect()
                return socket
            }
            catch (e: Exception){
                e.printStackTrace()
            }
            return socket
        }

        @Throws(IOException::class)
        fun print(data: ByteArray) {
            socket?.outputStream?.write(data)
        }

        @Throws(IOException::class)
        fun close() {
            socket?.close()
        }
    }


}

