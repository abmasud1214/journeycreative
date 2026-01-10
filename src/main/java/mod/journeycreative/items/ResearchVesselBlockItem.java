package mod.journeycreative.items;

import mod.journeycreative.ResearchConfig;
import net.minecraft.block.Block;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;

import java.util.List;

public class ResearchVesselBlockItem extends BlockItem {

    public ResearchVesselBlockItem(Block block, Settings settings) {
        super(block, settings);
    }

    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.removeIf(text -> {
            if (text.getContent() instanceof TranslatableTextContent content) {
                String key = content.getKey();
                return key.equals("item.container.item_count") || key.equals("item.container.more_items");
            }
            return false;
        });

        ContainerComponent containerComponent = stack.get(DataComponentTypes.CONTAINER);
        if (containerComponent.copyFirstStack().isEmpty()) {
            tooltip.add(Text.translatable("item.journeycreative.research_vessel.tooltip").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
        } else {
            ItemStack target = stack.get(ModComponents.RESEARCH_VESSEL_TARGET_COMPONENT);
            int capacity = ResearchConfig.RESEARCH_AMOUNT_REQUIREMENTS.getOrDefault(Registries.ITEM.getId(target.getItem()),27 * target.getMaxCount());
            capacity = Math.min(capacity, 27 * target.getMaxCount());
            int quantity = 0;
            Iterable<ItemStack> containerStacks = containerComponent.iterateNonEmpty();
            for (ItemStack s : containerStacks) {
                quantity += s.getCount();
            }

            if (quantity < capacity) {
                tooltip.add(Text.translatable("item.journeycreative.research_vessel.tooltip.status", quantity, capacity, target.getItem().getName()).formatted(Formatting.YELLOW));
            } else {
                tooltip.add(Text.translatable("item.journeycreative.research_vessel.tooltip.status", quantity, capacity, target.getItem().getName()).formatted(Formatting.GREEN));
            }
        }
    }

}
