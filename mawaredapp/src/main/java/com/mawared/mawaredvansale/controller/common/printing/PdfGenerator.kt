package com.mawared.mawaredvansale.controller.common.printing

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.print.PrintAttributes
import android.print.PrintManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.itextpdf.text.*
import com.itextpdf.text.pdf.*
import com.itextpdf.text.pdf.draw.LineSeparator
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.controller.common.Common
import com.mawared.mawaredvansale.controller.common.PdfDocumentAdapter
import com.mawared.mawaredvansale.data.db.entities.fms.Receivable
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Items
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


class MawaredPdf: PdfPageEventHelper(){
    // variable
    companion object{
        var pageNumber: Int = 0

        var headerRow: HashMap<Int, RowHeader>? = null
        var pageHeader:ArrayList<HeaderFooterRow>? = null
        var pageFooter: ArrayList<HeaderFooterRow>? = null
        var pageFilter: HashMap<String, PageFilter>? = null
        var summary: ArrayList<HeaderFooterRow>? = null

        var workArea: Float = 0f
        var yPos: Float = 0F
        var yPosForSummary: Float = 0F
        var footerHeight = 90F
        var logo: RepLogo? = null
        var isRTL: Boolean = false
        var pageSize = PageSize.A4
        var isPrintHeader = true
        var isPrintFooter = true
    }

    // override function

    fun setConfig(mlogo: RepLogo?, _rowHeader: HashMap<Int, RowHeader>?, _pageHeader: ArrayList<HeaderFooterRow>?,
                  _pageFooter: ArrayList<HeaderFooterRow>?, _reportFitler: HashMap<String, PageFilter>?, _summary: ArrayList<HeaderFooterRow>?, _workArea: Float, _isRTL: Boolean, _pageSize: Rectangle): MawaredPdf{
        val pdf = MawaredPdf()
        logo = mlogo
        headerRow = _rowHeader
        pageHeader = _pageHeader
        pageFooter = _pageFooter
        pageFilter = _reportFitler
        workArea = _workArea
        summary = _summary
        isRTL = _isRTL
        pageSize = _pageSize
        return pdf
    }

    fun writeBody(writer: PdfWriter, document: Document, rows: List<Sale_Items>){
        isPrintHeader = true
        isPrintFooter = true
        val table = PdfPTable(headerRow!!.count())
        document.open()
        var rowNo = 1
        var pageRows = 1
        val cellsWidth : ArrayList<Int> = arrayListOf() // intArrayOf(20, 8, 8, 8, 5, 20, 10, 5)
        table.spacingAfter = 10F
        table.defaultCell.verticalAlignment = Element.ALIGN_CENTER
        if(isRTL) {
            table.runDirection = PdfWriter.RUN_DIRECTION_RTL
            for (c in headerRow!!.toSortedMap(compareByDescending { it })){
                cellsWidth.add(c.value.cellWidth)
            }
        }else{
            for (c in headerRow!!.toSortedMap(compareBy { it })){
                cellsWidth.add(c.value.cellWidth)
            }
        }

        table.totalWidth = workArea
        table.widthPercentage = 100F

        if(cellsWidth.size > 0) {
            table.setWidths(cellsWidth.toIntArray())
        }


        val mBreakPage = writer.pageSize.bottom + footerHeight
        val mCenterOfBody = (document.pageSize.width / 2) - (table.totalWidth / 2)
        val mRowsCount = rows.count()
        val df = DecimalFormat("#,###,###.###")
        val df1 = DecimalFormat("#,###")
        val fontName = BaseFont.createFont(headerRow!![0]!!.fontName, BaseFont.IDENTITY_H, true)
        val fontStyle = Font(fontName, headerRow!![0]!!.fontSize, headerRow!![0]!!.fontWeight, BaseColor.BLACK)
        while (true){
            for(c in headerRow!!.toSortedMap()){
                //val fontName = BaseFont.createFont(c.value.fontName, BaseFont.IDENTITY_H, true)
                //val fontStyle = Font(fontName, c.value.fontSize, c.value.fontWeight, BaseColor.BLACK)
                val cell = PdfPCell(Phrase(9F, c.value.cellName, fontStyle))
                cell.horizontalAlignment = c.value.align
                cell.backgroundColor = BaseColor.LIGHT_GRAY
                cell.paddingBottom = 5F
                cell.paddingTop = 5F
                table.addCell(cell)
            }
            for (r in rows){
                // Col = 0
                var cell = PdfPCell(Phrase(9F, rowNo.toString(), fontStyle))
                cell.horizontalAlignment = Element.ALIGN_CENTER

                table.addCell(cell)

                // Col = 1
                cell = PdfPCell(Phrase(9F, r.sld_barcode, fontStyle))
                cell.paddingTop = 4F
                cell.paddingBottom = 4F
                cell.horizontalAlignment = Element.ALIGN_TOP
                cell.verticalAlignment = Element.ALIGN_CENTER
                cell.runDirection = PdfWriter.RUN_DIRECTION_RTL
                table.addCell(cell)
                var pr_name = r.sld_prod_name
                if(isRTL){
                    pr_name = r.sld_prod_name_ar
                }

                // Col = 2
                cell = PdfPCell(Phrase(9F, pr_name, fontStyle))
                cell.setPadding(5F)
                if(isRTL){
                    cell.runDirection = PdfWriter.RUN_DIRECTION_RTL
                    cell.horizontalAlignment = Element.ALIGN_LEFT
                }else{
                    cell.horizontalAlignment = Element.ALIGN_LEFT
                }

                cell.verticalAlignment = Element.ALIGN_TOP
                table.addCell(cell)
                val cellHeight = cell.calculatedHeight
                // Col = 3
                val qty = r.sld_pack_qty!! - (r.sld_gift_qty ?: 0.0)
                cell = PdfPCell(Phrase(9F, df1.format(qty), fontStyle))
                cell.setPadding(5F)
                cell.runDirection = PdfWriter.RUN_DIRECTION_RTL
                cell.horizontalAlignment = Element.ALIGN_LEFT
                cell.verticalAlignment = Element.ALIGN_TOP
                table.addCell(cell)

                // Uom
                // Col = 4
                cell = PdfPCell(Phrase(9F, r.sld_uom_name, fontStyle))
                cell.setPadding(5F)
                if(isRTL){
                    cell.runDirection = PdfWriter.RUN_DIRECTION_RTL
                    cell.horizontalAlignment = Element.ALIGN_LEFT
                }else{
                    cell.horizontalAlignment = Element.ALIGN_LEFT
                }

                cell.verticalAlignment = Element.ALIGN_TOP
                table.addCell(cell)
                // Col = 5
                cell = PdfPCell(Phrase(9F, df1.format(r.sld_gift_qty), fontStyle))
                cell.setPadding(5F)
                cell.runDirection = PdfWriter.RUN_DIRECTION_RTL
                cell.horizontalAlignment = Element.ALIGN_LEFT
                cell.verticalAlignment = Element.ALIGN_TOP
                table.addCell(cell)
                // Col = 6
                cell = PdfPCell(Phrase(9F, df.format(r.sld_unit_price), fontStyle))
                cell.setPadding(5F)
                cell.runDirection = PdfWriter.RUN_DIRECTION_RTL
                cell.horizontalAlignment = Element.ALIGN_LEFT
                cell.verticalAlignment = Element.ALIGN_TOP
                table.addCell(cell)
                // Col = 7
//                cell = PdfPCell(Phrase(9F, df.format(r.sld_dis_value), fontStyle))
//                cell.setPadding(5F)
//                cell.runDirection = PdfWriter.RUN_DIRECTION_RTL
//                cell.horizontalAlignment = Element.ALIGN_LEFT
//                cell.verticalAlignment = Element.ALIGN_TOP
//                table.addCell(cell)

                // Col = 8
                cell = PdfPCell(Phrase(9F, df.format(r.sld_net_total), fontStyle))
                cell.setPadding(5F)
                cell.runDirection = PdfWriter.RUN_DIRECTION_RTL
                cell.horizontalAlignment = Element.ALIGN_LEFT
                cell.verticalAlignment = Element.ALIGN_TOP
                table.addCell(cell)
                // Col = 9
                cell = PdfPCell(Phrase(9F, "", fontStyle))
                cell.setPadding(5F)
                cell.runDirection = PdfWriter.RUN_DIRECTION_RTL
                cell.horizontalAlignment = Element.ALIGN_RIGHT
                cell.verticalAlignment = Element.ALIGN_TOP
                table.addCell(cell)

                // increase page number
                pageRows++
                // increase Row number
                rowNo++

                val mHeightOfTable = table.calculateHeights() + cellHeight
                if((yPos - mHeightOfTable) <= mBreakPage){
                    break
                }

            }// end rows
            yPosForSummary = table.writeSelectedRows(0, -1, mCenterOfBody, yPos, writer.directContent)
            yPos = yPosForSummary
            // check if end of page or not
            if(pageRows < mRowsCount){
                // write the rows in the pdf Page
                document.newPage()
                table.flushContent()
            }else{
                break
            }
        } // end while

        // Print Total In Last Page
        if(summary != null){
            showSummary(writer, document, mCenterOfBody)
        }
        document.close()
    }

