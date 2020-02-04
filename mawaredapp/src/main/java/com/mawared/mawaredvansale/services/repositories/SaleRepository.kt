package com.mawared.mawaredvansale.services.repositories

import com.mawared.mawaredvansale.data.db.AppDatabase
import com.mawared.mawaredvansale.data.db.entities.sales.Sale
import com.mawared.mawaredvansale.data.db.entities.sales.Sale_Items
import com.mawared.mawaredvansale.services.netwrok.ApiService
import com.mawared.mawaredvansale.services.netwrok.SafeApiRequest
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.threeten.bp.LocalDate

class SaleRepository(private val api: ApiService, private val db: AppDatabase): SafeApiRequest(){

    suspend fun Add(sale: Sale) : Long = db.getSaleDao().insert(sale)

    suspend fun Update(sale: Sale) = db.getSaleDao().update(sale)


    fun getById(id : Int) = db.getSaleDao().getById(id)

    fun getByNo(docNo: Int) = db.getSaleDao().getByNo(docNo)

    fun getAll() = db.getSaleDao().getAll()

    /* Sale detail functions */
    suspend fun AddItem(item: Sale_Items) : Long = db.getSaleDetailDao().insert(item)

    suspend fun UpdateItem(item: Sale_Items) = db.getSaleDetailDao().update(item)

    fun getByMasterId(saleId : Int) = db.getSaleDetailDao().getBySaleId(saleId)

    // for init
    private val _currentSale = MutableLiveData<Sale>()
    val currentSale : LiveData<Sale>
        get() = _currentSale

    private val _currentSaleItems = MutableLiveData<List<Sale_Items>>()

    val currentSaleItems : LiveData<List<Sale_Items>>
        get() = _currentSaleItems

}