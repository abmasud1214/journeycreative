package mod.journeycreative.screen;

import mod.journeycreative.networking.JourneyClientNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;

public class JourneyInventoryListener implements ContainerListener {
    private final Minecraft client;

    public JourneyInventoryListener(Minecraft client) {
        this.client = client;
    }

    public void slotChanged(AbstractContainerMenu handler, int slotId, ItemStack stack) {
        JourneyClientNetworking.clickJourneyStack(stack, slotId);
    }

    public void dataChanged(AbstractContainerMenu handler, int property, int value) {
    }
}
