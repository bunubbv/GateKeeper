package com.bunubbv.gatekeeper.fabric;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.PersistentStateType;
import net.minecraft.world.World;

import java.util.*;

public class GateKeeperState extends PersistentState {

    public final Set<UUID> verifiedPlayers = new HashSet<>();
    public static final Codec<GateKeeperState> CODEC;
    public static final PersistentStateType<GateKeeperState> TYPE;

    static {
        CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.STRING.listOf().fieldOf("verified").forGetter(
                                state -> state.verifiedPlayers.stream().map(UUID::toString).toList()
                        )
                ).apply(instance, list -> {
                    GateKeeperState state = new GateKeeperState();
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
                ctx -> new GateKeeperState(),
                ctx -> CODEC,
                DataFixTypes.LEVEL
        );
    }

    public GateKeeperState() {
        super();
    }

    public static GateKeeperState get(MinecraftServer server) {
        ServerWorld world = server.getWorld(World.OVERWORLD);
        PersistentStateManager manager = Objects.requireNonNull(world).getPersistentStateManager();
        return manager.getOrCreate(TYPE);
    }

    public Set<UUID> getVerifiedPlayers() {
        return Collections.unmodifiableSet(verifiedPlayers);
    }

    public void addVerifiedPlayer(UUID uuid) {
        verifiedPlayers.add(uuid);
        markDirty();
    }

    public void removeVerifiedPlayer(UUID uuid) {
        verifiedPlayers.remove(uuid);
        markDirty();
    }

    public boolean isVerified(UUID uuid) {
        return verifiedPlayers.contains(uuid);
    }

    public static boolean isVerifiedPlayer(ServerPlayerEntity player) {
        return GateKeeperState.get(
                Objects.requireNonNull(player.getServer())
                ).verifiedPlayers.contains(player.getUuid());
    }

    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList list = new NbtList();
        for (UUID uuid : verifiedPlayers) {
            list.add(NbtString.of(uuid.toString()));
        }
        nbt.put("verified", list);
        return nbt;
    }

    public static GateKeeperState fromNbt(NbtCompound nbt) {
        GateKeeperState state = new GateKeeperState();

        if (nbt.contains("verified")) {
            NbtList list = (NbtList) nbt.get("verified");

            for (int i = 0; i < Objects.requireNonNull(list).size(); i++) {
                list.getString(i).ifPresent(str -> {
                    try {
                        state.verifiedPlayers.add(UUID.fromString(str));
                    } catch (IllegalArgumentException ignored) {}
                });
            }
        }
        return state;
    }
}
