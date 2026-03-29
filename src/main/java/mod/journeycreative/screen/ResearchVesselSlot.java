package mod.journeycreative.screen;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public class ResearchVesselSlot extends Slot {
    public ResearchVesselSlot(Container inventory, int i, int j, int k) {
        super(inventory, i, j, k);
    }

    public boolean mayPlace(ItemStack stack) {
        return canInsertItem(stack);
    }

    public static boolean canInsertItem(ItemStack stack) {
        boolean nested = stack.getItem().canFitInsideContainerItems();
        if (!nested && stack.has(DataComponents.CONTAINER)) {
            ItemContainerContents container = stack.get(DataComponents.CONTAINER);
            if (container.copyOne().isEmpty()) {
                nested = true;
            }
        }
        return nested;
    }

}
