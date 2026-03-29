package mod.journeycreative.screen;

import com.google.common.collect.ImmutableSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;

import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//THIS CLASS RIPPED STRAIGHT FROM FabricCreativeGuiComponents.class to enable itemGroup support in JourneyCreative inventory.
@Environment(EnvType.CLIENT)
public class JourneyCreativeGuiComponents {
    private static final Identifier BUTTON_TEX = Identifier.fromNamespaceAndPath("fabric", "textures/gui/creative_buttons.png");
    private static final double TABS_PER_PAGE = 10.0;
    public static final Set<CreativeModeTab> COMMON_GROUPS;

    public JourneyCreativeGuiComponents() {

    }

    public static int getPageCount() {
        return (int) Math.ceil((double) ((long) CreativeModeTabs.tabs().size() - COMMON_GROUPS.stream().filter(CreativeModeTab::shouldDisplay).count()) / TABS_PER_PAGE);
    }

    static {
        Stream<ResourceKey<CreativeModeTab>> groupStream = ImmutableSet.of(CreativeModeTabs.SEARCH, CreativeModeTabs.INVENTORY, CreativeModeTabs.HOTBAR, CreativeModeTabs.OP_BLOCKS).stream();
        Registry<CreativeModeTab> groupRegistries = BuiltInRegistries.CREATIVE_MODE_TAB;
        Objects.requireNonNull(groupRegistries);
        COMMON_GROUPS = (Set<CreativeModeTab>) groupStream.map(groupRegistries::getValueOrThrow).collect(Collectors.toSet());
    }

    @Environment(EnvType.CLIENT)
    public static enum Type {
        NEXT(Component.literal(">"), JourneyInventoryScreen::switchToNextPage, (screen) -> {
            return screen.getCurrentPage() + 1 < screen.getPageCount();
        }),
        PREVIOUS(Component.literal("<"), JourneyInventoryScreen::switchToPreviousPage, (screen) -> {
            return screen.getCurrentPage() != 0;
        });

        final Component text;
        final Consumer<JourneyInventoryScreen> clickConsumer;
        final Predicate<JourneyInventoryScreen> isEnabled;

        private Type(Component text, Consumer<JourneyInventoryScreen> clickConsumer, Predicate<JourneyInventoryScreen> isEnabled) {
            this.text = text;
            this.clickConsumer = clickConsumer;
            this.isEnabled = isEnabled;
        }
    }

    @Environment(EnvType.CLIENT)
    public static class ItemGroupButtonWidget extends Button {
        final JourneyInventoryScreen screen;
        final Type type;

        public ItemGroupButtonWidget(int x, int y, Type type, JourneyInventoryScreen screen) {
            super(x, y, 10, 12, type.text, (bw) -> {
                type.clickConsumer.accept(screen);
            }, Button.DEFAULT_NARRATION);
            this.type = type;
            this.screen = screen;
        }

        protected void extractContents(GuiGraphicsExtractor drawContext, int mouseX, int mouseY, float delta) {
            this.active = this.type.isEnabled.test(this.screen);
            this.visible = this.screen.hasAdditionalPages();
            if (this.visible) {
                int u = this.active && this.isHovered() ? 20 : 0;
                int v = this.active ? 0 : 12;
                drawContext.blit(RenderPipelines.GUI_TEXTURED, JourneyCreativeGuiComponents.BUTTON_TEX, this.getX(), this.getY(), (float) (u + (this.type == JourneyCreativeGuiComponents.Type.NEXT ? 10 : 0)), (float) v, 10, 12, 256, 256);
                if (this.isHovered()) {
                    drawContext.setTooltipForNextFrame(Minecraft.getInstance().font, Component.translatable("fabric.gui.creativeTabPage", screen.getCurrentPage() + 1, getPageCount()), mouseX, mouseY);
                }
            }
        }
    }
}
