package com.mawared.mawaredvansale.controller.inventory.stockin.stockinlist

import androidx.lifecycle.ViewModel
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.data.db.entities.inventory.Stockin
import com.mawared.mawaredvansale.data.db.entities.inventory.Stockout
import com.mawared.mawaredvansale.interfaces.IMainNavigator
import com.mawared.mawaredvansale.services.repositories.stockin.IStockInRepository
import com.mawared.mawaredvansale.utilities.Coroutines
import com.mawared.mawaredvansale.utilities.lazyDeferred

class StockInViewModel(private val repository: IStockInRepository) : ViewModel() {

    private val userId: Int =  if(App.prefs.saveUser?.id != null)  App.prefs.saveUser!!.id else 0

    private var navigator: IMainNavigator<Stockin>? = null

    var term: String? = ""

    fun loadData(list: MutableList<Stockin>, term: String, pageCount: Int, loadMore: (List<Stockin>?, Int) -> Unit){
        try {
            Coroutines.ioThenMain({
                val tmp = repository.getOnpages(userId, term, pageCount)
                if(tmp != null){
                    list.addAll(tmp)
                }
            }, {loadMore(list, pageCount)})
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun setNavigator(navigator: IMainNavigator<Stockin>)
    {
        this.navigator = navigator
    }

    fun onItemDelete(baseEo: Stockin)
    {
        navigator?.onItemDeleteClick(baseEo)
    }

    fun onItemEdit(baseEo: Stockin)
    {
        navigator?.onItemEditClick(baseEo)
    }

    fun onItemView(baseEo: Stockin)
    {
        navigator?.onItemViewClick(baseEo)
    }

    fun cancelJob(){
        repository.cancelJob()
    }
}
