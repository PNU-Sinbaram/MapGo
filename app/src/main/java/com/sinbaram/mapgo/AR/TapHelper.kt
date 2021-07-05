package com.sinbaram.mapgo.AR

import android.content.Context
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue

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


/**
 * Helper to detect taps using Android GestureDetector, and pass the taps between UI thread and
 * render thread.
 */
class TapHelper(context: Context?) : OnTouchListener {
    private val gestureDetector: GestureDetector
    private val queuedSingleTaps: BlockingQueue<MotionEvent> = ArrayBlockingQueue(16)

    /**
     * Polls for a tap.
     *
     * @return if a tap was queued, a MotionEvent for the tap. Otherwise null if no taps are queued.
     */
    fun poll(): MotionEvent {
        return queuedSingleTaps.poll()
    }

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(motionEvent)
    }

    /**
     * Creates the tap helper.
     *
     * @param context the application's context.
     */
    init {
        gestureDetector = GestureDetector(
            context,
            object : SimpleOnGestureListener() {
                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    // Queue tap if there is space. Tap is lost if queue is full.
                    queuedSingleTaps.offer(e)
                    return true
                }

                override fun onDown(e: MotionEvent): Boolean {
                    return true
                }
            })
    }
}
