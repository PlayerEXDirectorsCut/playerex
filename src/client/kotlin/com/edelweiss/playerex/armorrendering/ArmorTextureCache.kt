package com.edelweiss.playerex.armorrendering

import net.minecraft.entity.EquipmentSlot
import net.minecraft.util.Identifier

// todo: Maybe extend A to be a more clearer type. Perhaps A: Any?
interface ArmorTextureCache<A> {
    /** Gets a custom armor based on the provided equipment slot. */
    fun getCustomArmor(slot: EquipmentSlot): A

    fun getOrCache(path: String): Identifier
}