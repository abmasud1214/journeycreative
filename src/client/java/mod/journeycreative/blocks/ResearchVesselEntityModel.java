package mod.journeycreative.blocks;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;


@Environment(EnvType.CLIENT)
public class ResearchVesselEntityModel extends Model {
    private final ModelPart Bottom;
    private final ModelPart Top;

    public ResearchVesselEntityModel(ModelPart root) {
        super(RenderLayer::getEntityCutout);
        this.Bottom = root.getChild("Bottom");
        this.Top = root.getChild("Top");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData Bottom = modelPartData.addChild("Bottom", ModelPartBuilder.create().uv(0, 0).cuboid(-6.0F, -1.0F, -6.0F, 12.0F, 1.0F, 12.0F, new Dilation(0.0F))
                .uv(0, 32).cuboid(-4.0F, -2.0F, 4.0F, 8.0F, 1.0F, 2.0F, new Dilation(0.0F))
                .uv(20, 32).cuboid(-4.0F, -2.0F, -6.0F, 8.0F, 1.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

        ModelPartData cube_r1 = Bottom.addChild("cube_r1", ModelPartBuilder.create().uv(28, 26).cuboid(-6.0F, -1.0F, -1.0F, 12.0F, 1.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(-5.0F, -1.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        ModelPartData cube_r2 = Bottom.addChild("cube_r2", ModelPartBuilder.create().uv(0, 26).cuboid(-6.0F, -1.0F, -1.0F, 12.0F, 1.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(5.0F, -1.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        ModelPartData Top = modelPartData.addChild("Top", ModelPartBuilder.create().uv(0, 13).cuboid(-6.0F, 4.0F, -6.0F, 12.0F, 1.0F, 12.0F, new Dilation(0.0F))
                .uv(0, 35).cuboid(-4.0F, 3.0F, 4.0F, 8.0F, 1.0F, 2.0F, new Dilation(0.0F))
                .uv(20, 35).cuboid(-4.0F, 3.0F, -6.0F, 8.0F, 1.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 19.0F, 0.0F, 3.1416F, 0.0F, 0.0F));

        ModelPartData cube_r3 = Top.addChild("cube_r3", ModelPartBuilder.create().uv(28, 29).cuboid(-6.0F, 4.0F, -1.0F, 12.0F, 1.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(-5.0F, -1.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        ModelPartData cube_r4 = Top.addChild("cube_r4", ModelPartBuilder.create().uv(0, 29).cuboid(-6.0F, 4.0F, -1.0F, 12.0F, 1.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(5.0F, -1.0F, 0.0F, 0.0F, 1.5708F, 0.0F));
        return TexturedModelData.of(modelData, 64, 64);
    }

    public void setOpenProgress(float progress) {
        float pivotY = 24.0F - progress * 11.0F;
        this.Top.pivotY = pivotY;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
        this.Bottom.render(matrices, vertices, light, overlay, color);
        this.Top.render(matrices, vertices, light, overlay, color);
    }

}
