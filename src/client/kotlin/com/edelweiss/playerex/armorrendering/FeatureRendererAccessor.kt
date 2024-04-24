package com.edelweiss.playerex.armorrendering

import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer
import net.minecraft.client.render.entity.model.EntityModel
import net.minecraft.entity.LivingEntity

interface FeatureRendererAccessor<T: LivingEntity, M: EntityModel<T>> {
    fun getFeatureRenderer(): ArmorFeatureRenderer<*, *, *>
}