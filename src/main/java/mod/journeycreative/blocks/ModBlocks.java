package mod.journeycreative.blocks;

import mod.journeycreative.Journeycreative;
import mod.journeycreative.items.EnderArchiveBlockItem;
import mod.journeycreative.items.ModComponents;
import mod.journeycreative.items.ResearchVesselBlockItem;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.BiFunction;
import java.util.function.Function;

public class ModBlocks {
    private static Block register(String name, Function<BlockBehaviour.Properties, Block> blockFactory,
                                  BlockBehaviour.Properties blockSettings){
        ResourceKey<Block> blockKey = keyOfBlock(name);
        Block block = blockFactory.apply(blockSettings.setId(blockKey));
        return Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
    }

    private static BlockItem register(String name, Block block, BiFunction<Block, Item.Properties, BlockItem> itemFactory,
                                      Item.Properties itemSettings) {
        ResourceKey<Item> itemRegistryKey = keyOfItem(name);
        BlockItem item = itemFactory.apply(block, itemSettings.setId(itemRegistryKey));
        return Registry.register(BuiltInRegistries.ITEM, itemRegistryKey, item);
    }

    private static <T extends BlockEntity> BlockEntityType<T> register(
            String name,
            FabricBlockEntityTypeBuilder.Factory<? extends T> entityFactory,
            Block... blocks
    ) {
        Identifier id = Identifier.fromNamespaceAndPath(Journeycreative.MOD_ID, name);
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id, FabricBlockEntityTypeBuilder.<T>create(entityFactory, blocks).build());
    }

    public static final Block RESEARCH_VESSEL_BLOCK = register("research_vessel",
            ResearchVesselBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.CHEST)
                    .noOcclusion()
                    .strength(1.0F, 1200.0F)
                    .lightLevel(state -> state.getValue(ResearchVesselBlock.OPENED) ? 10 : 0)
    );

    public static final BlockItem RESEARCH_VESSEL_BLOCK_ITEM = register("research_vessel",
            RESEARCH_VESSEL_BLOCK,
            ResearchVesselBlockItem::new,
            new Item.Properties()
                    .stacksTo(1)
                    .component(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
                    .component(ModComponents.RESEARCH_VESSEL_TARGET_COMPONENT, ItemStack.EMPTY)
    );

    public static final BlockEntityType<ResearchVesselBlockEntity> RESEARCH_VESSEL_BLOCK_ENTITY = register(
            "research_vessel",
            ResearchVesselBlockEntity::new,
            RESEARCH_VESSEL_BLOCK
    );

    public static final Block ENDER_ARCHIVE_BLOCK = register("ender_archive",
            EnderArchiveBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.CHISELED_BOOKSHELF)
                    .strength(5.0F, 1200.0F)
                    .lightLevel(state -> 5)
    );

    public static final BlockItem ENDER_ARCHIVE_BLOCK_ITEM = register("ender_archive",
            ENDER_ARCHIVE_BLOCK,
            EnderArchiveBlockItem::new,
            new Item.Properties()
    );

    public static final BlockEntityType<EnderArchiveBlockEntity> ENDER_ARCHIVE_BLOCK_ENTITY = register(
            "ender_archive",
            EnderArchiveBlockEntity::new,
            ENDER_ARCHIVE_BLOCK
    );

    private static ResourceKey<Block> keyOfBlock(String name) {
        return ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(Journeycreative.MOD_ID, name));
    }

    private static ResourceKey<Item> keyOfItem(String name) {
        return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Journeycreative.MOD_ID, name));
    }

    public static void initialize() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS)
                .register(itemGroup -> itemGroup.accept(ModBlocks.RESEARCH_VESSEL_BLOCK.asItem()));
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS)
                .register(itemGroup -> itemGroup.accept(ModBlocks.ENDER_ARCHIVE_BLOCK.asItem()));
    }
}
