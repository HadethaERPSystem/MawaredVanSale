package com.mawared.mawaredvansale.controller.home

import androidx.lifecycle.ViewModel
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.data.db.entities.security.User
import com.mawared.mawaredvansale.services.repositories.UserRepository
import com.mawared.mawaredvansale.utilities.Coroutines

class HomeViewModel(private val repository: UserRepository): ViewModel() {

    val userName: String? = App.prefs.saveUser?.user_name
    val clientName: String? = App.prefs.saveUser?.client_name ?: "AL-NADER Co."

    fun deleteUser(user: User){
        Coroutines.main {
            repository.delete(user)
        }
    }
}