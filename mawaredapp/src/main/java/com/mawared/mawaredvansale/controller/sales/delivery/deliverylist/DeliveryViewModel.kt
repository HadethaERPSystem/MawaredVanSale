package com.mawared.mawaredvansale.controller.sales.delivery.deliverylist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.PagedList
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.controller.base.BaseViewModel
import com.mawared.mawaredvansale.data.db.entities.sales.Delivery
import com.mawared.mawaredvansale.interfaces.IMainNavigator
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.services.repositories.delivery.IDeliveryRepository

class DeliveryViewModel(private val repository: IDeliveryRepository) : BaseViewModel() {
    private val _sm_id: Int = if(App.prefs.savedSalesman?.sm_user_id != null)  App.prefs.savedSalesman!!.sm_user_id!! else 0

    var navigator: IMainNavigator<Delivery>? = null
    var errorMessage: MutableLiveData<String> = MutableLiveData()
    private val cuId: MutableLiveData<Int> = MutableLiveData()

    val entityEoList: LiveData<PagedList<Delivery>> = Transformations
        .switchMap(cuId){
            repository.fetchLivePagedList(_sm_id, it)
        }


    val networkStateRV: LiveData<NetworkState> by lazy {
        repository.getDelvNetworkState()
    }

    val networkState: LiveData<NetworkState> by lazy {
        repository.networkState
    }

    fun listIsEmpty():Boolean{
        return entityEoList.value?.isEmpty() ?: true
    }

    private val _dl_Id: MutableLiveData<Int> = MutableLiveData()
    val baseEo: LiveData<Delivery> = Transformations
        .switchMap(_dl_Id){
            repository.getById(it)
        }

    fun refresh(){
        setCustomer(cuId.value)
    }

    fun setCustomer(cm_Id: Int?){
        if(cuId.value == cm_Id && cm_Id != null){
            return
        }
        cuId.value = cm_Id
    }

    fun find(id: Int){
        if(_dl_Id.value == id){
            return
        }
        _dl_Id.value = id
    }

    // on press edit invoice
    fun onItemEdit(baseEo: Delivery)
    {
        navigator?.onItemEditClick(baseEo)
    }

    // on press view invoice
    fun onItemView(baseEo: Delivery)
    {
        navigator?.onItemViewClick(baseEo)
    }

    fun onPrient(sl_Id: Int){
        find(sl_Id)
    }

    // cancel job call in destroy fragment
    fun cancelJob(){
        repository.cancelJob()
    }
}
