package com.sinbaram.mapgo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.sinbaram.mapgo.Model.PostFeedItem

class SnsFeedActivity : AppCompatActivity() {
    val baseurl = BuildConfig.SERVER_ADDRESS
    val postImageList = mutableListOf<String>()
    val commentList = mutableListOf<Map<String, String>>()
    val likerList = mutableListOf<Int>()
    lateinit var viewpager : ViewPager2
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

        // load views from activity_sns_feed
        val userName = findViewById<TextView>(R.id.userName)
        val userimage = findViewById<ImageView>(R.id.userImage)
        val posttime = findViewById<TextView>(R.id.postTime)
        val content = findViewById<TextView>(R.id.content)
        viewpager = findViewById(R.id.imageSlider)

        // set post's username, user image, post time, content
        userName.setText(writerName)
        posttime.setText(postTime)
        content.setText(postcontent)
        Glide.with(this).load(writerImage).into(userimage)
        viewpager.adapter = ImageSliderAdapter(this, postImageList)

        //Log.d("outputtest", postId.toString()+" "+writerName+" "+writerImage+" "+postcontent+" "+postcontent+" "+postImageList+" "+postLat+" "+postLong+" "+postTime)
    }
}
