package mod.journeycreative.screen;

import mod.journeycreative.Journeycreative;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.ForgingScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class EnderArchiveScreen extends ForgingScreen<EnderArchiveScreenHandler> {
    private static final Identifier ERROR_TEXTURE = Identifier.ofVanilla("container/smithing/error");
    private final Identifier texture = Identifier.of(Journeycreative.MOD_ID, "textures/gui/ender_archive.png");

    public EnderArchiveScreen(EnderArchiveScreenHandler handler, PlayerInventory playerInventory, Text title) {
        super(handler, playerInventory, title, Identifier.of(Journeycreative.MOD_ID, "textures/gui/ender_archive.png"));
        this.titleX = 52;
    }

    @Override
    protected void drawBackground(DrawContext context, float deltaTicks, int mouseX, int mouseY) {
        context.drawTexture(RenderPipelines.GUI_TEXTURED, this.texture, this.x, this.y, 0.0F, 0.0F, this.backgroundWidth, this.backgroundHeight, 176, 166);
        this.drawInvalidRecipeArrow(context, this.x, this.y);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
    }

    @Override
    protected void drawInvalidRecipeArrow(DrawContext context, int x, int y) {
        if (this.getScreenHandler().hasInvalidRecipe()) {
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, ERROR_TEXTURE, this.x + 74,this.y + 33, 28, 21);
        }
    }


}
