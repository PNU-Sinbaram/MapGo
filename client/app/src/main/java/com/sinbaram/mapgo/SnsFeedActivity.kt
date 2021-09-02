package com.sinbaram.mapgo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.UiThread
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.sinbaram.mapgo.API.ServerClient
import com.sinbaram.mapgo.API.ServerPostAPI
import com.sinbaram.mapgo.Model.Comment
import com.sinbaram.mapgo.Model.Like
import com.sinbaram.mapgo.Model.PostFeedItem
import com.sinbaram.mapgo.databinding.ActivitySnsFeedBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


/** Activity to show SNS Feed
 *
 * Previous activity should send single sns object as name "data"
 * ex.
 * val data = response.body()!![1]
 * nextIntent.putExtra("data", data)
 *
 * Also, val userId should be user's id who logged in.
 * */
class SnsFeedActivity : AppCompatActivity(), OnMapReadyCallback {
    val userId : Int = 2 // this value will get from MapGoActivity
    val baseurl = BuildConfig.SERVER_ADDRESS
    val postImageList = mutableListOf<String>()
    val commentList = mutableListOf<Map<String, String>>()
    val likerList = mutableListOf<Int>()
    val serverAPI = ServerPostAPI.GetSnsClient()!!.create(ServerClient::class.java)
    val marker = Marker()
    private lateinit var binding : ActivitySnsFeedBinding
    val TAG_SUCCESS = "success"
    val TAG_FAILURE = "error"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySnsFeedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // load sns post's data
        val postInfo = intent.getSerializableExtra("data") as PostFeedItem
        val snsfeed = SnsFeedInformation(postInfo, baseurl)
        marker.position = LatLng(snsfeed.postLat, snsfeed.postLong)
        for (likeobj in snsfeed.liker) {
            likerList += likeobj.liker
        }
        snsfeed.likerCount = likerList.size
        for (imageobj in postInfo.postImage) {
            postImageList += baseurl + imageobj.image
        }

        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map, it).commit()
            }
        mapFragment.getMapAsync(this)

        // set like button visiblity by the user liked/not liked status for post
        val isUserLikedThisPost = likerList.contains(userId)
        if (isUserLikedThisPost) {
            binding.likedButton.setVisibility(View.VISIBLE)
            binding.unlikedButton.setVisibility(View.INVISIBLE)
        }
        else {
            binding.likedButton.setVisibility(View.INVISIBLE)
            binding.unlikedButton.setVisibility(View.VISIBLE)
        }

        // set post's username, user image, post time, content
        setViewWithFeedInformation(snsfeed)
        binding.imageSlider.adapter = ImageSliderAdapter(this, postImageList)
        binding.commentRecycler.layoutManager = LinearLayoutManager(this)
        binding.commentRecycler.setHasFixedSize(false)
        getPostComment(snsfeed.postId)

        binding.commentButton.setOnClickListener(View.OnClickListener {
            val comment_toPost : String = binding.commentEdittext.text.toString()
            uploadPostComment(snsfeed.postId, userId, comment_toPost)
        })
        binding.likeCount.setText(snsfeed.likerCount.toString())
        binding.unlikedButton.setOnClickListener(View.OnClickListener {
            setPostLike(snsfeed.postId, userId)
            setLikeStatus(snsfeed, false)
        })
        binding.likedButton.setOnClickListener(View.OnClickListener {
            removePostLike(snsfeed.postId, userId)
            setLikeStatus(snsfeed, true)
        })
    }

    @UiThread
    override fun onMapReady(map: NaverMap) {
        marker.map = map
        val cameraUpdate = CameraUpdate.scrollTo(marker.position)
        map.moveCamera(cameraUpdate)
    }

    /** set post's username, user image, post time, content */
    fun setViewWithFeedInformation(snsfeed : SnsFeedInformation) {
        binding.userName.text = snsfeed.writerName
        binding.postTime.text = snsfeed.postTime
        binding.content.text = snsfeed.postcontent
        Glide.with(this).load(snsfeed.writerImage).into(binding.userImage)
    }

    /** set button, liker count as current user's like status of current post
     * @Param snsfeed : class SnsFeedInformation which contains information of sns feed
     * @Param isCurrentlyLike : 'current' ike status of user.
     *                           true if current user liked to this post(heart is red),
     *                           false if current user unliked to this post(heart is empty)
     * */
    fun setLikeStatus(snsfeed: SnsFeedInformation, isCurrentlyLike: Boolean) {
        if (isCurrentlyLike) {
            binding.likedButton.setVisibility(View.INVISIBLE)
            binding.unlikedButton.setVisibility(View.VISIBLE)
            snsfeed.likerCount--
        }
        else {
            binding.unlikedButton.setVisibility(View.INVISIBLE)
            binding.likedButton.setVisibility(View.VISIBLE)
            snsfeed.likerCount++
        }
        binding.likeCount.setText(snsfeed.likerCount.toString())
    }

    /** Get comment from post
     * @param postId : id of sns post
     * */
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
                    binding.commentRecycler.adapter = null
                    binding.commentRecycler.layoutManager = null
                    val adapter = CommentRecyclerAdapter(this@SnsFeedActivity, commentList)
                    binding.commentRecycler.adapter = adapter
                    binding.commentRecycler.layoutManager = LinearLayoutManager(this@SnsFeedActivity)
                }
            }

            override fun onFailure(call: Call<List<Comment>>, t: Throwable) {
                Log.d("respError", t.toString())
            }
        })
    }

    /** upload comment to post
     * @param postId : id of post which want to upload comments to
     * @param userId : id of user which will be author of comment
     * @param comment : content of comment
     * */
    fun uploadPostComment(postId : Int, userId : Int, comment : String) {
        val apiCall : Call<String> = serverAPI.PostComment(postId, userId, comment)
        apiCall.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                Log.d(TAG_SUCCESS, "comment done")
                getPostComment(postId)
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d(TAG_FAILURE, "failed to upload comment")
            }
        })
    }

    /** add like to post
     * @param postId : id of post which added like to
     * @param userId : id of user who liked to this post
     */
    fun setPostLike(postId : Int, userId : Int) {
        val apiCall : Call<String> = serverAPI.AddLike(postId, userId)
        apiCall.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                Log.d(TAG_SUCCESS, "add like done")
                likerList+=userId
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d(TAG_FAILURE, "failed to add like")
            }
        })
    }

    /** remove like to post
     * @param postId : id of post which removed like to
     * @param userId : id of user who unliked to this post
     */
    fun removePostLike(postId : Int, userId : Int) {
        val apiCall : Call<String> = serverAPI.DeleteLike(postId, userId)
        apiCall.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                Log.d(TAG_SUCCESS, "remove like done")
                likerList.remove(userId)
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d(TAG_FAILURE, "failed to remove like")
            }
        })
    }
}

/** class of informations of sns feed */
data class SnsFeedInformation(
    val postId: Int,
    val writerName: String,
    val writerImage: String,
    val postcontent: String,
    val postTime: String,
    val postLat: Double,
    val postLong: Double,
    val liker: List<Like>,
    var likerCount: Int,
) {
    constructor(postInfo: PostFeedItem, baseurl : String) : this(
        postInfo.postID,
        postInfo.writer.username,
        baseurl + postInfo.writer.picture,
        postInfo.contents,
        postInfo.postTime.split(".")[0].replace("T", " "),
        postInfo.location.lat,
        postInfo.location.lng,
        postInfo.like,
        0,
    )
}
