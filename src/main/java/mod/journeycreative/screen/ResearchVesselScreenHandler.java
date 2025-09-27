package mod.journeycreative.screen;

import mod.journeycreative.ResearchConfig;
import mod.journeycreative.blocks.ResearchVesselBlockEntity;
import mod.journeycreative.blocks.ResearchVesselInventory;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.ShulkerBoxSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ClickType;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.world.World;

public class ResearchVesselScreenHandler extends ScreenHandler {
    public final ResearchVesselInventory inventory;
    private DefaultedList<ResearchVesselSlot> vesselSlots = DefaultedList.of();
    private final Property quantity;
    private final Property capacity;
    private final Property reason;
    private final World world;

    public ResearchVesselScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ResearchVesselInventory.ofSize(27));
    }

    public ResearchVesselScreenHandler(int syncId, PlayerInventory playerInventory, ResearchVesselInventory inventory) {
        super(ModScreens.RESEARCH_VESSEL_SCREEN_HANDLER, syncId);
        checkSize(inventory, 27);
        this.inventory = inventory;
        world = playerInventory.player.getWorld();

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

        this.quantity = Property.create();
        this.capacity = Property.create();
        this.reason = Property.create();
        ItemStack target = this.inventory.getTarget();
        this.addProperty(this.quantity).set(inventory.getQuantity());
        this.addProperty(this.capacity).set(inventory.getCapacity());
        this.addProperty(this.reason).set(0);
        setReason(target);
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

        if (this.world instanceof ServerWorld) {
            setReason(this.inventory.getTarget());
            this.quantity.set(this.inventory.getQuantity());
            this.capacity.set(this.inventory.getCapacity());
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
            boolean canInsert = true;
            if (stack.isDamageable()) {
                int damage = stack.getDamage();
                int maxDamage = stack.getMaxDamage();
                canInsert = damage == 0;
            } else if (stack.hasEnchantments()) {
                canInsert = false;
            }

            if (canInsert) {
                inserted = inventory.insertIntoInventory(inputStack);
            }
            this.inventory.getTarget();
        } else {
            ItemStack target = this.inventory.getTarget().copy();
            target.remove(DataComponentTypes.REPAIR_COST);
            ItemStack inputStackCopy = inputStack.copy();
            inputStackCopy.remove(DataComponentTypes.REPAIR_COST);
            if (ItemStack.areItemsAndComponentsEqual(target, inputStackCopy)) {
                inserted = inventory.insertIntoInventory(inputStack);
            }
        }
        stack.decrement(inserted);
    }

    private void setReason(ItemStack target) {
        EnderArchiveScreenHandler.researchInvalidReason r = EnderArchiveScreenHandler.researchInvalidReason.VALID;
        if (ResearchConfig.RESEARCH_BLOCKED.contains(Registries.ITEM.getId(target.getItem()))) {
            r = EnderArchiveScreenHandler.researchInvalidReason.BLOCKED;
        } else if (ResearchConfig.RESEARCH_PROHIBITED.contains(Registries.ITEM.getId(target.getItem()))) {
            r = EnderArchiveScreenHandler.researchInvalidReason.PROHIBITED;
        }
        this.reason.set(r.ordinal());
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.inventory.onClose(player);
    }

    public int getInventoryQuantity() {
        return this.quantity.get();
    }

    public int getInventoryCapacity() {
        return this.capacity.get();
    }

    public EnderArchiveScreenHandler.researchInvalidReason getReason() {
        return EnderArchiveScreenHandler.researchInvalidReason.values()[this.reason.get()];
    }
}
