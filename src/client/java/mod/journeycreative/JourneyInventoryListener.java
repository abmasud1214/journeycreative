package mod.journeycreative;

import mod.journeycreative.networking.JourneyClientNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;

public class JourneyInventoryListener implements ScreenHandlerListener {
    private final MinecraftClient client;

    public JourneyInventoryListener(MinecraftClient client) {
        this.client = client;
    }

    public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stack) {
        JourneyClientNetworking.clickJourneyStack(stack, slotId);
    }

    public void onPropertyUpdate(ScreenHandler handler, int property, int value) {
    }
}
