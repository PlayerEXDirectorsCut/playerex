package com.edelweiss.playerex.attributes.items

import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.item.Item

class ItemAccessor : Item(FabricItemSettings()) {
    companion object {
        val attackDamageModifierID get() = ATTACK_DAMAGE_MODIFIER_ID
        val attackSpeedModifierID get() = ATTACK_SPEED_MODIFIER_ID
    }
}