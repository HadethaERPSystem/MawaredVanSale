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
import com.mawared.mawaredvansale.utilities.Coroutines
import com.mawared.mawaredvansale.utilities.URL_LOGO
import java.util.*

class SaleReturnViewModel(private val repository: ISaleReturnRepository) : BaseViewModel() {
    private val _sm_id: Int = if(App.prefs.savedSalesman?.sm_user_id != null)  App.prefs.savedSalesman!!.sm_user_id!! else 0
    var errorMessage: MutableLiveData<String> = MutableLiveData()
    var msgListener: IMessageListener? = null
    var ctx: Context? = null
    var term: String? = ""

    fun loadData(list: MutableList<Sale_Return>, term: String, pageCount: Int, loadMore: (List<Sale_Return>?, Int) -> Unit){
        try {
            Coroutines.ioThenMain({
                val tmp = repository.return_OnPages(_sm_id, term, pageCount)
                if(tmp != null){
                    list.addAll(tmp)
                }
            }, {loadMore(list, pageCount)})
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    var isPrint = false

    private val _sr_Id: MutableLiveData<Int> = MutableLiveData()
    val baseEo: LiveData<Sale_Return> = Transformations
        .switchMap(_sr_Id) {
            repository.getReturnById(it)
        }

    private val _sr_Id_for_delete: MutableLiveData<Int> = MutableLiveData()
    val deleteRecord: LiveData<String> = Transformations
        .switchMap(_sr_Id_for_delete){
            repository.delete(it)
        }

    fun find(id: Int) {
        if (_sr_Id.value == id) {
            return
        }
        _sr_Id.value = id
    }
    // confirm delete sale return record
    fun confirmDelete(baseEo: Sale_Return){
        _sr_Id_for_delete.value = baseEo.sr_Id
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

    fun cancelJob(){
        repository.cancelJob()
    }
}
