package com.example.anigma.anigma.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.example.anigma.R
import com.example.anigma.anigma.firebase.FireStoreClass
import com.example.anigma.anigma.model.User
import com.example.anigma.anigma.utils.Constants
import com.example.anigma.databinding.ActivityProfileBinding
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import java.io.File
import java.io.IOException

class ProfileActivity : BaseActivity() {
    private lateinit var binding: ActivityProfileBinding
    private var mSelectedImageFileUri: Uri? = null
    private var profileImageURL: String = ""
    private lateinit var mUser: User

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.profileToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""
        supportActionBar?.setHomeAsUpIndicator(R.drawable.back_arrow)
        FireStoreClass().loadUserData(this)
        binding.profileToolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        binding.btnUpdate.setOnClickListener {
            if(mSelectedImageFileUri != null){
                uploadUserImage()
            }
            else{
                showProgressDialog(getString(R.string.please_wait))
                updateUserData(false)
            }
        }

        binding.ivProfile.setOnClickListener {
            val imageSelectionDialog = AlertDialog.Builder(this)
            imageSelectionDialog.setTitle("Select Action")
            val imageDialogItems = arrayOf("Select photo from gallery", "Capture with camera")
            imageSelectionDialog.setItems(imageDialogItems){
                    _, which ->
                when(which){
                    0 -> Constants.choosePhotoFromGallery(this)
                    1 -> Constants.captureNewPhoto(this)
                }
            }
            imageSelectionDialog.show()
        }
    }

    fun updateProfileUI(user: User){
        mUser = user
        Glide
            .with(this)
            .load(user.imageLocation)
            .centerCrop()
            .placeholder(R.drawable.ic_nav_placeholder)
            .into(findViewById(R.id.iv_profile))

        binding.profileName.setText(user.name)
        binding.profileEmail.setText(user.email)
        binding.profilePhone.setText(user.mobileNo.toString())
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == Constants.GALLERY_REQUEST_CODE){
                if(data != null){
                    val contentUri = data.data
                    mSelectedImageFileUri = contentUri
                    try {
                        val selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentUri)
                        binding.ivProfile.setImageBitmap(selectedImageBitmap)
                    }catch (e: IOException){
                        e.printStackTrace()
                        Toast.makeText(this,
                            "Failed to load the image",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else if(requestCode == Constants.CAMERA_REQUEST_CODE){
                val imageBitmap = data!!.extras!!.get("data") as Bitmap
                mSelectedImageFileUri = data.data
                binding.ivProfile.setImageBitmap(imageBitmap)
            }

        }
    }

    private fun uploadUserImage(){
        showProgressDialog(resources.getString(R.string.please_wait))
        if(mSelectedImageFileUri != null){
            val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
                "USER_IMAGE"+System.currentTimeMillis()+"."+Constants.getFileExtension(this,mSelectedImageFileUri!!)
            )
            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
                taskSnapshot ->
                Log.e(
                    "Firebase image URL",
                    taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )
                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    uri->
                    profileImageURL = uri.toString()
                    Log.i("Downloadable image url", profileImageURL)
                    updateUserData(true)
                }
            }
        }
        else{
            Log.e("Profile error", "couldn't upload image to firebase")
            hideProgressDialog()
        }
    }

    fun profileUpdateSuccess(){
        hideProgressDialog()
        Toast.makeText(this@ProfileActivity, "Your profile has been updated successfully!",
            Toast.LENGTH_SHORT).show()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun updateUserData(imageChanged: Boolean){
        val userHashmap = HashMap<String, Any>()
        if(imageChanged){
            Log.e("IMAGE_URL_", profileImageURL)
            if(profileImageURL.isEmpty()) Log.e("EMPTY_URL", "image url is empty")
            if(profileImageURL == mUser.imageLocation) Log.e("URL_SAME", "same image url")
            if(profileImageURL.isNotEmpty() && profileImageURL != mUser.imageLocation){
                userHashmap[Constants.IMAGE] = profileImageURL
            }
        }
        if(binding.profileName.text.toString() != mUser.name){
            userHashmap[Constants.NAME] = binding.profileName.text.toString()
        }
        if(binding.profilePhone.toString() != mUser.mobileNo.toString()){
            userHashmap[Constants.MOBILE] = binding.profilePhone.text.toString().toLong()
        }
        FireStoreClass().updateUserData(this, userHashmap)
    }
}