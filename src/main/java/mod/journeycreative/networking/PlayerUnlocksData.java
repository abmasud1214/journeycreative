package mod.journeycreative.networking;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

import java.util.HashSet;
import java.util.Set;

public class PlayerUnlocksData {
    private ImmutableSet<RegistryKey<Item>> unlockedItemKeys;

    public PlayerUnlocksData() {
        unlockedItemKeys = ImmutableSet.of();
    }

    public PlayerUnlocksData(ImmutableSet<RegistryKey<Item>> unlockedItemKeys) {
        this.unlockedItemKeys = unlockedItemKeys;
    }

    public ImmutableSet<RegistryKey<Item>> getUnlockedItemKeys() {
        return unlockedItemKeys;
    }

    public void setUnlockedItemKeys(ImmutableSet<RegistryKey<Item>> itemKeys) {
        unlockedItemKeys = itemKeys;
    }

    public boolean unlockItem(RegistryKey<Item> item) {
        if (unlockedItemKeys.contains(item)) {
            return false;
        } else {
            this.unlockedItemKeys = ImmutableSet.<RegistryKey<Item>>builder().addAll(this.unlockedItemKeys).add(item).build();
            return true;
        }
    }

    public static final Codec<PlayerUnlocksData> PLAYER_UNLOCKS_CODEC = RegistryKey.createCodec(RegistryKeys.ITEM)
            .listOf()
            .xmap(
                    ImmutableSet::copyOf,
                    ImmutableList::copyOf
            )
            .fieldOf("unlockedItems")
            .codec()
            .xmap(
                    PlayerUnlocksData::new,
                    PlayerUnlocksData::getUnlockedItemKeys
            );
}
