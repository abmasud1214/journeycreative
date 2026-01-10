package mod.journeycreative.screen;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import mod.journeycreative.Journeycreative;
import mod.journeycreative.networking.JourneyClientNetworking;
import mod.journeycreative.networking.PlayerClientUnlocksData;
import mod.journeycreative.networking.TrashcanServerStorage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.itemgroup.v1.FabricCreativeInventoryScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenPos;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.ingame.StatusEffectsDisplay;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.HotbarStorage;
import net.minecraft.client.option.HotbarStorageEntry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.search.SearchManager;
import net.minecraft.client.search.SearchProvider;
import net.minecraft.client.util.InputUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.*;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class JourneyInventoryScreen extends HandledScreen<JourneyInventoryScreen.JourneyScreenHandler> implements FabricCreativeInventoryScreen {
    public static final ButtonTextures JOURNEY_BUTTON_TEXTURES = new ButtonTextures(
            Identifier.of(Journeycreative.MOD_ID, "journey_button"),
            Identifier.of(Journeycreative.MOD_ID, "journey_button_highlighted"));

    private static final Identifier SCROLLER_TEXTURE = Identifier.ofVanilla("container/creative_inventory/scroller");
    private static final Identifier SCROLLER_DISABLED_TEXTURE = Identifier.ofVanilla("container/creative_inventory/scroller_disabled");
    private static final Identifier[] TAB_TOP_UNSELECTED_TEXTURES = new Identifier[]{Identifier.ofVanilla("container/creative_inventory/tab_top_unselected_1"), Identifier.ofVanilla("container/creative_inventory/tab_top_unselected_2"), Identifier.ofVanilla("container/creative_inventory/tab_top_unselected_3"), Identifier.ofVanilla("container/creative_inventory/tab_top_unselected_4"), Identifier.ofVanilla("container/creative_inventory/tab_top_unselected_5"), Identifier.ofVanilla("container/creative_inventory/tab_top_unselected_6"), Identifier.ofVanilla("container/creative_inventory/tab_top_unselected_7")};
    private static final Identifier[] TAB_TOP_SELECTED_TEXTURES = new Identifier[]{Identifier.ofVanilla("container/creative_inventory/tab_top_selected_1"), Identifier.ofVanilla("container/creative_inventory/tab_top_selected_2"), Identifier.ofVanilla("container/creative_inventory/tab_top_selected_3"), Identifier.ofVanilla("container/creative_inventory/tab_top_selected_4"), Identifier.ofVanilla("container/creative_inventory/tab_top_selected_5"), Identifier.ofVanilla("container/creative_inventory/tab_top_selected_6"), Identifier.ofVanilla("container/creative_inventory/tab_top_selected_7")};
    private static final Identifier[] TAB_BOTTOM_UNSELECTED_TEXTURES = new Identifier[]{Identifier.ofVanilla("container/creative_inventory/tab_bottom_unselected_1"), Identifier.ofVanilla("container/creative_inventory/tab_bottom_unselected_2"), Identifier.ofVanilla("container/creative_inventory/tab_bottom_unselected_3"), Identifier.ofVanilla("container/creative_inventory/tab_bottom_unselected_4"), Identifier.ofVanilla("container/creative_inventory/tab_bottom_unselected_5"), Identifier.ofVanilla("container/creative_inventory/tab_bottom_unselected_6"), Identifier.ofVanilla("container/creative_inventory/tab_bottom_unselected_7")};
    private static final Identifier[] TAB_BOTTOM_SELECTED_TEXTURES = new Identifier[]{Identifier.ofVanilla("container/creative_inventory/tab_bottom_selected_1"), Identifier.ofVanilla("container/creative_inventory/tab_bottom_selected_2"), Identifier.ofVanilla("container/creative_inventory/tab_bottom_selected_3"), Identifier.ofVanilla("container/creative_inventory/tab_bottom_selected_4"), Identifier.ofVanilla("container/creative_inventory/tab_bottom_selected_5"), Identifier.ofVanilla("container/creative_inventory/tab_bottom_selected_6"), Identifier.ofVanilla("container/creative_inventory/tab_bottom_selected_7")};
    private static final Identifier JOURNEY_INVENTORY_TEXTURE = Identifier.of(Journeycreative.MOD_ID, "textures/gui/gui_journey_inventory.png");
    private static final int ROWS_COUNT = 5;
    private static final int COLUMNS_COUNT = 9;
    private static final int TAB_WIDTH = 26;
    private static final int TAB_HEIGHT = 32;
    private static final int SCROLLBAR_WIDTH = 12;
    private static final int SCROLLBAR_HEIGHT = 15;
    static final SimpleInventory INVENTORY = new SimpleInventory(45);
    private static final Text DELETE_ITEM_SLOT_TEXT = Text.translatable("inventory.binSlot");
    private static ItemGroup selectedTab = ItemGroups.getDefaultTab();
    private static int currentPage = 0;
    private float scrollPosition;
    private boolean scrolling;
    private TextFieldWidget searchBox;
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
    private final StatusEffectsDisplay statusEffectsDisplay;
    private final Map<ItemGroup, Integer> itemGroupPage = new HashMap();

    public JourneyInventoryScreen(ClientPlayerEntity player, FeatureSet enabledFeatures, boolean operatorTabEnabled) {
        super(new JourneyScreenHandler(player), player.getInventory(), ScreenTexts.EMPTY);
        player.currentScreenHandler = this.handler;
        this.backgroundHeight = 136;
        this.backgroundWidth = 195;
        this.operatorTabEnabled = operatorTabEnabled;
        this.statusEffectsDisplay = new StatusEffectsDisplay(this);
    }

    private boolean shouldShowOperatorTab(PlayerEntity player) {
        return false;
    }

    private void updateDisplayParameters(FeatureSet enabledFeatures, boolean showOperatorTab, RegistryWrapper.WrapperLookup registries) {
        ClientPlayNetworkHandler clientPlayNetworkHandler = this.client.getNetworkHandler();

        if (this.populateDisplay(clientPlayNetworkHandler != null ? clientPlayNetworkHandler.getSearchManager() : null, enabledFeatures, showOperatorTab, registries)) {
            Iterator var5 = ItemGroups.getGroups().iterator();

            while(true) {
                while(true) {
                    ItemGroup itemGroup;
                    Collection collection;
                    do {
                        if (!var5.hasNext()) {
                            return;
                        }

                        itemGroup = (ItemGroup)var5.next();
                        collection = itemGroup.getDisplayStacks();
                    } while(itemGroup != selectedTab);

                    if (itemGroup.getType() == ItemGroup.Type.CATEGORY && collection.isEmpty()) {
                        this.setSelectedTab(ItemGroups.getDefaultTab());
                    } else {
                        collection = filterUnlockedItems(collection);
                        this.refreshSelectedTab(collection);
                    }
                }
            }
        }

    }

    private boolean populateDisplay(@Nullable SearchManager searchManager, FeatureSet enabledFeatures, boolean showOperatorTab, RegistryWrapper.WrapperLookup registries) {
        if (!ItemGroups.updateDisplayContext(enabledFeatures, showOperatorTab, registries)) {
            return false;
        } else {
            if (searchManager != null) {
                List<ItemStack> list = List.copyOf(ItemGroups.getSearchGroup().getDisplayStacks());
                searchManager.addItemTooltipReloader(registries, list);
                searchManager.addItemTagReloader(list);
            }

            return true;
        }
    }

    private void refreshSelectedTab(Collection<ItemStack> displayStacks) {
        int i = ((JourneyScreenHandler) this.handler).getRow(this.scrollPosition);
        ((JourneyScreenHandler) this.handler).itemList.clear();
        if (selectedTab.getType() == ItemGroup.Type.SEARCH) {
            this.search();
        } else {
            ((JourneyScreenHandler) this.handler).itemList.addAll(displayStacks);
        }

        this.scrollPosition = ((JourneyScreenHandler) this.handler).getScrollPosition(i);
        ((JourneyScreenHandler) this.handler).scrollItems(this.scrollPosition);
    }

    public void handledScreenTick() {
        super.handledScreenTick();
        if (this.client != null) {
            ClientPlayerEntity clientPlayerEntity = this.client.player;
            if (clientPlayerEntity != null) {
                this.updateDisplayParameters(clientPlayerEntity.networkHandler.getEnabledFeatures(), this.shouldShowOperatorTab(clientPlayerEntity), clientPlayerEntity.getWorld().getRegistryManager());
            }
        }
    }

    protected void onMouseClick(@Nullable Slot slot, int slotId, int button, SlotActionType actionType) {
        if (this.isCreativeInventorySlot(slot)) {
            this.searchBox.setCursorToEnd(false);
            this.searchBox.setSelectionEnd(0);
        }

        boolean bl = actionType == SlotActionType.QUICK_MOVE;
        actionType = slotId == -999 && actionType == SlotActionType.PICKUP ? SlotActionType.THROW : actionType; // If pickup action, then throw
        if (actionType != SlotActionType.THROW || this.client.player.canDropItems()) { // if not throw, or throw not on cooldown
            this.onMouseClick(slot, actionType);
            ItemStack itemStack;
            if (slot == null && selectedTab.getType() != ItemGroup.Type.INVENTORY && actionType != SlotActionType.QUICK_CRAFT) { // click outside inventory
                if (!((JourneyScreenHandler) this.handler).getCursorStack().isEmpty() && this.lastClickOutsideBounds) {
                    if (!this.client.player.canDropItems()) {
                        return;
                    }

                    if (button == 0) {
                        this.client.player.dropItem(((JourneyScreenHandler) this.handler).getCursorStack(), true);
                        JourneyClientNetworking.dropJourneyStack(((JourneyScreenHandler) this.handler).getCursorStack(), this.client.player);
                        ((JourneyScreenHandler) this.handler).setCursorStack(ItemStack.EMPTY);
                    }

                    if (button == 1) {
                        itemStack = ((JourneyScreenHandler) this.handler).getCursorStack().split(1);
                        this.client.player.dropItem(itemStack, true);
                        JourneyClientNetworking.dropJourneyStack(itemStack, this.client.player);
                    }
                }
            } else {
                if (slot != null && !slot.canTakeItems(this.client.player)) {
                    return;
                }

                if (slot == this.deleteItemSlot && bl && this.deleteItemSlot.hasStack()) { // shift click delete item slot
                    ItemStack tcStack = this.deleteItemSlot.getStack();
                    boolean ret = this.handler.insertItem(tcStack, 9, 46, false);
                    if(ret) {
                        for (int k = 9; k < 45; ++k) {
                            Slot s = ((JourneyScreenHandler) this.handler).getSlot(k);
                            JourneyClientNetworking.clickJourneyStack(s.getStack(), ((JourneySlot) s).slot.id);
                        }
                        JourneyClientNetworking.sendTrashcanUpdate(tcStack);
                    }
                } else {
                    ItemStack itemStack2;
                    if (selectedTab.getType() == ItemGroup.Type.INVENTORY) { // player inventory visible
                        if (slot == this.deleteItemSlot) { // click delete item slot
                            ItemStack cursorStack = this.handler.getCursorStack().copy();
                            if (cursorStack.isEmpty() && this.deleteItemSlot.hasStack()) {
                                itemStack2 = this.deleteItemSlot.getStack();
                                ((JourneyScreenHandler) this.handler).setCursorStack(itemStack2);
                                JourneyClientNetworking.sendTrashcanUpdate(ItemStack.EMPTY);
                            } else {
                                JourneyClientNetworking.sendTrashcanUpdate(cursorStack);
                                ((JourneyScreenHandler) this.handler).setCursorStack(ItemStack.EMPTY);
                            }
                        } else if (bl && slot != null && slot.hasStack()) { // shift click slot.
                            itemStack2 = slot.getStack();
                            JourneyClientNetworking.sendTrashcanUpdate(itemStack2);
                            slot.setStack(ItemStack.EMPTY);
                            JourneyClientNetworking.clickJourneyStack(ItemStack.EMPTY, ((JourneySlot) slot).slot.id);
                        } else if (actionType == SlotActionType.THROW && slot != null && slot.hasStack()) {
                            itemStack = slot.takeStack(button == 0 ? 1 : slot.getStack().getMaxCount());
                            itemStack2 = slot.getStack();
                            this.client.player.dropItem(itemStack, true);
                            JourneyClientNetworking.dropJourneyStack(itemStack, this.client.player);
                            JourneyClientNetworking.clickJourneyStack(itemStack2, ((JourneySlot) slot).slot.id);
                        } else if (actionType == SlotActionType.THROW && slotId == -999 && !((JourneyScreenHandler) this.handler).getCursorStack().isEmpty()) {
                            this.client.player.dropItem(((JourneyScreenHandler) this.handler).getCursorStack(), true);
                            JourneyClientNetworking.dropJourneyStack(((JourneyScreenHandler) this.handler).getCursorStack(), this.client.player);
                            ((JourneyScreenHandler) this.handler).setCursorStack(ItemStack.EMPTY);
                        } else {
                            this.client.player.playerScreenHandler.onSlotClick(slot == null ? slotId : ((JourneySlot) slot).slot.id, button, actionType, this.client.player);
                        }
                        for (int k = 9; k < 45; ++k) {
                            Slot s = ((JourneyScreenHandler) this.handler).getSlot(k);
                            JourneyClientNetworking.clickJourneyStack(s.getStack(), ((JourneySlot) s).slot.id);
                        }
                    } else {
                        ItemStack itemStack3;
                        if (actionType != SlotActionType.QUICK_CRAFT && slot.inventory == INVENTORY) {
                            itemStack = ((JourneyScreenHandler) this.handler).getCursorStack();
                            itemStack2 = slot.getStack();
                            if (actionType == SlotActionType.SWAP) {
                                if (!itemStack2.isEmpty() && this.client.player.getInventory().getStack(button).isEmpty()) {
                                    this.client.player.getInventory().setStack(button, itemStack2.copyWithCount(itemStack2.getMaxCount()));
                                    this.client.player.playerScreenHandler.sendContentUpdates();
                                }

                                return;
                            }

                            if (actionType == SlotActionType.CLONE) {
                                if (((JourneyScreenHandler) this.handler).getCursorStack().isEmpty() && slot.hasStack()) {
                                    itemStack3 = slot.getStack();
                                    ((JourneyScreenHandler) this.handler).setCursorStack(itemStack3.copyWithCount(itemStack3.getMaxCount()));
                                }

                                return;
                            }

                            if (actionType == SlotActionType.THROW) {
                                if (!itemStack2.isEmpty()) {
                                    itemStack3 = itemStack2.copyWithCount(button == 0 ? 1 : itemStack2.getMaxCount());
                                    this.client.player.dropItem(itemStack3, true);
                                    JourneyClientNetworking.dropJourneyStack(itemStack3, this.client.player);
                                }

                                return;
                            }

                            if (!itemStack.isEmpty() && !itemStack2.isEmpty() && ItemStack.areItemsAndComponentsEqual(itemStack, itemStack2)) {
                                if (button == 0) {
                                    if (bl) {
                                        itemStack.setCount(itemStack.getMaxCount());
                                    } else if (itemStack.getCount() < itemStack.getMaxCount()) {
                                        itemStack.increment(1);
                                    }
                                } else {
                                    itemStack.decrement(1);
                                }
                            } else if (!itemStack2.isEmpty() && itemStack.isEmpty()) {
                                int j = bl ? itemStack2.getMaxCount() : itemStack2.getCount();
                                ((JourneyScreenHandler) this.handler).setCursorStack(itemStack2.copyWithCount(j));
                            } else if (button == 0) {
                                ((JourneyScreenHandler) this.handler).setCursorStack(ItemStack.EMPTY);
                            } else if (!((JourneyScreenHandler) this.handler).getCursorStack().isEmpty()) {
                                ((JourneyScreenHandler) this.handler).getCursorStack().decrement(1);
                            }
                        } else if (this.handler != null) {
                            itemStack = slot == null ? ItemStack.EMPTY : ((JourneyScreenHandler) this.handler).getSlot(slot.id).getStack();
                            ((JourneyScreenHandler) this.handler).onSlotClick(slot == null ? slotId : slot.id, button, actionType, this.client.player);
                            int k;
                            if (ScreenHandler.unpackQuickCraftStage(button) == 2) {
                                for (k = 0; k < 9; ++k) {
                                    JourneyClientNetworking.clickJourneyStack(((JourneyScreenHandler) this.handler).getSlot(45 + k).getStack(), 36 + k);
                                }
                            } else if (slot != null && PlayerInventory.isValidHotbarIndex(slot.getIndex()) && selectedTab.getType() != ItemGroup.Type.INVENTORY) {
                                if (actionType == SlotActionType.THROW && !itemStack.isEmpty() && !((JourneyScreenHandler) this.handler).getCursorStack().isEmpty()) {
                                    k = button == 0 ? 1 : itemStack.getCount();
                                    itemStack3 = itemStack.copyWithCount(k);
                                    itemStack.decrement(k);
                                    this.client.player.dropItem(itemStack3, true);
                                    JourneyClientNetworking.dropJourneyStack(itemStack3, this.client.player);
                                }

                                this.client.player.playerScreenHandler.sendContentUpdates();
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isCreativeInventorySlot(@Nullable Slot slot) {
        return slot != null && slot.inventory == INVENTORY;
    }

    @Override
    protected  void init() {
        super.init();
        paginateGroups();
        TextRenderer var10003 = this.textRenderer;
        int var10004 = this.x + 82;
        int var10005 = this.y + 6;
        Objects.requireNonNull(this.textRenderer);
        this.searchBox = new TextFieldWidget(var10003, var10004, var10005, 80, 9, Text.translatable("itemGroup.search"));
        this.searchBox.setMaxLength(50);
        this.searchBox.setDrawsBackground(false);
        this.searchBox.setVisible(false);
        this.searchBox.setEditableColor(-1);
        currentPage = this.getPage(selectedTab);
        int xpos = this.x + 171;
        int ypos = this.y + 4;
        this.addSelectableChild(this.searchBox);
        ItemGroup itemGroup = selectedTab;
        selectedTab = ItemGroups.getDefaultTab();
        this.setSelectedTab(itemGroup);
        JourneyInventoryScreen self = (JourneyInventoryScreen) this;
        this.addDrawableChild(new JourneyCreativeGuiComponents.ItemGroupButtonWidget(xpos + 10, ypos, JourneyCreativeGuiComponents.Type.NEXT, self));
        this.addDrawableChild(new JourneyCreativeGuiComponents.ItemGroupButtonWidget(xpos, ypos, JourneyCreativeGuiComponents.Type.PREVIOUS, self));
        this.client.player.playerScreenHandler.removeListener(this.listener);
        this.listener = new JourneyInventoryListener(this.client);
        this.client.player.playerScreenHandler.addListener(this.listener);
        if (!selectedTab.shouldDisplay()) {
            this.setSelectedTab(ItemGroups.getDefaultTab());
        }

        ScreenPos survivalButtonPos = new ScreenPos(this.x + 149, this.y + this.backgroundHeight + 5);
        this.addDrawableChild(
                new TexturedButtonWidget(survivalButtonPos.x(), survivalButtonPos.y(), 20, 18, JourneyInventoryScreen.JOURNEY_BUTTON_TEXTURES, (button) -> {
                    MinecraftClient.getInstance().setScreen(new InventoryScreen(MinecraftClient.getInstance().player));
                })
        );
    }

    public void resize(MinecraftClient client, int width, int height) {
        int i = ((JourneyScreenHandler) this.handler).getRow(this.scrollPosition);
        String string  = this.searchBox.getText();
        this.init(client, width, height);
        this.searchBox.setText(string);
        if (!this.searchBox.getText().isEmpty()) {
            this.search();
        }

        this.scrollPosition = ((JourneyScreenHandler) this.handler).getScrollPosition(i);
        ((JourneyScreenHandler) this.handler).scrollItems(this.scrollPosition);
    }

    public void removed() {
        super.removed();
        if (this.client.player != null && this.client.player.getInventory() != null) {
            this.client.player.playerScreenHandler.removeListener(this.listener);
        }
    }

    public boolean charTyped(char chr, int modifiers) {
        if (this.ignoreTypedCharacter) {
            return false;
        } else if (selectedTab.getType() != ItemGroup.Type.SEARCH) {
            return false;
        } else {
            String string = this.searchBox.getText();
            if (this.searchBox.charTyped(chr, modifiers)) {
                if (!Objects.equals(string, this.searchBox.getText())) {
                    this.search();
                }

                return true;
            } else {
                return false;
            }
        }
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 266) {
            if (this.switchToPreviousPage()) {
                return true;
            } else if (keyCode == 267 && this.switchToNextPage()) {
                return true;
            }
        }
        this.ignoreTypedCharacter = false;
        if (selectedTab.getType() != ItemGroup.Type.SEARCH) {
            if (this.client.options.chatKey.matchesKey(keyCode, scanCode)) {
                this.ignoreTypedCharacter = true;
                this.setSelectedTab(ItemGroups.getSearchGroup());
                return true;
            } else {
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
        } else {
            boolean bl = !this.isCreativeInventorySlot(this.focusedSlot) || this.focusedSlot.hasStack();
            boolean bl2 = InputUtil.fromKeyCode(keyCode, scanCode).toInt().isPresent();
            if (bl && bl2 && this.handleHotbarKeyPressed(keyCode, scanCode)) {
                this.ignoreTypedCharacter = true;
                return true;
            } else {
                String string = this.searchBox.getText();
                if (this.searchBox.keyPressed(keyCode, scanCode, modifiers)) {
                    if (!Objects.equals(string, this.searchBox.getText())) {
                        this.search();
                    }

                    return true;
                } else {
                    return this.searchBox.isFocused() && this.searchBox.isVisible() && keyCode != 256 || super.keyPressed(keyCode, scanCode, modifiers);
                }
            }
        }
    }

    private boolean isGroupVisible(ItemGroup group) { // FROM FABRIC API FOR CREATIVE INVENTORY
        return group.shouldDisplay() && currentPage == this.getPage(group);
    }

    public int getPage(ItemGroup group) {
        if (JourneyCreativeGuiComponents.COMMON_GROUPS.contains(group)) {
            return currentPage;
        } else {
            return itemGroupPage.get(group);
        }
    }

    private boolean hasGroupForPage(int page) {
        return ItemGroups.getGroupsToDisplay().stream().anyMatch((itemGroup -> {
            return this.getPage(itemGroup) == page;
        }));
    }

    public boolean switchToPage(int page) {
        if (!this.hasGroupForPage(page)) {
            return false;
        } else if (currentPage == page) {
            return false;
        } else {
            currentPage = page;
            this.updateSelection();
            return true;
        }
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

    public List<ItemGroup> getItemGroupsOnPage(int page) {
        return ItemGroups.getGroupsToDisplay().stream().filter((itemGroup) -> {
            return this.getPage(itemGroup) == page;
        }).sorted(Comparator.comparing(ItemGroup::getRow).thenComparingInt(ItemGroup::getColumn)).sorted((a, b) -> {
            return Boolean.compare(a.isSpecial(), b.isSpecial());
        }).toList();
    }

    public boolean hasAdditionalPages() {
        return ItemGroups.getGroupsToDisplay().size() > (this.operatorTabEnabled ? 14 : 13);
    }

    public ItemGroup getSelectedItemGroup() {
        return selectedTab;
    }

    public boolean setSelectedItemGroup(ItemGroup itemGroup) {
        Objects.requireNonNull(itemGroup, "itemGroup");
        if (selectedTab == itemGroup) {
            return false;
        } else if (currentPage != this.getPage(itemGroup) && !this.switchToPage(this.getPage(itemGroup))) {
            return false;
        } else {
            this.setSelectedTab(itemGroup);
            return true;
        }
    }

    private void paginateGroups() {
        List<RegistryKey<ItemGroup>> vanillaGroups = List.of(ItemGroups.BUILDING_BLOCKS, ItemGroups.COLORED_BLOCKS, ItemGroups.NATURAL,
                ItemGroups.FUNCTIONAL, ItemGroups.REDSTONE, ItemGroups.HOTBAR, ItemGroups.SEARCH, ItemGroups.TOOLS, ItemGroups.COMBAT,
                ItemGroups.FOOD_AND_DRINK, ItemGroups.INGREDIENTS, ItemGroups.SPAWN_EGGS, ItemGroups.OPERATOR, ItemGroups.INVENTORY);
        int count = 0;
        Comparator<RegistryEntry.Reference<ItemGroup>> entryComparator = (e1, e2) -> {
            int displayCompare = Boolean.compare(((ItemGroup) e1.value()).shouldDisplay(), ((ItemGroup) e2.value()).shouldDisplay());
            return displayCompare != 0 ? -displayCompare : compareNamespaceFirst(e1.registryKey().getValue(), e2.registryKey().getValue());
        };
        List<RegistryEntry.Reference<ItemGroup>> sortedItemGroups = Registries.ITEM_GROUP.streamEntries().sorted(entryComparator).toList();
        Iterator<RegistryEntry.Reference<ItemGroup>> groupIterator = sortedItemGroups.iterator();

        while (groupIterator.hasNext()) {
            RegistryEntry.Reference<ItemGroup> reference = (RegistryEntry.Reference<ItemGroup>) groupIterator.next();
            ItemGroup itemGroup = (ItemGroup) reference.value();
            if (vanillaGroups.contains(reference.registryKey())) {
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

    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        this.ignoreTypedCharacter = false;
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    private void search() {
        ((JourneyScreenHandler) this.handler).itemList.clear();
        this.searchResultTags.clear();
        String searchString = this.searchBox.getText();
        if (searchString.isEmpty()) {
            Collection<ItemStack> filteredItems = filterUnlockedItems(selectedTab.getDisplayStacks());
            ((JourneyScreenHandler) this.handler).itemList.addAll(filteredItems);
        } else {
            ClientPlayNetworkHandler clientPlayNetworkHandler = this.client.getNetworkHandler();
            if (clientPlayNetworkHandler != null) {
                SearchManager searchManager = clientPlayNetworkHandler.getSearchManager();
                SearchProvider searchProvider;
                if (searchString.startsWith("#")) {
                    searchString = searchString.substring(1);
                    searchProvider = searchManager.getItemTagReloadFuture();
                    this.searchForTags(searchString);
                } else {
                    searchProvider = searchManager.getItemTooltipReloadFuture();
                }

                Collection<ItemStack> filteredItems = filterUnlockedItems((Collection<ItemStack>) searchProvider.findAll(searchString.toLowerCase(Locale.ROOT)));
                ((JourneyScreenHandler) this.handler).itemList.addAll(filteredItems);
            }
        }

        this.scrollPosition = 0.0F;
        ((JourneyScreenHandler) this.handler).scrollItems(0.0F);
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

        Stream var10000 = Registries.ITEM.streamTags().map(RegistryEntryList.Named::getTag).filter((tag) -> {
            return predicate.test(tag.id());
        });
        Set var10001 = this.searchResultTags;
        Objects.requireNonNull(var10001);
        var10000.forEach(var10001::add);
    }

    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        if (selectedTab.shouldRenderName()) {
            context.drawText(this.textRenderer, selectedTab.getDisplayName(), 8, 6, -12566464, false);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            double d = mouseX - (double) this.x;
            double e = mouseY - (double) this.y;
            Iterator var10 = ItemGroups.getGroupsToDisplay().iterator();

            while (var10.hasNext()) {
                ItemGroup itemGroup = (ItemGroup) var10.next();
                if (itemGroup.getType() == ItemGroup.Type.HOTBAR) continue;
                if (this.isClickInTab(itemGroup, d, e)) {
                    return true;
                }
            }

            if (selectedTab.getType() != ItemGroup.Type.INVENTORY && this.isClickInScrollbar(mouseX, mouseY)) {
                this.scrolling = this.hasScrollbar();
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            double d = mouseX - (double) this.x;
            double e = mouseY - (double) this.y;
            this.scrolling = false;
            Iterator var10 = ItemGroups.getGroupsToDisplay().iterator();

            while (var10.hasNext()) {
                ItemGroup itemGroup = (ItemGroup) var10.next();
                if (itemGroup.getType() == ItemGroup.Type.HOTBAR) continue;
                if (this.isClickInTab(itemGroup, d, e)) {
                    this.setSelectedTab(itemGroup);
                    return true;
                }
            }
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    private boolean hasScrollbar() {
        return selectedTab.hasScrollbar() && ((JourneyScreenHandler) this.handler).shouldShowScrollbar();
    }

    private Collection<ItemStack> filterUnlockedItems(Collection<ItemStack> unfilteredItems) {
        Collection<ItemStack> filtered = ItemStackSet.create();
        for (ItemStack itemStack : unfilteredItems) {
            if (PlayerClientUnlocksData.isUnlocked(itemStack)) {
                filtered.add(itemStack);
            }
        }

        return filtered;
    }

    private void setSelectedTab(ItemGroup group) {
        if (!this.isGroupVisible(group)) {
            return;
        }
        ItemGroup itemGroup = selectedTab;
        selectedTab = group;
        this.cursorDragSlots.clear();
        ((JourneyScreenHandler) this.handler).itemList.clear();
        this.endTouchDrag();
        int i;
        int j;
        if (selectedTab.getType() == ItemGroup.Type.CATEGORY) {
            Collection<ItemStack> filteredItems = filterUnlockedItems(selectedTab.getDisplayStacks());
            ((JourneyScreenHandler) this.handler).itemList.addAll(filteredItems);
        }

        if (selectedTab.getType() == ItemGroup.Type.INVENTORY) {
            ScreenHandler screenHandler = this.client.player.playerScreenHandler;
            if (this.slots == null) {
                this.slots = ImmutableList.copyOf(((JourneyScreenHandler)this.handler).slots);
            }

            ((JourneyScreenHandler) this.handler).slots.clear();

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
                ((JourneyScreenHandler) this.handler).slots.add(slot);
            }

            this.deleteItemSlot = new Slot(this.handler.trashcanInventory, 0, 173, 112);
            ((JourneyScreenHandler) this.handler).slots.add(this.deleteItemSlot);
        } else if (itemGroup.getType() == ItemGroup.Type.INVENTORY) {
            ((JourneyScreenHandler) this.handler).slots.clear();
            ((JourneyScreenHandler) this.handler).slots.addAll(this.slots);
            this.slots = null;
        }

        if (selectedTab.getType() == ItemGroup.Type.SEARCH) {
            this.searchBox.setVisible(true);
            this.searchBox.setFocusUnlocked(false);
            this.searchBox.setFocused(true);
            if (itemGroup != group) {
                this.searchBox.setText("");
            }

            this.search();
        } else {
            this.searchBox.setVisible(false);
            this.searchBox.setFocusUnlocked(true);
            this.searchBox.setFocused(false);
            this.searchBox.setText("");
        }

        this.scrollPosition = 0.0F;
        ((JourneyScreenHandler) this.handler).scrollItems(0.0F);
    }

    private void updateSelection() { //FROM Fabric API CreativeInventoryScreenMixin.class
        if (!this.isGroupVisible(selectedTab)) {
            ItemGroups.getGroups().stream().filter(this::isGroupVisible).min((a, b) -> {
                return Boolean.compare(a.isSpecial(), b.isSpecial());
            }).ifPresent(this::setSelectedTab);
        }
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
            return true;
        } else if (!this.hasScrollbar()) {
            return false;
        } else {
            this.scrollPosition = ((JourneyScreenHandler) this.handler).getScrollPosition(this.scrollPosition, verticalAmount);
            ((JourneyScreenHandler) this.handler).scrollItems(this.scrollPosition);
            return true;
        }
    }

    protected boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button) {
        boolean bl = mouseX < (double)left || mouseY < (double)top || mouseX >= (double)(left + this.backgroundWidth) || mouseY >= (double)(top + this.backgroundHeight);
        this.lastClickOutsideBounds = bl && !this.isClickInTab(selectedTab, mouseX, mouseY);
        return this.lastClickOutsideBounds;
    }

    protected boolean isClickInScrollbar(double mouseX, double mouseY) {
        int i = this.x;
        int j = this.y;
        int k = i + 175;
        int l = j + 18;
        int m = k + 14;
        int n = l + 112;
        return mouseX >= (double) k && mouseY >= (double) l && mouseX < (double) m && mouseY < (double) n;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.scrolling) {
            int i = this.y + 18;
            int j = i + 112;
            this.scrollPosition = ((float) mouseY - (float) i - 7.5F) / ((float) (j - i) - 15.0F);
            this.scrollPosition = MathHelper.clamp(this.scrollPosition, 0.0F, 1.0F);
            ((JourneyScreenHandler) this.handler).scrollItems(this.scrollPosition);
            return true;
        } else {
            return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
        this.statusEffectsDisplay.drawStatusEffects(context, mouseX, mouseY, deltaTicks);
        Iterator var5 = ItemGroups.getGroupsToDisplay().iterator();

        while (var5.hasNext()) {
            ItemGroup itemGroup = (ItemGroup)var5.next();
            if (itemGroup.getType() == ItemGroup.Type.HOTBAR) continue;
            if (this.renderTabTooltipIfHovered(context, itemGroup, mouseX, mouseY)) {
                break;
            }
        }

        if (this.deleteItemSlot != null &&
                selectedTab.getType() == ItemGroup.Type.INVENTORY &&
                this.isPointWithinBounds(this.deleteItemSlot.x, this.deleteItemSlot.y, 16, 16, (double) mouseX, (double) mouseY)) {
            context.drawTooltip(this.textRenderer, DELETE_ITEM_SLOT_TEXT, mouseX, mouseY);
        }

        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    public boolean showStatusEffects() {
        return this.statusEffectsDisplay.shouldHideStatusEffectHud();
    }

    public List<Text> getTooltipFromItem(ItemStack stack) {
        boolean bl = this.focusedSlot != null && this.focusedSlot instanceof JourneyInventoryScreen.LockableSlot;
        boolean bl2 = selectedTab.getType() == ItemGroup.Type.CATEGORY;
        boolean bl3 = selectedTab.getType() == ItemGroup.Type.SEARCH;
        TooltipType.Default default_ = this.client.options.advancedItemTooltips ? TooltipType.Default.ADVANCED : TooltipType.Default.BASIC;
        TooltipType tooltipType = bl ? default_.withCreative() : default_;
        List<Text> list = stack.getTooltip(Item.TooltipContext.create(this.client.world), this.client.player, tooltipType);
        if (list.isEmpty()) {
            return list;
        } else if (bl2 && bl) {
            return list;
        } else {
            List<Text> list2 = Lists.newArrayList(list);
            if (bl3 && bl) {
                this.searchResultTags.forEach((tagKey) -> {
                    if (stack.isIn(tagKey)) {
                        list2.add(1, Text.literal("#" + String.valueOf(tagKey.id())).formatted(Formatting.DARK_PURPLE));
                    }

                });
            }

            int i = 1;
            Iterator var10 = ItemGroups.getGroupsToDisplay().iterator();

            while(var10.hasNext()) {
                ItemGroup itemGroup = (ItemGroup) var10.next();
                if (itemGroup.getType() != ItemGroup.Type.SEARCH && itemGroup.contains(stack)) {
                    list2.add(i++, itemGroup.getDisplayName().copy().formatted(Formatting.BLUE));
                }
            }

            return list2;
        }
    }

    protected void drawBackground(DrawContext context, float deltaTicks, int mouseX, int mouseY) {
        Iterator var5 = ItemGroups.getGroupsToDisplay().iterator();

        while(var5.hasNext()) {
            ItemGroup itemGroup = (ItemGroup) var5.next();
            if (itemGroup.getType() == ItemGroup.Type.HOTBAR) continue;
            if (itemGroup != selectedTab) {
                this.renderTabIcon(context, itemGroup);
            }
        }

        if (selectedTab.getType() == ItemGroup.Type.INVENTORY) {
            context.drawTexture(RenderLayer::getGuiTextured, JourneyInventoryScreen.JOURNEY_INVENTORY_TEXTURE, this.x, this.y, 0.0F, 0.0F, this.backgroundWidth, this.backgroundHeight, 256, 256);
        } else {
            context.drawTexture(RenderLayer::getGuiTextured, selectedTab.getTexture(), this.x, this.y, 0.0F, 0.0F, this.backgroundWidth, this.backgroundHeight, 256, 256);
        }
        this.searchBox.render(context, mouseX, mouseY, deltaTicks);
        int i = this.x + 175;
        int j = this.y + 18;
        int k = j + 112;
        if (selectedTab.hasScrollbar()) {
            Identifier identifier = this.hasScrollbar() ? SCROLLER_TEXTURE : SCROLLER_DISABLED_TEXTURE;
            context.drawGuiTexture(RenderLayer::getGuiTextured, identifier, i, j + (int)((float)(k - j - 17) * this.scrollPosition), 12, 15);
        }

        this.renderTabIcon(context, selectedTab);
        if (selectedTab.getType() == ItemGroup.Type.INVENTORY) {
            InventoryScreen.drawEntity(context, this.x + 73, this.y + 6, this.x + 105, this.y + 49, 20, 0.0625F, (float)mouseX, (float)mouseY, this.client.player);
        }
    }

    private int getTabX(ItemGroup group) {
        int i = group.getColumn();
        int k = 27 * i;
        if (group.isSpecial()) {
            k = this.backgroundWidth - 27 * (7 - i) + 1;
        }

        return k;
    }

    private int getTabY(ItemGroup group) {
        int i = 0;
        if (group.getRow() == ItemGroup.Row.TOP) {
            i -= 32;
        } else {
            i += this.backgroundHeight;
        }

        return i;
    }

    protected boolean isClickInTab(ItemGroup group, double mouseX, double mouseY) {
        if (!this.isGroupVisible(group)) {
            return false;
        }
        int i = this.getTabX(group);
        int j = this.getTabY(group);
        return mouseX >= (double) i && mouseX <= (double) (i + 26) && mouseY >= (double) j && mouseY <= (double) (j + 32);
    }

    protected boolean renderTabTooltipIfHovered(DrawContext context, ItemGroup group, int mouseX, int mouseY) {
        if (!this.isGroupVisible(group)) {
            return false;
        }
        int i = this.getTabX(group);
        int j = this.getTabY(group);
        if (this.isPointWithinBounds(i + 3, j + 3, 21, 27, (double) mouseX, (double) mouseY)) {
            context.drawTooltip(this.textRenderer, group.getDisplayName(), mouseX, mouseY);
            return true;
        } else {
            return false;
        }
    }

    protected void renderTabIcon(DrawContext context, ItemGroup group) {
        if (!this.isGroupVisible(group)) {
            return;
        }
        boolean bl = group == selectedTab;
        boolean bl2 = group.getRow() == ItemGroup.Row.TOP;
        int i = group.getColumn();
        int j = this.x + this.getTabX(group);
        int k = this.y - (bl2 ? 28 : -(this.backgroundHeight - 4));
        Identifier[] identifiers;
        if (bl2) {
            identifiers = bl ? TAB_TOP_SELECTED_TEXTURES : TAB_TOP_UNSELECTED_TEXTURES;
        } else {
            identifiers = bl ? TAB_BOTTOM_SELECTED_TEXTURES : TAB_BOTTOM_UNSELECTED_TEXTURES;
        }

        context.drawGuiTexture(RenderLayer::getGuiTextured, identifiers[MathHelper.clamp(i, 0, identifiers.length)], j, k, 26, 32);
        int l = j + 13 - 8;
        int m = k + 16 - 8 + (bl2 ? 1 : -1);
        context.drawItem(group.getIcon(), l, m);
    }

    public boolean isInventoryTabSelected() {
        return selectedTab.getType() == ItemGroup.Type.INVENTORY;
    }

    public static void onHotbarKeyPress(MinecraftClient client, int index, boolean restore, boolean save) {
        ClientPlayerEntity clientPlayerEntity = client.player;
        DynamicRegistryManager dynamicRegistryManager = clientPlayerEntity.getWorld().getRegistryManager();
        HotbarStorage hotbarStorage = client.getCreativeHotbarStorage();
        HotbarStorageEntry hotbarStorageEntry = hotbarStorage.getSavedHotbar(index);
        if (restore) {
            List<ItemStack> list = hotbarStorageEntry.deserialize(dynamicRegistryManager);

            for(int i = 0; i < PlayerInventory.getHotbarSize(); ++i) {
                ItemStack itemStack = (ItemStack)list.get(i);
                clientPlayerEntity.getInventory().setStack(i, itemStack);
                JourneyClientNetworking.clickJourneyStack(itemStack, 36 + i);
            }

            clientPlayerEntity.playerScreenHandler.sendContentUpdates();
        } else if (save) {
            hotbarStorageEntry.serialize(clientPlayerEntity.getInventory(), dynamicRegistryManager);
            Text text = client.options.hotbarKeys[index].getBoundKeyLocalizedText();
            Text text2 = client.options.loadToolbarActivatorKey.getBoundKeyLocalizedText();
            Text text3 = Text.translatable("inventory.hotbarSaved", new Object[]{text2, text});
            client.inGameHud.setOverlayMessage(text3, false);
            client.getNarratorManager().narrate(text3);
            hotbarStorage.save();
        }

    }

    public Slot getDeleteItemSlot() {
        return this.deleteItemSlot;
    }

    @Environment(EnvType.CLIENT)
    public static class JourneyScreenHandler extends ScreenHandler {
        public final DefaultedList<ItemStack> itemList = DefaultedList.of();
        private final ScreenHandler parent;
        private TrashcanInventory trashcanInventory;

        public JourneyScreenHandler(PlayerEntity player) {
            super((ScreenHandlerType) null, 0);
            this.parent = player.playerScreenHandler;
            PlayerInventory playerInventory = player.getInventory();

            for(int i = 0; i < 5; ++i) {
                for(int j = 0; j < 9; ++j) {
                    this.addSlot(new JourneyInventoryScreen.LockableSlot(JourneyInventoryScreen.INVENTORY, i * 9 + j, 9 + j * 18, 18 + i * 18));
                }
            }

            this.addPlayerHotbarSlots(playerInventory, 9, 112);
            this.scrollItems(0.0F);
            this.trashcanInventory = TrashcanServerStorage.get(player);
        }

        public boolean canUse(PlayerEntity player) {
            return true;
        }

        protected int getOverflowRows() {
            return MathHelper.ceilDiv(this.itemList.size(), 9) - 5;
        }

        protected int getRow(float scroll) {
            return Math.max((int)((double)(scroll * (float)this.getOverflowRows()) + 0.5), 0);
        }

        protected float getScrollPosition(int row) {
            return MathHelper.clamp((float)row / (float)this.getOverflowRows(), 0.0F, 1.0F);
        }

        protected float getScrollPosition(float current, double amount) {
            return MathHelper.clamp(current - (float)(amount / (double)this.getOverflowRows()), 0.0F, 1.0F);
        }

        public void scrollItems(float position) {
            int i = this.getRow(position);

            for(int j = 0; j < 5; ++j) {
                for(int k = 0; k < 9; ++k) {
                    int l = k + (j + i) * 9;
                    if (l >= 0 && l < this.itemList.size()) {
                        JourneyInventoryScreen.INVENTORY.setStack(k + j * 9, (ItemStack)this.itemList.get(l));
                    } else {
                        JourneyInventoryScreen.INVENTORY.setStack(k + j * 9, ItemStack.EMPTY);
                    }
                }
            }
        }

        public boolean shouldShowScrollbar() {
            return this.itemList.size() > 45;
        }

        public ItemStack quickMove(PlayerEntity player, int slot) {
            if (slot >= this.slots.size() - 9 && slot < this.slots.size()) {
                Slot slot2 = (Slot)this.slots.get(slot);
                if (slot2 != null && slot2.hasStack()) {
                    slot2.setStack(ItemStack.EMPTY);
                }
            }

            return ItemStack.EMPTY;
        }

        public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
            return slot.inventory != JourneyInventoryScreen.INVENTORY;
        }

        public boolean canInsertIntoSlot(Slot slot) {
            return slot.inventory != JourneyInventoryScreen.INVENTORY;
        }

        public ItemStack getCursorStack() {
            return this.parent.getCursorStack();
        }

        public void setCursorStack(ItemStack stack) {
            this.parent.setCursorStack(stack);
        }

        protected boolean insertItem(ItemStack stack, int startIndex, int endIndex, boolean fromLast) {
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
                    itemStack = slot.getStack();
                    if (!itemStack.isEmpty() && ItemStack.areItemsAndComponentsEqual(stack, itemStack)) {
                        j = itemStack.getCount() + stack.getCount();
                        int k = slot.getMaxItemCount(itemStack);
                        if (j <= k) {
                            stack.setCount(0);
                            itemStack.setCount(j);
                            slot.markDirty();
                            bl = true;
                        } else if (itemStack.getCount() < k) {
                            stack.decrement(k - itemStack.getCount());
                            itemStack.setCount(k);
                            slot.markDirty();
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
                    itemStack = slot.getStack();
                    if (itemStack.isEmpty() && slot.canInsert(stack)) {
                        j = slot.getMaxItemCount(stack);
                        slot.setStack(stack.split(Math.min(stack.getCount(), j)));
                        slot.markDirty();
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
        public void onClosed(PlayerEntity player) {
            super.onClosed(player);

            ItemStack cursorStack = this.getCursorStack();

            if (!cursorStack.isEmpty()) {
                player.dropItem(cursorStack, true);
                JourneyClientNetworking.dropJourneyStack(cursorStack, (ClientPlayerEntity) player);
                this.setCursorStack(ItemStack.EMPTY);
            }
        }

    }

    @Environment(EnvType.CLIENT)
    private static class JourneySlot extends Slot {
        final Slot slot;

        public JourneySlot(Slot slot, int invSlot, int x, int y) {
            super(slot.inventory, invSlot, x, y);
            this.slot = slot;
        }

        public void onTakeItem(PlayerEntity player, ItemStack stack) {
            this.slot.onTakeItem(player, stack);
        }

        public boolean canInsert(ItemStack stack) {
            return this.slot.canInsert(stack);
        }

        public ItemStack getStack() {
            return this.slot.getStack();
        }

        public boolean hasStack() {
            return this.slot.hasStack();
        }

        public void setStack(ItemStack stack, ItemStack previousStack) {
            this.slot.setStack(stack, previousStack);
        }

        public void setStackNoCallbacks(ItemStack stack) {
            this.slot.setStackNoCallbacks(stack);
        }

        public void markDirty() {
            this.slot.markDirty();
        }

        public int getMaxItemCount() {
            return this.slot.getMaxItemCount();
        }

        public int getMaxItemCount(ItemStack stack) {
            return this.slot.getMaxItemCount(stack);
        }

        @Nullable
        public Identifier getBackgroundSprite() {
            return this.slot.getBackgroundSprite();
        }

        public ItemStack takeStack(int amount) {
            return this.slot.takeStack(amount);
        }

        public boolean isEnabled() {
            return this.slot.isEnabled();
        }

        public boolean canTakeItems(PlayerEntity playerEntity) {
            return this.slot.canTakeItems(playerEntity);
        }
    }

    @Environment(EnvType.CLIENT)
    static class LockableSlot extends Slot {
        public LockableSlot(Inventory inventory, int i, int j, int k) {
            super(inventory, i, j, k);
        }

        public boolean canTakeItems(PlayerEntity playerEntity) {
            ItemStack itemStack = this.getStack();
            if (super.canTakeItems(playerEntity) && !itemStack.isEmpty()) {
                return itemStack.isItemEnabled(playerEntity.getWorld().getEnabledFeatures()) && !itemStack.contains(DataComponentTypes.CREATIVE_SLOT_LOCK);
            } else {
                return itemStack.isEmpty();
            }
        }
    }
}

