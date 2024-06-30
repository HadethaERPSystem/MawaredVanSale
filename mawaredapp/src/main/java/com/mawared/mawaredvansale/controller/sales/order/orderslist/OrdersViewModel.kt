package com.mawared.mawaredvansale.controller.sales.order.orderslist

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.PagedList
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.controller.base.BaseViewModel
import com.mawared.mawaredvansale.controller.common.GenerateTicket
import com.mawared.mawaredvansale.controller.common.SunmiTicket
import com.mawared.mawaredvansale.controller.common.TicketPrinting
import com.mawared.mawaredvansale.data.db.entities.sales.Sale
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Order
import com.mawared.mawaredvansale.interfaces.IMainNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.services.repositories.order.IOrderRepository
import com.mawared.mawaredvansale.utilities.Coroutines
import com.mawared.mawaredvansale.utilities.ImageLoader
import com.mawared.mawaredvansale.utilities.URL_LOGO
import com.mawared.mawaredvansale.utils.SunmiPrintHelper
import java.util.*

class OrdersViewModel(private val orderRepository: IOrderRepository) : BaseViewModel() {

    private val _sm_id: Int = if(App.prefs.savedSalesman?.sm_user_id != null)  App.prefs.savedSalesman!!.sm_user_id!! else 0
    var ctx: Context? = null
    var errorMessage: MutableLiveData<String> = MutableLiveData()
    var msgListener: IMessageListener? = null
    var isPrint = false
    var cu_id: Int? = null
    var term: String? = ""


    fun loadData(list: MutableList<Sale_Order>, term: String, cu_id: Int?, pageCount: Int, loadMore: (List<Sale_Order>?, Int) -> Unit){
        try {
            Coroutines.ioThenMain({
                val tmp = orderRepository.getOrderOnPages(_sm_id, cu_id, "SaleOrder", term, pageCount)
                if(tmp != null){
                    list.addAll(tmp)
                }
            }, {loadMore(list, pageCount)})
        }catch (e: Exception){
            e.printStackTrace()
        }
    }


    private val _so_Id: MutableLiveData<Int> = MutableLiveData()
    val baseEo: LiveData<Sale_Order> = Transformations
        .switchMap(_so_Id) {
            orderRepository.getOrderById(it)
        }

    private val _so_Id_for_delete: MutableLiveData<Int> = MutableLiveData()
    val deleteRecord: LiveData<String> = Transformations
        .switchMap(_so_Id_for_delete){
            orderRepository.delete(it)
        }

    // confirm delete
    fun confirmDelete(baseEo: Sale_Order){
        _so_Id_for_delete.value = baseEo.so_id
    }

    fun find(id: Int) {
        if (_so_Id.value == id) {
            return
        }
        _so_Id.value = id
    }

    fun onPrint(so_Id: Int) {
        isPrint = true
        msgListener?.onStarted()
        if (so_Id != _so_Id.value) {
            find(so_Id)
            return
        }
        onPrintTicket(baseEo.value!!)
    }

    fun onPrintTicket(entityEo: Sale_Order) {

            try {
                entityEo.so_salesman_name = App.prefs.savedSalesman?.sm_phone_no ?: ""
                val lang = Locale.getDefault().toString().toLowerCase()
                if(App.prefs.printer == "Sunmi"){
                    var bmp : Bitmap? = null
                    ImageLoader().LoadImageFromUrl(URL_LOGO + "co_black_logo.png") {
                        bmp = it
                        //val bmp = SunmiPrintHelper.getInstance().LoadImageFromUrl()
                        var tickets : List<SunmiTicket> = arrayListOf()
                        if(App.prefs.printing_so_mode == "H") {
                            tickets = GenerateTicket(ctx!!, lang).createSunmiTicket(entityEo, bmp,  "Mawared Vansale\nAL-HADETHA FRO SOFTWATE & AUTOMATION" )
                        }
                        else{
                            tickets = GenerateTicket(ctx!!, lang).createSunmiTicket(entityEo, bmp,  "Mawared Vansale\nAL-HADETHA FRO SOFTWATE & AUTOMATION", "", "" )
                        }

                        SunmiPrintHelper.getInstance().printReceipt(ctx, tickets)
                        msgListener?.onSuccess("Print Successfully")
                    }
                }else{
                    val tickets = GenerateTicket(ctx!!, lang).create(entityEo, URL_LOGO + "co_black_logo.png", "Mawared Vansale\nAL-HADETHA FRO SOFTWATE & AUTOMATION", null, null )

                    TicketPrinting(ctx!!, tickets).run()
                    msgListener?.onSuccess("Print Successfully")
                }

            } catch (e: Exception) {
                msgListener?.onFailure("Error Exception ${e.message}")
                e.printStackTrace()
            }

        isPrint = false
    }

    fun cancelJob(){
        orderRepository.cancelJob()
    }

    override fun onCleared() {
        super.onCleared()
        orderRepository.cancelJob()
    }
}
