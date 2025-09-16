package mod.journeycreative.networking;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ClientGameRule {
    private static boolean researchItemsUnlocked = false;

    public static void setResearchItemsUnlocked(boolean value) {
        researchItemsUnlocked = value;
    }

    public static boolean isResearchItemsUnlocked() {
        return researchItemsUnlocked;
    }
}
