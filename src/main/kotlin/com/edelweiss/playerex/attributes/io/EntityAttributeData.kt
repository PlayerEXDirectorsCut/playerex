package com.edelweiss.playerex.attributes.io

import com.edelweiss.playerex.attributes.json.AttributeFunctionJSON
import com.edelweiss.playerex.attributes.json.AttributeOverrideJSON
import com.edelweiss.playerex.attributes.mutable.MutableEntityAttribute
import com.edelweiss.playerex.attributes.tags.AttributeTags
import com.edelweiss.playerex.attributes.utils.NbtIO
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier

class EntityAttributeData(
    private var attributeOverride: AttributeOverrideJSON? = null,
    private val functions: MutableMap<Identifier, AttributeFunctionJSON> = mutableMapOf(),
    private val properties: MutableMap<String, String> = mutableMapOf()
) : NbtIO {
    fun override(identifier: Identifier, function: (Identifier, EntityAttribute) -> EntityAttribute) {
        this.attributeOverride?.let { override ->
            val attribute = function(identifier, override.create())
            override.override(attribute as MutableEntityAttribute)
        }
    }

    /**
     * Copies data from itself to an `EntityAttribute`.
     */
    fun copy(target: EntityAttribute) {
        val targetMutable = target as MutableEntityAttribute
        targetMutable.setProperties(this.properties)

        this.functions.keys.forEach { id ->
            val attribute = Registries.ATTRIBUTE.get(id) ?: return@forEach
            val function = this.functions[id] ?: return@forEach
            targetMutable.addChild(attribute as MutableEntityAttribute, function)
        }
    }

    fun putFunctions(functions: Map<Identifier, AttributeFunctionJSON>) = this.functions.putAll(functions)

    fun putProperties(properties: Map<String, String>) = this.properties.putAll(properties)

    override fun readFromNbt(tag: NbtCompound) {
        if (tag.contains(AttributeTags.ATTRIBUTE)) this.attributeOverride = AttributeOverrideJSON(tag.getCompound(AttributeTags.ATTRIBUTE))

        val functions = tag.getCompound(AttributeTags.FUNCTIONS)
        functions.keys.forEach { key ->
            val function = AttributeFunctionJSON.read(functions.getByteArray(key)) ?: return@forEach
            this.functions[Identifier(key)] = function
        }

        val properties = tag.getCompound(AttributeTags.PROPERTIES)
        properties.keys.forEach { key -> this.properties[key] = properties.getString(key) }
    }

    override fun writeToNbt(tag: NbtCompound) {
        val attribute = NbtCompound()

        this.attributeOverride?.let { override ->
            override.writeToNbt(attribute)
            tag.put(AttributeTags.ATTRIBUTE, attribute)
        }

        val functions = NbtCompound()
        this.functions.forEach { (k, v) -> functions.putByteArray(k.toString(), v.write()) }
        tag.put(AttributeTags.FUNCTIONS, functions)

        val properties = NbtCompound()
        this.properties.forEach { (k, v) -> properties.putString(k, v) }
        tag.put(AttributeTags.PROPERTIES, properties)
    }
}