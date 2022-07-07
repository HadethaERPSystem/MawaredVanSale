package com.mawared.mawaredvansale.controller.inventory.stockout.detail

import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mawared.mawaredvansale.controller.helpers.extension.returnDateString
import com.mawared.mawaredvansale.data.db.entities.inventory.Stockout
import com.mawared.mawaredvansale.data.db.entities.inventory.Stockout_Items
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository
import com.mawared.mawaredvansale.services.repositories.stockout.IStockOutRepository
import com.mawared.mawaredvansale.utilities.Coroutines

class AddStockOutViewModel(private val repository: IStockOutRepository, private val mdRepository: IMDataRepository) : ViewModel() {
    var docNo : MutableLiveData<String> = MutableLiveData()
    var docDate : MutableLiveData<String> = MutableLiveData()
    var invStatus: MutableLiveData<String> = MutableLiveData()
    var baseRefNo: MutableLiveData<String> = MutableLiveData()
    var bpName: MutableLiveData<String> = MutableLiveData()
    var whsName: MutableLiveData<String> = MutableLiveData()
    var msgListener: IMessageListener? = null
    var visible = View.VISIBLE
    var baseEo : Stockout? = null

    fun loadLines(list: MutableList<Stockout_Items>, doc_id: Int, pageCount: Int, loadMore: (List<Stockout_Items>?, Int) -> Unit){
        try {
            Coroutines.ioThenMain( {
                baseEo = repository.getStockoutById(doc_id)
                if(baseEo != null){
                    list.addAll(baseEo!!.docLines)
                }
           },{
                loadMore(list, 1)
                if(baseEo != null) {
                    displayInfo(baseEo!!)
                }
           })
        }catch (e: Exception){
            Log.e("AddStockOut", "${e.message}")
        }
    }

    private fun displayInfo(obj: Stockout) {
        docNo.value = obj.refNo
        docDate.value = "${obj.doc_date.toString().returnDateString()}"
        invStatus.value = obj.invStatusName
        baseRefNo.value = obj.baseRefno
        bpName.value = obj.bpName
        whsName.value = obj.whsName
    }

}
