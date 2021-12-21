package com.mawared.mawaredvansale.data.db.entities.reports.customer

class CustomerStatement(
    var row_no: Int?,
    var cu_code: String?,
    var cu_name_ar: String?,
    var cu_name: String?,
    var ref_no: String?,
    var ref_date: String?,
    var net_amount: Double?,
    var rcv_amount: Double?,
    var py_amount: Double?,
    var total_net_amount: Double?,
    var total_rcv_amount: Double?,
    var total_py_amount: Double?,
    var cu_balance: Double?
)