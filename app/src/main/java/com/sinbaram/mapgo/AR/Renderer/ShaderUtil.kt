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
import android.opengl.GLES20
import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

/** Shader helper functions.  */
object ShaderUtil {
    /**
     * Converts a raw text file, saved as a resource, into an OpenGL ES shader.
     *
     * @param type The type of shader we will be creating.
     * @param filename The filename of the asset file about to be turned into a shader.
     * @param defineValuesMap The #define values to add to the top of the shader source code.
     * @return The shader object handler.
     */
    @Throws(IOException::class)
    fun loadGLShader(
        tag: String?,
        context: Context,
        type: Int,
        filename: String,
        defineValuesMap: Map<String, Int>
    ): Int {
        // Load shader source code.
        var code = readShaderFileFromAssets(context, filename)

        // Prepend any #define values specified during this run.
        var defines = ""
        for ((key, value) in defineValuesMap) {
            defines += """#define $key $value
"""
        }
        code = defines + code

        // Compiles shader code.
        var shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, code)
        GLES20.glCompileShader(shader)

        // Get the compilation status.
        val compileStatus = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0)

        // If the compilation failed, delete the shader.
        if (compileStatus[0] == 0) {
            Log.e(tag, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shader))
            GLES20.glDeleteShader(shader)
            shader = 0
        }
        if (shader == 0) {
            throw RuntimeException("Error creating shader.")
        }
        return shader
    }

    /** Overload of loadGLShader that assumes no additional #define values to add.  */
    @Throws(IOException::class)
    fun loadGLShader(tag: String?, context: Context, type: Int, filename: String): Int {
        val emptyDefineValuesMap: Map<String, Int> = TreeMap()
        return loadGLShader(tag, context, type, filename, emptyDefineValuesMap)
    }

    /**
     * Checks if we've had an error inside of OpenGL ES, and if so what that error is.
     *
     * @param label Label to report in case of error.
     * @throws RuntimeException If an OpenGL error is detected.
     */
    fun checkGLError(tag: String?, label: String) {
        var lastError = GLES20.GL_NO_ERROR
        // Drain the queue of all errors.
        var error: Int
        while (GLES20.glGetError().also { error = it } != GLES20.GL_NO_ERROR) {
            Log.e(tag, "$label: glError $error")
            lastError = error
        }
        if (lastError != GLES20.GL_NO_ERROR) {
            throw RuntimeException("$label: glError $lastError")
        }
    }

    /**
     * Converts a raw shader file into a string.
     *
     * @param filename The filename of the shader file about to be turned into a shader.
     * @return The context of the text file, or null in case of error.
     */
    @Throws(IOException::class)
    private fun readShaderFileFromAssets(context: Context, filename: String): String {
        context.assets.open(filename).use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                val sb = StringBuilder()
                var line: String
                while (reader.readLine().also { line = it } != null) {
                    val tokens =
                        line.split(" ").dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                    if (tokens[0] == "#include") {
                        var includeFilename = tokens[1]
                        includeFilename = includeFilename.replace("\"", "")
                        if (includeFilename == filename) {
                            throw IOException("Do not include the calling file.")
                        }
                        sb.append(readShaderFileFromAssets(context, includeFilename))
                    } else {
                        sb.append(line).append("\n")
                    }
                }
                return sb.toString()
            }
        }
    }
}