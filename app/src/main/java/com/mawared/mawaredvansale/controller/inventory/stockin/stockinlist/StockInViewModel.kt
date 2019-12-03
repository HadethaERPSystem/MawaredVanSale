package com.mawared.mawaredvansale.controller.inventory.stockin.stockinlist

import androidx.lifecycle.ViewModel
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.data.db.entities.inventory.Stockin
import com.mawared.mawaredvansale.interfaces.IMainNavigator
import com.mawared.mawaredvansale.services.repositories.stockin.IStockInRepository
import com.mawared.mawaredvansale.utilities.lazyDeferred

class StockInViewModel(private val repository: IStockInRepository) : ViewModel() {

    private val userId: Int =  if(App.prefs.saveUser?.id != null)  App.prefs.saveUser!!.id else 0

    private var navigator: IMainNavigator<Stockin>? = null

    val baseEoList by lazyDeferred {
        repository.getStockin(userId)
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
