package com.mawared.mawaredvansale.controller.common.printing

import com.itextpdf.text.Document
import com.itextpdf.text.pdf.PdfWriter
import java.lang.reflect.ParameterizedType
import kotlin.reflect.full.memberProperties

class PdfGenerator {

    var write: PdfWriter? = null
    var docuemnt: Document? = null
    var file_name: String = "test.pdf"

    var labelHeaderFont: ReportFont? = null
    var valueHeaderFont: ReportFont? = null

    var labelFilterFont: ReportFont? = null
    var valueFilterFont: ReportFont? = null

    var labelRowHeaderFont: ReportFont? = null
    var valueRowBodyFont: ReportFont? = null

    //==================== Reports properties
    private var hasHeader: Boolean = true
    private var hasFooter: Boolean = true

    private var headerRow: HashMap<String, RowHeader>? = null
    private var pageHeader: HashMap<String, String>? = null
    private var pageFooter: HashMap<String, String>? = null
    private var pageFilter: HashMap<String, PageFilter>? = null

    private var tableWidth: Float = 0f
    private var cellWidth: Array<Float> = arrayOf()
    private var beginOfTable = 0f

    private var yPos = 0f
    private var xPos = 0f

    private var ratioOfBreakTable = 0f


    fun <T> buildDocument(rows: ArrayList<T>, rowHeader: HashMap<String, RowHeader>, pageHeader: HashMap<String, String>?,
                          pageFooter: HashMap<String, String>?, reportFitler: HashMap<String, PageFilter>){

        ratioOfBreakTable = 0.55f
        hasHeader = false
        hasFooter = false
        if(pageHeader!!.contains("hasHeader") || pageFooter!!.containsKey("hasFooter")){
            ratioOfBreakTable = 045f
            hasHeader = true
        }
        if(pageFooter!!.containsKey("hasFooter")){
            hasFooter = true
        }

        val hasBottomLable = false
        val d: T = rows[0]

        val NameOfReports = ((javaClass
            .genericSuperclass as ParameterizedType?)?.getActualTypeArguments()?.get(0) as Class<T>).name

    }

    fun writePageHeader(){

    }

    fun writeRowHeader(){

    }

    fun writeBody(){

    }

    fun writeSummary(){

    }

    fun writeFooter(){

    }

    @Throws(IllegalAccessException::class, ClassCastException::class)
    inline fun <reified T> Any.getField(fieldName: String): T? {

        this::class.memberProperties.forEach { kCallable ->
            if (fieldName == kCallable.name) {
                return kCallable.getter.call(this) as T?
            }
        }
        return null
    }
}