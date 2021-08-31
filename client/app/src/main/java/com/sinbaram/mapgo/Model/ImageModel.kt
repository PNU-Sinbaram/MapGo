package com.sinbaram.mapgo.Model

import com.squareup.moshi.Json

/** Naver openapi image search model */
data class ImageModel(
    @Json(name = "display") var displayCount: Int,
    var items: List<ImageItem>
)

/** naver openapi image search query model */
data class ImageItem(
    @Json(name = "title") var name: String,
    @Json(name = "link") var linkUrl: String,
    @Json(name = "thumbnail") var thumbnail: String,
    @Json(name = "sizeheight") var sizeheight: String,
    @Json(name = "sizewidth") var sizewidth: String
)