    private fun printHeader(writer: PdfWriter?, baseEo: Receivable, ypos: Float) {
        val fontName = BaseFont.createFont(pageHeader!![2].fontName, BaseFont.IDENTITY_H, true)
        val fontStyle = Font(fontName, 10F, Font.NORMAL, BaseColor.BLACK)
        var table = PdfPTable(2)
        table.spacingAfter = 10F
        table.totalWidth = pageSize.width
        table.defaultCell.verticalAlignment = Element.ALIGN_CENTER
        table.runDirection = PdfWriter.RUN_DIRECTION_RTL
        table.widthPercentage = 100F
        table.setWidths(intArrayOf(30, 200))

        val tbl = PdfPTable(1)
        tbl.totalWidth = 200F
        tbl.runDirection = PdfWriter.RUN_DIRECTION_RTL
        tbl.widthPercentage = 100F
        tbl.setWidths(intArrayOf(200))
        var p = Paragraph()
        var c = PdfPCell(Phrase(App.prefs.saveUser!!.client_name, fontStyle))
        c.horizontalAlignment = Element.ALIGN_CENTER
        c.border = -1
        c.horizontalAlignment = Element.ALIGN_CENTER
        c.verticalAlignment = Element.ALIGN_CENTER
        c.runDirection = PdfWriter.RUN_DIRECTION_RTL
        tbl.addCell(c)

        c = PdfPCell(Phrase("", fontStyle))
        c.horizontalAlignment = Element.ALIGN_CENTER
        c.border = -1
        c.horizontalAlignment = Element.ALIGN_CENTER
        c.verticalAlignment = Element.ALIGN_CENTER
        c.runDirection = PdfWriter.RUN_DIRECTION_RTL
        tbl.addCell(c)

        c = PdfPCell(Phrase(baseEo.rcv_org_name, fontStyle))
        c.horizontalAlignment = Element.ALIGN_CENTER
        c.border = -1
        c.horizontalAlignment = Element.ALIGN_CENTER
        c.verticalAlignment = Element.ALIGN_CENTER
        c.runDirection = PdfWriter.RUN_DIRECTION_RTL
        tbl.addCell(c)

        c = PdfPCell(Phrase(baseEo.rcv_org_phone, fontStyle))
        c.horizontalAlignment = Element.ALIGN_CENTER
        c.border = -1
        c.horizontalAlignment = Element.ALIGN_CENTER
        c.verticalAlignment = Element.ALIGN_CENTER
        c.runDirection = PdfWriter.RUN_DIRECTION_RTL
        tbl.addCell(c)

        val cel = PdfPCell(tbl)
        cel.border = -1
        table.addCell(cel)

        val cb = writer!!.directContent
        if(logo!!.bmp != null) {
            val stream = ByteArrayOutputStream()
            logo!!.bmp!!.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val image: Image = Image.getInstance(stream.toByteArray())
            image.scaleAbsolute(120F, 110F)
            image.setAbsolutePosition(2f, 2f)
            val cell = PdfPCell()
            cell.horizontalAlignment = Element.ALIGN_CENTER
            cell.verticalAlignment = Element.ALIGN_CENTER
            cell.addElement(image)

            cell.paddingTop = 4F
            cell.paddingBottom = 4F
            cell.border = -1
            cell.horizontalAlignment = Element.ALIGN_CENTER
            cell.verticalAlignment = Element.ALIGN_CENTER
            cell.runDirection = PdfWriter.RUN_DIRECTION_RTL
            table.addCell(cell)
        }else{
            val cell = PdfPCell()
            cell.horizontalAlignment = Element.ALIGN_CENTER
            cell.verticalAlignment = Element.ALIGN_CENTER

            cell.paddingTop = 4F
            cell.paddingBottom = 4F
            cell.border = -1
            cell.horizontalAlignment = Element.ALIGN_CENTER
            cell.verticalAlignment = Element.ALIGN_CENTER
            cell.runDirection = PdfWriter.RUN_DIRECTION_RTL
            table.addCell(cell)
        }
        yPos =  table.writeSelectedRows(0, -1, 5F, ypos, writer.directContent)
    }

    fun writeBody(writer: PdfWriter, document: Document, baseEo: Receivable){
        isPrintHeader = false
        isPrintFooter = false
        document.open()
        val cb: PdfContentByte = writer.directContent

        printHeader(writer, baseEo, 840F)
        yPos = pageSize.height - 100
        printDetail(writer, baseEo)

        cb.setLineWidth(1f)
        cb.moveTo(30F, yPos - 25)
        cb.lineTo(570F, yPos - 25)
        cb.stroke()

        printHeader(writer, baseEo, yPos - 50)

        printDetail(writer, baseEo)

        document.close()
    }

