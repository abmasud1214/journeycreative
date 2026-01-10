package mod.journeycreative.networking;

import mod.journeycreative.screen.TrashcanInventory;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TrashcanServerStorage {
    private static final Map<UUID, TrashcanInventory> TRASH_CANS = new HashMap<>();

    public static TrashcanInventory get(PlayerEntity player) {
        return TRASH_CANS.computeIfAbsent(player.getUuid(), id -> new TrashcanInventory());
    }

    public static void remove(PlayerEntity player) {
        TRASH_CANS.remove(player.getUuid());
    }
}
