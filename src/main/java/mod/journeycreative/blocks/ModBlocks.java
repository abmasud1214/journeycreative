package mod.journeycreative.blocks;

import mod.journeycreative.Journeycreative;
import mod.journeycreative.items.EnderArchiveBlockItem;
import mod.journeycreative.items.ModComponents;
import mod.journeycreative.items.ModItems;
import mod.journeycreative.items.ResearchVesselBlockItem;
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

import java.util.function.BiFunction;
import java.util.function.Function;

public class ModBlocks {
    private static Block register(String name, Function<AbstractBlock.Settings, Block> blockFactory,
                                  AbstractBlock.Settings blockSettings){
        RegistryKey<Block> blockKey = keyOfBlock(name);
        Block block = blockFactory.apply(blockSettings.registryKey(blockKey));
        return Registry.register(Registries.BLOCK, blockKey, block);
    }

    private static BlockItem register(String name, Block block, BiFunction<Block, Item.Settings, BlockItem> itemFactory,
                                      Item.Settings itemSettings) {
        RegistryKey<Item> itemRegistryKey = keyOfItem(name);
        BlockItem item = itemFactory.apply(block, itemSettings.registryKey(itemRegistryKey));
        return Registry.register(Registries.ITEM, itemRegistryKey, item);
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
                    .strength(3.0F, 1200.0F)
                    .luminance(state -> state.get(ResearchVesselBlock.OPENED) ? 10 : 0)
    );

    public static final BlockItem RESEARCH_VESSEL_BLOCK_ITEM = register("research_vessel",
            RESEARCH_VESSEL_BLOCK,
            ResearchVesselBlockItem::new,
            new Item.Settings()
                    .maxCount(1)
                    .component(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT)
                    .component(ModComponents.RESEARCH_VESSEL_TARGET_COMPONENT, ItemStack.EMPTY)
    );

    public static final BlockEntityType<ResearchVesselBlockEntity> RESEARCH_VESSEL_BLOCK_ENTITY = register(
            "research_vessel",
            ResearchVesselBlockEntity::new,
            RESEARCH_VESSEL_BLOCK
    );

    public static final Block ENDER_ARCHIVE_BLOCK = register("ender_archive",
            EnderArchiveBlock::new,
            AbstractBlock.Settings.copy(Blocks.CHISELED_BOOKSHELF)
                    .strength(3.0F, 1200.0F)
                    .luminance(state -> 5)
    );

    public static final BlockItem ENDER_ARCHIVE_BLOCK_ITEM = register("ender_archive",
            ENDER_ARCHIVE_BLOCK,
            EnderArchiveBlockItem::new,
            new Item.Settings()
    );

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
