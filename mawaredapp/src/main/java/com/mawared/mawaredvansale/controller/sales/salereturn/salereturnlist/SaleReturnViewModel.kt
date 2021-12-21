package com.mawared.mawaredvansale.controller.sales.salereturn.salereturnlist

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.PagedList
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.controller.base.BaseViewModel
import com.mawared.mawaredvansale.controller.common.GenerateTicket
import com.mawared.mawaredvansale.controller.common.TicketPrinting
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Return
import com.mawared.mawaredvansale.interfaces.IMainNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.services.repositories.salereturn.ISaleReturnRepository
import com.mawared.mawaredvansale.utilities.URL_LOGO
import java.util.*

class SaleReturnViewModel(private val repository: ISaleReturnRepository) : BaseViewModel() {
    private val _sm_id: Int = if(App.prefs.savedSalesman?.sm_user_id != null)  App.prefs.savedSalesman!!.sm_user_id!! else 0
    var navigator: IMainNavigator<Sale_Return>? = null
    var errorMessage: MutableLiveData<String> = MutableLiveData()
    var msgListener: IMessageListener? = null
    var ctx: Context? = null

    private val cuId: MutableLiveData<Int> = MutableLiveData()
    var isPrint = false

    val saleReturns: LiveData<PagedList<Sale_Return>> = Transformations
        .switchMap(cuId){
            repository.fetchLivePagedList(_sm_id, it)
        }

    private val _sr_Id: MutableLiveData<Int> = MutableLiveData()
    val baseEo: LiveData<Sale_Return> = Transformations
        .switchMap(_sr_Id) {
            repository.getReturnById(it)
        }

    val networkStateRV: LiveData<NetworkState> by lazy {
        repository.getPagedNetworkState()
    }

    fun listIsEmpty():Boolean{
        return saleReturns.value?.isEmpty() ?: true
    }

    private val _sr_Id_for_delete: MutableLiveData<Int> = MutableLiveData()
    val deleteRecord: LiveData<String> = Transformations
        .switchMap(_sr_Id_for_delete){
            repository.delete(it)
        }

    fun setCustomer(cm_Id: Int?){
        if(cuId.value == cm_Id && cm_Id != null){
            return
        }
        cuId.value = cm_Id
    }

    fun refresh(){
        setCustomer(cuId.value)
    }

    fun find(id: Int) {
        if (_sr_Id.value == id) {
            return
        }
        _sr_Id.value = id
    }
    // on press delete button in recycler view
    fun onItemDelete(baseEo: Sale_Return)
    {
        navigator?.onItemDeleteClick(baseEo)
    }

    // confirm delete sale return record
    fun confirmDelete(baseEo: Sale_Return){
        _sr_Id_for_delete.value = baseEo.sr_Id
    }

    // on press edit button in recycler view
    fun onItemEdit(baseEo: Sale_Return)
    {
        navigator?.onItemEditClick(baseEo)
    }

    fun onPrint(sl_Id: Int) {
        isPrint = true
        msgListener?.onStarted()
        if (sl_Id != _sr_Id.value) {
            find(sl_Id)
            return
        }
        onPrintTicket(baseEo.value!!)
    }

    fun onPrintTicket(baseEo: Sale_Return){
        if (App.prefs.printing_type == "R") {
            try {
                val lang = Locale.getDefault().toString().toLowerCase()
                val tickets = GenerateTicket(ctx!!, lang).create(
                    baseEo,
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

        }
    }
    // on press view button in recycler view
    fun onItemView(baseEo: Sale_Return)
    {
        navigator?.onItemViewClick(baseEo)
    }

    fun cancelJob(){
        repository.cancelJob()
    }
}
