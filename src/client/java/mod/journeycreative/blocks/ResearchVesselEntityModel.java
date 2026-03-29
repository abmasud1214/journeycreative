package mod.journeycreative.blocks;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class ResearchVesselEntityModel extends Model<ResearchVesselEntityRenderState> {
    private final ModelPart Bottom;
    private final ModelPart Top;

    public ResearchVesselEntityModel(ModelPart root) {
        super(root, id -> RenderTypes.entityCutout((Identifier) id));
        this.Bottom = root.getChild("Bottom");
        this.Top = root.getChild("Top");
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition PartDefinition = modelData.getRoot();
        PartDefinition Bottom = PartDefinition.addOrReplaceChild("Bottom", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -1.0F, -6.0F, 12.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(0, 32).addBox(-4.0F, -2.0F, 4.0F, 8.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(20, 32).addBox(-4.0F, -2.0F, -6.0F, 8.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition cube_r1 = Bottom.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(28, 26).addBox(-6.0F, -1.0F, -1.0F, 12.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, -1.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition cube_r2 = Bottom.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 26).addBox(-6.0F, -1.0F, -1.0F, 12.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, -1.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition Top = PartDefinition.addOrReplaceChild("Top", CubeListBuilder.create().texOffs(0, 13).addBox(-6.0F, 4.0F, -6.0F, 12.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(0, 35).addBox(-4.0F, 3.0F, 4.0F, 8.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(20, 35).addBox(-4.0F, 3.0F, -6.0F, 8.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 19.0F, 0.0F, 3.1416F, 0.0F, 0.0F));

        PartDefinition cube_r3 = Top.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(28, 29).addBox(-6.0F, 4.0F, -1.0F, 12.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, -1.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition cube_r4 = Top.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 29).addBox(-6.0F, 4.0F, -1.0F, 12.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, -1.0F, 0.0F, 0.0F, 1.5708F, 0.0F));
        return LayerDefinition.create(modelData, 64, 64);
    }

    @Override
    public void setupAnim(ResearchVesselEntityRenderState researchVesselEntityRenderState) {
        super.setupAnim(researchVesselEntityRenderState);

        this.Top.resetPose();
        this.Bottom.resetPose();

        float f = researchVesselEntityRenderState.openProgress * 11.0F;
        this.Top.y = 24.0F - f;
    }

}
