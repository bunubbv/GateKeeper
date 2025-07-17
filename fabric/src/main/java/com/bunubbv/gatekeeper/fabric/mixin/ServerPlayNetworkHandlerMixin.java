package com.bunubbv.gatekeeper.fabric.mixin;

import com.bunubbv.gatekeeper.fabric.PlayerState;
import net.minecraft.network.packet.c2s.play.ChatCommandSignedC2SPacket;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

    @Inject(method = "onCommandExecution", at = @At("HEAD"), cancellable = true)
    private void preventUnSignedCommand(CommandExecutionC2SPacket packet, CallbackInfo ci) {
        ServerPlayNetworkHandler handler = (ServerPlayNetworkHandler)(Object)this;
        ServerPlayerEntity player = handler.player;

        if (!PlayerState.check(player)) {
            ci.cancel();
        }
    }

    @Inject(method = "onChatCommandSigned", at = @At("HEAD"), cancellable = true)
    private void preventSignedCommand(ChatCommandSignedC2SPacket packet, CallbackInfo ci) {
        ServerPlayNetworkHandler handler = (ServerPlayNetworkHandler)(Object)this;
        ServerPlayerEntity player = handler.player;

        if (!PlayerState.check(player)) {
            ci.cancel();
        }
    }
}
