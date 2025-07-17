package com.bunubbv.gatekeeper.fabric;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.PersistentStateType;
import net.minecraft.world.World;

import java.util.*;

public class PlayerState extends PersistentState {

    public final Set<UUID> verifiedPlayers = new HashSet<>();
    public static final Codec<PlayerState> CODEC;
    public static final PersistentStateType<PlayerState> TYPE;

    static {
        CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.STRING.listOf().fieldOf("verified").forGetter(
                                state -> state.verifiedPlayers.stream().map(UUID::toString).toList()
                        )
                ).apply(instance, list -> {
                    PlayerState state = new PlayerState();
                    for (String uuidStr : list) {
                        try {
                            state.verifiedPlayers.add(UUID.fromString(uuidStr));
                        } catch (IllegalArgumentException ignored) {}
                    }
                    return state;
                })
        );

        TYPE = new PersistentStateType<>(
                "gatekeeper",
                ctx -> new PlayerState(),
                ctx -> CODEC,
                DataFixTypes.LEVEL
        );
    }

    public PlayerState() {
        super();
    }

    public static PlayerState get(MinecraftServer server) {
        ServerWorld world = server.getWorld(World.OVERWORLD);
        PersistentStateManager manager = Objects.requireNonNull(world).getPersistentStateManager();
        return manager.getOrCreate(TYPE);
    }

    public void insert(UUID uuid) {
        verifiedPlayers.add(uuid);
        markDirty();
    }

    public void remove(UUID uuid) {
        verifiedPlayers.remove(uuid);
        markDirty();
    }

    public boolean check(UUID uuid) {
        return verifiedPlayers.contains(uuid);
    }

    public static boolean check(PlayerEntity player) {
        return get(player.getServer()).check(player.getUuid());
    }
}
