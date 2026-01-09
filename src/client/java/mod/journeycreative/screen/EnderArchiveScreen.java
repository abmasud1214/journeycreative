package mod.journeycreative.screen;

import mod.journeycreative.Journeycreative;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.CyclingSlotIcon;
import net.minecraft.client.gui.screen.ingame.ForgingScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;

public class EnderArchiveScreen extends ForgingScreen<EnderArchiveScreenHandler> {
    private static final Identifier ERROR_TEXTURE = Identifier.ofVanilla("container/smithing/error");
    private final Identifier texture = Identifier.of(Journeycreative.MOD_ID, "textures/gui/ender_archive.png");
    private static final Identifier EMPTY_SLOT_RESEARCH_VESSEL_TEXTURE = Identifier.of(Journeycreative.MOD_ID, "item/empty_slot_research_vessel");
//    private static final Identifier EMPTY_SLOT_RESEARCH_VESSEL_TEXTURE = Identifier.ofVanilla("container/slot/empty_input");
    private static final Identifier EMPTY_SLOT_RESEARCH_CERTIFICATE_TEXTURE = Identifier.of(Journeycreative.MOD_ID, "item/empty_slot_research_certificate");
//    private static final Identifier EMPTY_SLOT_RESEARCH_CERTIFICATE_TEXTURE = Identifier.ofVanilla("container/slot/empty_input");
    private final CyclingSlotIcon researchVesselSlotIcon = new CyclingSlotIcon(0);
    private final CyclingSlotIcon researchCertificateSlotIcon = new CyclingSlotIcon(1);
    private static final Text RESEARCH_BLOCKED_TOOLTIP = Text.translatable("container.ender_archive.research_blocked_tooltip");
    private static final Text CANNOT_RESEARCH_TOOLTIP = Text.translatable("container.ender_archive.cannot_research_tooltip");
    private static final Text NOT_ENOUGH_ITEMS_TOOLTIP = Text.translatable("container.ender_archive.not_enough_items_tooltip");

    public EnderArchiveScreen(EnderArchiveScreenHandler handler, PlayerInventory playerInventory, Text title) {
        super(handler, playerInventory, title, Identifier.of(Journeycreative.MOD_ID, "textures/gui/ender_archive.png"));
        this.titleX = 52;
    }

    public void handledScreenTick() {
        super.handledScreenTick();
        this.researchVesselSlotIcon.updateTexture(List.of(EMPTY_SLOT_RESEARCH_VESSEL_TEXTURE));
        this.researchCertificateSlotIcon.updateTexture(List.of(EMPTY_SLOT_RESEARCH_CERTIFICATE_TEXTURE));
    }

    @Override
    protected void drawBackground(DrawContext context, float deltaTicks, int mouseX, int mouseY) {
        context.drawTexture(RenderLayer::getGuiTextured, this.texture, this.x, this.y, 0.0F, 0.0F, this.backgroundWidth, this.backgroundHeight, 256, 256);
        this.researchCertificateSlotIcon.render(this.handler, context, deltaTicks, this.x, this.y);
        this.researchVesselSlotIcon.render(this.handler, context, deltaTicks, this.x, this.y);
        this.drawInvalidRecipeArrow(context, this.x, this.y);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
        this.renderSlotTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawInvalidRecipeArrow(DrawContext context, int x, int y) {
        if (this.getScreenHandler().hasInvalidRecipe()) {
            context.drawGuiTexture(RenderLayer::getGuiTextured, ERROR_TEXTURE, this.x + 74,this.y + 31, 28, 21);
        }
    }

    private void renderSlotTooltip(DrawContext context, int mouseX, int mouseY) {
        Optional<Text> optional = Optional.empty();
        if (this.getScreenHandler().hasInvalidRecipe() & this.isPointWithinBounds(74, 31, 28, 21, (double) mouseX, (double) mouseY)) {
            EnderArchiveScreenHandler.researchInvalidReason reason = this.getScreenHandler().getReason();
            if (reason == EnderArchiveScreenHandler.researchInvalidReason.PROHIBITED) {
                optional = Optional.of(CANNOT_RESEARCH_TOOLTIP);
            } else if (reason == EnderArchiveScreenHandler.researchInvalidReason.BLOCKED) {
                optional = Optional.of(RESEARCH_BLOCKED_TOOLTIP);
            } else if (reason == EnderArchiveScreenHandler.researchInvalidReason.INSUFFICIENT) {
                optional = Optional.of(NOT_ENOUGH_ITEMS_TOOLTIP);
            }
        }

        optional.ifPresent((text) -> {
            context.drawOrderedTooltip(this.textRenderer, this.textRenderer.wrapLines(text, 115), mouseX, mouseY);
        });
    }


}
