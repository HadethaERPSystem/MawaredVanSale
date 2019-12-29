package com.mawared.mawaredvansale.controller.sales.invoices.invoiceslist

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.mawared.mawaredvansale.controller.common.LineType
import com.mawared.mawaredvansale.controller.common.printing.*
import com.mawared.mawaredvansale.data.db.entities.sales.Sale
import com.mawared.mawaredvansale.interfaces.IMainNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.services.repositories.invoices.IInvoiceRepository
import java.io.IOException
import java.io.InputStream
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class InvoicesViewModel(private val repository: IInvoiceRepository) : BaseViewModel() {

    private val _sm_id: Int =
        if (App.prefs.savedSalesman?.sm_user_id != null) App.prefs.savedSalesman!!.sm_user_id!! else 0
    var ctx: Context? = null
    var activity: AppCompatActivity? = null
    var navigator: IMainNavigator<Sale>? = null
    var msgListener: IMessageListener? = null
    var isPrint = false
    private val _cu_id: MutableLiveData<Int> = MutableLiveData()

    val sales: LiveData<List<Sale>> = Transformations
        .switchMap(_cu_id) {
            repository.getInvoices(_sm_id, it)
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

    fun setCustomer(cm_Id: Int?) {
        if (_cu_id.value == cm_Id && cm_Id != null) {
            return
        }
        _cu_id.value = cm_Id
    }

    fun find(id: Int) {
        if (_sl_Id.value == id) {
            return
        }
        _sl_Id.value = id
    }

    // on press delete invoice
    fun onItemDelete(sale: Sale) {
        navigator?.onItemDeleteClick(sale)
    }

    fun confirmDelete(baseEo: Sale) {
        _sl_Id_for_delete.value = baseEo.sl_Id
    }

    // on press edit invoice
    fun onItemEdit(sale: Sale) {
        navigator?.onItemEditClick(sale)
    }

    // on press view invoice
    fun onItemView(sale: Sale) {
        navigator?.onItemViewClick(sale)
    }

    fun onPrint(sl_Id: Int) {
        isPrint = true
        msgListener?.onStarted()
        if (sl_Id != _sl_Id.value) {
            find(sl_Id)
            return
        }
        onPrintTicket(baseEo.value!!)
    }

    fun onPrintTicket(entityEo: Sale) {
//        val lang = Locale.getDefault().toString().toLowerCase()
//        val tickets = GenerateTicket(ctx!!, lang).create(entityEo,
//            R.drawable.ic_logo_black, "Mawared Vansale\nAL-HADETHA FRO SOFTWATE & AUTOMATION", null, null)

        //TicketPrinting(ctx!!, tickets).run()
        val lang = Locale.getDefault().toString().toLowerCase()
        var bmp: Bitmap? = null // BitmapFactory.decodeResource(ctx!!.resources, R.drawable.ic_logo_black)

        val mngr: AssetManager = ctx!!.getAssets()
        var `is`: InputStream? = null
        try {
            `is` = mngr.open("images/co_logo.bmp")
            bmp = BitmapFactory.decodeStream(`is`)
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
                val phones = if(entityEo.sl_org_phone != null) entityEo.sl_org_phone!!.replace("|", "\n\r") else ""

                header.add(HeaderFooterRow(0, null, "Al-Nadir Trading Company",  16F, Element.ALIGN_CENTER, Font.BOLD, fontNameEn))
                header.add(HeaderFooterRow(1, null, "${entityEo.sl_org_name}", 16F, Element.ALIGN_CENTER, Font.BOLD, fontNameEn))
                header.add(HeaderFooterRow(2, null, phones, 16F, Element.ALIGN_CENTER, Font.BOLD, fontNameEn))
                //header.add(HeaderFooterRow(3, null, "Asia: 0770-6502228", 20F, Element.ALIGN_CENTER, Font.BOLD, fontNameEn))
                header.add(HeaderFooterRow(3, null, "شركة النادر التجارية", 16F, Element.ALIGN_CENTER, Font.BOLD, fontNameAr ))
                header.add(HeaderFooterRow(4, null, "", 16F, Element.ALIGN_CENTER, Font.BOLD, fontNameEn))
                header.add(HeaderFooterRow(5, null, "", 16F, Element.ALIGN_CENTER, Font.BOLD, fontNameEn))

                tbl.put(0,TCell("", 10F, false, 5f, "", com.itextpdf.text.Element.ALIGN_CENTER, 0))
                tbl.put(1,TCell(ctx!!.resources!!.getString(R.string.rpt_list_name), 10f, false, 15F, "", Element.ALIGN_RIGHT, 0))
                tbl.put(2,TCell(entityEo.sl_vo_name!!,10F, false, 25F, "", com.itextpdf.text.Element.ALIGN_RIGHT, 0))

                tbl.put(3,TCell(ctx!!.resources!!.getString(R.string.rpt_invoice_no), 10F, false, 15F, "", Element.ALIGN_RIGHT, 0))
                tbl.put(4,TCell(entityEo.sl_refNo!!,10F, false, 30F, "", com.itextpdf.text.Element.ALIGN_RIGHT, 0))

                tbl.put(5,TCell(ctx!!.resources!!.getString(R.string.rpt_invoice_date) ,10F, false, 13F, "", com.itextpdf.text.Element.ALIGN_RIGHT,0,fontName = fontNameAr))
                tbl.put(6,TCell( returnDateString(entityEo.sl_doc_date!!),10F, false, 25F, "", com.itextpdf.text.Element.ALIGN_RIGHT,0,fontName = fontNameAr))

                tbl.put(7,TCell("", 10F, false, 15F, "", com.itextpdf.text.Element.ALIGN_CENTER, 0))
                tbl.put(8,TCell("", 10F, false, 10F, "", com.itextpdf.text.Element.ALIGN_CENTER, 0))
                tbl.put(9,TCell("", 10F, false, 5F, "", com.itextpdf.text.Element.ALIGN_CENTER, 0))
                rws.add(CTable(tbl))
                tbl = hashMapOf()

                tbl.put(0,TCell("", 10F, false, 12F, "", com.itextpdf.text.Element.ALIGN_CENTER, 0))
                tbl.put(1,TCell(ctx!!.resources!!.getString(R.string.rpt_customer) ,10F, false, 12F,"", Element.ALIGN_RIGHT,0,fontName = fontNameAr) )
                tbl.put(2,TCell(entityEo.sl_customer_name!!,10F, false, 12F,"", Element.ALIGN_RIGHT,0,fontName = fontNameAr) )

                tbl.put(3,TCell(ctx!!.resources!!.getString(R.string.rpt_contact_name),10F,false,12F,"", Element.ALIGN_RIGHT,0,fontNameAr))
                tbl.put(4,TCell("${entityEo.sl_contact_name}",10F,false,12F,"", Element.ALIGN_RIGHT,0,fontNameAr))

                tbl.put(5,TCell(ctx!!.resources!!.getString(R.string.rpt_customer_phone), 10F, false, 12F, "", com.itextpdf.text.Element.ALIGN_RIGHT, 0))
                tbl.put(6,TCell(if(entityEo.sl_customer_phone == null) "" else entityEo.sl_customer_phone!!, 10F, false, 12F, "", com.itextpdf.text.Element.ALIGN_RIGHT, 0))

                tbl.put(7,TCell(ctx!!.resources!!.getString(R.string.rpt_cr_name) , 10F, false, 18F, "", com.itextpdf.text.Element.ALIGN_RIGHT, 0))
                tbl.put(8,TCell(if(entityEo.sl_cr_name == null) "" else entityEo.sl_cr_name!! , 10F, false, 18F, "", com.itextpdf.text.Element.ALIGN_RIGHT, 0))

                tbl.put(9,TCell("", 10F, false, 12F, "", com.itextpdf.text.Element.ALIGN_CENTER, 0))
                rws.add(CTable(tbl))

                val cw: ArrayList<Int> = arrayListOf(5, 15, 25, 10, 30, 25, 15, 15, 10, 5)
                header.add(HeaderFooterRow(8, rws, null, cellsWidth = cw))

                val footer: ArrayList<HeaderFooterRow> = arrayListOf()
                footer.add(HeaderFooterRow(0,null,"موارد",fontSize = 9F,align = Element.ALIGN_LEFT,fontName = fontNameAr))
                footer.add(HeaderFooterRow(1,null,"الشركة الحديثة للبرامجيات الاتمتة المحدودة",fontSize = 9F, align = Element.ALIGN_LEFT, fontName = fontNameAr))
                footer.add(HeaderFooterRow(2,null,ctx!!.resources!!.getString(R.string.rpt_user_name) + "${App.prefs.saveUser!!.name}",fontSize = 9F, align = Element.ALIGN_LEFT, fontName = fontNameAr))
                val rowHeader: HashMap<Int, RowHeader> = hashMapOf()
                rowHeader.put(0, RowHeader("#", 9.0F, false, 5, "", 0, 0F))
                rowHeader.put(1, RowHeader(ctx!!.resources!!.getString(R.string.rpt_barcode), 9.0F, false, 12, "", 0, 0F))
                rowHeader.put(2, RowHeader(ctx!!.resources!!.getString(R.string.rpt_prod_name), 9.0F, false, 20, "", 0, 0F))
                rowHeader.put(3, RowHeader(ctx!!.resources!!.getString(R.string.rpt_qty), 9.0F, false, 7, "", 0, 0F))
                rowHeader.put(4, RowHeader(ctx!!.resources!!.getString(R.string.rpt_gift), 9.0F, false, 7, "", 0, 0F))
                rowHeader.put(5, RowHeader(ctx!!.resources!!.getString(R.string.rpt_unit_price), 9.0F, false, 12, "", 0, 0F))
                rowHeader.put(6, RowHeader(ctx!!.resources!!.getString(R.string.rpt_dis_value), 9.0F, false, 8, "", 0, 0F))
                rowHeader.put(7, RowHeader(ctx!!.resources!!.getString(R.string.rpt_net_total), 9.0F, false, 12, "", 0, 0F))
                rowHeader.put(8, RowHeader(ctx!!.resources!!.getString(R.string.rpt_notes), 9.0F, false, 20, "Total", 0, 0F))

                // Summary part
                val df1 = DecimalFormat("#,###")
                val df2 = DecimalFormat("#,###,###.#")
                val summary: ArrayList<HeaderFooterRow> = arrayListOf()
                tbl = hashMapOf()
                var srows: ArrayList<CTable> = arrayListOf()
                val tQty = entityEo.items.sumByDouble { it.sld_pack_qty!! }
                tbl.put(0, TCell(ctx!!.resources!!.getString(R.string.rpt_total_qty),10F,false,25F,"", Element.ALIGN_RIGHT,1, fontName = fontNameAr))
                tbl.put(1, TCell("${df1.format(tQty)}", 10F, false, 80F, "", Element.ALIGN_RIGHT, 1))
                srows.add(CTable(tbl))

                tbl = hashMapOf()
                val tweight = entityEo.items.sumByDouble {if(it.sld_total_weight == null) 0.00 else it.sld_total_weight!! }
                tbl.put(0, TCell(ctx!!.resources!!.getString(R.string.rpt_total_weight),10F,false,12F,"", Element.ALIGN_RIGHT,1, fontName = fontNameAr))
                tbl.put(1, TCell("${df2.format(tweight)}", 10F, false, 80F, "", Element.ALIGN_RIGHT, 1))
                srows.add(CTable(tbl))
                // row 2
                tbl = hashMapOf()
                tbl.put(0,TCell(ctx!!.resources!!.getString(R.string.rpt_total_amount),10F,false,12F,"", Element.ALIGN_RIGHT,1, fontName = fontNameAr))
                tbl.put(1,TCell("${df2.format(entityEo.sl_total_amount)}", 10F, false, 80F, "", Element.ALIGN_RIGHT, 1))
                srows.add(CTable(tbl))
                // row 3
                val tDiscount = if(entityEo.sl_total_discount == null) 0.00 else entityEo.sl_total_discount
                tbl = hashMapOf()
                tbl.put(0,TCell(ctx!!.resources!!.getString(R.string.rpt_total_discount),10F,false,12F,"",Element.ALIGN_RIGHT,1, fontName = fontNameAr))
                tbl.put(1,TCell("${df2.format(tDiscount)}",10F,false,80F,"", Element.ALIGN_RIGHT,1))
                srows.add(CTable(tbl))
                // row 4
                tbl = hashMapOf()
                tbl.put(0,TCell(ctx!!.resources!!.getString(R.string.rpt_net_amount),10F,false,12F,"", Element.ALIGN_RIGHT,1, fontName = fontNameAr ) )
                tbl.put(1,TCell("${df2.format(entityEo.sl_net_amount)}", 10F, false, 80F, "", Element.ALIGN_RIGHT, 1) )
                srows.add(CTable(tbl))

                val scw: java.util.ArrayList<Int> = arrayListOf(80, 20)
                summary.add(HeaderFooterRow(0, srows, null, cellsWidth = scw))

                summary.add(HeaderFooterRow(1, null, "T", fontSize = 20F, fontColor = BaseColor.WHITE))
                summary.add(HeaderFooterRow(2, null, "T", fontSize = 20F, fontColor = BaseColor.WHITE))
                summary.add(HeaderFooterRow(3, null, "T", fontSize = 20F, fontColor = BaseColor.WHITE))
                summary.add(HeaderFooterRow(4, null, "T", fontSize = 20F, fontColor = BaseColor.WHITE))
                srows = arrayListOf()
                tbl = hashMapOf()
                tbl.put(0, TCell(ctx!!.resources.getString(R.string.rpt_person_reciever_sig),10F,false,12F,"", Element.ALIGN_CENTER,0, fontName = fontNameAr ))
                tbl.put(1, TCell(ctx!!.resources.getString(R.string.rpt_storekeeper_sig),10F,false,12F, "",Element.ALIGN_CENTER,0, fontName = fontNameAr ))
                tbl.put(2, TCell(ctx!!.resources.getString(R.string.rpt_sales_manager_sig),10F,false,12F,"",  Element.ALIGN_CENTER,0, fontName = fontNameAr))
                srows.add(CTable(tbl))

                summary.add(HeaderFooterRow(5, srows, null, cellsWidth = arrayListOf(35, 35, 34)))

                GeneratePdf().createPdf(activity!!,imgLogo, entityEo.items, rowHeader, header, footer,null, summary, lang) { _, path ->
                    msgListener?.onSuccess("Pdf Created Successfully")
                    GeneratePdf().printPDF(activity!!, path)
                }
            } catch (e: Exception) {
                msgListener?.onFailure("Error Exception ${e.message}")
                e.printStackTrace()
            }
            isPrint = false

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
            header.add(ITextTicket(0, "Al-Nadir Trading Company", LineType.Text, xPos, yPos, uxPos, uyPos, PdfContentByte.ALIGN_CENTER, fontNameEn, BaseColor.BLACK, Font.NORMAL, 12F))
            header.add(ITextTicket(1, "Erbil Branch", LineType.Text, xPos, yPos - 12, uxPos, uyPos-12, PdfContentByte.ALIGN_CENTER, fontNameEn, BaseColor.BLACK, Font.NORMAL, 12F))
            header.add(ITextTicket(2, "Korek: 0750-7363286", LineType.Text, xPos, yPos - 24, uxPos, uyPos-24, PdfContentByte.ALIGN_CENTER, fontNameEn, BaseColor.BLACK, Font.NORMAL, 12F))
            header.add(ITextTicket(3, "Asia: 0770-6502228", LineType.Text, xPos, yPos-36, uxPos, uyPos-36, PdfContentByte.ALIGN_CENTER, fontNameEn, BaseColor.BLACK, Font.NORMAL, 12F))
            header.add(ITextTicket(4, "شركة النادر التجارية",LineType.Text, xPos, yPos-48, uxPos, uyPos-48, PdfContentByte.ALIGN_CENTER, fontNameAr, BaseColor.BLACK, Font.NORMAL, 12F))
            header.add(ITextTicket(5, "", LineType.Text, xPos, yPos-60, uxPos, yPos-60, PdfContentByte.ALIGN_CENTER, fontNameEn, BaseColor.BLACK, Font.NORMAL, 12F))
            header.add(ITextTicket(6, "Sale Invoice", LineType.Text, xPos, yPos-72, uxPos, uyPos-72, PdfContentByte.ALIGN_CENTER, fontNameEn, BaseColor.BLACK, Font.NORMAL, 12F))
            xPos = 10F
            uxPos = 100F
            yPos = 742F
            uyPos = 768F
            header.add(ITextTicket(13, "العملة:دينار", LineType.Text, xPos, yPos, uxPos, uyPos, PdfContentByte.ALIGN_CENTER, fontNameAr, BaseColor.BLACK, Font.NORMAL, 12F))
            header.add(ITextTicket(14, "نوع الفاتورة : اجل", LineType.Text, xPos , yPos - 12, uxPos , uyPos - 12, PdfContentByte.ALIGN_CENTER, fontNameAr, BaseColor.BLACK, Font.NORMAL, 12F))

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
            footer.add(ITextTicket(0, "موارد", LineType.Text, xPos, yPos, 0F, 0F, PdfContentByte.ALIGN_CENTER, fontNameAr, BaseColor.BLACK, Font.NORMAL, 12F))
            footer.add(ITextTicket(1, "الشركة الحديثة للبرامجيات الاتمتة المحدودة", LineType.Text, xPos, yPos - 12, 0F, 0F, PdfContentByte.ALIGN_CENTER, fontNameAr, BaseColor.BLACK, Font.NORMAL, 12F))

            footer.add(ITextTicket(2, "المستخدم", LineType.Text, xPos, yPos - 24, 0F, 0F, PdfContentByte.ALIGN_CENTER, fontNameAr, BaseColor.BLACK, Font.NORMAL, 12F))
            footer.add(ITextTicket(3, "${App.prefs.saveUser!!.user_name}", LineType.Text, xPos, yPos-36, 0F, 0F, PdfContentByte.ALIGN_CENTER, fontNameEn, BaseColor.BLACK, Font.NORMAL, 12F))
            // intArrayOf(20, 8, 8, 8, 5, 20, 10, 5)
            val rowHeader: ArrayList<ITextTicket> = arrayListOf()
            rowHeader.add(ITextTicket(0, "#", LineType.Text, 10F, 694F, 5F, 0F, PdfContentByte.ALIGN_CENTER, fontNameEn, BaseColor.BLACK, Font.NORMAL, 12F))
            rowHeader.add(ITextTicket(1, "Barcode", LineType.Text, 0F, 0F, 10F, 0F, PdfContentByte.ALIGN_CENTER, fontNameEn, BaseColor.BLACK, Font.NORMAL, 12F))
            rowHeader.add(ITextTicket(2, "Product Name", LineType.Text, 0F, 0F, 20F, 0F, PdfContentByte.ALIGN_CENTER, fontNameEn, BaseColor.BLACK, Font.NORMAL, 12F))
            rowHeader.add(ITextTicket(3, "Qty", LineType.Text, 0F, 0F, 5F, 0F, PdfContentByte.ALIGN_CENTER, fontNameEn, BaseColor.BLACK, Font.NORMAL, 12F))
            rowHeader.add(ITextTicket(4, "GitQty", LineType.Text, 0F, 0F, 8F, 0F, PdfContentByte.ALIGN_CENTER, fontNameEn, BaseColor.BLACK, Font.NORMAL, 12F))
            rowHeader.add(ITextTicket(5, "Unit Price", LineType.Text, 0F, 0F, 8F, 0F, PdfContentByte.ALIGN_CENTER, fontNameEn, BaseColor.BLACK, Font.NORMAL, 12F))
            rowHeader.add(ITextTicket(6, "Discount", LineType.Text, 0F, 0F, 8F, 0F, PdfContentByte.ALIGN_CENTER, fontNameEn, BaseColor.BLACK, Font.NORMAL, 12F))
            rowHeader.add(ITextTicket(7, "Net Total", LineType.Text, 0F, 0F, 8F, 0F, PdfContentByte.ALIGN_CENTER, fontNameEn, BaseColor.BLACK, Font.NORMAL, 12F))
            rowHeader.add(ITextTicket(8, "Notes", LineType.Text, 0F, 0F, 20F, 0F, PdfContentByte.ALIGN_CENTER, fontNameEn, BaseColor.BLACK, Font.NORMAL, 12F))


            // Summary part
            val tQty = entityEo.items.sumByDouble { it.sld_pack_qty!! }
             xPos = 100F
             yPos = 53F
            val summary: ArrayList<ITextTicket> = arrayListOf()
            summary.add(ITextTicket(0, "مجموع الكميات والوزن", LineType.Text, xPos, yPos, 0F, 0F, PdfContentByte.ALIGN_CENTER, fontNameEn, BaseColor.BLACK, Font.NORMAL, 12F))
            summary.add(ITextTicket(1, "${tQty}", LineType.Text, xPos + 100, yPos , 0F, 0F, PdfContentByte.ALIGN_CENTER, fontNameEn, BaseColor.BLACK, Font.NORMAL, 12F))

            summary.add(ITextTicket(2, "المجموع", LineType.Text, xPos, yPos - 12, 0F, 0F, PdfContentByte.ALIGN_CENTER, fontNameEn, BaseColor.BLACK, Font.NORMAL, 12F))
            summary.add(ITextTicket(3, "${entityEo.sl_total_amount}", LineType.Text, xPos + 100, yPos-12, 0F, 0F, PdfContentByte.ALIGN_CENTER, fontNameEn, BaseColor.BLACK, Font.NORMAL, 12F))

            summary.add(ITextTicket(4, "مجموع الخصومات", LineType.Text, xPos, yPos - 24, 0F, 0F, PdfContentByte.ALIGN_CENTER, fontNameEn, BaseColor.BLACK, Font.NORMAL, 12F))
            summary.add(ITextTicket(5, "${entityEo.sl_total_discount}", LineType.Text, xPos + 100, yPos-24, 0F, 0F, PdfContentByte.ALIGN_CENTER, fontNameEn, BaseColor.BLACK, Font.NORMAL, 12F))

            summary.add(ITextTicket(6, "الصافي للدفع", LineType.Text, xPos, yPos - 36, 0F, 0F, PdfContentByte.ALIGN_CENTER, fontNameEn, BaseColor.BLACK, Font.NORMAL, 12F))
            summary.add(ITextTicket(7, "${entityEo.sl_net_amount}", LineType.Text, xPos + 100, yPos-36, 0F, 0F, PdfContentByte.ALIGN_CENTER, fontNameEn, BaseColor.BLACK, Font.NORMAL, 12F))

            xPos = 10F
            yPos = 50F
            val signRow: ArrayList<ITextTicket> = arrayListOf()
            signRow.add(ITextTicket(0, "اسم وتوقيع المستلم", LineType.Text, xPos, yPos, 0F, 0F, PdfContentByte.ALIGN_CENTER, fontNameEn, BaseColor.BLACK, Font.NORMAL, 10F))
            signRow.add(ITextTicket(1, "اسم وتوقيع مدير المخازن", LineType.Text, xPos + 80, yPos, 0F, 0F, PdfContentByte.ALIGN_CENTER, fontNameEn, BaseColor.BLACK, Font.NORMAL, 10F))
            signRow.add(ITextTicket(1, "اسم وتوقيع مدير المبيعات", LineType.Text, xPos + 80, yPos , 0F, 0F, PdfContentByte.ALIGN_CENTER, fontNameEn, BaseColor.BLACK, Font.NORMAL, 10F))


            GeneratePdf1().createPdf(activity!!, entityEo.items, rowHeader, header, footer,null,  summary) { _, path ->
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