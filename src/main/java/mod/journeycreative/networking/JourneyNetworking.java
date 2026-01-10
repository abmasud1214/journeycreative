package mod.journeycreative.networking;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.logging.LogUtils;
import mod.journeycreative.Journeycreative;
import mod.journeycreative.screen.TrashcanInventory;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Cooldown;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

import java.util.*;

public class JourneyNetworking {
    public static final Identifier GIVE_ITEM = Identifier.of(Journeycreative.MOD_ID, "give_item");
    public static final Identifier UNLOCK_ITEM = Identifier.of(Journeycreative.MOD_ID, "unlock_item");
    public static final Identifier TRASH_CAN = Identifier.of(Journeycreative.MOD_ID, "trash_can");
    public static final Identifier SYNC_UNLOCKED_ITEMS = Identifier.of(Journeycreative.MOD_ID, "sync_unlock_item");
    public static final Identifier SYNC_TRASH_CAN = Identifier.of(Journeycreative.MOD_ID, "sync_trash_can");
    public static final Identifier SYNC_RESEARCH_ITEMS_UNLOCKED_RULE = Identifier.of(Journeycreative.MOD_ID, "sync_research_rule");
    public static final Identifier ROTATE_ITEMS = Identifier.of(Journeycreative.MOD_ID, "rotate_items");
    public static final Identifier SEND_ITEM_WARNING_MESSAGE = Identifier.of(Journeycreative.MOD_ID, "send_item_warning_message");

    static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<UUID, Cooldown> playerCreativeItemDropCooldowns = new HashMap<>();

    public static void registerClientPackets() {
        PayloadTypeRegistry.playS2C().register(JourneyNetworking.SyncUnlockedItemsPayload.ID, JourneyNetworking.SyncUnlockedItemsPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(JourneyNetworking.SyncResearchItemsUnlockRulePayload.ID,
                JourneyNetworking.SyncResearchItemsUnlockRulePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(JourneyNetworking.SyncTrashCanPayload.ID,
                JourneyNetworking.SyncTrashCanPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(JourneyNetworking.ItemWarningMessage.ID,
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
            playerCreativeItemDropCooldowns.values().forEach(Cooldown::tick);
        });
    }

    public static void rotateItemsPacket() {
        PayloadTypeRegistry.playC2S().register(RotateItemsPayload.ID, RotateItemsPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(RotateItemsPayload.ID, (payload, context) -> {
            PlayerEntity player = context.player();
            PlayerInventory inv = player.getInventory();

            boolean reversed = payload.reversed();

            List<ItemStack> hotbar = new ArrayList<>();
            List<ItemStack> row1 = new ArrayList<>();
            List<ItemStack> row2 = new ArrayList<>();
            List<ItemStack> row3 = new ArrayList<>();

            for (int i = 0; i < 9; i++) hotbar.add(inv.getStack(i));
            for (int i = 9; i < 18; i++) row1.add(inv.getStack(i));
            for (int i = 18; i < 27; i++) row2.add(inv.getStack(i));
            for (int i = 27; i < 36; i++) row3.add(inv.getStack(i));

            // Rotate
            if (!reversed) {
                for (int i = 0; i < 9; i++) {
                    inv.setStack(i, row3.get(i));       // hotbar <- row3
                    inv.setStack(i + 9, hotbar.get(i)); // row1 <- hotbar
                    inv.setStack(i + 18, row1.get(i));  // row2 <- row1
                    inv.setStack(i + 27, row2.get(i));  // row3 <- row2
                }
            } else {
                for (int i = 0; i < 9; i++) {
                    inv.setStack(i, row1.get(i)); // hotbar <- row1
                    inv.setStack(i + 9, row2.get(i)); // row1 <- row2
                    inv.setStack(i + 18, row3.get(i)); // row2 <- row3
                    inv.setStack(i + 27, hotbar.get(i)); // row3 <- hotbar
                }
            }

            player.currentScreenHandler.sendContentUpdates();
        });
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
                UUID uuid = player.getUuid();
                playerCreativeItemDropCooldowns.putIfAbsent(uuid, new Cooldown(20, 1480));
                Cooldown cooldown = playerCreativeItemDropCooldowns.get(uuid);

                if (bl2 && bl3) {
                    player.playerScreenHandler.getSlot(slot).setStack(stack);
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
            var item = payload.stack();

            context.server().execute(() -> {
                PlayerUnlocksData playerState = StateSaverAndLoader.getPlayerState(player);
                boolean r = playerState.unlockItem(item);

                ServerPlayerEntity playerEntity = server.getPlayerManager().getPlayer(player.getUuid());
                server.execute(() -> {
                    ServerPlayNetworking.send(playerEntity, new SyncUnlockedItemsPayload(playerState));
                });
            });
        });
    }

    private static void trashCanPacket() {
        PayloadTypeRegistry.playC2S().register(TrashCanPayload.ID, TrashCanPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(TrashCanPayload.ID, (payload, context) -> {
           PlayerEntity player = context.player();
           MinecraftServer server = context.server();
           context.server().execute(() -> {
               TrashcanInventory inv = TrashcanServerStorage.get(player);
               ItemStack stack = payload.stack();
               inv.setStack(0, stack);
               ServerPlayerEntity playerEntity = server.getPlayerManager().getPlayer(player.getUuid());
               ServerPlayNetworking.send(playerEntity, new SyncTrashCanPayload(stack));
           });
        });
    }

    private static void unlockItemCommandEvent() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
           dispatcher.register(CommandManager.literal("unlockitem")
                   .requires(src -> src.hasPermissionLevel(2))
                   .then(CommandManager.argument("item", ItemStackArgumentType.itemStack(registryAccess))
                           .executes(JourneyNetworking::unlockItemCommand)));
        });
    }

