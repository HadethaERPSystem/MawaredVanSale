package com.mawared.mawaredvansale.controller.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mawared.mawaredvansale.services.repositories.md.DownloadRepository

@Suppress("UNCHECKED_CAST")
class DownloadViewModelFactory(private val downloadRepository: DownloadRepository): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return DownloadViewModel(downloadRepository) as T
    }
}