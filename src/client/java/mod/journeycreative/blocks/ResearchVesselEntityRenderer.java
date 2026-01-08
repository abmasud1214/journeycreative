package mod.journeycreative.blocks;

import mod.journeycreative.Journeycreative;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Objects;
import java.util.Set;

@Environment(EnvType.CLIENT)
public class ResearchVesselEntityRenderer implements BlockEntityRenderer<ResearchVesselBlockEntity, ResearchVesselEntityRenderer.ResearchVesselRenderState> {
    private final ResearchVesselBlockModel model;
    private static final Identifier TEXTURE = Identifier.of(Journeycreative.MOD_ID, "textures/block/research_vessel.png");

    public ResearchVesselEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.model = new ResearchVesselBlockModel(ctx.getLayerModelPart(ModModelLayers.RESEARCH_VESSEL));
    }

    @Override
    public ResearchVesselRenderState createRenderState() {
        return new ResearchVesselRenderState();
    }

    @Override
    public void updateRenderState(ResearchVesselBlockEntity blockEntity, ResearchVesselRenderState state, float tickProgress, Vec3d cameraPos, @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay) {
        BlockEntityRenderer.super.updateRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        state.pos = blockEntity.getPos();
        state.facing = Direction.DOWN;
        state.openness = blockEntity.getAnimationProgress(tickProgress);
        state.showPortal = blockEntity.getAnimationStage() == ResearchVesselBlockEntity.AnimationStage.OPENED;
    }

    @Override
    public void render(ResearchVesselRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState camera) {
        matrices.push();
        this.setTransforms(matrices, state.facing, state.openness);
        queue.submitModel(
                this.model,
                state.openness,
                matrices,
//                RenderLayer.getEntityCutoutNoCull(TEXTURE),
                RenderLayers.entityCutoutNoCull(TEXTURE),
                state.lightmapCoordinates,
                OverlayTexture.DEFAULT_UV,
                0,
                null
        );
        matrices.pop();
        queue.submitCustom(
                matrices,
//                RenderLayer.getEndPortal(),
                RenderLayers.endPortal(),
                ((matricesEntry, vertexConsumer) -> {
                    renderSides(
                            state.showPortal,
                            matricesEntry.getPositionMatrix(),
                            vertexConsumer
                    );
                })
        );
    }

    private void renderSides(boolean show_portal, Matrix4f matrix, VertexConsumer vertexConsumer) {
        this.renderSide(show_portal, matrix, vertexConsumer, No16(4), No16(12), No16(2), No16(14), No16(12), No16(12), Direction.SOUTH);
        this.renderSide(show_portal, matrix, vertexConsumer, No16(4), No16(12), No16(2), No16(14), No16(4), No16(4), Direction.NORTH);
        this.renderSide(show_portal, matrix, vertexConsumer, No16(12), No16(12), No16(2), No16(14), No16(4), No16(12), Direction.EAST);
        this.renderSide(show_portal, matrix, vertexConsumer, No16(4), No16(4), No16(2), No16(14), No16(4), No16(12), Direction.WEST);
    }

    private float No16(float f) {
        return f / 16.0F;
    }

    private void renderSide(boolean show_portal, Matrix4f model, VertexConsumer vertices, float x1, float x2, float y1, float y2, float z1, float z2, Direction side) {
        if (show_portal) {
            vertices.vertex(model, x1, y1, z1);
            vertices.vertex(model, x2, y1, z2);
            vertices.vertex(model, x2, y2, z2);
            vertices.vertex(model, x1, y2, z1);

            vertices.vertex(model, x1, y2, z1);
            vertices.vertex(model, x2, y2, z2);
            vertices.vertex(model, x2, y1, z2);
            vertices.vertex(model, x1, y1, z1);
        }
    }

    private void setTransforms(MatrixStack matrices, Direction facing, float openness) {
        matrices.translate(0.5F, 0.5F, 0.5F);
        float f = 0.9995F;
        matrices.scale(0.9995F, 0.9995F, 0.9995F);
        matrices.scale(1.0F, -1.0F, -1.0F);
        matrices.translate(0.0F, -1.0F, 0.0F);
        this.model.setAngles(openness);
    }

    @Environment(EnvType.CLIENT)
    private static class ResearchVesselBlockModel extends Model<Float> {
        private final ModelPart Top;

        public ResearchVesselBlockModel(ModelPart root) {
            super(root, id -> RenderLayers.entityCutoutNoCull((Identifier) id));
            this.Top = root.getChild("Top");
        }

        public void setAngles(Float openness) {
            super.setAngles(openness);
            this.Top.setOrigin(0.0F, 24.0F - openness * 11.0F, 0.0F);
        }
    }

    public class ResearchVesselRenderState extends BlockEntityRenderState {
        public Direction facing;
        public float openness;
        public boolean showPortal;
    }
}
