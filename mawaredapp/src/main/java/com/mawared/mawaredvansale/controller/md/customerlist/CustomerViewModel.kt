package com.mawared.mawaredvansale.controller.md.customerlist

import androidx.lifecycle.MutableLiveData
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.controller.base.BaseViewModel
import com.mawared.mawaredvansale.data.db.entities.md.Customer
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository
import com.mawared.mawaredvansale.utilities.Coroutines

class CustomerViewModel(private val repository:IMDataRepository) : BaseViewModel() {

    private val _sm_id: Int = if(App.prefs.savedSalesman?.sm_user_id != null)  App.prefs.savedSalesman!!.sm_user_id!! else 0

    var errorMessage: MutableLiveData<String> = MutableLiveData()
    var term: String? = ""


    fun loadData(list: MutableList<Customer>, term: String,pageCount: Int, loadMore: (List<Customer>?, Int)->Unit){
        try {
            Coroutines.ioThenMain({
                val cu = repository.getCustomersOnPages(_sm_id, null, term, pageCount)
                if(cu != null){
                    list.addAll(cu)
                }
            },
                {loadMore(list, pageCount)})
        }catch (e: Exception){
            e.printStackTrace()
        }
    }


    fun cancelJob(){
        repository.cancelJob()
    }

//    fun doSearch(org_Id: Int?, term: String){
//        searchFilter.value = CustomerFilter(org_Id, term)
//    }
}
