package mod.journeycreative.blocks;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.util.math.Direction;

@Environment(EnvType.CLIENT)
public class ResearchVesselEntityRenderState extends EntityRenderState {
    public float openProgress;
    public Direction facing;

    public ResearchVesselEntityRenderState() {
        this.facing = Direction.DOWN;
    }

}
