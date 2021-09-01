package com.sinbaram.mapgo

import android.graphics.Path
import android.graphics.PointF
import android.location.Location
import android.util.Log
import androidx.fragment.app.FragmentManager
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.util.FusedLocationSource
import com.sinbaram.mapgo.Model.RecommendationModel
import kotlin.reflect.KFunction1

class MapWrapper(
    locationSource: FusedLocationSource,
    fragmentManager: FragmentManager,
    callback: KFunction1<MutableList<Symbol>, Unit>
)
    : OnMapReadyCallback {

    // Naver Map
    private lateinit var mNaverMap: NaverMap
    private lateinit var mCurrentLocation: Location
    private var mLocationSource: FusedLocationSource
    private var mCameraFirstMove = true

    // Rendering callback
    private var mRenderCallback: KFunction1<MutableList<Symbol>, Unit>

    companion object {
        val TAG: String = MapWrapper::class.java.simpleName

        @Volatile private var instance: MapWrapper? = null
        @JvmStatic fun getInstance(): MapWrapper {
            return instance!!
        }
    }

    init {
        // Pass naverMap FragmentLayout to naver maps sdk and connect listener
        val mapFragment = fragmentManager.findFragmentById(R.id.naverMap) as MapFragment?
            ?: MapFragment.newInstance(
                NaverMapOptions()
                    .scaleBarEnabled(false)
                    .rotateGesturesEnabled(false)
                    .scrollGesturesEnabled(true)
                    .zoomControlEnabled(true)
                    .zoomGesturesEnabled(true)
                    .stopGesturesEnabled(false)
                    .tiltGesturesEnabled(false)
                    .compassEnabled(true)
            ).also {
                fragmentManager.beginTransaction().add(R.id.naverMap, it).commit()
            }

        // Set location source
        mLocationSource = locationSource

        // Set Rendering callback
        mRenderCallback = callback

        // Synchronize naver map with this class
        mapFragment.getMapAsync(this)

        // Set this object to singleton instance
        instance = this
    }

    override fun onMapReady(naverMap: NaverMap) {
        // If naver map is ready, assign in to member variable
        this.mNaverMap = naverMap
        naverMap.locationSource = mLocationSource
        naverMap.locationTrackingMode = LocationTrackingMode.Face

        // Add gps location tracking listener
        naverMap.addOnLocationChangeListener {
            // Location information tracking here
            mCurrentLocation = it
            // Set camera location and tracking mode only once
            if (mCameraFirstMove) {
                mCameraFirstMove = false
                // Move camera to there
                naverMap.moveCamera(CameraUpdate.scrollTo(LatLng(it)))
                naverMap.locationTrackingMode = LocationTrackingMode.Face
                // Print camera location for once for debugging
                Log.d(MapGoActivity.TAG, String.format("Symbol(User) location %f, %f", it.latitude, it.longitude))
            } else {
                // Search nearby places
                collectNearbySymbols(mCurrentLocation)
            }
        }
    }

    fun collectNearbySymbols(location: Location) {
        // Transform given location information to screen position
        val cameraPos: PointF = mNaverMap.projection.toScreenLocation(LatLng(location))

        // Query nearby symbols
        val symbols = mutableListOf<Symbol>()
        mNaverMap.pickAll(cameraPos, MapGoActivity.NEARBY_RADIUS_MAP_COORD).forEach {
            when (it) {
                is Symbol -> symbols.add(it)
            }
        }

        // Invoke callback on Symbols
        mRenderCallback(symbols)
    }

    fun getCurrentLocation(): Location {
        return mCurrentLocation
    }

    fun setMarkerOnRecommendation(recommends: List<RecommendationModel>) {
        recommends.forEach {
            val marker: Marker = Marker()
            marker.position = LatLng(it.lat.toDouble(), it.long.toDouble())
            marker.map = mNaverMap
        }
    }

    fun addPathOverlay(paths: List<LatLng>) {
        val path = PathOverlay()
        path.coords = paths
        path.map = mNaverMap
    }
}
