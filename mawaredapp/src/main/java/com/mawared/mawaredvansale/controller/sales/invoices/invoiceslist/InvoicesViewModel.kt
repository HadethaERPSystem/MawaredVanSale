package com.mawared.mawaredvansale.controller.sales.invoices.invoiceslist

import HPRTAndroidSDK.HPRTPrinterHelper
import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.pdf.PdfContentByte
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.base.BaseViewModel
import com.mawared.mawaredvansale.controller.common.GenerateTicket
import com.mawared.mawaredvansale.controller.common.LineType
import com.mawared.mawaredvansale.controller.common.TicketPrinting
import com.mawared.mawaredvansale.controller.common.printing.*
import com.mawared.mawaredvansale.data.db.entities.sales.Sale
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.services.repositories.invoices.IInvoiceRepository
import com.mawared.mawaredvansale.utilities.Coroutines
import com.mawared.mawaredvansale.utilities.ImageLoader
import com.mawared.mawaredvansale.utilities.PrintingObject
import com.mawared.mawaredvansale.utilities.URL_LOGO
import com.mawared.mawaredvansale.utils.SunmiPrintHelper
import org.threeten.bp.LocalTime
import print.Print
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.text.DecimalFormat
import java.util.*


class InvoicesViewModel(private val repository: IInvoiceRepository) : BaseViewModel() {

    private val _sm_id: Int =
        if (App.prefs.savedSalesman?.sm_user_id != null) App.prefs.savedSalesman!!.sm_user_id!! else 0
    var ctx: Context? = null
    var activity: AppCompatActivity? = null

    var msgListener: IMessageListener? = null
    var term: String? = ""
    var isPrint = false

    var errorMessage: MutableLiveData<String> = MutableLiveData()

