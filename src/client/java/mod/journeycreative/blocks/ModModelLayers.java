package mod.journeycreative.blocks;

import mod.journeycreative.Journeycreative;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ModModelLayers {
    public static final EntityModelLayer RESEARCH_VESSEL =
            new EntityModelLayer(Identifier.of(Journeycreative.MOD_ID, "research_vessel"), "main");

    public static void initialize() {
        EntityModelLayerRegistry.registerModelLayer(RESEARCH_VESSEL, ResearchVesselEntityModel::getTexturedModelData);
    }
}
