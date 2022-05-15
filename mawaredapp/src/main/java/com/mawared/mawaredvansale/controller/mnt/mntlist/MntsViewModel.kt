package com.mawared.mawaredvansale.controller.mnt.mntlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.PagedList
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.controller.base.BaseViewModel
import com.mawared.mawaredvansale.data.db.entities.mnt.Mnts
import com.mawared.mawaredvansale.interfaces.IMainNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.services.repositories.mnt.IMaintenanceRepository

class MntsViewModel(private val repository: IMaintenanceRepository) : BaseViewModel() {
    private val _sm_id: Int =
        if (App.prefs.savedSalesman?.sm_user_id != null) App.prefs.savedSalesman!!.sm_user_id!! else 0
    var navigator: IMainNavigator<Mnts>? = null
    var msgListener: IMessageListener? = null

    var errorMessage: MutableLiveData<String> = MutableLiveData()
    private val cuId: MutableLiveData<Int> = MutableLiveData()

    val mntsList: LiveData<PagedList<Mnts>> = Transformations
        .switchMap(cuId) {
            repository.getOnPages(_sm_id, it)
        }

    val networkStateRV: LiveData<NetworkState> by lazy {
        repository.getSaleNetworkState()
    }

    val networkState: LiveData<NetworkState> by lazy {
        repository.networkState
    }

    fun listIsEmpty():Boolean{
        return mntsList.value?.isEmpty() ?: true
    }

    private val _Id: MutableLiveData<Int> = MutableLiveData()
    val baseEo: LiveData<Mnts> = Transformations
        .switchMap(_Id) {
            repository.getById(it)
        }

    fun setCustomer(cm_Id: Int?) {
        if (cuId.value == cm_Id && cm_Id != null) {
            return
        }
        cuId.value = cm_Id
    }

    fun onItemDelete(baseEo: Mnts) {
        navigator?.onItemDeleteClick(baseEo)
    }

    fun confirmDelete(baseEo: Mnts) {
        //_sl_Id_for_delete.value = baseEo.sl_Id
    }

    // on press edit invoice
    fun onItemEdit(baseEo: Mnts) {
        navigator?.onItemEditClick(baseEo)
    }

    // on press view invoice
    fun onItemView(baseEo: Mnts) {
        navigator?.onItemViewClick(baseEo)
    }

    // cancel job call in destroy fragment
    fun cancelJob() {
        repository.cancelJob()
    }
}