package com.sinbaram.mapgo.Model

import com.squareup.moshi.Json

/** Naver building search api model */
data class BuildingModel(
    @Json(name = "display") var displayCount: Int,
    var items: List<BuildingItem>
)

/** Naver openapi building search query model */
data class BuildingItem(
    @Json(name = "title") var name: String,
    @Json(name = "link") var linkUrl: String,
    @Json(name = "category") var category: String,
    @Json(name = "telephone") var telephone: String,
    @Json(name = "address") var address: String,
    @Json(name = "roadAddress") var roadAddress: String,
    @Json(name = "mapx") var mapX: Int,
    @Json(name = "mapy") var mapY: Int,
)
