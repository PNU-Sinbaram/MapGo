package com.sinbaram.mapgo.AR.Common

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

/** Collection of asset downloader from url */
class AssetDownloader {
    companion object {
        /**
         * Download image bitmap from given url
         * This can take quite long time, it is recommended to use thread executor
         */
        fun RequestImage(vararg urls: String): Bitmap? {
            try {
                val url = URL(urls[0])
                val connection =
                    url.openConnection() as HttpURLConnection
                connection.connect()
                val inputStream = connection.inputStream
                return BitmapFactory.decodeStream(inputStream)
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }
    }
}
