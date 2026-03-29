package mod.journeycreative.networking;

import mod.journeycreative.keybinds.KeyInputHandler;
import mod.journeycreative.screen.JourneyInventoryScreen;
import mod.journeycreative.screen.ResearchVesselScreenHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class JourneyClientNetworking {
    public static void sendGiveItem(int slot, ItemStack stack) {
        ClientPlayNetworking.send(new JourneyNetworking.GiveItemPayload(slot, stack.copy()));
    }

    public static void clickJourneyStack(ItemStack stack, int slot) {
        sendGiveItem(slot, stack);
    }

    public static void dropJourneyStack(ItemStack stack, LocalPlayer player) {
        if (!stack.isEmpty()) {
            sendGiveItem(-1, stack);
            player.getDropSpamThrottler().increment();
        }
    }

    public static void sendTrashcanUpdate(ItemStack stack) {
        ClientPlayNetworking.send(new JourneyNetworking.TrashCanPayload(stack));
    }

    public static void RegisterClientPackets(){
        ReceiveUnlockedItems();
        ReceiveResearchItemRule();
        KeyInputHandler.register();
        ReceiveTrashcanSync();
        ReceiveWarning();
    }

    private static void ReceiveUnlockedItems(){
        ClientPlayNetworking.registerGlobalReceiver(JourneyNetworking.SyncUnlockedItemsPayload.ID, (payload, context) -> {
            PlayerUnlocksData playerUnlocksData = payload.playerUnlocksData();
            context.client().execute(() -> {
                PlayerClientUnlocksData.playerUnlocksData = playerUnlocksData;
            });
        });
    }

    private static void ReceiveResearchItemRule() {
        ClientPlayNetworking.registerGlobalReceiver(JourneyNetworking.SyncResearchItemsUnlockRulePayload.ID, (payload, context) -> {
            boolean value = payload.value();
            context.client().execute(() -> {
                ClientGameRule.setResearchItemsUnlocked(value);
            });
        });
    }

    private static void ReceiveTrashcanSync() {
        ClientPlayNetworking.registerGlobalReceiver(JourneyNetworking.SyncTrashCanPayload.ID, (payload, context) -> {
            Minecraft.getInstance().execute(() -> {
                Screen current = Minecraft.getInstance().screen;
                if (current instanceof JourneyInventoryScreen screen) {
                    screen.getDeleteItemSlot().setByPlayer(payload.stack());
                }
            });
        });
    }

    private static void ReceiveWarning() {
        ClientPlayNetworking.registerGlobalReceiver(JourneyNetworking.ItemWarningMessage.ID, (payload, context) -> {
            context.client().execute(() -> {
                AbstractContainerMenu handler = context.client().player.containerMenu;

                if (!(handler instanceof ResearchVesselScreenHandler rvh)) return;

                rvh.setWarning(payload.warningMessage());
            });
        });
    }
}
