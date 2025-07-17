package com.bunubbv.gatekeeper.fabric;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class EventHandlers {

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            PlayerState state = PlayerState.get(server);
            ServerPlayerEntity player = handler.getPlayer();

            if (!state.check(player.getUuid())) {
                PlayerControl.freeze(player);
                player.sendMessage(Text.literal(ConfigManager.welcomeMessage).formatted(Formatting.YELLOW), false);
                player.sendMessage(Text.literal(ConfigManager.question).formatted(Formatting.AQUA), false);
                PlayerControl.scheduleKick(player);
            }
        });

        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
            PlayerState state = PlayerState.get(GateKeeper.getServer());
            String msg = message.getContent().getString();

            if (!state.check(sender.getUuid())) {
                if (msg.equalsIgnoreCase(ConfigManager.answer)) {
                    state.insert(sender.getUuid());
                    PlayerControl.unfreeze(sender);
                    PlayerControl.cancelKick(sender);
                    sender.sendMessage(Text.literal(ConfigManager.correctMessage).formatted(Formatting.GREEN), false);
                } else {
                    sender.sendMessage(Text.literal(ConfigManager.incorrectMessage).formatted(Formatting.RED), false);
                }

                return false;
            }

            return true;
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            PlayerControl.tickKickTask();
            PlayerControl.enforceLock();
        });
    }
}
