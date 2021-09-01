package com.sinbaram.mapgo.Model

import java.io.Serializable

class PostFeed : ArrayList<PostFeedItem>()

data class PostFeedItem(
    val comment: ArrayList<Comment>,
    val contents: String,
    val like: List<Like>,
    val location: Location,
    val postID: Int,
    val postImage: List<PostImage>,
    val postTime: String,
    val totalLikes: Int,
    val writer: Writer
) : Serializable

class CommentResponse : ArrayList<Comment>()

data class Comment(
    val writer: Writer,
    val contents: String,
    val post: Int
) : Serializable

data class Location(
    val lat: Double,
    val lng: Double
) : Serializable

data class Writer(
    val deviceID: String,
    val id: Int,
    val picture: String,
    val username: String
) : Serializable

data class PostImage(
    val image: String,
    val post: Int
) : Serializable

class LikeResponse : ArrayList<Like>()

data class Like(
    val liker: Int,
    val post: Int
) : Serializable
