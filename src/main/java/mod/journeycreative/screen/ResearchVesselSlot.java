package mod.journeycreative.screen;

import net.minecraft.inventory.Inventory;
import net.minecraft.screen.slot.ShulkerBoxSlot;

public class ResearchVesselSlot extends ShulkerBoxSlot {
    private int maxCount = this.inventory.getMaxCountPerStack();

    public ResearchVesselSlot(Inventory inventory, int i, int j, int k) {
        super(inventory, i, j, k);
    }

    @Override
    public int getMaxItemCount() {
        return maxCount;
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }


}
