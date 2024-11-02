package com.mawared.mawaredvansale.data.db.entities.reports
import java.io.Serializable


class ReportTemplate (val settings: ReportSettings , val header: Header, val rowheader: RowHeader, val details: Details, val summary: Summary, val footer: Footer):Serializable

class Header(val logo: String?, val xPos: Float?, val yPos: Float?, val height: Int = 0, val rows: ArrayList<LineDataReport>, val saperator: DataInfo?):Serializable

class RowHeader(val columns: ArrayList<ColumnDataReport>, val saperator: DataInfo?):Serializable

class Details(val columns: ArrayList<ColumnDataReport>, val saperator: DataInfo?):Serializable

class Summary(val rows: ArrayList<LineDataReport>, val saperator: DataInfo?):Serializable

class Footer(val rows: ArrayList<LineDataReport>, val saperator: DataInfo?):Serializable

class TypeFace(val font: String?, val size: Float?, val style: String?, val color: String?, val align: String?, val justfy: String?, val textWrap: String?):Serializable

class DataInfo(val type: String, val label: String?, val field: String?, val typeface: TypeFace?, val pointX: Float?, val start: Float?, val width: Float?):Serializable

class ColumnDataReport(val type: String, val label: String?, val field: String?, val typeface: TypeFace?, val pointX: Float?, val start: Float?, val width: Float?, val format:String?, val barcode_width: Int?, val barcode_height: Int?):Serializable

class LineDataReport(val width: Float?, val columns : ArrayList<ColumnDataReport>?):Serializable

class ReportSettings(val width: Float?, val xMargin: Float?, val yMargin: Float?, val lineHeight: Float?, val textSpacing: Float?) : Serializable