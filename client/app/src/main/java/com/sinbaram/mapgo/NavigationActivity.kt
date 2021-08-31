package com.sinbaram.mapgo

import android.content.Intent
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.sinbaram.mapgo.databinding.ActivityNavigationBinding

class NavigationActivity: AppCompatActivity() {
    // Activity binding variable
    private lateinit var mBinding: ActivityNavigationBinding

    private lateinit var mWebView: WebView

    inner class JsInterface {
        @JavascriptInterface
        @SuppressWarnings("unused")
        fun processData(data: String) {
            val extra = Bundle()
            val intent = Intent()
            extra.putString("data", data)
            intent.putExtras(extra)
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Binding mapgo activity layout
        mBinding = ActivityNavigationBinding.inflate(layoutInflater)

        mWebView = mBinding.webView
        mWebView.settings.javaScriptEnabled = true
        mWebView.addJavascriptInterface(JsInterface(), "Android")
        mWebView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                mWebView.loadUrl("javascript:sample2_execDaumPostcode();")
            }
        }

        mWebView.loadUrl(BuildConfig.SERVER_ADDRESS + "/images/html/daum.html")

        // Pass activity binding root
        setContentView(mBinding.root)
    }
}
