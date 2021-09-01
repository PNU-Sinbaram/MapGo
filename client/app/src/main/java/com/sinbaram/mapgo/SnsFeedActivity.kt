package com.sinbaram.mapgo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.sinbaram.mapgo.API.ServerAPI
import com.sinbaram.mapgo.API.ServerClient
import com.sinbaram.mapgo.API.ServerPostAPI
import com.sinbaram.mapgo.Model.Comment
import com.sinbaram.mapgo.Model.PostFeedItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SnsFeedActivity : AppCompatActivity() {
    val userId : Int = 1 // this value will get from MapGoActivity
    val baseurl = BuildConfig.SERVER_ADDRESS
    val postImageList = mutableListOf<String>()
    val commentList = mutableListOf<Map<String, String>>()
    val likerList = mutableListOf<Int>()
    val serverAPI = ServerPostAPI.GetSnsClient()!!.create(ServerClient::class.java)
    lateinit var viewpager : ViewPager2
    lateinit var recycler : RecyclerView
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
        val commentButton = findViewById<Button>(R.id.commentButton)
        val commentEditText = findViewById<EditText>(R.id.commentEdittext)
        val unlikedButton = findViewById<ImageButton>(R.id.unlikedButton)
        val likedButton = findViewById<ImageButton>(R.id.likedButton)
        recycler = findViewById(R.id.commentRecycler)

        // set like button visiblity by the user liked/not liked status for post
        val isUserLikedThisPost = likerList.contains(userId)
        if (isUserLikedThisPost) {
            likedButton.setVisibility(View.VISIBLE)
            unlikedButton.setVisibility(View.INVISIBLE)
        }
        else {
            likedButton.setVisibility(View.INVISIBLE)
            unlikedButton.setVisibility(View.VISIBLE)
        }


        // set post's username, user image, post time, content
        userName.setText(writerName)
        posttime.setText(postTime)
        content.setText(postcontent)
        Glide.with(this).load(writerImage).into(userimage)

        viewpager.adapter = ImageSliderAdapter(this, postImageList)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.setHasFixedSize(false)
        getPostComment(postId)

        commentButton.setOnClickListener(View.OnClickListener {
            val comment_toPost : String = commentEditText.text.toString()
            uploadPostComment(postId, userId, comment_toPost)
        })

        unlikedButton.setOnClickListener(View.OnClickListener {
            setPostLike(postId, userId)
            unlikedButton.setVisibility(View.INVISIBLE)
            likedButton.setVisibility(View.VISIBLE)
        })

        likedButton.setOnClickListener(View.OnClickListener {
            removePostLike(postId, userId)
            likedButton.setVisibility(View.INVISIBLE)
            unlikedButton.setVisibility(View.VISIBLE)
        })

        //Log.d("outputtest", postId.toString()+" "+writerName+" "+writerImage+" "+postcontent+" "+postcontent+" "+postImageList+" "+postLat+" "+postLong+" "+postTime)
    }

    fun getPostComment(postId : Int) {
        commentList.clear()
        val apiCall : Call<List<Comment>> = serverAPI.GetComments(postId)
        apiCall.enqueue(object: Callback<List<Comment>> {
            override fun onResponse(call: Call<List<Comment>>, response: Response<List<Comment>>) {
                if (response.code() == 200) {
                    val commentResponseList : List<Comment>
                    commentResponseList = response.body()!!
                    for (commentObj in commentResponseList) {
                        val commentMap = mapOf("writer" to commentObj.writer.username, "writerImage" to baseurl+commentObj.writer.picture, "contents" to commentObj.contents)
                        commentList += commentMap
                    }
                    recycler.adapter = null
                    recycler.layoutManager = null
                    val adapter = CommentRecyclerAdapter(this@SnsFeedActivity, commentList)
                    recycler.adapter = adapter
                    recycler.layoutManager = LinearLayoutManager(this@SnsFeedActivity)
                }
            }

            override fun onFailure(call: Call<List<Comment>>, t: Throwable) {
                Log.d("respError", "Failed to get comments : "+t.toString())
            }
        })
    }

    fun uploadPostComment(postId : Int, userId : Int, comment : String) {
        val apiCall : Call<String> = serverAPI.PostComment(postId, userId, comment)
        apiCall.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                Log.d("test", "comment done")
                getPostComment(postId)
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d("respError", "Failed to post comments : "+t.toString())
            }
        })
    }

    fun setPostLike(postId : Int, userId : Int) {
        val apiCall : Call<String> = serverAPI.AddLike(postId, userId)
        apiCall.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                Log.d("test", "add like done")
                likerList+=userId
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d("respError", "Failed to add like : "+t.toString())
            }
        })
    }

    fun removePostLike(postId : Int, userId : Int) {
        val apiCall : Call<String> = serverAPI.DeleteLike(postId, userId)
        apiCall.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                Log.d("test", "remove like done")
                likerList.remove(userId)
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d("respError", "Failed to remove like : "+t.toString())
            }
        })
    }
}
