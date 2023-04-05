package com.example.anigma.anigma.firebase

import android.util.Log
import com.example.anigma.anigma.activities.UserLogin
import com.example.anigma.anigma.activities.UserSignIn
import com.example.anigma.anigma.model.User
import com.example.anigma.anigma.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FireStoreClass {
    private val mFirestore = FirebaseFirestore.getInstance()

    fun registerUser(activity: UserLogin, userInfo: User){
        mFirestore.collection(Constants.USERS)
            .document(getCurrentUserId()).set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegisteredSuccess()
            }.addOnFailureListener {
                Log.e("REGISTER", it.toString())
            }
    }

    fun signInUser(activity: UserSignIn){
        mFirestore.collection(Constants.USERS)
            .document(getCurrentUserId()).get()
            .addOnSuccessListener { document ->
                val loggedInUser = document.toObject(User::class.java)
                if(loggedInUser != null){
                    activity.signInSuccess(loggedInUser)
                }
                else {
                    Log.e("OBJECT", "unable to parcel the object")
                }
            }.addOnFailureListener {
            Log.e("SIGN_IN", it.toString())
        }
    }


    fun getCurrentUserId(): String{
        val currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserId = ""
        if(currentUser != null){
            currentUserId = currentUser.uid
        }
        return currentUserId
    }
}