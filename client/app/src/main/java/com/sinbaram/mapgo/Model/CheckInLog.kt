package com.sinbaram.mapgo.Model

import com.squareup.moshi.Json

data class CheckInLog(
    @Json(name = "User_ID") var userID: String,
    @Json(name = "lat") var latitude: String,
    @Json(name = "long") var longitude: String,
    @Json(name = "timeStamp") var timeStamp: String
) {}
