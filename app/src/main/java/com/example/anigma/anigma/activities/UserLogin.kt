package com.example.anigma.anigma.activities

import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.example.anigma.R
import com.example.anigma.anigma.firebase.FireStoreClass
import com.example.anigma.anigma.model.User
import com.example.anigma.databinding.ActivityUserLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class UserLogin : BaseActivity() {
    private lateinit var binding: ActivityUserLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarItem)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""
        supportActionBar?.setHomeAsUpIndicator(R.drawable.back_arrow)
        binding.toolbarItem.setNavigationOnClickListener {
            onBackPressed()
        }

        binding.submitBtn.setOnClickListener {
            registerUser()
        }
    }

    fun userRegisteredSuccess(){
        Toast.makeText(this@UserLogin,
            "You has been signed up with email successfully.",
            Toast.LENGTH_SHORT).show()
        hideProgressDialog()
        FirebaseAuth.getInstance().signOut()
        finish()
    }

    private fun registerUser(){
        val name = binding.inputName.text.toString().trim{it <= ' '}
        val email = binding.inputEmail.text.toString().trim{it <= ' '}
        val password = binding.inputPassword.text.toString().trim{it <= ' '}

        if(validateForm(name, email, password)){
            showProgressDialog(getString(R.string.please_wait))
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                hideProgressDialog()
                if (task.isSuccessful) {
                    val firebaseUser: FirebaseUser = task.result!!.user!!
                    val registeredEmail = firebaseUser.email
                    val user = User(firebaseUser.uid, name, registeredEmail!!, "", 0, "")
                    FireStoreClass().registerUser(this, user)
                }
                else{
                    Toast.makeText(this@UserLogin,
                    task.exception!!.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun validateForm(name: String, email: String, password: String): Boolean{
        return when{
            TextUtils.isEmpty(name) -> {
                showErrorSnackBar("Name cannot be empty!")
                false
            }
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