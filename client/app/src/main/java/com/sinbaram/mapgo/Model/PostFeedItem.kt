package com.sinbaram.mapgo.Model

import java.io.Serializable

/** data class representing SNS Post
 * @property comment : List of dataclass Comment
 * @property contents : Content of sns post
 * @property like : List of dataclass Like which contains liker's userId
 * @property location : Location information which sns post contains
 * @property postId : id of sns post
 * @property postImage : List of sns post's images url
 * @property postTime : String of sns post's upload time
 * @property totalLikes : Number of person who liked this post
 * @property writer : Writer of sns post which contains writer's deviceId, userId, profile image as String, and username
 * */
data class PostFeedItem(
    val comment: List<Comment>,
    val contents: String,
    val like: List<Like>,
    val location: Location,
    val postID: Int,
    val postImage: List<PostImage>,
    val postTime: String,
    val totalLikes: Int,
    val writer: Writer
) : Serializable

/** data class representing single comment object */
data class Comment(
    val writer: Writer,
    val contents: String,
    val post: Int
) : Serializable

/** data class representing location information, latitude as [lat], longitude as [lng] */
data class Location(
    val lat: Double,
    val lng: Double
) : Serializable

/** data class representing writer of post */
data class Writer(
    val deviceID: String,
    val id: Int,
    val picture: String,
    val username: String
) : Serializable

/** data class representing single image object */
data class PostImage(
    val image: String,
    val post: Int
) : Serializable

/** data class representing liker's information */
data class Like(
    val liker: Int,
    val post: Int
) : Serializable
