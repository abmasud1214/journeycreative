package mod.journeycreative.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EnderArchiveBlockEntity extends BlockEntity {
    private BookPresent[] booksPresent = new BookPresent[6];
    private float[] animationProgress = new float[6];
    private float[] lastAnimationProgress = new float[6];
    private float[] targetTransparency = new float[6];
    private float[] timeUntilGone = new float[6];
    private float timeUntilNextBook = (float) Math.random() * (20 * 30.0F);

    public EnderArchiveBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.ENDER_ARCHIVE_BLOCK_ENTITY, pos, state);
        for (int i = 0; i < 6; i++) {
            booksPresent[i] = BookPresent.GONE;
            lastAnimationProgress[i] = 0.0F;
            animationProgress[i] = 0.0F;
            targetTransparency[i] = 0.0F;
        }
    }

    public static void tick(World world, BlockPos pos, BlockState state, EnderArchiveBlockEntity blockEntity) {
        blockEntity.updateAnimation(world, pos, state);
    }

    private void updateAnimation(World world, BlockPos pos, BlockState state) {
        int book = -1;
        timeUntilNextBook -= 1;
        if (timeUntilNextBook < 0.0F) {
            book = (int) Math.floor((Math.random() * 6));
            timeUntilNextBook = 20 * 30.0F;
        }
        for (int i = 0; i < 6; i++) {
            this.lastAnimationProgress[i] = this.animationProgress[i];
            switch (this.booksPresent[i].ordinal()) {
                case 0: // Book not displaying
                    this.animationProgress[i] = 0.0F;
                    if (i == book) {
                        this.booksPresent[i] = BookPresent.APPEARING;
                        this.targetTransparency[i] = (float) (Math.random() * 0.6 + 0.4); // between .4 and .6
                        this.timeUntilGone[i] = 20 * 120 * this.targetTransparency[i];
//                        this.targetTransparency[i] = 1;
                    }
                    break;
                case 1: // Book appearing
                    this.animationProgress[i] += 0.02F;
                    if (this.animationProgress[i] >= 1.0F) {
                        this.animationProgress[i] = 1.0F;
                        this.booksPresent[i] = BookPresent.PRESENT;
                    }
                    break;
                case 2: // Book present
                    this.animationProgress[i] = 1.0F;
                    this.timeUntilGone[i] -= 1;
                    if (timeUntilGone[i] <= 0.0F) {
                        this.booksPresent[i] = BookPresent.DISAPPEARING;
                    }
                    break;
                case 3: // Book disappearing
                    this.animationProgress[i] -= 0.02F;
                    if (this.animationProgress[i] <= 0.0F) {
                        this.animationProgress[i] = 0.0F;
                        this.targetTransparency[i] = 0.0F;
                        this.booksPresent[i] = BookPresent.GONE;
                    }
                    break;
            }
        }
    }

    public float[] getBookTransparency(float tickProgress) {
        float[] transparency = new float[6];
        for (int i = 0; i < 6; i++) {
            transparency[i] = MathHelper.lerp(tickProgress, this.lastAnimationProgress[i], this.animationProgress[i]) * targetTransparency[i];
        }
        return transparency;
    }


    public static enum BookPresent {
        GONE,
        APPEARING,
        PRESENT,
        DISAPPEARING;

        private BookPresent() {

        }
    }

}
