package mod.journeycreative.items;

import mod.journeycreative.ResearchConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.stream.Stream;

public class ResearchVesselBlockItem extends BlockItem {

    public ResearchVesselBlockItem(Block block, Properties settings) {
        super(block, settings);
    }

    public static void appendTooltip(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag type) {
        tooltip.removeIf(text -> {
            if (text.getContents() instanceof TranslatableContents content) {
                String key = content.getKey();
                return key.equals("item.container.item_count") || key.equals("item.container.more_items");
            }
            return false;
        });

        ItemContainerContents containerComponent = stack.get(DataComponents.CONTAINER);
        if (containerComponent.copyOne().isEmpty()) {
            tooltip.add(Component.translatable("item.journeycreative.research_vessel.tooltip").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        } else {
            ItemStack target = stack.get(ModComponents.RESEARCH_VESSEL_TARGET_COMPONENT);
            int default_lim = (int) Math.ceil(27 * target.getMaxStackSize() * ResearchConfig.DEFAULT_AMOUNT_ADJUSTMENT);
            default_lim = Math.max(1, default_lim);
            int capacity = ResearchConfig.RESEARCH_AMOUNT_REQUIREMENTS.getOrDefault(BuiltInRegistries.ITEM.getKey(target.getItem()),default_lim);
            capacity = Math.min(capacity, 27 * target.getMaxStackSize());
            Stream<ItemStack> containerStacks = containerComponent.nonEmptyItemCopyStream();
            int quantity = containerStacks
                    .mapToInt(ItemStack::getCount)
                    .sum();

            if (quantity < capacity) {
                tooltip.add(Component.translatable("item.journeycreative.research_vessel.tooltip.status", quantity, capacity, target.getItem().components().getOrDefault(DataComponents.ITEM_NAME, CommonComponents.EMPTY)).withStyle(ChatFormatting.YELLOW));
            } else {
                tooltip.add(Component.translatable("item.journeycreative.research_vessel.tooltip.status", quantity, capacity, target.getItem().components().getOrDefault(DataComponents.ITEM_NAME, CommonComponents.EMPTY)).withStyle(ChatFormatting.GREEN));
            }
        }
    }

}
