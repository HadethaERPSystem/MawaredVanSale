package com.mawared.mawaredvansale.controller.home.dashboard

import android.content.Context
import android.content.res.Resources
import androidx.lifecycle.*
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.data.db.entities.md.Customer
import com.mawared.mawaredvansale.data.db.entities.md.Salesman
import com.mawared.mawaredvansale.data.db.entities.md.UsersDiscounts
import com.mawared.mawaredvansale.data.db.entities.security.Menu
import com.mawared.mawaredvansale.interfaces.IMainNavigator
import com.mawared.mawaredvansale.services.repositories.MenuRepository
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.services.repositories.masterdata.IMDataRepository
import com.mawared.mawaredvansale.utilities.Coroutines
import com.mawared.mawaredvansale.utilities.lazyDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DashboardViewModel(private val repository: MenuRepository, private val mdRepository: IMDataRepository) : ViewModel() {

    private val _sm_id: Int = if(App.prefs.savedSalesman?.sm_user_id != null)  App.prefs.savedSalesman!!.sm_user_id!! else 0

    private var navigator: IMainNavigator<Menu>? = null
    var res: Resources? = null
    var ctx: Context? = null
    var userName: String? = if(App.prefs.saveUser?.name.isNullOrEmpty()) App.prefs.saveUser?.user_name else App.prefs.saveUser?.name
    val u_id : Int = App.prefs.saveUser!!.id

    val lang =  App.prefs.systemLanguage!!.substring(0,2)
    var clientName: String? = App.prefs.saveUser?.client_name ?: ""
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

    fun salesmanHasPlan(){
        var sm : Salesman? = null

            Coroutines.ioThenMain({
                try {
                    sm = mdRepository.salesman_hasSalesPlan(_sm_id)
                }catch (e: Exception){
                    e.printStackTrace()
                }
            },
            {App.prefs.hasSalesPlan = sm?.hasSalePlan ?: "N"}
            )
    }

//    fun userInvDisc(){
//        var disc : UsersDiscounts? = null
//        try {
//            Coroutines.ioThenMain({
//                try {
//                    disc = mdRepository.getDisc(u_id, "INVOICE")
//
//                }catch (e: Exception){
//                    e.printStackTrace()
//                }
//            },{
//                if(disc != null){
//                    App.prefs.user_invoice_disc = disc!!.discPrcnt.toString()
//                }
//            })
//        }catch (e: java.lang.Exception){
//            e.printStackTrace()
//        }
//    }
//
//    fun userItemDisc(){
//        var disc : UsersDiscounts? = null
//        try {
//            Coroutines.ioThenMain({
//                try {
//                    disc = mdRepository.getDisc(u_id, "ITEM")
//
//                }catch (e: Exception){
//                    e.printStackTrace()
//                }
//            },{
//                if(disc != null){
//                    App.prefs.user_item_disc = disc!!.discPrcnt.toString()
//                }
//            })
//        }catch (e: java.lang.Exception){
//            e.printStackTrace()
//        }
//    }

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
