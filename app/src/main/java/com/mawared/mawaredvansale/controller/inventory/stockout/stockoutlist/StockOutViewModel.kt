package com.mawared.mawaredvansale.controller.inventory.stockout.stockoutlist

import androidx.lifecycle.ViewModel
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.data.db.entities.inventory.Stockout
import com.mawared.mawaredvansale.interfaces.IMainNavigator
import com.mawared.mawaredvansale.services.repositories.stockout.IStockOutRepository
import com.mawared.mawaredvansale.utilities.lazyDeferred

class StockOutViewModel(private val repository: IStockOutRepository) : ViewModel() {
    private val userId: Int =  if(App.prefs.saveUser?.id != null)  App.prefs.saveUser!!.id else 0

    private var navigator: IMainNavigator<Stockout>? = null

    val baseEoList by lazyDeferred {
        repository.getStockout(userId)
    }

    fun setNavigator(navigator: IMainNavigator<Stockout>)
    {
        this.navigator = navigator
    }

    fun onItemDelete(baseEo: Stockout)
    {
        navigator?.onItemDeleteClick(baseEo)
    }

    fun onItemEdit(baseEo: Stockout)
    {
        navigator?.onItemEditClick(baseEo)
    }

    fun onItemView(baseEo: Stockout)
    {
        navigator?.onItemViewClick(baseEo)
    }

    fun cancelJob(){
        repository.cancelJob()
    }
}
