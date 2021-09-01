package com.sinbaram.mapgo.AR.Helper

import android.location.Location
import com.naver.maps.geometry.LatLng
import java.lang.Math.toDegrees
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/** Collection of coordinates transformation utilities */
class MathUtilHelper {
    companion object {
        /** Calculate angle between two locations which share same up vector */
        fun calculateBearing(loc1: Location, loc2: Location): Double {
            val latA = loc1.latitude
            val lonA = loc1.longitude
            val latB = loc2.latitude
            val lonB = loc2.longitude

            val lon = lonB - lonA
            val y = sin(lon) * cos(latB)
            val x = cos(latA) * sin(latB) - sin(latA) * cos(latB) * cos(lon)
            return toDegrees(atan2(y, x))
        }

        /** */
        fun signedArea(pos1: LatLng, pos2: LatLng): Double {
            return pos1.longitude * pos2.latitude - pos1.latitude * pos2.longitude
        }
    }
}
