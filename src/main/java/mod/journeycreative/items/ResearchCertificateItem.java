package mod.journeycreative.items;

import mod.journeycreative.networking.JourneyNetworking;
import mod.journeycreative.networking.PlayerUnlocksData;
import mod.journeycreative.networking.StateSaverAndLoader;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.UseAction;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.List;

public class ResearchCertificateItem extends Item {
    private static final RegistryKey<Item> barrier_key = RegistryKey.of(RegistryKeys.ITEM, Identifier.of("minecraft", "barrier"));

    public ResearchCertificateItem(Settings settings) {
        super(settings);
    }

    private static Text getItemName(RegistryKey<Item> itemKey) {
        Item item = Registries.ITEM.get(itemKey);

        if (item == null) {
            return Text.literal("Unknown Item: " + itemKey.getValue());
        }

        return item.getName();
    }

    public static void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        boolean exists = stack.contains(ModComponents.RESEARCH_ITEM_COMPONENT);
//        boolean exists = true;
        if (exists) {
//            RegistryKey<Item> research_item = stack.getOrDefault(ModComponents.RESEARCH_ITEM_COMPONENT, RegistryKey.of(RegistryKeys.ITEM, Identifier.of("minecraft", "diamond")));
            RegistryKey<Item> research_item = stack.get(ModComponents.RESEARCH_ITEM_COMPONENT);
            tooltip.add(Text.translatable("item.journeycreative.research_certificate.research_item", getItemName(research_item)).formatted(Formatting.GOLD));
        }
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        user.setCurrentHand(hand);
        return ActionResult.CONSUME;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 10;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BLOCK;
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!world.isClient && user instanceof PlayerEntity player) {
            int heldTicks = this.getMaxUseTime(stack, user) - remainingUseTicks;
            player.sendMessage(Text.literal("[" + "+".repeat(heldTicks) + "-".repeat(remainingUseTicks) + "]"), true);
        }
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        boolean exists = stack.contains(ModComponents.RESEARCH_ITEM_COMPONENT);

        if (!world.isClient && user instanceof PlayerEntity player && exists) {
            PlayerUnlocksData playerState = StateSaverAndLoader.getPlayerState(player);
            RegistryKey<Item> research_target = stack.get(ModComponents.RESEARCH_ITEM_COMPONENT);
//            RegistryKey<Item> research_target = stack.getOrDefault(ModComponents.RESEARCH_ITEM_COMPONENT, RegistryKey.of(RegistryKeys.ITEM, Identifier.of("minecraft", "diamond")));
            if (research_target.equals(barrier_key)) {
                player.sendMessage(Text.translatable("item.journeycreative.research_certificate.cannot_unlock", getItemName(research_target)), true);
                return stack;
            }

            boolean unlocked = playerState.unlockItem(research_target);
            ServerPlayerEntity playerEntity = world.getServer().getPlayerManager().getPlayer(player.getUuid());
            ServerPlayNetworking.send(playerEntity, new JourneyNetworking.SyncUnlockedItemsPayload(playerState));

            if (unlocked) {
                player.sendMessage(Text.translatable("item.journeycreative.research_certificate.unlocked", getItemName(research_target)), true);
                stack.decrement(1);
            } else {
                player.sendMessage(Text.translatable("item.journeycreative.research_certificate.already_unlocked", getItemName(research_target)), true);
            }
        }
        return stack;
    }

}