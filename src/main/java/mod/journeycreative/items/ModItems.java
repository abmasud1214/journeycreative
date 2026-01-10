package mod.journeycreative.items;

import mod.journeycreative.Journeycreative;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class ModItems {
    public static final Item RESEARCH_CERTIFICATE = register("research_certificate",
            settings -> new ResearchCertificateItem(settings),
            new Item.Settings()
                    .maxCount(1)
                    .component(ModComponents.RESEARCH_ITEM_COMPONENT, new ItemStack(Items.BARRIER)));

    public static Item register(String name, Function<Item.Settings, Item> itemFactory, Item.Settings settings) {
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Journeycreative.MOD_ID, name));

        Item item = itemFactory.apply(settings);

        Registry.register(Registries.ITEM, itemKey, item);

        return item;
    }

    public static void initialize() {
    }
}