    private fun printDetail(writer: PdfWriter, baseEo: Receivable){
        val df = DecimalFormat("#,###,###.###")
        val df1 = DecimalFormat("#,###")

        var table = PdfPTable(4)
        table.spacingAfter = 10F
        table.totalWidth = pageSize.width - 40
        table.defaultCell.verticalAlignment = Element.ALIGN_CENTER
        table.runDirection = PdfWriter.RUN_DIRECTION_RTL
        table.widthPercentage = 95F
        table.setWidths(intArrayOf(120,50, 30, 30))

        val fontName = BaseFont.createFont(pageHeader!![2].fontName, BaseFont.IDENTITY_H, true)
        val fontStyle = Font(fontName, 10F, Font.NORMAL, BaseColor.BLACK)
        // Row 1
        var cell = PdfPCell(Phrase(9F, "${baseEo.rcv_cr_name} - ${baseEo.rcv_cr_symbol}", fontStyle))
        cell.paddingTop = 8F
        cell.paddingBottom = 8F
        cell.border = -1
        cell.horizontalAlignment = Element.ALIGN_CENTER
        cell.verticalAlignment = Element.ALIGN_CENTER
        cell.runDirection = PdfWriter.RUN_DIRECTION_RTL
        table.addCell(cell)

        cell = PdfPCell(Phrase(9F, "${baseEo.rcv_lc_cr_name} - ${baseEo.rcv_lc_cr_symbol}", fontStyle))
        cell.paddingTop = 8F
        cell.paddingBottom = 8F
        cell.border = -1
        cell.horizontalAlignment = Element.ALIGN_CENTER
        cell.verticalAlignment = Element.ALIGN_CENTER
        cell.runDirection = PdfWriter.RUN_DIRECTION_RTL
        table.addCell(cell)

        cell = PdfPCell(Phrase(9F, "التاريخ", fontStyle))
        cell.paddingTop = 8F
        cell.paddingBottom = 8F
        cell.border = -1
        cell.horizontalAlignment = Element.ALIGN_RIGHT
        cell.verticalAlignment = Element.ALIGN_CENTER
        cell.runDirection = PdfWriter.RUN_DIRECTION_RTL
        table.addCell(cell)

        cell = PdfPCell(Phrase(9F, "${baseEo.rcv_doc_date}", fontStyle))
        cell.paddingTop = 8F
        cell.paddingBottom = 8F
        cell.border = -1
        cell.horizontalAlignment = Element.ALIGN_LEFT
        cell.verticalAlignment = Element.ALIGN_CENTER
        cell.runDirection = PdfWriter.RUN_DIRECTION_RTL
        table.addCell(cell)

        // Row 2
        var amount_bc = 0.00
        var amount_lc = 0.00
        if(baseEo.rcv_amount != null){
            amount_bc = baseEo.rcv_amount!!
        }
        if(baseEo.rcv_change != null){
            amount_bc -= baseEo.rcv_change!!
        }
        if(baseEo.rcv_lc_amount != null){
            amount_lc = baseEo.rcv_lc_amount!!
        }
        if(baseEo.rcv_lc_change != null){
            amount_lc -= baseEo.rcv_lc_change!!
        }

        cell = PdfPCell(Phrase(9F, "${amount_bc}", fontStyle))
        cell.paddingTop = 8F
        cell.paddingBottom = 8F
        cell.horizontalAlignment = Element.ALIGN_CENTER
        cell.verticalAlignment = Element.ALIGN_CENTER
        cell.runDirection = PdfWriter.RUN_DIRECTION_RTL
        table.addCell(cell)

        cell = PdfPCell(Phrase(9F, "${amount_lc}", fontStyle))
        cell.paddingTop = 8F
        cell.paddingBottom = 8F
        cell.horizontalAlignment = Element.ALIGN_CENTER
        cell.verticalAlignment = Element.ALIGN_CENTER
        cell.runDirection = PdfWriter.RUN_DIRECTION_RTL
        table.addCell(cell)

        cell = PdfPCell(Phrase(9F, "الرقم:", fontStyle))
        cell.paddingTop = 8F
        cell.paddingBottom = 8F
        cell.border = -1
        cell.horizontalAlignment = Element.ALIGN_RIGHT
        cell.verticalAlignment = Element.ALIGN_CENTER
        cell.runDirection = PdfWriter.RUN_DIRECTION_RTL
        table.addCell(cell)

        cell = PdfPCell(Phrase(9F, " ${baseEo.rcv_ref_no}", fontStyle))
        cell.paddingTop = 8F
        cell.paddingBottom = 8F
        cell.border = -1
        cell.horizontalAlignment = Element.ALIGN_LEFT
        cell.verticalAlignment = Element.ALIGN_CENTER
        cell.runDirection = PdfWriter.RUN_DIRECTION_RTL
        table.addCell(cell)

        yPos = table.writeSelectedRows(0, -1, 10F, yPos, writer.directContent)

        // New table without border
        // print name, amount and notes
        table = PdfPTable(2)
        table.totalWidth = pageSize.width - 40
        table.spacingAfter = 10F
        table.defaultCell.verticalAlignment = Element.ALIGN_CENTER
        table.runDirection = PdfWriter.RUN_DIRECTION_RTL
        table.widthPercentage = 100F
        table.setWidths(intArrayOf(150,20))
        // Row 1
        cell = PdfPCell(Phrase(9F, "استلمت من السيد :", fontStyle))
        cell.paddingTop = 8F
        cell.paddingBottom = 8F
        cell.border = -1
        cell.horizontalAlignment = Element.ALIGN_LEFT
        cell.verticalAlignment = Element.ALIGN_CENTER
        cell.runDirection = PdfWriter.RUN_DIRECTION_RTL
        table.addCell(cell)

        cell = PdfPCell(Phrase(9F, "${baseEo.rcv_cu_name}", fontStyle))
        cell.paddingTop = 8F
        cell.paddingBottom = 8F
        cell.border = 0
        cell.horizontalAlignment = Element.ALIGN_LEFT
        cell.verticalAlignment = Element.ALIGN_CENTER
        cell.runDirection = PdfWriter.RUN_DIRECTION_RTL
        table.addCell(cell)

        // Row 2
        cell = PdfPCell(Phrase(9F, "المبلغ :", fontStyle))
        cell.paddingTop = 8F
        cell.paddingBottom = 8F
        cell.border = -1
        cell.horizontalAlignment = Element.ALIGN_LEFT
        cell.verticalAlignment = Element.ALIGN_CENTER
        cell.runDirection = PdfWriter.RUN_DIRECTION_RTL
        table.addCell(cell)

        val str1 =  if(amount_bc > 0) "${amount_bc} ${baseEo.rcv_cr_name} - ${baseEo.rcv_cr_symbol}" else ""
        val str2 =  if(amount_lc > 0) "${amount_lc} ${baseEo.rcv_lc_cr_name} - ${baseEo.rcv_lc_cr_symbol}" else ""
        cell = PdfPCell(Phrase(9F, "${str1}  ${str2}", fontStyle))
        cell.paddingTop = 8F
        cell.paddingBottom = 8F
        cell.border = 0
        cell.horizontalAlignment = Element.ALIGN_LEFT
        cell.verticalAlignment = Element.ALIGN_CENTER
        cell.runDirection = PdfWriter.RUN_DIRECTION_RTL
        table.addCell(cell)

        // Row 3
        cell = PdfPCell(Phrase(9F, "وذلك عن :", fontStyle))
        cell.paddingTop = 8F
        cell.paddingBottom = 8F
        cell.border = -1
        cell.horizontalAlignment = Element.ALIGN_LEFT
        cell.verticalAlignment = Element.ALIGN_CENTER
        cell.runDirection = PdfWriter.RUN_DIRECTION_RTL
        table.addCell(cell)


        cell = PdfPCell(Phrase(9F, "${baseEo.rcv_comment}", fontStyle))
        cell.paddingTop = 8F
        cell.paddingBottom = 8F
        cell.border= 0
        cell.horizontalAlignment = Element.ALIGN_LEFT
        cell.verticalAlignment = Element.ALIGN_CENTER
        cell.runDirection = PdfWriter.RUN_DIRECTION_RTL
        table.addCell(cell)

        yPos = table.writeSelectedRows(0, -1, 10F, yPos - 20, writer.directContent)

        table = PdfPTable(2)
        table.totalWidth = pageSize.width
        table.spacingAfter = 10F
        table.defaultCell.verticalAlignment = Element.ALIGN_CENTER
        table.runDirection = PdfWriter.RUN_DIRECTION_RTL
        table.widthPercentage = 100F
        table.setWidths(intArrayOf(50,50))
        // Row 1
        cell = PdfPCell(Phrase(9F, "توقيع المستلم", fontStyle))
        cell.paddingTop = 4F
        cell.paddingBottom = 4F
        cell.border = -1
        cell.horizontalAlignment = Element.ALIGN_CENTER
        cell.verticalAlignment = Element.ALIGN_CENTER
        cell.runDirection = PdfWriter.RUN_DIRECTION_RTL
        table.addCell(cell)

        cell = PdfPCell(Phrase(9F, "توقيع المسلم", fontStyle))
        cell.paddingTop = 4F
        cell.paddingBottom = 4F
        cell.border = -1
        cell.horizontalAlignment = Element.ALIGN_CENTER
        cell.verticalAlignment = Element.ALIGN_CENTER
        cell.runDirection = PdfWriter.RUN_DIRECTION_RTL
        table.addCell(cell)

        yPos = table.writeSelectedRows(0, -1, 10F, yPos - 50, writer.directContent)

        table = PdfPTable(1)
        table.totalWidth = pageSize.width - 100
        table.spacingAfter = 10F
        table.defaultCell.verticalAlignment = Element.ALIGN_CENTER
        table.runDirection = PdfWriter.RUN_DIRECTION_RTL
        table.widthPercentage = 50F
        table.setWidths(intArrayOf(50))

        cell = PdfPCell(Phrase(9F, "${baseEo.created_by}", fontStyle))
        cell.paddingTop = 4F
        cell.paddingBottom = 4F
        cell.border = -1
        cell.horizontalAlignment = Element.ALIGN_RIGHT
        cell.verticalAlignment = Element.ALIGN_RIGHT
        cell.runDirection = PdfWriter.RUN_DIRECTION_RTL
        table.addCell(cell)

        cell = PdfPCell(Phrase(9F, "موارد" + " - " + "الشركة الحديثة للبرامجيات الاتمتة المحدودة", fontStyle))
        cell.paddingTop = 4F
        cell.paddingBottom = 4F
        cell.border = -1
        cell.horizontalAlignment = Element.ALIGN_RIGHT
        cell.verticalAlignment = Element.ALIGN_RIGHT
        cell.runDirection = PdfWriter.RUN_DIRECTION_RTL
        table.addCell(cell)

        yPos = table.writeSelectedRows(0, -1, 10F, yPos - 30, writer.directContent)
    }
    private fun showSummary(writer: PdfWriter, document: Document, xPos: Float){

        for (h in summary!!){
            if(h.rows == null){
                //addLineSpace(document)
                val table = PdfPTable(1)
                table.totalWidth = workArea
                if(isRTL) table.runDirection = PdfWriter.RUN_DIRECTION_RTL
                table.setWidths(intArrayOf(100))
                val fontName = BaseFont.createFont(h.fontName, BaseFont.IDENTITY_H, true)
                val fontStyle = Font(fontName, h.fontSize, h.fontWeight, h.fontColor)
                //val font: Font = FontFactory.getFont(h.fontName, BaseFont.CP1252,true,22F, Font.BOLD )
                val p1 = Paragraph(h.text, fontStyle)

                val cell = PdfPCell()//Phrase(9F, h.text, fontStyle))
                cell.runDirection = PdfWriter.RUN_DIRECTION_RTL
                cell.horizontalAlignment = h.align
                cell.border = 0
                cell.paddingBottom = 8F
                cell.paddingTop = 5F
                cell.addElement(p1)
                table.addCell(cell)

                yPosForSummary = table.writeSelectedRows(0, -1, xPos, yPosForSummary, writer.directContent)
            }else{
                val cells = h.rows[0].cells
                val table = PdfPTable(cells.size)
                val row = h.rows[0]
                val cwidth : ArrayList<Int> = arrayListOf()
                if(!isRTL){
                    for (c in row.cells.toSortedMap(compareBy { it })){
                        cwidth.add(c.value.cellWidth.toInt())
                    }
                }else{
                    for (c in row.cells.toSortedMap(compareByDescending { it })){
                        cwidth.add(c.value.cellWidth.toInt())
                    }
                }


                 table.setWidths(cwidth.toIntArray())

                table.totalWidth = workArea

                if(isRTL) table.runDirection = PdfWriter.RUN_DIRECTION_RTL

                for(row in h.rows){
                    for(c in row.cells.toSortedMap()){
                        val fontName = BaseFont.createFont(c.value.fontName, BaseFont.IDENTITY_H, true)
                        val fontStyle = Font(fontName, c.value.fontSize, c.value.fontWeight, BaseColor.BLACK)
                        val cell = PdfPCell(Phrase(9F, c.value.cellName, fontStyle))
                        cell.runDirection = PdfWriter.RUN_DIRECTION_RTL
                        cell.horizontalAlignment = c.value.align

                        if(c.value.border == 0){
                            cell.border = 0
                        }
                        cell.setPadding(4F)
                        table.addCell(cell)
                    }
                    table.spacingAfter = 10F
                }
                yPosForSummary = table.writeSelectedRows(0, -1, xPos, yPosForSummary, writer.directContent)
            }
        }
    }
    override fun onOpenDocument(writer: PdfWriter?, document: Document?) {
        //header[0] = Phrase("Al-Nahder")
    }

    override fun onChapter(writer: PdfWriter?,document: Document?, paragraphPosition: Float, title: Paragraph?) {
        //header[1] = Phrase(title!!.content)
        pageNumber = 1
    }

     override fun onStartPage(writer: PdfWriter?, document: Document?) {

        super.onStartPage(writer, document)
         if(isPrintHeader)
         header(writer, document)
        yPos -= 10
        pageNumber++
    }

