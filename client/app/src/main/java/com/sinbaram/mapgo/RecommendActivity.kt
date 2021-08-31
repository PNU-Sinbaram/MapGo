package com.sinbaram.mapgo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import com.sinbaram.mapgo.databinding.ActivityRecommendBinding
import java.util.regex.Pattern

class RecommendActivity: AppCompatActivity() {
    // Activity binding variable
    private lateinit var mBinding: ActivityRecommendBinding
    private var mKeywords: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Binding mapgo activity layout
        mBinding = ActivityRecommendBinding.inflate(layoutInflater)

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
                    mKeywords = s.toString()

                } else {
                    mBinding.textInputLayout.setError("특수 문자가 포함되어 있습니다.")
                }
            }
        })

        mBinding.recommendButton.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("Keywords", mKeywords)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        mBinding.cancelButton.setOnClickListener {
            finish()
        }

        // Pass activity binding root
        setContentView(mBinding.root)
    }
}
