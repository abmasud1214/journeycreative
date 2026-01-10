package mod.journeycreative.networking;

import mod.journeycreative.Journeycreative;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.UUID;

public class StateSaverAndLoader extends PersistentState {
    public HashMap<UUID, PlayerUnlocksData> players = new HashMap<>();

    private StateSaverAndLoader() {
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        NbtCompound playersNbt = new NbtCompound();

        players.forEach(((uuid, playerUnlocksData) -> {
            playersNbt.put(uuid.toString(), playerUnlocksData.toNbt(registries));
        }));

        nbt.put("players", playersNbt);
        return nbt;
    }

    public static StateSaverAndLoader createFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        StateSaverAndLoader state = new StateSaverAndLoader();
        NbtCompound playersNbt = nbt.getCompound("players");

        for (String key : playersNbt.getKeys()) {
            UUID uuid = UUID.fromString(key);
            PlayerUnlocksData data = PlayerUnlocksData.fromNbt(playersNbt.getCompound(key), registries);
            state.players.put(uuid, data);
        }
        return state;
    }

    public static PlayerUnlocksData getPlayerState(LivingEntity player) {
        StateSaverAndLoader serverState = getServerState(player.getWorld().getServer());

        PlayerUnlocksData playerState = serverState.players.computeIfAbsent(player.getUuid(), uuid -> new PlayerUnlocksData());

        return playerState;
    }

    private static Type<StateSaverAndLoader> type = new Type<>(
            StateSaverAndLoader::new,
            StateSaverAndLoader::createFromNbt,
            null
    );

    public static StateSaverAndLoader getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();

        StateSaverAndLoader state = persistentStateManager.getOrCreate(type, Journeycreative.MOD_ID);

        state.markDirty();

        return state;
    }
}
