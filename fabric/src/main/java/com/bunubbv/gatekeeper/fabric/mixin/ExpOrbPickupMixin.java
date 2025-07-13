package com.bunubbv.gatekeeper.fabric.mixin;

import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.bunubbv.gatekeeper.fabric.GateKeeperState.isVerifiedPlayer;

@Mixin(ExperienceOrbEntity.class)
public class ExpOrbPickupMixin {

    @Inject(method = "onPlayerCollision", at = @At("HEAD"), cancellable = true)
    private void preventExpOrbPickup(PlayerEntity player, CallbackInfo ci) {
        if (player instanceof ServerPlayerEntity serverPlayer && !isVerifiedPlayer(serverPlayer)) {
            ci.cancel();
        }
    }
}
