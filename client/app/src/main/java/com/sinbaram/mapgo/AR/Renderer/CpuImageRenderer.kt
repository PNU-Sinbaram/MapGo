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
package com.sinbaram.mapgo.AR.Renderer

import android.content.Context
import android.opengl.GLES11Ext
import android.opengl.GLES20
import com.google.ar.core.Coordinates2d
import com.google.ar.core.Frame
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * This class renders the screen with images from both GPU and CPU. The top half of the screen shows
 * the GPU image, while the bottom half of the screen shows the CPU image.
 */
class CpuImageRenderer {
    private lateinit var quadCoords: FloatBuffer
    private var quadTexCoords: FloatBuffer? = null
    private var quadImgCoords: FloatBuffer? = null
    private var quadProgram = 0
    private var quadPositionAttrib = 0
    private var quadTexCoordAttrib = 0
    private var quadImgCoordAttrib = 0
    private var quadSplitterUniform = 0
    var textureId = -1
        private set
    private var overlayTextureId = -1
    /**
     * Gets the texture splitter position.
     *
     * @return the splitter position.
     */
    /**
     * Sets the splitter position. This position defines the splitting position between the background
     * video and the image.
     *
     * @param position the new splitter position.
     */
    var splitterPosition = 0.0f

    /**
     * Allocates and initializes OpenGL resources needed by the background renderer. Must be called on
     * the OpenGL thread, typically in [GLSurfaceView.Renderer.onSurfaceCreated].
     *
     * @param context Needed to access shader source.
     */
    @Throws(IOException::class)
    fun createOnGlThread(context: Context?) {
        val textures = IntArray(2)
        GLES20.glGenTextures(2, textures, 0)

        // Generate the background texture.
        textureId = textures[0]
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST
        )

