package com.mawared.mawaredvansale.data.db.entities.reports.sales

class SalesStatement(
var row_no: Int?,
var cu_code: String?,
var cu_name_ar: String?,
var cu_name: String?,
var ref_no: String?,
var ref_date: String?,
var net_amount: Double?,
var pr_barcode: String?,
var pr_name_ar: String?,
var pr_name: String?,
//var pr_batch_no: String?,
//var pr_expiry_date: String?,
//var pr_mfg_date: String?,
var pr_image: String?,
var qty: Double?,
var total_net_amount: Double?,
var tqty: Double?
)