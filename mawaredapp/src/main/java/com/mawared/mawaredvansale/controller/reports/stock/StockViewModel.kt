package com.mawared.mawaredvansale.controller.reports.stock

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.PagedList
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.controller.base.BaseViewModel
import com.mawared.mawaredvansale.controller.reports.fms.DateFilter
import com.mawared.mawaredvansale.data.db.entities.md.Product
import com.mawared.mawaredvansale.data.db.entities.reports.stock.StockStatement
import com.mawared.mawaredvansale.interfaces.IDateRangePicker
import com.mawared.mawaredvansale.services.repositories.reports.stock.IStockRepository

class StockViewModel(private val repository: IStockRepository) : BaseViewModel() {
    private val _wr_id: Int = if(App.prefs.savedSalesman?.sm_warehouse_id != null)  App.prefs.savedSalesman!!.sm_warehouse_id!! else 0
    val _wr_Name: String? = App.prefs.savedSalesman?.sm_warehouse_name

    val dtTo : MutableLiveData<String> = MutableLiveData()
    var dateFilter: MutableLiveData<DateFilter> = MutableLiveData()
    var printdateFilter:MutableLiveData<DateFilter> = MutableLiveData()
    var dateNavigator: IDateRangePicker? = null
    var errorMessage: MutableLiveData<String> = MutableLiveData()
    val stocks: LiveData<PagedList<StockStatement>> = Transformations
        .switchMap(dateFilter){
            repository.fetchLivePagedList(_wr_id, it.dtTo)
        }

    val printingStock: LiveData<List<StockStatement>> = Transformations
        .switchMap(printdateFilter){
            repository.getStock(_wr_id, it.dtTo)
        }

    val networkState by lazy {
        repository.getRecNetworkState()
    }

    fun listIsEmpty():Boolean{
        return stocks.value?.isEmpty() ?: true
    }

    fun onFromDate(v: View) {
        dateNavigator?.fromDatePicker(v)
    }

    fun onToDate(v: View) {
        dateNavigator?.toDatePicker(v)
    }

    fun doSearch(){
        dateFilter.value = DateFilter(null, dtTo.value)
    }

    fun onPrint()
    {
        printdateFilter.value = DateFilter(null, dtTo.value)
    }
}
