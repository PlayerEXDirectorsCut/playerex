package com.edelweiss.playerex.mixin;

import com.edelweiss.playerex.armorrendering.ArmorTextureCache;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(ArmorFeatureRenderer.class)
abstract class ArmorFeatureRendererMixin<T extends LivingEntity, M extends BipedEntityModel<T>, A extends BipedEntityModel<T>> implements ArmorTextureCache<A> {

    @Shadow
    @Final
    private static Map<String, Identifier> ARMOR_TEXTURE_CACHE;

    @Shadow
    @Final
    private A innerModel;

    @Shadow
    @Final
    private A outerModel;

    /*
     * We do this instead of just an invoker because Geckolib Injects into #getArmor and runs an instanced set/get exiting the method,
     * which causes the game to crash anytime that method is accessed from anywhere else - and we want to be compatible with Geckolib.
     */
    @Override
    public A getCustomArmor(EquipmentSlot slot) {
        return slot == EquipmentSlot.LEGS ? this.innerModel : this.outerModel;
    }

    @Override
    public Identifier getOrCache(final String path) {
        return ARMOR_TEXTURE_CACHE.computeIfAbsent(path, Identifier::new);
    }
}