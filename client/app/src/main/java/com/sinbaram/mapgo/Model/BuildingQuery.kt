package com.sinbaram.mapgo.Model

import com.squareup.moshi.Json

/** Naver openapi building search query model */
data class BuildingQuery(
    @Json(name = "display") var displayCount: Int,
    var items: List<BuildingModel>
)
