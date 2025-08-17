package mod.journeycreative.networking;

import com.google.common.collect.ImmutableSet;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.logging.LogUtils;
import mod.journeycreative.Journeycreative;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.argument.RegistryKeyArgumentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Cooldown;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JourneyNetworking {
    public static final Identifier GIVE_ITEM = Identifier.of(Journeycreative.MOD_ID, "give_item");
    public static final Identifier UNLOCK_ITEM = Identifier.of(Journeycreative.MOD_ID, "unlock_item");
    public static final Identifier SYNC_UNLOCKED_ITEMS = Identifier.of(Journeycreative.MOD_ID, "sync_unlock_item");
    public static final Identifier INITIAL_SYNC = Identifier.of(Journeycreative.MOD_ID, "initial_sync");
    static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<UUID, Cooldown> playerCreativeItemDropCooldowns = new HashMap<>();

    private static final DynamicCommandExceptionType INVALID_ITEM_EXCEPTION =
            new DynamicCommandExceptionType(id -> Text.literal("Unknown item: " + id));

    public static void registerClientPackets() {

    }

    public static void registerServerPackets() {
        giveItemPacket();
        unlockItemPacket();
        initialSync();
        unlockItemCommandEvent();
    }

    private static void giveItemPacket(){
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

    private static void unlockItemPacket() {
        PayloadTypeRegistry.playC2S().register(UnlockItemPayload.ID, UnlockItemPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(UnlockItemPayload.ID, (payload, context) -> {
            var player = context.player();
            var server = context.server();
            var item = payload.itemKey();

            context.server().execute(() -> {
//                StateSaverAndLoader serverState = StateSaverAndLoader.getServerState(server);

                PlayerUnlocksData playerState = StateSaverAndLoader.getPlayerState(player);
                boolean r = playerState.unlockItem(item);

                ServerPlayerEntity playerEntity = server.getPlayerManager().getPlayer(player.getUuid());
                server.execute(() -> {
                    ServerPlayNetworking.send(playerEntity, new SyncUnlockedItemsPayload(playerState));
                });
            });
        });
    }

    private static void unlockItemCommandEvent() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
           dispatcher.register(CommandManager.literal("unlockitem")
                   .requires(src -> src.hasPermissionLevel(2))
                   .then(CommandManager.argument("item", RegistryKeyArgumentType.registryKey(RegistryKeys.ITEM))
                           .executes(ctx -> unlockItemCommand(ctx))));
        });
    }

    private static int unlockItemCommand(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity player = ctx.getSource().getPlayer();

        RegistryKey<Item> itemRegistryKey = RegistryKeyArgumentType.getKey(ctx, "item", RegistryKeys.ITEM, INVALID_ITEM_EXCEPTION);

        StateSaverAndLoader state = StateSaverAndLoader.getServerState(source.getServer());
        PlayerUnlocksData playerState = StateSaverAndLoader.getPlayerState(player);

        if (playerState.unlockItem(itemRegistryKey)) {
            player.sendMessage(Text.literal("Unlocked item: " + itemRegistryKey.getValue()), false);
        } else {
            player.sendMessage(Text.literal("Item was already unlocked: " + itemRegistryKey.getValue()), false);
        }

        source.getServer().execute(() -> {
            ServerPlayNetworking.send(player, new SyncUnlockedItemsPayload(playerState));
        });

        return 1;
    }

    private static void initialSync() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            PlayerUnlocksData playerState = StateSaverAndLoader.getPlayerState(handler.getPlayer());
            server.execute(() -> {
                ServerPlayNetworking.send(handler.getPlayer(), new SyncUnlockedItemsPayload(playerState));
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

    public record UnlockItemPayload(RegistryKey<Item> itemKey) implements CustomPayload {
        public static final CustomPayload.Id<UnlockItemPayload> ID =
                new CustomPayload.Id(UNLOCK_ITEM);
        public static final PacketCodec<RegistryByteBuf, UnlockItemPayload> CODEC =
                PacketCodec.tuple(PacketCodecs.registryCodec(RegistryKey.createCodec(RegistryKeys.ITEM)), UnlockItemPayload::itemKey, UnlockItemPayload::new);

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record SyncUnlockedItemsPayload(PlayerUnlocksData playerUnlocksData) implements CustomPayload {
        public static final CustomPayload.Id<SyncUnlockedItemsPayload> ID =
                new CustomPayload.Id(SYNC_UNLOCKED_ITEMS);
        public static final PacketCodec<RegistryByteBuf, SyncUnlockedItemsPayload> CODEC =
                PacketCodec.tuple(PacketCodecs.registryCodec(PlayerUnlocksData.PLAYER_UNLOCKS_CODEC), SyncUnlockedItemsPayload::playerUnlocksData, SyncUnlockedItemsPayload::new);

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
}

