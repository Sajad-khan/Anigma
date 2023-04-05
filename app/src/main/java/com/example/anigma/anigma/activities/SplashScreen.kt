package com.example.anigma.anigma.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.example.anigma.R
import com.example.anigma.anigma.firebase.FireStoreClass

class SplashScreen : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        Handler().postDelayed({
            val currentUserId = FireStoreClass().getCurrentUserId()
            if(currentUserId.isNotEmpty()){
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            else{
                startActivity(Intent(this, Login::class.java))
                finish()
            }
        },
            2500)
    }
}