package com.mawared.mawaredvansale.controller.callcycle.cylist

import android.location.Location
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.controller.base.BaseViewModel
import com.mawared.mawaredvansale.data.db.entities.md.Call_Cycle
import com.mawared.mawaredvansale.services.repositories.callcycle.ICallCycleRepository
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository
import com.mawared.mawaredvansale.utilities.Coroutines

class CallCycleViewModel(private val repository: ICallCycleRepository, private val mdRepository: IMDataRepository) : BaseViewModel() {
    private val _sm_id: Int = if(App.prefs.savedSalesman?.sm_user_id != null)  App.prefs.savedSalesman!!.sm_user_id!! else 0

    var location: Location? = null
    var term: String? = ""

    var errorMessage: MutableLiveData<String> = MutableLiveData()
    // prop. for data entry

    private val cuId: MutableLiveData<Int> = MutableLiveData()

    fun loadData(list: MutableList<Call_Cycle>, term: String, pageCount: Int, loadMore: (List<Call_Cycle>?, Int) -> Unit){
        try {
            Coroutines.ioThenMain({
                val tmp = repository.getOnPages(_sm_id, term, pageCount)
                if(tmp != null){
                    list.addAll(tmp)
                }
            }, {loadMore(list, pageCount)})
        }catch (e: Exception){
            e.printStackTrace()
        }
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
