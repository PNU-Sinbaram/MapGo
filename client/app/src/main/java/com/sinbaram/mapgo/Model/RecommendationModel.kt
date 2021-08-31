package com.sinbaram.mapgo.Model

import com.squareup.moshi.Json

/** mapgo server api recommendation response model */
data class RecommendationModel(
    @Json(name = "name") var name: String,
    @Json(name = "longitude") var long: Float,
    @Json(name = "lat") var latitude: Float,
    @Json(name = "filtering") var filtering: Int,
)
