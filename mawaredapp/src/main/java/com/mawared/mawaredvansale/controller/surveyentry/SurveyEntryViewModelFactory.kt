package com.mawared.mawaredvansale.controller.surveyentry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository
import com.mawared.mawaredvansale.services.repositories.srv.SurveyRepositoryImp

@Suppress("UNCHECKED_CAST")
class SurveyEntryViewModelFactory(private val repositoryImp: SurveyRepositoryImp, private val masterDataRepository: IMDataRepository): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SurveyEntryViewModel(repositoryImp, masterDataRepository) as T
    }
}