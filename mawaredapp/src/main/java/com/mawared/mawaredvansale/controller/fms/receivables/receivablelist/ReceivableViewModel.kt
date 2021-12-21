package com.mawared.mawaredvansale.controller.fms.receivables.receivablelist

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.PagedList
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.base.BaseViewModel
import com.mawared.mawaredvansale.controller.common.GenerateTicket
import com.mawared.mawaredvansale.controller.common.TicketPrinting
import com.mawared.mawaredvansale.controller.common.printing.*
import com.mawared.mawaredvansale.data.db.entities.fms.Receivable
import com.mawared.mawaredvansale.interfaces.IMainNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.services.repositories.fms.IReceivableRepository
import com.mawared.mawaredvansale.utilities.URL_LOGO
import java.io.InputStream
import java.util.*

class ReceivableViewModel(private val repository: IReceivableRepository) : BaseViewModel() {
    private val _sm_id: Int =
        if (App.prefs.savedSalesman?.sm_user_id != null) App.prefs.savedSalesman!!.sm_user_id!! else 0

    var navigator: IMainNavigator<Receivable>? = null
    var msgListener: IMessageListener? = null
    var ctx: Context? = null
    var activity: AppCompatActivity? = null
    var co_black_logo: Bitmap? = null
    var isPrint = false
    var errorMessage: MutableLiveData<String> = MutableLiveData()
    var lbl_SCAmount: MutableLiveData<String> = MutableLiveData()
    var lbl_SCChange: MutableLiveData<String> = MutableLiveData()
    var lbl_FCAmount: MutableLiveData<String> = MutableLiveData()
    var lbl_FCChange: MutableLiveData<String> = MutableLiveData()

    private val cuId: MutableLiveData<Int> = MutableLiveData()

    val baseEoList: LiveData<PagedList<Receivable>> = Transformations.switchMap(cuId) {
        repository.fetchLivePagedList(_sm_id, it)
    }

    val networkStateRV: LiveData<NetworkState> by lazy {
        repository.getRecNetworkState()
    }

    fun listIsEmpty(): Boolean {
        return baseEoList.value?.isEmpty() ?: true
    }

    val networkState by lazy {
        repository.networkState
    }

    private val _rcv_Id_for_delete: MutableLiveData<Int> = MutableLiveData()
    val deleteRecord: LiveData<String> = Transformations
        .switchMap(_rcv_Id_for_delete) {
            repository.delete(it)
        }

    private val _rcv_Id: MutableLiveData<Int> = MutableLiveData()
    val baseEo: LiveData<Receivable> = Transformations
        .switchMap(_rcv_Id) {
            repository.getById(it)
        }

    // set functions to refresh data
    fun setCustomer(cm_Id: Int?) {
        if (cuId.value == cm_Id && cm_Id != null) {
            return
        }
        cuId.value = cm_Id
    }

    fun refresh(){
        setCustomer(cuId.value)
    }
    fun find(id: Int) {
        if (_rcv_Id.value == id) {
            return
        }
        _rcv_Id.value = id
    }

    // confirm delete
    fun confirmDelete(baseEo: Receivable) {
        _rcv_Id_for_delete.value = baseEo.rcv_Id
    }

    fun onItemDelete(baseEo: Receivable) {
        navigator?.onItemDeleteClick(baseEo)
    }

    fun onItemEdit(baseEo: Receivable) {
        navigator?.onItemEditClick(baseEo)
    }

    fun onItemView(baseEo: Receivable) {
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

    fun onPrintTicket(_baseEo: Receivable) {
        if (App.prefs.printing_type == "R") {
            try {
                val lang = Locale.getDefault().toString().toLowerCase()
                val tickets = GenerateTicket(ctx!!, lang).create(
                    _baseEo,
                    URL_LOGO + "co_black_logo.png",
                    "Mawared Vansale\nAL-HADETHA FRO SOFTWATE & AUTOMATION",
                    null,
                    null
                )

                TicketPrinting(ctx!!, tickets).run()
                msgListener?.onSuccess("Print Successfully")
            } catch (e: Exception) {
                msgListener?.onFailure("Error Exception ${e.message}")
                e.printStackTrace()
            }

        } else {
            val config = activity!!.resources.configuration
            val isRTL = config.layoutDirection != View.LAYOUT_DIRECTION_LTR
            var bmp: Bitmap? = null

            val mngr: AssetManager = activity!!.assets
            var `is`: InputStream? = null
//            try {
//                `is` = mngr.open("images/co_logo.bmp")
//                bmp = BitmapFactory.decodeStream(`is`)
//            } catch (e1: IOException) {
//                e1.printStackTrace()
//            }
            val fontNameEn = "assets/fonts/arial.ttf"
            val fontNameAr = "assets/fonts/arial.ttf"
            val fontNameAr1 = "assets/fonts/droid_kufi_regular.ttf"
            try {

                val imgLogo = RepLogo(bmp, 10F, 800F)
                val header: ArrayList<HeaderFooterRow> = arrayListOf()
                var tbl: HashMap<Int, TCell> = hashMapOf()
                var rws: ArrayList<CTable> = arrayListOf()
                val phones = if (_baseEo.rcv_org_phone != null) _baseEo.rcv_org_phone!! else ""

                header.add(
                    HeaderFooterRow(
                        0,
                        null,
                        App.prefs.saveUser!!.client_name,
                        14F,
                        Element.ALIGN_CENTER,
                        Font.BOLD,
                        fontNameAr1
                    )
                )
                header.add(
                    HeaderFooterRow(
                        1,
                        null,
                        "",
                        14F,
                        Element.ALIGN_CENTER,
                        Font.BOLD,
                        fontNameEn
                    )
                )
                header.add(
                    HeaderFooterRow(
                        2,
                        null,
                        "${_baseEo.rcv_org_name}",
                        14F,
                        Element.ALIGN_CENTER,
                        Font.BOLD,
                        fontNameEn
                    )
                )
                header.add(
                    HeaderFooterRow(
                        3,
                        null,
                        phones,
                        12F,
                        Element.ALIGN_CENTER,
                        Font.BOLD,
                        fontNameEn
                    )
                )

                val footer: ArrayList<HeaderFooterRow> = arrayListOf()
                footer.add(
                    HeaderFooterRow(
                        0,
                        null,
                        "موارد" + " - " + "الشركة الحديثة للبرامجيات الاتمتة المحدودة",
                        fontSize = 9F,
                        align = Element.ALIGN_LEFT,
                        fontName = fontNameAr
                    )
                )
                footer.add(
                    HeaderFooterRow(
                        2,
                        null,
                        activity!!.resources!!.getString(R.string.rpt_user_name) + ": ${App.prefs.saveUser!!.name}",
                        fontSize = 9F,
                        align = Element.ALIGN_LEFT,
                        fontName = fontNameAr
                    )
                )
                _baseEo.created_by =
                    activity!!.resources!!.getString(R.string.rpt_user_name) + ": ${App.prefs.saveUser!!.name}"
                GeneratePdf().createPdf(
                    activity!!,
                    imgLogo,
                    _baseEo,
                    header,
                    footer,
                    isRTL
                ) { _, path ->
                    msgListener?.onSuccess("Pdf Created Successfully")
                    GeneratePdf().printPDF(activity!!, path)
                }
            } catch (e: Exception) {
                msgListener?.onFailure("Error Exception ${e.message}")
                e.printStackTrace()
            }
        }
        isPrint = false
    }

    fun cancelJob() {
        repository.cancelJob()
    }

}
