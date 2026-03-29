package mod.journeycreative.networking;

import com.mojang.serialization.Codec;
import mod.journeycreative.Journeycreative;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.UUID;

public class StateSaverAndLoader extends SavedData {
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
        StateSaverAndLoader serverState = getServerState(player.level().getServer());

        PlayerUnlocksData playerState = serverState.players.computeIfAbsent(player.getUUID(), uuid -> new PlayerUnlocksData());

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


    private static SavedDataType<StateSaverAndLoader> type = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(Journeycreative.MOD_ID, "unlockdata"),
            StateSaverAndLoader::new,
            CODEC,
            null
    );

    public static StateSaverAndLoader getServerState(MinecraftServer server) {
        ServerLevel serverWorld = server.getLevel(Level.OVERWORLD);
        assert serverWorld != null;

        StateSaverAndLoader state = serverWorld.getDataStorage().computeIfAbsent(type);

        state.setDirty();

        return state;
    }
}
