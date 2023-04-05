package com.example.anigma.anigma.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.anigma.databinding.ActivityLoginBinding

class Login : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvSignin.setOnClickListener {  }
        binding.tvSignup.setOnClickListener {
            val intent = Intent(this, UserLogin::class.java)
            startActivity(intent)
        }

        binding.tvSignin.setOnClickListener {
            startActivity(Intent(this, UserSignIn::class.java))
        }
    }
}