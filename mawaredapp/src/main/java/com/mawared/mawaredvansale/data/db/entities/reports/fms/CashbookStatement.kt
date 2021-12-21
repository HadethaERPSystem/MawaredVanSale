package com.mawared.mawaredvansale.data.db.entities.reports.fms

class CashbookStatement (
    var row_no: Int?,
    var cu_code: String?,
    var cu_name_ar: String?,
    var cu_name: String?,
    var rcv_amount: Double?,
    var py_amount: Double?,
    var balance: Double?,
    var total_rcv_amount: Double?,
    var total_py_amount: Double?,
    var total_balance: Double?
)

class ReportRowHeader(
    var colHeader1: String = "",
    var colHeader2: String = "",
    var colHeader3: String = "",
    var colHeader4: String = "",
    var colHeader5: String = "",
    var colHeader6: String = "",
    var colHeader7: String = "",
    var colHeader8: String = "",
    var colHeader9: String = "",
    var colHeader10: String = "",
    var colHeader11: String = "",
    var colHeader12: String = ""

)