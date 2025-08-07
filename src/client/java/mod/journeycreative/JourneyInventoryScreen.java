package mod.journeycreative;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

public class JourneyInventoryScreen extends Screen {

    private final PlayerEntity player;

    public JourneyInventoryScreen(PlayerEntity player) {
        super(Text.literal("Journey Inventory"));
        this.player = player;
    }

    @Override
    protected  void init() {
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Back to Survival"), button -> {
            MinecraftClient.getInstance().setScreen(new InventoryScreen(player));
        }).dimensions(this.width / 2 - 40, this.height - 30, 80, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0xC0101010);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }
}
