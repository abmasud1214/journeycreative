package mod.journeycreative.mixin;

import mod.journeycreative.blocks.ResearchVesselBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {

    @Inject(
            method = "canBeNested",
            at = @At("HEAD"),
            cancellable = true
    )
    private void preventResearchBoxNesting(CallbackInfoReturnable<Boolean> cir) {
        BlockItem blockItem = (BlockItem)(Object)this;

        if (blockItem.getBlock() instanceof ResearchVesselBlock) {
            cir.setReturnValue(false);
        }
    }
}
