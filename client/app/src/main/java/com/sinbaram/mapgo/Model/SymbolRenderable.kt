package com.sinbaram.mapgo.Model

import com.google.ar.sceneform.AnchorNode
import com.naver.maps.map.Symbol

/** Symbol and AnchorNode Pair data class */
data class SymbolRenderable(
    var symbol: Symbol,
    var anchor: AnchorNode?
) {
    /** Compare only with symbol */
    override fun equals(other: Any?): Boolean {
        if (other !is SymbolRenderable) {
            return false
        }
        return this.symbol == other.symbol
    }

    /** Get hashcode using symbol */
    override fun hashCode(): Int {
        return this.symbol.hashCode()
    }
}
