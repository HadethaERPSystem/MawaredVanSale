package com.mawared.mawaredvansale.controller.auth

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.home.HomeActivity
import com.mawared.mawaredvansale.data.db.entities.security.User
import com.mawared.mawaredvansale.databinding.ActivityLoginBinding
import com.mawared.mawaredvansale.utilities.hide
import com.mawared.mawaredvansale.utilities.show
import com.mawared.mawaredvansale.utilities.snackbar
import kotlinx.android.synthetic.main.activity_login.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class LoginActivity : AppCompatActivity(), IAuthListener, KodeinAware {

    override val kodein by kodein()
    private val factory : AuthViewModelFactory by instance()
    var user : User = User(1, "Ali Bawi", "ali.bawi@hadetha.com", "ali.bawi", "a12345", "",
        null, null, null, "", null, null, null, null, null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding : ActivityLoginBinding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        val viewModel = ViewModelProviders.of(this, factory).get(AuthViewModel::class.java)
        binding.viewmodel = viewModel

        group_loading.hide();

        //viewModel.saveUser(user)
        viewModel.authListener = this

        if(App.prefs.isLoggedIn){
            Intent(this, HomeActivity::class.java).also {
                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(it)
            }
        }
        else{
            viewModel.getLoggedInUser().observe(this, Observer { user ->
                if(user != null){
                    Intent(this, HomeActivity::class.java).also {
                        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(it)
                    }
                }
            })
        }
    }

    override fun onCreateView(
        parent: View?,
        name: String,
        context: Context,
        attrs: AttributeSet
    ): View? {
//        val packageName = getPackageName();
//        val resId: Int = getResources().getIdentifier("login_page_title", "string", packageName)
//        supportActionBar!!.title = getString(resId)
//        supportActionBar!!.subtitle = ""
        return super.onCreateView(parent, name, context, attrs)
    }
    override fun onStarted() {
        progressBar_loading.show()
    }

    override fun onSuccess(user: User) {
        progressBar_loading.hide()
    }

    override fun onFailure(message: String) {

        progressBar_loading.hide()
        root_layout.snackbar(message)
    }
}
