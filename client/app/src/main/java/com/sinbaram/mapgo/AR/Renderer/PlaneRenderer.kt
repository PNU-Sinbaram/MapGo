/*
 * Copyright 2017 Google LLC
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
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import android.opengl.Matrix
import com.google.ar.core.Plane
import com.google.ar.core.Pose
import com.google.ar.core.TrackingState
import com.sinbaram.mapgo.AR.Renderer.ShaderUtil.checkGLError
import com.sinbaram.mapgo.AR.Renderer.ShaderUtil.loadGLShader
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.Collections

/** Renders the detected AR planes.  */
class PlaneRenderer {
    private var planeProgram = 0
    private val textures = IntArray(1)
    private var planeXZPositionAlphaAttribute = 0
    private var planeModelUniform = 0
    private var planeNormalUniform = 0
    private var planeModelViewProjectionUniform = 0
    private var textureUniform = 0
    private var gridControlUniform = 0
    private var planeUvMatrixUniform = 0
    private var vertexBuffer = ByteBuffer.allocateDirect(INITIAL_VERTEX_BUFFER_SIZE_BYTES)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
    private var indexBuffer = ByteBuffer.allocateDirect(INITIAL_INDEX_BUFFER_SIZE_BYTES)
        .order(ByteOrder.nativeOrder())
        .asShortBuffer()

    // Temporary lists/matrices allocated here to reduce number of allocations for each frame.
    private val modelMatrix = FloatArray(16)
    private val modelViewMatrix = FloatArray(16)
    private val modelViewProjectionMatrix = FloatArray(16)
    private val planeAngleUvMatrix = FloatArray(4) // 2x2 rotation matrix applied to uv coords.
    private val planeIndexMap: MutableMap<Plane, Int> = HashMap()

