package com.sinbaram.mapgo.Model

import com.squareup.moshi.Json

/** Mapgo server api checkin response model */
data class CheckInResponse(
    @Json(name = "User_ID") var userID: String,
    @Json(name = "lat") var latitude: String,
    @Json(name = "long") var longitude: String,
    @Json(name = "timeStamp") var timeStamp: String
)
