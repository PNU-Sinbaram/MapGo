package com.sinbaram.mapgo

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuInflater
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.sinbaram.mapgo.Model.ProfileModel
import com.sinbaram.mapgo.databinding.ActivityNavigationBinding
import java.util.regex.Pattern

class NavigationActivity: AppCompatActivity() {
    // Activity binding variable
    private lateinit var mBinding: ActivityNavigationBinding

    //
    private var mDestination: String? = null
        get() {
            return field
        }

    companion object {
        const val ADDRESS_ACTIVITY_CODE = 1470
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Binding mapgo activity layout
        mBinding = ActivityNavigationBinding.inflate(layoutInflater)

        mBinding.addressText.focusable = View.NOT_FOCUSABLE
        mBinding.addressText.setOnClickListener {
            val intent = Intent(applicationContext, AddressActivity::class.java)
            overridePendingTransition(0, 0)
            startActivityForResult(intent, ADDRESS_ACTIVITY_CODE)
        }

        mBinding.confirmButton.setOnClickListener {
            val resultIntent = Intent()
            if (mDestination != null)
                resultIntent.putExtra("Destination", mDestination)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        mBinding.cancelButton.setOnClickListener {
            setResult(RESULT_CANCELED, intent)
            finish()
        }

        // Pass activity binding root
        setContentView(mBinding.root)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val extras: Bundle = data!!.extras!!
            when (requestCode) {
                ADDRESS_ACTIVITY_CODE -> {
                    mDestination = extras.getString("data")
                    mBinding.addressText.text = mDestination!!
                }
            }
        }
    }
}
