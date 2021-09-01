package com.sinbaram.mapgo.Model

import com.google.ar.sceneform.AnchorNode
import com.naver.maps.map.overlay.Marker

/** Symbol and AnchorNode Pair data class */
data class MarkerRenderable(
    var marker: Marker,
    var anchor: AnchorNode?
) {
    /** Compare only with symbol */
    override fun equals(other: Any?): Boolean {
        if (other !is MarkerRenderable) {
            return false
        }
        return this.marker == other.marker
    }

    /** Get hashcode using symbol */
    override fun hashCode(): Int {
        return this.marker.hashCode()
    }
}