    private fun header(writer: PdfWriter?, document: Document?){
        if(logo?.bmp != null){
            val cb = writer!!.directContent
            val stream = ByteArrayOutputStream()
            logo!!.bmp!!.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val image: Image = Image.getInstance(stream.toByteArray())
            image.scaleAbsolute(120F,110F)
            image.setAbsolutePosition(50f, 700f)
            cb.addImage(image)
        }
        val pw = writer!!.pageSize.width
        if(pageHeader != null){
            yPos = writer.getVerticalPosition(false)
            for (h in pageHeader!!){
                if(h.rows == null){
                    val table = PdfPTable(1)
                    table.totalWidth = workArea
                    if(isRTL) table.runDirection = PdfWriter.RUN_DIRECTION_RTL
                    val xPos = (pw / 2) - (table.totalWidth / 2)
                    val fontName = BaseFont.createFont(h.fontName, BaseFont.IDENTITY_H, true)
                    val fontStyle = Font(fontName, h.fontSize, h.fontWeight, BaseColor.BLACK)
                    //val font: Font = FontFactory.getFont(h.fontName, BaseFont.CP1252,true,22F, Font.BOLD )
                    val cell = PdfPCell(Phrase(9F, h.text, fontStyle))
                    cell.runDirection = PdfWriter.RUN_DIRECTION_RTL
                    cell.horizontalAlignment = h.align
                    cell.border = 0
                    cell.paddingBottom = 8F
                    cell.paddingTop = 5F
                    table.addCell(cell)

                    yPos = table.writeSelectedRows(0, -1, xPos, yPos, writer.directContent)

                }else{
                    val cells = h.rows[0].cells
                    val table = PdfPTable(cells.size)
                    val row = h.rows[0]
                    val cwidth : ArrayList<Int> = arrayListOf()
                    if(isRTL){
                        for (c in row.cells.toSortedMap(compareByDescending  { it })){
                            cwidth.add(c.value.cellWidth.toInt())
                        }
                    }else{
                        for (c in row.cells.toSortedMap(compareBy{ it })){
                            cwidth.add(c.value.cellWidth.toInt())
                        }
                    }


                    table.setWidths(cwidth.toIntArray())


                    table.totalWidth = workArea
                    if(isRTL) table.runDirection = PdfWriter.RUN_DIRECTION_RTL

                    val xPos = (pw / 2) - (table.totalWidth / 2)
                    for(row in h.rows){
                        for(c in row.cells.toSortedMap()){
                            val fontName = BaseFont.createFont(c.value.fontName, BaseFont.IDENTITY_H, true)
                            val fontStyle = Font(fontName, c.value.fontSize, c.value.fontWeight, BaseColor.BLACK)
                            val cell = PdfPCell(Phrase(9F, c.value.cellName, fontStyle))
                            cell.runDirection = PdfWriter.RUN_DIRECTION_RTL
                            cell.horizontalAlignment = c.value.align
                            cell.border = c.value.border
                            cell.paddingBottom = 8F
                            cell.paddingTop = 5F
                            table.addCell(cell)
                        }
                    }
                    yPos = table.writeSelectedRows(0, -1, xPos, yPos - 10, writer.directContent)
                }
            }
            // addLineSeparator(document!!)
        }
        yPos -= 10
    }
    override fun onEndPage(writer: PdfWriter?, document: Document?) {
        if(isPrintFooter){
            val rect = writer!!.pageSize
            val font = Font(Font.FontFamily.HELVETICA, 52f, Font.BOLD, GrayColor(0.85f))
            //ColumnText.showTextAligned(writer.directContent, Element.ALIGN_CENTER, Phrase("MAWARED VAN SALE", font),297.5f, 421f, if((writer.pageNumber % 2) == 1) 45f else -45f)
            footer(writer, document)
            val fontStyle = Font()
            fontStyle.color = BaseColor.BLACK
            fontStyle.size = 10F
            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
            val date: String = sdf.format(Date())
            ColumnText.showTextAligned(writer.directContent, Element.ALIGN_CENTER, Phrase(String.format( "Page - %d, Printed on : %s %s", writer.pageNumber, date, "" ), fontStyle),
                document!!.pageSize.width / 2, rect.bottom + 10, 0f
            )
        }

    }

    private fun footer(writer: PdfWriter?, document: Document?){
        if(isPrintFooter){
            val rect = writer!!.pageSize
            val font = Font(Font.FontFamily.HELVETICA, 52f, Font.BOLD, GrayColor(0.85f))
            val pw =rect.width
            var fyPos = rect.bottom + footerHeight
            if(pageFooter != null){
                var cnt = pageFooter!!.size
                if(cnt < 8){
                    cnt = 8
                }
                fyPos = rect.bottom + (20 * cnt)

                for (h in pageFooter!!){

                    if(h.rows == null){
                        val table = PdfPTable(1)
                        table.totalWidth = workArea
                        if(isRTL) table.runDirection = PdfWriter.RUN_DIRECTION_RTL
                        val xPos = (pw / 2) - (table.totalWidth / 2)
                        val fontName = BaseFont.createFont(h.fontName, BaseFont.IDENTITY_H, true)
                        val fontStyle = Font(fontName, h.fontSize, h.fontWeight, BaseColor.BLACK)
                        //val font: Font = FontFactory.getFont(h.fontName, BaseFont.CP1252,true,22F, Font.BOLD )
                        val cell = PdfPCell(Phrase(h.fontSize, h.text, fontStyle))
                        cell.runDirection = PdfWriter.RUN_DIRECTION_RTL
                        cell.horizontalAlignment = h.align
                        cell.border = 0
                        cell.paddingBottom = 8F
                        cell.paddingTop = 5F
                        table.addCell(cell)

                        fyPos = table.writeSelectedRows(0, -1, xPos, fyPos, writer.directContent)

                    }else{
                        val cells = h.rows[0].cells
                        val table = PdfPTable(cells.size)
                        //table.setWidths(intArrayOf(10, 20, 10, 25, 18, 10))
                        table.totalWidth = workArea
                        if(isRTL) table.runDirection = PdfWriter.RUN_DIRECTION_RTL

                        val xPos = (pw / 2) - (table.totalWidth / 2)
                        for(row in h.rows){
                            for(c in row.cells.toSortedMap()){
                                val fontName = BaseFont.createFont(c.value.fontName, BaseFont.IDENTITY_H, true)
                                val fontStyle = Font(fontName, c.value.fontSize, c.value.fontWeight, BaseColor.BLACK)
                                val cell = PdfPCell(Phrase(9F, c.value.cellName, fontStyle))
                                cell.runDirection = PdfWriter.RUN_DIRECTION_RTL
                                cell.horizontalAlignment = c.value.align
                                cell.border = c.value.border
                                cell.paddingBottom = 8F
                                cell.paddingTop = 5F
                                table.addCell(cell)
                            }
                        }
                        fyPos = table.writeSelectedRows(0, -1, xPos, fyPos , writer.directContent)
                    }
                }
            }
        }

    }
    @Throws(DocumentException::class)
    fun addLineSeparator(document: Document) {
        val lineSeparator = LineSeparator()
        lineSeparator.lineColor = BaseColor(0,0,0,68)
        addLineSpace(document)
        document.add(Chunk(lineSeparator))
        addLineSpace(document)
    }

    @Throws(DocumentException::class)
    fun addLineSpace(document: Document) {
        document.add(Paragraph(""))
    }

    @Throws(DocumentException::class)
    fun addNewItem(document: Document, text: String, align: Int, style: Font) {
        val chunk = Chunk(text, style)
        val p = Paragraph(chunk)
        p.alignment = align
        document.add(p)
    }
}

class MawaredPdf1: PdfPageEventHelper(){
    // variable
    companion object{

        var footer: ArrayList<ITextTicket>? = null
        var header: ArrayList<ITextTicket>? = null
        var tableHeader: ArrayList<ITextTicket>? = null
        var filter: ArrayList<ITextTicket>? = null
        var summary: ArrayList<ITextTicket>? = null

        var lastwriteposition = 100F
        var workArea: Float = 0f
        var yPos: Float = 0F
        //var footerHeight = 80F
    }

    // override function

    fun setConfig(_rowHeader: ArrayList<ITextTicket>?, _pageHeader: ArrayList<ITextTicket>?,
                  _pageFooter: ArrayList<ITextTicket>?, _reportFitler: ArrayList<ITextTicket>?, _summary: ArrayList<ITextTicket>?, _workArea: Float): MawaredPdf1{
        val pdf = MawaredPdf1()
        tableHeader = _rowHeader
        header = _pageHeader
        footer = _pageFooter
        filter = _reportFitler
        workArea = _workArea
        summary = _summary
        return pdf
    }

