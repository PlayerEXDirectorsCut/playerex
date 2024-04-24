package com.edelweiss.playerex.attributes

import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier


class AttributesAPI {
    companion object {
        /**
         * The entity instance for LivingEntity.class.
         */
        const val ENTITY_INSTANCE_LIVING_ENTITY: String = "living_entity"

        /**
         * The entity instance for MobEntity.class.
         */
        const val ENTITY_INSTANCE_MOB_ENTITY: String = "mob_entity"

        /**
         * The entity instance for PathAwareEntity.class.
         */
        const val ENTITY_INSTANCE_PATH_AWARE_ENTITY: String = "path_aware_entity"

        /**
         * The entity instance for HostileEntity.class.
         */
        const val ENTITY_INSTANCE_HOSTILE_ENTITY: String = "hostile_entity"

        /**
         * The entity instance for PassiveEntity.class.
         */
        const val ENTITY_INSTANCE_PASSIVE_ENTITY: String = "passive_entity"

        /**
         * The entity instance for AnimalEntity.class.
         */
        const val ENTITY_INSTANCE_ANIMAL_ENTITY: String = "animal_entity"

        /** Returns a `EntityAttribute` assigned to the input key. */
        fun getAttribute(key: Identifier): () -> EntityAttribute? = { -> Registries.ATTRIBUTE.get(key) }

        /**
         * Allows for an Optional-like use of attributes that may or may not exist all
         * the time. This is the correct way of getting and using
         * values from attributes loaded by datapacks.
         *
         * @param <T>
         * @param livingEntity
         * @param entityAttribute
         * @param fallback
         * @param function
         * @return If the input attribute is both registered to the game and present on
         *         the input entity, returns the returning value of the input function.
         *         Else returns the fallback input.
         */
        fun <T> ifPresent(livingEntity: LivingEntity, entityAttribute: () -> EntityAttribute?, fallback: T, function: (Double?) -> T): T {
            val container = livingEntity.attributes
            val attribute = entityAttribute()

            if (attribute != null && container.hasAttribute(attribute)) {
                return function(container.getValue(attribute))
            }

            return fallback
        }
    }
}