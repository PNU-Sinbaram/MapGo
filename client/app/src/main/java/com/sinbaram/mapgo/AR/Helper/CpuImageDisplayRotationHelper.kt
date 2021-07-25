/*
* Copyright 2018 Google LLC
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.sinbaram.mapgo.AR.Helper

import android.content.Context
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import android.hardware.display.DisplayManager
import android.hardware.display.DisplayManager.DisplayListener
import android.view.Display
import android.view.Surface
import android.view.WindowManager
import com.google.ar.core.Session

/**
 * Helper to track the display rotations. In particular, the 180 degree rotations are not notified
 * by the onSurfaceChanged() callback, and thus they require listening to the android display
 * events.
 */
class CpuImageDisplayRotationHelper(private val context: Context) :
    DisplayListener {
    private var viewportChanged = false
    private var viewportWidth = 0
    private var viewportHeight = 0
    private val display: Display

    /** Registers the display listener. Should be called from [Activity.onResume].  */
    fun onResume() {
        context.getSystemService(DisplayManager::class.java).registerDisplayListener(this, null)
    }

    /** Unregisters the display listener. Should be called from [Activity.onPause].  */
    fun onPause() {
        context.getSystemService(DisplayManager::class.java).unregisterDisplayListener(this)
    }

    /**
     * Records a change in surface dimensions. This will be later used by [ ][.updateSessionIfNeeded]. Should be called from [ ].
     *
     * @param width the updated width of the surface.
     * @param height the updated height of the surface.
     */
    fun onSurfaceChanged(width: Int, height: Int) {
        viewportWidth = width
        viewportHeight = height
        viewportChanged = true
    }

    /**
     * Updates the session display geometry if a change was posted either by [ ][.onSurfaceChanged] call or by [.onDisplayChanged] system callback. This
     * function should be called explicitly before each call to [Session.update]. This
     * function will also clear the 'pending update' (viewportChanged) flag.
     *
     * @param session the [Session] object to update if display geometry changed.
     */
    fun updateSessionIfNeeded(session: Session) {
        if (viewportChanged) {
            val displayRotation = display.rotation
            session.setDisplayGeometry(displayRotation, viewportWidth, viewportHeight)
            viewportChanged = false
        }
    }

    /**
     * Returns the current rotation state of android display. Same as [Display.getRotation].
     */
    val rotation: Int
        get() = display.rotation

    /**
     * Returns the aspect ratio of viewport.
     */
    val viewportAspectRatio: Float
        get() {
            val aspectRatio: Float
            aspectRatio =
                when (cameraToDisplayRotation) {
                    Surface.ROTATION_90, Surface.ROTATION_270 -> viewportHeight.toFloat() / viewportWidth.toFloat()
                    Surface.ROTATION_0, Surface.ROTATION_180 -> viewportWidth.toFloat() / viewportHeight.toFloat()
                    else -> viewportWidth.toFloat() / viewportHeight.toFloat()
                }
            return aspectRatio
        } // Get screen to device rotation in degress.

    // Convert degrees to rotation ids.
    /**
     * Returns the rotation of the back-facing camera with respect to the display. The value is one of
     * android.view.Surface.ROTATION_#(0, 90, 180, 270).
     */
    val cameraToDisplayRotation: Int
        get() {
            // Get screen to device rotation in degress.
            var screenDegrees = 0
            when (rotation) {
                Surface.ROTATION_0 -> screenDegrees = 0
                Surface.ROTATION_90 -> screenDegrees = 90
                Surface.ROTATION_180 -> screenDegrees = 180
                Surface.ROTATION_270 -> screenDegrees = 270
                else -> {
                }
            }
            val cameraInfo = CameraInfo()
            Camera.getCameraInfo(CameraInfo.CAMERA_FACING_BACK, cameraInfo)
            val cameraToScreenDegrees = (cameraInfo.orientation - screenDegrees + 360) % 360

            // Convert degrees to rotation ids.
            var cameraToScreenRotation = Surface.ROTATION_0
            when (cameraToScreenDegrees) {
                0 -> cameraToScreenRotation = Surface.ROTATION_0
                90 -> cameraToScreenRotation = Surface.ROTATION_90
                180 -> cameraToScreenRotation = Surface.ROTATION_180
                270 -> cameraToScreenRotation = Surface.ROTATION_270
                else -> {
                }
            }
            return cameraToScreenRotation
        }

    override fun onDisplayAdded(displayId: Int) {
        TODO("Not yet implemented")
    }

    override fun onDisplayRemoved(displayId: Int) {
        TODO("Not yet implemented")
    }

    override fun onDisplayChanged(displayId: Int) {
        viewportChanged = true
    }

    /**
     * Constructs the CpuImageDisplayRotationHelper but does not register the listener yet.
     *
     * @param context the Android [Context].
     */
    init {
        display = context.getSystemService(WindowManager::class.java).defaultDisplay
    }
}
