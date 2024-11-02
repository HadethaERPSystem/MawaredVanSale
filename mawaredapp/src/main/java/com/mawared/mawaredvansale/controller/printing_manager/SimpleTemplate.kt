package com.mawared.mawaredvansale.controller.printing_manager

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.text.Layout
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.View
import androidx.core.content.ContextCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.helpers.FormatterHelper
import com.mawared.mawaredvansale.data.db.entities.reports.ColumnDataReport
import com.mawared.mawaredvansale.data.db.entities.reports.DataInfo
import com.mawared.mawaredvansale.data.db.entities.reports.ReportTemplate
import com.mawared.mawaredvansale.data.db.entities.reports.TypeFace
import com.mawared.mawaredvansale.utilities.Coroutines
import com.mawared.mawaredvansale.utilities.PosBluetoothPrinter
import com.mawared.mawaredvansale.utilities.URL_GET_IMAGE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.URL
import kotlin.math.abs

class SimpleTemplate @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val printDataList: MutableList<PrintData> = java.util.ArrayList()
    private val paint = Paint()
    private val textPaint = Paint()
    private val linePaint = Paint()
    private val contentBgPaint = Paint()
    private val titleBgPaint = Paint()
    private var xWidth = 750f// 385f
    private var xHeight = 0f
    private var yMargin = 10f
    private var xMargin = 10f
    private var textSpacing = 5f
    private var yPointer = 0f
    private var lineHeight = 28f
    var logoBmp: Bitmap? = null

    var data: Map<String, Any?>? = emptyMap()
    var summary: Map<String, Any?>? = emptyMap()
    var lines: ArrayList<Map<String, Any?>>? = arrayListOf()//<Map<String, Any?>>()
    var template: ReportTemplate? = null

    init {

        paint.color = ContextCompat.getColor(context, R.color.black)
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL_AND_STROKE

        linePaint.color = ContextCompat.getColor(context, R.color.black)
        linePaint.style = Paint.Style.STROKE
        linePaint.strokeWidth = 2f

        textPaint.color = ContextCompat.getColor(context, R.color.black)
        textPaint.isAntiAlias = true


        contentBgPaint.color = ContextCompat.getColor(context, R.color.colorPrimary)
        contentBgPaint.isAntiAlias = true
        contentBgPaint.style = Paint.Style.FILL_AND_STROKE
        contentBgPaint.strokeWidth = 1f

        titleBgPaint.color = ContextCompat.getColor(context, R.color.teak_template)
        titleBgPaint.isAntiAlias = true
        titleBgPaint.style = Paint.Style.FILL_AND_STROKE

    }

    fun setData(data: Map<String, Any?>?, rows: ArrayList<Map<String, Any?>>?, summary: Map<String, Any?>?, template: ReportTemplate?){
        this.data = data
        this.lines = rows
        this.summary = summary
        this.template = template
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        initialize()
        canvas.apply {
            drawLogo(canvas, logoBmp)
            drawHeader(canvas)
            drawItemHeader(canvas)
            drawItems(canvas)
            drawSummary(canvas)
            drawFooter(canvas)
        }
    }
    fun createReceipt() : Bitmap?{
        initialize()
        val bm = Bitmap.createBitmap(xWidth.toInt(), xHeight.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bm)
//        for(item in printDataList){
//            canvas.drawText(item.text, item.xPos, item.yPos, item.paint)
//        }
//
        val bm1 = Bitmap.createBitmap(380, 100, Bitmap.Config.ARGB_8888)
        val canvas1 = Canvas(bm1)
        val paint = Paint()

        setTextSize(paint, 16f)
        setPaintColor(paint, getColor(R.color.black))
        setTypFace(paint, Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD))
        paint.textAlign = Paint.Align.CENTER
        canvas1.drawText("الشركة الحديثة للبرامجيات", 150f, 20f, paint)
