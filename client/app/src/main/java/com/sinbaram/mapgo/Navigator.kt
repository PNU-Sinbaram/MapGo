package com.sinbaram.mapgo

import android.location.Location
import android.util.Log
import com.naver.maps.map.Symbol
import com.sinbaram.mapgo.API.DirectionClient
import com.sinbaram.mapgo.API.GeocodingClient
import com.sinbaram.mapgo.API.NaverAPI
import com.sinbaram.mapgo.Model.DirectionModel
import com.sinbaram.mapgo.Model.GeocodingModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.reflect.KFunction1

class Navigator(callback: KFunction1<DirectionModel, Unit>) {
    // navigation direction callback
    private var mNaviCallback: KFunction1<DirectionModel, Unit>

    init {
        mNaviCallback = callback
    }

    fun startNavigation(source: Location, destination: String) {
        val naverAPI = NaverAPI.GetClient()!!.create(GeocodingClient::class.java)
        val apiCall : Call<GeocodingModel> = naverAPI.GetGeocode(destination)
        apiCall.enqueue(object: Callback<GeocodingModel> {
            override fun onResponse(call: Call<GeocodingModel>, response: Response<GeocodingModel>) {
                if (response.code() == 200 && response.body()!!.addresses.isNotEmpty()) {
                    val address = response.body()!!.addresses[0]
                    val destLoc = Location("")
                    destLoc.longitude = address.x.toDouble()
                    destLoc.latitude = address.y.toDouble()
                    calculatePath(source, destLoc)
                }
            }
            override fun onFailure(call: Call<GeocodingModel>, t: Throwable) {
                Log.d(MapGoActivity.TAG, "Cannot get geocoding")
            }
        })
    }

    fun calculatePath(source: Location, destination: Location) {
        val naverAPI = NaverAPI.GetClient()!!.create(DirectionClient::class.java)
        val apiCall : Call<DirectionModel> = naverAPI.GetDirection(source.toString(), destination.toString())
        apiCall.enqueue(object: Callback<DirectionModel> {
            override fun onResponse(call: Call<DirectionModel>, response: Response<DirectionModel>) {
                if (response.code() == 200) {
                    mNaviCallback(response.body()!!)
                }
            }
            override fun onFailure(call: Call<DirectionModel>, t: Throwable) {
                Log.d(MapGoActivity.TAG, "Cannot get direction")
            }
        })
    }
}
