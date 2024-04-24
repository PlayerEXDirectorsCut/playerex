package com.edelweiss.playerex.mixin.cache;

import com.edelweiss.playerex.cache.PlayerEXCacheData;
import com.edelweiss.playerex.cache.PlayerEXCache;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.storage.SaveVersionInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelProperties.class)
abstract class LevelPropertiesMixin implements PlayerEXCacheData {
    @Unique
    private final PlayerEXCache playerEXCache = new PlayerEXCache();

    @Inject(method = "updateProperties", at = @At("HEAD"))
    private void playerex_updateProperties(DynamicRegistryManager registryManager, NbtCompound levelNbt, NbtCompound playerNbt, CallbackInfo ci) {
        levelNbt.put("PlayerEXCache", playerEXCache.writeToNbt());
    }

    @Inject(method = "readProperties", at = @At("RETURN"))
    private static void playerex_readProperties(Dynamic<NbtElement> dynamic, DataFixer dataFixer, int dataVersion, NbtCompound playerData, LevelInfo levelInfo, SaveVersionInfo saveVersionInfo, LevelProperties.SpecialProperty specialProperty, GeneratorOptions generatorOptions, Lifecycle lifecycle, CallbackInfoReturnable<LevelProperties> cir) {
        LevelProperties levelProperties = cir.getReturnValue();
        dynamic.get("PlayerEXCache").result().map(Dynamic::getValue).ifPresent(nbtElement -> ((PlayerEXCacheData) levelProperties).playerEXCache().readFromNbt((NbtList) nbtElement));
    }

    @Override
    public PlayerEXCache playerEXCache() {
        return this.playerEXCache;
    }
}
