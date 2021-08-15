package com.sinbaram.mapgo.Model

import com.squareup.moshi.Json

data class BuildingModel(
    @Json(name = "title") var name: String,
    @Json(name = "link") var linkUrl: String,
    @Json(name = "category") var category: String,
    @Json(name = "telephone") var telephone: String,
    @Json(name = "address") var address: String,
    @Json(name = "roadAddress") var roadAddress: String,
    @Json(name = "mapx") var mapX: Int,
    @Json(name = "mapy") var mapY: Int,
)
