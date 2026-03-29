package mod.journeycreative.networking;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import mod.journeycreative.Journeycreative;
import mod.journeycreative.screen.TrashcanInventory;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.TickThrottler;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

import java.util.*;
import java.util.List;

public class JourneyNetworking {
    public static final Identifier GIVE_ITEM = Identifier.fromNamespaceAndPath(Journeycreative.MOD_ID, "give_item");
    public static final Identifier UNLOCK_ITEM = Identifier.fromNamespaceAndPath(Journeycreative.MOD_ID, "unlock_item");
    public static final Identifier TRASH_CAN = Identifier.fromNamespaceAndPath(Journeycreative.MOD_ID, "trash_can");
    public static final Identifier SYNC_UNLOCKED_ITEMS = Identifier.fromNamespaceAndPath(Journeycreative.MOD_ID, "sync_unlock_item");
    public static final Identifier SYNC_TRASH_CAN = Identifier.fromNamespaceAndPath(Journeycreative.MOD_ID, "sync_trash_can");
    public static final Identifier SYNC_RESEARCH_ITEMS_UNLOCKED_RULE = Identifier.fromNamespaceAndPath(Journeycreative.MOD_ID, "sync_research_rule");
    public static final Identifier ROTATE_ITEMS = Identifier.fromNamespaceAndPath(Journeycreative.MOD_ID, "rotate_items");
    public static final Identifier SEND_ITEM_WARNING_MESSAGE = Identifier.fromNamespaceAndPath(Journeycreative.MOD_ID, "send_item_warning_message");

    static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<UUID, TickThrottler> playerCreativeItemDropCooldowns = new HashMap<>();

    public static void registerClientPackets() {
        PayloadTypeRegistry.clientboundPlay().register(JourneyNetworking.SyncUnlockedItemsPayload.ID, JourneyNetworking.SyncUnlockedItemsPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(JourneyNetworking.SyncResearchItemsUnlockRulePayload.ID,
                JourneyNetworking.SyncResearchItemsUnlockRulePayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(JourneyNetworking.SyncTrashCanPayload.ID,
                JourneyNetworking.SyncTrashCanPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(JourneyNetworking.ItemWarningMessage.ID,
                JourneyNetworking.ItemWarningMessage.CODEC);
    }

    public static void registerServerPackets() {
        giveItemPacket();
        unlockItemPacket();
        initialSync();
        unlockItemCommandEvent();
        rotateItemsPacket();
        trashCanPacket();
    }

    public static void tick() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            playerCreativeItemDropCooldowns.values().forEach(TickThrottler::tick);
        });
    }

