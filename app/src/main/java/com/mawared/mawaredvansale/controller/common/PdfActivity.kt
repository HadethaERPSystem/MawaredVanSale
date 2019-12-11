package com.mawared.mawaredvansale.controller.common

import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.os.Environment
import android.print.PrintAttributes
import android.print.PrintManager
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.itextpdf.text.*
import com.itextpdf.text.pdf.*
import com.itextpdf.text.pdf.draw.LineSeparator
import com.itextpdf.text.pdf.draw.VerticalPositionMark
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.mawared.mawaredvansale.R
import org.w3c.dom.NodeList
import java.io.File
import java.io.FileOutputStream


class PdfActivity : AppCompatActivity() {

    var marginLeft = 1f
    var marginRight = 1f
    var marginTop = 1f
    var marginBottom = 1f

    val file_name: String = "test_pdf.pdf"
    @Suppress("UNCHECKED_CAST")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf)


        Dexter.withActivity(this)
            .withPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object :PermissionListener{
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    //val lines = intent.getJsonExtra("", List<PdfTicket>)
                    //val lines = intent.getSerializableExtra("TRS_PDF_TICKET")
                    //val bundle = intent.extras
                   // if(lines != null){
                        //val lines = bundle.getSerializable("TRS_PDF_TICKET")
                        //if(lines != null)
                  //      createPDFFile(Common.getAppPath(this@PdfActivity)+file_name, lines as List<PdfTicket>)

                   // }

                    val template = intent.getSerializableExtra("TRS_PDF_TICKET")
                    if(template != null){
                        createPDF1(Common.getAppPath(this@PdfActivity)+file_name, template as Template)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken? ) {

                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {

                }
            })
            .check()
    }



    private fun setPageSittings(document: Document){

        document.pageSize = PageSize.A4
        document.setMargins(marginLeft, marginRight, marginTop, marginBottom)
        document.addCreationDate()
        document.addAuthor("Mawared Van Sale")
        document.addCreator("Hadetha Software Automation")
    }

    private fun pdfHeader(document: Document, nodes: NodeList) {

    }

    private fun pdfFooter(document: Document, element: org.w3c.dom.Element){

    }

    private fun pdfBody(document: Document, body: org.w3c.dom.Element){

    }

    private fun getValue(tag: String, element: org.w3c.dom.Element): String?{
        val nodeList = element.getElementsByTagName(tag).item(0).childNodes
        val node = nodeList.item(0)
        return node.nodeValue
    }

    private fun createDoc(path: String): Document{
        val document = Document()
        //Save
        PdfWriter.getInstance(document, FileOutputStream(path))
        // open to write
        document.open()
        return document
    }

    private fun createPDFFile(path: String, lines: List<PdfTicket>) {
        if(File(path).exists())
            File(path).delete()

        try {

            val document = Document()
            //Save
            PdfWriter.getInstance(document, FileOutputStream(path))
            // open to write
            document.open()

            //setting
            document.pageSize = PageSize.A4
            document.addCreationDate()
            document.addAuthor("Mawared Van Sale")
            document.addCreator("Hadetha Software Automation")

            for (t in lines){
                when(t.type){
                    LineType.Text ->{
                        val fontName = BaseFont.createFont(t.fontName, "UTF-8", BaseFont.EMBEDDED)
                        val fontStyle = Font(fontName, t.fontSize, t.fontWeight, BaseColor.BLACK)
                        addNewItem(document, t.text!!, t.align, fontStyle)
                    }
                    LineType.NewLine -> {
                        addLineSpace(document)
                    }
                    LineType.LineSeparator -> {
                        addLineSeparator(document)
                    }
                    LineType.Image -> {

                    }
                    LineType.Column -> {
                        val cellCount = t.rows!![0].size
                        val pdfTable = PdfPTable(cellCount)

                        for (row in t.rows!!){
                            for (c in row){

                                val col = c.value
                                val width = col.width ?: col.text!!.length
                                val cell = PdfPCell()
                                val fontName = BaseFont.createFont(col.fontName, "UTF-8", BaseFont.EMBEDDED)
                                val fontStyle = Font(fontName, col.fontSize, col.fontWeight, BaseColor.BLACK)

                                val p = Phrase(col.text, fontStyle)
                                cell.borderWidth = 0f
                                cell.addElement(p)

                                pdfTable.addCell(cell)
                            }
                        }
                        document.add(pdfTable)
                    }
                    else -> {}
                }
            }
            // Font Setting
//            val colorAccent = BaseColor(0, 153, 204, 255)
//            val headingFontSize = 20.0f
//            val valueFontSize = 26.0f
//
//            // Custom font
//            val fontName = BaseFont.createFont("assets/fonts/brandon_medium.otf", "UTF-8", BaseFont.EMBEDDED)
//
//            val titleStyle = Font(fontName, 36.0f, Font.NORMAL, BaseColor.BLACK)
//            addNewItem(document, "Order Details", Element.ALIGN_CENTER, titleStyle)
//
//            val headingStyle = Font(fontName, headingFontSize, Font.NORMAL, colorAccent)
//            addNewItem(document, "Order No:", Element.ALIGN_LEFT, headingStyle)
//
//            val valueStyle = Font(fontName, valueFontSize, Font.NORMAL, BaseColor.BLACK)
//            addNewItem(document, "#123123", Element.ALIGN_LEFT, valueStyle)
//
//            // Add Line Separator
//            addLineSeparator(document)
//
//            addNewItem(document, "Order Date:", Element.ALIGN_LEFT, headingStyle)
//            addNewItem(document, "2019-12-03", Element.ALIGN_LEFT, valueStyle)
//
//            addLineSeparator(document)
//
//            addNewItem(document, "Account Name:", Element.ALIGN_LEFT, headingStyle)
//            addNewItem(document, "Ali Bawi", Element.ALIGN_LEFT, valueStyle)
//
//            addLineSeparator(document)
//
//            // Product Detail
//            addLineSpace(document)
//            addNewItem(document, "Product Details", Element.ALIGN_CENTER, titleStyle)
//
//            addLineSeparator(document)
//
//            //Item1
//            addNewItemWithLeftAndRight(document, "Pizza 25", "(0.0%)", titleStyle, valueStyle)
//            addNewItemWithLeftAndRight(document, "12.0.*1000", "12000.0", titleStyle, valueStyle)
//
//            addLineSeparator(document)
//
//            //Item2
//            addNewItemWithLeftAndRight(document, "Pizza 26", "(0.0%)", titleStyle, valueStyle)
//            addNewItemWithLeftAndRight(document, "12.0.*1000", "12000.0", titleStyle, valueStyle)
//
//            addLineSeparator(document)
//
//            // Total
//            addLineSpace(document)
//            addLineSpace(document)
//
//            addNewItemWithLeftAndRight(document, "Total", "24000.0", titleStyle, valueStyle)

            // close
            document.close()

            Toast.makeText(this@PdfActivity, "Success", Toast.LENGTH_SHORT).show()

            printPDF()

        }catch (e: Exception){
            Log.e("PdfActivity", "" + e.message)
        }
    }

    private fun header(writer: PdfWriter, document: Document, pdfTemplate: Template) {
        val cellCount = pdfTemplate.header!!.size
        val pdfTable = PdfPTable(cellCount)

        for (h in pdfTemplate.header!!) {


            for (c in h.header!!) {
                val col = c.value
                val width = col.width ?: col.text!!.length
                var color = BaseColor(0, 153, 204, 255)
                if(col.styleType == "V")
                    color = BaseColor.BLACK
                val cell = PdfPCell()
                val fontName = BaseFont.createFont(col.fontName, "UTF-8", BaseFont.EMBEDDED)
                val fontStyle = Font(fontName, col.fontSize, col.fontWeight, color)

                val p = Phrase(col.text, fontStyle)
                cell.borderWidth = 0f
                cell.addElement(p)

                pdfTable.addCell(cell)
            }
            pdfTable.writeSelectedRows(0, 300, 1f, 10f, writer.directContent)
            document.add(pdfTable)

        }
    }

    private fun footer(document: Document, pdfTemplate: Template) {
        val cellCount = pdfTemplate.header!!.size
        val pdfTable = PdfPTable(cellCount)
        for (h in pdfTemplate.footer!!) {

            for (c in h.header!!) {
                val col = c.value
                val width = col.width ?: col.text!!.length
                var color = BaseColor(0, 153, 204, 255)
                if(col.styleType == "V")
                    color = BaseColor.BLACK
                val cell = PdfPCell()
                val fontName = BaseFont.createFont(col.fontName, "UTF-8", BaseFont.EMBEDDED)
                val fontStyle = Font(fontName, col.fontSize, col.fontWeight, color)

                val p = Phrase(col.text, fontStyle)
                cell.borderWidth = 0f
                cell.addElement(p)

                pdfTable.addCell(cell)
            }
            document.add(pdfTable)
            addLineSpace(document)
        }
    }

    private fun body(document: Document, pdfTemplate: Template) {
        val cellCount = pdfTemplate.body!!.rows!![0].size
        val pdfTable = PdfPTable(cellCount)
        for (row in pdfTemplate.body!!.rows!!) {
            for (c in row){

                val col = c.value
                val width = col.width ?: col.text!!.length
                var color = BaseColor(0, 153, 204, 255)
                if(col.styleType == "V")
                    color = BaseColor.BLACK
                val cell = PdfPCell()
                val fontName = BaseFont.createFont(col.fontName, "UTF-8", BaseFont.EMBEDDED)
                val fontStyle = Font(fontName, col.fontSize, col.fontWeight, color)

                val p = Phrase(col.text, fontStyle)
                cell.borderWidth = col.boderWidth
                cell.addElement(p)

                pdfTable.addCell(cell)
            }
        }
        document.add(pdfTable)
    }

    private fun createPDF1(path: String, template: Template) {
        if(File(path).exists())
            File(path).delete()

        try {

            val document = Document()
            //Save
            PdfWriter.getInstance(document, FileOutputStream(path))
            // open to write
            document.open()

            //setting
            document.pageSize = PageSize.A4
            document.addCreationDate()
            document.addAuthor("Mawared Van Sale")
            document.addCreator("Hadetha Software Automation")

            //header(document, template)
            body(document, template)
            footer(document, template)

            // close
            document.close()

            Toast.makeText(this@PdfActivity, "Success", Toast.LENGTH_SHORT).show()

            printPDF()

        }catch (e: Exception){
            Log.e("PdfActivity", "" + e.message)
        }
    }
    private fun printPDF() {
        val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager

        try {
            val printAdapter = PdfDocumentAdapter(this@PdfActivity, Common.getAppPath(this@PdfActivity) + file_name)
            printManager.print("Document", printAdapter, PrintAttributes.Builder().build())

        }catch (e: Exception){
            Log.e("PdfActivity", "" + e.message)
        }
    }

    @Throws(DocumentException::class)
    private fun addNewItemWithLeftAndRight(document: Document, textLeft: String, textRight: String, leftStyle: Font, rightStyle: Font) {
        val chunkTextLeft = Chunk(textLeft, leftStyle)
        val chunkTextRight = Chunk(textRight, rightStyle)
        val p = Paragraph(chunkTextLeft)
        p.add(Chunk(VerticalPositionMark()))
        p.add(chunkTextRight)
        document.add(p)
    }

    @Throws(DocumentException::class)
    private fun addLineSeparator(document: Document) {
        val lineSeparator = LineSeparator()
        lineSeparator.lineColor = BaseColor(0,0,0,68)
        addLineSpace(document)
        document.add(Chunk(lineSeparator))
        addLineSpace(document)
    }

    @Throws(DocumentException::class)
    private fun addLineSpace(document: Document) {
        document.add(Paragraph(""))
    }

    @Throws(DocumentException::class)
    private fun addNewItem(document: Document, text: String, align: Int, style: Font) {
        val chunk = Chunk(text, style)
        val p = Paragraph(chunk)
        p.alignment = align
        document.add(p)

    }

    private fun addLineItemTable(){

    }

    private fun createNewPDF(){
        val myPdfDocument = android.graphics.pdf.PdfDocument()
        val myPageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(300, 600, 1).create()
        val myPage = myPdfDocument.startPage(myPageInfo)

        val myPaint = Paint()
        val myString = "Al-Hadetha For Software and Automation"
        val myString1 = "Mawared ERP System"

        val x = 10f
        var y = 25f;

        myPage.canvas.drawText(myString, x, y, myPaint)
        y += myString.length + 10;
        myPage.canvas.drawText(myString1, x, y, myPaint)

        val path = Environment.getExternalStorageDirectory().path + "/myPDFFile.Pdf"
        val myflie = File(path)

        try {
            myPdfDocument.writeTo(FileOutputStream(myflie))
        }catch (e: java.lang.Exception){
            e.printStackTrace()
        }

        myPdfDocument.close()
    }

}

