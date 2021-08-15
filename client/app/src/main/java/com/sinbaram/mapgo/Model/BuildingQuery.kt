package com.sinbaram.mapgo.Model

import com.squareup.moshi.Json

data class BuildingQuery(
    @Json(name = "display") var displayCount: Int,
    var items: List<BuildingModel>
)
