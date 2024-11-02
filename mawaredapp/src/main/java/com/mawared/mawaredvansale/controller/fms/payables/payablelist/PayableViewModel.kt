package com.mawared.mawaredvansale.controller.fms.payables.payablelist

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.controller.base.BaseViewModel
import com.mawared.mawaredvansale.controller.common.GenerateTicket
import com.mawared.mawaredvansale.controller.common.TicketPrinting
import com.mawared.mawaredvansale.data.db.entities.fms.Payable
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.services.repositories.fms.IPayableRepository
import com.mawared.mawaredvansale.utilities.Coroutines
import com.mawared.mawaredvansale.utilities.URL_LOGO
import java.util.*

class PayableViewModel(private val repository: IPayableRepository) : BaseViewModel() {
    private val _user_id: Int = App.prefs.saveUser!!.id

    var msgListener: IMessageListener? = null
    var ctx: Context? = null
    var errorMessage: MutableLiveData<String> = MutableLiveData()

    var isPrint = false
    var term: String? = ""

    fun loadData(list: MutableList<Payable>, term: String, pageCount: Int, loadMore: (List<Payable>?, Int) -> Unit){
        try {
            Coroutines.ioThenMain({
                val tmp = repository.get_OnPages(_user_id, term, pageCount)
                if(tmp != null){
                    list.addAll(tmp)
                }
            }, {loadMore(list, pageCount)})
        }catch (e: Exception){
            e.printStackTrace()
        }
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
