package mod.journeycreative.blocks;

import mod.journeycreative.Journeycreative;
import mod.journeycreative.items.ModComponents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class ModBlocks {
    private static Block register(String name, Function<AbstractBlock.Settings,
            Block> blockFactory, AbstractBlock.Settings settings, boolean shouldRegisterItem) {
        return register(name, blockFactory, settings, shouldRegisterItem, null);
    }

    private static Block register(String name, Function<AbstractBlock.Settings, Block> blockFactory,
                                  AbstractBlock.Settings settings, boolean shouldRegisterItem,
                                  @Nullable Function<Item.Settings, Item.Settings> itemSettingsModifier) {
        RegistryKey<Block> blockKey = keyOfBlock(name);
        Block block = blockFactory.apply(settings.registryKey(blockKey));

        if (shouldRegisterItem) {
            RegistryKey<Item> itemKey = keyOfItem(name);
            Item.Settings itemSettings = new Item.Settings().registryKey(itemKey);
            if (itemSettingsModifier != null) {
                itemSettings = itemSettingsModifier.apply(itemSettings);
            }

            BlockItem blockItem = new BlockItem(block, itemSettings);
            Registry.register(Registries.ITEM, itemKey, blockItem);
        }

        return Registry.register(Registries.BLOCK, blockKey, block);
    }

    private static <T extends BlockEntity> BlockEntityType<T> register(
            String name,
            FabricBlockEntityTypeBuilder.Factory<? extends T> entityFactory,
            Block... blocks
    ) {
        Identifier id = Identifier.of(Journeycreative.MOD_ID, name);
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, id, FabricBlockEntityTypeBuilder.<T>create(entityFactory, blocks).build());
    }

    public static final Block RESEARCH_VESSEL_BLOCK = register("research_vessel",
            ResearchVesselBlock::new,
            AbstractBlock.Settings.copy(Blocks.CHEST)
                    .nonOpaque()
                    .luminance(state -> state.get(ResearchVesselBlock.OPENED) ? 10 : 0),
            true,
            settings -> settings.maxCount(1).component(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT).component(ModComponents.RESEARCH_VESSEL_TARGET_COMPONENT, ItemStack.EMPTY));

    public static final BlockEntityType<ResearchVesselBlockEntity> RESEARCH_VESSEL_BLOCK_ENTITY = register(
            "research_vessel",
            ResearchVesselBlockEntity::new,
            RESEARCH_VESSEL_BLOCK
    );

    public static final Block ENDER_ARCHIVE_BLOCK = register("ender_archive",
            EnderArchiveBlock::new,
            AbstractBlock.Settings.copy(Blocks.CHISELED_BOOKSHELF)
                    .luminance(state -> 5),
            true);

    public static final BlockEntityType<EnderArchiveBlockEntity> ENDER_ARCHIVE_BLOCK_ENTITY = register(
            "ender_archive",
            EnderArchiveBlockEntity::new,
            ENDER_ARCHIVE_BLOCK
    );

    private static RegistryKey<Block> keyOfBlock(String name) {
        return RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(Journeycreative.MOD_ID, name));
    }

    private static RegistryKey<Item> keyOfItem(String name) {
        return RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Journeycreative.MOD_ID, name));
    }

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL)
                .register(itemGroup -> itemGroup.add(ModBlocks.RESEARCH_VESSEL_BLOCK.asItem()));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL)
                .register(itemGroup -> itemGroup.add(ModBlocks.ENDER_ARCHIVE_BLOCK.asItem()));
    }
}
