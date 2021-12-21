package com.mawared.mawaredvansale.controller.home

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.auth.LoginActivity
import java.lang.Exception

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val background = object: Thread(){
            override fun run() {
                try {
                    Thread.sleep(4000)
                    if(App.prefs.isLoggedIn){
                        Intent(baseContext, HomeActivity::class.java).also {
                            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(it)
                        }
                    }else {
                        val intent = Intent(baseContext, LoginActivity::class.java)
                        startActivity(intent)
                    }
//                    val intent = Intent(baseContext, HomeActivity::class.java)
//                    startActivity(intent)
                }catch (e: Exception){
                    e.printStackTrace()
                }
            }
        }
        background.start()
    }
}
