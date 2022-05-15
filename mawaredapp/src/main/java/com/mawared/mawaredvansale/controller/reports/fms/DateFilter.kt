package com.mawared.mawaredvansale.controller.reports.fms

class DateFilter(
    val dtFrom: String?,
    val dtTo: String?
)

class SearchFilter(
    val cu_Id: Int =0,
    val dtFrom: String?,
    val dtTo: String?
)

class ItemFilter(val cat_id: Int?, val br_id: Int?, val term: String?)