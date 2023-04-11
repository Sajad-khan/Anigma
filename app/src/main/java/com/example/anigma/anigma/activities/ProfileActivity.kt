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
    companion object{
        const val CAMERA_REQUEST_CODE = 1
        const val GALLERY_REQUEST_CODE = 2
    }
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
        }

        binding.ivProfile.setOnClickListener {
            val imageSelectionDialog = AlertDialog.Builder(this)
            imageSelectionDialog.setTitle("Select Action")
            val imageDialogItems = arrayOf("Select photo from gallery", "Capture with camera")
            imageSelectionDialog.setItems(imageDialogItems){
                    _, which ->
                when(which){
                    0 -> choosePhotoFromGallery()
                    1 -> captureNewPhoto()
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

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun choosePhotoFromGallery() {
        Dexter.withContext(this).withPermission(
            android.Manifest.permission.READ_MEDIA_IMAGES
        ).withListener(object: PermissionListener {
            override fun onPermissionGranted(response: PermissionGrantedResponse) {
                val galleryIntent = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE)
            }
            override fun onPermissionDenied(response: PermissionDeniedResponse) {
                showRationaleDialogForPermissions()
            }
            override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest,
                                                            token: PermissionToken) {
                showRationaleDialogForPermissions()
            }
        }).check()
    }

    private fun captureNewPhoto() {
        Dexter.withContext(this).withPermission(
            android.Manifest.permission.CAMERA
        ).withListener(object: PermissionListener {
            override fun onPermissionGranted(response: PermissionGrantedResponse) {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
            }
            override fun onPermissionDenied(response: PermissionDeniedResponse) {
                showRationaleDialogForPermissions()
            }
            override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest,
                                                            token: PermissionToken
            ) {
                showRationaleDialogForPermissions()
            }
        }).onSameThread().check()
    }

    private fun showRationaleDialogForPermissions() {
        AlertDialog.Builder(this)
            .setMessage("Permission is required. You can enable it at Settings/HappyPlaces/Permissions")
            .setPositiveButton("Settings"){
                    _, _ -> try{
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            } catch (e: ActivityNotFoundException){
                e.printStackTrace()
            }
            }.setNegativeButton("Cancel"){
                    dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == GALLERY_REQUEST_CODE){
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
            else if(requestCode == CAMERA_REQUEST_CODE){
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
                "USER_IMAGE"+System.currentTimeMillis()+"."+getFileExtension(mSelectedImageFileUri!!)
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
                    updateUserData(profileImageURL)
                }
            }
        }
        else{
            Log.e("Profile error", "couldn't upload image to firebase")
            hideProgressDialog()
        }
    }

    private fun getFileExtension(uri: Uri): String? {

        //Check uri format to avoid null

        //Check uri format to avoid null
        val extension: String? = if (uri.scheme.equals(ContentResolver.SCHEME_CONTENT)) {
            //If scheme is a content
            val mime = MimeTypeMap.getSingleton()
            mime.getExtensionFromMimeType(contentResolver.getType(uri))
        } else {
            //If scheme is a File
            //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
            MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(File(uri.path.toString())).toString())
        }

        return extension
    }

    fun profileUpdateSuccess(){
        hideProgressDialog()
        Toast.makeText(this@ProfileActivity, "Your profile has been updated successfully!",
            Toast.LENGTH_SHORT).show()
        FireStoreClass().loadUserData(this)
        finish()
    }

    private fun updateUserData(imageURL: String){
        val userHashmap = HashMap<String, Any>()
        Log.e("IMAGE_URL_", imageURL)
        if(imageURL.isEmpty()) Log.e("EMPTY_URL", "image url is empty")
        if(imageURL == mUser.imageLocation) Log.e("URL_SAME", "same image url")
        if(imageURL.isNotEmpty() && imageURL != mUser.imageLocation){
            userHashmap[Constants.IMAGE] = imageURL
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