package com.mawared.mawaredvansale.controller.sales.invoices.invoiceslist

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.base.BaseViewModel
import com.mawared.mawaredvansale.controller.common.GenerateTicket
import com.mawared.mawaredvansale.controller.common.TicketPrinting
import com.mawared.mawaredvansale.data.db.entities.sales.Sale
import com.mawared.mawaredvansale.interfaces.IMainNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.services.repositories.invoices.IInvoiceRepository
import java.util.*

class InvoicesViewModel(private val repository: IInvoiceRepository) : BaseViewModel() {

    private val _sm_id: Int = if(App.prefs.savedSalesman?.sm_id != null)  App.prefs.savedSalesman!!.sm_id else 0
    var ctx: Context? = null
    var navigator: IMainNavigator<Sale>? = null
    var msgListener: IMessageListener? = null

    private val _cu_id: MutableLiveData<Int> = MutableLiveData()

    val sales: LiveData<List<Sale>> = Transformations
        .switchMap(_cu_id){
            repository.getInvoices(_sm_id, it)
        }

    private val _sl_Id: MutableLiveData<Int> = MutableLiveData()
    val baseEo: LiveData<Sale> = Transformations
        .switchMap(_sl_Id){
            repository.getInvoice(it)
        }

    private val _sl_Id_for_delete: MutableLiveData<Int> = MutableLiveData()
    val deleteRecord: LiveData<String> = Transformations
        .switchMap(_sl_Id_for_delete){
            repository.delete(it)
        }

    fun setCustomer(cm_Id: Int?){
        if(_cu_id.value == cm_Id && cm_Id != null){
            return
        }
        _cu_id.value = cm_Id
    }

    fun find(id: Int){
        if(_sl_Id.value == id){
            return
        }
        _sl_Id.value = id
    }
    // on press delete invoice
    fun onItemDelete(sale: Sale)
    {
        navigator?.onItemDeleteClick(sale)
    }

    fun confirmDelete(baseEo: Sale){
        _sl_Id_for_delete.value = baseEo.sl_Id
    }

    // on press edit invoice
    fun onItemEdit(sale: Sale)
    {
        navigator?.onItemEditClick(sale)
    }

    // on press view invoice
    fun onItemView(sale: Sale)
    {
        navigator?.onItemViewClick(sale)
    }

    fun onPrint(sl_Id: Int){
        msgListener?.onStarted()
        if(sl_Id != _sl_Id.value){
            find(sl_Id)
            return
        }
        onPrintTicket(baseEo.value!!)
    }

    fun onPrintTicket(entityEo: Sale){
        val lang = Locale.getDefault().toString().toLowerCase()
        val tickets = GenerateTicket(ctx!!, lang).Create(entityEo,
            R.drawable.ic_logo_black, "Mawared Vansale\nAL-HADETHA FRO SOFTWATE & AUTOMATION", null, null)

        TicketPrinting(ctx!!, tickets).run()
    }
    // cancel job call in destroy fragment
    fun cancelJob(){
        repository.cancelJob()
    }
}