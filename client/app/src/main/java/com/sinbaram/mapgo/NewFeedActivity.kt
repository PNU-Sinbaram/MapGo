package com.sinbaram.mapgo

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sinbaram.mapgo.Model.ProfileModel
import com.sinbaram.mapgo.databinding.ActivityNewfeedBinding
import com.sinbaram.mapgo.databinding.ActivityProfileBinding
import java.util.regex.Pattern

class NewFeedActivity: AppCompatActivity() {
    // Activity binding variable
    private lateinit var mBinding: ActivityNewfeedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Binding mapgo activity layout
        mBinding = ActivityNewfeedBinding.inflate(layoutInflater)

        mBinding.keywordInputLayout.isCounterEnabled = true
        mBinding.keywordInputLayout.counterMaxLength = 64
        mBinding.keywordInputLayout.editText!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                val p = Pattern.compile("[0-9|a-z|A-Z|ㄱ-ㅎ|ㅏ-ㅣ|가-힝| ]*")
                val matcher = p.matcher(s.toString())
                if (matcher.find()) {
                    mBinding.keywordInputLayout.setError(null)

                } else {
                    mBinding.keywordInputLayout.setError("특수 문자가 포함되어 있습니다.")
                }
            }
        })

        mBinding.addressInputLayout.isCounterEnabled = true
        mBinding.addressInputLayout.counterMaxLength = 64
        mBinding.addressInputLayout.editText!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                val p = Pattern.compile("[0-9|a-z|A-Z|ㄱ-ㅎ|ㅏ-ㅣ|가-힝| ]*")
                val matcher = p.matcher(s.toString())
                if (matcher.find()) {
                    mBinding.addressInputLayout.setError(null)

                } else {
                    mBinding.addressInputLayout.setError("특수 문자가 포함되어 있습니다.")
                }
            }
        })

        mBinding.contentInputLayout.isCounterEnabled = true
        mBinding.contentInputLayout.counterMaxLength = 100
        mBinding.contentInputLayout.editText!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {}
        })

        mBinding.confirmButton.setOnClickListener {
            //val resultIntent = Intent()
            //resultIntent.putExtra("Profile", mProfileModel)
            //setResult(Activity.RESULT_OK, resultIntent)
            //finish()
        }

        mBinding.cancelButton.setOnClickListener {
            //val resultIntent = Intent()
            //resultIntent.putExtra("Profile", mProfileModel)
            //setResult(Activity.RESULT_OK, resultIntent)
            //finish()
        }

        // Pass activity binding root
        setContentView(mBinding.root)
    }
}
