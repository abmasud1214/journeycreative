package mod.journeycreative.screen;

import mod.journeycreative.Journeycreative;
import mod.journeycreative.blocks.ResearchVesselInventory;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class ResearchVesselScreen extends HandledScreen<ResearchVesselScreenHandler> {

    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/gui/container/shulker_box.png");
    private static final Identifier INVALID_RESEARCH_TEXTURE = Identifier.of(Journeycreative.MOD_ID, "textures/gui/sprites/invalid_research.png");
    private static final Text CANNOT_RESEARCH_TOOLTIP = Text.translatable("container.ender_archive.cannot_research_tooltip");
    private static final Text RESEARCH_BLOCKED_TOOLTIP = Text.translatable("container.ender_archive.research_blocked_tooltip");

    public ResearchVesselScreen(ResearchVesselScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void drawBackground(DrawContext context, float deltaTicks, int mouseX, int mouseY) {
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, i, j, 0.0F, 0.0F, this.backgroundWidth, this.backgroundHeight, 256, 256);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
        this.renderItemProgress(context);
        this.renderInvalid(context);
        this.renderSlotTooltip(context, mouseX, mouseY);
    }

    private void renderItemProgress(DrawContext context) {
        Optional<Text> optional = Optional.empty();
        ResearchVesselScreenHandler handler = this.getScreenHandler();
        EnderArchiveScreenHandler.researchInvalidReason reason = handler.getReason();
        boolean bl = (reason == EnderArchiveScreenHandler.researchInvalidReason.BLOCKED || reason == EnderArchiveScreenHandler.researchInvalidReason.PROHIBITED);
        if (!(handler.getInventoryCapacity() == 0) && !bl) {
            int quantity = handler.getInventoryQuantity();
            int capacity = handler.getInventoryCapacity();
            optional = Optional.of(Text.literal(String.format("%d/%d", quantity, capacity)));
        }
        optional.ifPresent((text) -> {
            int width = this.textRenderer.getWidth(text);
            int x = this.x + 168 - width;

            context.drawText(this.textRenderer, text, x, this.y + 6, Colors.DARK_GRAY, false);
        });
    }

    private void renderInvalid(DrawContext context) {
        ResearchVesselScreenHandler handler = this.getScreenHandler();
        EnderArchiveScreenHandler.researchInvalidReason reason = handler.getReason();
        boolean bl = (reason == EnderArchiveScreenHandler.researchInvalidReason.BLOCKED || reason == EnderArchiveScreenHandler.researchInvalidReason.PROHIBITED);
        if (!(handler.getInventoryCapacity() == 0) && bl) {
            context.drawTexture(RenderPipelines.GUI_TEXTURED, INVALID_RESEARCH_TEXTURE,
                    this.x + 157, this.y + 5, 0, 0,
                    11, 11, 11, 11);
        }
    }

    private void renderSlotTooltip(DrawContext context, int mouseX, int mouseY) {
        Optional<Text> optional = Optional.empty();
        ResearchVesselScreenHandler handler = this.getScreenHandler();
        EnderArchiveScreenHandler.researchInvalidReason reason = handler.getReason();
        boolean bl = (reason == EnderArchiveScreenHandler.researchInvalidReason.BLOCKED || reason == EnderArchiveScreenHandler.researchInvalidReason.PROHIBITED);
        if (!(handler.getInventoryCapacity() == 0) && bl && this.isPointWithinBounds(157, 5, 11, 11, (double) mouseX, (double) mouseY)) {
            if (reason == EnderArchiveScreenHandler.researchInvalidReason.PROHIBITED) {
                optional = Optional.of(CANNOT_RESEARCH_TOOLTIP);
            } else {
                optional = Optional.of(RESEARCH_BLOCKED_TOOLTIP);
            }
        }

        optional.ifPresent((text) -> {
            context.drawOrderedTooltip(this.textRenderer, this.textRenderer.wrapLines(text, 115), mouseX, mouseY);
        });
    }

}
