package mod.journeycreative.screen;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import mod.journeycreative.Journeycreative;
import mod.journeycreative.networking.PlayerClientUnlocksData;
import mod.journeycreative.networking.JourneyClientNetworking;
import mod.journeycreative.networking.TrashcanServerStorage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.creativetab.v1.FabricCreativeModeInventoryScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.HotbarManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.SessionSearchTrees;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.inventory.Hotbar;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.searchtree.SearchTree;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

/*
 * This file is part of Journey Creative.
 * * Potions of this code are derived from Minecraft source code.
 * Intellectual Property of Mojang AB. All rights reserved.
 *
 * All modifications, additions, and custom logic are licensed under the MIT License.
 * Full license text can be found in the LICENSE file at the root of this project.
 */
public class JourneyInventoryScreen extends AbstractContainerScreen<JourneyInventoryScreen.JourneyScreenHandler> implements FabricCreativeModeInventoryScreen {
    public static final WidgetSprites JOURNEY_BUTTON_TEXTURES = new WidgetSprites(
            Identifier.fromNamespaceAndPath(Journeycreative.MOD_ID, "journey_button"),
            Identifier.fromNamespaceAndPath(Journeycreative.MOD_ID, "journey_button_highlighted"));

    private static final Identifier SCROLLER_TEXTURE = Identifier.withDefaultNamespace("container/creative_inventory/scroller");
    private static final Identifier SCROLLER_DISABLED_TEXTURE = Identifier.withDefaultNamespace("container/creative_inventory/scroller_disabled");
    private static final Identifier[] TAB_TOP_UNSELECTED_TEXTURES = new Identifier[]{Identifier.withDefaultNamespace("container/creative_inventory/tab_top_unselected_1"), Identifier.withDefaultNamespace("container/creative_inventory/tab_top_unselected_2"), Identifier.withDefaultNamespace("container/creative_inventory/tab_top_unselected_3"), Identifier.withDefaultNamespace("container/creative_inventory/tab_top_unselected_4"), Identifier.withDefaultNamespace("container/creative_inventory/tab_top_unselected_5"), Identifier.withDefaultNamespace("container/creative_inventory/tab_top_unselected_6"), Identifier.withDefaultNamespace("container/creative_inventory/tab_top_unselected_7")};
    private static final Identifier[] TAB_TOP_SELECTED_TEXTURES = new Identifier[]{Identifier.withDefaultNamespace("container/creative_inventory/tab_top_selected_1"), Identifier.withDefaultNamespace("container/creative_inventory/tab_top_selected_2"), Identifier.withDefaultNamespace("container/creative_inventory/tab_top_selected_3"), Identifier.withDefaultNamespace("container/creative_inventory/tab_top_selected_4"), Identifier.withDefaultNamespace("container/creative_inventory/tab_top_selected_5"), Identifier.withDefaultNamespace("container/creative_inventory/tab_top_selected_6"), Identifier.withDefaultNamespace("container/creative_inventory/tab_top_selected_7")};
    private static final Identifier[] TAB_BOTTOM_UNSELECTED_TEXTURES = new Identifier[]{Identifier.withDefaultNamespace("container/creative_inventory/tab_bottom_unselected_1"), Identifier.withDefaultNamespace("container/creative_inventory/tab_bottom_unselected_2"), Identifier.withDefaultNamespace("container/creative_inventory/tab_bottom_unselected_3"), Identifier.withDefaultNamespace("container/creative_inventory/tab_bottom_unselected_4"), Identifier.withDefaultNamespace("container/creative_inventory/tab_bottom_unselected_5"), Identifier.withDefaultNamespace("container/creative_inventory/tab_bottom_unselected_6"), Identifier.withDefaultNamespace("container/creative_inventory/tab_bottom_unselected_7")};
    private static final Identifier[] TAB_BOTTOM_SELECTED_TEXTURES = new Identifier[]{Identifier.withDefaultNamespace("container/creative_inventory/tab_bottom_selected_1"), Identifier.withDefaultNamespace("container/creative_inventory/tab_bottom_selected_2"), Identifier.withDefaultNamespace("container/creative_inventory/tab_bottom_selected_3"), Identifier.withDefaultNamespace("container/creative_inventory/tab_bottom_selected_4"), Identifier.withDefaultNamespace("container/creative_inventory/tab_bottom_selected_5"), Identifier.withDefaultNamespace("container/creative_inventory/tab_bottom_selected_6"), Identifier.withDefaultNamespace("container/creative_inventory/tab_bottom_selected_7")};
    private static final Identifier JOURNEY_INVENTORY_TEXTURE = Identifier.fromNamespaceAndPath(Journeycreative.MOD_ID, "textures/gui/gui_journey_inventory.png");
    private static final int ROWS_COUNT = 5;
    private static final int COLUMNS_COUNT = 9;
    private static final int TAB_WIDTH = 26;
    private static final int TAB_HEIGHT = 32;
    private static final int SCROLLBAR_WIDTH = 12;
    private static final int SCROLLBAR_HEIGHT = 15;
    static SimpleContainer INVENTORY;
    private static final Component DELETE_ITEM_SLOT_TEXT = Component.translatable("inventory.binSlot");
    private static CreativeModeTab selectedTab = CreativeModeTabs.getDefaultTab();
    private static int currentPage = 0;
    private float scrollPosition;
    private boolean scrolling;
    private EditBox searchBox;
    @Nullable
    private List<Slot> slots;
    @Nullable
    private Slot deleteItemSlot;
    private Slot pickupSlot;
    private JourneyInventoryListener listener;
    private boolean ignoreTypedCharacter;
    private boolean lastClickOutsideBounds;
    private final Set<TagKey<Item>> searchResultTags = new HashSet();
    private final boolean operatorTabEnabled;
    private final EffectsInInventory statusEffectsDisplay;
    private final Map<CreativeModeTab, Integer> itemGroupPage = new HashMap();

    public JourneyInventoryScreen(LocalPlayer player, FeatureFlagSet enabledFeatures, boolean operatorTabEnabled) {
        super(new JourneyScreenHandler(player), player.getInventory(), CommonComponents.EMPTY, 195, 136);
        player.containerMenu = this.menu;
        this.operatorTabEnabled = operatorTabEnabled;
        INVENTORY = this.menu.INVENTORY;
        this.statusEffectsDisplay = new EffectsInInventory(this);
    }

    private boolean shouldShowOperatorTab(Player player) {
        return false;
    }

