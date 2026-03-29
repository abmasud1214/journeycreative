package mod.journeycreative.screen;

import mod.journeycreative.ResearchConfig;
import mod.journeycreative.blocks.ModBlocks;
import mod.journeycreative.items.ModComponents;
import mod.journeycreative.items.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class EnderArchiveScreenHandler extends ItemCombinerMenu {
    private final Level world;
    private final DataSlot invalidRecipe;
    public enum researchInvalidReason {
        VALID,
        INSUFFICIENT,
        BLOCKED,
        PROHIBITED
    }
    private final DataSlot reason;


    public EnderArchiveScreenHandler(int syncId, Inventory inventory) {
        this(syncId, inventory, ContainerLevelAccess.NULL);
    }

    public EnderArchiveScreenHandler(int syncId, Inventory playerInventory, ContainerLevelAccess context) {
        this(syncId, playerInventory, context, playerInventory.player.level());
    }

    private EnderArchiveScreenHandler(int syncId, Inventory playerInventory, ContainerLevelAccess context, Level world) {
        super(ModScreens.ENDER_ARCHIVE_SCREEN_HANDLER, syncId, playerInventory, context, createForgingSlotsManager(world.recipeAccess()));
        this.invalidRecipe = DataSlot.standalone();
        this.reason = DataSlot.standalone();
        this.world = world;
        this.addDataSlot(this.invalidRecipe).set(0);
        this.addDataSlot(this.reason).set(0);
    }

    private static ItemCombinerMenuSlotDefinition createForgingSlotsManager(RecipeAccess recipeManager) {
        ItemCombinerMenuSlotDefinition.Builder builder = ItemCombinerMenuSlotDefinition.create();
        builder = builder.withSlot(0, 53, 33, EnderArchiveScreenHandler::canUseSlot);
        return builder.withResultSlot(1, 107, 33).build();
    }

    protected boolean isValidBlock(BlockState state) {
        return state.is(ModBlocks.ENDER_ARCHIVE_BLOCK);
    }

    protected void onTake(Player player, ItemStack stack) {
        stack.onCraftedBy(player, stack.getCount());
        this.decrementStack(0);
        this.access.execute((world, pos) -> {
            world.levelEvent(1044, pos, 0);
        });
    }

    private void decrementStack(int slot) {
        ItemStack itemStack = this.inputSlots.getItem(slot);
        if (!itemStack.isEmpty()) {
            itemStack.shrink(1);
            this.inputSlots.setItem(slot, itemStack);
        }
    }

    public void slotsChanged(Container inventory) {
        super.slotsChanged(inventory);
        if (this.world instanceof ServerLevel) {
            boolean bl = this.getSlot(0).hasItem() && !this.getSlot(this.getResultSlot()).hasItem();
            this.invalidRecipe.set(bl ? 1 : 0);
        }
    }

    public void createResult() {
        if (this.world instanceof ServerLevel serverWorld && isValidIngedient(this.inputSlots.getItem(0))) {
            ItemStack input = this.inputSlots.getItem(0);
            boolean exists = input.has(ModComponents.RESEARCH_VESSEL_TARGET_COMPONENT);
            boolean container_exists = input.has(DataComponents.CONTAINER);
            if (exists && container_exists) {
                ItemStack target = input.get(ModComponents.RESEARCH_VESSEL_TARGET_COMPONENT);
                ItemContainerContents container = input.get(DataComponents.CONTAINER);

                boolean full = fullContainer(target, container);
                boolean canCreateCertificate = !ResearchConfig.RESEARCH_BLOCKED.contains(BuiltInRegistries.ITEM.getKey(target.getItem()));
                boolean canResearchItem = !ResearchConfig.RESEARCH_PROHIBITED.contains(BuiltInRegistries.ITEM.getKey(target.getItem()));

                researchInvalidReason r = researchInvalidReason.VALID;
                if (!canCreateCertificate) {
                    r = researchInvalidReason.BLOCKED;
                } else if (!canResearchItem) {
                    r = researchInvalidReason.PROHIBITED;
                } else if (!full) {
                    r = researchInvalidReason.INSUFFICIENT;
                } else if (!target.isEmpty()) {
                    ItemStack output = new ItemStack(ModItems.RESEARCH_CERTIFICATE, 1);
                    output.set(ModComponents.RESEARCH_ITEM_COMPONENT, target);
                    this.resultSlots.setItem(0, output);
                    this.reason.set(0);
                    return;
                }
                this.reason.set(r.ordinal());
            }
        }
        this.resultSlots.setItem(0, ItemStack.EMPTY);
    }

    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.container != this.resultSlots && super.canTakeItemForPickAll(stack, slot);
    }

    public boolean isValidIngedient(ItemStack stack) {
        if (EnderArchiveScreenHandler.canUseSlot(stack) && this.getSlot(0).hasItem()) {
            return true;
        }
        return false;
    }

    public boolean hasInvalidRecipe() {
        return this.invalidRecipe.get() > 0;
    }

    private static boolean canUseSlot(ItemStack stack) {
        if (stack.getItem() instanceof BlockItem blockItem) {
            return blockItem.getBlock() == ModBlocks.RESEARCH_VESSEL_BLOCK;
        }
        return false;
    }

    private static boolean fullContainer(ItemStack target, ItemContainerContents container) {
        Iterable<ItemStack> containerStacks = container.nonEmptyItemCopyStream().toList();
        int default_lim = (int) Math.ceil(27 * target.getMaxStackSize() * ResearchConfig.DEFAULT_AMOUNT_ADJUSTMENT);
        default_lim = Math.max(1, default_lim);
        int full = ResearchConfig.RESEARCH_AMOUNT_REQUIREMENTS.getOrDefault(BuiltInRegistries.ITEM.getKey(target.getItem()),default_lim);
        full = Math.min(full, 27 * target.getMaxStackSize());
        for (ItemStack stack : containerStacks) {
            full -= stack.getCount();
        }
        return full <= 0;
    }

    public researchInvalidReason getReason() {
        return researchInvalidReason.values()[this.reason.get()];
    }

}
