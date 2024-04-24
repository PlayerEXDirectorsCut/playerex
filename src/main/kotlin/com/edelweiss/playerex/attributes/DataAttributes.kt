package com.edelweiss.playerex.attributes

import com.edelweiss.playerex.PlayerEXDirectorsCut.CHANNEL
import com.edelweiss.playerex.attributes.mutable.MutableAttributeContainer
import com.edelweiss.playerex.attributes.networking.Packets
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.resource.ResourceType

object DataAttributes {
    val MANAGER = AttributeManager()

    // Refreshes the attributes for an entity if it is a `LivingEntity`.
    fun refreshAttributes(entity: Entity) {
        if (entity is LivingEntity) (entity.attributes as MutableAttributeContainer).refresh()
    }

    fun initialize() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(MANAGER)

        ServerLoginConnectionEvents.QUERY_START.register { _, _, _, _ -> CHANNEL.clientHandle().send(Packets.Handshake(MANAGER.toNBT())) }
    }
}