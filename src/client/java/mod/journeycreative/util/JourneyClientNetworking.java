package mod.journeycreative.util;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;

public class JourneyClientNetworking {
    public static void sendGiveItem(int slot, ItemStack stack) {
        ClientPlayNetworking.send(new JourneyNetworking.GiveItemPayload(slot, stack.copy()));
    }

    public static void clickJourneyStack(ItemStack stack, int slot) {
        sendGiveItem(slot, stack);
    }

    public static void dropJourneyStack(ItemStack stack, ClientPlayerEntity player) {
        if (!stack.isEmpty()) {
            sendGiveItem(-1, stack);
            player.getItemDropCooldown().increment();
        }
    }
}
