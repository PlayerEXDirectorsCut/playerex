package com.edelweiss.playerex.attributes.mutable

import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

/**
 * Formerly `MutableRegistryImpl`,
 * Meant to register/unregister `EntityAttributes` to the specified registry, specifically `Registries.ATTRIBUTE`.
*/
@Suppress("UNCHECKED_CAST")
interface PEXMutableRegistry {
    companion object {
        fun <T: EntityAttribute> register(registry: Registry<T>, id: Identifier, value: T): T {
            (registry as MutableSimpleRegistry<T>).cacheId(id)
            return Registry.register(registry, id, value)
        }

        fun <T: EntityAttribute> unregister(registry: Registry<T>) {
            (registry as MutableSimpleRegistry<T>).removeCachedIds(registry)
        }
    }
}