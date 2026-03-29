package mod.journeycreative.items;

import mod.journeycreative.Journeycreative;
import mod.journeycreative.ResearchConfig;
import mod.journeycreative.networking.JourneyNetworking;
import mod.journeycreative.networking.PlayerUnlocksData;
import mod.journeycreative.networking.StateSaverAndLoader;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ResearchCertificateItem extends Item {

    public ResearchCertificateItem(Properties settings) {
        super(settings);
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        stack.set(ModComponents.RESEARCH_ITEM_COMPONENT,
                new ItemStack(Items.BARRIER));
        return stack;
    }

    private static Component getItemName(ItemStack stack) {
        Item item = stack.getItem();

        if (item == null) {
            return Component.literal("Unknown Item");
        }

        return item.components().getOrDefault(DataComponents.ITEM_NAME, CommonComponents.EMPTY);
    }

    public static void appendTooltip(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag type) {
        boolean exists = stack.has(ModComponents.RESEARCH_ITEM_COMPONENT);
        if (exists) {
            ItemStack research_item = stack.get(ModComponents.RESEARCH_ITEM_COMPONENT);
            tooltip.add(Component.translatable("item.journeycreative.research_certificate.research_item", getItemName(research_item)).withStyle(ChatFormatting.GOLD));
        }
    }

    @Override
    public InteractionResult use(final Level level, final Player player, final InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 10;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BLOCK;
    }

    @Override
    public void onUseTick(Level world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!world.isClientSide() && user instanceof Player player) {
            int heldTicks = this.getUseDuration(stack, user) - remainingUseTicks;
            player.sendOverlayMessage(Component.literal("[" + "+".repeat(heldTicks) + "-".repeat(remainingUseTicks) + "]"));
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        boolean exists = stack.has(ModComponents.RESEARCH_ITEM_COMPONENT);

        if (world instanceof ServerLevel serverWorld && user instanceof Player player && exists) {
            PlayerUnlocksData playerState = StateSaverAndLoader.getPlayerState(player);
            ItemStack research_target = stack.get(ModComponents.RESEARCH_ITEM_COMPONENT);

            Set<Identifier> prohibited = ResearchConfig.RESEARCH_PROHIBITED;
            if (prohibited.contains(BuiltInRegistries.ITEM.getKey(research_target.getItem()))) {
                player.sendOverlayMessage(Component.translatable("item.journeycreative.research_certificate.cannot_unlock", getItemName(research_target)));
                return stack;
            }

            List<Identifier> prerequisites = ResearchConfig.RESEARCH_PREREQUISITES.getOrDefault(
                    BuiltInRegistries.ITEM.getKey(research_target.getItem()), new ArrayList<Identifier>()
            );
            ArrayList<Component> prereqs = new ArrayList<>();
            if (!prerequisites.isEmpty()) {
                for (Identifier id : prerequisites) {
                    ItemStack prereqStack = new ItemStack(BuiltInRegistries.ITEM.getValue(id), 1);
                    if (!playerState.isUnlocked(prereqStack,
                            serverWorld.getGameRules().get(Journeycreative.RESEARCH_ITEMS_UNLOCKED))) {
                        prereqs.add(getItemName(prereqStack));
                    }
                }
            }
            if (!prereqs.isEmpty()) {
                MutableComponent prereqText = Component.empty();
                prereqText.append(Component.literal("["));
                prereqText.append(ComponentUtils.formatList(prereqs, Component.literal(", ")));
                prereqText.append(Component.literal("]"));
                player.sendSystemMessage(Component.translatable("item.journeycreative.research_certificate.need_prerequisite", prereqText, getItemName(research_target)));
                return stack;
            }

            boolean unlocked = playerState.unlockItem(research_target);
            ServerPlayer playerEntity = world.getServer().getPlayerList().getPlayer(player.getUUID());
            ServerPlayNetworking.send(playerEntity, new JourneyNetworking.SyncUnlockedItemsPayload(playerState));

            if (unlocked) {
                player.sendOverlayMessage(Component.translatable("item.journeycreative.research_certificate.unlocked", getItemName(research_target)));
                stack.shrink(1);
            } else {
                player.sendOverlayMessage(Component.translatable("item.journeycreative.research_certificate.already_unlocked", getItemName(research_target)));
            }
        }
        return stack;
    }

}