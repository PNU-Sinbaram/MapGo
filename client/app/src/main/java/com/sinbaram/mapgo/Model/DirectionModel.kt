package com.sinbaram.mapgo.Model

import com.squareup.moshi.Json

data class DirectionModel(
    @Json(name = "route") val route: TrackOption,
    @Json(name = "message") val message: String,
    @Json(name = "code") val code: Int,
)

data class TrackOption(
    @Json(name = "traoptimal") val traOptimal: List<DirectionPath>
)

data class DirectionPath(
    @Json(name = "summary") val summary : DirectionDistance,
    @Json(name = "path") val path : List<List<Double>>
)

data class DirectionDistance(
    @Json(name = "distance") val distance: Int
)
