package com.sinbaram.mapgo.Model

import com.squareup.moshi.Json

data class Recommendation(
    @Json(name = "name") var name: String,
    @Json(name = "longitude") var long: Float,
    @Json(name = "lat") var latitude: Float,
    @Json(name = "filtering") var filtering: Int,
)
