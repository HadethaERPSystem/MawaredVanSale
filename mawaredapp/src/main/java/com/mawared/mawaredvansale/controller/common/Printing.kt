package com.mawared.mawaredvansale.controller.common

import HPRTAndroidSDK.HPRTPrinterHelper
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Message
import android.util.Log
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.utilities.toast
import print.Print
import print.PublicFunction
import java.io.File
import java.io.InputStream
import java.net.URL
import java.util.concurrent.Executors

class TicketPrinting(val ctx: Context, val lines: List<Ticket>) {
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var PFun: PublicFunction? = null
    private var PAct: PublicAction? = null
    private val executorService = Executors.newSingleThreadExecutor()
    private val PRINT_SUCCEED = 1
    private val PRINT_FAILURE = 0
    //private var handler: Handler? = null

    private var PRINTER_CODE_PAGE: Int = 0
    private var PRINTER_ENCODE: String = ""

    private var BarcodeType = Print.BC_CODE128
    private var BarcodeWidth = 1
    private var BarcodeHeight = 80
    private var BarcodeHRILayout = 2

    private var handler: Handler? = null
    private var dialog: ProgressDialog? = null

    fun run() {

        PFun = PublicFunction(ctx)
        PAct = PublicAction(ctx)
        try {
            EnableBluetooth()

            handler = object : Handler() {
                override fun handleMessage(msg: Message) {
                    super.handleMessage(msg)
                    if (msg.what == PRINT_SUCCEED) {
                        dialog?.cancel()
                    } else {
                        dialog?.cancel()
                    }
                }
            }
            //val lang = Locale.getDefault().toString()
//            if (lang == "en_US") {
//                PRINTER_CODE_PAGE = App.prefs.lang_CodePage_latin!!.toInt()
//                val settingValue: String = App.prefs.lang_Encode_latin ?: "0"
//                PRINTER_ENCODE = settingValue.split(",")[1]
//            } else {
                PRINTER_CODE_PAGE = App.prefs.lang_CodePage_ar!!.toInt()
                val settingValue: String = App.prefs.lang_Encode_ar ?: "0"
                PRINTER_ENCODE = settingValue.split(",")[1]
            //}

            val toothAddress = App.prefs.bluetooth_port
            if (toothAddress != null) {
                val portOpen = Print.PortOpen(ctx, "$toothAddress")
                if (portOpen == 0) {
                    printReceipt(lines)
                } else {
                    ctx.toast("Printer Connection Failure")
                }
            } else {
                ctx.toast("Printer port not specified in application settings")
            }

        } catch (e: Exception) {
            Log.e(
                "HPRTSDKSample",
                StringBuilder("Activity_Main --> onCreate ").append(e.message).toString()
            )
        }
    }

    //EnableBluetooth
    private fun EnableBluetooth(): Boolean {
        var bRet = false
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (mBluetoothAdapter != null) {
            if (mBluetoothAdapter!!.isEnabled)
                return true
            mBluetoothAdapter!!.enable()
            try {
                Thread.sleep(500)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            if (!mBluetoothAdapter!!.isEnabled) {
                bRet = true
                Log.d("PRTLIB", "BTO_EnableBluetooth --> Open OK")
            }
        } else {
            Log.d(
                "HPRTSDKSample",
                StringBuilder("Activity_Main --> EnableBluetooth ").append("Bluetooth Adapter is null.").toString()
            )
        }
        return bRet
    }

    @Throws(Exception::class)
    private fun Barcode_BC_CODE128(barcode: String, justification: Int): Int {
        //"{BS/N:{$barcode"
        //Print.PrintBarCode(BarcodeType, "197288L")
        val bc = "{B$barcode"
        return Print.PrintBarCode(BarcodeType, bc, BarcodeWidth, BarcodeHeight, BarcodeHRILayout, 1)//justification )
    }

    private fun printReceipt(tickets: List<Ticket>) {
        dialog = ProgressDialog(ctx)
        dialog?.setMessage("Printing.....")
        dialog?.progress = 100
        dialog?.show()
        executorService.execute {
            try {
                val th : Thread = Thread(Runnable {
                    Print.Initialize()
                    //Print.setPrintResolution(203, 203)
                    //Print.SelectPageMode()
                    //Print.SetPageModePrintArea(0, 0, 640, 240);//Area (start x, start y, width, height)
                    //Print.SetPageModePrintDirection(0)
                    Print.setCodePage(PRINTER_CODE_PAGE)
                    Print.LanguageEncode = PRINTER_ENCODE
                    HPRTPrinterHelper.SetCharacterSet(PRINTER_CODE_PAGE.toByte())
                    HPRTPrinterHelper.LanguageEncode = PRINTER_ENCODE

                    // Print Line x1,y1 to x2,y2 , Line Width
                    //Print.PrintPageLine(0, 128, 1280, 128, 2)
                    //Print.setPrintResolution(203,203) set print resolution
                    //Print.SetCharacterSize(17) set character size
                    for (t in tickets){

                        when(t.type){
                            LineType.Rectangle -> {
                                Print.PrintPageRectangle(t.xPos!!, t.yPos!!, t.exPos!!, t.eyPos!!, 2)
                            }
                            LineType.Text -> {
                                if(t.xPos != null && t.yPos != null){
                                    Print.SetPageModeAbsolutePosition(t.xPos!!, t.yPos!!)
                                    Print.SetCharacterSize(0)
                                    Print.PrintText(t.text)//, t.align.ordinal, t.attribute.ordinal, t.textSize)
                                }
                                else{
                                    Print.PrintText(t.text, t.align.ordinal, t.attribute.ordinal, t.textSize)
                                }

                            }
                            LineType.Image -> {
                                try {
                                    //val bmp = BitmapFactory.decodeResource(ctx.resources, R.mipmap.ic_logo_black)
                                    val conn = URL(t.text).openConnection()
                                    conn.connect()
                                    val length = conn.contentLength
                                    if (length > 0) {
                                        val bitmapData = IntArray(length)
                                        val bitmapData2 = ByteArray(length)
                                        val `is`: InputStream = conn.getInputStream()
                                        val bmp = BitmapFactory.decodeStream(`is`)
                                        if(bmp != null){
                                            Print.PrintBitmap(bmp, 2, 1)
                                            HPRTPrinterHelper.PrintText("\r\n")
                                        }
                                    }

                                }catch (e: java.lang.Exception){
                                    e.printStackTrace()
                                }

                            }
                            LineType.Barcode -> {
                                if(t.text != null){
                                    PAct!!.BeforePrintAction()
                                    Barcode_BC_CODE128(t.text!!, t.align.ordinal)
                                    PAct!!.AfterPrintAction()
                                }
                            }
                        }
                    }

                    Print.PrintDataInPageMode()
                    //HPRTPrinterHelper.CutPaper(Print.PARTIAL_CUT_FEED.toInt(), 50)

                    handler?.sendEmptyMessage(PRINT_SUCCEED)
                })
                th.start()

            } catch (e: Exception) {
                Log.e(
                    "Print",
                    StringBuilder("Activity_Main --> PrintSampleReceipt ").append(e.message).toString()
                )
                handler?.sendEmptyMessage(PRINT_FAILURE)
            }

        }
    }

    private fun printPDF(ctx: Context,mFilename: String){
        val file = File(mFilename)
        val bitmaps = HPRTPrinterHelper.PrintPDF(ctx, file, "1", 384)
        Print.PrintBitmap(bitmaps[0], 1, 0)
    }
}