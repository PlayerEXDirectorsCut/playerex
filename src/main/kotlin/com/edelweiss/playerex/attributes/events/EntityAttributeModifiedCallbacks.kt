package com.edelweiss.playerex.attributes.events

import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier

object EntityAttributeModifiedCallbacks {
    /**
     * Fired when the value of an attribute instance was modified, either by adding/removing a modifier or changing
     * the value of the modifier, or by reloading the datapack and having the living entity renew its attribute container.
     * Living entity and modifiers may or may not be null.
     */
    val MODIFIED = EventFactory.createArrayBacked(Modified::class.java) { listeners -> Modified {
            attribute, entity, modifier, prev, added -> listeners.forEach { it.onModified(attribute, entity, modifier, prev, added) }
    }}

    /**
     * Fired after the attribute instance value was calculated, but before it was output. This offers one last chance to alter the
     * value in some way (for example round a decimal to an integer).
     */
    val CLAMPED = EventFactory.createArrayBacked(Clamped::class.java) { listeners -> Clamped {
        attribute, value ->
            var cache = value
            listeners.forEach { cache = it.onClamped(attribute, cache) }
            cache
        }
    }

    fun interface Modified {
        fun onModified(attribute: EntityAttribute, livingEntity: LivingEntity, modifier: EntityAttributeModifier, previousValue: Double, added: Boolean)
    }

    fun interface Clamped {
        fun onClamped(attribute: EntityAttribute, value: Double): Double
    }
}