    fun createDoc(writer: PdfWriter, document: Document, rows: List<Sale_Items>){
        try {
            val table = PdfPTable(tableHeader!!.count())
            document.open()
            var rowNo = 1
            var pageRows = 1
            val cellsWidth : ArrayList<Int> = arrayListOf() // intArrayOf(20, 8, 8, 8, 5, 20, 10, 5)
            table.spacingAfter = 10F
            table.defaultCell.verticalAlignment = Element.ALIGN_CENTER
            table.runDirection = PdfWriter.RUN_DIRECTION_RTL
            table.totalWidth = workArea

            for (c in tableHeader!!.sortedByDescending { it.order }){
                cellsWidth.add(c.width.toInt())
            }
            if(cellsWidth.size > 0) {
                table.setWidths(cellsWidth.toIntArray())
            }

            // Makes it possible to add text to a specific place in the document using
            // a X & Y placement syntax.
            // Makes it possible to add text to a specific place in the document using
            // a X & Y placement syntax.

            val cb: PdfContentByte = writer.directContent
            cb.saveState()
            cb.setColorStroke(BaseColor.BLACK)
            cb.rectangle(1F,1F,595F,842F)
            cb.stroke()
            cb.restoreState()
            // Don't forget to call the BeginText() method when done doing graphics!
            //cb.beginText()

            //val mBreakPage = writer.pageSize.bottom + footerHeight
            //val mCenterOfBody = (document.pageSize.width / 2) - (table.totalWidth / 2)
            val mRowsCount = rows.count()

            var top_margin = 0F
            var left_margin = 0F

            while (true){
                top_margin = tableHeader!![0].yPos
                left_margin = tableHeader!![0].xPos
                // table header
                for (h in tableHeader!!.sortedBy { it.order }){
                    val fontName = BaseFont.createFont(h.fontName, BaseFont.IDENTITY_H, true)
                    val fontStyle = Font(fontName, h.size, h.bold, h.color)
                    val cell = PdfPCell(Phrase(9F, h.text, fontStyle))
                    cell.horizontalAlignment = h.align
                    cell.backgroundColor = BaseColor.LIGHT_GRAY
                    cell.setPadding(0.8F)
                    table.addCell(cell)
                }
                //top_margin -= 12
                for (row in rows){
                    table.addCell(Phrase(9F, rowNo.toString()))
                    var cell = PdfPCell(Phrase(9F, row.sld_barcode))
                    cell.horizontalAlignment = Element.ALIGN_RIGHT
                    cell.verticalAlignment = Element.ALIGN_TOP
                    table.addCell(cell)

                    cell = PdfPCell(Phrase(9F, row.sld_prod_name))
                    cell.horizontalAlignment = Element.ALIGN_RIGHT
                    cell.verticalAlignment = Element.ALIGN_TOP
                    table.addCell(cell)
                    val cellHeight = cell.calculatedHeight
                    cell = PdfPCell(Phrase(9F, row.sld_pack_qty.toString()))
                    cell.horizontalAlignment = Element.ALIGN_LEFT
                    cell.verticalAlignment = Element.ALIGN_TOP
                    table.addCell(cell)

                    cell = PdfPCell(Phrase(9F, "0"))
                    cell.horizontalAlignment = Element.ALIGN_LEFT
                    cell.verticalAlignment = Element.ALIGN_TOP
                    table.addCell(cell)

                    cell = PdfPCell(Phrase(9F, row.sld_unit_price.toString()))
                    cell.horizontalAlignment = Element.ALIGN_LEFT
                    cell.verticalAlignment = Element.ALIGN_TOP
                    table.addCell(cell)

                    cell = PdfPCell(Phrase(9F, row.sld_dis_value.toString()))
                    cell.horizontalAlignment = Element.ALIGN_LEFT
                    cell.verticalAlignment = Element.ALIGN_TOP
                    table.addCell(cell)

                    cell = PdfPCell(Phrase(9F, row.sld_net_total.toString()))
                    cell.horizontalAlignment = Element.ALIGN_LEFT
                    cell.verticalAlignment = Element.ALIGN_TOP
                    table.addCell(cell)

                    cell = PdfPCell(Phrase(9F, ""))
                    cell.horizontalAlignment = Element.ALIGN_RIGHT
                    cell.verticalAlignment = Element.ALIGN_TOP
                    table.addCell(cell)

                    // increase page number
                    pageRows++
                    // increase Row number
                    rowNo++

                    val mHeightOfTable = table.calculateHeights() + cellHeight
                    if(top_margin <= lastwriteposition){
                        break
                    }
                }// end items

                yPos = table.writeSelectedRows(0, -1, left_margin, top_margin, cb)
                // check if end of page or not
                if(pageRows < mRowsCount){
                    // write the rows in the pdf Page
                    document.newPage()
                    table.flushContent()
                }else{
                    break
                }

            }// end while print all data
            // Okay, write out the totals table
            // Here you might want to do some page break scenarios, as well:
            // Example:
            // Calculate how many rows you are about to print and see if they fit before the lastwriteposition,
            // then decide how to do; write some on first page, then the rest on second page or perhaps all the
            // total lines after the page break.
            // We are not doing this here, we just write them out 80 points below the last writed item row
            if(summary != null){
                val tmp = pdfSummary(cb)
                cb.addTemplate(tmp, left_margin, yPos - 100)
            }

            document.close()
            writer.close()
        }catch (e: Exception){
            throw e
        }
    }

    override fun onOpenDocument(writer: PdfWriter?, document: Document?) {
        //header[0] = Phrase("Al-Nahder")
    }

    override fun onChapter(writer: PdfWriter?,document: Document?, paragraphPosition: Float, title: Paragraph?) {
        //header[1] = Phrase(title!!.content)
        //pageNumber = 1
    }

    override fun onStartPage(writer: PdfWriter?, document: Document?) {

        super.onStartPage(writer, document)
        val cb = writer!!.directContent
        //cb.addTemplate(pdfHeader(cb), 10F, 700F)
        pdfHeader(cb)
    }

    override fun onEndPage(writer: PdfWriter?, document: Document?) {
        val cb = writer!!.directContent
        // Add a footer template to the document
        cb.addTemplate(pdfFooter(cb, writer.pageNumber), 10F, 1F)
    }

    private fun writeText(cb: PdfContentByte, text: String, x: Float, y: Float, font: BaseFont, size: Float, align: Int){
        cb.setFontAndSize(font, size)
        cb.showTextAligned(align, text, x, y, 0F)
    }

    private fun pdfFooter(cb: PdfContentByte, pageNumber: Int): PdfTemplate{
        // Create the template and assign height
        val tmpFooter: PdfTemplate = cb.createTemplate(580F,70F)
        // Move to the bottom left corner of the template
        tmpFooter.moveTo(1F, 1F)
        // Place the footer content
        tmpFooter.stroke()
        // Begin writing the footer
        tmpFooter.beginText()
        // Set the font and size

        // Write out details from the payee table
        for (f in footer!!.sortedBy { it.order }){
            val f_cn =  BaseFont.createFont(f.fontName, BaseFont.IDENTITY_H, true)
            tmpFooter.setFontAndSize(f_cn, f.size)
            tmpFooter.showTextAligned(f.align, f.text, f.xPos, f.yPos, 0F)
        }

        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
        val date: String = sdf.format(Date())

        val x = 580F / 2
        val y = 5F
        val fontName = BaseFont.createFont("assets/fonts/brandon_medium.otf", BaseFont.IDENTITY_H, true)
        writeText(cb, String.format( "Page - %d, Printed on : %s %s", pageNumber, date, "" ),x, y, fontName, 8F, PdfContentByte.ALIGN_CENTER)
        // End text
        tmpFooter.endText()
        // Stamp a line above the page footer
        cb.setLineWidth(4f)
        cb.moveTo(30F, 70F)
        cb.lineTo(570F, 70F)
        cb.stroke()
        // Write page number
        //val fontStyle = Font()
        //fontStyle.color = BaseColor.BLACK
        //fontStyle.size = 12F
       // val sdf = SimpleDateFormat("dd-MM-YYYY", Locale.ENGLISH)
        //val date: String = sdf.format(Date())
//        ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, Phrase(String.format( "Page - %d, Printed on : %s %s", writer.pageNumber, date, "" ), fontStyle),
//            document!!.pageSize.width / 2, rect.bottom + 5, 0f
//        )

        // Return the footer template
        return tmpFooter
    }

    private fun pdfHeader(cb: PdfContentByte){
        for (f in header!!.sortedBy { it.order }){
            val fontName =  BaseFont.createFont(f.fontName, BaseFont.IDENTITY_H, true)
            val fontStyle = Font(fontName, f.size, f.bold, f.color)
            val ct = ColumnText(cb)
            ct.runDirection = PdfWriter.RUN_DIRECTION_RTL
            val p = Paragraph(f.text, fontStyle)
            p.alignment = f.align
            ct.addElement(p)
            ct.setSimpleColumn(f.xPos, f.yPos, f.width, f.height)
            ct.go()
            //ColumnText.showTextAligned(cb, f.align, Phrase(f.text), f.xPos, f.yPos, 0F)
        }
//        val fontName =  BaseFont.createFont("assets/fonts/droid_kufi_regular.ttf", BaseFont.IDENTITY_H, true)
//        val fontStyle = Font(fontName, 14F, 0, BaseColor.BLACK)
//
//        val ct = ColumnText(cb)
//
//        ct.runDirection = PdfWriter.RUN_DIRECTION_RTL
//        ct.addElement(Paragraph("موارد الشركة الحديثة", fontStyle))
//        ct.setSimpleColumn(10f, 800f, 200f, 840f)
//
//        ct.go()


//        // Create the template and assign height
        //val tmpHeader: PdfTemplate = cb.createTemplate(580F,140F)
//        // Move to the bottom left corner of the template
//        tmpHeader.moveTo(1F, 1F)
//        // Place the footer content
//        tmpHeader.stroke();
//        // Begin writing the footer
//        tmpHeader.beginText();
//        // Set the font and size
//
//        // Write out details from the payee table
//        for (f in header!!.sortedBy { it.order }){
//            val f_cn =  BaseFont.createFont(f.fontName, BaseFont.IDENTITY_H, true)
//            tmpHeader.setFontAndSize(f_cn, f.size)
//            tmpHeader.showTextAligned(f.align, f.text, f.xPos, f.yPos, 0F)
//        }
//        // End text
//        tmpHeader.endText()
//        // Stamp a line above the page footer
        cb.setLineWidth(4f)
        cb.moveTo(30F, 700F)
        cb.lineTo(570F, 700F)
        cb.stroke()
//        // Return the footer template
//        return tmpHeader;
    }

