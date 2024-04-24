package com.edelweiss.playerex.attributes.overrides

import java.util.UUID

interface PEXEntityAttributeInstance {
    /** Changes the value of the input modifier if in existence, and updates the instance & children. */
    fun updateModifier(uuid: UUID, value: Double)
}