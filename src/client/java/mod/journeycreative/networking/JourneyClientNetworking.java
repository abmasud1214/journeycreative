package mod.journeycreative.networking;

import mod.journeycreative.Journeycreative;
import mod.journeycreative.PlayerClientUnlocksData;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

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

//    public static void unlockItem(Item item) {
//        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Registries.ITEM.getId(item));
//        ClientPlayNetworking.send(new JourneyNetworking.UnlockItemPayload(itemKey));
//    }
//
//    public static void unlockItem(RegistryKey<Item> itemKey) {
//        ClientPlayNetworking.send(new JourneyNetworking.UnlockItemPayload(itemKey));
//    }
}
