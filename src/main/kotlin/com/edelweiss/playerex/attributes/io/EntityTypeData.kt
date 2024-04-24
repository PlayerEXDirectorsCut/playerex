package com.edelweiss.playerex.attributes.io

import com.edelweiss.playerex.attributes.mutable.MutableDefaultAttributeContainer
import com.edelweiss.playerex.attributes.utils.NbtIO
import net.minecraft.entity.attribute.DefaultAttributeContainer
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier

class EntityTypeData(val data: MutableMap<Identifier, Double> = mutableMapOf()) : NbtIO {

    /** Builds the type data using a nullable DAC and the builder associated. */
    fun build(builder: DefaultAttributeContainer.Builder, container: DefaultAttributeContainer? = null) {
        if (container != null) (container as MutableDefaultAttributeContainer).copy(builder)
        this.data.keys.forEach { key ->
            val attribute = Registries.ATTRIBUTE.get(key) ?: return@forEach
            val value = this.data[key] ?: return@forEach
            val clamp = attribute.clamp(value)
            builder.add(attribute, clamp)
        }
    }

    override fun readFromNbt(tag: NbtCompound) {
        tag.keys.forEach { key -> this.data[Identifier(key)] = tag.getDouble(key) }
    }

    override fun writeToNbt(tag: NbtCompound) {
        this.data.forEach { k, v -> tag.putDouble(k.toString(), v)}
    }
}