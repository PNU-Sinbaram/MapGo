package com.sinbaram.mapgo

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.ar.core.*
import com.google.ar.core.exceptions.*
import com.sinbaram.mapgo.AR.Helper.CameraPermissionHelper
import com.sinbaram.mapgo.AR.Helper.FrameTimeHelper
import com.sinbaram.mapgo.AR.Helper.SnackbarHelper
import com.sinbaram.mapgo.AR.Renderer.CpuImageRenderer
import com.sinbaram.mapgo.databinding.ActivityMapgoBinding
import java.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MapGoActivity : AppCompatActivity(), GLSurfaceView.Renderer {
    val TAG = MapGoActivity::class.java.simpleName

    lateinit var mBinding : ActivityMapgoBinding
    lateinit var mConfig : Config
    lateinit var mSurfaceView : GLSurfaceView

    lateinit var mCpuResolution : CameraConfig
    lateinit var mLowCameraConfig : CameraConfig
    lateinit var mMediumCameraConfig : CameraConfig
    lateinit var mHighCameraConfig : CameraConfig

    val renderFrameTimeHelper : FrameTimeHelper = FrameTimeHelper()
    val cpuImageFrameTimeHelper : FrameTimeHelper = FrameTimeHelper()
    val mMessageSnackbarHelper : SnackbarHelper = SnackbarHelper()
    var mUserRequestedInstall = false

    var mSession : Session? = null

    enum class ImageResolution {
        LOW_RESOLUTION,
        MEDIUM_RESOLUTION,
        HIGH_RESOLUTION
    }

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

        setContentView(R.layout.activity_mapgo)
    }

    override fun onResume() {
        super.onResume()

        if (mSession == null) {
            var exception : Exception? = null
            var message : String? = null

            try {
                when(ArCoreApk.getInstance().requestInstall(this, !mUserRequestedInstall)) {
                    ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
                        mUserRequestedInstall = true
                        return
                    }
                }

                // ARCore requires camera permission to operate.
                if (!CameraPermissionHelper.hasCameraPermission(this)) {
                    CameraPermissionHelper.requestCameraPermission(this)
                    return
                }

                mSession = Session(this)
                mConfig = Config(mSession)
            }
            catch(e : UnavailableArcoreNotInstalledException) {
                message = "Please install ARCore"
                exception = e
            }
            catch(e : UnavailableApkTooOldException) {
                message = "Please Update ARCore"
                exception = e
            }
            catch(e : UnavailableSdkTooOldException) {
                message = "Please Update this application"
                exception = e
            }
            catch(e : Exception) {
                message = "This device does not support AR"
                exception = e
            }

            if (message != null) {
                mMessageSnackbarHelper.showError(this, message)
                Log.e(TAG, "Exception creating session", exception)
                return
            }
        }

        obtainCameraConfigs()

        try {
            mSession!!.resume()
        }
        catch (e : CameraNotAvailableException) {
            mMessageSnackbarHelper.showError(this, "Camera not available. Try restarting the application")
            mSession = null
            return
        }
        mSurfaceView.onResume()
    }

    override fun onPause() {
        super.onPause()
        if (mSession != null) {
            mSurfaceView.onPause()
            mSession!!.pause()
        }
    }

    // Obtain the supported camera configs and build the list of radio button one for each camera config
    private fun obtainCameraConfigs() {
        // First obtain the session handle before getting the camera config
        if (mSession != null) {
            // Create filter here with desired fps filters
            var cameraConfigFilter : CameraConfigFilter =
                CameraConfigFilter(mSession)
                    .setTargetFps(EnumSet.of(CameraConfig.TargetFps.TARGET_FPS_30, CameraConfig.TargetFps.TARGET_FPS_60))

            var cameraConfigs : List<CameraConfig> = mSession!!.getSupportedCameraConfigs(cameraConfigFilter)

            mLowCameraConfig = getCameraConfigWithSelectedResolution(cameraConfigs,
                ImageResolution.LOW_RESOLUTION
            )
            mMediumCameraConfig = getCameraConfigWithSelectedResolution(cameraConfigs,
                ImageResolution.MEDIUM_RESOLUTION
            )
            mHighCameraConfig = getCameraConfigWithSelectedResolution(cameraConfigs,
                ImageResolution.HIGH_RESOLUTION
            )

            // Default camera config
            mCpuResolution = mMediumCameraConfig
        }
    }

    // Get the camera config with selected resolution
    private fun getCameraConfigWithSelectedResolution(cameraConfigs: List<CameraConfig>, resolution: MapGoActivity.ImageResolution): CameraConfig {
        // Take the first three camera configs, if camera configs size are larger than 3
        var cameraConfigsByResolution : List<CameraConfig> = ArrayList<CameraConfig>(
            cameraConfigs.subList(0, Math.min(cameraConfigs.size, 3))
        )
        Collections.sort(cameraConfigsByResolution, {
            p1 : CameraConfig, p2 : CameraConfig ->
            Integer.compare(p1.imageSize.height, p2.imageSize.height)
        })
        var cameraConfig : CameraConfig = cameraConfigsByResolution.get(0)
        when(resolution) {
            ImageResolution.LOW_RESOLUTION -> {
                cameraConfig = cameraConfigsByResolution.get(0)
            }
            ImageResolution.MEDIUM_RESOLUTION -> {
                cameraConfig = cameraConfigsByResolution.get(1)
            }
            ImageResolution.HIGH_RESOLUTION -> {
                cameraConfig = cameraConfigsByResolution.get(2)
            }
        }
        return cameraConfig
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