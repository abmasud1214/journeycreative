package mod.journeycreative.blocks;

import mod.journeycreative.Journeycreative;
import mod.journeycreative.screen.ResearchVesselScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public class ResearchVesselBlockEntity extends LootableContainerBlockEntity implements NamedScreenHandlerFactory, SidedInventory, ResearchVesselInventory {
    private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(27, ItemStack.EMPTY);
    private ItemStack target = ItemStack.EMPTY;


    public ResearchVesselBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.RESEARCH_VESSEL_BLOCK_ENTITY, pos, state);
    }

    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    public Text getDisplayName() {
        return Text.translatable(getCachedState().getBlock().getTranslationKey());
    }

    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new ResearchVesselScreenHandler(syncId, playerInventory, this);
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        this.readInventoryNbt(view);
    }

    public int size() {
        return this.inventory.size();
    }

    public void readInventoryNbt(ReadView readView) {
        this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        Inventories.readData(readView, this.inventory);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        Inventories.writeData(view, this.inventory, false);
    }

    @Override
    public void  onBlockReplaced(BlockPos pos, BlockState oldState) {

    }

    protected Text getContainerName() {
        return Text.translatable("container.journeycreative.research_vessel");
    }

    @Override
    protected DefaultedList<ItemStack> getHeldStacks() {
        return this.inventory;
    }

    @Override
    protected void setHeldStacks(DefaultedList<ItemStack> inventory) {
        this.inventory = inventory;
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new ResearchVesselScreenHandler(syncId, playerInventory, this);
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return new int[]{};
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return false;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return true;
    }

    @Override
    public ItemStack getTarget() {
        boolean empty = true;
        for (int i = 0; i < size(); i++) {
            ItemStack stack = getStack(i);
            if (!stack.isEmpty()) {
                empty = false;
                target = stack;
            }
        }

        if (empty) {
            target = ItemStack.EMPTY;
        }

        return target;
    }
}
