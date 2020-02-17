package com.mawared.mawaredvansale.controller.home.reportsdashboard

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.data.db.entities.security.Menu
import com.mawared.mawaredvansale.interfaces.IMainNavigator
import com.mawared.mawaredvansale.services.repositories.MenuRepository
import com.mawared.mawaredvansale.services.repositories.NetworkState

class ReportsViewModel(repository: MenuRepository) : ViewModel() {
    private var navigator: IMainNavigator<Menu>? = null
    var ctx: Context? = null
    var userName: String? = App.prefs.saveUser?.user_name
    val lang =  App.prefs.systemLanguage!!.substring(0,2)
    var clientName: String? = App.prefs.saveUser?.client_name ?: "AL-Nadir Trading Company"
    var branchName: String? = App.prefs.saveUser?.org_name ?: ""
    var system_name: String? = "Mawared App."
    var system_version: String? = ""

    val menus by lazy {
        repository.getReportLocalMenu(ctx!!)
    }

    val networkState: LiveData<NetworkState> by lazy {
        repository.networkState
    }

    fun listIsEmpty():Boolean{
        return menus.value?.isEmpty() ?: true
    }

    fun setNavigator(navigator: IMainNavigator<Menu>)
    {
        this.navigator = navigator
    }

    fun itemClick(menu: Menu)
    {
        navigator?.onItemViewClick(menu)
    }
}
