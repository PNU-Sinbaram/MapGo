package com.sinbaram.mapgo.Model

import com.google.ar.sceneform.AnchorNode
import com.naver.maps.map.Symbol

data class SymbolRenderable (
   var symbol: Symbol,
   var anchor: AnchorNode?
) {
    override fun equals(other: Any?): Boolean {
        if (other !is SymbolRenderable) {
            return false
        }
        return this.symbol == other.symbol
    }

    override fun hashCode(): Int {
        return this.symbol.hashCode()
    }
}