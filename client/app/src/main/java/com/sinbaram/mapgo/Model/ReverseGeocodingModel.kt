package com.sinbaram.mapgo.Model

import com.squareup.moshi.Json

/** Naver building search api model */
data class ReverseGeocodingModel(
    @Json(name = "name") var name: String,
    @Json(name = "code") var code: RgCode,
    @Json(name = "region") var region: RgRegion,
    @Json(name = "land") var land: RgLand,
    @Json(name = "addition0") var addition0: RgAddition,
    @Json(name = "addition1") var addition1: RgAddition,
    @Json(name = "addition2") var addition2: RgAddition,
    @Json(name = "addition3") var addition3: RgAddition,
    @Json(name = "addition4") var addition4: RgAddition
)

data class RgCode(
    @Json(name = "id") var id: String,
    @Json(name = "type") var type: String,
    @Json(name = "mappingId") var mappingId: String
)

data class RgRegion (
    @Json(name = "area0") var area0: RgArea,
    @Json(name = "area1") var area1: RgArea,
    @Json(name = "area2") var area2: RgArea,
    @Json(name = "area3") var area3: RgArea,
    @Json(name = "area4") var area4: RgArea
)

data class RgArea (
    @Json(name = "name") var name: String,
    @Json(name = "coords") var coords: RgCoords
)

data class RgCoords (
    @Json(name = "center") var center: RgCenter
)

data class RgCenter (
    @Json(name = "crs") var crs: String,
    @Json(name = "x") var x: Float,
    @Json(name = "y") var y: Float
)

data class RgLand (
    @Json(name = "type") var type: String,
    @Json(name = "name") var name: String,
    @Json(name = "number1") var number1: String,
    @Json(name = "number2") var number2: String,
    @Json(name = "coords") var coords: RgCoords,
)

data class RgAddition (
    @Json(name = "type") var type: String,
    @Json(name = "value") var value: String,
)
