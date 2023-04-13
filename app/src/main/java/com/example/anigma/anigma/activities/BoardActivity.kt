package com.example.anigma.anigma.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.anigma.R
import com.example.anigma.anigma.utils.Constants
import com.example.anigma.databinding.ActivityBoardBinding
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import java.io.IOException

class BoardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBoardBinding
    private var selectedUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityBoardBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setUpActionBar()

        binding.btnCreate.setOnClickListener {

        }
        binding.ivBoard.setOnClickListener {
            Constants.choosePhotoFromGallery(this)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == Constants.GALLERY_REQUEST_CODE){
                if(data != null){
                    val contentUri = data.data
                    selectedUri = contentUri
                    try {
                        val selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentUri)
                        binding.ivBoard.setImageBitmap(selectedImageBitmap)
                    }catch (e: IOException){
                        e.printStackTrace()
                        Toast.makeText(this,
                            "Failed to load the image",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    private fun setUpActionBar() {
        setSupportActionBar(binding.boardToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.back_arrow)
        supportActionBar?.title=""
        binding.boardToolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
}