    private static int unlockItemCommand(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity player = ctx.getSource().getPlayer();

        ItemStack unlockStack = ItemStackArgumentType.getItemStackArgument(ctx, "item").createStack(1, false);

        StateSaverAndLoader state = StateSaverAndLoader.getServerState(source.getServer());
        PlayerUnlocksData playerState = StateSaverAndLoader.getPlayerState(player);

        if (playerState.unlockItem(unlockStack)) {
            player.sendMessage(Text.translatable("item.journeycreative.research_certificate.unlocked", unlockStack.getItem().getName()), true);
        } else {
            player.sendMessage(Text.translatable("item.journeycreative.research_certificate.already_unlocked", unlockStack.getItem().getName()), true);
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

    public static void syncResearchItemsUnlocked(ServerPlayerEntity player) {
        if (player.getWorld() instanceof ServerWorld serverWorld) {
            boolean value = serverWorld.getGameRules().getBoolean(Journeycreative.RESEARCH_ITEMS_UNLOCKED);
            player.getWorld().getServer().execute(() -> {
                ServerPlayNetworking.send(player, new SyncResearchItemsUnlockRulePayload(value));
            });
        }
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

    public record UnlockItemPayload(ItemStack stack) implements CustomPayload {
        public static final CustomPayload.Id<UnlockItemPayload> ID =
                new CustomPayload.Id(UNLOCK_ITEM);
        public static final PacketCodec<RegistryByteBuf, UnlockItemPayload> CODEC =
                PacketCodec.tuple(ItemStack.PACKET_CODEC, UnlockItemPayload::stack, UnlockItemPayload::new);

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

    public record SyncResearchItemsUnlockRulePayload(boolean value) implements CustomPayload {
        public static final CustomPayload.Id<SyncResearchItemsUnlockRulePayload> ID =
                new CustomPayload.Id(SYNC_RESEARCH_ITEMS_UNLOCKED_RULE);
        public static final PacketCodec<RegistryByteBuf, SyncResearchItemsUnlockRulePayload> CODEC =
                PacketCodec.tuple(PacketCodecs.BOOL, SyncResearchItemsUnlockRulePayload::value, SyncResearchItemsUnlockRulePayload::new);

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record RotateItemsPayload(boolean reversed) implements CustomPayload {
        public static final CustomPayload.Id<RotateItemsPayload> ID =
                new CustomPayload.Id(ROTATE_ITEMS);
        public static final PacketCodec<RegistryByteBuf, RotateItemsPayload> CODEC =
                PacketCodec.tuple(PacketCodecs.BOOL, RotateItemsPayload::reversed, RotateItemsPayload::new);

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record TrashCanPayload(ItemStack stack) implements CustomPayload {
        public static final CustomPayload.Id<TrashCanPayload> ID =
                new CustomPayload.Id(TRASH_CAN);
        public static final PacketCodec<RegistryByteBuf, TrashCanPayload> CODEC =
                PacketCodec.tuple(ItemStack.OPTIONAL_PACKET_CODEC, TrashCanPayload::stack, TrashCanPayload::new);

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record SyncTrashCanPayload(ItemStack stack) implements CustomPayload {
        public static final CustomPayload.Id<SyncTrashCanPayload> ID =
                new CustomPayload.Id(SYNC_TRASH_CAN);
        public static final PacketCodec<RegistryByteBuf, SyncTrashCanPayload> CODEC =
                PacketCodec.tuple(ItemStack.OPTIONAL_PACKET_CODEC, SyncTrashCanPayload::stack, SyncTrashCanPayload::new);

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record ItemWarningMessage(Text warningMessage) implements CustomPayload {
        public static final CustomPayload.Id<ItemWarningMessage> ID =
                new CustomPayload.Id(SEND_ITEM_WARNING_MESSAGE);

        public static final PacketCodec<RegistryByteBuf, ItemWarningMessage> CODEC =
                PacketCodec.tuple(TextCodecs.PACKET_CODEC, ItemWarningMessage::warningMessage, ItemWarningMessage::new);

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
}

