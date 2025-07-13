package com.bunubbv.gatekeeper.fabric.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.bunubbv.gatekeeper.fabric.GateKeeperState.isVerifiedPlayer;

@Mixin(Slot.class)
public class SlotMixin {

    @Inject(method = "canTakeItems", at = @At("HEAD"), cancellable = true)
    private void preventTakeItem(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        if (!isVerifiedPlayer((ServerPlayerEntity) player)) {
            cir.setReturnValue(false);
        }
    }
}
