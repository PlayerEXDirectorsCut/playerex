package com.edelweiss.playerex.armorrendering

fun interface ArmorRenderingData {
    fun accept(texturePath: String, color: Int, hasGlint: Boolean)
}