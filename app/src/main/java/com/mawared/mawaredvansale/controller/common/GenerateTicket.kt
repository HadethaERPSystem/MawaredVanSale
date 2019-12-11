package com.mawared.mawaredvansale.controller.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.text.TextUtilsCompat
import androidx.core.view.ViewCompat
import com.itextpdf.text.Element
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.data.db.entities.sales.Sale
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Items
import com.mawared.mawaredvansale.data.db.entities.sales.Transfer
import com.mawared.mawaredvansale.data.db.entities.sales.Transfer_Items
import org.threeten.bp.LocalTime
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class GenerateTicket(val ctx: Context, val lang: String){
    val res = ctx.resources
    fun create(baseEo: Sale, logo: Int?, systemInfo: String?, message: String?, cmsMessage: String?): List<Ticket>{
        val lines: ArrayList<Ticket> = arrayListOf()

        if(logo != null) {
            val bmp: Bitmap = BitmapFactory.decodeResource(res, logo)
            lines.add(Ticket(null, LineType.Image, AlignText.CENTER, bmp = bmp))
        }
        // number and date
        lines.add(Ticket("${res.getString(R.string.lbl_no)} ${baseEo.sl_refNo.toString().padEnd(24, ' ')} ${res.getString(R.string.lbl_doc_date)}: ${returnDateString(baseEo.sl_doc_date!!)}", LineType.Text))

        lines.add(Ticket("${res.getString(R.string.lbl_time).padStart(35,' ')} ${LocalTime.now()}", LineType.Text))

        lines.add(Ticket("${res.getString(R.string.lbl_customer_name)}: ${baseEo.sl_customer_name!!.padEnd(20, ' ')}", LineType.Text))

        lines.add(Ticket("".padEnd(48, 'ـ'), LineType.Text))
        // Lines title
        lines.add(Ticket(res.getString(R.string.lbl_prod_name).padEnd(34, ' ') + res.getString(R.string.lbl_qty).padEnd(6, ' ') + res.getString(R.string.lbl_line_total), LineType.Text))

        lines.add(Ticket("".padEnd(48, 'ـ'), LineType.Text))

        for (d: Sale_Items in baseEo.items){
            val prod = if(lang == "ar_iq") d.sld_prod_name_ar!!.trim().padEnd(34, ' ') else d.sld_prod_name!!.trim().padEnd(34, ' ')
            val qty = d.sld_unit_qty!!.toString().padEnd(6, ' ')
            val net = d.sld_net_total!!.toString()//.padEnd(7, ' ')
            val num = prod.length + qty.length + net.length
            val line = prod + qty + net + if(num < 47) "".padEnd(47-num) else ""
            lines.add(Ticket(line, LineType.Text, AlignText.LEFT))
        }

        lines.add(Ticket("".padEnd(48, 'ـ'), LineType.Text))

        lines.add(Ticket("${res.getString(R.string.lbl_total)} ".padStart(35, ' ') + if(baseEo.sl_total_amount == null) "0.00".padStart(10,' ') else baseEo.sl_total_amount.toString().padStart(10,' '), LineType.Text))

        lines.add(Ticket("${res.getString(R.string.lbl_total_discount)} ".padStart(35, ' ') + if(baseEo.sl_total_discount == null) "0.00".padStart(10,' ') else baseEo.sl_total_discount.toString().padStart(10,' '), LineType.Text))

        lines.add(Ticket("${res.getString(R.string.lbl_net_amount)} ".padStart(35, ' ') + if(baseEo.sl_net_amount == null) "0.00".padStart(10,' ') else baseEo.sl_net_amount.toString().padStart(10,' '), LineType.Text))

        lines.add(Ticket("".padEnd(48, 'ـ'), LineType.Text))

        if(!baseEo.sl_refNo.isNullOrEmpty()){
            lines.add(Ticket("\n", LineType.Text))
            lines.add(Ticket(baseEo.sl_refNo, LineType.Barcode, AlignText.CENTER, Attribute.LARGE_FONT_BOLD_NO_UNDERLINE_HIGHLIGHT))
            lines.add(Ticket("\n", LineType.Text))
        }

        if(!cmsMessage.isNullOrEmpty()){
            lines.add(Ticket(cmsMessage, LineType.Text))
            lines.add(Ticket("\n", LineType.Text))
        }

        if(!systemInfo.isNullOrEmpty()){
            lines.add(Ticket(systemInfo, LineType.Text))
            lines.add(Ticket("\n", LineType.Text))
        }

        return lines
    }

    fun create(baseEo: Transfer, logo: Int?, systemInfo: String?, message: String?, cmsMessage: String?): List<Ticket>{
        val lines: ArrayList<Ticket> = arrayListOf()

        if(logo != null) {
            val bmp: Bitmap = BitmapFactory.decodeResource(res, logo)
            lines.add(Ticket(null, LineType.Image, AlignText.CENTER, bmp = bmp))
        }
        // number and date
        lines.add(Ticket("${res.getString(R.string.lbl_no)} ${baseEo.tr_ref_no.toString().padEnd(24, ' ')} ${res.getString(R.string.lbl_doc_date)}: ${returnDateString(baseEo.tr_doc_date!!)}", LineType.Text))

        lines.add(Ticket("${res.getString(R.string.lbl_time).padStart(35,' ')} ${LocalTime.now()}", LineType.Text))

        lines.add(Ticket("${res.getString(R.string.lbl_to_warehouse)}: ${baseEo.tr_wr_name!!.padEnd(20, ' ')}", LineType.Text))

        lines.add(Ticket("".padEnd(48, 'ـ'), LineType.Text))
        // Lines title
        lines.add(Ticket(res.getString(R.string.lbl_prod_name).padEnd(34, ' ') + res.getString(R.string.lbl_qty).padEnd(6, ' ') + res.getString(R.string.lbl_line_total), LineType.Text))

        lines.add(Ticket("".padEnd(48, 'ـ'), LineType.Text))

        for (d: Transfer_Items in baseEo.items){
            val prod = if(lang == "ar_iq") d.trd_prod_name_ar!!.trim().padEnd(34, ' ') else d.trd_prod_name!!.trim().padEnd(34, ' ')
            val qty = d.trd_unit_qty!!.toString().padEnd(6, ' ')
            val num = prod.length + qty.length
            val line = prod + qty + if(num < 47) "".padEnd(47-num) else ""
            lines.add(Ticket(line, LineType.Text, AlignText.LEFT))
        }

        lines.add(Ticket("".padEnd(48, 'ـ'), LineType.Text))

        if(!baseEo.tr_ref_no.isNullOrEmpty()){
            lines.add(Ticket("\n", LineType.Text))
            lines.add(Ticket(baseEo.tr_ref_no, LineType.Barcode, AlignText.CENTER, Attribute.LARGE_FONT_BOLD_NO_UNDERLINE_HIGHLIGHT))
            lines.add(Ticket("\n", LineType.Text))
        }

        if(!cmsMessage.isNullOrEmpty()){
            lines.add(Ticket(cmsMessage, LineType.Text))
            lines.add(Ticket("\n", LineType.Text))
        }

        if(!systemInfo.isNullOrEmpty()){
            lines.add(Ticket(systemInfo, LineType.Text))
            lines.add(Ticket("\n", LineType.Text))
        }

        return lines
    }

    fun createPdfTicket(baseEo: Transfer, logo: Int?, systemInfo: String?, message: String?, cmsMessage: String?): List<PdfTicket>{
        val lines: ArrayList<PdfTicket> = arrayListOf()

        if(logo != null) {
            val bmp: Bitmap = BitmapFactory.decodeResource(res, logo)
            lines.add(PdfTicket(null, null, LineType.Image, Element.ALIGN_CENTER, bmp = bmp))
        }
        // number and date

        lines.add(PdfTicket("${res.getString(R.string.lbl_no)}", null, LineType.Text, Element.ALIGN_LEFT, "assets/fonts/brandon_medium.otf", styleType = "L"))
        lines.add(PdfTicket("${baseEo.tr_ref_no.toString()}", null, LineType.Text, Element.ALIGN_LEFT, "assets/fonts/brandon_medium.otf", styleType = "V"))

        lines.add(PdfTicket("", null, LineType.NewLine))

        lines.add(PdfTicket("${res.getString(R.string.lbl_doc_date)}", null, LineType.Text, Element.ALIGN_LEFT, "assets/fonts/brandon_medium.otf", styleType = "L"))
        lines.add(PdfTicket("${returnDateString(baseEo.tr_doc_date!!)}", null, LineType.Text, Element.ALIGN_LEFT, "assets/fonts/brandon_medium.otf", styleType = "V"))


        lines.add(PdfTicket("", null, LineType.NewLine))

        lines.add(PdfTicket("${res.getString(R.string.lbl_time)}", null, LineType.Text, Element.ALIGN_LEFT, "assets/fonts/brandon_medium.otf", styleType = "L"))
        lines.add(PdfTicket("${LocalTime.now()}", null, LineType.Text, Element.ALIGN_LEFT, "assets/fonts/brandon_medium.otf", styleType = "V"))

        lines.add(PdfTicket("", null, LineType.NewLine))

        lines.add(PdfTicket("${res.getString(R.string.lbl_to_warehouse)}", null, LineType.Text, Element.ALIGN_LEFT, "assets/fonts/brandon_medium.otf"))
        lines.add(PdfTicket("${baseEo.tr_wr_name!!}", null, LineType.Text, Element.ALIGN_LEFT, "assets/fonts/brandon_medium.otf"))

        lines.add(PdfTicket("", null, LineType.LineSeparator))

        var cols: HashMap<Int, Column> = HashMap()
        val rows: ArrayList<HashMap<Int, Column>> = arrayListOf()

        // Lines title
        cols.put(0, Column("${res.getString(R.string.lbl_prod_name)}", Element.ALIGN_LEFT, 30, "assets/fonts/brandon_medium.otf"))
        cols.put(1, Column("${res.getString(R.string.lbl_barcode)}", Element.ALIGN_LEFT, 30, "assets/fonts/brandon_medium.otf"))
        cols.put(2, Column("${res.getString(R.string.lbl_qty)}", Element.ALIGN_LEFT, 30, "assets/fonts/brandon_medium.otf"))
        rows.add(cols)

        for (d: Transfer_Items in baseEo.items){
            cols = HashMap()
            val prod = if(lang == "ar_iq") d.trd_prod_name_ar!! else d.trd_prod_name!!.trim()

            cols.put(0, Column(prod, Element.ALIGN_LEFT, 30, "assets/fonts/brandon_medium.otf"))
            cols.put(1, Column("${d.trd_barcode}", Element.ALIGN_LEFT, 30, "assets/fonts/brandon_medium.otf"))
            cols.put(2, Column("${d.trd_unit_qty!!}", Element.ALIGN_LEFT, 30, "assets/fonts/brandon_medium.otf"))
            rows.add(cols)
        }

        lines.add(PdfTicket(null, rows, LineType.Column ))

        if(!baseEo.tr_ref_no.isNullOrEmpty()){
            lines.add(PdfTicket(baseEo.tr_ref_no, null, LineType.Barcode, Element.ALIGN_CENTER))
            lines.add(PdfTicket("", null, LineType.NewLine))
        }

        if(!cmsMessage.isNullOrEmpty()){
            lines.add(PdfTicket(cmsMessage, null, LineType.Text, Element.ALIGN_LEFT))
            lines.add(PdfTicket("", null, LineType.NewLine))
        }

        if(!systemInfo.isNullOrEmpty()){
            lines.add(PdfTicket(systemInfo, null, LineType.Text, Element.ALIGN_LEFT))
            lines.add(PdfTicket("", null, LineType.NewLine))
        }

        return lines
    }

    fun createPdf(baseEo: Transfer, logo: Int?, systemInfo: String?, message: String?, cmsMessage: String?): Template{
//        val lines: ArrayList<PdfTicket> = arrayListOf()
//
//        if(logo != null) {
//            val bmp: Bitmap = BitmapFactory.decodeResource(res, logo)
//            lines.add(PdfTicket(null, null, LineType.Image, Element.ALIGN_CENTER, bmp = bmp))
//        }
        // number and date

        var template = Template()
        var cells: HashMap<Int, Column> = HashMap()

        cells.put(0, Column("${res.getString(R.string.lbl_no)}",Element.ALIGN_LEFT, 30, "assets/fonts/brandon_medium.otf", styleType = "L"))
        cells.put(0, Column("${baseEo.tr_ref_no.toString()}",Element.ALIGN_LEFT, 30, "assets/fonts/brandon_medium.otf", styleType = "V"))

        template.header!!.add(PdfHeader(cells))

        cells = HashMap()
        cells.put(0, Column("${res.getString(R.string.lbl_doc_date)}",Element.ALIGN_LEFT, 30, "assets/fonts/brandon_medium.otf", styleType = "L"))
        cells.put(0, Column("${returnDateString(baseEo.tr_doc_date!!)}",Element.ALIGN_LEFT, 30, "assets/fonts/brandon_medium.otf", styleType = "V"))

        template.header!!.add(PdfHeader(cells))

        cells = HashMap()
        cells.put(0, Column("${res.getString(R.string.lbl_time)}",Element.ALIGN_LEFT, 30, "assets/fonts/brandon_medium.otf", styleType = "L"))
        cells.put(0, Column("${LocalTime.now()}",Element.ALIGN_LEFT, 30, "assets/fonts/brandon_medium.otf", styleType = "V"))

        template.header!!.add(PdfHeader(cells))


        cells = HashMap()
        cells.put(0, Column("${res.getString(R.string.lbl_to_warehouse)}",Element.ALIGN_LEFT, 30, "assets/fonts/brandon_medium.otf", styleType = "L"))
        cells.put(0, Column("${baseEo.tr_wr_name!!}",Element.ALIGN_LEFT, 30, "assets/fonts/brandon_medium.otf", styleType = "V"))

        template.header!!.add(PdfHeader(cells))

        var cols: HashMap<Int, Column> = HashMap()
        val rows: ArrayList<HashMap<Int, Column>> = arrayListOf()

        // Lines title
        cols.put(0, Column("${res.getString(R.string.lbl_prod_name)}", Element.ALIGN_LEFT, 30, "assets/fonts/brandon_medium.otf", styleType = "L", boderWidth = 1f))
        cols.put(1, Column("${res.getString(R.string.lbl_barcode)}", Element.ALIGN_LEFT, 30, "assets/fonts/brandon_medium.otf", styleType = "L", boderWidth = 1f))
        cols.put(2, Column("${res.getString(R.string.lbl_qty)}", Element.ALIGN_LEFT, 30, "assets/fonts/brandon_medium.otf", styleType = "L", boderWidth = 1f))
        rows.add(cols)

        for (d: Transfer_Items in baseEo.items){
            cols = HashMap()
            val prod = if(lang == "ar_iq") d.trd_prod_name_ar!! else d.trd_prod_name!!.trim()

            cols.put(0, Column(prod, Element.ALIGN_LEFT, 30, "assets/fonts/brandon_medium.otf", styleType = "V", boderWidth = 1f))
            cols.put(1, Column("${d.trd_barcode}", Element.ALIGN_LEFT, 30, "assets/fonts/brandon_medium.otf", styleType = "V", boderWidth = 1f))
            cols.put(2, Column("${d.trd_unit_qty!!}", Element.ALIGN_LEFT, 30, "assets/fonts/brandon_medium.otf", styleType = "V", boderWidth = 1f))
            rows.add(cols)
        }

        //lines.add(PdfTicket(null, rows, LineType.Column ))
        template.body = PdfBody()
        template.body!!.rows = rows

        if(!baseEo.tr_ref_no.isNullOrEmpty()){

            cells = HashMap()
            cells.put(0, Column("",Element.ALIGN_LEFT, 30, "assets/fonts/brandon_medium.otf", styleType = "L"))
            cells.put(0, Column("${res.getString(R.string.lbl_to_warehouse)}",Element.ALIGN_LEFT, 30, "assets/fonts/brandon_medium.otf", styleType = "L"))
            cells.put(0, Column("",Element.ALIGN_LEFT, 30, "assets/fonts/brandon_medium.otf", styleType = "L"))
            template.footer!!.add(PdfFooter(cells))
        }

        if(!cmsMessage.isNullOrEmpty()){
            cells = HashMap()
            cells.put(0, Column(cmsMessage,Element.ALIGN_LEFT, 30, "assets/fonts/brandon_medium.otf", styleType = "L"))
            template.footer!!.add(PdfFooter(cells))
        }

        if(!systemInfo.isNullOrEmpty()){
            cells = HashMap()
            cells.put(0, Column(systemInfo, Element.ALIGN_LEFT, 30, "assets/fonts/brandon_medium.otf", styleType = "L"))
            template.footer!!.add(PdfFooter(cells))
        }

        return template
    }

    fun returnDateString(isoString: String) : String{
        // 2017-09-11T01:16:13.858Z converted to below
        // Monday 4:35 PM format "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        val isLeftToRight = TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == ViewCompat.LAYOUT_DIRECTION_LTR
        val pattern: String = if(isLeftToRight) "dd-MM-yyyy" else "yyyy-MM-dd"
        val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        isoFormatter.timeZone = TimeZone.getTimeZone("UTC")
        var convertedDate = Date()
        try {
            convertedDate = isoFormatter.parse(isoString)
        }catch (e: ParseException){
            Log.d("PARSE", "Cannot parse date")
        }

        val outDateString = SimpleDateFormat(pattern, Locale.getDefault())
        return  outDateString.format(convertedDate)
    }

}
