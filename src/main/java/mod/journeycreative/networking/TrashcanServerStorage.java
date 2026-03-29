package mod.journeycreative.networking;

import mod.journeycreative.screen.TrashcanInventory;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TrashcanServerStorage {
    private static final Map<UUID, TrashcanInventory> TRASH_CANS = new HashMap<>();

    public static TrashcanInventory get(Player player) {
        return TRASH_CANS.computeIfAbsent(player.getUUID(), id -> new TrashcanInventory());
    }

    public static void remove(Player player) {
        TRASH_CANS.remove(player.getUUID());
    }
}
