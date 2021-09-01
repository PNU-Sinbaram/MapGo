package com.sinbaram.mapgo

import android.app.Activity
import android.content.Intent
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentOnAttachListener
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.math.Vector3
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.Symbol
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import com.sinbaram.mapgo.API.ServerAPI
import com.sinbaram.mapgo.API.ServerClient
import com.sinbaram.mapgo.AR.Helper.SnackbarHelper
import com.sinbaram.mapgo.AR.Helper.MathUtilHelper
import com.sinbaram.mapgo.Model.DirectionModel
import com.sinbaram.mapgo.Model.ProfileModel
import com.sinbaram.mapgo.Model.RecommendationModel
import com.sinbaram.mapgo.Model.SymbolRenderable
import com.sinbaram.mapgo.databinding.ActivityMapgoBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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
    SensorEventListener {
    // Activity binding variable
    private lateinit var mBinding: ActivityMapgoBinding

    // Sensor related stuffs
    private lateinit var mSensorManager: SensorManager
    private val mAccelerometerReading = FloatArray(3)
    private val mMagnetometerReading = FloatArray(3)
    private val mRotationMatrix = FloatArray(9)
    private val mOrientationAngles = FloatArray(3)

    // Snackbar message popping helper
    private val mMessageSnackbarHelper: SnackbarHelper = SnackbarHelper()

    // com.sinbaram.mapgo.Map class
    private lateinit var mMap: MapWrapper
    private lateinit var mLocationSource: FusedLocationSource

    // ARCore Renderer class
    private lateinit var mRenderer: Renderer
    private var mSymbolNodes: List<SymbolRenderable> = mutableListOf()

    // Profile data
    private var mProfile: ProfileModel = ProfileModel()

    // Navigator
    private lateinit var mNavigator: Navigator

    companion object {
        val TAG: String = MapGoActivity::class.java.simpleName
        const val LOCATION_PERMISSION_REQUEST_CODE = 1000
        const val NEARBY_RADIUS_MAP_COORD = 300
        const val NEARBY_RADIUS_WORLD_COORD = 3.0f
        const val PROFILE_ACTIVITY_CODE = 1234
        const val NEWFEED_ACTIVITY_CODE = 4567
        const val RECOMMEND_ACTIVITY_CODE = 1357
        const val NAVIGATION_ACTIVITY_CODE = 2468
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Binding mapgo activity layout
        mBinding = ActivityMapgoBinding.inflate(layoutInflater)

        // Show License text
        mMessageSnackbarHelper.setParentView(mBinding.naverMap)
        mMessageSnackbarHelper.showMessageWithDismiss(
            this,
            "This application runs on Google Play Services for AR (ARCore), " +
                "which is provided by Google LLC and governed by the Google Privacy Policy"
        )

        supportFragmentManager.addFragmentOnAttachListener(this)

        // Setup Renderer
        mRenderer = Renderer(
            applicationContext,
            supportFragmentManager,
            this::onUpdate
        )

        // Set Sensor manager of this device
        mSensorManager = getSystemService()!!

        // Setup Naver Map Wrapper
        mLocationSource = FusedLocationSource(this, MapGoActivity.LOCATION_PERMISSION_REQUEST_CODE)
        mMap = MapWrapper(
            mLocationSource,
            supportFragmentManager,
            this::renderNearby
        )

        // Pass processDirection navigator
        mNavigator = Navigator(this::processDirection)

        // Set menu button listener
        mBinding.menuButton.setOnClickListener {
            showPopup(it)
        }

        // Set profile button listener
        mBinding.profileButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("Profile", mProfile)
            startActivityForResult(intent, PROFILE_ACTIVITY_CODE)
        }

        // Pass activity binding root
        setContentView(mBinding.root)
    }

    /** Update the application in given frame */
    private fun onUpdate(frametime: FrameTime) {
        if (!mMap.checkMapInitialized())
            return

        // Set direction for living node
        mSymbolNodes.forEach {
            if (it.anchor != null) {
                // Calculate degree between current location to symbol location
                val targetLocation = Location("")
                targetLocation.latitude = it.symbol.position.latitude
                targetLocation.longitude = it.symbol.position.longitude
                val degree = (360 - ((MathUtilHelper.calculateBearing(mMap.getCurrentLocation(), targetLocation) + 360) % 360))

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
                mRenderer.renewAnchorTransform(it.anchor!!, Vector3(x, y, z))
            }
        }
    }

    /**
     * @param location : center location of nearby radius
     * rendering nearby symbols with given location
     */
    private fun renderNearby(symbols: MutableList<Symbol>, markers: MutableList<Marker>) {
        // Add symbols with null renderable
        val symbolNodes = mutableListOf<SymbolRenderable>()
        symbols.forEach { symbol: Symbol ->
            symbolNodes.add(SymbolRenderable(symbol, null))
        }

        val rootScene = mRenderer.getScene()

        // Add new collected nodes to render list
        symbolNodes.subtract(mSymbolNodes).forEach {
            // Add new renderable node
            it.anchor = mRenderer.createAnchor()
            // Create image symbol and attach to anchor
            mRenderer.createImageSymbol(Vector3(0.0f, 1.0f, 0.0f), it.symbol.caption)
                .setParent(it.anchor)
            mRenderer.createTextSymbol(Vector3(0.0f, 0.5f, 0.0f), it.symbol.caption)
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

    /** Get recommendations with keywords from mapgo server */
    fun getRecommendations(keywords: String) {
        val loc = mMap.getCurrentLocation()
        val serverAPI = ServerAPI.GetClient()!!.create(ServerClient::class.java)
        val apiCall : Call<List<RecommendationModel>> = serverAPI.GetRecommendations(
            mProfile.nickname!!,
            loc.latitude.toFloat(),
            loc.longitude.toFloat(),
            50,
            keywords
        )
        apiCall.enqueue(object: Callback<List<RecommendationModel>> {
            override fun onResponse(call: Call<List<RecommendationModel>>, response: Response<List<RecommendationModel>>) {
                if (response.code() == 200) {
                    mMap.setMarkerOnRecommendation(response.body()!!)
                }
            }
            override fun onFailure(call: Call<List<RecommendationModel>>, t: Throwable) {
                Log.d(TAG, "Cannot get recommendations")
            }
        })
    }

    /** Process navigation direction data model */
    fun processDirection(direction: DirectionModel) {
        if (direction.code != 0) {
            mMessageSnackbarHelper.showMessageWithDismiss(
                this,
                direction.message
            )
            return
        }

        val directionPath = direction.route.traOptimal[0]
        val paths: MutableList<LatLng> = mutableListOf()
        directionPath.path.forEach {
            paths.add(LatLng(it[1], it[0]))
        }
        mMap.addPathOverlay(paths)
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
        if (mLocationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            return;
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
                    val intent = Intent(this, NewFeedActivity::class.java).apply {
                        intent.putExtra("Profile", mProfile)
                    }
                    startActivityForResult(intent, NEWFEED_ACTIVITY_CODE)
                }
                R.id.menu_keyword-> {
                    val intent = Intent(this, RecommendActivity::class.java).apply {
                        intent.putExtra("Profile", mProfile)
                    }
                    startActivityForResult(intent, RECOMMEND_ACTIVITY_CODE)
                }
                R.id.menu_navigation-> {
                    val intent = Intent(this, NavigationActivity::class.java)
                    overridePendingTransition(0, 0)
                    startActivityForResult(intent, NAVIGATION_ACTIVITY_CODE)
                }
            }
            true
        }
        popup.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val extras: Bundle = data!!.extras!!
            when (requestCode) {
                PROFILE_ACTIVITY_CODE -> {
                    mProfile = extras["Profile"] as ProfileModel
                }
                NEWFEED_ACTIVITY_CODE -> {

                }
                RECOMMEND_ACTIVITY_CODE -> {
                    val keywords = extras["Keywords"] as String
                    getRecommendations(keywords)
                }
                NAVIGATION_ACTIVITY_CODE -> {
                    if (extras.containsKey("Destination"))
                        mNavigator.startNavigation(mMap.getCurrentLocation(), extras["Destination"] as String)
                }
            }
        }
    }
}
