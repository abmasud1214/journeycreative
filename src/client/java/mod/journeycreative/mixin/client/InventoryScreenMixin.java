package mod.journeycreative.mixin.client;

import mod.journeycreative.Journeycreative;
import mod.journeycreative.screen.JourneyInventoryScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ScreenPos;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public class InventoryScreenMixin {
    @Inject(method = "init", at = @At("TAIL"))
    private void addJourneyTab(CallbackInfo ci) {
        InventoryScreen screen = (InventoryScreen)(Object)this;
        ScreenPos survivalButtonPos = new ScreenPos(screen.x + 129, screen.height / 2 - 22);

        screen.addDrawableChild(
                new TexturedButtonWidget(survivalButtonPos.x(), survivalButtonPos.y(), 19, 18, JourneyInventoryScreen.JOURNEY_BUTTON_TEXTURES, (button) -> {
                    MinecraftClient.getInstance().setScreen(new JourneyInventoryScreen(MinecraftClient.getInstance().player, FeatureSet.empty(), false));
                })
        );
    }

}
