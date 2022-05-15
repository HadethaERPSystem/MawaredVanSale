package com.mawared.mawaredvansale.controller.marketplace.brand

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.mawared.mawaredvansale.controller.base.BaseViewModel
import com.mawared.mawaredvansale.data.db.entities.md.Customer
import com.mawared.mawaredvansale.data.db.entities.md.Product_Brand
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository
import com.mawared.mawaredvansale.utilities.Coroutines

class BrandViewModel (private val repository: IMDataRepository) : BaseViewModel() {

    var customer : Customer? = null
    var vocode: String = ""
    //var _term : MutableLiveData<String?> = MutableLiveData()
    var term: String? = ""
//    val brandList : LiveData<List<Product_Brand>> =  Transformations
//            .switchMap(_term) {
//        repository.brand_GetByTerm(it)
//    }

    fun loadData(list: MutableList<Product_Brand>, term: String, pageCount: Int, loadMore: (List<Product_Brand>?, Int) -> Unit){
        try {
            Coroutines.ioThenMain({
                                  val brans = repository.brandOnPages(term, pageCount)
                if(brans != null){
                    list.addAll(brans)
                }
            }, {
                loadMore(list, pageCount)})
        }catch (e: Exception){
            e.printStackTrace()
        }
    }
}