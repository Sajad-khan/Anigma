package com.example.anigma.anigma.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.anigma.R
import com.example.anigma.anigma.utils.Constants
import com.example.anigma.databinding.ActivityBoardBinding

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
                    Glide
                        .with(this)
                        .load(contentUri)
                        .centerCrop()
                        .placeholder(R.drawable.grey_placeholder)
                        .into(binding.ivBoard)
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