    private void updateDisplayParameters(FeatureFlagSet enabledFeatures, boolean showOperatorTab, HolderLookup.Provider registries) {
        ClientPacketListener clientPlayNetworkHandler = this.minecraft.getConnection();

        if (this.populateDisplay(clientPlayNetworkHandler != null ? clientPlayNetworkHandler.searchTrees() : null, enabledFeatures, showOperatorTab, registries)) {
            Iterator var5 = CreativeModeTabs.allTabs().iterator();

            while(true) {
                while(true) {
                    CreativeModeTab itemGroup;
                    Collection collection;
                    do {
                        if (!var5.hasNext()) {
                            return;
                        }

                        itemGroup = (CreativeModeTab)var5.next();
                        collection = itemGroup.getDisplayItems();
                    } while(itemGroup != selectedTab);

                    if (itemGroup.getType() == CreativeModeTab.Type.CATEGORY && collection.isEmpty()) {
                        this.setSelectedTab(CreativeModeTabs.getDefaultTab());
                    } else {
                        collection = filterUnlockedItems(collection);
                        this.refreshSelectedTab(collection);
                    }
                }
            }
        }

    }

    private boolean populateDisplay(@Nullable SessionSearchTrees searchManager, FeatureFlagSet enabledFeatures, boolean showOperatorTab, HolderLookup.Provider registries) {
        if (!CreativeModeTabs.tryRebuildTabContents(enabledFeatures, showOperatorTab, registries)) {
            return false;
        } else {
            if (searchManager != null) {
                List<ItemStack> list = List.copyOf(CreativeModeTabs.searchTab().getDisplayItems());
                searchManager.updateCreativeTooltips(registries, list);
                searchManager.updateCreativeTags(list);
            }

            return true;
        }
    }

    private void refreshSelectedTab(Collection<ItemStack> displayStacks) {
        int i = ((JourneyScreenHandler) this.menu).getRow(this.scrollPosition);
        ((JourneyScreenHandler) this.menu).itemList.clear();
        if (selectedTab.getType() == CreativeModeTab.Type.SEARCH) {
            this.search();
        } else {
            ((JourneyScreenHandler) this.menu).itemList.addAll(displayStacks);
        }

        this.scrollPosition = ((JourneyScreenHandler) this.menu).getScrollPosition(i);
        ((JourneyScreenHandler) this.menu).scrollItems(this.scrollPosition);
    }

    public void containerTick() {
        super.containerTick();
        if (this.minecraft != null) {
            LocalPlayer clientPlayerEntity = this.minecraft.player;
            if (clientPlayerEntity != null) {
                this.updateDisplayParameters(clientPlayerEntity.connection.enabledFeatures(), this.shouldShowOperatorTab(clientPlayerEntity), clientPlayerEntity.level().registryAccess());
            }
        }
    }

