package com.sinbaram.mapgo.Model

import com.squareup.moshi.Json

/** mapgo server api recommendation response model */
data class RecommendationModel(
    @Json(name = "name") var name: String,
    @Json(name = "lat") var lat: Float,
    @Json(name = "long") var long: Float,
    @Json(name = "filtering") var filtering: Int,
)
