package mod.journeycreative.networking;

import mod.journeycreative.keybinds.KeyInputHandler;
import mod.journeycreative.screen.JourneyInventoryScreen;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;

public class JourneyClientNetworking {
    public static void sendGiveItem(int slot, ItemStack stack) {
//        if (PlayerClientUnlocksData.isUnlocked(stack)) {
            ClientPlayNetworking.send(new JourneyNetworking.GiveItemPayload(slot, stack.copy()));
//        } else {
//            return;
//        }
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

    public static void sendTrashcanUpdate(ItemStack stack) {
        ClientPlayNetworking.send(new JourneyNetworking.TrashCanPayload(stack));
    }

    public static void RegisterClientPackets(){
        ReceiveUnlockedItems();
        ReceiveResearchItemRule();
        KeyInputHandler.register();
        ReceiveTrashcanSync();
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
            MinecraftClient.getInstance().execute(() -> {
                Screen current = MinecraftClient.getInstance().currentScreen;
                if (current instanceof JourneyInventoryScreen screen) {
                    screen.getDeleteItemSlot().setStack(payload.stack());
                }
            });
        });
    }
}
