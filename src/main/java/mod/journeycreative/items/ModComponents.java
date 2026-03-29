package mod.journeycreative.items;

import mod.journeycreative.Journeycreative;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class ModComponents {
    public static void initialize() {
    }

    public static final DataComponentType<ItemStack> RESEARCH_ITEM_COMPONENT = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath(Journeycreative.MOD_ID, "research_item_component"),
            DataComponentType.<ItemStack>builder().persistent(ItemStack.CODEC).build()
    );

    public static final DataComponentType<ItemStack> RESEARCH_VESSEL_TARGET_COMPONENT = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath(Journeycreative.MOD_ID, "research_vessel_target_component"),
            DataComponentType.<ItemStack>builder().persistent(ItemStack.OPTIONAL_CODEC).build()
    );
}
