package com.mawared.mawaredvansale.controller.transfer.transferlist

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.base.BaseViewModel
import com.mawared.mawaredvansale.controller.common.GenerateTicket
import com.mawared.mawaredvansale.controller.common.TicketPrinting
import com.mawared.mawaredvansale.data.db.entities.sales.Transfer
import com.mawared.mawaredvansale.interfaces.IMainNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.services.repositories.transfer.ITransferRepository
import java.util.*

class TransferViewModel(private val repository: ITransferRepository) : BaseViewModel() {

    private val _user_id: Int = if(App.prefs.saveUser?.id != null)  App.prefs.saveUser!!.id else 0
    var ctx: Context? = null
    var navigator: IMainNavigator<Transfer>? = null
    var msgListener: IMessageListener? = null

    val baseEoList by lazy {
        repository.getByUserId(_user_id)
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

    // on press delete button in recycler view
    fun onItemDelete(baseEo: Transfer)
    {
        navigator?.onItemDeleteClick(baseEo)
    }

    fun confirmDelete(baseEo: Transfer){
       // _sr_Id_for_delete.value = baseEo.sr_Id
    }

    // on press edit button in recycler view
    fun onItemEdit(baseEo: Transfer)
    {
        navigator?.onItemEditClick(baseEo)
    }

    // on press view button in recycler view
    fun onItemView(baseEo: Transfer)
    {
        navigator?.onItemViewClick(baseEo)
    }

    fun onPrint(tr_Id: Int){
        msgListener?.onStarted()
        if(tr_Id != _tr_Id.value){
            find(tr_Id)
            return
        }
        onPrintTicket(baseEo.value!!)
    }

    fun onPrintTicket(entityEo: Transfer){
        val lang = Locale.getDefault().toString().toLowerCase()
        val tickets = GenerateTicket(ctx!!, lang).Create(entityEo,
            R.drawable.ic_logo_black, "Mawared Vansale\nAL-HADETHA FRO SOFTWATE & AUTOMATION", null, null)

        TicketPrinting(ctx!!, tickets).run()
    }

    fun cancelJob(){
        repository.cancelJob()
    }
}
