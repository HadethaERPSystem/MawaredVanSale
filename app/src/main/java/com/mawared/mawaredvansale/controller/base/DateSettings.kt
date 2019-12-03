package com.mawared.mawaredvansale.controller.base

import android.app.DatePickerDialog
import android.content.Context
import android.widget.DatePicker
import android.widget.Toast

class DateSettings : DatePickerDialog.OnDateSetListener {

    var ctx: Context

    constructor(ctx: Context){
        this.ctx = ctx
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        Toast.makeText(ctx, "Selected Date : $dayOfMonth / $month / $year", Toast.LENGTH_LONG ).show()
    }
}