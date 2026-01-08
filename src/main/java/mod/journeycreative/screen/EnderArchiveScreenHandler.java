package mod.journeycreative.screen;

import mod.journeycreative.ResearchConfig;
import mod.journeycreative.blocks.ModBlocks;
import mod.journeycreative.items.ModComponents;
import mod.journeycreative.items.ModItems;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.ForgingSlotsManager;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public class EnderArchiveScreenHandler extends ForgingScreenHandler {
    private final World world;
    private final Property invalidRecipe;
    public enum researchInvalidReason {
        VALID,
        INSUFFICIENT,
        BLOCKED,
        PROHIBITED
    }
    private final Property reason;


    public EnderArchiveScreenHandler(int syncId, PlayerInventory inventory) {
        this(syncId, inventory, ScreenHandlerContext.EMPTY);
    }

    public EnderArchiveScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        this(syncId, playerInventory, context, playerInventory.player.getEntityWorld());
    }

    private EnderArchiveScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, World world) {
        super(ModScreens.ENDER_ARCHIVE_SCREEN_HANDLER, syncId, playerInventory, context, createForgingSlotsManager(world.getRecipeManager()));
        this.invalidRecipe = Property.create();
        this.reason = Property.create();
        this.world = world;
        this.addProperty(this.invalidRecipe).set(0);
        this.addProperty(this.reason).set(0);
    }

    private static ForgingSlotsManager createForgingSlotsManager(RecipeManager recipeManager) {
        ForgingSlotsManager.Builder builder = ForgingSlotsManager.builder();
        builder = builder.input(0, 53, 33, EnderArchiveScreenHandler::canUseSlot);
        return builder.output(1, 107, 33).build();
    }

    protected boolean canUse(BlockState state) {
        return state.isOf(ModBlocks.ENDER_ARCHIVE_BLOCK);
    }

    protected void onTakeOutput(PlayerEntity player, ItemStack stack) {
        stack.onCraftByPlayer(player, stack.getCount());
        this.decrementStack(0);
        this.context.run((world, pos) -> {
            world.syncWorldEvent(1044, pos, 0);
        });
    }

    private void decrementStack(int slot) {
        ItemStack itemStack = this.input.getStack(slot);
        if (!itemStack.isEmpty()) {
            itemStack.decrement(1);
            this.input.setStack(slot, itemStack);
        }
    }

    public void onContentChanged(Inventory inventory) {
        super.onContentChanged(inventory);
        if (this.world instanceof ServerWorld) {
            boolean bl = this.getSlot(0).hasStack() && !this.getSlot(this.getResultSlotIndex()).hasStack();
            this.invalidRecipe.set(bl ? 1 : 0);
        }
    }

    public void updateResult() {
        if (this.world instanceof ServerWorld serverWorld && isValidIngedient(this.input.getStack(0))) {
            ItemStack input = this.input.getStack(0);
            boolean exists = input.contains(ModComponents.RESEARCH_VESSEL_TARGET_COMPONENT);
            boolean container_exists = input.contains(DataComponentTypes.CONTAINER);
            if (exists && container_exists) {
                ItemStack target = input.get(ModComponents.RESEARCH_VESSEL_TARGET_COMPONENT);
                ContainerComponent container = input.get(DataComponentTypes.CONTAINER);

                boolean full = fullContainer(target, container);
                boolean canCreateCertificate = !ResearchConfig.RESEARCH_BLOCKED.contains(Registries.ITEM.getId(target.getItem()));
                boolean canResearchItem = !ResearchConfig.RESEARCH_PROHIBITED.contains(Registries.ITEM.getId(target.getItem()));

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
                    this.output.setStack(0, output);
                    this.reason.set(0);
                    return;
                }
                this.reason.set(r.ordinal());
            }
        }
        this.output.setStack(0, ItemStack.EMPTY);
    }

    public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
        return slot.inventory != this.output && super.canInsertIntoSlot(stack, slot);
    }

    public boolean isValidIngedient(ItemStack stack) {
        if (EnderArchiveScreenHandler.canUseSlot(stack) && this.getSlot(0).hasStack()) {
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

    private static boolean fullContainer(ItemStack target, ContainerComponent container) {
        Iterable<ItemStack> containerStacks = container.iterateNonEmpty();
        int full = ResearchConfig.RESEARCH_AMOUNT_REQUIREMENTS.getOrDefault(Registries.ITEM.getId(target.getItem()),27 * target.getMaxCount());
        full = Math.min(full, 27 * target.getMaxCount());
        for (ItemStack stack : containerStacks) {
            full -= stack.getCount();
        }
        return full <= 0;
    }

    public researchInvalidReason getReason() {
        return researchInvalidReason.values()[this.reason.get()];
    }

}
