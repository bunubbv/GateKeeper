package com.bunubbv.gatekeeper.fabric;

import static com.bunubbv.gatekeeper.fabric.GateKeeper.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlayerControl {
    public static void freeze(ServerPlayerEntity player) {
        lockedPlayers.put(player.getUuid(), new PlayerLockPosition(player));
    }

    public static void unfreeze(ServerPlayerEntity player) {
        lockedPlayers.remove(player.getUuid());
    }

    public static void scheduleKick(ServerPlayerEntity player) {
        long when = getServer().getTicks() + (20L * 60 * ConfigManager.kickDelay);
        scheduledKicks.put(player.getUuid(), when);
    }

    public static void cancelKick(ServerPlayerEntity player) {
        scheduledKicks.remove(player.getUuid());
    }

    public static void tickKickTask() {
        MinecraftServer server = getServer();
        long currentTick = server.getTicks();

        scheduledKicks.entrySet().removeIf(entry -> {
            UUID uuid = entry.getKey();
            if (currentTick >= entry.getValue()) {
                ServerPlayerEntity player = server.getPlayerManager().getPlayer(uuid);
                if (player != null && !PlayerState.get(server).check(uuid)) {
                    player.networkHandler.disconnect(Text.literal(ConfigManager.kickMessage).formatted(Formatting.RED));
                }
                return true;
            }
            return false;
        });
    }

    public static void enforceLock() {
        MinecraftServer server = getServer();

        for (Map.Entry<UUID, PlayerLockPosition> entry : lockedPlayers.entrySet()) {
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(entry.getKey());
            if (player != null && !entry.getValue().isAtLocation(player)) {
                PlayerLockPosition pos = entry.getValue();
                player.teleport(
                        player.getWorld(),
                        pos.x, pos.y, pos.z,
                        Set.of(), pos.yaw, pos.pitch,
                        true
                );
            }
        }
    }
}
