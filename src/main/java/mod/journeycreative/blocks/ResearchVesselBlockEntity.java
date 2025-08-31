package mod.journeycreative.blocks;

import mod.journeycreative.Journeycreative;
import mod.journeycreative.items.ModComponents;
import mod.journeycreative.screen.ResearchVesselScreenHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

public class ResearchVesselBlockEntity extends LootableContainerBlockEntity implements NamedScreenHandlerFactory, SidedInventory, ResearchVesselInventory {
    private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(27, ItemStack.EMPTY);
    private ItemStack target = ItemStack.EMPTY;
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
            Box box = calculateBoundingBox(animationProgress, pos.toBottomCenterPos());
            Direction direction = Direction.UP;
            List<Entity> list = world.getOtherEntities((Entity) null, box);
            if (!list.isEmpty()) {
                java.util.Iterator<Entity> entities = list.iterator();

                while (entities.hasNext()) {
                    Entity entity = (Entity) entities.next();
                    if (entity.getPistonBehavior() != PistonBehavior.IGNORE) {
                        entity.move(MovementType.SHULKER_BOX,
                                new Vec3d((box.getLengthX() + 0.01) * (double) direction.getOffsetX(),
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
    public void removeFromCopiedStackData(WriteView view) {
        view.remove("target");
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
