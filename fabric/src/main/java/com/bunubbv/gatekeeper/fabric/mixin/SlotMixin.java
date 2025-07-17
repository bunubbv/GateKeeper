package com.bunubbv.gatekeeper.fabric.mixin;

import com.bunubbv.gatekeeper.fabric.PlayerState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public class SlotMixin {

    @Inject(method = "canTakeItems", at = @At("HEAD"), cancellable = true)
    private void preventTakeItem(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        if (!PlayerState.check(player)) {
            cir.setReturnValue(false);
        }
    }
}
