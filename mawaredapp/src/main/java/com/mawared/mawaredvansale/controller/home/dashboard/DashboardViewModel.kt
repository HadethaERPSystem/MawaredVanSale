package com.mawared.mawaredvansale.controller.home.dashboard

import android.content.Context
import android.content.res.Resources
import androidx.lifecycle.*
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.data.db.entities.security.Menu
import com.mawared.mawaredvansale.interfaces.IMainNavigator
import com.mawared.mawaredvansale.services.repositories.MenuRepository
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.utilities.lazyDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DashboardViewModel(private val repository: MenuRepository) : ViewModel() {

    private var navigator: IMainNavigator<Menu>? = null
    var res: Resources? = null
    var ctx: Context? = null
    var userName: String? = if(App.prefs.saveUser?.name.isNullOrEmpty()) App.prefs.saveUser?.user_name else App.prefs.saveUser?.name
    val lang =  App.prefs.systemLanguage!!.substring(0,2)
    var clientName: String? = App.prefs.saveUser?.client_name ?: "AL-Nadir Trading Company"
    var branchName: String? = App.prefs.saveUser?.org_name ?: ""
    var system_name: String? = "Mawared App."
    var system_version: String? = ""
    var client_logo = "client_logo.png"
    var menusCount = 0
    var errorMessage: MutableLiveData<String> = MutableLiveData()
    private var userId : MutableLiveData<Int> = MutableLiveData(App.prefs.saveUser!!.id)
    val menus : LiveData<List<Menu>> = Transformations.switchMap(userId) {
        repository.getByUserId(it, lang)
    }

    val networkState: LiveData<NetworkState> by lazy {
        repository.networkState
    }

    fun refresh(){
        userId.value = App.prefs.saveUser!!.id
    }

    fun listIsEmpty():Boolean{
        return menusCount == 0
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