//        canvas.apply {
//            drawLogo(canvas, logoBmp)
//            drawHeader(canvas)
//            drawItemHeader(canvas)
//            drawItems(canvas)
//            drawSummary(canvas)
//            drawFooter(canvas)
//        }

        return bm1
    }

    fun initialize() {
        xHeight = calculateReceiptHeight()

        val displayMetrics = DisplayMetrics()
        (context as Activity).windowManager
            .defaultDisplay
            .getMetrics(displayMetrics)
        xWidth = displayMetrics.widthPixels.toFloat()

        if(template?.settings != null){
           template!!.settings.let {
               xWidth = it.width!!
               xMargin= it.xMargin!!
               yMargin = it.yMargin!!
               lineHeight = it.lineHeight!!
               textSpacing = it.textSpacing!!
           }
        }
    }

    fun loadLogo(success: () -> Unit){
        try{
            if(!template?.header?.logo.isNullOrEmpty()) {
                val logoPath = URL_GET_IMAGE + "/CompanyInfo/" + template?.header?.logo
                Coroutines.ioThenMain({
                    val conn = withContext(Dispatchers.IO) {
                        URL(logoPath).openConnection()
                    }
                    withContext(Dispatchers.IO) {
                        conn.connect()
                    }
                    val length = conn.contentLength
                    var bmp: Bitmap? = null
                    if (length > 0) {

                        val `is`: InputStream =
                            withContext(Dispatchers.IO) {
                                conn.getInputStream()
                            }
                        bmp = BitmapFactory.decodeStream(`is`)
                    }
                    logoBmp = bmp

                }, {
                    success()
                })
            }else{
                success()
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    private fun drawLogo(canvas: Canvas?, bmp: Bitmap?){
        try {
            yPointer = 0f
            if (!template?.header?.logo.isNullOrEmpty() && bmp != null) {

                var left = xWidth / 2
                var top = yMargin

                if (template?.header?.xPos != null) {
                    left = xWidth * template?.header?.xPos!!
                }
                if (template?.header?.yPos != null) {
                    top = yPointer + template?.header?.yPos!!
                }

                left -= (bmp.width / 2f)
                canvas?.drawBitmap(bmp, left, top, paint)
                yPointer += bmp.height + yMargin

            }
        }
        catch (e: Exception){
            e.printStackTrace()
        }
    }

    private fun drawTextBounds(canvas: Canvas, rect: Rect, x: Int, y: Int) {
        textPaint.color = Color.rgb(0, 0, 0)
        textPaint.style = Paint.Style.STROKE
        textPaint.strokeWidth = 1f
        rect.offset(x, y)
        canvas.drawRect(rect, textPaint)
    }

    private fun drawHeader(canvas: Canvas?){
        if(data != null && template?.header?.rows != null){
            for(row in template!!.header.rows){

                //setPaintColor(contentBgPaint, getColor(R.color.white))
                setTextSize(textPaint, 16f)
                setPaintColor(textPaint, getColor(R.color.black))
                setTypFace(textPaint, Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD))
                val top = yPointer + textSpacing
                val bottom = top + lineHeight
                yPointer = bottom
                val rect = RectF(xMargin, top, xWidth - xMargin, bottom )

                if(row.columns != null){
                    for (col in row.columns){

                        if(col.typeface != null){
                            setTypFace(textPaint, Typeface.createFromAsset(context.assets, col.typeface.font))
                            textAlign(col.typeface, textPaint)
                            if(col.typeface.size != null && col.typeface.size != 0f){
                                setTextSize(textPaint, col.typeface.size)
                            }
                        }
                        val txt = getText(col, data)
                        if(txt.isNotEmpty()){
                            val pointX = col.pointX!! * xWidth
                            drawText(txt, pointX, rect, canvas, textPaint)
                        }
                    }
                }
            }
            if(template!!.header.saperator != null){
                template!!.header.saperator.let {
                    drawLine(it!!, canvas)
                }
            }
        }
    }

    private fun drawItemHeader(canvas: Canvas?){
        if(data != null && template?.rowheader?.columns != null){
            val top = yPointer + yMargin
            val bottom = top + lineHeight
            yPointer = bottom
            val rect = RectF(xMargin, top, xWidth, bottom )

            //canvas?.drawRect(rect, titleBgPaint)
            setTextSize(textPaint, 16f)
            setPaintColor(textPaint, getColor(R.color.black))
            setTypFace(textPaint, Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD))
            for(col in template!!.rowheader.columns){
                if(col.typeface != null){
                    setTypFace(textPaint, Typeface.createFromAsset(context.assets, col.typeface.font))
                    textAlign(col.typeface, textPaint)
                    if(col.typeface.size != null && col.typeface.size != 0f){
                        setTextSize(textPaint, col.typeface.size)
                    }
                }
                val txt = getText(col, data)
                if(txt.isNotEmpty()){
                    val pointX = col.pointX!! * xWidth
                    drawText(txt, pointX, rect, canvas, textPaint)
                }
            }
        }
    }

    private fun drawItems(canvas: Canvas?) {

        if (lines != null && template?.details?.columns != null) {
            val startY = yPointer + yMargin
            var endY = 0f

            lines!!.forEach { line ->

                val top = yPointer + yMargin
                val bottom = top + lineHeight
                yPointer = bottom
                endY = bottom
                val rect = RectF(
                    xMargin,
                    top,
                    xWidth - xMargin,
                    bottom
                )

                setTextSize(textPaint, 16f)
                setPaintColor(textPaint, getColor(R.color.black))
                setTypFace(textPaint, Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD))
                for (col in template!!.details.columns){
                    var textWrap = "N"
                    col.typeface?.let { tf ->
                        setTypFace(textPaint, Typeface.createFromAsset(context.assets, tf.font))
                        textAlign(tf, textPaint)
                        if (tf.size != null && tf.size != 0f) {
                            setTextSize(textPaint, tf.size)
                        }
                        textWrap = tf.textWrap ?: "N"
                    }

                    val txt = getText(col, line)
                    if (txt.isNotEmpty()) {
                        if (textWrap == "N") {
                            val pointX = col.pointX!! * xWidth
                            drawText(txt, pointX, rect, canvas, textPaint)
                        } else {
                            drawTextMultiline(txt, col, rect, canvas, textPaint)
                        }
                    }
                }
            }

            val rect = RectF(xMargin, startY, xWidth, endY )

            //canvas?.drawRect(rect, linePaint)
        }
    }

    private fun drawSummary(canvas: Canvas?){
        if(summary != null && template?.summary?.rows != null){
            for(row in template!!.summary.rows){
                setTextSize(textPaint, 16f)
                setPaintColor(textPaint, getColor(R.color.black))
                setTypFace(textPaint, Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD))
                val top = yPointer + textSpacing
                val bottom = top + lineHeight
                yPointer = bottom
                val rect = RectF(xMargin, top, (xWidth * (row.width ?: 1f) + xMargin), bottom )

                //canvas?.drawRect(rect, titleBgPaint)

                if(row.columns != null){
                    for(col in row.columns){
                        if(col.typeface != null){
                            setTypFace(textPaint, Typeface.createFromAsset(context.assets, col.typeface.font))
                            textAlign(col.typeface, textPaint)
                            if(col.typeface.size != null && col.typeface.size != 0f){
                                setTextSize(textPaint, col.typeface.size)
                            }
                        }
                        val txt = getText(col, summary)
                        if(txt.isNotEmpty()){
                            val pointX = col.pointX!! * xWidth
                            drawText(txt, pointX, rect, canvas, textPaint)
                        }
                    }
                }

            }
            if(template!!.summary.saperator != null){
                template!!.summary.saperator.let {
                    drawLine(it!!, canvas)
                }
            }
        }
    }

    private fun drawFooter(canvas: Canvas?){
        if(data != null && template?.footer?.rows != null){
            for(row in template!!.footer.rows) {
                setTextSize(textPaint, 16f)
                setPaintColor(textPaint, getColor(R.color.black))
                setTypFace(textPaint, Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD))
                val top = yPointer + textSpacing
                val bottom = top + lineHeight
                yPointer = bottom
                val rect = RectF(xMargin, top, xWidth - xMargin, bottom )

                //canvas?.drawRect(rect, contentBgPaint)

                if(row.columns != null){

                    for(col in row.columns) {
                        col.typeface.let { tf ->
                            if(!tf?.font.isNullOrEmpty()){
                                setTypFace(textPaint, Typeface.createFromAsset(context.assets, tf!!.font))
                            }

                            textAlign(tf!!, textPaint)
                            if(tf.size != null && tf.size != 0f){
                                setTextSize(textPaint, tf.size)
                            }
                        }
                        val txt = getText(col, data)
                        if(col.type == "barcode")
                        {
                            drawBarcode(txt, col, canvas, textPaint)
                        }
                        else{
                            if(txt.isNotEmpty()){
                                val pointX = col.pointX!! * xWidth
                                drawText(txt, pointX, rect, canvas, textPaint)
                            }
                        }

                    }
                }

            }
            if(template!!.footer.saperator != null){
                template!!.footer.saperator.let {
                    drawLine(it!!, canvas)
                }
            }
        }
    }

    //https://www.w3schools.com/tags/canvas_textalign.asp
    private fun drawText(text: String, pointX: Float, rect: RectF, canvas: Canvas?, paint: Paint){
        val metrics = paint.fontMetrics;
        val acent = abs(metrics.ascent)
        val descent = abs(metrics.descent)
        val y = rect.centerY() + (acent - descent) / 2f
        canvas!!.drawText(text, pointX, y, paint)
        printDataList.add(PrintData(pointX, y, text, paint))

    }

    private fun drawTextMultiline(text: String, col: ColumnDataReport, rect: RectF, canvas: Canvas?, paint: Paint){

        val width = (col.width!! * xWidth)
        val pointX = col.pointX!! * xWidth

        val textWeight = paint.measureText(text)

        if (textWeight > width) {
            var i = 1
            val arr = splitStringOnRatio(text, width, paint)
            var rec = rect
            arr.map {
                drawText(it, pointX, rec, canvas, paint)
                if(i < arr.count()) {
                    val top = yPointer + yMargin
                    val bottom = top + 15
                    yPointer = bottom
                    rec = RectF(
                        xMargin,
                        top,
                        xWidth - xMargin,
                        bottom
                    )
                }
                i++
            }
        }
        else{
            drawText(text, pointX, rect, canvas, paint)
        }

    }

    private fun splitStringOnRatio(text: String, targetWidth: Float, paint: Paint): List<String> {
        val words = text.split(" ")
        val segments = mutableListOf<String>()
        var currentSegment = StringBuilder()
        var currentSegmentWidth = 0f

        for (word in words) {
            val wordWidth = paint.measureText(word)
            if (currentSegmentWidth + wordWidth > targetWidth) {
                segments.add(currentSegment.toString())
                currentSegment = StringBuilder(word)
                currentSegmentWidth = wordWidth
            } else {
                if (currentSegment.isNotEmpty()) {
                    currentSegment.append(" ")
                    currentSegmentWidth += paint.measureText(" ")
                }
                currentSegment.append(word)
                currentSegmentWidth += wordWidth
            }
        }
        if (currentSegment.isNotEmpty()) {
            segments.add(currentSegment.toString())
        }

        return segments
    }

    private fun drawBarcode(text: String, col: ColumnDataReport, canvas: Canvas?, paint: Paint){
        try {
            val bmp = createBarCode(text, BarcodeFormat.CODE_128, col.barcode_width ?: 200, col.barcode_height ?: 60)

            var left = xWidth / 2

            if (col.pointX != null) {
                left = xWidth * col.pointX
            }

            val top = yPointer

            left -= (bmp.width / 2f)
            canvas?.drawBitmap(bmp, left, top, paint)

            yPointer += bmp.height + yMargin
            val bottom = yPointer + lineHeight

            val rect = RectF(xMargin, yPointer, xWidth - xMargin, bottom )
            drawText(text,  xWidth * col.pointX!!, rect, canvas, paint)
            yPointer += lineHeight
        }catch (e: Exception){
            e.printStackTrace()
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

    private fun getLayoutAlign(align: String) : Layout.Alignment {
        when(align){
            "left" -> return Layout.Alignment.ALIGN_NORMAL
            "right" -> return Layout.Alignment.ALIGN_OPPOSITE
            "center" -> return Layout.Alignment.ALIGN_CENTER
            else -> return  Layout.Alignment.ALIGN_NORMAL
        }
    }

    private fun getJustification(textJust: String): Int{
        when(textJust){
            "j" -> return Layout.JUSTIFICATION_MODE_INTER_WORD
            else -> return  Layout.JUSTIFICATION_MODE_NONE
        }
    }

    private fun drawLine(it: DataInfo, canvas: Canvas?){
        val startX = xMargin
        val stopX = xWidth * it.width!!
        yPointer += 5
        //canvas?.drawLine(startX, yPointer, stopX,  yPointer, linePaint )
    }

    fun setBackgroundPaint(templateId: Int?) {
        when (templateId) {
            1 -> titleBgPaint.color = ContextCompat.getColor(context, R.color.gray)
            2 -> titleBgPaint.color = ContextCompat.getColor(context, R.color.lineBlue)
            3 -> titleBgPaint.color = ContextCompat.getColor(context, R.color.white)
            4 -> titleBgPaint.color = ContextCompat.getColor(context, R.color.teak_template)
            else -> {
                titleBgPaint.color = ContextCompat.getColor(context, R.color.teak_template)
            }
        }
    }

    private fun getColor(colorId: Int) = ContextCompat.getColor(context, colorId)
    private fun setTextSize(paint: Paint, size: Float) {
        paint.textSize = size.spToPx()
    }
    private fun setPaintColor(paint: Paint, color: Int) {
        paint.color = color
    }
    private fun setTypFace(paint: Paint, typeface: Typeface) {
        paint.typeface = typeface
    }
    private fun textAlignCentre(paint: Paint) {
        paint.textAlign = Paint.Align.CENTER
    }
    private fun textAlignLeft(paint: Paint) {
        paint.textAlign = Paint.Align.LEFT
    }
    private fun textAlignRight(paint: Paint) {
        paint.textAlign = Paint.Align.RIGHT
    }

    private fun textAlign(typeface: TypeFace, paint: Paint) {
        when(typeface.align){
            "left"-> paint.textAlign = Paint.Align.LEFT
            "right"-> paint.textAlign = Paint.Align.RIGHT
            "center"-> paint.textAlign = Paint.Align.CENTER
            else -> Paint.Align.LEFT
        }

    }


    private fun getText(col: ColumnDataReport, record: Map<String, Any?>?): String{
        if(col.type == "data"){
            return "${col.label}${record?.get(col.field).toString()}"
        }
        else if(col.type == "field"){
            var value = "${record?.get(col.field)}"

            if(!col.format.isNullOrEmpty()){
                when(col.format){
                    "N" -> value = FormatterHelper.numberFormat(record?.get(col.field) as Double?)
                    "D" -> value = FormatterHelper.returnUKDateString(record?.get(col.field) as String?)
                }
            }
            return value
        }
        else if(col.type == "label"){
            return "${col.label}"
        }else if(col.type == "barcode") {
            return "${record?.get(col.field)}"
        }else if(col.type == "newline"){
            yPointer += lineHeight
        }
        return ""
    }

    private fun calculateReceiptHeight() : Float{
        val mpaint = Paint()
        setTextSize(mpaint, 16f)
        var height: Float = 0f
        if(!template?.header?.logo.isNullOrEmpty()){
            if(logoBmp != null){
                height += logoBmp!!.height
            }
        }
        if(template?.header?.rows != null && data != null){
            for(row in template!!.header.rows){
                if(row.columns != null){
                    row.columns[0].let {
                        if(it.type == "field"){
                            val bounds = Rect()
                            if(it.typeface != null){
                                setTypFace(mpaint, Typeface.createFromAsset(context.assets, it.typeface.font))
                                textAlign(it.typeface, mpaint)
                                if(it.typeface.size != null && it.typeface.size != 0f){
                                    setTextSize(mpaint, it.typeface.size)
                                }
                            }
                            val txt = getText(it, data)
                            mpaint.getTextBounds(txt, 0, txt.length, bounds)
                            height += bounds.height()
                        }
                        else if(it.type == "label"){
                            val bounds = Rect()
                            if(it.typeface != null){
                                setTypFace(mpaint, Typeface.createFromAsset(context.assets, it.typeface.font))
                                textAlign(it.typeface, mpaint)
                                if(it.typeface.size != null && it.typeface.size != 0f){
                                    setTextSize(mpaint, it.typeface.size)
                                }
                            }
                            mpaint.getTextBounds(it.label, 0, it.label!!.length, bounds)
                            height += bounds.height()
                        }
                        else{
                            height += lineHeight
                        }
                    }
                }
            }
        }

        template?.header?.saperator.let {
            if(it != null){
                height += lineHeight
            }
        }

        if(template?.rowheader != null) {
            template!!.rowheader.columns[0].let {

                if (it.type == "label") {
                    val bounds = Rect()
                    if(it.typeface != null){
                        setTypFace(mpaint, Typeface.createFromAsset(context.assets, it.typeface.font))
                        textAlign(it.typeface, mpaint)
                        if(it.typeface.size != null && it.typeface.size != 0f){
                            setTextSize(mpaint, it.typeface.size)
                        }
                    }
                    mpaint.getTextBounds(it.label, 0, it.label!!.length, bounds)
                    height += bounds.height()
                } else {
                    height += lineHeight
                }
            }
            if (template?.rowheader?.saperator != null) {
                height += lineHeight
            }
        }

        if(template?.details != null){
           for(line in  lines!!) {
                val bounds = Rect()
               val col = template!!.details.columns[0]
               if(col.typeface != null){
                   setTypFace(mpaint, Typeface.createFromAsset(context.assets, col.typeface.font))
                   textAlign(col.typeface, mpaint)
                   if(col.typeface.size != null && col.typeface.size != 0f){
                       setTextSize(mpaint, col.typeface.size)
                   }
               }
                mpaint.getTextBounds(line[template!!.details.columns[0].field].toString(), 0, line[template!!.details.columns[0].field].toString().length, bounds)
                height += bounds.height()
            }
            if (template!!.details.saperator != null) {
                height += lineHeight
            }
        }
        // print sammary
        if(summary != null && template?.summary?.rows != null){
            for(row in template!!.summary.rows) {
                if(row.columns != null){
                    row.columns[0].let {
                        if(it.type == "field"){
                            val bounds = Rect()
                            if(it.typeface != null){
                                setTypFace(mpaint, Typeface.createFromAsset(context.assets, it.typeface.font))
                                textAlign(it.typeface, mpaint)
                                if(it.typeface.size != null && it.typeface.size != 0f){
                                    setTextSize(mpaint, it.typeface.size)
                                }
                            }
                            val txt = getText(it, data)
                            mpaint.getTextBounds(txt, 0, txt.length, bounds)
                            height += bounds.height()
                        }
                        else if(it.type == "label"){
                            val bounds = Rect()
                            if(it.typeface != null){
                                setTypFace(mpaint, Typeface.createFromAsset(context.assets, it.typeface.font))
                                textAlign(it.typeface, mpaint)
                                if(it.typeface.size != null && it.typeface.size != 0f){
                                    setTextSize(mpaint, it.typeface.size)
                                }
                            }
                            mpaint.getTextBounds(it.label, 0, it.label!!.length, bounds)
                            height += bounds.height()
                        }
                        else{
                            height += lineHeight
                        }
                    }
                }
            }
            if (template!!.summary.saperator != null) {
                height += lineHeight
            }
        }
        // print footer
        if(data != null && template?.footer?.rows != null){
            for(row in template!!.footer.rows)
            {
                if(row.columns != null){
                    row.columns[0].let {
                        if(it.type == "field"){
                            val bounds = Rect()
                            if(it.typeface != null){
                                setTypFace(mpaint, Typeface.createFromAsset(context.assets, it.typeface.font))
                                textAlign(it.typeface, mpaint)
                                if(it.typeface.size != null && it.typeface.size != 0f){
                                    setTextSize(mpaint, it.typeface.size)
                                }
                            }
                            val txt = getText(it, data)
                            mpaint.getTextBounds(txt, 0, txt.length, bounds)
                            height += bounds.height()
                        }
                        else if(it.type == "label"){
                            val bounds = Rect()
                            if(it.typeface != null){
                                setTypFace(mpaint, Typeface.createFromAsset(context.assets, it.typeface.font))
                                textAlign(it.typeface, mpaint)
                                if(it.typeface.size != null && it.typeface.size != 0f){
                                    setTextSize(mpaint, it.typeface.size)
                                }
                            }
                            mpaint.getTextBounds(it.label, 0, it.label!!.length, bounds)
                            height += bounds.height()
                        }
                        else{
                            height += lineHeight
                        }
                    }
                }
            }
            if (template!!.summary.saperator != null) {
                height += lineHeight
            }
        }
        return height
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

    private fun Float.dpToPx() = this * resources.displayMetrics.density
    private fun Int.dpToPx() = (this * resources.displayMetrics.density).toInt()
    private fun Float.spToPx() = this * resources.displayMetrics.scaledDensity
    private fun Int.pxToDp() = (this * 160) / resources.displayMetrics.densityDpi
}