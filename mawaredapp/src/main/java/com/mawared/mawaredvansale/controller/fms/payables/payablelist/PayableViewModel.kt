package com.mawared.mawaredvansale.controller.fms.payables.payablelist

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.PagedList
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.controller.base.BaseViewModel
import com.mawared.mawaredvansale.controller.common.GenerateTicket
import com.mawared.mawaredvansale.controller.common.TicketPrinting
import com.mawared.mawaredvansale.data.db.entities.fms.Payable
import com.mawared.mawaredvansale.interfaces.IMainNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.services.repositories.fms.IPayableRepository
import com.mawared.mawaredvansale.utilities.URL_LOGO
import java.util.*

class PayableViewModel(private val repository: IPayableRepository) : BaseViewModel() {
    private val _sm_id: Int = if(App.prefs.savedSalesman?.sm_user_id != null)  App.prefs.savedSalesman!!.sm_user_id!! else 0

    var navigator: IMainNavigator<Payable>? = null
    var msgListener: IMessageListener? = null
    var ctx: Context? = null
    var errorMessage: MutableLiveData<String> = MutableLiveData()
    var lbl_SCAmount: MutableLiveData<String> = MutableLiveData()
    var lbl_SCChange: MutableLiveData<String> = MutableLiveData()
    var lbl_FCAmount: MutableLiveData<String> = MutableLiveData()
    var lbl_FCChange: MutableLiveData<String> = MutableLiveData()
    var isPrint = false

    private val cuId: MutableLiveData<Int> = MutableLiveData()

    val baseEoList: LiveData<PagedList<Payable>> =Transformations.switchMap(cuId) {
        repository.fetchLivePagedList(_sm_id, it)
    }

    val networkStateRV: LiveData<NetworkState> by lazy {
        repository.getPayableNetworkState()
    }

    fun listIsEmpty():Boolean{
        return baseEoList.value?.isEmpty() ?: true
    }

    val networkState by lazy {
        repository.networkState
    }

    private val _py_Id_for_delete: MutableLiveData<Int> = MutableLiveData()
    val deleteRecord: LiveData<String> = Transformations
        .switchMap(_py_Id_for_delete){
            repository.delete(it)
        }

    private val _py_Id: MutableLiveData<Int> = MutableLiveData()
    val baseEo: LiveData<Payable> = Transformations
        .switchMap(_py_Id) {
            repository.getById(it)
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
        if (_py_Id.value == id) {
            return
        }
        _py_Id.value = id
    }

    // confirm delete
    fun confirmDelete(baseEo: Payable){
        _py_Id_for_delete.value = baseEo.py_Id
    }

    fun onItemDelete(baseEo: Payable)
    {
        navigator?.onItemDeleteClick(baseEo)
    }

    fun onItemEdit(baseEo: Payable)
    {
        navigator?.onItemEditClick(baseEo)
    }

    fun onItemView(baseEo: Payable)
    {
        navigator?.onItemViewClick(baseEo)
    }

    fun cancelJob(){
        repository.cancelJob()
    }

    fun onPrint(rcv_Id: Int) {
        isPrint = true
        msgListener?.onStarted()
        if (rcv_Id != _py_Id.value) {
            find(rcv_Id)
            return
        }
        onPrintTicket(baseEo.value!!)
    }

    fun onPrintTicket(_baseEo: Payable) {
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

        }
        isPrint = false
    }
}
