package mod.journeycreative.screen;

import mod.journeycreative.Journeycreative;
import mod.journeycreative.ResearchConfig;
import mod.journeycreative.blocks.ResearchVesselInventory;
import mod.journeycreative.networking.JourneyNetworking;
import mod.journeycreative.networking.PlayerUnlocksData;
import mod.journeycreative.networking.StateSaverAndLoader;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class ResearchVesselScreenHandler extends AbstractContainerMenu {
    public final ResearchVesselInventory inventory;
    private NonNullList<ResearchVesselSlot> vesselSlots = NonNullList.create();
    private final DataSlot quantity;
    private final DataSlot capacity;
    private final DataSlot reason;
    private final Player player;
    private final Level world;
    private Component warning;
    private boolean warningSent = false;

    private ItemStack previousTarget;

    public ResearchVesselScreenHandler(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, ResearchVesselInventory.ofSize(27));
    }

    public ResearchVesselScreenHandler(int syncId, Inventory playerInventory, ResearchVesselInventory inventory) {
        super(ModScreens.RESEARCH_VESSEL_SCREEN_HANDLER, syncId);
        checkContainerSize(inventory, 27);
        this.inventory = inventory;
        player = playerInventory.player;
        world = playerInventory.player.level();

        inventory.startOpen(playerInventory.player);

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

        this.quantity = DataSlot.standalone();
        this.capacity = DataSlot.standalone();
        this.reason = DataSlot.standalone();
        ItemStack target = this.inventory.getTarget();
        previousTarget = target;
        this.addDataSlot(this.quantity).set(inventory.getQuantity());
        this.addDataSlot(this.capacity).set(inventory.getCapacity());
        this.addDataSlot(this.reason).set(0);
        setReason(target);
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        if (!warningSent && player instanceof ServerPlayer serverPlayerEntity && world instanceof ServerLevel serverWorld) {
            sendWarningPacket(previousTarget, serverWorld, serverPlayerEntity, true);
            warningSent = true;
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.inventory.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasItem()) {
            ItemStack originalStack = slot.getItem();
            newStack = originalStack.copy();
            if (invSlot < this.inventory.getContainerSize()) {
                if (!this.moveItemStackTo(originalStack, this.inventory.getContainerSize(), this.slots.size(), true)) { // FROM VESSEL TO INVENTORY
                    return ItemStack.EMPTY;
                }
            }
            // ONLY NEED TO DO QUICK MOVE FROM CONTAINER INVENTORY
            // BECAUSE quickMove IS CALLED BY ScreenHandler.onSlotClick
            // WHICH WE DON'T CALL ON QUICK MOVE ACTION FROM PLAYER INVENTORY.

            if (originalStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return newStack;
    }

    @Override
    public void clicked(int slotIndex, int button, ContainerInput actionType, Player player) {
        ItemStack stack = isInsertAction(slotIndex, button, actionType, player);
        if (!stack.isEmpty()) {
            try {
                onContainerInsertClick(slotIndex, button, actionType, player, stack);
            } catch (Exception var8) {
                Exception exception = var8;
                CrashReport crashReport = CrashReport.forThrowable(exception, "Container click");
                CrashReportCategory crashReportSection = crashReport.addCategory("Click info");
                crashReportSection.setDetail("Menu Type", () -> {
                    return ModScreens.RESEARCH_VESSEL_SCREEN_HANDLER != null ? BuiltInRegistries.MENU.getKey(ModScreens.RESEARCH_VESSEL_SCREEN_HANDLER).toString() : "<no type>";
                });
                crashReportSection.setDetail("Menu Class", () -> {
                    return this.getClass().getCanonicalName();
                });
                crashReportSection.setDetail("Slot Count", this.slots.size());
                crashReportSection.setDetail("Slot", slotIndex);
                crashReportSection.setDetail("Button", button);
                crashReportSection.setDetail("Type", actionType);
                throw new ReportedException(crashReport);
            }
        } else {
            super.clicked(slotIndex, button, actionType, player);
            ItemStack target = inventory.getTarget();
            this.inventory.refactorInventory(target);
        }

        if (this.world instanceof ServerLevel serverWorld && this.player instanceof ServerPlayer serverPlayer) {
            ItemStack target = this.inventory.getTarget();
            sendWarningPacket(target, serverWorld, serverPlayer);
            setReason(target);
            this.quantity.set(this.inventory.getQuantity());
            this.capacity.set(this.inventory.getCapacity());
        }
    }

    private ItemStack isInsertAction(int slotIndex, int button, ContainerInput actionType, Player player) {
        if (slotIndex < this.inventory.getContainerSize() && slotIndex != -999) {
            if (actionType != ContainerInput.THROW) {
                ItemStack stack;
                if (actionType == ContainerInput.SWAP && (button >= 0 && button < 9 || button == 40)) { // Press F or 0-9
                    stack = player.getInventory().getItem(button);
                } else {
                    stack = this.getCarried();
                }

                return stack;
            }
        } else if (slotIndex >= this.inventory.getContainerSize()) {
            if (actionType == ContainerInput.QUICK_MOVE) {
                Slot slot = this.slots.get(slotIndex);
                return slot.getItem();
            }
        }
        return ItemStack.EMPTY;
    }

    private void onContainerInsertClick(int slotIndex, int button, ContainerInput actionType, Player player, ItemStack stack) {
        if (!ResearchVesselSlot.canInsertItem(stack)) {
            return;
        }

        if (actionType == ContainerInput.SWAP && (button == 40)) { // BLOCK SWAP FROM F KEY BECAUSE OF WEIRD BUG.
            return;
        }

        ClickAction clickType = button == 0 ? ClickAction.PRIMARY : ClickAction.SECONDARY;
        ItemStack inputStack;
        if (clickType == ClickAction.SECONDARY) {
            inputStack = stack.copyWithCount(1);
        } else {
            inputStack = stack;
        }

        int inserted = 0;
        if (inventory.isEmpty()) {
            boolean canInsert = true;
            if (stack.isDamageableItem()) {
                int damage = stack.getDamageValue();
                int maxDamage = stack.getMaxDamage();
                canInsert = damage == 0;
            } else if (stack.isEnchanted()) {
                canInsert = false;
            }

            if (canInsert) {
                inserted = inventory.insertIntoInventory(inputStack);
            }
            this.inventory.getTarget();
        } else {
            ItemStack target = this.inventory.getTarget().copy();
            target.remove(DataComponents.REPAIR_COST);
            ItemStack inputStackCopy = inputStack.copy();
            inputStackCopy.remove(DataComponents.REPAIR_COST);
            if (ItemStack.isSameItemSameComponents(target, inputStackCopy)) {
                inserted = inventory.insertIntoInventory(inputStack);
            }
        }
        stack.shrink(inserted);
    }

    private void setReason(ItemStack target) {
        EnderArchiveScreenHandler.researchInvalidReason r = EnderArchiveScreenHandler.researchInvalidReason.VALID;
        if (ResearchConfig.RESEARCH_BLOCKED.contains(BuiltInRegistries.ITEM.getKey(target.getItem()))) {
            r = EnderArchiveScreenHandler.researchInvalidReason.BLOCKED;
        } else if (ResearchConfig.RESEARCH_PROHIBITED.contains(BuiltInRegistries.ITEM.getKey(target.getItem()))) {
            r = EnderArchiveScreenHandler.researchInvalidReason.PROHIBITED;
        }
        this.reason.set(r.ordinal());
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.inventory.stopOpen(player);
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

    private void sendWarningPacket(ItemStack target, ServerLevel serverWorld, ServerPlayer serverPlayer, boolean init) {
        if (!ItemStack.isSameItemSameComponents(target, previousTarget) || init) {
            previousTarget = target;
            PlayerUnlocksData playerUnlocksData = StateSaverAndLoader.getPlayerState(player);
            List<Identifier> prerequisites = ResearchConfig.RESEARCH_PREREQUISITES.getOrDefault(
                    BuiltInRegistries.ITEM.getKey(target.getItem()), new ArrayList<Identifier>()
            );
            ArrayList<Component> prereqs = new ArrayList<>();
            if (!prerequisites.isEmpty()) {
                for (Identifier id : prerequisites) {
                    ItemStack prereqStack = new ItemStack(BuiltInRegistries.ITEM.getValue(id), 1);
                    if (!playerUnlocksData.isUnlocked(prereqStack,
                            serverWorld.getGameRules().get(Journeycreative.RESEARCH_ITEMS_UNLOCKED))) {
                        prereqs.add(prereqStack.getItemName());
                    }
                }
            }
            if (!prereqs.isEmpty()) {
                MutableComponent prereqText = Component.empty();
                prereqText.append(Component.literal("["));
                prereqText.append(ComponentUtils.formatList(prereqs, Component.literal(", ")));
                prereqText.append(Component.literal("]"));
                Component warning = Component.translatable("item.journeycreative.research_certificate.need_prerequisite", prereqText, target.getItemName());
                this.warning = warning;
                ServerPlayNetworking.send(
                        serverPlayer,
                        new JourneyNetworking.ItemWarningMessage(warning)
                );
            } else {
                ServerPlayNetworking.send(
                        serverPlayer,
                        new JourneyNetworking.ItemWarningMessage(Component.empty())
                );
            }
        }
    }

    private void sendWarningPacket(ItemStack target, ServerLevel serverWorld, ServerPlayer serverPlayer) {
        sendWarningPacket(target, serverWorld, serverPlayer, false);
    }

    public Component getWarning() {
        return warning;
    }

    public void setWarning(Component warning) {
        this.warning = warning;
    }

    private void addPlayerSlots(Inventory playerInventory, int x, int y) {
        int m;
        int l;
        // Player Inventory (3 rows)
        for (m = 0; m < 3; ++m) {
            for (l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + m * 9 + 9, x + l * 18, y + m * 18));
            }
        }
        // Player Hotbar
        for (m = 0; m < 9; ++m) {
            this.addSlot(new Slot(playerInventory, m, x + m * 18, y + 58));
        }
    }
}
