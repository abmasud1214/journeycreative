package mod.journeycreative.screen;

import mod.journeycreative.blocks.ResearchVesselBlockEntity;
import mod.journeycreative.blocks.ResearchVesselInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.ShulkerBoxSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

public class ResearchVesselScreenHandler extends ScreenHandler {
    private final ResearchVesselInventory inventory;

    public ResearchVesselScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ResearchVesselInventory.ofSize(27));
    }

    public ResearchVesselScreenHandler(int syncId, PlayerInventory playerInventory, ResearchVesselInventory inventory) {
        super(ModScreens.RESEARCH_VESSEL_SCREEN_HANDLER, syncId);
        checkSize(inventory, 27);
        this.inventory = inventory;

        inventory.onOpen(playerInventory.player);

        int m;
        int l;

        for (m = 0; m < 3; ++m) {
            for (l = 0; l < 9; ++l) {
                this.addSlot(new ShulkerBoxSlot(inventory, l + m * 9, 8 + l * 18, 18 + m * 18));
            }
        }

        this.addPlayerSlots(playerInventory, 8, 84);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            if (invSlot < this.inventory.size()) {
//                if (!this.insertItem(originalStack, this.inventory.size(), this.slots.size(), true)) {
                if (!insertItemOnTarget(originalStack, this.inventory.size(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
//            } else if (!this.insertItem(originalStack, 0, this.inventory.size(), false)) {
            } else if (!insertItemOnTarget(originalStack, 0, this.inventory.size(), false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return newStack;
    }

    private boolean insertItemOnTarget(ItemStack stack, int startIndex, int endIndex, boolean fromLast) {
        if (this.inventory.isEmpty()) {
            return this.insertItem(stack, startIndex, endIndex, fromLast);
        } else if (ItemStack.areItemsAndComponentsEqual(stack, this.inventory.getTarget())) {
            return this.insertItem(stack, startIndex, endIndex, fromLast);
        } else {
            return false;
        }
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        boolean canInsert = true;
        if (!this.inventory.isEmpty()) {
            if (slotIndex < this.inventory.size() && slotIndex != -999) {
                if (actionType != SlotActionType.THROW) {
                    ItemStack stack;
                    if (actionType == SlotActionType.SWAP && (button >= 0 && button < 9 || button == 40)) {
                        stack = player.getInventory().getStack(button);
                    } else {
                        stack = this.getCursorStack();
                    }

                    if (!stack.isEmpty()) {
                        if (!ItemStack.areItemsAndComponentsEqual(stack, this.inventory.getTarget())) {
                            canInsert = false;
                        }
                    }
                }
            }
        }
        if (canInsert) {
            super.onSlotClick(slotIndex, button, actionType, player);
        }
    }
}
