package com.mawared.mawaredvansale.controller.marketplace.category

import com.mawared.mawaredvansale.controller.base.BaseViewModel
import com.mawared.mawaredvansale.data.db.entities.md.Customer
import com.mawared.mawaredvansale.data.db.entities.md.Product_Category
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository
import com.mawared.mawaredvansale.utilities.Coroutines

class CategoryViewModel (private val repository: IMDataRepository) : BaseViewModel() {

    var customer : Customer? = null
    var vocode: String = ""
    var term: String? = ""

    fun loadData(list: MutableList<Product_Category>, term: String, pageCount: Int, loadMore: (List<Product_Category>?, Int) -> Unit){
        try {
            Coroutines.ioThenMain({
                val cats = repository.categoriesOnPages(term, pageCount)
                if(cats != null){
                    list.addAll(cats)
                }
            }, {loadMore(list, pageCount)})
        }catch (e: Exception){
            e.printStackTrace()
        }
    }
}