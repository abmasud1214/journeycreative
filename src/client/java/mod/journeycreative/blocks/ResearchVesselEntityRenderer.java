package mod.journeycreative.blocks;

import mod.journeycreative.Journeycreative;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Objects;
import java.util.Set;

@Environment(EnvType.CLIENT)
public class ResearchVesselEntityRenderer implements BlockEntityRenderer<ResearchVesselBlockEntity> {
    private final ResearchVesselBlockModel model;
    private static final Identifier TEXTURE = Identifier.of(Journeycreative.MOD_ID, "textures/block/research_vessel.png");

    public ResearchVesselEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this(ctx.getLoadedEntityModels());
    }

    public ResearchVesselEntityRenderer(LoadedEntityModels models) {
        this.model = new ResearchVesselBlockModel(models.getModelPart(ModModelLayers.RESEARCH_VESSEL));
    }

    @Override
    public void render(ResearchVesselBlockEntity researchVesselBlockEntity, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j) {
        Direction direction = Direction.UP;

        float g = researchVesselBlockEntity.getAnimationProgress(f);
        boolean portal = researchVesselBlockEntity.getAnimationStage() == ResearchVesselBlockEntity.AnimationStage.OPENED;
        this.render(matrixStack, vertexConsumerProvider, i, j, direction, g, portal);
    }

    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Direction facing, float openness, boolean show_portal) {
        matrices.push();
        this.setTransforms(matrices, facing, openness);
        ResearchVesselBlockModel blockModel = this.model;
        Objects.requireNonNull(blockModel);
//        VertexConsumer vertexConsumer = textureId.getVertexConsumer(vertexConsumers, blockModel::getLayer);
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(TEXTURE));
        this.model.render(matrices, vertexConsumer, light, overlay);
        matrices.pop();
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        this.renderSides(show_portal, matrix4f, vertexConsumers.getBuffer(RenderLayer.getEndPortal()));
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
        this.model.animateTop(openness);
    }

    @Environment(EnvType.CLIENT)
    private static class ResearchVesselBlockModel extends Model {
        private final ModelPart Top;

        public ResearchVesselBlockModel(ModelPart root) {
            super(root, RenderLayer::getEntityCutoutNoCull);
            this.Top = root.getChild("Top");
        }

        public void animateTop(float openness) {
            this.Top.setPivot(0.0F, 24.0F - openness * 11.0F, 0.0F);
        }
    }
}
