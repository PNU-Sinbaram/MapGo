package com.sinbaram.mapgo.Model

/** MapGo server api checkin request model */
data class CheckInRequest(
    var User_ID: String,
    var lat: Float,
    var long: Float
)
