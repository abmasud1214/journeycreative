package mod.journeycreative.networking;

import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Cooldown;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JourneyNetworking {
    public static final Identifier GIVE_ITEM = Identifier.of("journeycreative", "give_item");

    static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<UUID, Cooldown> playerCreativeItemDropCooldowns = new HashMap<>();

    public static void registerClientPackets() {

    }

    public static void registerServerPackets() {
        PayloadTypeRegistry.playC2S().register(GiveItemPayload.ID, GiveItemPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(GiveItemPayload.ID, (payload, context) -> {
            var player = context.player();
            int slot = payload.slot();
            var stack = payload.stack();
            boolean bl = slot < 0;
            //TODO: return here if item is not unlocked

            boolean bl2 = slot >= 1 && slot <= 45;
            boolean bl3 = stack.isEmpty() || stack.getCount() <= stack.getMaxCount();

            context.server().execute(() -> {
//               JourneyUnlocks unlocks = ((JourneyPlayerAccess) player).getJourneyUnlocks();
//               if (unlocks.isUnlocked(stack.getItem())) {
//               }
                UUID uuid = player.getUuid();
                playerCreativeItemDropCooldowns.putIfAbsent(uuid, new Cooldown(20, 1480));
                Cooldown cooldown = playerCreativeItemDropCooldowns.get(uuid);

                if (bl2 && bl3) {
                    player.playerScreenHandler.getSlot(slot).setStack(stack);
                    player.playerScreenHandler.setReceivedStack(slot, stack);
                    player.playerScreenHandler.sendContentUpdates();
                } else if (bl && bl3) {
                    if (cooldown.canUse()) {
                        cooldown.increment();
                        player.dropItem(stack, true);
                    } else {
                        LOGGER.warn("Player {} was dropping items too fast in journey mode, ignoring.", player.getName().getString());
                    }
                }
            });
        });
    }

    public record GiveItemPayload(int slot, ItemStack stack) implements CustomPayload {
        public static final CustomPayload.Id<GiveItemPayload> ID =
                new CustomPayload.Id<>(GIVE_ITEM);

        public static final PacketCodec<RegistryByteBuf, GiveItemPayload> CODEC =
                PacketCodec.tuple(PacketCodecs.INTEGER, GiveItemPayload::slot, ItemStack.OPTIONAL_PACKET_CODEC, GiveItemPayload::stack, GiveItemPayload::new);

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

}

