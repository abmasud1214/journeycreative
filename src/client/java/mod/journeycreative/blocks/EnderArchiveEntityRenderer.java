package mod.journeycreative.blocks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mod.journeycreative.Journeycreative;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class EnderArchiveEntityRenderer implements BlockEntityRenderer<EnderArchiveBlockEntity, EnderArchiveEntityRenderer.EnderArchiveRenderState> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Journeycreative.MOD_ID, "textures/block/ender_archive.png");

    public EnderArchiveEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        this(ctx.entityModelSet());
    }

    public EnderArchiveEntityRenderer(EntityModelSet models) {

    }

    @Override
    public EnderArchiveRenderState createRenderState() {
        return new EnderArchiveRenderState();
    }

    @Override
    public void extractRenderState(EnderArchiveBlockEntity blockEntity, EnderArchiveRenderState state, float tickProgress, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(blockEntity, state, crumblingOverlay);

        state.blockPos = blockEntity.getBlockPos();
        state.facing = (Direction) blockEntity.getBlockState().getValueOrElse(EnderArchiveBlock.FACING, Direction.NORTH);
        state.g = blockEntity.getBookTransparency(tickProgress);

        state.lightCoords = LevelRenderer.getLightCoords(
                blockEntity.getLevel(),
                blockEntity.getBlockPos()
        );
    }

    @Override
    public void submit(EnderArchiveRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState camera) {
        float rotationDegrees = switch (state.facing) {
            case NORTH -> 0F;
            case SOUTH -> 180F;
            case WEST -> 90F;
            case EAST -> -90F;
            default -> 0F;
        };

        matrices.pushPose();
        matrices.translate(0.5, 0.5, 0.5);
        matrices.mulPose(new Quaternionf().rotateY((float)Math.toRadians(rotationDegrees)));
        matrices.mulPose(new Quaternionf().rotateZ((float)Math.toRadians(180)));
        matrices.translate(-0.5, -0.5, -0.5);

        queue.submitCustomGeometry(
                matrices,
                RenderTypes.entityTranslucent(TEXTURE),
                (entry, consumer) -> {
                    Vec3i normalVec = state.facing.getUnitVec3i();
                    for (int i = 0; i < 6; i++) {
                        renderbook(
                                entry,
                                consumer,
                                renderPos(i),
                                uvRanges(i),
                                state.g[i],
                                state.lightCoords,
                                normalVec
                        );
                    }
                }
        );

        matrices.popPose();

        queue.submitCustomGeometry(
                matrices,
                RenderTypes.endPortal(),
                (entry, consumer) -> {
                    Matrix4f model = entry.pose();
                    consumer.addVertex(model, No16(1), No16(15.5f), No16(15));
                    consumer.addVertex(model, No16(15), No16(15.5f), No16(15));
                    consumer.addVertex(model, No16(15), No16(15.5f), No16(1));
                    consumer.addVertex(model, No16(1), No16(15.5f), No16(1));
                }
        );
    }

    private void renderbook(PoseStack.Pose entry, VertexConsumer consumer, float[] renderPos, float[] uvRanges, float transparency, int light, Vec3i normal) {
        Matrix4f model = entry.pose();
        consumer.addVertex(model, renderPos[0], renderPos[1], -0.01F)
                .setColor(1, 1, 1, transparency)
                .setUv(uvRanges[0], uvRanges[1])
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(entry, normal.getX(), normal.getY(), normal.getZ());
        consumer.addVertex(model, renderPos[2], renderPos[1], -0.01F)
                .setColor(1, 1, 1, transparency)
                .setUv(uvRanges[2], uvRanges[1])
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(entry, normal.getX(), normal.getY(), normal.getZ());
        consumer.addVertex(model, renderPos[2], renderPos[3], -0.01F)
                .setColor(1, 1, 1, transparency)
                .setUv(uvRanges[2], uvRanges[3])
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(entry, normal.getX(), normal.getY(), normal.getZ());
        consumer.addVertex(model, renderPos[0], renderPos[3], -0.01F)
                .setColor(1, 1, 1, transparency)
                .setUv(uvRanges[0], uvRanges[3])
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(entry, normal.getX(), normal.getY(), normal.getZ());
    }

    private float[] renderPos(int bookNumber) {
        switch (bookNumber) {
            case 0: // 3, 2 - 5, 6
                return new float[]{No16(3), No16(2), No16(6), No16(7)};
            case 1: // 7, 2 - 9, 6
                return new float[]{No16(7), No16(2), No16(10), No16(7)};
            case 2: // 10, 3 - 12, 6
                return new float[]{No16(10), No16(3), No16(13), No16(7)};
            case 3: // 3, 9 - 6, 13
                return new float[]{No16(3), No16(9), No16(7), No16(14)};
            case 4: // 7, 10 - 9, 13
                return new float[]{No16(7), No16(9), No16(10), No16(14)};
            case 5: // 11, 9 - 13, 13
                return new float[]{No16(11), No16(9), No16(14), No16(14)};
            default:
                return new float[]{No16(3), No16(2), No16(6), No16(7)};
        }
    }

    private float[] uvRanges(int bookNumber) {
        switch (bookNumber){
            case 0: // 48, 16 - 50, 20
                return new float[]{48.0F / 64, 16.0F / 64, 51.0F / 64, 21.0F / 64};
            case 1: // 52, 16 - 54, 20
                return new float[]{52.0F / 64, 16.0F / 64, 55.0F / 64, 21.0F / 64};
            case 2: // 55, 17 - 57, 20
                return new float[]{55.0F / 64, 17.0F / 64, 58.0F / 64, 21.0F / 64};
            case 3: // 48, 23 - 51, 27
                return new float[]{48.0F / 64, 23.0F / 64, 52.0F / 64, 28.0F / 64};
            case 4: // 52, 24 - 54, 27
                return new float[]{52.0F / 64, 23.0F / 64, 55.0F / 64, 28.0F / 64};
            case 5: // 56, 23 - 58, 27
                return new float[]{56.0F / 64, 23.0F / 64, 59.0F / 64, 28.0F / 64};
            default:
                return new float[]{48.0F / 64, 16.0F / 64, 51.0F / 64, 21.0F / 64};
        }
    }

    private float No16(float f) {
        return f / 16.0F;
    }

    public class EnderArchiveRenderState extends BlockEntityRenderState {
        public Direction facing;
        public float[] g;
    }
}
