package com.mawared.mawaredvansale.controller.settings

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mawared.mawaredvansale.services.repositories.md.DownloadRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception


class DownloadViewModel(private val downloadRepository: DownloadRepository): ViewModel() {

    private var ErrorMessage : MutableLiveData<String> = MutableLiveData<String>()

    fun onDownloadClick(){
        try {
            GlobalScope.launch {
                downloadRepository.downloadProductBrand("")
                downloadRepository.downloadProductCategory("")
                downloadRepository.downloadProduct("", null, "POS")
                downloadRepository.downloadProductPriceList()

                //downloadRepository.downloadCustomers(1)

                downloadRepository.downloadCurrency()
                downloadRepository.downloadCurrencyRate()

                //downloadRepository.downloadRegions()
                //downloadRepository.downloadSalesman("pda-00001")
            }
        }
        catch (ex : Exception){
            ErrorMessage.value = "حدث خطأ خلال عملية تنزيل البيانات"
            Log.e("DownloadExepction", "Error during dowanload data from server ${ex.message}")
        }

    }
}