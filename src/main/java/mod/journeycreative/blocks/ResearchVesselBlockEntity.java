package mod.journeycreative.blocks;

import mod.journeycreative.ResearchConfig;
import mod.journeycreative.items.ModComponents;
import mod.journeycreative.screen.ResearchVesselScreenHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ResearchVesselBlockEntity extends RandomizableContainerBlockEntity implements MenuProvider, WorldlyContainer, ResearchVesselInventory {
    private NonNullList<ItemStack> inventory = NonNullList.withSize(27, ItemStack.EMPTY);
    private ItemStack target = ItemStack.EMPTY;
    private int capacity = 0;
    private float animationProgress;
    private float lastAnimationProgress;
    private AnimationStage animationStage;
    private int viewerCount;

    public ResearchVesselBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.RESEARCH_VESSEL_BLOCK_ENTITY, pos, state);
        this.animationStage = ResearchVesselBlockEntity.AnimationStage.CLOSED;
    }

    public NonNullList<ItemStack> getItems() {
        return inventory;
    }

    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new ResearchVesselScreenHandler(syncId, playerInventory, this);
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        super.loadAdditional(view);
        this.readInventoryNbt(view);
    }

    public int size() {
        return this.inventory.size();
    }

    public void readInventoryNbt(ValueInput readView) {
        this.inventory = NonNullList.withSize(this.size(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(readView, this.inventory);
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
        ContainerHelper.saveAllItems(view, this.inventory, false);
    }

    @Override
    public void  preRemoveSideEffects(BlockPos pos, BlockState oldState) {

    }

    protected Component getDefaultName() {
        return Component.translatable("container.journeycreative.research_vessel");
    }

    @Override
    protected void setItems(NonNullList<ItemStack> inventory) {
        this.inventory = inventory;
    }

    @Override
    protected AbstractContainerMenu createMenu(int syncId, Inventory playerInventory) {
        return new ResearchVesselScreenHandler(syncId, playerInventory, this);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return new int[]{};
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction dir) {
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return true;
    }

    @Override
    public ItemStack getTarget() {
        boolean empty = true;
        for (int i = 0; i < size(); i++) {
            ItemStack stack = getItem(i);
            if (!stack.isEmpty()) {
                empty = false;
                target = stack;
                int default_lim = (int) Math.ceil(27 * stack.getMaxStackSize() * ResearchConfig.DEFAULT_AMOUNT_ADJUSTMENT);
                default_lim = Math.max(1, default_lim);
                capacity = ResearchConfig.RESEARCH_AMOUNT_REQUIREMENTS.getOrDefault(
                        BuiltInRegistries.ITEM.getKey(stack.getItem()),default_lim);
                capacity = Math.min(capacity, 27 * stack.getMaxStackSize());
            }
        }

        if (empty) {
            target = ItemStack.EMPTY;
            capacity = 0;
        }

        return target;
    }

    public int getQuantity() {
        return this.countItem(target.getItem());
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    public void refactorInventory(ItemStack stack) {
        int quantity = this.countItem(stack.getItem());
        for (int i = 0; i < 27; i++) {
            if (quantity == 0) {
                inventory.set(i, ItemStack.EMPTY);
            } else {
                int splitNumber = Math.min(stack.getMaxStackSize(), quantity);
                ItemStack split = stack.copyWithCount(splitNumber);
                inventory.set(i, split);
                quantity -= splitNumber;
                quantity = Math.max(quantity, 0);
            }
        }
    }

    @Override
    public int insertIntoInventory(ItemStack stack) {
        int quantity = this.countItem(stack.getItem());
        int default_lim = (int) Math.ceil(27 * stack.getMaxStackSize() * ResearchConfig.DEFAULT_AMOUNT_ADJUSTMENT);
        default_lim = Math.max(1, default_lim);
        int capacity = ResearchConfig.RESEARCH_AMOUNT_REQUIREMENTS.getOrDefault(
                BuiltInRegistries.ITEM.getKey(stack.getItem()),default_lim);
        capacity = Math.min(capacity, 27 * stack.getMaxStackSize());
        int remaining = capacity - quantity;
        int split = Math.min(remaining, stack.getCount());
        quantity += split;
        for (int i = 0; i < 27; i++) {
            if (quantity == 0) {
                inventory.set(i, ItemStack.EMPTY);
            } else {
                int splitNumber = Math.min(stack.getMaxStackSize(), quantity);
                ItemStack splitStack = stack.copyWithCount(splitNumber);
                inventory.set(i, splitStack);
                quantity -= splitNumber;
                quantity = Math.max(quantity, 0);
            }
        }
        return split;
    }

    @Override
    public int countItem(Item item) {
        int c = 0;

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.get(i);
            if (stack.getItem().equals(item)) {
                c += stack.getCount();
            }
        }

        return c;
    }

    public float getAnimationProgress(float tickProgress) {
        return Mth.lerp(tickProgress, this.lastAnimationProgress, this.animationProgress);
    }

    public static void tick(Level world, BlockPos pos, BlockState state, ResearchVesselBlockEntity blockEntity) {
        blockEntity.updateAnimation(world, pos, state);
    }

    private void updateAnimation(Level world, BlockPos pos, BlockState state) {
        this.lastAnimationProgress = this.animationProgress;
        switch (this.animationStage.ordinal()) {
            case 0:
                this.animationProgress = 0.0F;
                break;
            case 1:
                this.animationProgress += 0.1F;
                if (this.lastAnimationProgress == 0.00F) {
                    updateNeighborStates(world, pos, state);
                }

                if (this.animationProgress >= 1.0F) {
                    this.animationStage = ResearchVesselBlockEntity.AnimationStage.OPENED;
                    this.animationProgress = 1.0F;
                    if (!state.getValue(ResearchVesselBlock.OPENED)) {
                        this.level.setBlock(pos, state.setValue(ResearchVesselBlock.OPENED, true), Block.UPDATE_ALL);
                    }

                    updateNeighborStates(world, pos, state);
                }

                this.pushEntities(world, pos, state);
                break;
            case 2:
                this.animationProgress = 1.0F;
                break;
            case 3:
                if (state.getValue(ResearchVesselBlock.OPENED)) {
                    this.level.setBlock(pos, state.setValue(ResearchVesselBlock.OPENED, false), Block.UPDATE_ALL);
                }
                this.animationProgress -= 0.1F;
                if (this.lastAnimationProgress == 1.0F) {
                    updateNeighborStates(world, pos, state);
                }

                if (this.animationProgress <= 0.0F) {
                    this.animationStage = ResearchVesselBlockEntity.AnimationStage.CLOSED;
                    this.animationProgress = 0.0F;
                    updateNeighborStates(world, pos, state);
                }
        }
    }

    public AnimationStage getAnimationStage() {
        return this.animationStage;
    }

    public AABB getBoundingBox(BlockState state) {
        Vec3 vec3d = new Vec3(0, 0.0, 0);
        AABB box = calculateBoundingBox(animationProgress, vec3d);
        return box;
    }

    public static AABB calculateBoundingBox(float animationProgress, Vec3 pos) {
        VoxelShape shape = Block.box(2, 0, 2, 14, 5 + 11 * animationProgress, 14);
        AABB box = shape.bounds().move(pos.x, pos.y, pos.z);
        return box;
    }

    private void pushEntities(Level world, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof ResearchVesselBlock) {
            AABB box = calculateBoundingBox(animationProgress, new Vec3(pos.getX(), pos.getY(), pos.getZ()));
            Direction direction = Direction.UP;
            List<Entity> list = world.getEntities((Entity) null, box);
            if (!list.isEmpty()) {
                java.util.Iterator<Entity> entities = list.iterator();

                while (entities.hasNext()) {
                    Entity entity = (Entity) entities.next();
                    if (entity.getPistonPushReaction() != PushReaction.IGNORE) {
                        entity.move(MoverType.SHULKER_BOX,
                                new Vec3((box.getXsize() - 0.01) * (double) direction.getStepX(),
                                (box.getYsize() + 0.01) * (double) direction.getStepY(),
                                        (box.getZsize() + 0.01) * (double) direction.getStepZ()));
                    }
                 }
            }
        }
    }

    public boolean triggerEvent(int type, int data) {
        if (type == 1) {
            this.viewerCount = data;
            if (data == 0) {
                this.animationStage = ResearchVesselBlockEntity.AnimationStage.CLOSING;
            }

            if (data == 1) {
                this.animationStage = ResearchVesselBlockEntity.AnimationStage.OPENING;
            }

            return true;
        } else {
            return super.triggerEvent(type, data);
        }
    }

    private static void updateNeighborStates(Level world, BlockPos pos, BlockState state) {
        state.updateNeighbourShapes(world, pos, 3);
        world.updateNeighborsAt(pos, state.getBlock());
    }

    public void startOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            if (this.viewerCount < 0) {
                this.viewerCount = 0;
            }

            ++this.viewerCount;
            this.level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 1, this.viewerCount);
            if (this.viewerCount == 1) {
                this.level.gameEvent(player, GameEvent.CONTAINER_OPEN, this.worldPosition);
                //TODO: Add sound event
            }
        }
    }

    public void stopOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            --this.viewerCount;
            this.level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 1, this.viewerCount);
            if (this.viewerCount <= 0) {
                this.level.gameEvent(player, GameEvent.CONTAINER_CLOSE, this.worldPosition);
                //TODO: Add sound event
            }
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        this.target = components.getOrDefault(ModComponents.RESEARCH_VESSEL_TARGET_COMPONENT, ItemStack.EMPTY);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder componentMapBuilder) {
        super.collectImplicitComponents(componentMapBuilder);
        componentMapBuilder.set(ModComponents.RESEARCH_VESSEL_TARGET_COMPONENT, getTarget());
    }

    @Override
    public void removeComponentsFromTag(ValueOutput view) {
        view.discard("target");
    }

    public static enum AnimationStage {
        CLOSED,
        OPENING,
        OPENED,
        CLOSING;

        private AnimationStage() {

        }
    }
}
