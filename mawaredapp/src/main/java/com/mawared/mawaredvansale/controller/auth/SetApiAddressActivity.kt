package com.mawared.mawaredvansale.controller.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.LayoutDirection
import android.view.View
import androidx.core.view.ViewCompat
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.home.SplashScreenActivity
import com.mawared.mawaredvansale.utilities.snackbar
import kotlinx.android.synthetic.main.activity_set_api_address.*

class SetApiAddressActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_api_address)
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        btnSave.setOnClickListener {
            if(etxt_ApiAddress.text.isNullOrEmpty()){
                it.snackbar(getString(R.string.msg_notempty_api_address))
            }else{
                App.prefs.ApiIP = etxt_ApiAddress.text.toString()

                Intent(baseContext, SplashScreenActivity::class.java).also {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(it)
                }
            }
        }
    }
}