package mod.journeycreative.blocks;

import mod.journeycreative.Journeycreative;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class ModModelLayers {
    public static final ModelLayerLocation RESEARCH_VESSEL =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(Journeycreative.MOD_ID, "research_vessel"), "main");

    public static void initialize() {
        ModelLayerRegistry.registerModelLayer(RESEARCH_VESSEL, ResearchVesselEntityModel::getTexturedModelData);
    }
}
