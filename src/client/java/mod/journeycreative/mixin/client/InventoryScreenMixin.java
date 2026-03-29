package mod.journeycreative.mixin.client;

import mod.journeycreative.screen.JourneyInventoryScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.flag.FeatureFlagSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public class InventoryScreenMixin {
    private ImageButton journeyButton;

    @Inject(method = "init", at = @At("TAIL"))
    private void addJourneyTab(CallbackInfo ci) {
        InventoryScreen screen = (InventoryScreen)(Object)this;
        ScreenPosition survivalButtonPos = new ScreenPosition(screen.leftPos + 129, screen.height / 2 - 22);

        journeyButton = new ImageButton(survivalButtonPos.x(), survivalButtonPos.y(), 20, 18, JourneyInventoryScreen.JOURNEY_BUTTON_TEXTURES, (button) -> {
            Minecraft.getInstance().setScreen(new JourneyInventoryScreen(Minecraft.getInstance().player, FeatureFlagSet.of(), false));
        });

        screen.addRenderableWidget(journeyButton);
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void updateJourneyButtonPosition(CallbackInfo ci) {
        if (journeyButton != null) {
            InventoryScreen screen = (InventoryScreen)(Object)this;
            journeyButton.setPosition(screen.leftPos + 129, screen.height / 2 - 22);
        }
    }

}
