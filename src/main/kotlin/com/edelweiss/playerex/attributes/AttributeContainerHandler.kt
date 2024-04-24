package com.edelweiss.playerex.attributes

import com.edelweiss.playerex.attributes.io.EntityTypeData
import com.edelweiss.playerex.attributes.mutable.MutableAttributeContainer
import com.edelweiss.playerex.attributes.mutable.MutableDefaultAttributeContainer
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.AttributeContainer
import net.minecraft.entity.attribute.DefaultAttributeContainer
import net.minecraft.entity.attribute.DefaultAttributeRegistry
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import java.util.stream.Collectors
import kotlin.math.round

class AttributeContainerHandler(
    private var implicitContainers: Map<Int, AttributeManager.Tuple<DefaultAttributeContainer>> = mapOf(),
    private var explicitContainers: Map<EntityType<out LivingEntity>, DefaultAttributeContainer> = mapOf()
) {
    /**
     * Gets an `AttributeContainer` based on an `EntityType`s DAC Builder, which will then wrap a container with that builder to the given
     * `LivingEntity` and return it.
     */
    fun getContainer(entityType: EntityType<out LivingEntity>, livingEntity: LivingEntity): AttributeContainer {
        val builder = DefaultAttributeContainer.builder()
        (DefaultAttributeRegistry.get(entityType) as MutableDefaultAttributeContainer).copy(builder)

        this.implicitContainers.values.forEach { tuple ->
            if (tuple.entity.isInstance(livingEntity)) {
                (tuple.value as MutableDefaultAttributeContainer).copy(builder)
            }
        }

        if (this.explicitContainers.containsKey(entityType)) {
            (this.explicitContainers[entityType] as MutableDefaultAttributeContainer).copy(builder)
        }

        val container = AttributeContainer(builder.build())
        (container as MutableAttributeContainer).setLivingEntity(livingEntity)

        return container
    }

   fun buildContainers(entityTypeDataIn: Map<Identifier, EntityTypeData>, instances: Map<Identifier, AttributeManager.Tuple<Int>>) {
        val entityTypes = Registries.ENTITY_TYPE.ids.stream()
            .filter { id -> DefaultAttributeRegistry.hasDefinitionFor(Registries.ENTITY_TYPE[id]) }
            .collect(Collectors.toSet())

        val implContainers = mutableMapOf<Int, AttributeManager.Tuple<DefaultAttributeContainer>>()
        val explContainers = mutableMapOf<EntityType<out LivingEntity>, DefaultAttributeContainer>()
        val orderedEntityTypes = mutableMapOf<Int, AttributeManager.Tuple<Identifier>>()

        entityTypeDataIn.keys.forEach { id ->
            if (instances.containsKey(id)) {
                val tuple = instances[id]!!
                orderedEntityTypes[tuple.value] = AttributeManager.Tuple(tuple.entity, id)
            }
            if (!entityTypes.contains(id)) return@forEach

            val entityType = (Registries.ENTITY_TYPE[id] as EntityType<out LivingEntity>)
            val builder = DefaultAttributeContainer.builder()
            val entityTypeData = entityTypeDataIn[id]
            entityTypeData?.build(builder, DefaultAttributeRegistry.get(entityType))
            explContainers[entityType] = builder.build()
        }

        val size = orderedEntityTypes.size
        val max = orderedEntityTypes.keys.maxOrNull() ?: 0

        orderedEntityTypes.entries.forEach { entry ->
            val tuple = entry.value
            val id = tuple.value
            val hierarchy = entry.key
            val index = (round(size.toDouble() * hierarchy / max) - 1.0).toInt()

            val builder = DefaultAttributeContainer.builder()
            val entityTypeData = entityTypeDataIn[id]
            entityTypeData?.build(builder)
            implContainers[index] = AttributeManager.Tuple(tuple.entity, builder.build())
        }

        this.implicitContainers = implContainers
        this.explicitContainers = explContainers
    }
}