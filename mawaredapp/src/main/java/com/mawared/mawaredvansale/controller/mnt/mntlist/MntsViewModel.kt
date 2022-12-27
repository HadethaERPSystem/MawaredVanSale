package com.mawared.mawaredvansale.controller.mnt.mntlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.controller.base.BaseViewModel
import com.mawared.mawaredvansale.data.db.entities.mnt.Mnts
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.services.repositories.mnt.IMaintenanceRepository
import com.mawared.mawaredvansale.utilities.Coroutines

class MntsViewModel(private val repository: IMaintenanceRepository) : BaseViewModel() {
    private val _sm_id: Int =
        if (App.prefs.savedSalesman?.sm_user_id != null) App.prefs.savedSalesman!!.sm_user_id!! else 0

    var errorMessage: MutableLiveData<String> = MutableLiveData()
    var term: String? = ""

    fun loadData(list: MutableList<Mnts>, term: String, pageCount: Int, loadMore: (List<Mnts>?, Int) -> Unit){
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

    private val _Id: MutableLiveData<Int> = MutableLiveData()
    val baseEo: LiveData<Mnts> = Transformations
        .switchMap(_Id) {
            repository.getById(it)
        }

    // cancel job call in destroy fragment
    fun cancelJob() {
        repository.cancelJob()
    }
}