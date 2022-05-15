package com.mawared.mawaredvansale.controller.auth

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.home.HomeActivity
import com.mawared.mawaredvansale.data.db.entities.security.User
import com.mawared.mawaredvansale.databinding.ActivityLoginBinding
import com.mawared.mawaredvansale.services.repositories.NetworkState
import com.mawared.mawaredvansale.utilities.snackbar
import com.mawared.update.AppUtils
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import java.io.File

class LoginActivity : AppCompatActivity(), IAuthListener, KodeinAware {
    //var filePath: File? = null
    override val kodein by kodein()
    private val factory : AuthViewModelFactory by instance()

    val viewModel by lazy {
        ViewModelProviders.of(this, factory).get(AuthViewModel::class.java)
    }

    var user : User = User(
        1, "Ali Bawi", "ali.bawi@hadetha.com", "ali.bawi", "a12345", "",
        null, null, null, "", null, null, null, null, null, null,
        null,0
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding : ActivityLoginBinding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_login
        )

        binding.viewmodel = viewModel
        binding.lifecycleOwner = this
        viewModel.activity = this
        llProgressBar?.visibility = View.GONE

        //viewModel.saveUser(user)
        viewModel.authListener = this
        val serial = Build.SERIAL
        //AppUtils.getVersionCode(context)
        //deleteCache(this)
        if(App.prefs.isLoggedIn){
            Intent(this, HomeActivity::class.java).also {
                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(it)
            }
        }
        else{
            viewModel.getLoggedInUser().observe(this, Observer { user ->
                if (user != null) {
                    Intent(this, HomeActivity::class.java).also {
                        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(it)
                    }
                    App.prefs.app_version = AppUtils.getVersionName(this)
                }
            })
        }
        bindUI()
    }

    fun bindUI()= GlobalScope.launch(Dispatchers.Main){
//        viewModel.client.observe(this@LoginActivity, Observer {
//            viewModel.clientName.postValue(it.name)
//        })

        viewModel.networkState.observe(this@LoginActivity, Observer {
            progress_bar_login.visibility =
                if (it == NetworkState.LOADING) View.VISIBLE else View.GONE

            if ((it == NetworkState.ERROR || it == NetworkState.NODATA)) {
                val pack = packageName
                val id = resources.getIdentifier(it.msg, "string", pack)
                viewModel.errorMessage.value = resources.getString(id)
                txt_error_login.visibility = View.VISIBLE
            } else {
                txt_error_login.visibility = View.GONE
            }

        })
    }

    override fun onStarted() {
        llProgressBar?.visibility = View.VISIBLE
    }

    override fun onSuccess(user: User) {
        llProgressBar?.visibility = View.GONE
    }

    override fun onFailure(message: String) {

        llProgressBar?.visibility = View.GONE
        root_layout?.snackbar(message)
    }
}
