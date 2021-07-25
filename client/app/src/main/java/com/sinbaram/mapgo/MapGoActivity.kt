package com.sinbaram.mapgo

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.*
import com.google.ar.core.exceptions.*
import com.sinbaram.mapgo.AR.Common.TextureReader
import com.sinbaram.mapgo.AR.Common.TextureReaderImage
import com.sinbaram.mapgo.AR.Helper.*
import com.sinbaram.mapgo.AR.Renderer.CpuImageRenderer
import com.sinbaram.mapgo.databinding.ActivityMapgoBinding
import java.io.IOException
import java.nio.ByteBuffer
import java.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class MapGoActivity : AppCompatActivity(), GLSurfaceView.Renderer {
    val TAG = MapGoActivity::class.java.simpleName

    lateinit var mBinding: ActivityMapgoBinding
    lateinit var mConfig: Config
    lateinit var mSurfaceView: GLSurfaceView
    lateinit var mCpuImageDisplayRotationHelper: CpuImageDisplayRotationHelper

    lateinit var mCpuResolution: CameraConfig
    lateinit var mLowCameraConfig: CameraConfig
    lateinit var mMediumCameraConfig: CameraConfig
    lateinit var mHighCameraConfig: CameraConfig

    val renderFrameTimeHelper: FrameTimeHelper = FrameTimeHelper()
    val cpuImageFrameTimeHelper: FrameTimeHelper = FrameTimeHelper()
    val mMessageSnackbarHelper: SnackbarHelper = SnackbarHelper()
    val mTextureReader: TextureReader = TextureReader()
    val mCpuImageRenderer: CpuImageRenderer = CpuImageRenderer()
    val mTrackingStateHelper: TrackingStateHelper = TrackingStateHelper(this)
    val mFrameImageInUseLock: Object = Object()
    var mGpuDownloadFramebufferIndex = -1
    var mUserRequestedInstall = false

    companion object {
        val IMAGE_WIDTH: Int = 1280
        val IMAGE_HEIGHT: Int = 720

        val TEXTURE_WIDTH: Int = 1920
        val TEXTURE_HEIGHT: Int = 1080
    }

    var mSession: Session? = null

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

        //! Initialize Cpu Image display rotation helper
        mCpuImageDisplayRotationHelper = CpuImageDisplayRotationHelper(this)

        //! Setup renderer
        mSurfaceView.preserveEGLContextOnPause = true
        mSurfaceView.setEGLContextClientVersion(2)
        mSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        mSurfaceView.setRenderer(this)
        mSurfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        mSurfaceView.setWillNotDraw(false)

        lifecycle.addObserver(renderFrameTimeHelper)
        lifecycle.addObserver(cpuImageFrameTimeHelper)

        //! Pass activity binding root
        setContentView(mBinding.root)
    }

    override fun onResume() {
        super.onResume()

        if (mSession == null) {
            var exception: Exception? = null
            var message: String? = null

            try {
                when (ArCoreApk.getInstance().requestInstall(this, !mUserRequestedInstall)) {
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
            } catch (e: UnavailableArcoreNotInstalledException) {
                message = "Please install ARCore"
                exception = e
            } catch (e: UnavailableApkTooOldException) {
                message = "Please Update ARCore"
                exception = e
            } catch (e: UnavailableSdkTooOldException) {
                message = "Please Update this application"
                exception = e
            } catch (e: Exception) {
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
        } catch (e: CameraNotAvailableException) {
            mMessageSnackbarHelper.showError(
                this,
                "Camera not available. Try restarting the application"
            )
            mSession = null
            return
        }
        mSurfaceView.onResume()
        mCpuImageDisplayRotationHelper.onResume()
    }

    override fun onPause() {
        super.onPause()
        if (mSession != null) {
            mCpuImageDisplayRotationHelper.onPause()
            mSurfaceView.onPause()
            mSession!!.pause()
        }
    }

    // Obtain the supported camera configs and build the list of radio button one for each camera config
    private fun obtainCameraConfigs() {
        // First obtain the session handle before getting the camera config
        if (mSession != null) {
            // Create filter here with desired fps filters
            var cameraConfigFilter: CameraConfigFilter =
                CameraConfigFilter(mSession)
                    .setTargetFps(
                        EnumSet.of(
                            CameraConfig.TargetFps.TARGET_FPS_30,
                            CameraConfig.TargetFps.TARGET_FPS_60
                        )
                    )

            var cameraConfigs: List<CameraConfig> =
                mSession!!.getSupportedCameraConfigs(cameraConfigFilter)

            mLowCameraConfig = getCameraConfigWithSelectedResolution(
                cameraConfigs,
                ImageResolution.LOW_RESOLUTION
            )
            mMediumCameraConfig = getCameraConfigWithSelectedResolution(
                cameraConfigs,
                ImageResolution.MEDIUM_RESOLUTION
            )
            mHighCameraConfig = getCameraConfigWithSelectedResolution(
                cameraConfigs,
                ImageResolution.HIGH_RESOLUTION
            )

            // Default camera config
            mCpuResolution = mMediumCameraConfig
            onCameraConfigChanged(mCpuResolution)
        }
    }

    // Get the camera config with selected resolution
    private fun getCameraConfigWithSelectedResolution(
        cameraConfigs: List<CameraConfig>,
        resolution: MapGoActivity.ImageResolution
    ): CameraConfig {
        // Take the first three camera configs, if camera configs size are larger than 3
        var cameraConfigsByResolution: List<CameraConfig> = ArrayList<CameraConfig>(
            cameraConfigs.subList(0, Math.min(cameraConfigs.size, 3))
        )
        Collections.sort(cameraConfigsByResolution, { p1: CameraConfig, p2: CameraConfig ->
            Integer.compare(p1.imageSize.height, p2.imageSize.height)
        })
        var cameraConfig: CameraConfig = cameraConfigsByResolution.get(0)
        when (resolution) {
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

    private fun onCameraConfigChanged(cameraConfig: CameraConfig) {
        // To change the AR camera config - first we pause the AR session, set the desired camera
        // config and then resume the AR session.
        if (mSession != null) {
            // Block here if the image is still being used.
            synchronized(mFrameImageInUseLock) {
                mSession!!.pause()
                mSession!!.cameraConfig = cameraConfig
                try {
                    mSession!!.resume()
                } catch (ex: CameraNotAvailableException) {
                    mMessageSnackbarHelper.showError(
                        this,
                        "Camera not available. Try restarting the app."
                    )
                    mSession = null
                    return
                }
            }

            // Let the user know that the camera config is set.
            val toastMessage = ("Set the camera config with CPU image resolution of "
                    + cameraConfig.imageSize
                    + " and fps "
                    + cameraConfig.fpsRange
                    + ".")
            val toast = Toast.makeText(this, toastMessage, Toast.LENGTH_LONG)
            toast.setGravity(Gravity.BOTTOM,  /* xOffset= */0,  /* yOffset=*/250)
            toast.show()
        }
    }

    override fun onDestroy() {
        if (mSession != null) {
            // Release native heap memory used by an ARCore session.
            mSession!!.close()
            mSession = null
        }
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        results: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, results)
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            Toast.makeText(
                this,
                "Camera permission is needed to run this application",
                Toast.LENGTH_LONG
            )
                .show()
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                CameraPermissionHelper.launchPermissionSettings(this)
            }
            finish()
        }
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f)

        // Create the texture and pass it to ARCore session to be filled during update
        try {
            mCpuImageRenderer.createOnGlThread(this)

            // ths image format can be either IMAGE_FORMAT_RGBA or IMAGE_FORMAT_I8.
            // Set keepAspectRatio to false so that the output image covers the whole viewport
            mTextureReader.create(
                this,
                TextureReaderImage.IMAGE_FORMAT_I8,
                IMAGE_WIDTH,
                IMAGE_HEIGHT,
                false
            )
        } catch (e: IOException) {
            Log.e(TAG, "Failed to read an asset file")
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        mCpuImageDisplayRotationHelper.onSurfaceChanged(width, height)
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        if (mSession == null)
            return

        //! Synchronize here to avoid calling Session.update or Session.acquireCameraImage while paused
        synchronized(mFrameImageInUseLock) {
            //! Notify ARCore session that the view size changed so that the perspective matrix and
            //! the video background can be properly adjusted
            mCpuImageDisplayRotationHelper.updateSessionIfNeeded(mSession!!)

            try {
                mSession!!.setCameraTextureName(mCpuImageRenderer.textureId)
                val frame = mSession!!.update()
                val camera = frame.camera

                mTrackingStateHelper.updateKeepScreenOnFlag(camera.trackingState)
                renderFrameTimeHelper.nextFrame()

                renderProcessedImageGpuDownload(frame)
            } catch (e: Exception) {
                // Avoid crashing the application due to unhandled exceptions
                Log.e(TAG, "Exception on the OpenGL Thread")
            }
        }
    }

    //! Demonstrates how to access a CPU image using a download from GPU
    private fun renderProcessedImageGpuDownload(frame: Frame?) {
        //! If there is a frame being requested previously, acquire the pixels and process it
        if (mGpuDownloadFramebufferIndex >= 0) {
            var image: TextureReaderImage =
                mTextureReader.acquireFrame(mGpuDownloadFramebufferIndex)

            if (image.format != TextureReaderImage.IMAGE_FORMAT_I8)
                throw IllegalArgumentException("Expected image in I8 format, got format " + image.format)

            //! If you want to post-process image, write code here.
            //!

            //! You should always release frame buffer after using. Otherwise the next fcall to submitFrame() may fail
            mTextureReader.releaseFrame(mGpuDownloadFramebufferIndex)

            mCpuImageRenderer.drawWithCpuImage(
                frame,
                IMAGE_WIDTH,
                IMAGE_HEIGHT,
                image.buffer,
                mCpuImageDisplayRotationHelper.viewportAspectRatio,
                mCpuImageDisplayRotationHelper.cameraToDisplayRotation
            )

            //! Measure frame time since last successful execution of drawWithCpuImage()
            cpuImageFrameTimeHelper.nextFrame()
        } else {
            mCpuImageRenderer.drawWithoutCpuImage()
        }

        //! Submit request for the texture from the current frame
        mGpuDownloadFramebufferIndex =
            mTextureReader.submitFrame(mCpuImageRenderer.textureId, TEXTURE_WIDTH, TEXTURE_HEIGHT)
    }
}