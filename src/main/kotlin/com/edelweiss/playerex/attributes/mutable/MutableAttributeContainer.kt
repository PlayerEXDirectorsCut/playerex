package com.edelweiss.playerex.attributes.mutable

import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttributeInstance
import net.minecraft.util.Identifier

interface MutableAttributeContainer {
    /** Contains custom attributes associated with the container. */
    fun customAttributes(): Map<Identifier, EntityAttributeInstance>

    /** The `LivingEntity` associated with this container. */
    fun getLivingEntity(): LivingEntity

    /** Sets the living entity associted with this container. */
    fun setLivingEntity(entity: LivingEntity)

    /** Refreshes attributes of the `LivingEntity` in this container. */
    fun refresh()

    /** Clears tracked information related to attribute changes. */
    fun clearTrackedInfo()
}