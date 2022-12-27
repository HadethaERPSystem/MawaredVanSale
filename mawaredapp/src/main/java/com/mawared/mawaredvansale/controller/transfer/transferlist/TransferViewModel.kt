package com.mawared.mawaredvansale.controller.transfer.transferlist

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.controller.base.BaseViewModel
import com.mawared.mawaredvansale.data.db.entities.sales.Transfer
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.services.repositories.transfer.ITransferRepository
import com.mawared.mawaredvansale.utilities.Coroutines

class TransferViewModel(private val repository: ITransferRepository) : BaseViewModel() {

    private val userId: Int = if(App.prefs.saveUser?.id != null)  App.prefs.saveUser!!.id else 0
    var ctx: Context? = null

    var msgListener: IMessageListener? = null

    var isPrint = false
    var errorMessage: MutableLiveData<String> = MutableLiveData()
    var term: String? = ""
    fun loadData(list: MutableList<Transfer>, term: String, pageCount: Int, loadMore: (List<Transfer>?, Int) -> Unit){
        try {
            Coroutines.ioThenMain({
                val tmp = repository.get_OnPages(userId, term, pageCount)
                if(tmp != null){
                    list.addAll(tmp)
                }
            }, {loadMore(list, pageCount)})
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    private val _tr_Id: MutableLiveData<Int> = MutableLiveData()
    val baseEo: LiveData<Transfer> = Transformations
        .switchMap(_tr_Id){
            repository.getById(it)
        }

    // confirm delete sale return record
    fun find(id: Int){
        if(_tr_Id.value == id){
            return
        }
        _tr_Id.value = id
    }

    fun confirmDelete(baseEo: Transfer){
       // _sr_Id_for_delete.value = baseEo.sr_Id
    }

    fun onPrint(tr_Id: Int){
        isPrint = true
        msgListener?.onStarted()
        if(tr_Id != _tr_Id.value){
            find(tr_Id)
            return
        }
       // onPrintTicket(baseEo.value!!)
    }

    fun cancelJob(){
        repository.cancelJob()
    }
}
