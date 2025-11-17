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
                                            builder.suggest(player.getGameProfile().name())
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
        );
    }
}
