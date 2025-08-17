package mod.journeycreative;

import mod.journeycreative.networking.PlayerUnlocksData;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public class PlayerClientUnlocksData {
    public static PlayerUnlocksData playerUnlocksData = new PlayerUnlocksData();

    public static boolean isUnlocked(Item item) {
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Registries.ITEM.getId(item));
        return playerUnlocksData.getUnlockedItemKeys().contains(itemKey);
    }

    public static boolean isUnlocked(RegistryKey<Item> itemKey) {
        return playerUnlocksData.getUnlockedItemKeys().contains(itemKey);

    }
}
