package com.edelweiss.playerex.armorrendering

import com.edelweiss.playerex.mixin.ArmorFeatureRendererInvoker
import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer
import net.fabricmc.fabric.impl.client.rendering.ArmorRendererRegistryImpl
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.model.BipedEntityModel
import net.minecraft.client.render.item.ItemRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ArmorItem
import net.minecraft.item.Item
import net.minecraft.item.ItemConvertible
import net.minecraft.item.ItemStack
import net.minecraft.item.trim.ArmorTrim

object ArmorRenderingAPI {
    private val RENDERERS: Multimap<Item, ArmorRenderingLayer> = ArrayListMultimap.create()

    /**
     * By utilizing the Fabric Renderer, this function registers the provided items with the given `ArmorRenderingLayer`.
     * */
    fun register(layer: ArmorRenderingLayer, vararg items: ItemConvertible) {
        for (item in items) {
            ArmorRendererRegistryImpl.get(item.asItem()) ?: ArmorRenderer.register(ArmorRenderingAPI::render, item)
            RENDERERS.put(item.asItem(), layer)
        }
    }

    /** This is the function that implements the rendering process. */
    private fun render(matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, stack: ItemStack, entity: LivingEntity, slot: EquipmentSlot, light: Int, ctxModel: BipedEntityModel<LivingEntity>) {
        if (stack.item !is ArmorItem) return

        val item = stack.item as ArmorItem

        if (item.slotType != slot) return

        val entityRenderer = MinecraftClient.getInstance().entityRenderDispatcher.getRenderer(entity)
        val armorFeatureRenderer = (entityRenderer as FeatureRendererAccessor<LivingEntity, BipedEntityModel<LivingEntity>>).getFeatureRenderer()
        val model = (armorFeatureRenderer as ArmorTextureCache<BipedEntityModel<LivingEntity>>).getCustomArmor(slot)

        ctxModel.copyBipedStateTo(model)
        (armorFeatureRenderer as ArmorFeatureRendererInvoker<LivingEntity, BipedEntityModel<LivingEntity>, BipedEntityModel<LivingEntity>>).invokeSetVisible(model, slot)

        for (layer in RENDERERS.get(item)) {
            layer.render(stack, entity, slot).from { path, color, glint ->
                val red = (color shr 16 and 0xFF).toFloat() / 255.0F
                val green = (color shr 8 and 0xFF).toFloat() / 255.0F
                val blue = (color and 0xFF).toFloat() / 255.0F

                val vertexConsumer = ItemRenderer.getArmorGlintConsumer(
                    vertexConsumers,
                    RenderLayer.getArmorCutoutNoCull((armorFeatureRenderer as ArmorTextureCache<BipedEntityModel<LivingEntity>>).getOrCache(path)),
                    false, glint
                )

                model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, red, green, blue, 1.0F)
            }
        }

//        // todo: May have to revisit, for now this will be my solution.
//        ArmorTrim.getTrim(entity.world.registryManager, stack).ifPresent { trim ->
//            if (FabricLoader.getInstance().isModLoaded("allthetrims")) {
//                DynamicTrimRenderer.renderTrim(item.material, matrices, vertexConsumers, light, trim, model, slot == EquipmentSlot.LEGS)
//            }
//        }
    }
}