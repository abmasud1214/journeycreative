package mod.journeycreative.keybinds;

import mod.journeycreative.networking.JourneyNetworking;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class KeyInputHandler {
    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (ModKeyBindings.ROTATE_INVENTORY.consumeClick()) {
                if (client.player != null) {
                    ClientPlayNetworking.send(new JourneyNetworking.RotateItemsPayload(false));
                }
            }
            while (ModKeyBindings.REVERSE_ROTATE_INVENTORY.consumeClick()) {
                if (client.player != null) {
                    ClientPlayNetworking.send(new JourneyNetworking.RotateItemsPayload(true));
                }
            }
        });
    }
}