    protected void slotClicked(@Nullable Slot slot, int slotId, int button, ContainerInput actionType) {
        if (this.isCreativeInventorySlot(slot)) {
            this.searchBox.moveCursorToEnd(false);
            this.searchBox.setHighlightPos(0);
        }


        boolean bl = actionType == ContainerInput.QUICK_MOVE;
        actionType = slotId == -999 && actionType == ContainerInput.PICKUP ? ContainerInput.THROW : actionType; // If pickup action, then throw
        if (actionType != ContainerInput.THROW || this.minecraft.player.canDropItems()) {
            this.onMouseClickAction(slot, actionType);
            ItemStack itemStack;
            if (slot == null && selectedTab.getType() != CreativeModeTab.Type.INVENTORY && actionType != ContainerInput.QUICK_CRAFT) { // click outside inventory
                if (!((JourneyScreenHandler) this.menu).getCarried().isEmpty() && this.lastClickOutsideBounds) {
                    if (!this.minecraft.player.canDropItems()) {
                        return;
                    }
                    if (button == 0) {
                        this.minecraft.player.drop(((JourneyScreenHandler) this.menu).getCarried(), true);
                        JourneyClientNetworking.dropJourneyStack(((JourneyScreenHandler) this.menu).getCarried(), this.minecraft.player);
                        ((JourneyScreenHandler) this.menu).setCarried(ItemStack.EMPTY);
                    }

                    if (button == 1) {
                        itemStack = ((JourneyScreenHandler) this.menu).getCarried().split(1);
                        this.minecraft.player.drop(itemStack, true);
                        JourneyClientNetworking.dropJourneyStack(itemStack, this.minecraft.player);
                    }
                }
            } else {
                if (slot != null && !slot.mayPickup(this.minecraft.player)) {
                    return;
                }

                if (slot == this.deleteItemSlot && bl && this.deleteItemSlot.hasItem()) { // shift click delete item slot
                    ItemStack tcStack = this.deleteItemSlot.getItem();
                    boolean ret = this.menu.insertItemTrashcan(tcStack, 9, 46, false);
                    if(ret) {
                        for (int k = 9; k < 45; ++k) {
                            Slot s = ((JourneyScreenHandler) this.menu).getSlot(k);
                            JourneyClientNetworking.clickJourneyStack(s.getItem(), ((JourneySlot) s).slot.index);
                        }
                        JourneyClientNetworking.sendTrashcanUpdate(tcStack);
                    }
                } else {
                    ItemStack itemStack2;
                    if (selectedTab.getType() == CreativeModeTab.Type.INVENTORY) { // player inventory visible
                        if (slot == this.deleteItemSlot) { // click delete item slot
                            ItemStack cursorStack = this.menu.getCarried().copy();
                            if (cursorStack.isEmpty() && this.deleteItemSlot.hasItem()) {
                                itemStack2 = this.deleteItemSlot.getItem();
                                ((JourneyScreenHandler) this.menu).setCarried(itemStack2);
                                JourneyClientNetworking.sendTrashcanUpdate(ItemStack.EMPTY);
                            } else {
                                JourneyClientNetworking.sendTrashcanUpdate(cursorStack);
                                ((JourneyScreenHandler) this.menu).setCarried(ItemStack.EMPTY);
                            }
                        } else if (bl && slot != null && slot.hasItem()) { // shift click slot.
                            itemStack2 = slot.getItem();
                            JourneyClientNetworking.sendTrashcanUpdate(itemStack2);
                            slot.setByPlayer(ItemStack.EMPTY);
                            JourneyClientNetworking.clickJourneyStack(ItemStack.EMPTY, ((JourneySlot) slot).slot.index);
                        } else if (actionType == ContainerInput.THROW && slot != null && slot.hasItem()) {
                            itemStack = slot.remove(button == 0 ? 1 : slot.getItem().getMaxStackSize());
                            itemStack2 = slot.getItem();
                            this.minecraft.player.drop(itemStack, true);
                            JourneyClientNetworking.dropJourneyStack(itemStack, this.minecraft.player);
                            JourneyClientNetworking.clickJourneyStack(itemStack2, ((JourneySlot) slot).slot.index);
                        } else if (actionType == ContainerInput.THROW && slotId == -999 && !((JourneyScreenHandler) this.menu).getCarried().isEmpty()) {
                            this.minecraft.player.drop(((JourneyScreenHandler) this.menu).getCarried(), true);
                            JourneyClientNetworking.dropJourneyStack(((JourneyScreenHandler) this.menu).getCarried(), this.minecraft.player);
                            ((JourneyScreenHandler) this.menu).setCarried(ItemStack.EMPTY);
                        } else {
                            this.minecraft.player.inventoryMenu.clicked(slot == null ? slotId : ((JourneySlot) slot).slot.index, button, actionType, this.minecraft.player);
                        }
                        for (int k = 9; k < 45; ++k) {
                            Slot s = ((JourneyScreenHandler) this.menu).getSlot(k);
                            JourneyClientNetworking.clickJourneyStack(s.getItem(), ((JourneySlot) s).slot.index);
                        }
                    } else {
                        ItemStack itemStack3;
                        if (actionType != ContainerInput.QUICK_CRAFT && slot.container == INVENTORY) {
                            itemStack = ((JourneyScreenHandler) this.menu).getCarried();
                            itemStack2 = slot.getItem();
                            if (actionType == ContainerInput.SWAP) {
                                if (!itemStack2.isEmpty() && this.minecraft.player.getInventory().getItem(button).isEmpty()) {
                                    this.minecraft.player.getInventory().setItem(button, itemStack2.copyWithCount(itemStack2.getMaxStackSize()));
                                    this.minecraft.player.inventoryMenu.broadcastChanges();
                                }

                                return;
                            }

                            if (actionType == ContainerInput.CLONE) {
                                if (((JourneyScreenHandler) this.menu).getCarried().isEmpty() && slot.hasItem()) {
                                    itemStack3 = slot.getItem();
                                    ((JourneyScreenHandler) this.menu).setCarried(itemStack3.copyWithCount(itemStack3.getMaxStackSize()));
                                }

                                return;
                            }

                            if (actionType == ContainerInput.THROW) {
                                if (!itemStack2.isEmpty()) {
                                    itemStack3 = itemStack2.copyWithCount(button == 0 ? 1 : itemStack2.getMaxStackSize());
                                    this.minecraft.player.drop(itemStack3, true);
                                    JourneyClientNetworking.dropJourneyStack(itemStack3, this.minecraft.player);
                                }

                                return;
                            }

                            if (!itemStack.isEmpty() && !itemStack2.isEmpty() && ItemStack.isSameItemSameComponents(itemStack, itemStack2)) {
                                if (button == 0) {
                                    if (bl) {
                                        itemStack.setCount(itemStack.getMaxStackSize());
                                    } else if (itemStack.getCount() < itemStack.getMaxStackSize()) {
                                        itemStack.grow(1);
                                    }
                                } else {
                                    itemStack.shrink(1);
                                }
                            } else if (!itemStack2.isEmpty() && itemStack.isEmpty()) {
                                int j = bl ? itemStack2.getMaxStackSize() : itemStack2.getCount();
                                ((JourneyScreenHandler) this.menu).setCarried(itemStack2.copyWithCount(j));
                            } else if (button == 0) {
                                ((JourneyScreenHandler) this.menu).setCarried(ItemStack.EMPTY);
                            } else if (!((JourneyScreenHandler) this.menu).getCarried().isEmpty()) {
                                ((JourneyScreenHandler) this.menu).getCarried().shrink(1);
                            }
                        } else if (this.menu != null) {
                            itemStack = slot == null ? ItemStack.EMPTY : ((JourneyScreenHandler) this.menu).getSlot(slot.index).getItem();
                            ((JourneyScreenHandler) this.menu).clicked(slot == null ? slotId : slot.index, button, actionType, this.minecraft.player);
                            int k;
                            if (AbstractContainerMenu.getQuickcraftHeader(button) == 2) {
                                for (k = 0; k < 9; ++k) {
                                    JourneyClientNetworking.clickJourneyStack(((JourneyScreenHandler) this.menu).getSlot(45 + k).getItem(), 36 + k);
                                }
                            } else if (slot != null && Inventory.isHotbarSlot(slot.getContainerSlot()) && selectedTab.getType() != CreativeModeTab.Type.INVENTORY) {
                                if (actionType == ContainerInput.THROW && !itemStack.isEmpty() && !((JourneyScreenHandler) this.menu).getCarried().isEmpty()) {
                                    k = button == 0 ? 1 : itemStack.getCount();
                                    itemStack3 = itemStack.copyWithCount(k);
                                    itemStack.shrink(k);
                                    this.minecraft.player.drop(itemStack3, true);
                                    JourneyClientNetworking.dropJourneyStack(itemStack3, this.minecraft.player);
                                }

                                this.minecraft.player.inventoryMenu.broadcastChanges();
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isCreativeInventorySlot(@Nullable Slot slot) {
        return slot != null && slot.container == INVENTORY;
    }

    @Override
    protected  void init() {
        super.init();
        paginateTabs();
        Font var10003 = this.font;
        int var10004 = this.leftPos + 82;
        int var10005 = this.topPos + 6;
        Objects.requireNonNull(this.font);
        this.searchBox = new EditBox(var10003, var10004, var10005, 80, 9, Component.translatable("itemGroup.search"));
        this.searchBox.setMaxLength(50);
        this.searchBox.setBordered(false);
        this.searchBox.setVisible(false);
        this.searchBox.setTextColor(-1);
        currentPage = this.getPage(selectedTab);
        int xpos = this.leftPos + 171;
        int ypos = this.topPos + 4;
        this.addWidget(this.searchBox);
        CreativeModeTab itemGroup = selectedTab;
        selectedTab = CreativeModeTabs.getDefaultTab();
        this.selectTab(itemGroup);
        JourneyInventoryScreen self = (JourneyInventoryScreen) this;
        this.addRenderableWidget(new JourneyCreativeGuiComponents.ItemGroupButtonWidget(xpos + 10, ypos, JourneyCreativeGuiComponents.Type.NEXT, self));
        this.addRenderableWidget(new JourneyCreativeGuiComponents.ItemGroupButtonWidget(xpos, ypos, JourneyCreativeGuiComponents.Type.PREVIOUS, self));
        this.minecraft.player.inventoryMenu.removeSlotListener(this.listener);
        this.listener = new JourneyInventoryListener(this.minecraft);
        this.minecraft.player.inventoryMenu.addSlotListener(this.listener);
        if (!selectedTab.shouldDisplay()) {
            this.selectTab(CreativeModeTabs.getDefaultTab());
        }

        ScreenPosition survivalButtonPos = new ScreenPosition(this.leftPos + 149, this.topPos + this.imageHeight + 5);
        this.addRenderableWidget(
                new ImageButton(survivalButtonPos.x(), survivalButtonPos.y(), 20, 18, JourneyInventoryScreen.JOURNEY_BUTTON_TEXTURES, (button) -> {
                    Minecraft.getInstance().setScreen(new InventoryScreen(Minecraft.getInstance().player));
                })
        );
    }

    public void resize(int width, int height) {
        int i = ((JourneyScreenHandler) this.menu).getRow(this.scrollPosition);
        String string  = this.searchBox.getValue();
        this.init(width, height);
        this.searchBox.setValue(string);
        if (!this.searchBox.getValue().isEmpty()) {
            this.search();
        }

        this.scrollPosition = ((JourneyScreenHandler) this.menu).getScrollPosition(i);
        ((JourneyScreenHandler) this.menu).scrollItems(this.scrollPosition);
    }

    public void removed() {
        super.removed();
        if (this.minecraft.player != null && this.minecraft.player.getInventory() != null) {
            this.minecraft.player.inventoryMenu.removeSlotListener(this.listener);
        }
    }

    public boolean charTyped(CharacterEvent characterEvent) {
        if (this.ignoreTypedCharacter) {
            return false;
        } else if (selectedTab.getType() != CreativeModeTab.Type.SEARCH) {
            return false;
        } else {
            String string = this.searchBox.getValue();
            if (this.searchBox.charTyped(characterEvent)) {
                if (!Objects.equals(string, this.searchBox.getValue())) {
                    this.search();
                }

                return true;
            } else {
                return false;
            }
        }
    }

    public boolean keyPressed(KeyEvent keyEvent) {
        int keyCode = keyEvent.key();
        if (keyCode == 266) {
            if (this.switchToPreviousPage()) {
                return true;
            } else if (keyCode == 267 && this.switchToNextPage()) {
                return true;
            }
        }
        this.ignoreTypedCharacter = false;
        if (selectedTab.getType() != CreativeModeTab.Type.SEARCH) {
            if (this.minecraft.options.keyChat.matches(keyEvent)) {
                this.ignoreTypedCharacter = true;
                this.selectTab(CreativeModeTabs.searchTab());
                return true;
            } else {
                return super.keyPressed(keyEvent);
            }
        } else {
            boolean bl = !this.isCreativeInventorySlot(this.hoveredSlot) || this.hoveredSlot.hasItem();
            boolean bl2 = InputConstants.getKey(keyEvent).getNumericKeyValue().isPresent();
            if (bl && bl2 && this.checkHotbarKeyPressed(keyEvent)) {
                this.ignoreTypedCharacter = true;
                return true;
            } else {
                String string = this.searchBox.getValue();
                if (this.searchBox.keyPressed(keyEvent)) {
                    if (!Objects.equals(string, this.searchBox.getValue())) {
                        this.search();
                    }

                    return true;
                } else {
                    return this.searchBox.isFocused() && this.searchBox.isVisible() && keyCode != 256 || super.keyPressed(keyEvent);
                }
            }
        }
    }

    private boolean isTabVisible(CreativeModeTab group) { // FROM FABRIC API FOR CREATIVE INVENTORY
        return group.shouldDisplay() && currentPage == this.getPage(group);
    }

    public int getPage(CreativeModeTab group) {
        if (JourneyCreativeGuiComponents.COMMON_GROUPS.contains(group)) {
            return currentPage;
        } else {
            return itemGroupPage.get(group);
        }
    }

    private boolean hasGroupForPage(int page) {
        return CreativeModeTabs.tabs()
                .stream()
                .anyMatch(creativeModeTab -> getPage(creativeModeTab) == page);
    }

    @Override
    public boolean switchToPage(int page) {
        if (!hasGroupForPage(page)) {
            return false;
        }

        if (currentPage == page) {
            return false;
        }

        currentPage = page;
        updateSelection();
        return true;
    }

    public boolean switchToNextPage() {
        return this.switchToPage(this.getCurrentPage() + 1);
    }

    public boolean switchToPreviousPage() {
        return this.switchToPage(this.getCurrentPage() - 1);
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getPageCount() {
        return JourneyCreativeGuiComponents.getPageCount();
    }

    @Override
    public List<CreativeModeTab> getTabsOnPage(int page) {
        return CreativeModeTabs.tabs()
                .stream()
                .filter(creativeModeTab -> getPage(creativeModeTab) == page)
                // Thanks to isXander for the sorting
                .sorted(Comparator.comparing(CreativeModeTab::row).thenComparingInt(CreativeModeTab::column))
                .sorted((a, b) -> Boolean.compare(a.isAlignedRight(), b.isAlignedRight()))
                .toList();
    }

    @Override
    public boolean hasAdditionalPages() {
        return CreativeModeTabs.tabs().size() > (this.operatorTabEnabled ? 14 : 13);
    }

    public CreativeModeTab getSelectedTab() {
        return selectedTab;
    }

    @Override
    public boolean setSelectedTab(CreativeModeTab creativeModeTab) {
        Objects.requireNonNull(creativeModeTab, "creativeModeTab");

        if (selectedTab == creativeModeTab) {
            return false;
        }

        if (currentPage != getPage(creativeModeTab)) {
            if (!switchToPage(getPage(creativeModeTab))) {
                return false;
            }
        }

        selectTab(creativeModeTab);
        return true;
    }

    private void paginateTabs() {
        List<ResourceKey<CreativeModeTab>> vanillaGroups = List.of(CreativeModeTabs.BUILDING_BLOCKS, CreativeModeTabs.COLORED_BLOCKS, CreativeModeTabs.NATURAL_BLOCKS,
                CreativeModeTabs.FUNCTIONAL_BLOCKS, CreativeModeTabs.REDSTONE_BLOCKS, CreativeModeTabs.HOTBAR, CreativeModeTabs.SEARCH, CreativeModeTabs.TOOLS_AND_UTILITIES, CreativeModeTabs.COMBAT,
                CreativeModeTabs.FOOD_AND_DRINKS, CreativeModeTabs.INGREDIENTS, CreativeModeTabs.SPAWN_EGGS, CreativeModeTabs.OP_BLOCKS, CreativeModeTabs.INVENTORY);
        int count = 0;
        Comparator<Holder.Reference<CreativeModeTab>> entryComparator = (e1, e2) -> {
            int displayCompare = Boolean.compare(((CreativeModeTab) e1.value()).shouldDisplay(), ((CreativeModeTab) e2.value()).shouldDisplay());
            return displayCompare != 0 ? -displayCompare : compareNamespaceFirst(e1.key().identifier(), e2.key().identifier());
        };
        List<Holder.Reference<CreativeModeTab>> sortedItemGroups = BuiltInRegistries.CREATIVE_MODE_TAB.listElements().sorted(entryComparator).toList();
        Iterator<Holder.Reference<CreativeModeTab>> groupIterator = sortedItemGroups.iterator();

        while (groupIterator.hasNext()) {
            Holder.Reference<CreativeModeTab> reference = (Holder.Reference<CreativeModeTab>) groupIterator.next();
            CreativeModeTab itemGroup = (CreativeModeTab) reference.value();
            if (vanillaGroups.contains(reference.key())) {
                itemGroupPage.put(itemGroup, 0);
            } else {
                itemGroupPage.put(itemGroup, count / 10 + 1);
                ++count;
            }
        }
    }

    private static int compareNamespaceFirst(Identifier a, Identifier b) {
        int c = a.getNamespace().compareTo(b.getNamespace());
        return c != 0 ? c : a.getPath().compareTo(b.getPath());
    }

    public boolean keyReleased(KeyEvent keyEvent) {
        this.ignoreTypedCharacter = false;
        return super.keyReleased(keyEvent);
    }

    private void search() {
        ((JourneyScreenHandler) this.menu).itemList.clear();
        this.searchResultTags.clear();
        String searchString = this.searchBox.getValue();
        if (searchString.isEmpty()) {
            Collection<ItemStack> filteredItems = filterUnlockedItems(selectedTab.getDisplayItems());
            ((JourneyScreenHandler) this.menu).itemList.addAll(filteredItems);
        } else {
            ClientPacketListener clientPlayNetworkHandler = this.minecraft.getConnection();
            if (clientPlayNetworkHandler != null) {
                SessionSearchTrees searchManager = clientPlayNetworkHandler.searchTrees();
                SearchTree searchProvider;
                if (searchString.startsWith("#")) {
                    searchString = searchString.substring(1);
                    searchProvider = searchManager.creativeTagSearch();
                    this.searchForTags(searchString);
                } else {
                    searchProvider = searchManager.creativeNameSearch();
                }

                Collection<ItemStack> filteredItems = filterUnlockedItems((Collection<ItemStack>) searchProvider.search(searchString.toLowerCase(Locale.ROOT)));
                ((JourneyScreenHandler) this.menu).itemList.addAll(filteredItems);
            }
        }

        this.scrollPosition = 0.0F;
        ((JourneyScreenHandler) this.menu).scrollItems(0.0F);
    }

    private void searchForTags(String id) {
        int i = id.indexOf(58);
        Predicate<Identifier> predicate;
        if (i == -1) {
            predicate = (Identifier idx) -> {
                return idx.getPath().contains(id);
            };
        } else {
            String string = id.substring(0, i).trim();
            String string2 = id.substring(i+1).trim();
            predicate = (Identifier idx) -> {
                return idx.getNamespace().contains(string) && idx.getPath().contains(string2);
            };
        }

        Stream var10000 = BuiltInRegistries.ITEM.getTags().map(HolderSet.Named::key).filter((tag) -> {
            return predicate.test(tag.location());
        });
        Set var10001 = this.searchResultTags;
        Objects.requireNonNull(var10001);
        var10000.forEach(var10001::add);
    }

    protected void renderLabels(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        if (selectedTab.showTitle()) {
            context.text(this.font, selectedTab.getDisplayName(), 8, 6, -12566464, false);
        }
    }

    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        double mouseX = click.x(), mouseY = click.y();
        if (click.button() == 0) {
            double d = mouseX - (double) this.leftPos;
            double e = mouseY - (double) this.topPos;
            Iterator var10 = CreativeModeTabs.tabs().iterator();

            while (var10.hasNext()) {
                CreativeModeTab itemGroup = (CreativeModeTab) var10.next();
                if (itemGroup.getType() == CreativeModeTab.Type.HOTBAR) continue;
                if (this.isClickInTab(itemGroup, d, e)) {
                    return true;
                }
            }

            if (selectedTab.getType() != CreativeModeTab.Type.INVENTORY && this.isClickInScrollbar(mouseX, mouseY)) {
                this.scrolling = this.hasScrollbar();
                return true;
            }
        }

        return super.mouseClicked(click, doubled);
    }

    public boolean mouseReleased(MouseButtonEvent click) {
        double mouseX = click.x(), mouseY = click.y();
        if (click.button() == 0) {
            double d = mouseX - (double) this.leftPos;
            double e = mouseY - (double) this.topPos;
            this.scrolling = false;
            Iterator var10 = CreativeModeTabs.tabs().iterator();

            while (var10.hasNext()) {
                CreativeModeTab itemGroup = (CreativeModeTab) var10.next();
                if (itemGroup.getType() == CreativeModeTab.Type.HOTBAR) continue;
                if (this.isClickInTab(itemGroup, d, e)) {
                    this.selectTab(itemGroup);
                    return true;
                }
            }
        }

        return super.mouseReleased(click);
    }

    private boolean hasScrollbar() {
        return selectedTab.canScroll() && ((JourneyScreenHandler) this.menu).shouldShowScrollbar();
    }

    private Collection<ItemStack> filterUnlockedItems(Collection<ItemStack> unfilteredItems) {
        Collection<ItemStack> filtered = ItemStackLinkedSet.createTypeAndComponentsSet();
        for (ItemStack itemStack : unfilteredItems) {
            if (PlayerClientUnlocksData.isUnlocked(itemStack)) {
                filtered.add(itemStack);
            }
        }

        return filtered;
    }

    public void selectTab(CreativeModeTab group) {
        if (!this.isTabVisible(group)) {
            return;
        }
        CreativeModeTab itemGroup = selectedTab;
        selectedTab = group;
        ((JourneyScreenHandler) this.menu).itemList.clear();
        this.clearDraggingState();
        int i;
        int j;
        if (selectedTab.getType() == CreativeModeTab.Type.CATEGORY) {
            Collection<ItemStack> filteredItems = filterUnlockedItems(selectedTab.getDisplayItems());
            ((JourneyScreenHandler) this.menu).itemList.addAll(filteredItems);
        }

        if (selectedTab.getType() == CreativeModeTab.Type.INVENTORY) {
            AbstractContainerMenu screenHandler = this.minecraft.player.inventoryMenu;
            if (this.slots == null) {
                this.slots = ImmutableList.copyOf(((JourneyScreenHandler)this.menu).slots);
            }

            ((JourneyScreenHandler) this.menu).slots.clear();

            for (i = 0; i < screenHandler.slots.size(); ++i) {
                int n;
                int k;
                int l;
                int m;
                if (i >= 5 && i < 9) {
                    k = i - 5;
                    l = k / 2;
                    m = k % 2;
                    n = 54 + l * 54;
                    j = 6 + m * 27;
                } else if (i >= 0 && i < 5) {
                    n = -2000;
                    j = -2000;
                } else if (i == 45) {
                    n = 35;
                    j = 20;
                } else {
                    k = i - 9;
                    l = k % 9;
                    m = k / 9;
                    n = 9 + l * 18;
                    if (i >= 36) {
                        j = 112;
                    } else {
                        j = 54 + m * 18;
                    }
                }

                Slot slot = new JourneySlot((Slot) screenHandler.slots.get(i), i, n, j);
                ((JourneyScreenHandler) this.menu).slots.add(slot);
            }

            this.deleteItemSlot = new Slot(this.menu.trashcanInventory, 0, 173, 112);
            ((JourneyScreenHandler) this.menu).slots.add(this.deleteItemSlot);
        } else if (itemGroup.getType() == CreativeModeTab.Type.INVENTORY) {
            ((JourneyScreenHandler) this.menu).slots.clear();
            ((JourneyScreenHandler) this.menu).slots.addAll(this.slots);
            this.slots = null;
        }

        if (selectedTab.getType() == CreativeModeTab.Type.SEARCH) {
            this.searchBox.setVisible(true);
            this.searchBox.setCanLoseFocus(false);
            this.searchBox.setFocused(true);
            if (itemGroup != group) {
                this.searchBox.setValue("");
            }

            this.search();
        } else {
            this.searchBox.setVisible(false);
            this.searchBox.setCanLoseFocus(true);
            this.searchBox.setFocused(false);
            this.searchBox.setValue("");
        }

        this.scrollPosition = 0.0F;
        ((JourneyScreenHandler) this.menu).scrollItems(0.0F);
    }

    private void updateSelection() { //FROM Fabric API CreativeInventoryScreenMixin.class
        if (!this.isTabVisible(selectedTab)) {
            CreativeModeTabs.tabs().stream().filter(this::isTabVisible).min((a, b) -> {
                return Boolean.compare(a.isAlignedRight(), b.isAlignedRight());
            }).ifPresent(this::selectTab);
        }
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
            return true;
        } else if (!this.hasScrollbar()) {
            return false;
        } else {
            this.scrollPosition = ((JourneyScreenHandler) this.menu).getScrollPosition(this.scrollPosition, verticalAmount);
            ((JourneyScreenHandler) this.menu).scrollItems(this.scrollPosition);
            return true;
        }
    }

    protected boolean hasClickedOutside(double mouseX, double mouseY, int left, int top) {
        boolean bl = mouseX < (double)left || mouseY < (double)top || mouseX >= (double)(left + this.imageWidth) || mouseY >= (double)(top + this.imageHeight);
        this.lastClickOutsideBounds = bl && !this.isClickInTab(selectedTab, mouseX, mouseY);
        return this.lastClickOutsideBounds;
    }

    protected boolean isClickInScrollbar(double mouseX, double mouseY) {
        int i = this.leftPos;
        int j = this.topPos;
        int k = i + 175;
        int l = j + 18;
        int m = k + 14;
        int n = l + 112;
        return mouseX >= (double) k && mouseY >= (double) l && mouseX < (double) m && mouseY < (double) n;
    }

    public boolean mouseDragged(MouseButtonEvent click, double deltaX, double deltaY) {
        double mouseX = click.x();
        double mouseY = click.y();
        if (this.scrolling) {
            int i = this.topPos + 18;
            int j = i + 112;
            this.scrollPosition = ((float) mouseY - (float) i - 7.5F) / ((float) (j - i) - 15.0F);
            this.scrollPosition = Mth.clamp(this.scrollPosition, 0.0F, 1.0F);
            ((JourneyScreenHandler) this.menu).scrollItems(this.scrollPosition);
            return true;
        } else {
            return super.mouseDragged(click, deltaX, deltaY);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
        super.extractRenderState(context, mouseX, mouseY, deltaTicks);
        this.statusEffectsDisplay.extractRenderState(context, mouseX, mouseY);
        Iterator var5 = CreativeModeTabs.tabs().iterator();

        while (var5.hasNext()) {
            CreativeModeTab itemGroup = (CreativeModeTab)var5.next();
            if (itemGroup.getType() == CreativeModeTab.Type.HOTBAR) continue;
            if (this.renderTabTooltipIfHovered(context, itemGroup, mouseX, mouseY)) {
                break;
            }
        }

        if (this.deleteItemSlot != null &&
                selectedTab.getType() == CreativeModeTab.Type.INVENTORY &&
                this.isHovering(this.deleteItemSlot.x, this.deleteItemSlot.y, 16, 16, (double) mouseX, (double) mouseY)) {
            context.setTooltipForNextFrame(this.font, DELETE_ITEM_SLOT_TEXT, mouseX, mouseY);
        }

        this.extractTooltip(context, mouseX, mouseY);
    }

    public boolean showsActiveEffects() {
        return this.statusEffectsDisplay.canSeeEffects();
    }

    public List<Component> getTooltipFromContainerItem(ItemStack stack) {
        boolean bl = this.hoveredSlot != null && this.hoveredSlot instanceof JourneyInventoryScreen.LockableSlot;
        boolean bl2 = selectedTab.getType() == CreativeModeTab.Type.CATEGORY;
        boolean bl3 = selectedTab.getType() == CreativeModeTab.Type.SEARCH;
        TooltipFlag.Default default_ = this.minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
        TooltipFlag tooltipType = bl ? default_.asCreative() : default_;
        List<Component> list = stack.getTooltipLines(Item.TooltipContext.of(this.minecraft.level), this.minecraft.player, tooltipType);
        if (list.isEmpty()) {
            return list;
        } else if (bl2 && bl) {
            return list;
        } else {
            List<Component> list2 = Lists.newArrayList(list);
            if (bl3 && bl) {
                this.searchResultTags.forEach((tagKey) -> {
                    if (stack.is(tagKey)) {
                        list2.add(1, Component.literal("#" + String.valueOf(tagKey.location())).withStyle(ChatFormatting.DARK_PURPLE));
                    }

                });
            }

            int i = 1;
            Iterator var10 = CreativeModeTabs.tabs().iterator();

            while(var10.hasNext()) {
                CreativeModeTab itemGroup = (CreativeModeTab) var10.next();
                if (itemGroup.getType() != CreativeModeTab.Type.SEARCH && itemGroup.contains(stack)) {
                    list2.add(i++, itemGroup.getDisplayName().copy().withStyle(ChatFormatting.BLUE));
                }
            }

            return list2;
        }
    }

    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
        Iterator var5 = CreativeModeTabs.tabs().iterator();

        while(var5.hasNext()) {
            CreativeModeTab itemGroup = (CreativeModeTab) var5.next();
            if (itemGroup.getType() == CreativeModeTab.Type.HOTBAR) continue;
            if (itemGroup != selectedTab) {
                this.renderTabIcon(context, itemGroup);
            }
        }

        if (selectedTab.getType() == CreativeModeTab.Type.INVENTORY) {
            context.blit(RenderPipelines.GUI_TEXTURED, JourneyInventoryScreen.JOURNEY_INVENTORY_TEXTURE, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
        } else {
            context.blit(RenderPipelines.GUI_TEXTURED, selectedTab.getBackgroundTexture(), this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
        }
        this.searchBox.extractRenderState(context, mouseX, mouseY, deltaTicks);
        int i = this.leftPos + 175;
        int j = this.topPos + 18;
        int k = j + 112;
        if (selectedTab.canScroll()) {
            Identifier identifier = this.hasScrollbar() ? SCROLLER_TEXTURE : SCROLLER_DISABLED_TEXTURE;
            context.blitSprite(RenderPipelines.GUI_TEXTURED, identifier, i, j + (int)((float)(k - j - 17) * this.scrollPosition), 12, 15);
        }

        this.renderTabIcon(context, selectedTab);
        if (selectedTab.getType() == CreativeModeTab.Type.INVENTORY) {
            InventoryScreen.extractEntityInInventoryFollowsMouse(context, this.leftPos + 73, this.topPos + 6, this.leftPos + 105, this.topPos + 49, 20, 0.0625F, (float)mouseX, (float)mouseY, this.minecraft.player);
        }
    }

    private int getTabX(CreativeModeTab group) {
        int i = group.column();
        int k = 27 * i;
        if (group.isAlignedRight()) {
            k = this.imageWidth - 27 * (7 - i) + 1;
        }

        return k;
    }

    private int getTabY(CreativeModeTab group) {
        int i = 0;
        if (group.row() == CreativeModeTab.Row.TOP) {
            i -= 32;
        } else {
            i += this.imageHeight;
        }

        return i;
    }

    protected boolean isClickInTab(CreativeModeTab group, double mouseX, double mouseY) {
        if (!this.isTabVisible(group)) {
            return false;
        }
        int i = this.getTabX(group);
        int j = this.getTabY(group);
        return mouseX >= (double) i && mouseX <= (double) (i + 26) && mouseY >= (double) j && mouseY <= (double) (j + 32);
    }

    protected boolean renderTabTooltipIfHovered(GuiGraphicsExtractor context, CreativeModeTab group, int mouseX, int mouseY) {
        if (!this.isTabVisible(group)) {
            return false;
        }
        int i = this.getTabX(group);
        int j = this.getTabY(group);
        if (this.isHovering(i + 3, j + 3, 21, 27, (double) mouseX, (double) mouseY)) {
            context.setTooltipForNextFrame(this.font, group.getDisplayName(), mouseX, mouseY);
            return true;
        } else {
            return false;
        }
    }

    protected void renderTabIcon(GuiGraphicsExtractor context, CreativeModeTab group) {
        if (!this.isTabVisible(group)) {
            return;
        }
        boolean bl = group == selectedTab;
        boolean bl2 = group.row() == CreativeModeTab.Row.TOP;
        int i = group.column();
        int j = this.leftPos + this.getTabX(group);
        int k = this.topPos - (bl2 ? 28 : -(this.imageHeight - 4));
        Identifier[] identifiers;
        if (bl2) {
            identifiers = bl ? TAB_TOP_SELECTED_TEXTURES : TAB_TOP_UNSELECTED_TEXTURES;
        } else {
            identifiers = bl ? TAB_BOTTOM_SELECTED_TEXTURES : TAB_BOTTOM_UNSELECTED_TEXTURES;
        }

        context.blitSprite(RenderPipelines.GUI_TEXTURED, identifiers[Mth.clamp(i, 0, identifiers.length)], j, k, 26, 32);
        int l = j + 13 - 8;
        int m = k + 16 - 8 + (bl2 ? 1 : -1);
        context.item(group.getIconItem(), l, m);
    }

    public boolean isInventoryTabSelected() {
        return selectedTab.getType() == CreativeModeTab.Type.INVENTORY;
    }

    public static void onHotbarKeyPress(Minecraft client, int index, boolean restore, boolean save) {
        LocalPlayer clientPlayerEntity = client.player;
        RegistryAccess dynamicRegistryManager = clientPlayerEntity.level().registryAccess();
        HotbarManager hotbarStorage = client.getHotbarManager();
        Hotbar hotbarStorageEntry = hotbarStorage.get(index);
        if (restore) {
            List<ItemStack> list = hotbarStorageEntry.load(dynamicRegistryManager);

            for(int i = 0; i < Inventory.getSelectionSize(); ++i) {
                ItemStack itemStack = (ItemStack)list.get(i);
                clientPlayerEntity.getInventory().setItem(i, itemStack);
                JourneyClientNetworking.clickJourneyStack(itemStack, 36 + i);
            }

            clientPlayerEntity.inventoryMenu.broadcastChanges();
        } else if (save) {
            hotbarStorageEntry.storeFrom(clientPlayerEntity.getInventory(), dynamicRegistryManager);
            Component text = client.options.keyHotbarSlots[index].getTranslatedKeyMessage();
            Component text2 = client.options.keyLoadHotbarActivator.getTranslatedKeyMessage();
            Component text3 = Component.translatable("inventory.hotbarSaved", new Object[]{text2, text});
            client.gui.setOverlayMessage(text3, false);
            client.getNarrator().saySystemNow(text3);
            hotbarStorage.save();
        }

    }

    public Slot getDeleteItemSlot() {
        return this.deleteItemSlot;
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int xm, int ym) {
        graphics.text(this.font, this.title, this.titleLabelX, this.titleLabelY, -12566464, false);
    }

    @Environment(EnvType.CLIENT)
    public static class JourneyScreenHandler extends AbstractContainerMenu {
        public final NonNullList<ItemStack> itemList = NonNullList.create();
        private final AbstractContainerMenu parent;
        private TrashcanInventory trashcanInventory;
        private SimpleContainer INVENTORY;

        public JourneyScreenHandler(Player player) {
            super((MenuType) null, 0);
            this.parent = player.inventoryMenu;
            Inventory playerInventory = player.getInventory();
            INVENTORY = new SimpleContainer(45);

            for(int i = 0; i < 5; ++i) {
                for(int j = 0; j < 9; ++j) {
                    this.addSlot(new JourneyInventoryScreen.LockableSlot(INVENTORY, i * 9 + j, 9 + j * 18, 18 + i * 18));
                }
            }

            this.addPlayerHotbarSlots(playerInventory, 9, 112);
            this.scrollItems(0.0F);
            this.trashcanInventory = TrashcanServerStorage.get(player);
        }

        public boolean stillValid(Player player) {
            return true;
        }

        protected int getOverflowRows() {
            return Mth.positiveCeilDiv(this.itemList.size(), 9) - 5;
        }

        protected int getRow(float scroll) {
            return Math.max((int)((double)(scroll * (float)this.getOverflowRows()) + 0.5), 0);
        }

        protected float getScrollPosition(int row) {
            return Mth.clamp((float)row / (float)this.getOverflowRows(), 0.0F, 1.0F);
        }

        protected float getScrollPosition(float current, double amount) {
            return Mth.clamp(current - (float)(amount / (double)this.getOverflowRows()), 0.0F, 1.0F);
        }

        public void scrollItems(float position) {
            int i = this.getRow(position);

            for(int j = 0; j < 5; ++j) {
                for(int k = 0; k < 9; ++k) {
                    int l = k + (j + i) * 9;
                    if (l >= 0 && l < this.itemList.size()) {
                        INVENTORY.setItem(k + j * 9, (ItemStack)this.itemList.get(l));
                    } else {
                        INVENTORY.setItem(k + j * 9, ItemStack.EMPTY);
                    }
                }
            }
        }

        public boolean shouldShowScrollbar() {
            return this.itemList.size() > 45;
        }

        public ItemStack quickMoveStack(Player player, int slot) {
            if (slot >= this.slots.size() - 9 && slot < this.slots.size()) {
                Slot slot2 = (Slot)this.slots.get(slot);
                if (slot2 != null && slot2.hasItem()) {
                    slot2.setByPlayer(ItemStack.EMPTY);
                }
            }

            return ItemStack.EMPTY;
        }

        public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
            return slot.container != JourneyInventoryScreen.INVENTORY;
        }

        public boolean canDragTo(Slot slot) {
            return slot.container != JourneyInventoryScreen.INVENTORY;
        }

        public ItemStack getCarried() {
            return this.parent.getCarried();
        }

        public void setCarried(ItemStack stack) {
            this.parent.setCarried(stack);
        }

        @Override
        public void setItem(int slot, int revision, ItemStack stack) {
            if (slot < 45 && JourneyInventoryScreen.selectedTab.getType() != CreativeModeTab.Type.INVENTORY) {
                this.parent.setItem(slot, revision, stack);
                return;
            }
            super.setItem(slot, revision, stack);
        }

        protected boolean insertItemTrashcan(ItemStack stack, int startIndex, int endIndex, boolean fromLast) {
            boolean bl = false;
            int i = startIndex;
            if (fromLast) {
                i = endIndex - 1;
            }

            Slot slot;
            ItemStack itemStack;
            int j;
            if (stack.isStackable()) {
                while(!stack.isEmpty()) {
                    if (fromLast) {
                        if (i < startIndex) {
                            break;
                        }
                    } else if (i >= endIndex) {
                        break;
                    }

                    slot = (Slot)this.slots.get(i);
                    itemStack = slot.getItem();
                    if (!itemStack.isEmpty() && ItemStack.isSameItemSameComponents(stack, itemStack)) {
                        j = itemStack.getCount() + stack.getCount();
                        int k = slot.getMaxStackSize(itemStack);
                        if (j <= k) {
                            stack.setCount(0);
                            itemStack.setCount(j);
                            slot.setChanged();
                            bl = true;
                        } else if (itemStack.getCount() < k) {
                            stack.shrink(k - itemStack.getCount());
                            itemStack.setCount(k);
                            slot.setChanged();
                            bl = true;
                        }
                    }

                    if (fromLast) {
                        --i;
                    } else {
                        ++i;
                    }
                }
            }

            if (!stack.isEmpty()) {
                if (fromLast) {
                    i = endIndex - 1;
                } else {
                    i = startIndex;
                }

                while(true) {
                    if (fromLast) {
                        if (i < startIndex) {
                            break;
                        }
                    } else if (i >= endIndex) {
                        break;
                    }

                    slot = (Slot)this.slots.get(i);
                    itemStack = slot.getItem();
                    if (itemStack.isEmpty() && slot.mayPlace(stack)) {
                        j = slot.getMaxStackSize(stack);
                        slot.setByPlayer(stack.split(Math.min(stack.getCount(), j)));
                        slot.setChanged();
                        bl = true;
                        break;
                    }

                    if (fromLast) {
                        --i;
                    } else {
                        ++i;
                    }
                }
            }

            return bl;
        }

        @Override
        public void removed(Player player) {
            super.removed(player);

            ItemStack cursorStack = this.getCarried();

            if (!cursorStack.isEmpty()) {
                player.drop(cursorStack, true);
                JourneyClientNetworking.dropJourneyStack(cursorStack, (LocalPlayer) player);
                this.setCarried(ItemStack.EMPTY);
            }
        }

        private void addPlayerHotbarSlots(Inventory playerInventory, int x, int y) {
            int m;

            // Player Hotbar
            for (m = 0; m < 9; ++m) {
                this.addSlot(new Slot(playerInventory, m, x + m * 18, y));
            }
        }

    }

    @Environment(EnvType.CLIENT)
    private static class JourneySlot extends Slot {
        final Slot slot;

        public JourneySlot(Slot slot, int invSlot, int x, int y) {
            super(slot.container, invSlot, x, y);
            this.slot = slot;
        }

        public void onTake(Player player, ItemStack stack) {
            this.slot.onTake(player, stack);
        }

        public boolean mayPlace(ItemStack stack) {
            return this.slot.mayPlace(stack);
        }

        public ItemStack getItem() {
            return this.slot.getItem();
        }

        public boolean hasItem() {
            return this.slot.hasItem();
        }

        public void setByPlayer(ItemStack stack, ItemStack previousStack) {
            this.slot.setByPlayer(stack, previousStack);
        }

        public void set(ItemStack stack) {
            this.slot.set(stack);
        }

        public void setChanged() {
            this.slot.setChanged();
        }

        public int getMaxStackSize() {
            return this.slot.getMaxStackSize();
        }

        public int getMaxStackSize(ItemStack stack) {
            return this.slot.getMaxStackSize(stack);
        }

        @Nullable
        public Identifier getNoItemIcon() {
            return this.slot.getNoItemIcon();
        }

        public ItemStack remove(int amount) {
            return this.slot.remove(amount);
        }

        public boolean isActive() {
            return this.slot.isActive();
        }

        public boolean mayPickup(Player playerEntity) {
            return this.slot.mayPickup(playerEntity);
        }
    }

    @Environment(EnvType.CLIENT)
    static class LockableSlot extends Slot {
        public LockableSlot(Container inventory, int i, int j, int k) {
            super(inventory, i, j, k);
        }

        public boolean mayPickup(Player playerEntity) {
            ItemStack itemStack = this.getItem();
            if (super.mayPickup(playerEntity) && !itemStack.isEmpty()) {
                return itemStack.isItemEnabled(playerEntity.level().enabledFeatures()) && !itemStack.has(DataComponents.CREATIVE_SLOT_LOCK);
            } else {
                return itemStack.isEmpty();
            }
        }
    }
}

