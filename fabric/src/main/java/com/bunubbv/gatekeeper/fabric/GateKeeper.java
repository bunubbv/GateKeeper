package com.bunubbv.gatekeeper.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.*;

public class GateKeeper implements ModInitializer {
    private static MinecraftServer serverInstance;
    static final Map<UUID, PlayerLockPosition> lockedPlayers = new HashMap<>();
    static final Map<UUID, Long> scheduledKicks = new HashMap<>();

    public static MinecraftServer getServer() {
        return serverInstance;
    }

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            serverInstance = server;
            ConfigManager.load();
        });
    }

    public static class PlayerLockPosition {
        public final double x, y, z;
        public final float yaw, pitch;

        public PlayerLockPosition(ServerPlayerEntity player) {
            this.x = player.getX();
            this.y = player.getY();
            this.z = player.getZ();
            this.yaw = player.getYaw();
            this.pitch = player.getPitch();
        }

        public boolean isAtLocation(ServerPlayerEntity player) {
            return Math.abs(player.getX() - x) < 0.1
                    && Math.abs(player.getY() - y) < 0.1
                    && Math.abs(player.getZ() - z) < 0.1;
        }
    }
}
