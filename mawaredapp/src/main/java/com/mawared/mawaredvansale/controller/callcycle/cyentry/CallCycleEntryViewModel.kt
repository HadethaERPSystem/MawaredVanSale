package com.mawared.mawaredvansale.controller.callcycle.cyentry

import android.content.Context
import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.base.BaseViewModel
import com.mawared.mawaredvansale.data.db.entities.md.Call_Cycle
import com.mawared.mawaredvansale.data.db.entities.md.Lookups
import com.mawared.mawaredvansale.interfaces.IAddNavigator
import com.mawared.mawaredvansale.interfaces.IMainNavigator
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.services.repositories.callcycle.ICallCycleRepository
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import java.lang.Exception

class CallCycleEntryViewModel(private val repository: ICallCycleRepository, private val mdRepository: IMDataRepository) : BaseViewModel() {
    private val _sm_id: Int = if(App.prefs.savedSalesman?.sm_user_id != null)  App.prefs.savedSalesman!!.sm_user_id!! else 0

    var mode: String = "Add"
    var addNavigator: IAddNavigator<Call_Cycle>? = null
    var navigator: IMainNavigator<Call_Cycle>? = null
    var msgListener: IMessageListener? = null
    var location: Location? = null
    var ctx: Context? = null
    var isRunning: Boolean = false
    // prop. for data entry
    var mNetworkState: LiveData<NetworkState> = MutableLiveData()

    var cy_notes = MutableLiveData<String>()
    var _cyBaseEo: Call_Cycle? = null

    ///////////////////////////////////////
    /// Autocomplete object for Customer Payment Type
    var selectedStatus: Lookups? = null
    val callCycleStatus: LiveData<List<Lookups>> by lazy {
        mdRepository.lookup_getByEntity("CallCycle")
    }

    var _baseEo: MutableLiveData<Call_Cycle> = MutableLiveData()
    val savedEntity: LiveData<Call_Cycle> = Transformations
        .switchMap(_baseEo){
            repository.saveOrUpdate(it)
        }

    val networkState: LiveData<NetworkState> by lazy {
        repository.networkState
    }

    // is valid
    private fun isValid(): Boolean{
        var isSuccess = true
        var msg: String? = ""

        if(_cyBaseEo == null){
            msg = ctx!!.getString(R.string.msg_error_not_load_call_cycle)
        }
        if(selectedStatus == null){
            msg =(if(!msg.isNullOrEmpty()) "\n\r" else "") + ctx!!.getString(R.string.msg_error_not_select_status)
        }
        if(cy_notes.value.isNullOrEmpty()){
            msg = (if(!msg.isNullOrEmpty()) "\n\r" else "") + ctx!!.getString(R.string.msg_error_not_fill_notes)
        }
        if(!msg.isNullOrEmpty()){
            msgListener?.onFailure(msg)
            isSuccess = false
        }
        return isSuccess
    }

    fun onSave(){
        try {
            if(isValid()){
                isRunning = true
                val user = App.prefs.saveUser!!
                val strDate = LocalDateTime.now()
                val cy_date = LocalDate.now()

                val baseEo = Call_Cycle(
                    user.cl_Id, user.org_Id, _cyBaseEo!!.cy_cu_Id, _sm_id, null, "$cy_date", _cyBaseEo!!.cy_dayname,
                    selectedStatus?.lk_Id, cy_notes.value, location?.latitude, location?.longitude,
                    "$strDate", "${user.id}", "$strDate", "${user.id}"
                )
                _baseEo.postValue(baseEo)
            }
        }catch (e: Exception){
            isRunning = false
            Log.e("OnSaveError", "CallCycleViewModel: OnSave error ${e.message}")
        }
    }

    fun onClear(){
        selectedStatus = null
    }

    fun onCancel(){

    }

    fun cancelJob(){
        repository.cancelJob()
    }
}
