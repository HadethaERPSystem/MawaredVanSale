package com.mawared.mawaredvansale.controller.auth

import android.app.Activity
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.controller.base.BaseViewModel
import com.mawared.mawaredvansale.data.db.entities.security.User
import com.mawared.mawaredvansale.services.repositories.UserRepository
import com.mawared.mawaredvansale.utilities.ApiException
import com.mawared.mawaredvansale.utilities.Coroutines
import com.mawared.mawaredvansale.utilities.NoConnectivityException
import com.mawared.mawaredvansale.utilities.lazyDeferred


class AuthViewModel(private val repository: UserRepository) : BaseViewModel() {

    var name: String? = null
    var userName : String? = null
    var password : String? = null
    var activity: Activity? = null
    var authListener: IAuthListener? = null

    fun getLoggedInUser() = repository.getUser()

    fun saveUser(user: User){
        lazyDeferred { repository.saveUser(user);}
    }
    fun onLoginButtonClick(){

        hideKeyboard(activity!!)
        authListener?.onStarted()
        if(userName.isNullOrEmpty() || password.isNullOrEmpty()){

            authListener?.onFailure("Invalid email or password")
            return
        }
        val ur : User = User(0, "","", userName, password, "", null, null,
            null, null, null, null, "", "", "")
        var msg = ""
        //success
        Coroutines.main{
            try {
                val authResponse = repository.login(ur)

                authResponse.user?.let {
                    it.uid = 0
                    val salesman = repository.salesmanByUser(it.id)
                    if(salesman != null){
                        App.prefs.savedSalesman = salesman
                        App.prefs.savedVanCode = salesman.sm_van_code
                    }
//                    else{
//                        msg = "You can't login by this user because this user dos'nt related with any salesman."
//                        return@let
//                    }
                    authListener?.onSuccess(it)

                    App.prefs.saveUser = authResponse.user
                    App.prefs.isLoggedIn = true

                    repository.saveUser(it)
                    return@main
                }
                if(!msg.isEmpty()){
                    authListener?.onFailure(msg)
                }else{
                    authListener?.onFailure(authResponse.message!!)
                }
            }catch (e: ApiException){
                authListener?.onFailure(e.message!!)
            }catch (e: NoConnectivityException){
                authListener?.onFailure(e.message!!)
            }
        }
    }

}