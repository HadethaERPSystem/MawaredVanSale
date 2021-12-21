package com.mawared.mawaredvansale.controller.settings

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.base.BaseViewModel
import com.mawared.mawaredvansale.data.db.entities.md.Salesman
import com.mawared.mawaredvansale.data.db.entities.md.Warehouse
import com.mawared.mawaredvansale.interfaces.IMessageListener
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository

class SettingsViewModel(private val masterDataRepository: IMDataRepository) : BaseViewModel() {
    var msgListener: IMessageListener? = null
    var ctx: Context? = null
    var errorMessage: MutableLiveData<String> = MutableLiveData()

    var selectedSalesman: Salesman? = null
    private val term: MutableLiveData<String> = MutableLiveData()
    val salesmanList: LiveData<List<Salesman>> = Transformations
        .switchMap(term) {
            masterDataRepository.salesman_GetAll()
        }

    var selectedWarehouse: Warehouse? = null
    private val wr_Id: MutableLiveData<Int> = MutableLiveData()
    val warEoList: LiveData<List<Warehouse>> = Transformations
        .switchMap(wr_Id) {
            masterDataRepository.warehouse_GetBySalesman(it)
        }

    //------------- set function
    fun setWhsId(id: Int) {
        if (wr_Id.value == id) {
            return
        }
        wr_Id.value = id
    }

    fun setTerm(name: String) {
        if (term.value == name) {
            return
        }
        term.value = name
    }

    //---------------------
    //---- button function
    fun onSave() {
        if (isValid()) {
            try {
                selectedSalesman!!.sm_warehouse_id = selectedWarehouse!!.wr_Id
                App.prefs.savedSalesman = selectedSalesman
                App.prefs.savedWarehouse = selectedWarehouse

            } catch (e: Exception) {
                msgListener?.onFailure("${ctx!!.resources!!.getString(R.string.msg_exception)} Exception is ${e.message}")
            }
        }
    }

    private fun isValid(): Boolean {
        var isSuccess = true
        var msg: String? = null

        if (App.prefs.savedSalesman == null) {
            msg += "\n\r" + ctx!!.resources!!.getString(R.string.msg_error_no_selected_salesman)
        }

        if (App.prefs.savedWarehouse == null) {
            msg += "\n\r" + ctx!!.resources!!.getString(R.string.msg_error_no_selected_warehouse)
        }

        if (!msg.isNullOrEmpty()) {
            isSuccess = false
            msgListener?.onFailure(msg)
        }
        return isSuccess
    }
}
