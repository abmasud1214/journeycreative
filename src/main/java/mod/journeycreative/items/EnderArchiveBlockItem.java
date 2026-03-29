package mod.journeycreative.items;

import mod.journeycreative.Journeycreative;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class EnderArchiveBlockItem extends BlockItem {
    public EnderArchiveBlockItem(Block block, Properties settings) {
        super(block, settings);
    }

    public static void appendTooltip(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag type) {
        tooltip.add(Component.translatable("item.journeycreative.ender_archive.tooptip",
                BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(Journeycreative.MOD_ID,"research_vessel")).components().getOrDefault(DataComponents.ITEM_NAME, CommonComponents.EMPTY),
                BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(Journeycreative.MOD_ID, "research_certificate")).components().getOrDefault(DataComponents.ITEM_NAME, CommonComponents.EMPTY)
                ).withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_GRAY));
    }
}
