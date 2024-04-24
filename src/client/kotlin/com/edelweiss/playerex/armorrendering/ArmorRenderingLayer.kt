package com.edelweiss.playerex.armorrendering

import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack

interface ArmorRenderingLayer {
    fun render(stack: ItemStack, entity: LivingEntity, slot: EquipmentSlot): ArmorRenderingProvider
}