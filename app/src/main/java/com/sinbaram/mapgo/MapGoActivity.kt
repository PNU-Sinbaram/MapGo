package com.sinbaram.mapgo

import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.ar.core.*
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import com.sinbaram.mapgo.AR.Helper.CameraPermissionHelper
import com.sinbaram.mapgo.AR.Helper.FrameTimeHelper
import com.sinbaram.mapgo.databinding.ActivityMapgoBinding
import java.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MapGoActivity : AppCompatActivity(), GLSurfaceView.Renderer {
    lateinit var mBinding : ActivityMapgoBinding
    lateinit var mSession : Session
    lateinit var mSurfaceView : GLSurfaceView

    var renderFrameTimeHelper : FrameTimeHelper = FrameTimeHelper()
    var cpuImageFrameTimeHelper : FrameTimeHelper = FrameTimeHelper()

    var mUserRequestedInstall = true
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //! Binding mapgo activity layout
        mBinding = ActivityMapgoBinding.inflate(layoutInflater)

        //! Bind each view to member variables
        mSurfaceView = mBinding.surfaceView

        //! Setup renderer
        mSurfaceView.preserveEGLContextOnPause = true
        mSurfaceView.setEGLContextClientVersion(2)
        mSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        mSurfaceView.setRenderer(this)
        mSurfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        mSurfaceView.setWillNotDraw(false)

        lifecycle.addObserver(renderFrameTimeHelper)
        lifecycle.addObserver(cpuImageFrameTimeHelper)

        if (!CheckARSupport())
            Toast.makeText(this, "Cannot find AR Core module in this android device", Toast.LENGTH_LONG)

        setContentView(R.layout.activity_mapgo)
    }

    override fun onResume() {
        super.onResume()

        // ARCore requires camera permission to operate.
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            CameraPermissionHelper.requestCameraPermission(this)
            return
        }

        // Ensure that Google Play Services for AR and ARCore device profile data are
        // installed and up to date.
        try {
            if (mSession == null) {
                when (ArCoreApk.getInstance().requestInstall(this, mUserRequestedInstall)) {
                    ArCoreApk.InstallStatus.INSTALLED -> {
                        // Success: Safe to create the AR session.
                        mSession = Session(this)

                        // Create a session config.
                        val config = Config(mSession)

                        // Do feature-specific operations here, such as enabling depth or turning on
                        // support for Augmented Faces.

                        // Configure the session.
                        mSession!!.configure(config)
                    }
                    ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
                        // When this method returns `INSTALL_REQUESTED`:
                        // 1. ARCore pauses this activity.
                        // 2. ARCore prompts the user to install or update Google Play
                        //    Services for AR (market://details?id=com.google.ar.core).
                        // 3. ARCore downloads the latest device profile data.
                        // 4. ARCore resumes this activity. The next invocation of
                        //    requestInstall() will either return `INSTALLED` or throw an
                        //    exception if the installation or update did not succeed.
                        mUserRequestedInstall = false
                        return
                    }
                }
            }
        } catch (e: UnavailableUserDeclinedInstallationException) {
            // Display an appropriate message to the user and return gracefully.
            Toast.makeText(this, "TODO: handle exception " + e, Toast.LENGTH_LONG)
                .show()
            return
        }

        // Create a camera config filter for the session.
        val filter = CameraConfigFilter(mSession)

        // Return only camera configs that target 30 fps camera capture frame rate.
        filter.targetFps = EnumSet.of(CameraConfig.TargetFps.TARGET_FPS_30)

        // Return only camera configs that will not use the depth sensor.
        filter.depthSensorUsage = EnumSet.of(CameraConfig.DepthSensorUsage.DO_NOT_USE)

        // Get list of configs that match filter settings.
        // In this case, this list is guaranteed to contain at least one element,
        // because both TargetFps.TARGET_FPS_30 and DepthSensorUsage.DO_NOT_USE
        // are supported on all ARCore supported devices.
        val cameraConfigList = mSession!!.getSupportedCameraConfigs(filter)

        // Use element 0 from the list of returned camera configs. This is because
        // it contains the camera config that best matches the specified filter
        // settings.
        mSession!!.cameraConfig = cameraConfigList[0]
    }

    override fun onDestroy() {
        super.onDestroy()

        // Release native heap memory used by an ARCore session.
        mSession?.close()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        results: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, results)
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            Toast.makeText(this, "Camera permission is needed to run this application", Toast.LENGTH_LONG)
                .show()
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                CameraPermissionHelper.launchPermissionSettings(this)
            }
            finish()
        }
    }

    fun CheckARSupport() : Boolean {
        val availability = ArCoreApk.getInstance().checkAvailability(this)
        if (availability.isTransient) {
            // Continue to query availability at 5Hz while compatibility is checked in the background.
            Handler(Looper.getMainLooper()).postDelayed({
                CheckARSupport()
            }, 200)
        }
        return availability.isSupported
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        TODO("Not yet implemented")
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        TODO("Not yet implemented")
    }

    override fun onDrawFrame(gl: GL10?) {
        TODO("Not yet implemented")
    }
}