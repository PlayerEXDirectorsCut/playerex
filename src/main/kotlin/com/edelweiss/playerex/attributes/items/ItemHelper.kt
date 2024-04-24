package com.edelweiss.playerex.attributes.items

import net.minecraft.item.ItemStack
import net.minecraft.sound.SoundEvent


/**
 * Helper interface to enable stack-specific operations. For example, using nbt
 * data for stack-specific attributes. Can be
 * used in tandem with FabricItem.
 *
 * @author CleverNucleus
 */
interface ItemHelper {
    /**
     * Fired on the constructor for itemstacks. All items are automatically an
     * instance of ItemHelper, so checks should be
     * made when using this to avoid running unnecessary logic on all itemstack
     * creation events. Example usage includes
     * attaching nbt data when an itemstack is first created.
     *
     * @param itemStack
     * @param count
     */
    fun onStackCreated(itemStack: ItemStack, count: Int) {
    }

    /**
     * ItemStack dependent version of SwordItem#getAttackDamage and
     * MiningToolItem#getAttackDamage. Default
     * implementation returns the aforementioned.
     *
     * @param itemStack
     * @return
     */
    fun getAttackDamage(itemStack: ItemStack): Float {
        return 0.0f
    }

    /**
     * ItemStack dependent version of ArmorItem#getProtection. Default
     * implementation returns aforementioned.
     *
     * @param itemStack
     * @return
     */
    fun getProtection(itemStack: ItemStack): Int {
        return 0
    }

    /**
     * ItemStack dependent version of ArmorItem#getToughness. Default implementation
     * returns aforementioned.
     *
     * @param itemStack
     * @return
     */
    fun getToughness(itemStack: ItemStack): Float {
        return 0.0f
    }

    /**
     * ItemStack dependent version of Item#getEquipSound. Default implementation
     * returns aforementioned.
     *
     * @param itemStack
     * @return
     */
    fun getEquipSound(itemStack: ItemStack): SoundEvent? {
        return null
    }
}