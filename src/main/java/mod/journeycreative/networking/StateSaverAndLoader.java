package mod.journeycreative.networking;

import com.mojang.serialization.Codec;
import mod.journeycreative.Journeycreative;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.UUID;

public class StateSaverAndLoader extends PersistentState {
    public HashMap<UUID, PlayerUnlocksData> players = new HashMap<>();

    private StateSaverAndLoader() {
    }

    private StateSaverAndLoader(HashMap<UUID, PlayerUnlocksData> players) {
        this.players = players;
    }

    public HashMap<UUID, PlayerUnlocksData> getPlayers() {
        return players;
    }

    public static PlayerUnlocksData getPlayerState(LivingEntity player) {
        StateSaverAndLoader serverState = getServerState(player.getWorld().getServer());

        PlayerUnlocksData playerState = serverState.players.computeIfAbsent(player.getUuid(), uuid -> new PlayerUnlocksData());

        return playerState;
    }

    public static final Codec<UUID> UUID_CODEC = Codec.STRING.xmap(UUID::fromString, UUID::toString);

    private static final Codec<HashMap<UUID, PlayerUnlocksData>> PLAYER_DATA_CODEC =
            Codec.unboundedMap(UUID_CODEC, PlayerUnlocksData.PLAYER_UNLOCKS_CODEC)
                    .xmap(HashMap::new, map -> map);

    public static final Codec<StateSaverAndLoader> CODEC =
            PLAYER_DATA_CODEC.xmap(
                    StateSaverAndLoader::new,
                    StateSaverAndLoader::getPlayers
            );


    private static PersistentStateType<StateSaverAndLoader> type = new PersistentStateType<>(
            (String) Journeycreative.MOD_ID,
            StateSaverAndLoader::new,
            CODEC,
            null
    );

    public static StateSaverAndLoader getServerState(MinecraftServer server) {
        ServerWorld serverWorld = server.getWorld(World.OVERWORLD);
        assert serverWorld != null;

        StateSaverAndLoader state = serverWorld.getPersistentStateManager().getOrCreate(type);

        state.markDirty();

        return state;
    }
}
