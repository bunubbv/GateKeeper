package com.bunubbv.gatekeeper.fabric.mixin;

import com.bunubbv.gatekeeper.fabric.PlayerState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PersistentProjectileEntity.class)
public class PersistentProjectileEntityMixin {

    @Inject(method = "onPlayerCollision", at = @At("HEAD"), cancellable = true)
    private void preventArrowPickup(PlayerEntity player, CallbackInfo ci) {
        if (!PlayerState.check(player)) {
            ci.cancel();
        }
    }
}
