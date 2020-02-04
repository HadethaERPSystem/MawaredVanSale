package com.mawared.mawaredvansale.controller.home.dashboard

import android.content.Context
import android.content.res.Resources
import androidx.lifecycle.ViewModel
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.data.db.entities.security.Menu
import com.mawared.mawaredvansale.interfaces.IMainNavigator
import com.mawared.mawaredvansale.services.repositories.MenuRepository
import com.mawared.mawaredvansale.utilities.lazyDeferred

class DashboardViewModel(repository: MenuRepository) : ViewModel() {

    private var navigator: IMainNavigator<Menu>? = null
    var res: Resources? = null
    var ctx: Context? = null
    var userName: String? = App.prefs.saveUser?.user_name
    val lang =  App.prefs.systemLanguage!!.substring(0,2)
    var clientName: String? = App.prefs.saveUser?.client_name ?: "AL-NADER Co."
    val menus by lazyDeferred {
        repository.getByUserId(App.prefs.saveUser!!.id, lang)
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
