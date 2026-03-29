package mod.journeycreative.screen;

import mod.journeycreative.Journeycreative;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.CyclingSlotBackground;
import net.minecraft.client.gui.screens.inventory.ItemCombinerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;
import java.util.Optional;

public class EnderArchiveScreen extends ItemCombinerScreen<EnderArchiveScreenHandler> {
    private static final Identifier ERROR_TEXTURE = Identifier.withDefaultNamespace("container/smithing/error");
    private final Identifier texture = Identifier.fromNamespaceAndPath(Journeycreative.MOD_ID, "textures/gui/ender_archive.png");
    private static final Identifier EMPTY_SLOT_RESEARCH_VESSEL_TEXTURE = Identifier.fromNamespaceAndPath(Journeycreative.MOD_ID, "container/slot/research_vessel");
    private static final Identifier EMPTY_SLOT_RESEARCH_CERTIFICATE_TEXTURE = Identifier.fromNamespaceAndPath(Journeycreative.MOD_ID, "container/slot/research_certificate");
    private final CyclingSlotBackground researchVesselSlotIcon = new CyclingSlotBackground(0);
    private final CyclingSlotBackground researchCertificateSlotIcon = new CyclingSlotBackground(1);
    private static final Component RESEARCH_BLOCKED_TOOLTIP = Component.translatable("container.ender_archive.research_blocked_tooltip");
    private static final Component CANNOT_RESEARCH_TOOLTIP = Component.translatable("container.ender_archive.cannot_research_tooltip");
    private static final Component NOT_ENOUGH_ITEMS_TOOLTIP = Component.translatable("container.ender_archive.not_enough_items_tooltip");

    public EnderArchiveScreen(EnderArchiveScreenHandler handler, Inventory playerInventory, Component title) {
        super(handler, playerInventory, title, Identifier.fromNamespaceAndPath(Journeycreative.MOD_ID, "textures/gui/ender_archive.png"));
        this.titleLabelX = 52;
    }

    public void containerTick() {
        super.containerTick();
        this.researchVesselSlotIcon.tick(List.of(EMPTY_SLOT_RESEARCH_VESSEL_TEXTURE));
        this.researchCertificateSlotIcon.tick(List.of(EMPTY_SLOT_RESEARCH_CERTIFICATE_TEXTURE));
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
        context.blit(RenderPipelines.GUI_TEXTURED, this.texture, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
        this.researchCertificateSlotIcon.extractRenderState(this.menu, context, deltaTicks, this.leftPos, this.topPos);
        this.researchVesselSlotIcon.extractRenderState(this.menu, context, deltaTicks, this.leftPos, this.topPos);
        this.extractErrorIcon(context, this.leftPos, this.topPos);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
        super.extractRenderState(context, mouseX, mouseY, deltaTicks);
        this.renderSlotTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void extractErrorIcon(GuiGraphicsExtractor context, int x, int y) {
        if (this.getMenu().hasInvalidRecipe()) {
            context.blitSprite(RenderPipelines.GUI_TEXTURED, ERROR_TEXTURE, this.leftPos + 74,this.topPos + 31, 28, 21);
        }
    }

    private void renderSlotTooltip(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        Optional<Component> optional = Optional.empty();
        if (this.getMenu().hasInvalidRecipe() & this.isHovering(74, 31, 28, 21, (double) mouseX, (double) mouseY)) {
            EnderArchiveScreenHandler.researchInvalidReason reason = this.getMenu().getReason();
            if (reason == EnderArchiveScreenHandler.researchInvalidReason.PROHIBITED) {
                optional = Optional.of(CANNOT_RESEARCH_TOOLTIP);
            } else if (reason == EnderArchiveScreenHandler.researchInvalidReason.BLOCKED) {
                optional = Optional.of(RESEARCH_BLOCKED_TOOLTIP);
            } else if (reason == EnderArchiveScreenHandler.researchInvalidReason.INSUFFICIENT) {
                optional = Optional.of(NOT_ENOUGH_ITEMS_TOOLTIP);
            }
        }

        optional.ifPresent((text) -> {
            var lines = this.font.split(text, 115);

            List<ClientTooltipComponent> components = lines.stream()
                    .map(ClientTooltipComponent::create)
                    .toList();

            context.tooltip(this.font,
                    components, mouseX, mouseY,
                    DefaultTooltipPositioner.INSTANCE,
                    null);
        });
    }


}
