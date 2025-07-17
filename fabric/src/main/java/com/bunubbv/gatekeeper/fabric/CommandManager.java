package com.bunubbv.gatekeeper.fabric;

import static com.bunubbv.gatekeeper.fabric.PlayerControl.*;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Objects;
import java.util.UUID;

public class CommandManager {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(net.minecraft.server.command.CommandManager.literal("gk")
                .requires(source -> source.hasPermissionLevel(4))

                .then(net.minecraft.server.command.CommandManager.literal("bypass")
                        .then(net.minecraft.server.command.CommandManager.argument("player", StringArgumentType.word())
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

                                    PlayerState state = PlayerState.get(server);

                                    if (state.check(target.getUuid())) {
                                        context.getSource().sendError(Text.literal("Player " + targetName + " is already verified."));
                                        return 0;
                                    }

                                    state.insert(target.getUuid());

                                    context.getSource().sendFeedback(
                                            () -> Text.literal("Player " + targetName + " is now verified.").formatted(Formatting.GREEN), false
                                    );

                                    unfreeze(target);
                                    cancelKick(target);
                                    return 1;
                                })
                        )
                )

                .then(net.minecraft.server.command.CommandManager.literal("reload")
                        .executes(context -> {
                            ConfigManager.load();

                            context.getSource().sendFeedback(() ->
                                    Text.literal("Config reloaded successfully!").formatted(Formatting.GREEN), false);
                            return 1;
                        }))

                .then(net.minecraft.server.command.CommandManager.literal("revoke")
                        .then(net.minecraft.server.command.CommandManager.argument("player", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    var server = context.getSource().getServer();
                                    var state = PlayerState.get(server);

                                    for (UUID uuid : state.verifiedPlayers) {
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
                                    PlayerState state = PlayerState.get(server);

                                    if (!state.check(uuid)) {
                                        context.getSource().sendError(Text.literal("Player " + targetName + " is not verified."));
                                        return 0;
                                    }

                                    state.remove(uuid);
                                    context.getSource().sendFeedback(
                                            () -> Text.literal("Player " + targetName + " is no longer verified.").formatted(Formatting.YELLOW), false
                                    );

                                    ServerPlayerEntity target = server.getPlayerManager().getPlayer(uuid);
                                    if (target != null) {
                                        freeze(target);
                                        target.sendMessage(Text.literal(ConfigManager.welcomeMessage).formatted(Formatting.YELLOW), false);
                                        target.sendMessage(Text.literal(ConfigManager.question).formatted(Formatting.AQUA), false);
                                        scheduleKick(target);
                                    }

                                    return 1;
                                })
                        )
                )
        );
    }
}
