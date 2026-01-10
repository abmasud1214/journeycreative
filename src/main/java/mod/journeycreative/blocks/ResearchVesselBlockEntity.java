package mod.journeycreative.blocks;

import mod.journeycreative.ResearchConfig;
import mod.journeycreative.items.ModComponents;
import mod.journeycreative.screen.ResearchVesselScreenHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.component.ComponentMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ResearchVesselBlockEntity extends LootableContainerBlockEntity implements NamedScreenHandlerFactory, SidedInventory, ResearchVesselInventory {
    private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(27, ItemStack.EMPTY);
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
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        this.readInventoryNbt(nbt, registries);
    }

    public int size() {
        return this.inventory.size();
    }

    public void readInventoryNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        Inventories.readNbt(nbt, this.inventory, registries);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        Inventories.writeNbt(nbt, this.inventory, registries);
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
                capacity = ResearchConfig.RESEARCH_AMOUNT_REQUIREMENTS.getOrDefault(
                        Registries.ITEM.getId(stack.getItem()),27 * stack.getMaxCount());
                capacity = Math.min(capacity, 27 * stack.getMaxCount());
            }
        }

        if (empty) {
            target = ItemStack.EMPTY;
            capacity = 0;
        }

        return target;
    }

    public int getQuantity() {
        return this.count(target.getItem());
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    public void refactorInventory(ItemStack stack) {
        int quantity = this.count(stack.getItem());
        for (int i = 0; i < 27; i++) {
            if (quantity == 0) {
                inventory.set(i, ItemStack.EMPTY);
            } else {
                int splitNumber = Math.min(stack.getMaxCount(), quantity);
                ItemStack split = stack.copyWithCount(splitNumber);
                inventory.set(i, split);
                quantity -= splitNumber;
                quantity = Math.max(quantity, 0);
            }
        }
    }

    @Override
    public int insertIntoInventory(ItemStack stack) {
        int quantity = this.count(stack.getItem());
        int capacity = ResearchConfig.RESEARCH_AMOUNT_REQUIREMENTS.getOrDefault(
                Registries.ITEM.getId(stack.getItem()),27 * stack.getMaxCount());
        capacity = Math.min(capacity, 27 * stack.getMaxCount());
        int remaining = capacity - quantity;
        int split = Math.min(remaining, stack.getCount());
        quantity += split;
        for (int i = 0; i < 27; i++) {
            if (quantity == 0) {
                inventory.set(i, ItemStack.EMPTY);
            } else {
                int splitNumber = Math.min(stack.getMaxCount(), quantity);
                ItemStack splitStack = stack.copyWithCount(splitNumber);
                inventory.set(i, splitStack);
                quantity -= splitNumber;
                quantity = Math.max(quantity, 0);
            }
        }
        return split;
    }

    public float getAnimationProgress(float tickProgress) {
        return MathHelper.lerp(tickProgress, this.lastAnimationProgress, this.animationProgress);
    }

    public static void tick(World world, BlockPos pos, BlockState state, ResearchVesselBlockEntity blockEntity) {
        blockEntity.updateAnimation(world, pos, state);
    }

    private void updateAnimation(World world, BlockPos pos, BlockState state) {
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
                    if (!state.get(ResearchVesselBlock.OPENED)) {
                        this.world.setBlockState(pos, state.with(ResearchVesselBlock.OPENED, true), Block.NOTIFY_ALL);
                    }

                    updateNeighborStates(world, pos, state);
                }

                this.pushEntities(world, pos, state);
                break;
            case 2:
                this.animationProgress = 1.0F;
                break;
            case 3:
                if (state.get(ResearchVesselBlock.OPENED)) {
                    this.world.setBlockState(pos, state.with(ResearchVesselBlock.OPENED, false), Block.NOTIFY_ALL);
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

    public Box getBoundingBox(BlockState state) {
        Vec3d vec3d = new Vec3d(0, 0.0, 0);
        Box box = calculateBoundingBox(animationProgress, vec3d);
        return box;
    }

    public static Box calculateBoundingBox(float animationProgress, Vec3d pos) {
        VoxelShape shape = Block.createCuboidShape(2, 0, 2, 14, 5 + 11 * animationProgress, 14);
        Box box = shape.getBoundingBox().offset(pos.x, pos.y, pos.z);
        return box;
    }

    private void pushEntities(World world, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof ResearchVesselBlock) {
            Box box = calculateBoundingBox(animationProgress, new Vec3d(pos.getX(), pos.getY(), pos.getZ()));
            Direction direction = Direction.UP;
            List<Entity> list = world.getOtherEntities((Entity) null, box);
            if (!list.isEmpty()) {
                java.util.Iterator<Entity> entities = list.iterator();

                while (entities.hasNext()) {
                    Entity entity = (Entity) entities.next();
                    if (entity.getPistonBehavior() != PistonBehavior.IGNORE) {
                        entity.move(MovementType.SHULKER_BOX,
                                new Vec3d((box.getLengthX() - 0.01) * (double) direction.getOffsetX(),
                                (box.getLengthY() + 0.01) * (double) direction.getOffsetY(),
                                        (box.getLengthZ() + 0.01) * (double) direction.getOffsetZ()));
                    }
                 }
            }
        }
    }

    public boolean onSyncedBlockEvent(int type, int data) {
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
            return super.onSyncedBlockEvent(type, data);
        }
    }

    private static void updateNeighborStates(World world, BlockPos pos, BlockState state) {
        state.updateNeighbors(world, pos, 3);
        world.updateNeighbors(pos, state.getBlock());
    }

    public void onOpen(PlayerEntity player) {
        if (!this.removed && !player.isSpectator()) {
            if (this.viewerCount < 0) {
                this.viewerCount = 0;
            }

            ++this.viewerCount;
            this.world.addSyncedBlockEvent(this.pos, this.getCachedState().getBlock(), 1, this.viewerCount);
            if (this.viewerCount == 1) {
                this.world.emitGameEvent(player, GameEvent.CONTAINER_OPEN, this.pos);
                //TODO: Add sound event
            }
        }
    }

    public void onClose(PlayerEntity player) {
        if (!this.removed && !player.isSpectator()) {
            --this.viewerCount;
            this.world.addSyncedBlockEvent(this.pos, this.getCachedState().getBlock(), 1, this.viewerCount);
            if (this.viewerCount <= 0) {
                this.world.emitGameEvent(player, GameEvent.CONTAINER_CLOSE, this.pos);
                //TODO: Add sound event
            }
        }
    }

    @Override
    protected void readComponents(ComponentsAccess components) {
        super.readComponents(components);
        this.target = components.getOrDefault(ModComponents.RESEARCH_VESSEL_TARGET_COMPONENT, ItemStack.EMPTY);
    }

    @Override
    protected void addComponents(ComponentMap.Builder componentMapBuilder) {
        super.addComponents(componentMapBuilder);
        componentMapBuilder.add(ModComponents.RESEARCH_VESSEL_TARGET_COMPONENT, getTarget());
    }

    @Override
    public void removeFromCopiedStackNbt(NbtCompound nbt) {
        nbt.remove("target");
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
