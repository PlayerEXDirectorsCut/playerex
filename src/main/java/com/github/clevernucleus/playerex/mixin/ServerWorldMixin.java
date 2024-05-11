package com.github.clevernucleus.playerex.mixin;

import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.server.world.ServerWorld;

@Mixin(ServerWorld.class)
abstract class ServerWorldMixin {
    
    // Redirect the "warn" method call in the "addEntity" method
    @Redirect(method = "addEntity", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;)V"))
    private void playerex_addEntity(Logger logger, String arg0, Object arg1) {
        // Redirecting the "warn" method call to an empty method effectively disables the warning
        // This is a way to suppress or alter log messages during entity addition
        // You can customize the behavior based on your requirements
        // The parameters (logger, arg0, arg1) are the original parameters of the "warn" method
        // and can be used if you want to perform specific actions or logging.
    }
}
