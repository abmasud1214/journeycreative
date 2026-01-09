package mod.journeycreative.items;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

public class EnderArchiveBlockItem extends BlockItem {
    public EnderArchiveBlockItem(Block block, Settings settings) {
        super(block, settings);
    }

    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.journeycreative.ender_archive.tooptip",
                Registries.ITEM.get(Identifier.of("journeycreative:research_vessel")).getName(),
                Registries.ITEM.get(Identifier.of("journeycreative:research_certificate")).getName()
                ).formatted(Formatting.ITALIC, Formatting.DARK_GRAY));
    }
}
