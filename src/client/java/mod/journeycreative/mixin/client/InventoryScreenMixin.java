package mod.journeycreative.mixin.client;

import mod.journeycreative.screen.JourneyInventoryScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ScreenPos;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.resource.featuretoggle.FeatureSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public class InventoryScreenMixin {
    private TexturedButtonWidget journeyButton;

    @Inject(method = "init", at = @At("TAIL"))
    private void addJourneyTab(CallbackInfo ci) {
        InventoryScreen screen = (InventoryScreen)(Object)this;
        ScreenPos survivalButtonPos = new ScreenPos(screen.x + 129, screen.height / 2 - 22);

        journeyButton = new TexturedButtonWidget(survivalButtonPos.x(), survivalButtonPos.y(), 20, 18, JourneyInventoryScreen.JOURNEY_BUTTON_TEXTURES, (button) -> {
            MinecraftClient.getInstance().setScreen(new JourneyInventoryScreen(MinecraftClient.getInstance().player, FeatureSet.empty(), false));
        });

        screen.addDrawableChild(journeyButton);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void updateJourneyButtonPosition(CallbackInfo ci) {
        if (journeyButton != null) {
            InventoryScreen screen = (InventoryScreen)(Object)this;
            journeyButton.setPosition(screen.x + 129, screen.height / 2 - 22);
        }
    }

}
