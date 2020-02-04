package com.mawared.mawaredvansale.controller.fms.receivables.receivablelist

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.base.BaseViewModel
import com.mawared.mawaredvansale.controller.common.printing.*
import com.mawared.mawaredvansale.data.db.entities.fms.Receivable
import com.mawared.mawaredvansale.interfaces.IMainNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.services.repositories.fms.IReceivableRepository
import java.io.IOException
import java.io.InputStream
import java.util.*

class ReceivableViewModel(private val repository: IReceivableRepository) : BaseViewModel() {
    private val _sm_id: Int = if(App.prefs.savedSalesman?.sm_user_id != null)  App.prefs.savedSalesman!!.sm_user_id!! else 0

    var navigator: IMainNavigator<Receivable>? = null
    var msgListener: IMessageListener? = null
    var activity: AppCompatActivity? = null
    var isPrint = false
    private val _cu_id: MutableLiveData<Int> = MutableLiveData()

    val baseEoList: LiveData<List<Receivable>> = Transformations
        .switchMap(_cu_id){
            repository.getReceivable(_sm_id, it)
        }

    private val _rcv_Id_for_delete: MutableLiveData<Int> = MutableLiveData()
    val deleteRecord: LiveData<String> = Transformations
        .switchMap(_rcv_Id_for_delete){
            repository.delete(it)
        }

    private val _rcv_Id: MutableLiveData<Int> = MutableLiveData()
    val baseEo: LiveData<Receivable> = Transformations
        .switchMap(_rcv_Id) {
            repository.getById(it)
        }
    // set functions to refresh data
    fun setCustomer(cm_Id: Int?){
        if(_cu_id.value == cm_Id && cm_Id != null){
            return
        }
        _cu_id.value = cm_Id
    }

    fun find(id: Int) {
        if (_rcv_Id.value == id) {
            return
        }
        _rcv_Id.value = id
    }

    // confirm delete
    fun confirmDelete(baseEo: Receivable){
        _rcv_Id_for_delete.value = baseEo.rcv_Id
    }

    fun onItemDelete(baseEo: Receivable)
    {
        navigator?.onItemDeleteClick(baseEo)
    }

    fun onItemEdit(baseEo: Receivable)
    {
        navigator?.onItemEditClick(baseEo)
    }

    fun onItemView(baseEo: Receivable)
    {
        navigator?.onItemViewClick(baseEo)
    }

    fun onPrint(rcv_Id: Int) {
        isPrint = true
        msgListener?.onStarted()
        if (rcv_Id != _rcv_Id.value) {
            find(rcv_Id)
            return
        }
        onPrintTicket(baseEo.value!!)
    }

    fun onPrintTicket(_baseEo: Receivable){
        val config = activity!!.resources.configuration
        val isRTL = if(config.layoutDirection == View.LAYOUT_DIRECTION_LTR) false else true
        var bmp: Bitmap? = null

        val mngr: AssetManager = activity!!.getAssets()
        var `is`: InputStream? = null
        try {
            `is` = mngr.open("images/co_logo.bmp")
            bmp = BitmapFactory.decodeStream(`is`)
        } catch (e1: IOException) {
            e1.printStackTrace()
        }
        val fontNameEn = "assets/fonts/arial.ttf"
        val fontNameAr = "assets/fonts/arial.ttf"
        val fontNameAr1 = "assets/fonts/droid_kufi_regular.ttf"
        try {

            val imgLogo = RepLogo(bmp, 10F, 800F)
            val header: ArrayList<HeaderFooterRow> = arrayListOf()
            var tbl: HashMap<Int, TCell> = hashMapOf()
            var rws: ArrayList<CTable> = arrayListOf()
            val phones = if(_baseEo.rcv_org_phone != null) _baseEo.rcv_org_phone!! else ""

            header.add(HeaderFooterRow(0, null, "شركة النادر التجارية", 14F, Element.ALIGN_CENTER, Font.BOLD, fontNameAr1 ))
            header.add(HeaderFooterRow(1, null, "Al-Nadir Trading Company",  14F, Element.ALIGN_CENTER, Font.BOLD, fontNameEn))
            header.add(HeaderFooterRow(2, null, "${_baseEo.rcv_org_name}", 14F, Element.ALIGN_CENTER, Font.BOLD, fontNameEn))
            header.add(HeaderFooterRow(3, null, phones, 12F, Element.ALIGN_CENTER, Font.BOLD, fontNameEn))

            val footer: ArrayList<HeaderFooterRow> = arrayListOf()
            footer.add(HeaderFooterRow(0,null,"موارد" + " - " + "الشركة الحديثة للبرامجيات الاتمتة المحدودة",fontSize = 9F,align = Element.ALIGN_LEFT,fontName = fontNameAr))
            footer.add(HeaderFooterRow(2,null,activity!!.resources!!.getString(R.string.rpt_user_name) + ": ${App.prefs.saveUser!!.name}",fontSize = 9F, align = Element.ALIGN_LEFT, fontName = fontNameAr))
            _baseEo.created_by = activity!!.resources!!.getString(R.string.rpt_user_name) + ": ${App.prefs.saveUser!!.name}"
            GeneratePdf().createPdf(activity!!,imgLogo, _baseEo, header, footer,isRTL) { _, path ->
                msgListener?.onSuccess("Pdf Created Successfully")
                GeneratePdf().printPDF(activity!!, path)
            }
        } catch (e: Exception) {
            msgListener?.onFailure("Error Exception ${e.message}")
            e.printStackTrace()
        }
        isPrint = false

    }
    fun cancelJob(){
        repository.cancelJob()
    }
}
