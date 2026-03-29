package mod.journeycreative.blocks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mod.journeycreative.Journeycreative;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class ResearchVesselEntityRenderer implements BlockEntityRenderer<ResearchVesselBlockEntity, ResearchVesselEntityRenderState> {
    private final ResearchVesselEntityModel model;
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Journeycreative.MOD_ID, "textures/block/research_vessel.png");

    public ResearchVesselEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        this(ctx.entityModelSet());
    }

    public ResearchVesselEntityRenderer(EntityModelSet loader) {
        this.model = new ResearchVesselEntityModel(loader.bakeLayer(ModModelLayers.RESEARCH_VESSEL));
    }

    @Override
    public ResearchVesselEntityRenderState createRenderState() {
        return new ResearchVesselEntityRenderState();
    }

    @Override
    public void extractRenderState(ResearchVesselBlockEntity blockEntity, ResearchVesselEntityRenderState state, float tickProgress, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        state.blockPos = blockEntity.getBlockPos();
        state.facing = Direction.DOWN;
        state.openProgress = blockEntity.getAnimationProgress(tickProgress);
        state.showPortal = blockEntity.getAnimationStage() == ResearchVesselBlockEntity.AnimationStage.OPENED;
    }

    @Override
    public void submit(ResearchVesselEntityRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState camera) {
        matrices.pushPose();
        this.setTransforms(matrices, state);
        queue.submitModel(
                this.model,
                state,
                matrices,
                RenderTypes.entityCutout(TEXTURE),
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                0,
                null
        );
        matrices.popPose();
        queue.submitCustomGeometry(
                matrices,
                RenderTypes.endPortal(),
                ((matricesEntry, vertexConsumer) -> {
                    renderSides(
                            state.showPortal,
                            matricesEntry.pose(),
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
            vertices.addVertex(model, x1, y1, z1);
            vertices.addVertex(model, x2, y1, z2);
            vertices.addVertex(model, x2, y2, z2);
            vertices.addVertex(model, x1, y2, z1);

            vertices.addVertex(model, x1, y2, z1);
            vertices.addVertex(model, x2, y2, z2);
            vertices.addVertex(model, x2, y1, z2);
            vertices.addVertex(model, x1, y1, z1);
        }
    }

    private void setTransforms(PoseStack matrices, ResearchVesselEntityRenderState state) {
        matrices.translate(0.5F, 0.5F, 0.5F);
        float f = 0.9995F;
        matrices.scale(0.9995F, 0.9995F, 0.9995F);
        matrices.scale(1.0F, -1.0F, -1.0F);
        matrices.translate(0.0F, -1.0F, 0.0F);
        this.model.setupAnim(state);
    }
}
