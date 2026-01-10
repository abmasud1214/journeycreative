package mod.journeycreative.screen;

import mod.journeycreative.Journeycreative;
import mod.journeycreative.ResearchConfig;
import mod.journeycreative.blocks.ResearchVesselInventory;
import mod.journeycreative.networking.JourneyNetworking;
import mod.journeycreative.networking.PlayerUnlocksData;
import mod.journeycreative.networking.StateSaverAndLoader;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.ClickType;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class ResearchVesselScreenHandler extends ScreenHandler {
    public final ResearchVesselInventory inventory;
    private DefaultedList<ResearchVesselSlot> vesselSlots = DefaultedList.of();
    private final Property quantity;
    private final Property capacity;
    private final Property reason;
    private final PlayerEntity player;
    private final World world;
    private Text warning;
    private boolean warningSent = false;

    private ItemStack previousTarget;

    public ResearchVesselScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ResearchVesselInventory.ofSize(27));
    }

    public ResearchVesselScreenHandler(int syncId, PlayerInventory playerInventory, ResearchVesselInventory inventory) {
        super(ModScreens.RESEARCH_VESSEL_SCREEN_HANDLER, syncId);
        checkSize(inventory, 27);
        this.inventory = inventory;
        player = playerInventory.player;
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
        previousTarget = target;
        this.addProperty(this.quantity).set(inventory.getQuantity());
        this.addProperty(this.capacity).set(inventory.getCapacity());
        this.addProperty(this.reason).set(0);
        setReason(target);
    }

    @Override
    public void sendContentUpdates() {
        super.sendContentUpdates();

        if (!warningSent && player instanceof ServerPlayerEntity serverPlayerEntity && world instanceof ServerWorld serverWorld) {
            sendWarningPacket(previousTarget, serverWorld, serverPlayerEntity, true);
            warningSent = true;
        }
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
                if (!this.insertItem(originalStack, this.inventory.size(), this.slots.size(), true)) { // FROM VESSEL TO INVENTORY
                    return ItemStack.EMPTY;
                }
            }

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

        if (this.world instanceof ServerWorld serverWorld && this.player instanceof ServerPlayerEntity serverPlayer) {
            ItemStack target = this.inventory.getTarget();
            sendWarningPacket(target, serverWorld, serverPlayer);
            setReason(target);
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
        if (clickType == ClickType.RIGHT) {
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

    private void sendWarningPacket(ItemStack target, ServerWorld serverWorld, ServerPlayerEntity serverPlayer, boolean init) {
        if (!ItemStack.areItemsAndComponentsEqual(target, previousTarget) || init) {
            previousTarget = target;
            PlayerUnlocksData playerUnlocksData = StateSaverAndLoader.getPlayerState(player);
            List<Identifier> prerequisites = ResearchConfig.RESEARCH_PREREQUISITES.getOrDefault(
                    Registries.ITEM.getId(target.getItem()), new ArrayList<Identifier>()
            );
            ArrayList<Text> prereqs = new ArrayList<>();
            if (!prerequisites.isEmpty()) {
                for (Identifier id : prerequisites) {
                    ItemStack prereqStack = new ItemStack(Registries.ITEM.get(id), 1);
                    if (!playerUnlocksData.isUnlocked(prereqStack, serverWorld.getGameRules().getBoolean(Journeycreative.RESEARCH_ITEMS_UNLOCKED))) {
                        prereqs.add(prereqStack.getName());
                    }
                }
            }
            if (!prereqs.isEmpty()) {
                MutableText prereqText = Text.empty();
                prereqText.append(Text.literal("["));
                prereqText.append(Texts.join(prereqs, Text.literal(", ")));
                prereqText.append(Text.literal("]"));
                Text warning = Text.translatable("item.journeycreative.research_certificate.need_prerequisite", prereqText, target.getName());
                this.warning = warning;
                ServerPlayNetworking.send(
                        serverPlayer,
                        new JourneyNetworking.ItemWarningMessage(warning)
                );
            } else {
                ServerPlayNetworking.send(
                        serverPlayer,
                        new JourneyNetworking.ItemWarningMessage(Text.empty())
                );
            }
        }
    }

    private void sendWarningPacket(ItemStack target, ServerWorld serverWorld, ServerPlayerEntity serverPlayer) {
        sendWarningPacket(target, serverWorld, serverPlayer, false);
    }

    public Text getWarning() {
        return warning;
    }

    public void setWarning(Text warning) {
        this.warning = warning;
    }

    private void addPlayerSlots(PlayerInventory playerInventory, int x, int y) {
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
