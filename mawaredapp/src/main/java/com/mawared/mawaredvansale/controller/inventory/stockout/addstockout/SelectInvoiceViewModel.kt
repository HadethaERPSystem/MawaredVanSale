package com.mawared.mawaredvansale.controller.inventory.stockout.addstockout

import androidx.lifecycle.ViewModel
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.data.db.entities.inventory.InventoryDoc
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository
import com.mawared.mawaredvansale.utilities.Coroutines

class SelectInvoiceViewModel(private val repository: IMDataRepository) : ViewModel() {
    private val _wr_id: Int = if(App.prefs.savedSalesman?.sm_warehouse_id != null)  App.prefs.savedSalesman!!.sm_warehouse_id!! else 0
    var term: String? = ""
    var baseEo : InventoryDoc? = null
    var stockType: String = "Sale"

    fun loadData(list: MutableList<InventoryDoc>, term: String, pageCount: Int, loadMore: (List<InventoryDoc>?, Int) -> Unit){
        try {
            Coroutines.ioThenMain({
                val tmp = repository.invoices_SearchOnPages(term, _wr_id, stockType, pageCount)
                if(tmp != null){
                    list.addAll(tmp)
                }
            }, {loadMore(list, pageCount)})
        }catch (e: Exception){
            e.printStackTrace()
        }
    }
}