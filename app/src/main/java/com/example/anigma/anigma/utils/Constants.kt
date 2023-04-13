package com.example.anigma.anigma.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import android.webkit.MimeTypeMap
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat.startActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import java.io.File

object Constants {
    const val USERS: String = "users"
    const val CURR_USER: String = "currentUser"

    const val MOBILE: String = "mobileNo"
    const val NAME: String = "name"
    const val IMAGE: String = "imageLocation"
    const val GALLERY_REQUEST_CODE = 1
    const val CAMERA_REQUEST_CODE = 5

    fun choosePhotoFromGallery(activity: Activity) {
        Dexter.withContext(activity.baseContext).withPermission(
            android.Manifest.permission.READ_MEDIA_IMAGES
        ).withListener(object: PermissionListener {
            override fun onPermissionGranted(response: PermissionGrantedResponse) {
                val galleryIntent = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(activity, galleryIntent, GALLERY_REQUEST_CODE, null)
            }
            override fun onPermissionDenied(response: PermissionDeniedResponse) {
                showRationaleDialogForPermissions(activity.baseContext)
            }
            override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest,
                                                            token: PermissionToken
            ) {
                showRationaleDialogForPermissions(activity.baseContext)
            }
        }).check()
    }

    fun captureNewPhoto(activity: Activity) {
        Dexter.withContext(activity.baseContext).withPermission(
            android.Manifest.permission.CAMERA
        ).withListener(object: PermissionListener {
            override fun onPermissionGranted(response: PermissionGrantedResponse) {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(activity,cameraIntent, CAMERA_REQUEST_CODE,null)
            }
            override fun onPermissionDenied(response: PermissionDeniedResponse) {
                showRationaleDialogForPermissions(activity.baseContext)
            }
            override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest,
                                                            token: PermissionToken
            ) {
                showRationaleDialogForPermissions(activity.baseContext)
            }
        }).onSameThread().check()
    }

    fun getFileExtension(activity: Activity, uri: Uri): String? {

        //Check uri format to avoid null

        //Check uri format to avoid null
        val extension: String? = if (uri.scheme.equals(ContentResolver.SCHEME_CONTENT)) {
            //If scheme is a content
            val mime = MimeTypeMap.getSingleton()
            mime.getExtensionFromMimeType(activity.contentResolver.getType(uri))
        } else {
            //If scheme is a File
            //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
            MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(File(uri.path.toString())).toString())
        }

        return extension
    }
    private fun showRationaleDialogForPermissions(context: Context) {
        AlertDialog.Builder(context)
            .setMessage("Permission is required. You can enable it at Settings/HappyPlaces/Permissions")
            .setPositiveButton("Settings"){
                    _, _ -> try{
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package",context.packageName, null)
                intent.data = uri
                startActivity(context, intent, null)
            } catch (e: ActivityNotFoundException){
                e.printStackTrace()
            }
            }.setNegativeButton("Cancel"){
                    dialog, _ ->
                dialog.dismiss()
            }.show()
    }
}