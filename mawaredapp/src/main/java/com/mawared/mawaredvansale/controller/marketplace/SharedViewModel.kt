package com.mawared.mawaredvansale.controller.marketplace

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mawared.mawaredvansale.data.db.entities.md.Customer

class SharedViewModel : ViewModel() {
    val onlyBrowsing = MutableLiveData<String>()
    val customer = MutableLiveData<Customer>()
    val vocode = MutableLiveData<String>()

    fun setBrowsingOnly(value: String){
        onlyBrowsing.value = value
    }

    fun setCustomer(cu: Customer){
        customer.value = cu
    }

    fun setVoucher(v: String){
        vocode.value = v
    }
}