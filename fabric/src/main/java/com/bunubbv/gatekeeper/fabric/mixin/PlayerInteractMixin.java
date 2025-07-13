package com.bunubbv.gatekeeper.fabric.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.bunubbv.gatekeeper.fabric.GateKeeperState.isVerifiedPlayer;

@Mixin(ServerPlayerInteractionManager.class)
public class PlayerInteractMixin {

    @Final
    @Shadow
    protected ServerPlayerEntity player;

    @Inject(method = "interactBlock", at = @At("HEAD"), cancellable = true)
    private void preventBlockInteract(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        if (!isVerifiedPlayer(this.player)) {
            int slot = player.getInventory().getSelectedSlot();
            ItemStack itemStackCount = player.getInventory().getStack(slot);

            player.currentScreenHandler.getSlotIndex(player.getInventory(), slot).ifPresent(index -> player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(
                    player.currentScreenHandler.syncId,
                    player.currentScreenHandler.nextRevision(),
                    index,
                    itemStackCount
            )));

            cir.setReturnValue(ActionResult.FAIL);
        }
    }

    @Inject(method = "processBlockBreakingAction", at = @At("HEAD"), cancellable = true)
    private void preventBlockBreaking(BlockPos pos, PlayerActionC2SPacket.Action action, Direction direction, int worldHeight, int sequence, CallbackInfo ci) {
        if (!isVerifiedPlayer(player)) {
            ci.cancel();
        }
    }

    @Inject(method = "interactItem", at = @At("HEAD"), cancellable = true)
    private void preventItemInteract(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (!isVerifiedPlayer(player)) {
            cir.setReturnValue(ActionResult.FAIL);
        }
    }
}
