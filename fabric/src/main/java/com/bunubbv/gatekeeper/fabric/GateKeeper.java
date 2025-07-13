package com.bunubbv.gatekeeper.fabric;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;

public class GateKeeper implements ModInitializer {

    private static MinecraftServer serverInstance;
    private static final Map<UUID, FrozenData> frozenDataMap = new HashMap<>();
    private static final Map<UUID, Long> kickSchedule = new HashMap<>();

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            serverInstance = server;
            GateKeeperConfig.load();
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            GateKeeperState state = GateKeeperState.get(server);
            ServerPlayerEntity player = handler.getPlayer();

            if (!state.isVerified(player.getUuid())) {
                freezePlayer(player);
                player.sendMessage(Text.literal(GateKeeperConfig.welcomeMessage).formatted(Formatting.YELLOW), false);
                player.sendMessage(Text.literal(GateKeeperConfig.question).formatted(Formatting.AQUA), false);
                scheduleKick(player);
            }
        });

        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
            GateKeeperState state = GateKeeperState.get(Objects.requireNonNull(sender.getServer()));
            String msg = message.getContent().getString();

            if (!state.isVerified(sender.getUuid())) {
                if (msg.equalsIgnoreCase(GateKeeperConfig.answer)) {
                    state.addVerifiedPlayer(sender.getUuid());
                    unfreezePlayer(sender);
                    cancelKick(sender);

                    sender.sendMessage(Text.literal(GateKeeperConfig.correctMessage).formatted(Formatting.GREEN), false);
                }
                else {
                    sender.sendMessage(Text.literal(GateKeeperConfig.incorrectMessage).formatted(Formatting.RED), false);
                }

                return false;
            }

            return true;
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            long currentTick = server.getTicks();
            Iterator<Map.Entry<UUID, Long>> it = kickSchedule.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<UUID, Long> entry = it.next();
                if (currentTick >= entry.getValue()) {
                    ServerPlayerEntity player = server.getPlayerManager().getPlayer(entry.getKey());
                    if (player != null && !GateKeeperState.get(server).isVerified(player.getUuid())) {
                        player.networkHandler.disconnect(Text.literal(GateKeeperConfig.kickMessage).formatted(Formatting.RED));
                    }
                    it.remove();
                }
            }

            for (Map.Entry<UUID, FrozenData> entry : frozenDataMap.entrySet()) {
                ServerPlayerEntity player = server.getPlayerManager().getPlayer(entry.getKey());
                if (player != null && !entry.getValue().isAtLocation(player)) {
                    FrozenData frozen = entry.getValue();
                    player.teleport(
                            (ServerWorld) player.getWorld(),
                            frozen.x, frozen.y, frozen.z,
                            Set.of(),
                            frozen.yaw, frozen.pitch,
                            true
                    );
                }
            }
        });

        CommandRegistrationCallback.EVENT.register(
                (
                        dispatcher, registryAccess, environment
                ) -> registerCommands(dispatcher)
        );
    }

    private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("gk")
                .requires(source -> source.hasPermissionLevel(4))

                .then(CommandManager.literal("bypass")
                        .then(CommandManager.argument("player", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    context.getSource().getServer().getPlayerManager().getPlayerList().forEach(player ->
                                            builder.suggest(player.getGameProfile().getName())
                                    );
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    String targetName = StringArgumentType.getString(context, "player");
                                    MinecraftServer server = context.getSource().getServer();
                                    ServerPlayerEntity target = server.getPlayerManager().getPlayer(targetName);

                                    if (target == null) {
                                        context.getSource().sendError(Text.literal("Player " + targetName + " is not online."));
                                        return 0;
                                    }

                                    GateKeeperState state = GateKeeperState.get(server);

                                    if (state.isVerified(target.getUuid())) {
                                        context.getSource().sendError(Text.literal("Player " + targetName + " is already verified."));
                                        return 0;
                                    }

                                    state.addVerifiedPlayer(target.getUuid());

                                    context.getSource().sendFeedback(
                                            () -> Text.literal("Player " + targetName + " is now verified.").formatted(Formatting.GREEN), false
                                    );

                                    unfreezePlayer(target);
                                    cancelKick(target);
                                    return 1;
                                })
                        )
                )

                .then(CommandManager.literal("reload")
                        .executes(context -> {
                            GateKeeperConfig.load();

                            context.getSource().sendFeedback(() ->
                                    Text.literal("Config reloaded successfully!").formatted(Formatting.GREEN), false);
                            return 1;
                        }))

                .then(CommandManager.literal("revoke")
                        .then(CommandManager.argument("player", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    var server = context.getSource().getServer();
                                    var state = GateKeeperState.get(server);

                                    for (UUID uuid : state.getVerifiedPlayers()) {
                                        Objects.requireNonNull(server.getUserCache()).getByUuid(uuid).ifPresent(profile ->
                                                builder.suggest(profile.getName())
                                        );
                                    }

                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    String targetName = StringArgumentType.getString(context, "player");
                                    MinecraftServer server = context.getSource().getServer();

                                    GameProfile profile = server.getUserCache().findByName(targetName).orElse(null);
                                    if (profile == null) {
                                        context.getSource().sendError(Text.literal("Player " + targetName + " not found or has never joined."));
                                        return 0;
                                    }

                                    UUID uuid = profile.getId();
                                    GateKeeperState state = GateKeeperState.get(server);

                                    if (!state.isVerified(uuid)) {
                                        context.getSource().sendError(Text.literal("Player " + targetName + " is not verified."));
                                        return 0;
                                    }

                                    state.removeVerifiedPlayer(uuid);
                                    context.getSource().sendFeedback(
                                            () -> Text.literal("Player " + targetName + " is no longer verified.").formatted(Formatting.YELLOW), false
                                    );

                                    ServerPlayerEntity target = server.getPlayerManager().getPlayer(uuid);
                                    if (target != null) {
                                        freezePlayer(target);
                                        target.sendMessage(Text.literal(GateKeeperConfig.welcomeMessage).formatted(Formatting.YELLOW), false);
                                        target.sendMessage(Text.literal(GateKeeperConfig.question).formatted(Formatting.AQUA), false);
                                        scheduleKick(target);
                                    }

                                    return 1;
                                })
                        )
                )
        );
    }

    private static void freezePlayer(ServerPlayerEntity player) {
        frozenDataMap.put(player.getUuid(), new FrozenData(player));
    }

    private static void unfreezePlayer(ServerPlayerEntity player) {
        frozenDataMap.remove(player.getUuid());
    }

    private static void scheduleKick(ServerPlayerEntity player) {
        long kickTick = serverInstance.getTicks() + (20L * 60 * GateKeeperConfig.kickDelay);
        kickSchedule.put(player.getUuid(), kickTick);
    }

    private static void cancelKick(ServerPlayerEntity player) {
        kickSchedule.remove(player.getUuid());
    }

    private static class FrozenData {
        final double x, y, z;
        final float yaw, pitch;

        FrozenData(ServerPlayerEntity player) {
            this.x = player.getX();
            this.y = player.getY();
            this.z = player.getZ();
            this.yaw = player.getYaw();
            this.pitch = player.getPitch();
        }

        boolean isAtLocation(ServerPlayerEntity player) {
            return Math.abs(player.getX() - x) < 0.1
                    && Math.abs(player.getY() - y) < 0.1
                    && Math.abs(player.getZ() - z) < 0.1;
        }
    }
}
