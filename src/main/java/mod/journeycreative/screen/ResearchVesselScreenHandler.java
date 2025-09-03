package mod.journeycreative.screen;

import mod.journeycreative.ResearchConfig;
import mod.journeycreative.blocks.ResearchVesselBlockEntity;
import mod.journeycreative.blocks.ResearchVesselInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.ShulkerBoxSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.ClickType;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;

public class ResearchVesselScreenHandler extends ScreenHandler {
    private final ResearchVesselInventory inventory;
    private DefaultedList<ResearchVesselSlot> vesselSlots = DefaultedList.of();

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
                ResearchVesselSlot slot = new ResearchVesselSlot(inventory, l + m * 9, 8 + l * 18, 18 + m * 18);
                this.addSlot(slot);
                vesselSlots.add(slot);
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
                if (!this.insertItem(originalStack, this.inventory.size(), this.slots.size(), true)) { // FROM VESSEL TO INVENTORY
                    return ItemStack.EMPTY;
                }
//            } else if (!this.insertItem(originalStack, 0, this.inventory.size(), false)) {
            }
            // ONLY NEED TO DO QUICK MOVE FROM CONTAINER INVENTORY
            // BECAUSE quickMove IS CALLED BY ScreenHandler.onSlotClick
            // WHICH WE DON'T CALL ON QUICK MOVE ACTION FROM PLAYER INVENTORY.

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return newStack;
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        boolean canInsert = true;

        ItemStack stack = isInsertAction(slotIndex, button, actionType, player);
        if (!stack.isEmpty()) {
            try {
                onContainerInsertClick(slotIndex, button, actionType, player, stack);
            } catch (Exception var8) {
                Exception exception = var8;
                CrashReport crashReport = CrashReport.create(exception, "Container click");
                CrashReportSection crashReportSection = crashReport.addElement("Click info");
                crashReportSection.add("Menu Type", () -> {
                    return ModScreens.RESEARCH_VESSEL_SCREEN_HANDLER != null ? Registries.SCREEN_HANDLER.getId(ModScreens.RESEARCH_VESSEL_SCREEN_HANDLER).toString() : "<no type>";
                });
                crashReportSection.add("Menu Class", () -> {
                    return this.getClass().getCanonicalName();
                });
                crashReportSection.add("Slot Count", this.slots.size());
                crashReportSection.add("Slot", slotIndex);
                crashReportSection.add("Button", button);
                crashReportSection.add("Type", actionType);
                throw new CrashException(crashReport);
            }
        } else {
            super.onSlotClick(slotIndex, button, actionType, player);
            ItemStack target = inventory.getTarget();
            this.inventory.refactorInventory(target);
        }
    }

    private ItemStack isInsertAction(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (slotIndex < this.inventory.size() && slotIndex != -999) {
            if (actionType != SlotActionType.THROW) {
                ItemStack stack;
                if (actionType == SlotActionType.SWAP && (button >= 0 && button < 9 || button == 40)) { // Press F or 0-9
                    stack = player.getInventory().getStack(button);
                } else {
                    stack = this.getCursorStack();
                }

                return stack;
            }
        } else if (slotIndex >= this.inventory.size()) {
            if (actionType == SlotActionType.QUICK_MOVE) {
                Slot slot = this.slots.get(slotIndex);
                return slot.getStack();
            }
        }
        return ItemStack.EMPTY;
    }

    private void onContainerInsertClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, ItemStack stack) {
        if (!ResearchVesselSlot.canInsertItem(stack)) {
            return;
        }

        if (actionType == SlotActionType.SWAP && (button == 40)) { // BLOCK SWAP FROM F KEY BECAUSE OF WEIRD BUG.
            return;
        }

        ClickType clickType = button == 0 ? ClickType.LEFT : ClickType.RIGHT;
        ItemStack inputStack;
        if (actionType != SlotActionType.QUICK_MOVE && clickType == ClickType.LEFT) {
            inputStack = stack.copyWithCount(1);
        } else {
            inputStack = stack;
        }

        int inserted = 0;
        if (inventory.isEmpty()) {
            inserted = inventory.insertIntoInventory(inputStack);
        } else {
            ItemStack target = this.inventory.getTarget();
            if (ItemStack.areItemsAndComponentsEqual(target, inputStack)) {
                inserted = inventory.insertIntoInventory(inputStack);
            }
        }
        stack.decrement(inserted);
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.inventory.onClose(player);
    }
}
