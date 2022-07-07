package com.mawared.mawaredvansale.controller.inventory.stockin.detail

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mawared.mawaredvansale.controller.helpers.extension.returnDateString
import com.mawared.mawaredvansale.data.db.entities.inventory.Stockin
import com.mawared.mawaredvansale.data.db.entities.inventory.Stockin_Items
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository
import com.mawared.mawaredvansale.services.repositories.stockin.IStockInRepository
import com.mawared.mawaredvansale.utilities.Coroutines

class AddStockInViewModel(private val repository: IStockInRepository,private val  masterDataRepository: IMDataRepository) : ViewModel() {
    var docNo : MutableLiveData<String> = MutableLiveData()
    var docDate : MutableLiveData<String> = MutableLiveData()
    var invStatus: MutableLiveData<String> = MutableLiveData()
    var baseRefNo: MutableLiveData<String> = MutableLiveData()
    var bpName: MutableLiveData<String> = MutableLiveData()
    var whsName: MutableLiveData<String> = MutableLiveData()
    var msgListener: IMessageListener? = null
    var baseEo : Stockin? = null

    fun loadLines(list: MutableList<Stockin_Items>, doc_id: Int, pageCount: Int, loadMore: (List<Stockin_Items>?, Int) -> Unit){
        try {
            Coroutines.ioThenMain( {
                baseEo = repository.getStockinbyId(doc_id)
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
            Log.e("AddStockIn", "${e.message}")
        }
    }

    private fun displayInfo(obj: Stockin) {
        docNo.value = obj.refNo
        docDate.value = "${obj.doc_date.toString().returnDateString()}"
        invStatus.value = obj.invStatus
        baseRefNo.value = obj.baseRefno
        bpName.value = obj.bpName
        whsName.value = obj.whsName
    }
}
