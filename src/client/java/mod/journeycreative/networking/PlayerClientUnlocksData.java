package mod.journeycreative.networking;

import net.minecraft.item.ItemStack;

public class PlayerClientUnlocksData {
    public static PlayerUnlocksData playerUnlocksData = new PlayerUnlocksData();

    public static boolean isUnlocked(ItemStack stack) {
        return playerUnlocksData.isUnlocked(stack, ClientGameRule.isResearchItemsUnlocked());
    }
}
