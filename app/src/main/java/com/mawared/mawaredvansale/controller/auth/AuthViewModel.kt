package com.mawared.mawaredvansale.controller.auth

import androidx.lifecycle.ViewModel
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.data.db.entities.security.User
import com.mawared.mawaredvansale.services.repositories.UserRepository
import com.mawared.mawaredvansale.utilities.ApiException
import com.mawared.mawaredvansale.utilities.Coroutines
import com.mawared.mawaredvansale.utilities.NoConnectivityException
import com.mawared.mawaredvansale.utilities.lazyDeferred


class AuthViewModel(private val repository: UserRepository) : ViewModel() {

    var name: String? = null
    var userName : String? = null
    var password : String? = null

    var authListener: IAuthListener? = null

    fun getLoggedInUser() = repository.getUser()

    fun saveUser(user: User){
        lazyDeferred { repository.saveUser(user);}
    }
    fun onLoginButtonClick(){

        authListener?.onStarted()
        if(userName.isNullOrEmpty() || password.isNullOrEmpty()){

            authListener?.onFailure("Invalid email or password")
            return
        }

        val ur : User = User(0, "","", userName, password, "", null, null,
            null, null, null, null, "", "", "")
        //success
        Coroutines.main{
            try {
                val authResponse = repository.login(ur)
                authResponse.user?.let {
                    authListener?.onSuccess(it)
                    repository.saveUser(it)
                    App.prefs.saveUser = authResponse.user
                    App.prefs.isLoggedIn = true
                    App.prefs.savedVanCode = "VAN-00001"
                    val salesman = repository.salesmanGet("VAN-00001")
                    if(salesman != null){
                        App.prefs.savedSalesman = salesman
                    }
                    return@main
                }
                authListener?.onFailure(authResponse.message!!)
                //authListener?.onFailure("Something went wrong, please try again.")
            }catch (e: ApiException){
                authListener?.onFailure(e.message!!)
            }catch (e: NoConnectivityException){
                authListener?.onFailure(e.message!!)
            }
        }
    }

}