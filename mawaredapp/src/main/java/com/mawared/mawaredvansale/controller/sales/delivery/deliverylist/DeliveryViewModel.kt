package com.mawared.mawaredvansale.controller.sales.delivery.deliverylist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.PagedList
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.controller.base.BaseViewModel
import com.mawared.mawaredvansale.data.db.entities.fms.Receivable
import com.mawared.mawaredvansale.data.db.entities.sales.Delivery
import com.mawared.mawaredvansale.interfaces.IMainNavigator
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.services.repositories.delivery.IDeliveryRepository
import com.mawared.mawaredvansale.utilities.Coroutines

class DeliveryViewModel(private val repository: IDeliveryRepository) : BaseViewModel() {
    private val _sm_id: Int = if(App.prefs.savedSalesman?.sm_user_id != null)  App.prefs.savedSalesman!!.sm_user_id!! else 0

    var errorMessage: MutableLiveData<String> = MutableLiveData()
    var term: String? = ""

    fun loadData(list: MutableList<Delivery>, term: String, pageCount: Int, loadMore: (List<Delivery>?, Int) -> Unit){
        try {
            Coroutines.ioThenMain({
                val tmp = repository.get_OnPages(_sm_id, term, pageCount)
                if(tmp != null){
                    list.addAll(tmp)
                }
            }, {loadMore(list, pageCount)})
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    val networkState: LiveData<NetworkState> by lazy {
        repository.networkState
    }

    private val _dl_Id: MutableLiveData<Int> = MutableLiveData()
    val baseEo: LiveData<Delivery> = Transformations
        .switchMap(_dl_Id){
            repository.getById(it)
        }

    fun find(id: Int){
        if(_dl_Id.value == id){
            return
        }
        _dl_Id.value = id
    }

    fun onPrient(id: Int){
        find(id)
    }

    // cancel job call in destroy fragment
    fun cancelJob(){
        repository.cancelJob()
    }
}
