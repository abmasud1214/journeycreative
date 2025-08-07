package mod.journeycreative.mixin.client;

import mod.journeycreative.JourneyInventoryScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public class InventoryScreenMixin {

    @Inject(method = "init", at = @At("TAIL"))
    private void addJourneyTab(CallbackInfo ci) {
        System.out.println("[DEBUG InventoryScreenMixin injected!");

        InventoryScreen screen = (InventoryScreen)(Object)this;

        screen.addDrawableChild(
                ButtonWidget.builder(Text.literal("Journey"), button -> {
                    MinecraftClient.getInstance().setScreen(new JourneyInventoryScreen(MinecraftClient.getInstance().player));
                }).dimensions(screen.x + 10, screen.y - 20, 60, 20).build()
        );

        screen.addDrawableChild(
                ButtonWidget.builder(Text.literal("Survival"), button -> {
                    MinecraftClient.getInstance().setScreen(new InventoryScreen(MinecraftClient.getInstance().player));
                }).dimensions(screen.x + 75, screen.y - 20, 60, 20).build()
        );
    }

}
