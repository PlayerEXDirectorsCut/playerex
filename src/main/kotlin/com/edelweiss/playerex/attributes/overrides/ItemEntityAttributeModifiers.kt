package com.edelweiss.playerex.attributes.overrides

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.item.ItemStack

/** Meant to be implemented in the `Item` class. */
interface ItemEntityAttributeModifiers {
    /** Provides a mutable attribute modifier `MultiMap` so items can have dynamically changing modifiers based on the NBT. */
    fun get(stack: ItemStack, slot: EquipmentSlot): Multimap<EntityAttribute, EntityAttributeModifier> = HashMultimap.create()
}