package com.sinbaram.mapgo.Model

import com.squareup.moshi.Json

/** Naver building search api model */
data class GeocodingModel(
    @Json(name = "status") var status: String,
    @Json(name = "meta") var meta: GcMeta,
    @Json(name = "addresses") var addresses: List<GcAddress>
)

data class GcMeta(
    @Json(name = "totalCount") var totalCount: Number,
    @Json(name = "page") var page: Number,
    @Json(name = "count") var count: Number
)

data class GcAddress(
    @Json(name = "roadAddress") var roadAddress: String,
    @Json(name = "jibunAddress") var jibunAddress: String,
    @Json(name = "englishAddress") var englishAddress: String,
    @Json(name = "x") var x: String,
    @Json(name = "y") var y: String,
    @Json(name = "distance") var distance: Double,
    @Json(name = "addressElements") var addressElements: List<GcAddressElement>
)

data class GcAddressElement(
    @Json(name = "types") var types: List<String>,
    @Json(name = "longName") var longName: String,
    @Json(name = "shortName") var shortName: String,
    @Json(name = "code") var code: String
)
