package com.mawared.mawaredvansale.controller.common.printing

import android.util.Log
//import com.itextpdf.kernel.pdf.PdfWriter
//import com.itextpdf.kernel.pdf.PdfDocument
//import com.itextpdf.layout.Document
//import com.itextpdf.layout.element.Paragraph
import com.itextpdf.text.*
import com.itextpdf.text.pdf.*
import com.mawared.mawaredvansale.controller.common.Common
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Items
import java.io.File
import java.io.FileOutputStream
import java.util.ArrayList

class CreatePdf {
    fun createPdfFromString(content: String, filePath: String) {
        try {
            if (File(filePath).exists())
                File(filePath).delete()
           // val file = File(filePath)
//        val pdfWriter = PdfWriter(file)
//        val pdfDocument = PdfDocument(pdfWriter)
//        val document = Document(pdfDocument)
//
//        document.add(Paragraph(content))
            PageSetting().page = PageSize.POSTCARD
            val document =  Document(PageSize.POSTCARD, PageSetting().marginLeft, PageSetting().marginRight, PageSetting().marginTop, PageSetting().marginBottom)


            val fs = FileOutputStream(filePath)

            val writer = PdfWriter.getInstance(document, fs)
            writer.runDirection = PdfWriter.RUN_DIRECTION_RTL

            //Save

            // open to write
            document.open()

            createDoc(writer, document, content)
            //setting
//            document.pageSize = PageSize.SMALL_PAPERBACK
//            document.addCreationDate()
//            document.addAuthor("Mawared Van Sale")
//            document.addCreator("Hadetha Software Automation")
//            val fontNameAr = "assets/fonts/arial.ttf"
//            val fontName = BaseFont.createFont(fontNameAr, "UTF-8", BaseFont.EMBEDDED)
//            val fontStyle = Font(fontName, 12f, Font.NORMAL, BaseColor.BLACK)
//            val chunk = Chunk(content, fontStyle)
//            val p = com.itextpdf.text.Paragraph(chunk)
//            p.alignment = Element.ALIGN_CENTER
//            document.add(p)
//            //header(document, template)
//
//
//            document.close()
        }
        catch (e: Exception){
            Log.d("CreatePdf", "${e.message}")
        }
    }

    fun createDoc(writer: PdfWriter, document: Document, txt: String){
        try {
            val table = PdfPTable(1)
            document.open()
            var rowNo = 1
            var pageRows = 1
            val cellsWidth : ArrayList<Int> = arrayListOf() // intArrayOf(20, 8, 8, 8, 5, 20, 10, 5)
            table.spacingAfter = 10F
            table.defaultCell.verticalAlignment = Element.ALIGN_CENTER
            table.runDirection = PdfWriter.RUN_DIRECTION_RTL
            val workArea: Float = PageSize.POSTCARD.width
            table.totalWidth = workArea
            cellsWidth.add(265)

//            for (c in MawaredPdf1.tableHeader!!.sortedByDescending { it.order }){
//                cellsWidth.add(c.width.toInt())
//            }
           // if(cellsWidth.size > 0) {
              table.setWidths(cellsWidth.toIntArray())

            //}

            // Makes it possible to add text to a specific place in the document using
            // a X & Y placement syntax.
            // Makes it possible to add text to a specific place in the document using
            // a X & Y placement syntax.

            val cb: PdfContentByte = writer.directContent
            cb.saveState()
            cb.setColorStroke(BaseColor.BLACK)
            cb.rectangle(1F,1F,270F,416F)
            cb.stroke()
            cb.restoreState()

            var top_margin = 410F
            var left_margin = 10F
            val fontNameAr = "assets/fonts/arial.ttf"
            val fontName = BaseFont.createFont(fontNameAr, BaseFont.IDENTITY_H, true)
            val fontStyle = Font(fontName, 12f, Font.NORMAL, BaseColor.BLACK)

            val cell = PdfPCell(Phrase(8F, txt, fontStyle))
            cell.horizontalAlignment = Element.ALIGN_CENTER
            cell.backgroundColor = BaseColor.LIGHT_GRAY
            cell.setPadding(0.4F)

            table.addCell(cell)


            table.writeSelectedRows(0, -1, left_margin, top_margin, cb)
            table.writeSelectedRows(0, -1, left_margin, top_margin - 30, cb)
            table.flushContent()
            document.close()
            writer.close()
        }catch (e: Exception){
            throw e
        }
    }
}