    private fun pdfSummary(cb: PdfContentByte): PdfTemplate{
// Create the template and assign height
        val tmpSummary: PdfTemplate = cb.createTemplate(580F,70F)
        // Move to the bottom left corner of the template
        tmpSummary.moveTo(1F, 1F)
        // Place the footer content
        tmpSummary.stroke()
        // Begin writing the footer
        tmpSummary.beginText()
        // Set the font and size
        // Write out details from the payee table
        for (f in summary!!.sortedBy { it.order }){
            val f_cn =  BaseFont.createFont(f.fontName, BaseFont.IDENTITY_H, true)
            tmpSummary.setFontAndSize(f_cn, f.size)
            tmpSummary.showTextAligned(f.align, f.text, f.xPos, f.yPos, 0F)
        }
        // End text
        tmpSummary.endText()
        // Stamp a line above the page footer
//        cb.setLineWidth(4f);
//        cb.moveTo(30F, 480F);
//        cb.lineTo(570F, 480F);
//        cb.stroke();
        // Return the footer template
        return tmpSummary
    }

    @Throws(DocumentException::class)
    fun addLineSeparator(document: Document) {
        val lineSeparator = LineSeparator()
        lineSeparator.lineColor = BaseColor(0,0,0,68)
        addLineSpace(document)
        document.add(Chunk(lineSeparator))
        addLineSpace(document)
    }

    @Throws(DocumentException::class)
    fun addLineSpace(document: Document) {
        document.add(Paragraph(""))
    }

    @Throws(DocumentException::class)
    fun addNewItem(document: Document, text: String, align: Int, style: Font) {
        val chunk = Chunk(text, style)
        val p = Paragraph(chunk)
        p.alignment = align
        document.add(p)
    }
}

class GeneratePdf{
    var document: Document? = null
    var workArea: Float = 0F
    val fileName: String = "test.pdf"
    var activity: Activity? = null

    // Storage Permissions
    private val REQUEST_EXTERNAL_STORAGE = 1
    private val PERMISSIONS_STORAGE = arrayOf<String>(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    fun verifyStoragePermissions(activity: Activity?) {
        // Check if we have write permission
        val permission = ActivityCompat.checkSelfPermission(
            activity!!,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                activity,
                PERMISSIONS_STORAGE,
                REQUEST_EXTERNAL_STORAGE
            )
        }
    }

    fun createPdf(activity: Activity, logo: RepLogo?, rows: List<Sale_Items>, _rowHeader: HashMap<Int, RowHeader>, _pageHeader: ArrayList<HeaderFooterRow>?,
                  _pageFooter: ArrayList<HeaderFooterRow>?, _reportFitler: HashMap<String, PageFilter>?, _summary: ArrayList<HeaderFooterRow>?, isRTL: Boolean, complete: (Boolean, String) -> Unit){
        PageSetting().page = PageSize.A4

        document = Document(PageSize.A4, PageSetting().marginLeft, PageSetting().marginRight, PageSetting().marginTop, PageSetting().marginBottom)

        workArea = PageSetting().page.width * 0.95F // - (document!!.leftMargin() + document!!.rightMargin())
        this.activity = activity
        verifyStoragePermissions(activity)
        Dexter.withActivity(activity)
            .withPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {

                    val path = Common.getAppPath(activity) + fileName
                    val fs = FileOutputStream(path)
                    val pdf = MawaredPdf()
                    val writer = PdfWriter.getInstance(document, fs)
                    setDocumentInfo(document!!)
                    writer.pageEvent = pdf.setConfig(logo, _rowHeader, _pageHeader, _pageFooter, _reportFitler, _summary, workArea, isRTL, PageSize.A5)
                    pdf.writeBody(writer!!, document!!, rows)
                    complete(true, path)

                }

                override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken? ) {

                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {

                }
            })
            .check()

    }

    fun createPdf(activity: Activity, logo: RepLogo?, baseEo: Receivable, _pageHeader: ArrayList<HeaderFooterRow>?,
                  _pageFooter: ArrayList<HeaderFooterRow>?, isRTL: Boolean, complete: (Boolean, String) -> Unit){
        //PageSetting().page = PageSize.A5

        document = Document(PageSize.A4, 0.5F, 0.5F, 0.5F, 0.5F)
        workArea =PageSize.A4.width * 0.95F
        this.activity = activity
        Dexter.withActivity(activity)
            .withPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {

                    val path = Common.getAppPath(activity) + fileName
                    val fs = FileOutputStream(path)
                    val pdf = MawaredPdf()
                    val writer = PdfWriter.getInstance(document, fs)
                    setDocumentInfo(document!!)
                    writer.pageEvent = pdf.setConfig(logo, null, _pageHeader, _pageFooter, null, null, workArea, isRTL, PageSize.A4)
                    writer.runDirection = PdfWriter.RUN_DIRECTION_RTL
                    pdf.writeBody(writer!!, document!!, baseEo)
                    complete(true, path)
                }
                override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken? ) {

                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {

                }
            })
            .check()

    }

    private fun setDocumentInfo(document: Document){
        document.addAuthor("Al-Hadetha For Software and Automation")
        document.addCreator("Mawared Van Sale Pdf")
        document.addKeywords("PDF tutorial invoice")
        document.addSubject("Describing the steps creating a PDF document")
        document.addTitle("PDF creation using iText - Sample invoice")
    }
    fun printPDF(act: Activity, path: String) {
        val printManager = act.getSystemService(Context.PRINT_SERVICE) as PrintManager

        try {
            val printAdapter = PdfDocumentAdapter(act, path)
            printManager.print("Document", printAdapter, PrintAttributes.Builder().build())

        }catch (e: Exception){
            Log.e("PdfActivity", "" + e.message)
        }
    }
}

class GeneratePdf1{
    var document: Document? = null
    var workArea: Float = 0F
    val fileName: String = "test.pdf"
    var activity: Activity? = null
    fun createPdf(activity: Activity, rows: List<Sale_Items>, _rowHeader: ArrayList<ITextTicket>?, _pageHeader: ArrayList<ITextTicket>?,
                  _pageFooter: ArrayList<ITextTicket>?, _reportFitler: ArrayList<ITextTicket>?, _summary: ArrayList<ITextTicket>?, complete: (Boolean, String) -> Unit){
        PageSetting().page = PageSize.A4

        document = Document(PageSize.A4, PageSetting().marginLeft, PageSetting().marginRight, PageSetting().marginTop, PageSetting().marginBottom)

        workArea = PageSetting().page.width * 0.95F // - (document!!.leftMargin() + document!!.rightMargin())
        this.activity = activity
        Dexter.withActivity(activity)
            .withPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {

                    val path = Common.getAppPath(activity) + fileName
                    val fs = FileOutputStream(path)
                    val pdf = MawaredPdf1()
                    val writer = PdfWriter.getInstance(document, fs)
                    writer.runDirection = PdfWriter.RUN_DIRECTION_RTL
                    setDocumentInfo(document!!)
                    writer.pageEvent = pdf.setConfig(_rowHeader, _pageHeader, _pageFooter, _reportFitler, _summary, workArea)
                    pdf.createDoc(writer!!, document!!, rows)
                    complete(true, path)

                }

                override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken? ) {

                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {

                }
            })
            .check()

    }

    private fun setDocumentInfo(document: Document){
        document.addAuthor("Al-Hadetha For Software and Automation")
        document.addCreator("Mawared Van Sale Pdf")
        document.addKeywords("PDF tutorial invoice")
        document.addSubject("Describing the steps creating a PDF document")
        document.addTitle("PDF creation using iText - Sample invoice")
    }
    fun printPDF(act: Activity, path: String) {
        val printManager = act.getSystemService(Context.PRINT_SERVICE) as PrintManager

        try {
            val printAdapter = PdfDocumentAdapter(act, path)
            printManager.print("Document", printAdapter, PrintAttributes.Builder().build())

        }catch (e: Exception){
            Log.e("PdfActivity", "" + e.message)
        }
    }
}

class PdfTicket: PdfPageEventHelper(){
    // variable
    companion object{
        var pageNumber: Int = 0

        var headerRow: HashMap<Int, RowHeader>? = null
        var pageHeader:ArrayList<HeaderFooterRow>? = null
        var pageFooter: ArrayList<HeaderFooterRow>? = null
        var pageFilter: HashMap<String, PageFilter>? = null
        var summary: ArrayList<HeaderFooterRow>? = null
        var tableLoopData: ArrayList<Hashtable<String, String>>? = null

        var workArea: Float = 0f
        var yPos: Float = 0F
        var yPosForSummary: Float = 0F
        var footerHeight = 90F
        var logo: RepLogo? = null
        var isRTL: Boolean = false
        var pageSize = PageSize.A4
        var isPrintHeader = true
        var isPrintFooter = true
    }

    // override function

    fun setConfig(mlogo: RepLogo?, _rowHeader: HashMap<Int, RowHeader>?, _pageHeader: ArrayList<HeaderFooterRow>?,
                  _pageFooter: ArrayList<HeaderFooterRow>?, _summary: ArrayList<HeaderFooterRow>?, _workArea: Float, _isRTL: Boolean, _pageSize: Rectangle): PdfTicket{
        val pdf = PdfTicket()

        logo = mlogo
        headerRow = _rowHeader
        pageHeader = _pageHeader
        pageFooter = _pageFooter
        //pageFilter = _reportFitler
        workArea = _workArea
        summary = _summary
        isRTL = _isRTL
        pageSize = _pageSize
        return pdf
    }

