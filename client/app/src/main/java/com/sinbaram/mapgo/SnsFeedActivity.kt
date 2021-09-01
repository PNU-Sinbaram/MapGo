package com.sinbaram.mapgo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.sinbaram.mapgo.Model.PostFeedItem

class SnsFeedActivity : AppCompatActivity() {
    val baseurl = BuildConfig.SERVER_ADDRESS
    val postImageList = mutableListOf<String>()
    val commentList = mutableListOf<Map<String, String>>()
    val likerList = mutableListOf<Int>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sns_feed)

        // load sns post's data
        val postInfo = intent.getSerializableExtra("data") as PostFeedItem
        val postId : Int = postInfo.postID
        val writerName : String = postInfo.writer.username
        val writerImage : String = baseurl + postInfo.writer.picture
        val postcontent : String = postInfo.contents
        val postLat : Double = postInfo.location.lat
        val postLong : Double = postInfo.location.lng
        val postTime : String = postInfo.postTime.split(".")[0].replace("T", " ")
        val liker = postInfo.like
        for (likeobj in liker) {
            likerList += likeobj.liker
        }
        for (imageobj in postInfo.postImage) {
            postImageList += baseurl + imageobj.image
        }

        //Log.d("outputtest", postId.toString()+" "+writerName+" "+writerImage+" "+postcontent+" "+postcontent+" "+postImageList+" "+postLat+" "+postLong+" "+postTime)
    }
}
