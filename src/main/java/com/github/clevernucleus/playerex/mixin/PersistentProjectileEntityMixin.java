package com.github.clevernucleus.playerex.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.clevernucleus.dataattributes_dc.api.DataAttributesAPI;
import com.github.clevernucleus.playerex.api.ExAPI;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

@Mixin(PersistentProjectileEntity.class)
abstract class PersistentProjectileEntityMixin extends ProjectileEntity {

    // Constructor for the mixin class
    private PersistentProjectileEntityMixin(EntityType<? extends ProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    // Injecting code into the onEntityHit method at a specific point
    @Inject(method = "onEntityHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Vec3d;length()D"))
    private void playerex_onEntityHit(EntityHitResult entityHitResult, CallbackInfo info) {
        // Cast this to PersistentProjectileEntity
        PersistentProjectileEntity persistentProjectileEntity = (PersistentProjectileEntity) (Object) this;

        // Get the owner of the projectile
        Entity owner = persistentProjectileEntity.getOwner();

        // Check if the owner is a living entity
        if (owner instanceof LivingEntity) {
            // Modify the critical hit chance using DataAttributesAPI
            DataAttributesAPI.ifPresent((LivingEntity) owner, ExAPI.RANGED_CRIT_CHANCE, 0, value -> {
                persistentProjectileEntity.setCritical(false);
                return 0;
            });
        }
    }

    // Modifying an argument in the onEntityHit method
    @ModifyArg(method = "onEntityHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"), index = -1)
    private float playerex_onEntityHit(float i) {
        // Cast this to PersistentProjectileEntity
        PersistentProjectileEntity persistentProjectileEntity = (PersistentProjectileEntity) (Object) this;
        // Get the owner of the projectile
        Entity owner = persistentProjectileEntity.getOwner();
        float damage = i;

        // Check if the owner is a living entity
        if (owner instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) owner;
            final float amount = damage;

            // Modify the critical hit chance and damage using DataAttributesAPI
            if (DataAttributesAPI.ifPresent(livingEntity, ExAPI.RANGED_CRIT_CHANCE,
                    persistentProjectileEntity.isCritical(), value -> {
                        boolean cache = livingEntity.getRandom().nextFloat() < value;
                        persistentProjectileEntity.setCritical(cache);
                        return cache;
                    })) {
                final long randomOffset = this.random.nextInt(Math.round(i) / 2 + 2);
                final float fallback = Math.min(randomOffset + (long) i, Integer.MAX_VALUE);
                damage = DataAttributesAPI.ifPresent(livingEntity, ExAPI.RANGED_CRIT_DAMAGE, fallback,
                        value -> (float) (amount * (1.0 + (10.0 * value))));
            }
        }

        return damage;
    }
}
