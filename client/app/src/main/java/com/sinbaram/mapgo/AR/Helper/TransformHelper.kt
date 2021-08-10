package com.sinbaram.mapgo.AR.Helper

import android.location.Location
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.ln
import kotlin.math.tan

/** Collection of coordinates transformation utilities */
class TransformHelper {
    companion object {
        /** Calculate angle between two locations which share same up vector */
        fun calculateBearing(loc1: Location, loc2: Location): Double {
            val latA = loc1.latitude * PI / 180
            val lonA = loc1.longitude * PI / 180
            val latB = loc2.latitude * PI / 180
            val lonB = loc2.longitude * PI / 180

            val deltaOmega = ln(tan((latB / 2) + (PI / 4)) / tan((latA / 2) + (PI / 4)))
            val deltaLongitude = abs(lonA - lonB)

            return atan2(deltaLongitude, deltaOmega)
        }
    }
}
