package com.mawared.mawaredvansale.controller.printing_manager

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.helpers.loadLogo
import com.mawared.mawaredvansale.data.db.entities.reports.ReportTemplate
import com.mawared.mawaredvansale.utilities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class PrintManagerActivity : AppCompatActivity() {

    var data: Map<String, Any?>? = emptyMap()
    var summary: Map<String, Any?>? = emptyMap()
    var lines: ArrayList<Map<String, Any?>>? = arrayListOf()//<Map<String, Any?>>()
    var template: ReportTemplate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_print_manager)

        (intent.getSerializableExtra(ENTITY_DATA) as Map<String, Any?>?).also { data = it }
        (intent.getSerializableExtra(ENTITY_SUMMARY) as Map<String, Any?>?).also { summary = it }
        (intent.getSerializableExtra(ENTITY_LINES) as ArrayList<Map<String, Any?>>?).also { lines = it }
        (intent.getSerializableExtra(REPORT_TEMPLATE) as ReportTemplate).also { template = it }

        CoroutineScope(Dispatchers.Main).launch {
            val bitmap = loadLogo("${URL_GET_IMAGE}/CompanyInfo/${template?.header?.logo}")// loadImage(this@PrintManagerActivity, "${URL_GET_IMAGE}/CompanyInfo/${template?.header?.logo}")

            val preview = findViewById<SimpleTemplate>(R.id.template)
            preview.logoBmp = bitmap

            val btnPrint = findViewById<Button>(R.id.btnPrint)
            btnPrint.setOnClickListener {
                val bmp = preview.createReceipt()
                if(bmp != null){
                    PrintingObject(this@PrintManagerActivity).print(bmp)
                }
            }
            showPreviw(preview)
        }
    }


    private fun showPreviw(preview: SimpleTemplate) {
        preview.setBackgroundPaint(1)
        preview.setData(data, lines, summary, template)
        preview.invalidate()
    }
}
