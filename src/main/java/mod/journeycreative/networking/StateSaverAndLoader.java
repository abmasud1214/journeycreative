package mod.journeycreative.networking;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mod.journeycreative.Journeycreative;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.PersistentStateType;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
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

//    private static final Codec<RegistryKey<Item>> ITEM_KEY_CODEC = RegistryKey.createCodec(RegistryKeys.ITEM);

//    public static final Codec<StateSaverAndLoader> CODEC = ITEM_KEY_CODEC
//            .listOf()
//            .xmap(
//                    ImmutableSet::copyOf,
//                    ImmutableList::copyOf
//            )
//            .fieldOf("unlockedItems")
//            .codec()
//            .xmap(
//                    StateSaverAndLoader::new,
//                    StateSaverAndLoader::getUnlockedItems
//            );

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
