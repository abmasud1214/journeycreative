package mod.journeycreative.items;

import mod.journeycreative.Journeycreative;
import net.minecraft.component.ComponentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ModComponents {
    public static void initialize() {
    }

    public static final ComponentType<RegistryKey<Item>> RESEARCH_ITEM_COMPONENT = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(Journeycreative.MOD_ID, "research_item_component"),
            ComponentType.<RegistryKey<Item>>builder().codec(RegistryKey.createCodec(RegistryKeys.ITEM)).build()
    );

    public static final ComponentType<ItemStack> RESEARCH_VESSEL_TARGET_COMPONENT = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(Journeycreative.MOD_ID, "research_vessel_target_component"),
            ComponentType.<ItemStack>builder().codec(ItemStack.OPTIONAL_CODEC).build()
    );
}
