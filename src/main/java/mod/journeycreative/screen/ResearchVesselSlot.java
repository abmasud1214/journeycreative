package mod.journeycreative.screen;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class ResearchVesselSlot extends Slot {
    public ResearchVesselSlot(Inventory inventory, int i, int j, int k) {
        super(inventory, i, j, k);
    }

    public boolean canInsert(ItemStack stack) {
        return canInsertItem(stack);
    }

    public static boolean canInsertItem(ItemStack stack) {
        boolean nested = stack.getItem().canBeNested();
        if (!nested && stack.contains(DataComponentTypes.CONTAINER)) {
            ContainerComponent container = stack.get(DataComponentTypes.CONTAINER);
            if (container.copyFirstStack().isEmpty()) {
                nested = true;
            }
        }
        return nested;
    }

}
