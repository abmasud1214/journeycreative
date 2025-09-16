package mod.journeycreative.networking;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;

public class JourneyClientNetworking {
    public static void sendGiveItem(int slot, ItemStack stack) {
        if (PlayerClientUnlocksData.isUnlocked(stack)) {
            ClientPlayNetworking.send(new JourneyNetworking.GiveItemPayload(slot, stack.copy()));
        } else {
            return;
        }
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

    public static void RegisterClientPackets(){
        ReceiveUnlockedItems();
        ReceiveResearchItemRule();
    }

    private static void ReceiveUnlockedItems(){
        PayloadTypeRegistry.playS2C().register(JourneyNetworking.SyncUnlockedItemsPayload.ID, JourneyNetworking.SyncUnlockedItemsPayload.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(JourneyNetworking.SyncUnlockedItemsPayload.ID, (payload, context) -> {
            PlayerUnlocksData playerUnlocksData = payload.playerUnlocksData();
            context.client().execute(() -> {
                PlayerClientUnlocksData.playerUnlocksData = playerUnlocksData;
            });
        });
    }

    private static void ReceiveResearchItemRule() {
        PayloadTypeRegistry.playS2C().register(JourneyNetworking.SyncResearchItemsUnlockRulePayload.ID,
                JourneyNetworking.SyncResearchItemsUnlockRulePayload.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(JourneyNetworking.SyncResearchItemsUnlockRulePayload.ID, (payload, context) -> {
            boolean value = payload.value();
            context.client().execute(() -> {
                ClientGameRule.setResearchItemsUnlocked(value);
            });
        });
    }
}
