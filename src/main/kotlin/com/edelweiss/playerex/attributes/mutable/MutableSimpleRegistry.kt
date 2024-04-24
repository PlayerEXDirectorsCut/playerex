package com.edelweiss.playerex.attributes.mutable

import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

interface MutableSimpleRegistry<T> {
    /** Utilized to remove cached identifiers from a registry. */
    fun removeCachedIds(registry: Registry<T>) {}

    /** Utilized to cache an identifier. */
    fun cacheId(identifier: Identifier) {}
}