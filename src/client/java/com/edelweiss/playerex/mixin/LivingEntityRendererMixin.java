package com.edelweiss.playerex.mixin;

import com.edelweiss.playerex.armorrendering.FeatureRendererAccessor;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> implements FeatureRendererAccessor<T, M> {

    @Unique
    private FeatureRenderer<T, M> ar_armorFeature;

    @Inject(method = "addFeature", at = @At("HEAD"), cancellable = true)
    private void ar_addFeature(FeatureRenderer<T, M> feature, CallbackInfoReturnable<Boolean> info) {
        if(feature instanceof ArmorFeatureRenderer) {
            this.ar_armorFeature = feature;
        }
    }

    @Override
    public ArmorFeatureRenderer<?, ?, ?> getFeatureRenderer() {
        return (ArmorFeatureRenderer<?, ?, ?>)this.ar_armorFeature;
    }
}