    fun writeBody(writer: PdfWriter, document: Document, rows: List<Sale_Items>){
        isPrintHeader = true
        isPrintFooter = true
        val table = PdfPTable(headerRow!!.count())
        document.open()
        var rowNo = 1
        var pageRows = 1
        val cellsWidth : ArrayList<Int> = arrayListOf() // intArrayOf(20, 8, 8, 8, 5, 20, 10, 5)
        table.spacingAfter = 10F
        table.defaultCell.verticalAlignment = Element.ALIGN_CENTER
        if(isRTL) {
            table.runDirection = PdfWriter.RUN_DIRECTION_RTL
            for (c in headerRow!!.toSortedMap(compareByDescending { it })){
                cellsWidth.add(c.value.cellWidth)
            }
        }else{
            for (c in headerRow!!.toSortedMap(compareBy { it })){
                cellsWidth.add(c.value.cellWidth)
            }
        }

        table.totalWidth = workArea
        table.widthPercentage = 100F

        if(cellsWidth.size > 0) {
            table.setWidths(cellsWidth.toIntArray())
        }


        val mBreakPage = writer.pageSize.bottom //+ footerHeight
        val mCenterOfBody = (document.pageSize.width / 2) - (table.totalWidth / 2)
        val mRowsCount = rows.count()
        val df = DecimalFormat("#,###,###.###")
        val df1 = DecimalFormat("#,###")
        val fontName = BaseFont.createFont(headerRow!![0]!!.fontName, BaseFont.IDENTITY_H, true)
        val fontStyle = Font(fontName, headerRow!![0]!!.fontSize, headerRow!![0]!!.fontWeight, BaseColor.BLACK)
        while (true){
            for(c in headerRow!!.toSortedMap()){
                //val fontName = BaseFont.createFont(c.value.fontName, BaseFont.IDENTITY_H, true)
                //val fontStyle = Font(fontName, c.value.fontSize, c.value.fontWeight, BaseColor.BLACK)
                val cell = PdfPCell(Phrase(9F, c.value.cellName, fontStyle))
                cell.horizontalAlignment = c.value.align
                cell.backgroundColor = BaseColor.LIGHT_GRAY
                cell.paddingBottom = 5F
                cell.paddingTop = 5F
                table.addCell(cell)
            }
            for (r in rows){
//                var cell = PdfPCell(Phrase(9F, rowNo.toString(), fontStyle))
//                cell.horizontalAlignment = Element.ALIGN_CENTER
//
//                table.addCell(cell)

//                cell = PdfPCell(Phrase(9F, r.sld_barcode, fontStyle))
//                cell.paddingTop = 4F
//                cell.paddingBottom = 4F
//                cell.horizontalAlignment = Element.ALIGN_TOP
//                cell.verticalAlignment = Element.ALIGN_CENTER
//                cell.runDirection = PdfWriter.RUN_DIRECTION_RTL
//                table.addCell(cell)
                var pr_name = r.sld_prod_name
                if(isRTL){
                    pr_name = r.sld_prod_name_ar
                }
                var cell = PdfPCell(Phrase(9F, pr_name, fontStyle))
                cell.setPadding(5F)
                if(isRTL){
                    cell.runDirection = PdfWriter.RUN_DIRECTION_RTL
                    cell.horizontalAlignment = Element.ALIGN_LEFT
                }else{
                    cell.horizontalAlignment = Element.ALIGN_LEFT
                }

                cell.verticalAlignment = Element.ALIGN_TOP
                table.addCell(cell)
                val cellHeight = cell.calculatedHeight

                cell = PdfPCell(Phrase(9F, df1.format(r.sld_pack_qty), fontStyle))
                cell.setPadding(5F)
                cell.runDirection = PdfWriter.RUN_DIRECTION_RTL
                cell.horizontalAlignment = Element.ALIGN_LEFT
                cell.verticalAlignment = Element.ALIGN_TOP
                table.addCell(cell)

//                cell = PdfPCell(Phrase(9F, df1.format(r.sld_gift_qty), fontStyle))
//                cell.setPadding(5F)
//                cell.runDirection = PdfWriter.RUN_DIRECTION_RTL
//                cell.horizontalAlignment = Element.ALIGN_LEFT
//                cell.verticalAlignment = Element.ALIGN_TOP
//                table.addCell(cell)

                cell = PdfPCell(Phrase(9F, df.format(r.sld_unit_price), fontStyle))
                cell.setPadding(5F)
                cell.runDirection = PdfWriter.RUN_DIRECTION_RTL
                cell.horizontalAlignment = Element.ALIGN_LEFT
                cell.verticalAlignment = Element.ALIGN_TOP
                table.addCell(cell)

//                cell = PdfPCell(Phrase(9F, df.format(r.sld_dis_value), fontStyle))
//                cell.setPadding(5F)
//                cell.runDirection = PdfWriter.RUN_DIRECTION_RTL
//                cell.horizontalAlignment = Element.ALIGN_LEFT
//                cell.verticalAlignment = Element.ALIGN_TOP
//                table.addCell(cell)

                cell = PdfPCell(Phrase(9F, df.format(r.sld_net_total), fontStyle))
                cell.setPadding(5F)
                cell.runDirection = PdfWriter.RUN_DIRECTION_RTL
                cell.horizontalAlignment = Element.ALIGN_LEFT
                cell.verticalAlignment = Element.ALIGN_TOP
                table.addCell(cell)

//                cell = PdfPCell(Phrase(9F, "", fontStyle))
//                cell.setPadding(5F)
//                cell.runDirection = PdfWriter.RUN_DIRECTION_RTL
//                cell.horizontalAlignment = Element.ALIGN_RIGHT
//                cell.verticalAlignment = Element.ALIGN_TOP
//                table.addCell(cell)

                // increase page number
                pageRows++
                // increase Row number
                rowNo++

                val mHeightOfTable = table.calculateHeights() + cellHeight
//                if((yPos - mHeightOfTable) <= mBreakPage){
//                    break
//                }

            }// end rows
            yPosForSummary = table.writeSelectedRows(0, -1, mCenterOfBody, yPos, writer.directContent)
            yPos = yPosForSummary
            // check if end of page or not
            //if(pageRows < mRowsCount){
                // write the rows in the pdf Page
                //document.newPage()
                table.flushContent()
            //}else{
                break
            //}
        } // end while

        // Print Total In Last Page
        if(summary != null){
            showSummary(writer, mCenterOfBody)
        }
        document.close()
    }
    private fun showSummary(writer: PdfWriter, xPos: Float){

        for (h in summary!!){
            if(h.rows == null){
                //addLineSpace(document)
                val table = PdfPTable(1)
                table.totalWidth = workArea
                if(isRTL) table.runDirection = PdfWriter.RUN_DIRECTION_RTL
                table.setWidths(intArrayOf(100))
                val fontName = BaseFont.createFont(h.fontName, BaseFont.IDENTITY_H, true)
                val fontStyle = Font(fontName, h.fontSize, h.fontWeight, h.fontColor)
                //val font: Font = FontFactory.getFont(h.fontName, BaseFont.CP1252,true,22F, Font.BOLD )
                val p1 = Paragraph(h.text, fontStyle)

                val cell = PdfPCell()//Phrase(9F, h.text, fontStyle))
                cell.runDirection = PdfWriter.RUN_DIRECTION_RTL
                cell.horizontalAlignment = h.align
                cell.border = 0
                cell.paddingBottom = 8F
                cell.paddingTop = 5F
                cell.addElement(p1)
                table.addCell(cell)

                yPosForSummary = table.writeSelectedRows(0, -1, xPos, yPosForSummary, writer.directContent)
            }else{
                val cells = h.rows[0].cells
                val table = PdfPTable(cells.size)
                val row = h.rows[0]
                val cwidth : ArrayList<Int> = arrayListOf()
                if(!isRTL){
                    for (c in row.cells.toSortedMap(compareBy { it })){
                        cwidth.add(c.value.cellWidth.toInt())
                    }
                }else{
                    for (c in row.cells.toSortedMap(compareByDescending { it })){
                        cwidth.add(c.value.cellWidth.toInt())
                    }
                }


                table.setWidths(cwidth.toIntArray())

                table.totalWidth = workArea

                if(isRTL) table.runDirection = PdfWriter.RUN_DIRECTION_RTL

                for(row in h.rows){
                    for(c in row.cells.toSortedMap()){
                        val fontName = BaseFont.createFont(c.value.fontName, BaseFont.IDENTITY_H, true)
                        val fontStyle = Font(fontName, c.value.fontSize, c.value.fontWeight, BaseColor.BLACK)
                        val cell = PdfPCell(Phrase(9F, c.value.cellName, fontStyle))
                        cell.runDirection = PdfWriter.RUN_DIRECTION_RTL
                        cell.horizontalAlignment = c.value.align

                        if(c.value.border == 0){
                            cell.border = 0
                        }
                        cell.setPadding(4F)
                        table.addCell(cell)
                    }
                    table.spacingAfter = 10F
                }
                yPosForSummary = table.writeSelectedRows(0, -1, xPos, yPosForSummary, writer.directContent)
            }
        }
    }
    override fun onOpenDocument(writer: PdfWriter?, document: Document?) {
        //header[0] = Phrase("Al-Nahder")
    }

    override fun onChapter(writer: PdfWriter?,document: Document?, paragraphPosition: Float, title: Paragraph?) {
        //header[1] = Phrase(title!!.content)
        pageNumber = 1
    }

