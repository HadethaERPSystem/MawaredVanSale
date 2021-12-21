package com.mawared.mawaredvansale.controller.callcycle.cylist

import android.location.Location
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.PagedList
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.controller.base.BaseViewModel
import com.mawared.mawaredvansale.data.db.entities.md.Call_Cycle
import com.mawared.mawaredvansale.interfaces.IMainNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.services.repositories.callcycle.ICallCycleRepository
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository

class CallCycleViewModel(private val repository: ICallCycleRepository, private val mdRepository: IMDataRepository) : BaseViewModel() {
    private val _sm_id: Int = if(App.prefs.savedSalesman?.sm_user_id != null)  App.prefs.savedSalesman!!.sm_user_id!! else 0

    var navigator: IMainNavigator<Call_Cycle>? = null
    var msgListener: IMessageListener? = null
    var location: Location? = null

    var errorMessage: MutableLiveData<String> = MutableLiveData()
    // prop. for data entry

    private val cuId: MutableLiveData<Int> = MutableLiveData()

    val baseEoList: LiveData<PagedList<Call_Cycle>> = Transformations
        .switchMap(cuId) {
            repository.fetchLivePagedList(_sm_id, it)
        }

    val networkStateRV: LiveData<NetworkState> by lazy {
        repository.getCyNetworkState()
    }

    fun listIsEmpty():Boolean{
        return baseEoList.value?.isEmpty() ?: true
    }

    fun setCustomer(cu_id: Int?){
        if(cuId.value == cu_id && cu_id != null)
            return
        cuId.value = cu_id
    }

    fun refresh(){
        setCustomer(cuId.value)
    }
    fun onItemEdit(baseEo: Call_Cycle){

        navigator?.onItemEditClick(baseEo)
    }

    fun isVisibile(cy_id: Int): Int{
        if(cy_id == 0)
            return View.VISIBLE
        else
            return View.GONE
    }

    fun cancelJob(){
        repository.cancelJob()
    }
}
