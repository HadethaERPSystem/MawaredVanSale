package com.mawared.mawaredvansale.data.db.entities.md

import android.util.Log
import androidx.core.text.TextUtilsCompat
import androidx.core.view.ViewCompat
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

@Entity
data class Product (
    var pr_code: String?,             // Product Name
    var pr_barcode: String?,          //Product barcode
    var pr_description: String?,      //Product Latin Name
    var pr_description_ar: String?,   //Product Arabic Name
    var pr_partno: String?,          //Product Arabic Name
    var pr_clientId: Int?,           //Client Id
    var pr_categoryId: Int?,          //Product Category
    var pr_brandId: Int?,             //Product Brand Name
    var pr_vat: Double?,              //Porduct Value Added Tax
    var pr_uom_Id: Int?,               //Unit of measurement
    var pr_SUoMEntry: Int?,
    var pr_SalUnitMsr: String?,
    var pr_NumInSale: Double?,
    var pr_SUoMQty: Double?,
    var pr_image_name: String?,
    var pr_qty: Double?,
    var pr_unit_price: Double?,
    var pr_price_AfD: Double?,
    var pr_batch_no: String?,
    var pr_expiry_date: String?,
    var pr_mfg_date: String?,
    var pr_wr_Id: Int?,
    var pr_prc_cat_Id: Int?,
    var ref_rowNo: Int?,
    var ref_Id: Int?,
    var ref_no: String?,
    var pr_is_batch_no: String?
){
    @PrimaryKey(autoGenerate = false)
    var pr_Id:  Int = 0
    var pr_wr_name: String? = null
    var prom_qty: Double? = null
    var prom_ex_qty: Double? = null
    var pr_dis_value: Double? = null
    var pr_dis_per: Double? = null
    var pr_dis_type: String? = null
    var pr_uom_code: String? = null
    var pr_d_discPrcnt: Double = 0.0
    var pr_isGift: Boolean = false
    var pr_user_discPrcnt: Double = 0.0
    var pr_disc_amnt: Double = 0.0
    var pr_user_disc_amnt: Double = 0.0
    var addQty: Double? = null
    fun returnDateString(isoString: String?) : String{
        try {
            if(isoString == null) return ""
            // 2017-09-11T01:16:13.858Z converted to below
            // Monday 4:35 PM format "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
            val enLang = Locale("en")
            val isLeftToRight = TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == ViewCompat.LAYOUT_DIRECTION_LTR
            val pattern: String = if(isLeftToRight) "dd-MM-yyyy" else "yyyy-MM-dd"
            val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            isoFormatter.timeZone = TimeZone.getTimeZone("UTC")
            var convertedDate = Date()
            try {
                convertedDate = isoFormatter.parse(isoString)!!
            }catch (e: ParseException){
                Log.d("PARSE", "Cannot parse date")
            }

            val outDateString = SimpleDateFormat(pattern, enLang)
            return  outDateString.format(convertedDate)
        }catch (e: Exception){
            Log.i("Exc", "Error in BaseViewModel returnDateString($isoString)")
            return ""
        }
    }

    fun numberFormat(value: Double?): String{
        val enLang = Locale("en")
        if(value == null) return "0.00"
        val formatedNumber = NumberFormat.getInstance(enLang).format(value)

        return formatedNumber
    }
}