        // Generate the CPU Image overlay texture.
        overlayTextureId = textures[1]
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, overlayTextureId)
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_NEAREST
        )
        val numVertices = QUAD_COORDS.size / COORDS_PER_VERTEX
        val bbCoords = ByteBuffer.allocateDirect(QUAD_COORDS.size * FLOAT_SIZE)
        bbCoords.order(ByteOrder.nativeOrder())
        quadCoords = bbCoords.asFloatBuffer()
        quadCoords.put(QUAD_COORDS)
        quadCoords.position(0)
        val bbTexCoords = ByteBuffer.allocateDirect(numVertices * TEXCOORDS_PER_VERTEX * FLOAT_SIZE)
        bbTexCoords.order(ByteOrder.nativeOrder())
        quadTexCoords = bbTexCoords.asFloatBuffer()
        val bbImgCoords = ByteBuffer.allocateDirect(numVertices * TEXCOORDS_PER_VERTEX * FLOAT_SIZE)
        bbImgCoords.order(ByteOrder.nativeOrder())
        quadImgCoords = bbImgCoords.asFloatBuffer()
        val vertexShader = ShaderUtil.loadGLShader(
            TAG, context!!, GLES20.GL_VERTEX_SHADER, "shaders/cpu_screenquad.vert"
        )
        val fragmentShader = ShaderUtil.loadGLShader(
            TAG, context, GLES20.GL_FRAGMENT_SHADER, "shaders/cpu_screenquad.frag"
        )
        quadProgram = GLES20.glCreateProgram()
        GLES20.glAttachShader(quadProgram, vertexShader)
        GLES20.glAttachShader(quadProgram, fragmentShader)
        GLES20.glLinkProgram(quadProgram)
        GLES20.glUseProgram(quadProgram)
        ShaderUtil.checkGLError(TAG, "Program creation")
        quadPositionAttrib = GLES20.glGetAttribLocation(quadProgram, "a_Position")
        quadTexCoordAttrib = GLES20.glGetAttribLocation(quadProgram, "a_TexCoord")
        quadImgCoordAttrib = GLES20.glGetAttribLocation(quadProgram, "a_ImgCoord")
        quadSplitterUniform = GLES20.glGetUniformLocation(quadProgram, "s_SplitterPosition")
        var texLoc = GLES20.glGetUniformLocation(quadProgram, "TexVideo")
        GLES20.glUniform1i(texLoc, 0)
        texLoc = GLES20.glGetUniformLocation(quadProgram, "TexCpuImageGrayscale")
        GLES20.glUniform1i(texLoc, 1)
        ShaderUtil.checkGLError(TAG, "Program parameters")
    }

    /**
     * Draws the AR background image. The image will be drawn such that virtual content rendered with
     * the matrices provided by [Frame.getViewMatrix] and [ ][Session.getProjectionMatrix] will accurately follow static physical
     * objects. This must be called **before** drawing virtual content.
     *
     * @param frame The last `Frame` returned by [Session.update].
     * @param imageWidth The processed image width.
     * @param imageHeight The processed image height.
     * @param processedImageBytesGrayscale the processed bytes of the image, grayscale par only. Can
     * be null.
     * @param screenAspectRatio The aspect ratio of the screen.
     * @param cameraToDisplayRotation The rotation of camera with respect to the display. The value is
     * one of android.view.Surface.ROTATION_#(0, 90, 180, 270).
     */
    fun drawWithCpuImage(
        frame: Frame?,
        imageWidth: Int,
        imageHeight: Int,
        processedImageBytesGrayscale: ByteBuffer?,
        screenAspectRatio: Float,
        cameraToDisplayRotation: Int
    ) {

        // Apply overlay image buffer
        if (processedImageBytesGrayscale != null) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, overlayTextureId)
            GLES20.glTexImage2D(
                GLES20.GL_TEXTURE_2D,
                0,
                GLES20.GL_LUMINANCE,
                imageWidth,
                imageHeight,
                0,
                GLES20.GL_LUMINANCE,
                GLES20.GL_UNSIGNED_BYTE,
                processedImageBytesGrayscale
            )
        }
        updateTextureCoordinates(frame)

        // Rest of the draw code is shared between the two functions.
        drawWithoutCpuImage()
    }

    /**
     * Same as above, but will not update the CPU image drawn. Should be used when a CPU image is
     * unavailable for any reason, and only background should be drawn.
     */
    fun drawWithoutCpuImage() {
        // No need to test or write depth, the screen quad has arbitrary depth, and is expected
        // to be drawn first.
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthMask(false)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES20.glUseProgram(quadProgram)

        // Set the vertex positions.
        GLES20.glVertexAttribPointer(
            quadPositionAttrib, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, quadCoords
        )

        // Set splitter position.
        GLES20.glUniform1f(quadSplitterUniform, splitterPosition)

        // Set the GPU image texture coordinates.
        GLES20.glVertexAttribPointer(
            quadTexCoordAttrib, TEXCOORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, quadTexCoords
        )

        // Set the CPU image texture coordinates.
        GLES20.glVertexAttribPointer(
            quadImgCoordAttrib, TEXCOORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, quadImgCoords
        )

        // Enable vertex arrays
        GLES20.glEnableVertexAttribArray(quadPositionAttrib)
        GLES20.glEnableVertexAttribArray(quadTexCoordAttrib)
        GLES20.glEnableVertexAttribArray(quadImgCoordAttrib)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        // Disable vertex arrays
        GLES20.glDisableVertexAttribArray(quadPositionAttrib)
        GLES20.glDisableVertexAttribArray(quadTexCoordAttrib)
        GLES20.glDisableVertexAttribArray(quadImgCoordAttrib)

        // Restore the depth state for further drawing.
        GLES20.glDepthMask(true)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        ShaderUtil.checkGLError(TAG, "Draw")
    }

    private fun updateTextureCoordinates(frame: Frame?) {
        if (frame == null) {
            return
        }

        // Update GPU image texture coordinates.
        frame.transformCoordinates2d(
            Coordinates2d.OPENGL_NORMALIZED_DEVICE_COORDINATES,
            quadCoords,
            Coordinates2d.IMAGE_NORMALIZED,
            quadImgCoords
        )

        // Update GPU image texture coordinates.
        frame.transformCoordinates2d(
            Coordinates2d.OPENGL_NORMALIZED_DEVICE_COORDINATES,
            quadCoords,
            Coordinates2d.TEXTURE_NORMALIZED,
            quadTexCoords
        )
    }

    companion object {
        private val TAG = CpuImageRenderer::class.java.simpleName
        private const val COORDS_PER_VERTEX = 2
        private const val TEXCOORDS_PER_VERTEX = 2
        private const val FLOAT_SIZE = 4
        private val QUAD_COORDS = floatArrayOf(
            -1.0f, -1.0f, -1.0f, +1.0f, +1.0f, -1.0f, +1.0f, +1.0f
        )
    }
}
