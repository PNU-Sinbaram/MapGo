package com.sinbaram.mapgo

import android.Manifest
import android.R.attr
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sinbaram.mapgo.databinding.ActivityProfileBinding
import android.os.Environment
import androidx.core.app.ActivityCompat.startActivityForResult

import android.provider.MediaStore

import androidx.core.content.FileProvider

import android.widget.Toast

import android.content.Intent
import android.net.Uri
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.app.ActivityCompat.startActivityForResult
import android.R.attr.data
import android.graphics.Bitmap
import android.text.Editable

import android.text.TextWatcher
import com.sinbaram.mapgo.Model.ProfileModel
import java.util.regex.Pattern


class ProfileActivity : AppCompatActivity() {
    // Activity binding variable
    private lateinit var mBinding: ActivityProfileBinding

    // Profile model
    private val mProfileModel = ProfileModel()

    companion object {
        val REQUEST_PICTURE_CAPTURE = 1
        val PICK_IMAGE = 1111
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Binding mapgo activity layout
        mBinding = ActivityProfileBinding.inflate(layoutInflater)

        // Check required permissions and request.
        val bCameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val bWritePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        val bReadPermission = ContextCompat.checkSelfPermission (this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        if (!bCameraPermission && !bWritePermission && !bReadPermission) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
                0
            )
        }

        mBinding.imageButton.setOnClickListener {
            dispatchTakePictureIntent()
        }

        mBinding.textInputLayout.isCounterEnabled = true
        mBinding.textInputLayout.counterMaxLength = 16
        mBinding.textInputLayout.editText!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                val p = Pattern.compile("[0-9|a-z|A-Z|ㄱ-ㅎ|ㅏ-ㅣ|가-힝| ]*")
                val matcher = p.matcher(s.toString())
                if (matcher.find()) {
                    mBinding.textInputLayout.setError(null)
                    mProfileModel.nickname = s.toString()

                } else {
                    mBinding.textInputLayout.setError("특수 문자가 포함되어 있습니다.")
                }
            }
        })

        mBinding.confirmButton.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("Profile", mProfileModel)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        mBinding.cancelButton.setOnClickListener {
            finish()
        }

        // Pass activity binding root
        setContentView(mBinding.root)
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_PICTURE_CAPTURE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_PICTURE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data!!.extras!!.get("data") as Bitmap
            mBinding.imageButton.setImageBitmap(imageBitmap)
            mProfileModel.profile = imageBitmap
        }

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                val selectedImage: Uri? = data.data
            }
        }
    }

    private fun pickFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        val mimeTypes = arrayOf("image/jpeg", "image/png")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        startActivityForResult(intent, PICK_IMAGE)
    }
}