    /**
     * Allocates and initializes OpenGL resources needed by the plane renderer. Must be called on the
     * OpenGL thread, typically in [GLSurfaceView.Renderer.onSurfaceCreated].
     *
     * @param context Needed to access shader source and texture PNG.
     * @param gridDistanceTextureName Name of the PNG file containing the grid texture.
     */
    @Throws(IOException::class)
    fun createOnGlThread(context: Context, gridDistanceTextureName: String?) {
        val vertexShader = loadGLShader(TAG, context, GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_NAME)
        val passthroughShader =
            loadGLShader(TAG, context, GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_NAME)
        planeProgram = GLES20.glCreateProgram()
        GLES20.glAttachShader(planeProgram, vertexShader)
        GLES20.glAttachShader(planeProgram, passthroughShader)
        GLES20.glLinkProgram(planeProgram)
        GLES20.glUseProgram(planeProgram)
        checkGLError(TAG, "Program creation")

        // Read the texture.
        val textureBitmap =
            BitmapFactory.decodeStream(context.assets.open(gridDistanceTextureName!!))
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glGenTextures(textures.size, textures, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0])
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR
        )
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, textureBitmap, 0)
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        checkGLError(TAG, "Texture loading")
        planeXZPositionAlphaAttribute =
            GLES20.glGetAttribLocation(planeProgram, "a_XZPositionAlpha")
        planeModelUniform = GLES20.glGetUniformLocation(planeProgram, "u_Model")
        planeNormalUniform = GLES20.glGetUniformLocation(planeProgram, "u_Normal")
        planeModelViewProjectionUniform =
            GLES20.glGetUniformLocation(planeProgram, "u_ModelViewProjection")
        textureUniform = GLES20.glGetUniformLocation(planeProgram, "u_Texture")
        gridControlUniform = GLES20.glGetUniformLocation(planeProgram, "u_gridControl")
        planeUvMatrixUniform = GLES20.glGetUniformLocation(planeProgram, "u_PlaneUvMatrix")
        checkGLError(TAG, "Program parameters")
    }

    /** Updates the plane model transform matrix and extents.  */
    private fun updatePlaneParameters(
        planeMatrix: FloatArray,
        extentX: Float,
        extentZ: Float,
        boundary: FloatBuffer?
    ) {
        System.arraycopy(planeMatrix, 0, modelMatrix, 0, 16)
        if (boundary == null) {
            vertexBuffer.limit(0)
            indexBuffer.limit(0)
            return
        }

        // Generate a new set of vertices and a corresponding triangle strip index set so that
        // the plane boundary polygon has a fading edge. This is done by making a copy of the
        // boundary polygon vertices and scaling it down around center to push it inwards. Then
        // the index buffer is setup accordingly.
        boundary.rewind()
        val boundaryVertices = boundary.limit() / 2
        val numVertices: Int
        val numIndices: Int
        numVertices = boundaryVertices * VERTS_PER_BOUNDARY_VERT
        // drawn as GL_TRIANGLE_STRIP with 3n-2 triangles (n-2 for fill, 2n for perimeter).
        numIndices = boundaryVertices * INDICES_PER_BOUNDARY_VERT
        if (vertexBuffer.capacity() < numVertices * COORDS_PER_VERTEX) {
            var size = vertexBuffer.capacity()
            while (size < numVertices * COORDS_PER_VERTEX) {
                size *= 2
            }
            vertexBuffer = ByteBuffer.allocateDirect(BYTES_PER_FLOAT * size)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
        }
        vertexBuffer.rewind()
        vertexBuffer.limit(numVertices * COORDS_PER_VERTEX)
        if (indexBuffer.capacity() < numIndices) {
            var size = indexBuffer.capacity()
            while (size < numIndices) {
                size *= 2
            }
            indexBuffer = ByteBuffer.allocateDirect(BYTES_PER_SHORT * size)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer()
        }
        indexBuffer.rewind()
        indexBuffer.limit(numIndices)

        // Note: when either dimension of the bounding box is smaller than 2*FADE_RADIUS_M we
        // generate a bunch of 0-area triangles.  These don't get rendered though so it works
        // out ok.
        val xScale = Math.max((extentX - 2 * FADE_RADIUS_M) / extentX, 0.0f)
        val zScale = Math.max((extentZ - 2 * FADE_RADIUS_M) / extentZ, 0.0f)
        while (boundary.hasRemaining()) {
            val x = boundary.get()
            val z = boundary.get()
            vertexBuffer.put(x)
            vertexBuffer.put(z)
            vertexBuffer.put(0.0f)
            vertexBuffer.put(x * xScale)
            vertexBuffer.put(z * zScale)
            vertexBuffer.put(1.0f)
        }

        // step 1, perimeter
        indexBuffer.put(((boundaryVertices - 1) * 2).toShort())
        for (i in 0 until boundaryVertices) {
            indexBuffer.put((i * 2).toShort())
            indexBuffer.put((i * 2 + 1).toShort())
        }
        indexBuffer.put(1.toShort())
        // This leaves us on the interior edge of the perimeter between the inset vertices
        // for boundary verts n-1 and 0.

        // step 2, interior:
        for (i in 1 until boundaryVertices / 2) {
            indexBuffer.put(((boundaryVertices - 1 - i) * 2 + 1).toShort())
            indexBuffer.put((i * 2 + 1).toShort())
        }
        if (boundaryVertices % 2 != 0) {
            indexBuffer.put((boundaryVertices / 2 * 2 + 1).toShort())
        }
    }

    private fun draw(
        cameraView: FloatArray,
        cameraPerspective: FloatArray,
        planeNormal: FloatArray
    ) {
        // Build the ModelView and ModelViewProjection matrices
        // for calculating cube position and light.
        Matrix.multiplyMM(modelViewMatrix, 0, cameraView, 0, modelMatrix, 0)
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, cameraPerspective, 0, modelViewMatrix, 0)

        // Set the position of the plane
        vertexBuffer.rewind()
        GLES20.glVertexAttribPointer(
            planeXZPositionAlphaAttribute,
            COORDS_PER_VERTEX,
            GLES20.GL_FLOAT,
            false,
            BYTES_PER_FLOAT * COORDS_PER_VERTEX,
            vertexBuffer
        )

        // Set the Model and ModelViewProjection matrices in the shader.
        GLES20.glUniformMatrix4fv(planeModelUniform, 1, false, modelMatrix, 0)
        GLES20.glUniform3f(planeNormalUniform, planeNormal[0], planeNormal[1], planeNormal[2])
        GLES20.glUniformMatrix4fv(
            planeModelViewProjectionUniform, 1, false, modelViewProjectionMatrix, 0
        )
        indexBuffer.rewind()
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLE_STRIP, indexBuffer.limit(), GLES20.GL_UNSIGNED_SHORT, indexBuffer
        )
        checkGLError(TAG, "Drawing plane")
    }

    internal class SortablePlane(val distance: Float, val plane: Plane)

    /**
     * Draws the collection of tracked planes, with closer planes hiding more distant ones.
     *
     * @param allPlanes The collection of planes to draw.
     * @param cameraPose The pose of the camera, as returned by [Camera.getPose]
     * @param cameraPerspective The projection matrix, as returned by [     ][Camera.getProjectionMatrix]
     */
    fun drawPlanes(allPlanes: Collection<Plane>, cameraPose: Pose, cameraPerspective: FloatArray) {
        // Planes must be sorted by distance from camera so that we draw closer planes first, and
        // they occlude the farther planes.
        val sortedPlanes: MutableList<SortablePlane> = ArrayList()
        for (plane in allPlanes) {
            if (plane.trackingState != TrackingState.TRACKING || plane.subsumedBy != null) {
                continue
            }
            val distance = calculateDistanceToPlane(plane.centerPose, cameraPose)
            if (distance < 0) { // Plane is back-facing.
                continue
            }
            sortedPlanes.add(SortablePlane(distance, plane))
        }
        Collections.sort(
            sortedPlanes,
            object : Comparator<SortablePlane?> {
                override fun compare(o1: SortablePlane?, o2: SortablePlane?): Int {
                    return java.lang.Float.compare(o2!!.distance, o1!!.distance)
                }
            }
        )
        val cameraView = FloatArray(16)
        cameraPose.inverse().toMatrix(cameraView, 0)

        // Disable depth write.
        GLES20.glDepthMask(false)

        // Normal alpha blending with premultiplied alpha.
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        // Set up the shader.
        GLES20.glUseProgram(planeProgram)

        // Attach the texture.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0])
        GLES20.glUniform1i(textureUniform, 0)

        // Shared fragment uniforms.
        GLES20.glUniform4fv(gridControlUniform, 1, GRID_CONTROL, 0)

        // Enable vertex arrays
        GLES20.glEnableVertexAttribArray(planeXZPositionAlphaAttribute)
        checkGLError(TAG, "Setting up to draw planes")
        for (sortedPlane in sortedPlanes) {
            val plane = sortedPlane.plane
            val planeMatrix = FloatArray(16)
            plane.centerPose.toMatrix(planeMatrix, 0)
            val normal = FloatArray(3)
            // Get transformed Y axis of plane's coordinate system.
            plane.centerPose.getTransformedAxis(1, 1.0f, normal, 0)
            updatePlaneParameters(
                planeMatrix, plane.extentX, plane.extentZ, plane.polygon
            )

            // Get plane index. Keep a map to assign same indices to same planes.
            var planeIndex = planeIndexMap[plane]
            if (planeIndex == null) {
                planeIndex = planeIndexMap.size
                planeIndexMap[plane] = planeIndex
            }

            // Each plane will have its own angle offset from others, to make them easier to
            // distinguish. Compute a 2x2 rotation matrix from the angle.
            val angleRadians = planeIndex * 0.144f
            val uScale = DOTS_PER_METER
            val vScale = DOTS_PER_METER * EQUILATERAL_TRIANGLE_SCALE
            planeAngleUvMatrix[0] = (+Math.cos(angleRadians.toDouble())).toFloat() * uScale
            planeAngleUvMatrix[1] = (-Math.sin(angleRadians.toDouble())).toFloat() * vScale
            planeAngleUvMatrix[2] = (+Math.sin(angleRadians.toDouble())).toFloat() * uScale
            planeAngleUvMatrix[3] = (+Math.cos(angleRadians.toDouble())).toFloat() * vScale
            GLES20.glUniformMatrix2fv(planeUvMatrixUniform, 1, false, planeAngleUvMatrix, 0)
            draw(cameraView, cameraPerspective, normal)
        }

        // Clean up the state we set
        GLES20.glDisableVertexAttribArray(planeXZPositionAlphaAttribute)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glDisable(GLES20.GL_BLEND)
        GLES20.glDepthMask(true)
        checkGLError(TAG, "Cleaning up after drawing planes")
    }

    companion object {
        private val TAG = PlaneRenderer::class.java.simpleName

        // Shader names.
        private const val VERTEX_SHADER_NAME = "shaders/plane.vert"
        private const val FRAGMENT_SHADER_NAME = "shaders/plane.frag"
        private const val BYTES_PER_FLOAT = java.lang.Float.SIZE / 8
        private const val BYTES_PER_SHORT = java.lang.Short.SIZE / 8
        private const val COORDS_PER_VERTEX = 3 // x, z, alpha
        private const val VERTS_PER_BOUNDARY_VERT = 2
        private const val INDICES_PER_BOUNDARY_VERT = 3
        private const val INITIAL_BUFFER_BOUNDARY_VERTS = 64
        private const val INITIAL_VERTEX_BUFFER_SIZE_BYTES =
            BYTES_PER_FLOAT * COORDS_PER_VERTEX * VERTS_PER_BOUNDARY_VERT * INITIAL_BUFFER_BOUNDARY_VERTS
        private const val INITIAL_INDEX_BUFFER_SIZE_BYTES = (
            BYTES_PER_SHORT
                * INDICES_PER_BOUNDARY_VERT
                * INDICES_PER_BOUNDARY_VERT
                * INITIAL_BUFFER_BOUNDARY_VERTS
            )
        private const val FADE_RADIUS_M = 0.25f
        private const val DOTS_PER_METER = 10.0f
        private val EQUILATERAL_TRIANGLE_SCALE = (1 / Math.sqrt(3.0)).toFloat()

        // Using the "signed distance field" approach to render sharp lines and circles.
        // {dotThreshold, lineThreshold, lineFadeSpeed, occlusionScale}
        // dotThreshold/lineThreshold: red/green intensity above which dots/lines are present
        // lineFadeShrink:  lines will fade in between alpha = 1-(1/lineFadeShrink) and 1.0
        // occlusionShrink: occluded planes will fade out between alpha = 0 and 1/occlusionShrink
        private val GRID_CONTROL = floatArrayOf(0.2f, 0.4f, 2.0f, 1.5f)

        // Calculate the normal distance to plane from cameraPose, the given planePose should have y axis
        // parallel to plane's normal, for example plane's center pose or hit test pose.
        fun calculateDistanceToPlane(planePose: Pose, cameraPose: Pose): Float {
            val normal = FloatArray(3)
            val cameraX = cameraPose.tx()
            val cameraY = cameraPose.ty()
            val cameraZ = cameraPose.tz()
            // Get transformed Y axis of plane's coordinate system.
            planePose.getTransformedAxis(1, 1.0f, normal, 0)
            // Compute dot product of plane's normal with vector from camera to plane center.
            return (cameraX - planePose.tx()) * normal[0] + (cameraY - planePose.ty()) * normal[1] + (cameraZ - planePose.tz()) * normal[2]
        }
    }
}
