package com.example.anigma.anigma.firebase

import android.app.Activity
import android.util.Log
import com.example.anigma.anigma.activities.ProfileActivity
import com.example.anigma.anigma.activities.MainActivity
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

    fun updateUserData(activity: ProfileActivity, userHashMap: HashMap<String, Any>){
        mFirestore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .update(userHashMap)
            .addOnSuccessListener {
                activity.profileUpdateSuccess()
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
            }
    }
    fun loadUserData(activity: Activity){
        mFirestore.collection(Constants.USERS)
            .document(getCurrentUserId()).get()
            .addOnSuccessListener { document ->
                val loggedInUser = document.toObject(User::class.java)
                if(loggedInUser != null){
                    when(activity){
                        is UserSignIn -> {
                            activity.signInSuccess(loggedInUser)
                        }
                        is MainActivity -> {
                            activity.updateNavigationUserDetails(loggedInUser)
                        }
                        is ProfileActivity ->{
                            activity.updateProfileUI(loggedInUser)
                        }
                    }
                }
                else {
                    Log.e("OBJECT", "unable to parcel the object")
                    MainActivity().hideProgressDialog()
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