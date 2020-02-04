package com.mawared.mawaredvansale.controller.inventory.stockout.addstockout

import androidx.lifecycle.ViewModel
import com.mawared.mawaredvansale.data.db.entities.inventory.Stockout_Items
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository
import com.mawared.mawaredvansale.services.repositories.stockout.IStockOutRepository

class AddStockOutViewModel(private val repository: IStockOutRepository, mdRepository: IMDataRepository) : ViewModel() {


    fun onItemDelete(entityEo: Stockout_Items) {
//        if(entityEo.soti_Id != 0){
//
//        }
    }

}
