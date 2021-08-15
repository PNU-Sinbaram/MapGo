package com.sinbaram.mapgo.Model

import com.squareup.moshi.Json

data class ImageModel (
    @Json(name = "title") var name: String,
    @Json(name = "link") var linkUrl: String,
    @Json(name = "thumbnail") var thumbnail: String,
    @Json(name = "sizeheight") var sizeheight: String,
    @Json(name = "sizewidth") var sizewidth: String
)
