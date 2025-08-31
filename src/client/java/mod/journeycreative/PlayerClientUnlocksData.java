package mod.journeycreative;

import mod.journeycreative.networking.PlayerUnlocksData;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public class PlayerClientUnlocksData {
    public static PlayerUnlocksData playerUnlocksData = new PlayerUnlocksData();

    public static boolean isUnlocked(ItemStack stack) {
        return playerUnlocksData.isUnlocked(stack);
    }
}
