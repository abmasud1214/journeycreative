package mod.journeycreative.screen;

import com.google.common.collect.ImmutableSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//THIS CLASS RIPPED STRAIGHT FROM FabricCreativeGuiComponents.class to enable itemGroup support in JourneyCreative inventory.
@Environment(EnvType.CLIENT)
public class JourneyCreativeGuiComponents {
    private static final Identifier BUTTON_TEX = Identifier.of("fabric", "textures/gui/creative_buttons.png");
    private static final double TABS_PER_PAGE = 10.0;
    public static final Set<ItemGroup> COMMON_GROUPS;

    public JourneyCreativeGuiComponents() {

    }

    public static int getPageCount() {
        return (int) Math.ceil((double) ((long) ItemGroups.getGroupsToDisplay().size() - COMMON_GROUPS.stream().filter(ItemGroup::shouldDisplay).count()) / 10.0);
    }

    static {
        Stream<RegistryKey<ItemGroup>> groupStream = ImmutableSet.of(ItemGroups.SEARCH, ItemGroups.INVENTORY, ItemGroups.HOTBAR, ItemGroups.OPERATOR).stream();
        Registry<ItemGroup> groupRegistries = Registries.ITEM_GROUP;
        Objects.requireNonNull(groupRegistries);
        COMMON_GROUPS = (Set<ItemGroup>) groupStream.map(groupRegistries::getValueOrThrow).collect(Collectors.toSet());
    }

    @Environment(EnvType.CLIENT)
    public static enum Type {
        NEXT(Text.literal(">"), JourneyInventoryScreen::switchToNextPage, (screen) -> {
            return screen.getCurrentPage() + 1 < screen.getPageCount();
        }),
        PREVIOUS(Text.literal("<"), JourneyInventoryScreen::switchToPreviousPage, (screen) -> {
            return screen.getCurrentPage() != 0;
        });

        final Text text;
        final Function<JourneyInventoryScreen, Boolean> clickConsumer;
        final Predicate<JourneyInventoryScreen> isEnabled;

        private Type(Text text, Function<JourneyInventoryScreen, Boolean> clickConsumer, Predicate<JourneyInventoryScreen> isEnabled) {
            this.text = text;
            this.clickConsumer = clickConsumer;
            this.isEnabled = isEnabled;
        }
    }

    @Environment(EnvType.CLIENT)
    public static class ItemGroupButtonWidget extends ButtonWidget {
        final JourneyInventoryScreen screen;
        final Type type;

        public ItemGroupButtonWidget(int x, int y, Type type, JourneyInventoryScreen screen) {
            super(x, y, 10, 12, type.text, (bw) -> {
                type.clickConsumer.apply(screen);
            }, ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
            this.type = type;
            this.screen = screen;
        }

        protected void drawIcon(DrawContext drawContext, int mouseX, int mouseY, float delta) {
            this.active = this.type.isEnabled.test(this.screen);
            this.visible = this.screen.hasAdditionalPages();
            if (this.visible) {
                int u = this.active && this.isHovered() ? 20 : 0;
                int v = this.active ? 0 : 12;
                drawContext.drawTexture(RenderPipelines.GUI_TEXTURED, JourneyCreativeGuiComponents.BUTTON_TEX, this.getX(), this.getY(), (float) (u + (this.type == JourneyCreativeGuiComponents.Type.NEXT ? 10 : 0)), (float) v, 10, 12, 256, 256);
                if (this.isHovered()) {
                    drawContext.drawTooltip(MinecraftClient.getInstance().textRenderer, net.minecraft.text.Text.translatable("fabric.gui.creativeTabPage", new Object[]{this.screen.getCurrentPage() + 1, JourneyCreativeGuiComponents.getPageCount()}), mouseX, mouseY);
                }
            }
        }
    }
}
