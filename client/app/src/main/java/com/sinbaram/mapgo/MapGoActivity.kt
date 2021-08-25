package com.sinbaram.mapgo

import android.content.Intent
import android.graphics.PointF
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.MenuInflater
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentOnAttachListener
import com.google.ar.sceneform.math.Vector3
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.NaverMapOptions
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.Symbol
import com.naver.maps.map.util.FusedLocationSource
import com.sinbaram.mapgo.AR.Helper.SnackbarHelper
import com.sinbaram.mapgo.AR.Helper.TransformHelper
import com.sinbaram.mapgo.Model.SymbolRenderable
import com.sinbaram.mapgo.databinding.ActivityMapgoBinding
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Main activity of this application
 * This activity collect symbols from naver map sdk and
 * rendering them using SceneForm SDK
 */
class MapGoActivity :
    AppCompatActivity(),
    FragmentOnAttachListener,
    SensorEventListener,
    OnMapReadyCallback {
    // Activity binding variable
    private lateinit var mBinding: ActivityMapgoBinding

    // Sensor related stuffs
    private lateinit var mSensorManager: SensorManager
    private val mAccelerometerReading = FloatArray(3)
    private val mMagnetometerReading = FloatArray(3)
    private val mRotationMatrix = FloatArray(9)
    private val mOrientationAngles = FloatArray(3)

    // Location
    private lateinit var mLocationSource: FusedLocationSource
    private lateinit var mNaverMap: NaverMap
    private lateinit var mCurrentLocation: Location
    private var mCameraFirstMove = true

    // Snackbar message popping helper
    private val mMessageSnackbarHelper: SnackbarHelper = SnackbarHelper()

    // ARCore Renderer class
    private lateinit var mRenderer: Renderer
    private var mSymbolNodes: List<SymbolRenderable> = mutableListOf()

    companion object {
        val TAG: String = MapGoActivity::class.java.simpleName
        const val LOCATION_PERMISSION_REQUEST_CODE = 1000
        const val NEARBY_RADIUS_MAP_COORD = 300
        const val NEARBY_RADIUS_WORLD_COORD = 3.0f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show License text
        mMessageSnackbarHelper.showMessageWithDismiss(
            this,
            "This application runs on Google Play Services for AR (ARCore), " +
                "which is provided by Google LLC and governed by the Google Privacy Policy"
        )

        // Binding mapgo activity layout
        mBinding = ActivityMapgoBinding.inflate(layoutInflater)

        supportFragmentManager.addFragmentOnAttachListener(this)

        mRenderer = Renderer(applicationContext, supportFragmentManager)

        // Pass naverMap FragmentLayout to naver maps sdk and connect listener
        val mapFragment = supportFragmentManager.findFragmentById(R.id.naverMap) as MapFragment?
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
                supportFragmentManager.beginTransaction().add(R.id.naverMap, it).commit()
            }
        mapFragment.getMapAsync(this)

        // Set Sensor manager of this device
        mSensorManager = getSystemService()!!

        // Set location source
        mLocationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        // Pass activity binding root
        setContentView(mBinding.root)
    }

    override fun onMapReady(naverMap: NaverMap) {
        // If naver map is ready, assign in to member variable
        this.mNaverMap = naverMap
        naverMap.locationSource = mLocationSource
        naverMap.locationTrackingMode = LocationTrackingMode.Face

        // Add gps location tracking listener
        naverMap.addOnLocationChangeListener {
            Toast.makeText(
                this, "${it.latitude}, ${it.longitude}",
                Toast.LENGTH_SHORT
            ).show()
            // Location information tracking here
            mCurrentLocation = it
            // Set camera location and tracking mode only once
            if (mCameraFirstMove) {
                mCameraFirstMove = false
                // Move camera to there
                naverMap.moveCamera(CameraUpdate.scrollTo(LatLng(it)))
                naverMap.locationTrackingMode = LocationTrackingMode.Face
                // Print camera location for once for debugging
                Log.d(TAG, String.format("Symbol(User) location %f, %f", it.latitude, it.longitude))
            } else {
                // Search nearby places
                renderNearbySymbols(mCurrentLocation)
            }
        }
    }

    /**
     * @param location : center location of nearby radius
     * rendering nearby symbols with given location
     */
    private fun renderNearbySymbols(location: Location) {
        // Transform given location information to screen position
        val cameraPos: PointF = mNaverMap.projection.toScreenLocation(LatLng(location))

        // Query nearby symbols
        val symbols = mutableListOf<Symbol>()
        mNaverMap.pickAll(cameraPos, NEARBY_RADIUS_MAP_COORD).forEach {
            when (it) {
                is Symbol -> symbols.add(it)
            }
        }

        // Add symbols with null renderable
        val symbolNodes = mutableListOf<SymbolRenderable>()
        symbols.forEach { symbol: Symbol ->
            symbolNodes.add(SymbolRenderable(symbol, null))
        }

        val rootScene = mRenderer.getScene()

        // Add new collected nodes to render list
        symbolNodes.subtract(mSymbolNodes).forEach {
            // Calculate degree between current location to symbol location
            val targetLocation = Location("")
            targetLocation.latitude = it.symbol.position.latitude
            targetLocation.longitude = it.symbol.position.longitude
            val degree = (360 - ((TransformHelper.calculateBearing(mCurrentLocation, targetLocation) + 360) % 360))

            // Transform euler degree to world coordinates
            val y = 0.0f
            val x =
                (NEARBY_RADIUS_WORLD_COORD * cos(PI * degree / 180)).toFloat()
            val z = (-1 * NEARBY_RADIUS_WORLD_COORD * sin(PI * degree / 180)).toFloat()

            // Print Symbol informations for debugging purpose
            Log.d(
                TAG,
                String.format(
                    "Symbol(%s), Location(%f, %f), WorldCoord(%f, %f, %f), degree(%f)",
                    it.symbol.caption, it.symbol.position.latitude, it.symbol.position.longitude,
                    x, y, z, degree
                )
            )

            // Add new renderable node
            it.anchor = mRenderer.createAnchor(Vector3(x, y, z))
            // Create image symbol and attach to anchor
            mRenderer.createImageSymbol(Vector3(0.0f, 1.0f, 0.0f), it.symbol.caption)
                .setParent(it.anchor)
            rootScene.addChild(it.anchor)
        }

        // Remove collected nodes outside radius
        mSymbolNodes.subtract(symbolNodes).forEach {
            if (it.anchor != null)
                rootScene.removeChild(it.anchor!!)
        }

        // Swap old node list to new one
        mSymbolNodes = symbolNodes
    }

    override fun onResume() {
        super.onResume()
        // Connect magnetic field sensor (나침반)
        mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also {
            mSensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
        // Connect accelerometer sensor
        mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also {
            mSensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (
            mLocationSource.onRequestPermissionsResult(
                requestCode, permissions, grantResults
            )
        ) {
            if (!mLocationSource.isActivated) { // 권한 거부됨
                mNaverMap.locationTrackingMode = LocationTrackingMode.None
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) {
            return
        }
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, mAccelerometerReading, 0, mAccelerometerReading.size)
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, mMagnetometerReading, 0, mMagnetometerReading.size)
        }

        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(
            mRotationMatrix,
            null,
            mAccelerometerReading,
            mMagnetometerReading
        )
        SensorManager.getOrientation(mRotationMatrix, mOrientationAngles)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do nothing here
    }

    override fun onAttachFragment(fragmentManager: FragmentManager, fragment: Fragment) {
        if (fragment.id == mBinding.arFragment.id) {
            mRenderer.attachFragment(fragment)
        }
    }

    fun showPopup(v : View){
        val popup = PopupMenu(this, v)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.mapgo_menu, popup.menu)
        popup.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId){
                R.id.menu_new_feed-> {

                }
                R.id.menu_keyword-> {

                }
            }
            true
        }
        popup.show()
    }
}
