package com.example.anigma.anigma.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.example.anigma.R
import com.example.anigma.anigma.firebase.FireStoreClass
import com.example.anigma.anigma.model.User
import com.example.anigma.anigma.utils.Constants
import com.example.anigma.databinding.ActivityUserSignInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class UserSignIn : BaseActivity() {
    private lateinit var binding: ActivityUserSignInBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserSignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Initialize auth
        auth = Firebase.auth
        // Set up actionbar
        setSupportActionBar(binding.toolbarItem)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""
        supportActionBar?.setHomeAsUpIndicator(R.drawable.back_arrow)

        // Toolbar item onClickListener
        binding.toolbarItem.setNavigationOnClickListener {
            onBackPressed()
        }
        // Sign in button onClickListener
        binding.signInBtn.setOnClickListener {
            signInUser()
        }

    }

    fun signInSuccess(loggedInUser: User){
        hideProgressDialog()
        Toast.makeText(this, "${loggedInUser.name} you are logged in.", Toast.LENGTH_LONG).show()
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra(Constants.CURR_USER, loggedInUser)
        startActivity(intent)
        finish()
    }

    private fun signInUser(){
        val email = binding.inputEmail.text.toString().trim{it <= ' '}
        val password = binding.inputPassword.text.toString().trim{it <= ' '}
        if(validateForm(email, password)){
            showProgressDialog(getString(R.string.please_wait))
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        val user = auth.currentUser
                        FireStoreClass().loadUserData(this)
                    } else {
                        Log.e("SIGN_INN", "Problem in user sign in class.")
                    }
                }
        }
    }

    private fun validateForm(email: String, password: String): Boolean{
        return when{
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar("Email address cannot be empty!")
                false
            }
            TextUtils.isEmpty(password) -> {
                showErrorSnackBar("Password cannot be empty!")
                false
            }
            else -> {
                true
            }
        }
    }
}