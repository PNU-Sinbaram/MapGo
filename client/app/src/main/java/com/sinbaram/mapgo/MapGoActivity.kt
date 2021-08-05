package com.sinbaram.mapgo

import android.graphics.PointF
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentOnAttachListener
import com.google.android.filament.ColorGrading
import com.google.ar.core.Config
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.Session
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.SceneView
import com.google.ar.sceneform.Sceneform
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.EngineInstance
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.BaseArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.NaverMapOptions
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.Symbol
import com.sinbaram.mapgo.AR.Helper.FrameTimeHelper
import com.sinbaram.mapgo.AR.Helper.SnackbarHelper
import com.sinbaram.mapgo.databinding.ActivityMapgoBinding
import java.lang.ref.WeakReference

class MapGoActivity :
    AppCompatActivity(),
    FragmentOnAttachListener,
    BaseArFragment.OnTapArPlaneListener,
    BaseArFragment.OnSessionConfigurationListener,
    ArFragment.OnViewCreatedListener,
    SensorEventListener,
    OnMapReadyCallback {
    // Activity binding variable
    lateinit var mBinding: ActivityMapgoBinding
    // Main object for sceneform sdk
    lateinit var mArFragment: ArFragment

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

    // For sample gltf rendering
    var mModel: Renderable? = null
    var mViewRenderable: ViewRenderable? = null

    // Snackbar message popping helper
    val mMessageSnackbarHelper: SnackbarHelper = SnackbarHelper()

    companion object {
        val TAG = MapGoActivity::class.java.simpleName
        val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show License text
        mMessageSnackbarHelper.showMessage(
            this,
            "This application runs on Google Play Services for AR (ARCore), " +
                "which is provided by Google LLC and governed by the Google Privacy Policy"
        )

        // Binding mapgo activity layout
        mBinding = ActivityMapgoBinding.inflate(layoutInflater)

        supportFragmentManager.addFragmentOnAttachListener(this)

        if (savedInstanceState == null) {
            if (Sceneform.isSupported(this)) {
                supportFragmentManager.beginTransaction()
                    .add(mBinding.arFragment.id, ArFragment::class.java, null)
                    .commit()
            }
        }

        // Load sample gltf model for test rendernig
        loadModel("https://github.com/KhronosGroup/glTF-Sample-Models/raw/master/2.0/CesiumMan/glTF-Binary/CesiumMan.glb")
        // Pass naverMap FragmentLayout to naver maps sdk and connect listener
        val mapFragment = supportFragmentManager.findFragmentById(R.id.naverMap) as MapFragment?
            ?: MapFragment.newInstance(
                NaverMapOptions()
                    .scaleBarEnabled(false)
                    .zoomControlEnabled(false)
                    .zoomGesturesEnabled(false)
                    .rotateGesturesEnabled(false)
                    .scrollGesturesEnabled(false)
                    .stopGesturesEnabled(false)
                    .tiltGesturesEnabled(false)
                    .compassEnabled(true)
                    .minZoom(17.0)
                    .maxZoom(17.0)
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

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        if (mLocationSource.onRequestPermissionsResult(requestCode, permissions,
                grantResults)) {
            if (!mLocationSource.isActivated) { // 권한 거부됨
                mNaverMap.locationTrackingMode = LocationTrackingMode.None
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onMapReady(naverMap: NaverMap) {
        // If naver map is ready, assign in to member variable
        this.mNaverMap = naverMap
        naverMap.locationSource = mLocationSource
        // Add gps location tracking listener
        naverMap.addOnLocationChangeListener {
            Toast.makeText(this, "${it.latitude}, ${it.longitude}",
                Toast.LENGTH_SHORT).show()
            // Location information tracking here
            mCurrentLocation = it
            // Move camera to there
            naverMap.moveCamera(CameraUpdate.scrollTo(LatLng(it)))
            // Search nearby places
            collectNearbySymbols(mCurrentLocation)
        }

        // Set naver map location tracking mode
        naverMap.locationTrackingMode = LocationTrackingMode.Face
        mLocationSource.isCompassEnabled = true
    }

    fun collectNearbySymbols(location: Location) {
        TODO("Collect nearby symbols and rendering will be implemented")
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

    }

    override fun onAttachFragment(fragmentManager: FragmentManager, fragment: Fragment) {
        if (fragment.id == mBinding.arFragment.id) {
            mArFragment = fragment as ArFragment
            mArFragment.setOnSessionConfigurationListener(this)
            mArFragment.setOnViewCreatedListener(this)
            mArFragment.setOnTapArPlaneListener(this)
        }
    }

    override fun onTapPlane(hitResult: HitResult?, plane: Plane?, motionEvent: MotionEvent?) {
        if (mModel == null) {
            mMessageSnackbarHelper.showMessage(this, "Loading..." + Toast.LENGTH_SHORT)
            return
        }

        // Create the Anchor.
        val anchor = hitResult!!.createAnchor()
        val anchorNode = AnchorNode(anchor)
        anchorNode.setParent(mArFragment.getArSceneView().getScene())

        // Create the transformable model and add it to the anchor.
        val model = TransformableNode(mArFragment.getTransformationSystem())
        model.setParent(anchorNode)
        model.setRenderable(mModel)
            .animate(true).start()
        model.select()

        val titleNode = Node()
        titleNode.setParent(model)
        titleNode.isEnabled = false
        titleNode.localPosition = Vector3(0.0f, 1.0f, 0.0f)
        titleNode.renderable = mViewRenderable
        titleNode.isEnabled = true
    }

    override fun onSessionConfiguration(session: Session?, config: Config?) {
        if (session!!.isDepthModeSupported(Config.DepthMode.AUTOMATIC))
            config!!.setDepthMode(Config.DepthMode.AUTOMATIC)
    }

    override fun onViewCreated(arFragment: ArFragment?, arSceneView: ArSceneView?) {
        // Currently, the tone-mapping should be changed to FILMIC
        // because with other tone-mapping operators except LINEAR
        // the inverseTonemapSRGB function in the materials can produce incorrect results.
        // The LINEAR tone-mapping cannot be used together with the inverseTonemapSRGB function.
        val renderer = arSceneView!!.renderer

        if (renderer != null) {
            renderer.filamentView.colorGrading = ColorGrading.Builder()
                .toneMapping(ColorGrading.ToneMapping.FILMIC)
                .build(EngineInstance.getEngine().filamentEngine)
        }

        // Fine adjust the maximum frame rate
        arSceneView.setFrameRateFactor(SceneView.FrameRate.FULL)
    }

    fun loadModel(modelUrl: String) {
        val weakActivity: WeakReference<MapGoActivity> = WeakReference(this)
        ModelRenderable.builder()
            .setSource(
                this,
                Uri.parse(modelUrl)
            )
            .setIsFilamentGltf(true)
            .setAsyncLoadEnabled(true)
            .build()
            .thenAccept { model: ModelRenderable ->
                val activity: MapGoActivity? = weakActivity.get()
                if (activity != null) {
                    activity.mModel = model
                }
            }
            .exceptionally { throwable: Throwable? ->
                Toast.makeText(
                    this, "Unable to load model", Toast.LENGTH_LONG
                ).show()
                null
            }
    }
}