    override fun onStartPage(writer: PdfWriter?, document: Document?) {

        super.onStartPage(writer, document)
        if(isPrintHeader)
            header(writer)
        yPos -= 10
        pageNumber++
    }

    private fun header(writer: PdfWriter?){
        if(logo?.bmp != null){
            val cb = writer!!.directContent
            val stream = ByteArrayOutputStream()
            logo!!.bmp!!.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val image: Image = Image.getInstance(stream.toByteArray())
            image.scaleAbsolute(120F,110F)
            image.setAbsolutePosition(50f, 700f)
            cb.addImage(image)
        }
        val pw = writer!!.pageSize.width
        if(pageHeader != null){
            yPos = writer.getVerticalPosition(false)
            for (h in pageHeader!!){
                if(h.rows == null){
                    val table = PdfPTable(1)
                    table.totalWidth = workArea
                    if(isRTL) table.runDirection = PdfWriter.RUN_DIRECTION_RTL
                    val xPos = (pw / 2) - (table.totalWidth / 2)
                    val fontName = BaseFont.createFont(h.fontName, BaseFont.IDENTITY_H, true)
                    val fontStyle = Font(fontName, h.fontSize, h.fontWeight, BaseColor.BLACK)
                    //val font: Font = FontFactory.getFont(h.fontName, BaseFont.CP1252,true,22F, Font.BOLD )
                    val cell = PdfPCell(Phrase(9F, h.text, fontStyle))
                    cell.runDirection = PdfWriter.RUN_DIRECTION_RTL
                    cell.horizontalAlignment = h.align
                    cell.border = 0
                    cell.paddingBottom = 8F
                    cell.paddingTop = 5F
                    table.addCell(cell)

                    yPos = table.writeSelectedRows(0, -1, xPos, yPos, writer.directContent)

                }else{
                    val cells = h.rows[0].cells
                    val table = PdfPTable(cells.size)
                    val row = h.rows[0]
                    val cwidth : ArrayList<Int> = arrayListOf()
                    if(isRTL){
                        for (c in row.cells.toSortedMap(compareByDescending  { it })){
                            cwidth.add(c.value.cellWidth.toInt())
                        }
                    }else{
                        for (c in row.cells.toSortedMap(compareBy{ it })){
                            cwidth.add(c.value.cellWidth.toInt())
                        }
                    }


                    table.setWidths(cwidth.toIntArray())


                    table.totalWidth = workArea
                    if(isRTL) table.runDirection = PdfWriter.RUN_DIRECTION_RTL

                    val xPos = (pw / 2) - (table.totalWidth / 2)
                    for(row in h.rows){
                        for(c in row.cells.toSortedMap()){
                            val fontName = BaseFont.createFont(c.value.fontName, BaseFont.IDENTITY_H, true)
                            val fontStyle = Font(fontName, c.value.fontSize, c.value.fontWeight, BaseColor.BLACK)
                            val cell = PdfPCell(Phrase(9F, c.value.cellName, fontStyle))
                            cell.runDirection = PdfWriter.RUN_DIRECTION_RTL
                            cell.horizontalAlignment = c.value.align
                            cell.border = c.value.border
                            cell.paddingBottom = 8F
                            cell.paddingTop = 5F
                            table.addCell(cell)
                        }
                    }
                    yPos = table.writeSelectedRows(0, -1, xPos, yPos - 10, writer.directContent)
                }
            }
            // addLineSeparator(document!!)
        }
        yPos -= 10
    }
    override fun onEndPage(writer: PdfWriter?, document: Document?) {
        if(isPrintFooter){
//            val rect = writer!!.pageSize
//            val font = Font(Font.FontFamily.HELVETICA, 52f, Font.BOLD, GrayColor(0.85f))
//            //ColumnText.showTextAligned(writer.directContent, Element.ALIGN_CENTER, Phrase("MAWARED VAN SALE", font),297.5f, 421f, if((writer.pageNumber % 2) == 1) 45f else -45f)
//            footer(writer, document)
//            val fontStyle = Font()
//            fontStyle.color = BaseColor.BLACK
//            fontStyle.size = 10F
//            val sdf = SimpleDateFormat("dd-MM-YYYY", Locale.ENGLISH)
//            val date: String = sdf.format(Date())
//            ColumnText.showTextAligned(writer.directContent, Element.ALIGN_CENTER, Phrase(String.format( "Page - %d, Printed on : %s %s", writer.pageNumber, date, "" ), fontStyle),
//                document!!.pageSize.width / 2, rect.bottom + 10, 0f
//            )
        }

    }

    private fun footer(writer: PdfWriter?, document: Document?){
        if(isPrintFooter){
            val rect = writer!!.pageSize
            val font = Font(Font.FontFamily.HELVETICA, 52f, Font.BOLD, GrayColor(0.85f))
            val pw =rect.width
            var fyPos = rect.bottom + footerHeight
            if(pageFooter != null){

                for (h in pageFooter!!){
                    if(h.rows == null){
                        val table = PdfPTable(1)
                        table.totalWidth = workArea
                        if(isRTL) table.runDirection = PdfWriter.RUN_DIRECTION_RTL
                        val xPos = (pw / 2) - (table.totalWidth / 2)
                        val fontName = BaseFont.createFont(h.fontName, BaseFont.IDENTITY_H, true)
                        val fontStyle = Font(fontName, h.fontSize, h.fontWeight, BaseColor.BLACK)
                        //val font: Font = FontFactory.getFont(h.fontName, BaseFont.CP1252,true,22F, Font.BOLD )
                        val cell = PdfPCell(Phrase(9F, h.text, fontStyle))
                        cell.runDirection = PdfWriter.RUN_DIRECTION_RTL
                        cell.horizontalAlignment = h.align
                        cell.border = 0
                        cell.paddingBottom = 8F
                        cell.paddingTop = 5F
                        table.addCell(cell)

                        fyPos = table.writeSelectedRows(0, -1, xPos, fyPos, writer.directContent)

                    }else{
                        val cells = h.rows[0].cells
                        val table = PdfPTable(cells.size)
                        //table.setWidths(intArrayOf(10, 20, 10, 25, 18, 10))
                        table.totalWidth = workArea
                        if(isRTL) table.runDirection = PdfWriter.RUN_DIRECTION_RTL

                        val xPos = (pw / 2) - (table.totalWidth / 2)
                        for(row in h.rows){
                            for(c in row.cells.toSortedMap()){
                                val fontName = BaseFont.createFont(c.value.fontName, BaseFont.IDENTITY_H, true)
                                val fontStyle = Font(fontName, c.value.fontSize, c.value.fontWeight, BaseColor.BLACK)
                                val cell = PdfPCell(Phrase(9F, c.value.cellName, fontStyle))
                                cell.runDirection = PdfWriter.RUN_DIRECTION_RTL
                                cell.horizontalAlignment = c.value.align
                                cell.border = c.value.border
                                cell.paddingBottom = 8F
                                cell.paddingTop = 5F
                                table.addCell(cell)
                            }
                        }
                        fyPos = table.writeSelectedRows(0, -1, xPos, fyPos - 10, writer.directContent)
                    }
                }
            }
        }

    }

}

class GenerateTicketPDF{
    var document: Document? = null
    var workArea: Float = 0F
    val fileName: String = "ticket.pdf"
    var activity: Activity? = null
    fun createPdf(activity: Activity, logo: RepLogo?, rows: List<Sale_Items>, _rowHeader: HashMap<Int, RowHeader>, _pageHeader: ArrayList<HeaderFooterRow>?,
                  _pageFooter: ArrayList<HeaderFooterRow>?, _summary: ArrayList<HeaderFooterRow>?, isRTL: Boolean, complete: (Boolean, String) -> Unit){

        val rowsCount : Int = rows.count() + 1 + (_pageHeader?.count() ?: 0) + (_summary?.count() ?: 0) + (_pageFooter?.count() ?: 0)
        val height: Float = 100f + (rowsCount * 50)

        val pgSize = Rectangle(300f, height)

        document = Document(pgSize, 0.1F, 0.1F, 0.1F, 0.1F)

        workArea = pgSize.width * 0.95F
        this.activity = activity
        Dexter.withActivity(activity)
            .withPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {

                    val path = Common.getAppPath(activity) + fileName
                    val fs = FileOutputStream(path)
                    val pdf = PdfTicket()
                    val writer = PdfWriter.getInstance(document, fs)
                    setDocumentInfo(document!!)
                    writer.pageEvent = pdf.setConfig(logo, _rowHeader, _pageHeader, _pageFooter, _summary, workArea, isRTL, pgSize)
                    pdf.writeBody(writer!!, document!!, rows)
                    complete(true, path)

                }

                override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken? ) {

                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {

                }
            })
            .check()

    }

    private fun setDocumentInfo(document: Document){
        document.addAuthor("Al-Hadetha For Software and Automation")
        document.addCreator("Mawared Van Sale Pdf")
        document.addKeywords("PDF tutorial invoice")
        document.addSubject("Describing the steps creating a PDF document")
        document.addTitle("PDF creation using iText - Sample invoice")
    }
    fun printPDF(act: Activity, path: String) {
        val printManager = act.getSystemService(Context.PRINT_SERVICE) as PrintManager

        try {
            val printAdapter = PdfDocumentAdapter(act, path)
            printManager.print("Document", printAdapter, PrintAttributes.Builder().build())

        }catch (e: Exception){
            Log.e("PdfActivity", "" + e.message)
        }
    }
}