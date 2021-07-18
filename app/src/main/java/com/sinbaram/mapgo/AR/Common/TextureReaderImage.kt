package com.sinbaram.mapgo.AR.Common

import java.nio.ByteBuffer

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

/** Image Buffer Class.  */
class TextureReaderImage {
    /** The width of the image, in pixels.  */
    var width: Int

    /** The height of the image, in pixels.  */
    var height: Int

    /** The image buffer.  */
    var buffer: ByteBuffer

    /** Pixel format. Can be either IMAGE_FORMAT_RGBA or IMAGE_FORMAT_I8.  */
    var format: Int

    /** Default constructor.  */
    constructor() {
        width = 1
        height = 1
        format = IMAGE_FORMAT_RGBA
        buffer = ByteBuffer.allocateDirect(4)
    }

    /**
     * Constructor.
     *
     * @param imgWidth the width of the image, in pixels.
     * @param imgHeight the height of the image, in pixels.
     * @param imgFormat the format of the image.
     * @param imgBuffer the buffer of the image pixels.
     */
    constructor(imgWidth: Int, imgHeight: Int, imgFormat: Int, imgBuffer: ByteBuffer?) {
        if (imgWidth == 0 || imgHeight == 0) {
            throw RuntimeException("Invalid image size.")
        }
        if (imgFormat != IMAGE_FORMAT_RGBA && imgFormat != IMAGE_FORMAT_I8) {
            throw RuntimeException("Invalid image format.")
        }
        if (imgBuffer == null) {
            throw RuntimeException("Pixel buffer cannot be null.")
        }
        width = imgWidth
        height = imgHeight
        format = imgFormat
        buffer = imgBuffer
    }

    companion object {
        /** The id corresponding to RGBA8888.  */
        const val IMAGE_FORMAT_RGBA = 0

        /** The id corresponding to grayscale.  */
        const val IMAGE_FORMAT_I8 = 1
    }
}