    public static void rotateItemsPacket() {
        PayloadTypeRegistry.serverboundPlay().register(RotateItemsPayload.ID, RotateItemsPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(RotateItemsPayload.ID, (payload, context) -> {
            Player player = context.player();
            Inventory inv = player.getInventory();

            boolean reversed = payload.reversed();

            List<ItemStack> hotbar = new ArrayList<>();
            List<ItemStack> row1 = new ArrayList<>();
            List<ItemStack> row2 = new ArrayList<>();
            List<ItemStack> row3 = new ArrayList<>();

            for (int i = 0; i < 9; i++) hotbar.add(inv.getItem(i));
            for (int i = 9; i < 18; i++) row1.add(inv.getItem(i));
            for (int i = 18; i < 27; i++) row2.add(inv.getItem(i));
            for (int i = 27; i < 36; i++) row3.add(inv.getItem(i));

            // Rotate
            if (!reversed) {
                for (int i = 0; i < 9; i++) {
                    inv.setItem(i, row3.get(i));       // hotbar <- row3
                    inv.setItem(i + 9, hotbar.get(i)); // row1 <- hotbar
                    inv.setItem(i + 18, row1.get(i));  // row2 <- row1
                    inv.setItem(i + 27, row2.get(i));  // row3 <- row2
                }
            } else {
                for (int i = 0; i < 9; i++) {
                    inv.setItem(i, row1.get(i)); // hotbar <- row1
                    inv.setItem(i + 9, row2.get(i)); // row1 <- row2
                    inv.setItem(i + 18, row3.get(i)); // row2 <- row3
                    inv.setItem(i + 27, hotbar.get(i)); // row3 <- hotbar
                }
            }

            player.containerMenu.broadcastChanges();
        });
    }

    private static void giveItemPacket(){
        PayloadTypeRegistry.serverboundPlay().register(GiveItemPayload.ID, GiveItemPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(GiveItemPayload.ID, (payload, context) -> {
            var player = context.player();
            int slot = payload.slot();
            var stack = payload.stack();
            boolean bl = slot < 0;
            boolean bl2 = slot >= 1 && slot <= 45;
            boolean bl3 = stack.isEmpty() || stack.getCount() <= stack.getMaxStackSize();

            context.server().execute(() -> {
                UUID uuid = player.getUUID();
                playerCreativeItemDropCooldowns.putIfAbsent(uuid, new TickThrottler(20, 1480));
                TickThrottler cooldown = playerCreativeItemDropCooldowns.get(uuid);

                if (bl2 && bl3) {
                    player.inventoryMenu.getSlot(slot).setByPlayer(stack);
                    player.inventoryMenu.setRemoteSlot(slot, stack);
                    player.inventoryMenu.broadcastChanges();
                } else if (bl && bl3) {
                    if (cooldown.isUnderThreshold()) {
                        cooldown.increment();
                        player.drop(stack, true);
                    } else {
                        LOGGER.warn("Player {} was dropping items too fast in journey mode, ignoring.", player.getName().getString());
                    }
                }
            });
        });
    }

    private static void unlockItemPacket() {
        PayloadTypeRegistry.serverboundPlay().register(UnlockItemPayload.ID, UnlockItemPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(UnlockItemPayload.ID, (payload, context) -> {
            var player = context.player();
            var server = context.server();
            var item = payload.stack();

            context.server().execute(() -> {
                PlayerUnlocksData playerState = StateSaverAndLoader.getPlayerState(player);
                boolean r = playerState.unlockItem(item);

                ServerPlayer playerEntity = server.getPlayerList().getPlayer(player.getUUID());
                server.execute(() -> {
                    ServerPlayNetworking.send(playerEntity, new SyncUnlockedItemsPayload(playerState));
                });
            });
        });
    }

    private static void trashCanPacket() {
        PayloadTypeRegistry.serverboundPlay().register(TrashCanPayload.ID, TrashCanPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(TrashCanPayload.ID, (payload, context) -> {
           Player player = context.player();
           MinecraftServer server = context.server();
           context.server().execute(() -> {
               TrashcanInventory inv = TrashcanServerStorage.get(player);
               ItemStack stack = payload.stack();
               inv.setItem(0, stack);
               ServerPlayer playerEntity = server.getPlayerList().getPlayer(player.getUUID());
               ServerPlayNetworking.send(playerEntity, new SyncTrashCanPayload(stack));
           });
        });
    }

    private static void unlockItemCommandEvent() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
           dispatcher.register(Commands.literal("unlockitem")
                   .requires(src -> {
                       var player = src.getPlayer();
                       return player != null &&
                               src.getServer() != null &&
                               src.getServer().getPlayerList().isOp(src.getPlayer().nameAndId());
                   })
                   .then(Commands.argument("item", ItemArgument.item(registryAccess))
                           .executes(JourneyNetworking::unlockItemCommand)));
        });
    }

    private static int unlockItemCommand(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer player = ctx.getSource().getPlayer();

        ItemStack unlockStack = ItemArgument.getItem(ctx, "item").createItemStack(1);

        StateSaverAndLoader state = StateSaverAndLoader.getServerState(source.getServer());
        PlayerUnlocksData playerState = StateSaverAndLoader.getPlayerState(player);

        if (playerState.unlockItem(unlockStack)) {
            player.sendOverlayMessage(Component.translatable("item.journeycreative.research_certificate.unlocked", unlockStack.getItem().components().getOrDefault(DataComponents.ITEM_NAME, CommonComponents.EMPTY)));
        } else {
            player.sendOverlayMessage(Component.translatable("item.journeycreative.research_certificate.already_unlocked", unlockStack.getItem().components().getOrDefault(DataComponents.ITEM_NAME, CommonComponents.EMPTY)));
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
                syncResearchItemsUnlocked(handler.getPlayer());
            });
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            TrashcanServerStorage.remove(handler.player);
        });
    }

    public static void syncResearchItemsUnlocked(ServerPlayer player) {
        boolean value = player
                .level()
                        .getGameRules()
                                .get(Journeycreative.RESEARCH_ITEMS_UNLOCKED);
        player.level().getServer().execute(() -> {
            ServerPlayNetworking.send(player, new SyncResearchItemsUnlockRulePayload(value));
        });
    }

    public record GiveItemPayload(int slot, ItemStack stack) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<GiveItemPayload> ID =
                new CustomPacketPayload.Type<>(GIVE_ITEM);

        public static final StreamCodec<RegistryFriendlyByteBuf, GiveItemPayload> CODEC =
                StreamCodec.composite(ByteBufCodecs.INT, GiveItemPayload::slot, ItemStack.OPTIONAL_STREAM_CODEC, GiveItemPayload::stack, GiveItemPayload::new);

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record UnlockItemPayload(ItemStack stack) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<UnlockItemPayload> ID =
                new CustomPacketPayload.Type(UNLOCK_ITEM);
        public static final StreamCodec<RegistryFriendlyByteBuf, UnlockItemPayload> CODEC =
                StreamCodec.composite(ItemStack.STREAM_CODEC, UnlockItemPayload::stack, UnlockItemPayload::new);

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record SyncUnlockedItemsPayload(PlayerUnlocksData playerUnlocksData) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<SyncUnlockedItemsPayload> ID =
                new CustomPacketPayload.Type(SYNC_UNLOCKED_ITEMS);
        public static final StreamCodec<RegistryFriendlyByteBuf, SyncUnlockedItemsPayload> CODEC =
                StreamCodec.composite(ByteBufCodecs.fromCodecWithRegistries(PlayerUnlocksData.PLAYER_UNLOCKS_CODEC), SyncUnlockedItemsPayload::playerUnlocksData, SyncUnlockedItemsPayload::new);

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record SyncResearchItemsUnlockRulePayload(boolean value) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<SyncResearchItemsUnlockRulePayload> ID =
                new CustomPacketPayload.Type(SYNC_RESEARCH_ITEMS_UNLOCKED_RULE);
        public static final StreamCodec<RegistryFriendlyByteBuf, SyncResearchItemsUnlockRulePayload> CODEC =
                StreamCodec.composite(ByteBufCodecs.BOOL, SyncResearchItemsUnlockRulePayload::value, SyncResearchItemsUnlockRulePayload::new);

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record RotateItemsPayload(boolean reversed) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<RotateItemsPayload> ID =
                new CustomPacketPayload.Type(ROTATE_ITEMS);
        public static final StreamCodec<RegistryFriendlyByteBuf, RotateItemsPayload> CODEC =
                StreamCodec.composite(ByteBufCodecs.BOOL, RotateItemsPayload::reversed, RotateItemsPayload::new);

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record TrashCanPayload(ItemStack stack) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<TrashCanPayload> ID =
                new CustomPacketPayload.Type(TRASH_CAN);
        public static final StreamCodec<RegistryFriendlyByteBuf, TrashCanPayload> CODEC =
                StreamCodec.composite(ItemStack.OPTIONAL_STREAM_CODEC, TrashCanPayload::stack, TrashCanPayload::new);

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record SyncTrashCanPayload(ItemStack stack) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<SyncTrashCanPayload> ID =
                new CustomPacketPayload.Type(SYNC_TRASH_CAN);
        public static final StreamCodec<RegistryFriendlyByteBuf, SyncTrashCanPayload> CODEC =
                StreamCodec.composite(ItemStack.OPTIONAL_STREAM_CODEC, SyncTrashCanPayload::stack, SyncTrashCanPayload::new);

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record ItemWarningMessage(Component warningMessage) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<ItemWarningMessage> ID =
                new CustomPacketPayload.Type(SEND_ITEM_WARNING_MESSAGE);

        public static final StreamCodec<RegistryFriendlyByteBuf, ItemWarningMessage> CODEC =
                StreamCodec.composite(ComponentSerialization.STREAM_CODEC, ItemWarningMessage::warningMessage, ItemWarningMessage::new);

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }
}

