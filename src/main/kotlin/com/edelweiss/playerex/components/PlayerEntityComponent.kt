package com.edelweiss.playerex.components

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent
import dev.onyxstudios.cca.api.v3.entity.PlayerComponent
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound

class PlayerEntityComponent(private val provider: PlayerEntity) : PlayerComponent<AutoSyncedComponent>, AutoSyncedComponent {
    companion object {}

    override fun readFromNbt(tag: NbtCompound) {

    }

    override fun writeToNbt(tag: NbtCompound) {

    }
}