package com.edelweiss.playerex.attributes.mutable

import net.minecraft.entity.attribute.DefaultAttributeContainer

interface MutableDefaultAttributeContainer {
    /** Copies configurations from a **`DefaultAttributeContainer.Builder`**. */
    fun copy(builder: DefaultAttributeContainer.Builder)
}