    fun loadData(list: MutableList<Sale>, term: String, pageCount: Int, loadMore: (List<Sale>?, Int) -> Unit){
        try {
            Coroutines.ioThenMain({
                val tmp = repository.invoices_OnPages(_sm_id, term, pageCount)
                if(tmp != null){
                    list.addAll(tmp)
                }
            }, {loadMore(list, pageCount)})
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun loadData(id: Int , success:(Sale?) -> Unit){
        try {
            var obj: Sale? = null
            Coroutines.ioThenMain({
                val tmp = repository.loadInvoice(id)
                obj= tmp
            }, { success(obj)})
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    val networkState: LiveData<NetworkState> by lazy {
        repository.networkState
    }

    private val _sl_Id: MutableLiveData<Int> = MutableLiveData()
    val baseEo: LiveData<Sale> = Transformations
        .switchMap(_sl_Id) {
            repository.getInvoice(it)
        }

    private val _sl_Id_for_delete: MutableLiveData<Int> = MutableLiveData()
    val deleteRecord: LiveData<String> = Transformations
        .switchMap(_sl_Id_for_delete) {
            repository.delete(it)
        }

    fun find(id: Int) {
        if (_sl_Id.value == id) {
            return
        }
        _sl_Id.value = id
    }

    fun confirmDelete(baseEo: Sale) {
        _sl_Id_for_delete.value = baseEo.sl_Id
    }


    fun onPrint(sl_Id: Int) {
        isPrint = true
        msgListener?.onStarted()
        if (sl_Id != _sl_Id.value) {
            find(sl_Id)
            return
        }

        //onPrintTicket(baseEo.value!!)
    }

    fun onPrintTicket(entityEo: Sale) {

        if (App.prefs.printing_type == "R") {
            //PrintReciept(entityEo)
                entityEo.sl_salesman_phone = App.prefs.savedSalesman?.sm_phone_no ?: ""
            try {
                val lang = Locale.getDefault().toString().toLowerCase()
                if(App.prefs.printer == "Sunmi"){
                    var bmp : Bitmap? = null
                    ImageLoader().LoadImageFromUrl(URL_LOGO + "co_black_logo.png") {
                        bmp = it
                        val tickets = GenerateTicket(ctx!!, lang).createSunmiTicket(
                            entityEo,
                            bmp,
                            "Mawared Vansale\nAL-HADETHA FRO SOFTWATE & AUTOMATION",
                            null,
                            null
                        )
                        SunmiPrintHelper.getInstance().printReceipt(ctx, tickets)
                        msgListener?.onSuccess("Print Successfully")
                    }
                } else {

                    val tickets = GenerateTicket(ctx!!, lang).create(entityEo, URL_LOGO + "co_black_logo.png", "Mawared Vansale\nAL-HADETHA FRO SOFTWATE & AUTOMATION", null, null)

                    TicketPrinting(ctx!!, tickets).run()
                    msgListener?.onSuccess("Print Successfully")

                }
            } catch (e: Exception) {
                msgListener?.onFailure("Error Exception ${e.message}")
                e.printStackTrace()
            }

        } else {
            PrintInvoice(entityEo)
        }
        isPrint = false
    }

    private fun  PrintReciept(entityEo: Sale){
        val config = ctx!!.resources.configuration
        val isRTL = config.layoutDirection != View.LAYOUT_DIRECTION_LTR
        val lang = Locale.getDefault().toString()
        var bmp: Bitmap? =
            null // BitmapFactory.decodeResource(ctx!!.resources, R.drawable.ic_logo_black)

        val mngr: AssetManager = ctx!!.assets
        var `is`: InputStream? = null
        try {
           // `is` = mngr.open("images/co_logo.bmp")
           // bmp = BitmapFactory.decodeStream(`is`)
        } catch (e1: IOException) {
            e1.printStackTrace()
        }
        val fontNameEn = "assets/fonts/arial.ttf"
        val fontNameAr = "assets/fonts/arial.ttf"// "assets/fonts/droid_kufi_regular.ttf"
        try {

            val imgLogo = RepLogo(bmp, 10F, 800F)
            val header: ArrayList<HeaderFooterRow> = arrayListOf()
            var tbl: HashMap<Int, TCell> = hashMapOf()
            val rws: ArrayList<CTable> = arrayListOf()

            header.add(HeaderFooterRow(0,null,"Iraqi International For Food Industries", 14F, Element.ALIGN_CENTER, Font.NORMAL, fontNameEn ))
            header.add(HeaderFooterRow(1,null,"${entityEo.sl_org_name}",14F, Element.ALIGN_CENTER,Font.NORMAL, fontNameEn))

            header.add(HeaderFooterRow( 3,null,"الشركة الوطنية للصناعات الغذائية", 14F, Element.ALIGN_CENTER,  Font.NORMAL, fontNameAr))
            header.add(HeaderFooterRow(4,null,"",14F,Element.ALIGN_CENTER,Font.NORMAL, fontNameEn))
            header.add(HeaderFooterRow(5,null,"",14F,Element.ALIGN_CENTER, Font.NORMAL, fontNameEn))

            tbl.put(0, TCell(ctx!!.resources!!.getString(R.string.rpt_invoice_no),9F,false,15F,"", Element.ALIGN_RIGHT,0))
            tbl.put(1, TCell( entityEo.sl_refNo!!,9F,false,30F,"", com.itextpdf.text.Element.ALIGN_RIGHT,0))

            tbl.put(2, TCell("", 9F, false, 9f, "", com.itextpdf.text.Element.ALIGN_CENTER, 0))
            tbl.put(3, TCell("", 9f, false, 30F, "", Element.ALIGN_RIGHT, 0))

            rws.add(CTable(tbl))
            tbl = hashMapOf()

            tbl.put(0, TCell(ctx!!.resources!!.getString(R.string.rpt_invoice_date),9F,false,15F,"", com.itextpdf.text.Element.ALIGN_RIGHT,0, fontName = fontNameAr))
            tbl.put(1, TCell(returnDateString(entityEo.sl_doc_date!!),9F,false,30F,"", com.itextpdf.text.Element.ALIGN_RIGHT,0, fontName = fontNameAr))

            tbl.put(2, TCell("${ctx!!.resources!!.getString(R.string.lbl_time)}",9F,false,15F,"", com.itextpdf.text.Element.ALIGN_CENTER, 0 ))
            tbl.put(3, TCell("${LocalTime.now()}",9F, false, 30F,"", com.itextpdf.text.Element.ALIGN_CENTER,0))
            rws.add(CTable(tbl))

            tbl = hashMapOf()
            tbl.put(0, TCell(ctx!!.resources!!.getString(R.string.rpt_customer), 9F, false, 12F, "", com.itextpdf.text.Element.ALIGN_CENTER, 0))
            tbl.put(1, TCell( entityEo.sl_customer_name!!, 9F,false, 12F, "", Element.ALIGN_RIGHT, 0, fontName = fontNameAr))
            tbl.put(2, TCell("",9F,false,12F,"", Element.ALIGN_RIGHT,0, fontName = fontNameAr))
            tbl.put(3, TCell(ctx!!.resources!!.getString(R.string.rpt_contact_name),9F,false,12F,"", Element.ALIGN_RIGHT,0,fontNameAr))
            rws.add(CTable(tbl))

            val cw: ArrayList<Int> = arrayListOf(15, 30, 15, 30)
            header.add(HeaderFooterRow(8, rws, null, cellsWidth = cw))

            val footer: ArrayList<HeaderFooterRow> = arrayListOf()
            footer.add(HeaderFooterRow(0,null,"موارد", fontSize = 9F, align = Element.ALIGN_LEFT, fontName = fontNameAr))
            footer.add(HeaderFooterRow(1,null,"الشركة الحديثة للبرامجيات الاتمتة المحدودة", fontSize = 9F, align = Element.ALIGN_LEFT,fontName = fontNameAr))
            footer.add(HeaderFooterRow(2,null,ctx!!.resources!!.getString(R.string.rpt_user_name) + ": ${App.prefs.saveUser!!.name}", fontSize = 9F, align = Element.ALIGN_LEFT, fontName = fontNameAr))

            val rowHeader: HashMap<Int, RowHeader> = hashMapOf()
            //rowHeader.put(0, RowHeader("#", 9.0F, false, 4, "", 0, 0F))
            //rowHeader.put(1,RowHeader(ctx!!.resources!!.getString(R.string.rpt_barcode),9.0F, false,15, "", 0,0F))
            rowHeader.put(0, RowHeader( ctx!!.resources!!.getString(R.string.rpt_prod_name), 9.0F,false,30,"",0,0F))
            rowHeader.put(1, RowHeader(ctx!!.resources!!.getString(R.string.rpt_qty),9.0F,false,5,"",0,0F))
            //rowHeader.put(4,RowHeader(ctx!!.resources!!.getString(R.string.rpt_gift),9.0F,false,5,"",0,0F))
            //rowHeader.put(5,RowHeader(ctx!!.resources!!.getString(R.string.rpt_unit_price),9.0F,false,11,"",0,0F))
            //rowHeader.put(6,RowHeader(ctx!!.resources!!.getString(R.string.rpt_dis_value),9.0F,false,7,"",0,0F))
            rowHeader.put(2, RowHeader(ctx!!.resources!!.getString(R.string.rpt_net_total),9.0F,false,11,"",0,0F))
            //rowHeader.put(8,RowHeader(ctx!!.resources!!.getString(R.string.rpt_notes),9.0F,false,13,"Total",0,0F))

            // Summary part
            val df1 = DecimalFormat("#,###")
            val df2 = DecimalFormat("#,###,###.#")
            val summary: ArrayList<HeaderFooterRow> = arrayListOf()
            tbl = hashMapOf()
            var srows: ArrayList<CTable> = arrayListOf()
            val tQty = entityEo.items.sumByDouble { it.sld_pack_qty!! }
            tbl.put(0, TCell(ctx!!.resources!!.getString(R.string.rpt_total_qty),9F,false,25F,"",Element.ALIGN_RIGHT,1,fontName = fontNameAr))
            tbl.put(1, TCell("${df1.format(tQty)}", 9F, false, 80F, "", Element.ALIGN_LEFT, 1))
            srows.add(CTable(tbl))

            tbl = hashMapOf()
            val gQty = entityEo.items.sumByDouble { it.sld_gift_qty!! }
            tbl.put(0, TCell(ctx!!.resources!!.getString(R.string.lbl_total_gift),9F,false,25F,"",Element.ALIGN_RIGHT,1,fontName = fontNameAr))
            tbl.put(1, TCell("${df1.format(gQty)}", 9F, false, 80F, "", Element.ALIGN_RIGHT, 1))
            srows.add(CTable(tbl))
            // row 2
            tbl = hashMapOf()
            tbl.put(0, TCell(ctx!!.resources!!.getString(R.string.rpt_total_amount),9F,false,12F,"", Element.ALIGN_RIGHT,1, fontName = fontNameAr))
            tbl.put(1, TCell("${df2.format(entityEo.sl_total_amount)}  ${entityEo.sl_cr_name}",9F,false,80F,"", Element.ALIGN_RIGHT,1))
            srows.add(CTable(tbl))
            // row 3
            val tDiscount =if (entityEo.sl_total_discount == null) 0.00 else entityEo.sl_total_discount
            tbl = hashMapOf()
            tbl.put(0, TCell(ctx!!.resources!!.getString(R.string.rpt_total_discount),9F,false,12F, "", Element.ALIGN_RIGHT, 1,fontName = fontNameAr))
            tbl.put(1, TCell("${df2.format(tDiscount)}  ${entityEo.sl_cr_name}",9F,false,80F,"",Element.ALIGN_RIGHT,1))
            srows.add(CTable(tbl))
            // row 4
            tbl = hashMapOf()
            tbl.put(0, TCell( ctx!!.resources!!.getString(R.string.rpt_net_amount),9F,false,12F,"", Element.ALIGN_RIGHT,1,fontName = fontNameAr))
            tbl.put( 1, TCell("${df2.format(entityEo.sl_net_amount)}  ${entityEo.sl_cr_name}",9F,false,80F, "", Element.ALIGN_RIGHT,1))
            srows.add(CTable(tbl))

            val scw: java.util.ArrayList<Int> = arrayListOf(80, 20)
            summary.add(HeaderFooterRow(0, srows, null, cellsWidth = scw))

            summary.add(HeaderFooterRow(1, null, "T", fontSize = 20F, fontColor = BaseColor.WHITE))
            summary.add(HeaderFooterRow(2, null, "T", fontSize = 20F, fontColor = BaseColor.WHITE))
            summary.add(HeaderFooterRow(3, null, "T", fontSize = 20F, fontColor = BaseColor.WHITE))
            summary.add(HeaderFooterRow(4, null, "T", fontSize = 20F, fontColor = BaseColor.WHITE))


            GenerateTicketPDF().createPdf(activity!!, imgLogo, entityEo.items, rowHeader,header,footer, summary,true) { _, path ->
                msgListener?.onSuccess("Pdf Created Successfully")
                GeneratePdf().printPDF(activity!!, path)
               try {
                   val file: File = File(path)
                   val bitmaps = HPRTPrinterHelper.PrintPDF(ctx, file, "1", 384)
                   Print.PrintBitmap(bitmaps[0], 1, 0)
               }catch (e: Exception){
                   msgListener?.onFailure("Error Exception ${e.message}")
                   e.printStackTrace()
               }
            }
        } catch (e: Exception) {
            msgListener?.onFailure("Error Exception ${e.message}")
            e.printStackTrace()
        }
        isPrint = false
    }

//    fun getBitmapFromURL(src: String?) {
//
//
//        Thread(Runnable {
//            try {
//                val url = URL(src)
//                val `is`: InputStream? = url.openConnection().getInputStream()
//                if (`is` != null)
//                    bmpLogo = BitmapFactory.decodeStream(`is`)
//            } catch (e: IOException) {
//
//            } catch (e1: FileNotFoundException) {
//                e1.printStackTrace()
//            }
//        }
//        ).start()
//    }

    private fun PrintInvoice(entityEo: Sale){
        val config = ctx!!.resources.configuration
        val isRTL = config.layoutDirection != View.LAYOUT_DIRECTION_LTR
        val lang = Locale.getDefault().toString()
        var bmp: Bitmap? =
            null // BitmapFactory.decodeResource(ctx!!.resources, R.drawable.ic_logo_black)

        val mngr: AssetManager = ctx!!.assets
        var `is`: InputStream? = null
        try {
            //bmp = BitmapFactory.decodeStream((InputStream)new URL(URL_LOGO + "co_black_logo.png").getContent());
 //               bmp = bmpLogo// BitmapFactory.decodeStream(URL(URL_LOGO + "co_black_logo.png").content as InputStream)
                //bmp = getBitmapFromURL(URL_LOGO + "co_black_logo.png")
//            val conn = URL(URL_LOGO + "co_black_logo.png").openConnection()
//            conn.connect()
//            val length = conn.contentLength
//            if (length > 0) {
//                val bitmapData = IntArray(length)
//                val bitmapData2 = ByteArray(length)
//                `is`= conn.getInputStream()
//                bmp = BitmapFactory.decodeStream(`is`)
//
//            }
           // `is` = mngr.open("images/co_logo.bmp")
           // bmp = BitmapFactory.decodeStream(`is`)
        } catch (e1: IOException) {
            e1.printStackTrace()
        }
        val fontNameEn = "assets/fonts/arial.ttf"
        val fontNameAr = "assets/fonts/arial.ttf"// "assets/fonts/droid_kufi_regular.ttf"
        try {

            val imgLogo = RepLogo(bmp, 10F, 800F)
            val header: ArrayList<HeaderFooterRow> = arrayListOf()
            var tbl: HashMap<Int, TCell> = hashMapOf()
            var rws: ArrayList<CTable> = arrayListOf()
            val phones = if (entityEo.sl_org_phone != null) entityEo.sl_org_phone!!.replace(
                "|",
                "\n\r"
            ) else ""

            header.add(
                HeaderFooterRow(
                    0,
                    null,
                    App.prefs.saveUser!!.client_name,
                    14F,
                    Element.ALIGN_CENTER,
                    Font.NORMAL,
                    fontNameEn
                )
            )
            header.add(
                HeaderFooterRow(
                    1,
                    null,
                    "${entityEo.sl_org_name}",
                    14F,
                    Element.ALIGN_CENTER,
                    Font.NORMAL,
                    fontNameEn
                )
            )
            header.add(
                HeaderFooterRow(
                    2,
                    null,
                    phones,
                    11F,
                    Element.ALIGN_CENTER,
                    Font.NORMAL,
                    fontNameEn
                )
            )
            //header.add(HeaderFooterRow(3, null, "Asia: 0770-6502228", 20F, Element.ALIGN_CENTER, Font.BOLD, fontNameEn))
            header.add(
                HeaderFooterRow(
                    3,
                    null,
                    "",
                    14F,
                    Element.ALIGN_CENTER,
                    Font.NORMAL,
                    fontNameAr
                )
            )
            header.add(
                HeaderFooterRow(
                    4,
                    null,
                    "",
                    14F,
                    Element.ALIGN_CENTER,
                    Font.NORMAL,
                    fontNameEn
                )
            )
            header.add(
                HeaderFooterRow(
                    5,
                    null,
                    "",
                    14F,
                    Element.ALIGN_CENTER,
                    Font.NORMAL,
                    fontNameEn
                )
            )

            tbl.put(0, TCell("", 9F, false, 2f, "", com.itextpdf.text.Element.ALIGN_CENTER, 0))
            tbl.put( 1, TCell(  ctx!!.resources!!.getString(R.string.rpt_list_name),
                    9f,
                    false,
                    15F,
                    "",
                    Element.ALIGN_RIGHT,
                    0
                )
            )
            tbl.put(
                2,
                TCell(
                    entityEo.sl_vo_name!!,
                    9F,
                    false,
                    30F,
                    "",
                    com.itextpdf.text.Element.ALIGN_RIGHT,
                    0
                )
            )

            tbl.put(
                3,
                TCell(
                    ctx!!.resources!!.getString(R.string.rpt_invoice_no),
                    9F,
                    false,
                    15F,
                    "",
                    Element.ALIGN_RIGHT,
                    0
                )
            )
            tbl.put(
                4,
                TCell(
                    entityEo.sl_refNo!!,
                    9F,
                    false,
                    30F,
                    "",
                    com.itextpdf.text.Element.ALIGN_RIGHT,
                    0
                )
            )

            tbl.put(
                5,
                TCell(
                    ctx!!.resources!!.getString(R.string.rpt_invoice_date),
                    9F,
                    false,
                    10F,
                    "",
                    com.itextpdf.text.Element.ALIGN_RIGHT,
                    0,
                    fontName = fontNameAr
                )
            )
            tbl.put(
                6,
                TCell(
                    returnDateString(entityEo.sl_doc_date!!),
                    9F,
                    false,
                    25F,
                    "",
                    com.itextpdf.text.Element.ALIGN_RIGHT,
                    0,
                    fontName = fontNameAr
                )
            )

            tbl.put(7, TCell("", 9F, false, 15F, "", com.itextpdf.text.Element.ALIGN_CENTER, 0))
            tbl.put(8, TCell("", 9F, false, 10F, "", com.itextpdf.text.Element.ALIGN_CENTER, 0))
            tbl.put(9, TCell("", 9F, false, 2F, "", com.itextpdf.text.Element.ALIGN_CENTER, 0))
            rws.add(CTable(tbl))
            tbl = hashMapOf()

            tbl.put(0, TCell("", 9F, false, 12F, "", com.itextpdf.text.Element.ALIGN_CENTER, 0))
            tbl.put( 1,
                TCell(
                    ctx!!.resources!!.getString(R.string.rpt_customer),
                    9F,
                    false,
                    12F,
                    "",
                    Element.ALIGN_RIGHT,
                    0,
                    fontName = fontNameAr
                )
            )
            tbl.put(
                2,
                TCell(
                    entityEo.sl_customer_name!!,
                    9F,
                    false,
                    12F,
                    "",
                    Element.ALIGN_RIGHT,
                    0,
                    fontName = fontNameAr
                )
            )

            tbl.put(
                3,
                TCell(
                    ctx!!.resources!!.getString(R.string.rpt_contact_name),
                    9F,
                    false,
                    12F,
                    "",
                    Element.ALIGN_RIGHT,
                    0,
                    fontNameAr
                )
            )
            tbl.put(
                4,
                TCell(
                    "${entityEo.sl_contact_name}",
                    9F,
                    false,
                    12F,
                    "",
                    Element.ALIGN_RIGHT,
                    0,
                    fontNameAr
                )
            )

            tbl.put(
                5,
                TCell(
                    ctx!!.resources!!.getString(R.string.rpt_phone),
                    9F,
                    false,
                    12F,
                    "",
                    com.itextpdf.text.Element.ALIGN_RIGHT,
                    0
                )
            )
            tbl.put(
                6,
                TCell(
                    if (entityEo.sl_customer_phone == null) "" else entityEo.sl_customer_phone!!,
                    9F,
                    false,
                    12F,
                    "",
                    com.itextpdf.text.Element.ALIGN_RIGHT,
                    0
                )
            )

            tbl.put(
                7,
                TCell(
                    ctx!!.resources!!.getString(R.string.rpt_cr_name),
                    9F,
                    false,
                    18F,
                    "",
                    com.itextpdf.text.Element.ALIGN_RIGHT,
                    0
                )
            )
            tbl.put(
                8,
                TCell(
                    if (entityEo.sl_cr_name == null) "" else entityEo.sl_cr_name!!,
                    9F,
                    false,
                    18F,
                    "",
                    com.itextpdf.text.Element.ALIGN_RIGHT,
                    0
                )
            )

            tbl.put(9, TCell("", 9F, false, 12F, "", com.itextpdf.text.Element.ALIGN_CENTER, 0))
            rws.add(CTable(tbl))

            val cw: ArrayList<Int> = arrayListOf(5, 15, 25, 10, 30, 25, 15, 15, 10, 5)
            header.add(HeaderFooterRow(8, rws, null, cellsWidth = cw))

            val footer: ArrayList<HeaderFooterRow> = arrayListOf()
            var LineNum: Int = 0
            if(!App.prefs.saveUser!!.print_msg.isNullOrEmpty()){
                val lines = App.prefs.saveUser!!.print_msg!!.split("#").map{it.trim()}
                for (str: String in lines){
                    footer.add(HeaderFooterRow(LineNum, null, "$str", fontSize = 11F, align = Element.ALIGN_LEFT, Font.BOLD,  fontName = fontNameAr))
                    LineNum++
                }
                footer.add(HeaderFooterRow(LineNum++, null, "", fontSize = 12F, align = Element.ALIGN_LEFT,  fontName = fontNameAr))
                footer.add(HeaderFooterRow(LineNum++, null, "", fontSize = 12F, align = Element.ALIGN_LEFT,  fontName = fontNameAr))
                footer.add(HeaderFooterRow(LineNum++, null, "", fontSize = 12F, align = Element.ALIGN_LEFT,  fontName = fontNameAr))
            }

            footer.add(HeaderFooterRow(LineNum++,null,"موارد / الشركة الحديثة للبرامجيات الاتمتة المحدودة", fontSize = 9F, align = Element.ALIGN_LEFT, fontName = fontNameAr))
            footer.add( HeaderFooterRow( LineNum++,null,ctx!!.resources!!.getString(R.string.rpt_user_name) + ": ${App.prefs.saveUser!!.name}",  fontSize = 9F, align = Element.ALIGN_LEFT, fontName = fontNameAr))

            val rowHeader: HashMap<Int, RowHeader> = hashMapOf()
            rowHeader.put(0, RowHeader("#", 9.0F, false, 4, "", 0, 0F))
            rowHeader.put(  1,   RowHeader(   ctx!!.resources!!.getString(R.string.rpt_barcode),   9.0F, false,   15,  "", 0,   0F         )          )
            rowHeader.put( 2,  RowHeader(   ctx!!.resources!!.getString(R.string.rpt_prod_name),   9.0F,  false, 37, "", 0, 0F  )            )
            rowHeader.put(  3,   RowHeader(   ctx!!.resources!!.getString(R.string.rpt_qty),  9.0F,  false,  5,  "",  0,  0F                )            )
            rowHeader.put(  4,   RowHeader(   ctx!!.resources!!.getString(R.string.rpt_uom),  9.0F,  false,  8,  "",  0,  0F                )            )
            rowHeader.put( 5, RowHeader(  ctx!!.resources!!.getString(R.string.rpt_gift),  9.0F, false,5,  "", 0, 0F )  )
            rowHeader.put( 6, RowHeader(  ctx!!.resources!!.getString(R.string.rpt_unit_price), 9.0F,  false,11,  "", 0,  0F  )           )
            //rowHeader.put( 7,  RowHeader(   ctx!!.resources!!.getString(R.string.rpt_dis_value),  9.0F,   false,  7,    "",    0,  0F) )
            rowHeader.put(    7,   RowHeader(   ctx!!.resources!!.getString(R.string.rpt_net_total),   9.0F,  false,  11,  "",     0,    0F       )     )
            rowHeader.put( 8,RowHeader( ctx!!.resources!!.getString(R.string.rpt_notes),  9.0F,  false,  13,  "Total",   0,  0F           )            )

            // Summary part
            val df1 = DecimalFormat("#,###")
            val df2 = DecimalFormat("#,###,###.#")
            val summary: ArrayList<HeaderFooterRow> = arrayListOf()
            tbl = hashMapOf()
            var srows: ArrayList<CTable> = arrayListOf()
            val tQty = entityEo.items.sumByDouble { it.sld_pack_qty!! }
            tbl.put(    0,    TCell(   ctx!!.resources!!.getString(R.string.rpt_total_qty),   9F,  false,  25F,   "",   Element.ALIGN_RIGHT,  1,  fontName = fontNameAr)          )
            tbl.put(1, TCell("${df1.format(tQty)}", 9F, false, 80F, "", Element.ALIGN_RIGHT, 1))
            srows.add(CTable(tbl))

            tbl = hashMapOf()
            val tweight =  entityEo.items.sumByDouble { if (it.sld_total_weight == null) 0.00 else it.sld_total_weight!! }
            tbl.put(  0, TCell( ctx!!.resources!!.getString(R.string.rpt_total_weight), 9F,   false, 12F,  "",  Element.ALIGN_RIGHT,    1,    fontName = fontNameAr  ) )
            tbl.put(  1,  TCell("${df2.format(tweight)}", 9F, false, 80F, "", Element.ALIGN_RIGHT, 1)            )
            srows.add(CTable(tbl))
            // row 2
            tbl = hashMapOf()
            tbl.put(   0,  TCell(  ctx!!.resources!!.getString(R.string.rpt_total_amount),  9F,   false,  12F,  "",   Element.ALIGN_RIGHT,  1,   fontName = fontNameAr ) )
            tbl.put( 1,  TCell( "${df2.format(entityEo.sl_total_amount)}",   9F,    false, 80F, "",  Element.ALIGN_RIGHT, 1 ) )
            srows.add(CTable(tbl))
            // row 3
            val tDiscount =
                if (entityEo.sl_total_discount == null) 0.00 else entityEo.sl_total_discount
            tbl = hashMapOf()
            tbl.put( 0,  TCell(  ctx!!.resources!!.getString(R.string.rpt_total_discount), 9F, false,  12F,  "",  Element.ALIGN_RIGHT, 1, fontName = fontNameAr     )  )
            tbl.put(   1, TCell("${df2.format(tDiscount)}", 9F, false, 80F, "", Element.ALIGN_RIGHT, 1)          )
            srows.add(CTable(tbl))
            // row 4
            tbl = hashMapOf()
            tbl.put(  0,  TCell(  ctx!!.resources!!.getString(R.string.rpt_net_amount),9F,   false, 12F, "",  Element.ALIGN_RIGHT,  1, fontName = fontNameAr  )  )
            tbl.put( 1,  TCell( "${df2.format(entityEo.sl_net_amount)}", 9F, false,  80F,  "",  Element.ALIGN_RIGHT,  1  ) )
            srows.add(CTable(tbl))

            //sl_customer_balance
            var balance: Double = 0.00
            if (entityEo.sl_customer_balance != null) balance = entityEo.sl_customer_balance!!
            tbl = hashMapOf()
            tbl.put(   0,  TCell( ctx!!.resources!!.getString(R.string.rpt_cu_balance), 9F, false, 12F, "",  Element.ALIGN_RIGHT, 1,  fontName = fontNameAr )  )
            tbl.put( 1, TCell( "${df2.format(balance)}  ${entityEo.sl_cr_name}",   9F,   false, 80F,  "",   Element.ALIGN_RIGHT, 1 )  )
            srows.add(CTable(tbl))

            val scw: java.util.ArrayList<Int> = arrayListOf(80, 20)
            summary.add(HeaderFooterRow(0, srows, null, cellsWidth = scw))

            summary.add(
                HeaderFooterRow(
                    1,
                    null,
                    "T",
                    fontSize = 20F,
                    fontColor = BaseColor.WHITE
                )
            )
            summary.add(
                HeaderFooterRow(
                    2,
                    null,
                    "T",
                    fontSize = 20F,
                    fontColor = BaseColor.WHITE
                )
            )
            summary.add(
                HeaderFooterRow(
                    3,
                    null,
                    "T",
                    fontSize = 20F,
                    fontColor = BaseColor.WHITE
                )
            )
            summary.add(
                HeaderFooterRow(
                    4,
                    null,
                    "T",
                    fontSize = 20F,
                    fontColor = BaseColor.WHITE
                )
            )
            srows = arrayListOf()
            tbl = hashMapOf()
            tbl.put(
                0,
                TCell(
                    ctx!!.resources.getString(R.string.rpt_person_reciever_sig),
                    10F,
                    false,
                    12F,
                    "",
                    Element.ALIGN_CENTER,
                    0,
                    fontName = fontNameAr
                )
            )
            tbl.put(
                1,
                TCell(
                    ctx!!.resources.getString(R.string.rpt_storekeeper_sig),
                    10F,
                    false,
                    12F,
                    "",
                    Element.ALIGN_CENTER,
                    0,
                    fontName = fontNameAr
                )
            )
            tbl.put( 2,  TCell( ctx!!.resources.getString(R.string.rpt_sales_manager_sig),  10F,   false,  12F,  "", Element.ALIGN_CENTER,    0,fontName = fontNameAr))

            // Print Message
            srows.add(CTable(tbl))

            summary.add(HeaderFooterRow(5, srows, null, cellsWidth = arrayListOf(35, 35, 34)))

            GeneratePdf().createPdf(
                activity!!,
                imgLogo,
                entityEo.items,
                rowHeader,
                header,
                footer,
                null,
                summary,
                isRTL
            ) { _, path ->
                msgListener?.onSuccess("Pdf Created Successfully")
                GeneratePdf().printPDF(activity!!, path)
            }
        } catch (e: Exception) {
            msgListener?.onFailure("Error Exception ${e.message}")
            e.printStackTrace()
        }
    }
    fun onPrintTicket1(entityEo: Sale) {
//        val lang = Locale.getDefault().toString().toLowerCase()
//        val tickets = GenerateTicket(ctx!!, lang).create(entityEo,
//            R.drawable.ic_logo_black, "Mawared Vansale\nAL-HADETHA FRO SOFTWATE & AUTOMATION", null, null)

        //TicketPrinting(ctx!!, tickets).run()

        try {
            val fontNameEn = "assets/fonts/brandon_medium.otf"
            val fontNameAr = "assets/fonts/droid_kufi_regular.ttf"
            var xPos = 200F
            var uxPos = 350F
            var yPos = 820F
            var uyPos = 840F
            val header: ArrayList<ITextTicket> = arrayListOf()
            header.add(
                ITextTicket(
                    0,
                    "",
                    LineType.Text,
                    xPos,
                    yPos,
                    uxPos,
                    uyPos,
                    PdfContentByte.ALIGN_CENTER,
                    fontNameEn,
                    BaseColor.BLACK,
                    Font.NORMAL,
                    12F
                )
            )
            header.add(
                ITextTicket(
                    1,
                    "",
                    LineType.Text,
                    xPos,
                    yPos - 12,
                    uxPos,
                    uyPos - 12,
                    PdfContentByte.ALIGN_CENTER,
                    fontNameEn,
                    BaseColor.BLACK,
                    Font.NORMAL,
                    12F
                )
            )
            header.add(
                ITextTicket(
                    2,
                    "Korek: 0750-7363286",
                    LineType.Text,
                    xPos,
                    yPos - 24,
                    uxPos,
                    uyPos - 24,
                    PdfContentByte.ALIGN_CENTER,
                    fontNameEn,
                    BaseColor.BLACK,
                    Font.NORMAL,
                    12F
                )
            )
            header.add(
                ITextTicket(
                    3,
                    "Asia: 0770-6502228",
                    LineType.Text,
                    xPos,
                    yPos - 36,
                    uxPos,
                    uyPos - 36,
                    PdfContentByte.ALIGN_CENTER,
                    fontNameEn,
                    BaseColor.BLACK,
                    Font.NORMAL,
                    12F
                )
            )
            header.add(
                ITextTicket(
                    4,
                    "",
                    LineType.Text,
                    xPos,
                    yPos - 48,
                    uxPos,
                    uyPos - 48,
                    PdfContentByte.ALIGN_CENTER,
                    fontNameAr,
                    BaseColor.BLACK,
                    Font.NORMAL,
                    12F
                )
            )
            header.add(
                ITextTicket(
                    5,
                    "",
                    LineType.Text,
                    xPos,
                    yPos - 60,
                    uxPos,
                    yPos - 60,
                    PdfContentByte.ALIGN_CENTER,
                    fontNameEn,
                    BaseColor.BLACK,
                    Font.NORMAL,
                    12F
                )
            )
            header.add(
                ITextTicket(
                    6,
                    "Sale Invoice",
                    LineType.Text,
                    xPos,
                    yPos - 72,
                    uxPos,
                    uyPos - 72,
                    PdfContentByte.ALIGN_CENTER,
                    fontNameEn,
                    BaseColor.BLACK,
                    Font.NORMAL,
                    12F
                )
            )
            xPos = 10F
            uxPos = 100F
            yPos = 742F
            uyPos = 768F
            header.add(
                ITextTicket(
                    13,
                    "العملة:دينار",
                    LineType.Text,
                    xPos,
                    yPos,
                    uxPos,
                    uyPos,
                    PdfContentByte.ALIGN_CENTER,
                    fontNameAr,
                    BaseColor.BLACK,
                    Font.NORMAL,
                    12F
                )
            )
            header.add(
                ITextTicket(
                    14,
                    "نوع الفاتورة : اجل",
                    LineType.Text,
                    xPos,
                    yPos - 12,
                    uxPos,
                    uyPos - 12,
                    PdfContentByte.ALIGN_CENTER,
                    fontNameAr,
                    BaseColor.BLACK,
                    Font.NORMAL,
                    12F
                )
            )

//            xPos = 180F
//            uxPos = 220F
//            yPos = 744F
//            uyPos = 756F
//            header.add(ITextTicket(7, "رقم القائمة", LineType.Text, xPos, yPos, uxPos, uyPos, PdfContentByte.ALIGN_CENTER, fontNameAr, BaseColor.BLACK, Font.NORMAL, 12F))
//            header.add(ITextTicket(8, entityEo.sl_refNo!!, LineType.Text, xPos + 100, yPos, uxPos + 100, uyPos, PdfContentByte.ALIGN_CENTER, fontNameEn, BaseColor.BLACK, Font.NORMAL, 12F))
//            header.add(ITextTicket(9, "التاريخ", LineType.Text, xPos, yPos-12, uxPos, uyPos - 12, PdfContentByte.ALIGN_CENTER, fontNameAr, BaseColor.BLACK, Font.NORMAL, 12F))
//            header.add(ITextTicket(10, "${returnDateString(entityEo.sl_doc_date!!)}", LineType.Text, xPos + 100, yPos-12, uxPos + 100, uyPos - 12, PdfContentByte.ALIGN_CENTER, fontNameEn, BaseColor.BLACK, Font.NORMAL, 12F))
//            header.add(ITextTicket(11, "الزبون", LineType.Text, xPos, yPos-24, uxPos, uyPos - 24, PdfContentByte.ALIGN_CENTER, fontNameAr, BaseColor.BLACK, Font.NORMAL, 12F))
//            header.add(ITextTicket(12, entityEo.sl_customer_name!!, LineType.Text, xPos + 100, yPos-24, uxPos + 100, uyPos - 24, PdfContentByte.ALIGN_CENTER, fontNameEn, BaseColor.BLACK, Font.NORMAL, 12F))

            xPos = 100F
            yPos = 55F
            val footer: ArrayList<ITextTicket> = arrayListOf()
            footer.add(
                ITextTicket(
                    0,
                    "موارد",
                    LineType.Text,
                    xPos,
                    yPos,
                    0F,
                    0F,
                    PdfContentByte.ALIGN_CENTER,
                    fontNameAr,
                    BaseColor.BLACK,
                    Font.NORMAL,
                    12F
                )
            )
            footer.add(
                ITextTicket(
                    1,
                    "الشركة الحديثة للبرامجيات الاتمتة المحدودة",
                    LineType.Text,
                    xPos,
                    yPos - 12,
                    0F,
                    0F,
                    PdfContentByte.ALIGN_CENTER,
                    fontNameAr,
                    BaseColor.BLACK,
                    Font.NORMAL,
                    12F
                )
            )

            footer.add(
                ITextTicket(
                    2,
                    "المستخدم",
                    LineType.Text,
                    xPos,
                    yPos - 24,
                    0F,
                    0F,
                    PdfContentByte.ALIGN_CENTER,
                    fontNameAr,
                    BaseColor.BLACK,
                    Font.NORMAL,
                    12F
                )
            )
            footer.add(
                ITextTicket(
                    3,
                    "${App.prefs.saveUser!!.user_name}",
                    LineType.Text,
                    xPos,
                    yPos - 36,
                    0F,
                    0F,
                    PdfContentByte.ALIGN_CENTER,
                    fontNameEn,
                    BaseColor.BLACK,
                    Font.NORMAL,
                    12F
                )
            )
            // intArrayOf(20, 8, 8, 8, 5, 20, 10, 5)
            val rowHeader: ArrayList<ITextTicket> = arrayListOf()
            rowHeader.add(
                ITextTicket(
                    0,
                    "#",
                    LineType.Text,
                    10F,
                    694F,
                    5F,
                    0F,
                    PdfContentByte.ALIGN_CENTER,
                    fontNameEn,
                    BaseColor.BLACK,
                    Font.NORMAL,
                    12F
                )
            )
            rowHeader.add(
                ITextTicket(
                    1,
                    "Barcode",
                    LineType.Text,
                    0F,
                    0F,
                    10F,
                    0F,
                    PdfContentByte.ALIGN_CENTER,
                    fontNameEn,
                    BaseColor.BLACK,
                    Font.NORMAL,
                    12F
                )
            )
            rowHeader.add(
                ITextTicket(
                    2,
                    "Product Name",
                    LineType.Text,
                    0F,
                    0F,
                    20F,
                    0F,
                    PdfContentByte.ALIGN_CENTER,
                    fontNameEn,
                    BaseColor.BLACK,
                    Font.NORMAL,
                    12F
                )
            )
            rowHeader.add(
                ITextTicket(
                    3,
                    "Qty",
                    LineType.Text,
                    0F,
                    0F,
                    5F,
                    0F,
                    PdfContentByte.ALIGN_CENTER,
                    fontNameEn,
                    BaseColor.BLACK,
                    Font.NORMAL,
                    12F
                )
            )
            rowHeader.add(
                ITextTicket(
                    4,
                    "GitQty",
                    LineType.Text,
                    0F,
                    0F,
                    8F,
                    0F,
                    PdfContentByte.ALIGN_CENTER,
                    fontNameEn,
                    BaseColor.BLACK,
                    Font.NORMAL,
                    12F
                )
            )
            rowHeader.add(
                ITextTicket(
                    5,
                    "Unit Price",
                    LineType.Text,
                    0F,
                    0F,
                    8F,
                    0F,
                    PdfContentByte.ALIGN_CENTER,
                    fontNameEn,
                    BaseColor.BLACK,
                    Font.NORMAL,
                    12F
                )
            )
            rowHeader.add(
                ITextTicket(
                    6,
                    "Discount",
                    LineType.Text,
                    0F,
                    0F,
                    8F,
                    0F,
                    PdfContentByte.ALIGN_CENTER,
                    fontNameEn,
                    BaseColor.BLACK,
                    Font.NORMAL,
                    12F
                )
            )
            rowHeader.add(
                ITextTicket(
                    7,
                    "Net Total",
                    LineType.Text,
                    0F,
                    0F,
                    8F,
                    0F,
                    PdfContentByte.ALIGN_CENTER,
                    fontNameEn,
                    BaseColor.BLACK,
                    Font.NORMAL,
                    12F
                )
            )
            rowHeader.add(
                ITextTicket(
                    8,
                    "Notes",
                    LineType.Text,
                    0F,
                    0F,
                    20F,
                    0F,
                    PdfContentByte.ALIGN_CENTER,
                    fontNameEn,
                    BaseColor.BLACK,
                    Font.NORMAL,
                    12F
                )
            )


            // Summary part
            val tQty = entityEo.items.sumByDouble { it.sld_pack_qty!! }
             xPos = 100F
             yPos = 53F
            val summary: ArrayList<ITextTicket> = arrayListOf()
            summary.add(
                ITextTicket(
                    0,
                    "مجموع الكميات والوزن",
                    LineType.Text,
                    xPos,
                    yPos,
                    0F,
                    0F,
                    PdfContentByte.ALIGN_CENTER,
                    fontNameEn,
                    BaseColor.BLACK,
                    Font.NORMAL,
                    12F
                )
            )
            summary.add(
                ITextTicket(
                    1,
                    "${tQty}",
                    LineType.Text,
                    xPos + 100,
                    yPos,
                    0F,
                    0F,
                    PdfContentByte.ALIGN_CENTER,
                    fontNameEn,
                    BaseColor.BLACK,
                    Font.NORMAL,
                    12F
                )
            )

            summary.add(
                ITextTicket(
                    2,
                    "المجموع",
                    LineType.Text,
                    xPos,
                    yPos - 12,
                    0F,
                    0F,
                    PdfContentByte.ALIGN_CENTER,
                    fontNameEn,
                    BaseColor.BLACK,
                    Font.NORMAL,
                    12F
                )
            )
            summary.add(
                ITextTicket(
                    3,
                    "${entityEo.sl_total_amount}",
                    LineType.Text,
                    xPos + 100,
                    yPos - 12,
                    0F,
                    0F,
                    PdfContentByte.ALIGN_CENTER,
                    fontNameEn,
                    BaseColor.BLACK,
                    Font.NORMAL,
                    12F
                )
            )

            summary.add(
                ITextTicket(
                    4,
                    "مجموع الخصومات",
                    LineType.Text,
                    xPos,
                    yPos - 24,
                    0F,
                    0F,
                    PdfContentByte.ALIGN_CENTER,
                    fontNameEn,
                    BaseColor.BLACK,
                    Font.NORMAL,
                    12F
                )
            )
            summary.add(
                ITextTicket(
                    5,
                    "${entityEo.sl_total_discount}",
                    LineType.Text,
                    xPos + 100,
                    yPos - 24,
                    0F,
                    0F,
                    PdfContentByte.ALIGN_CENTER,
                    fontNameEn,
                    BaseColor.BLACK,
                    Font.NORMAL,
                    12F
                )
            )

            summary.add(
                ITextTicket(
                    6,
                    "الصافي للدفع",
                    LineType.Text,
                    xPos,
                    yPos - 36,
                    0F,
                    0F,
                    PdfContentByte.ALIGN_CENTER,
                    fontNameEn,
                    BaseColor.BLACK,
                    Font.NORMAL,
                    12F
                )
            )
            summary.add(
                ITextTicket(
                    7,
                    "${entityEo.sl_net_amount}",
                    LineType.Text,
                    xPos + 100,
                    yPos - 36,
                    0F,
                    0F,
                    PdfContentByte.ALIGN_CENTER,
                    fontNameEn,
                    BaseColor.BLACK,
                    Font.NORMAL,
                    12F
                )
            )

            xPos = 10F
            yPos = 50F
            val signRow: ArrayList<ITextTicket> = arrayListOf()
            signRow.add(
                ITextTicket(
                    0,
                    "اسم وتوقيع المستلم",
                    LineType.Text,
                    xPos,
                    yPos,
                    0F,
                    0F,
                    PdfContentByte.ALIGN_CENTER,
                    fontNameEn,
                    BaseColor.BLACK,
                    Font.NORMAL,
                    10F
                )
            )
            signRow.add(
                ITextTicket(
                    1,
                    "اسم وتوقيع مدير المخازن",
                    LineType.Text,
                    xPos + 80,
                    yPos,
                    0F,
                    0F,
                    PdfContentByte.ALIGN_CENTER,
                    fontNameEn,
                    BaseColor.BLACK,
                    Font.NORMAL,
                    10F
                )
            )
            signRow.add(
                ITextTicket(
                    1,
                    "اسم وتوقيع مدير المبيعات",
                    LineType.Text,
                    xPos + 80,
                    yPos,
                    0F,
                    0F,
                    PdfContentByte.ALIGN_CENTER,
                    fontNameEn,
                    BaseColor.BLACK,
                    Font.NORMAL,
                    10F
                )
            )


            GeneratePdf1().createPdf(
                activity!!,
                entityEo.items,
                rowHeader,
                header,
                footer,
                null,
                summary
            ) { _, path ->
                msgListener?.onSuccess("Pdf Created Successfully")
                GeneratePdf().printPDF(activity!!, path)
            }
        } catch (e: Exception) {
            msgListener?.onFailure("Error Exception ${e.message}")
            e.printStackTrace()
        }
        isPrint = false

    }
    // cancel job call in destroy fragment
    fun cancelJob() {
        repository.cancelJob()
    }
}