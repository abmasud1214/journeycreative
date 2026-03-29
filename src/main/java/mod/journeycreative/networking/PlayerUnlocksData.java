package mod.journeycreative.networking;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import mod.journeycreative.Journeycreative;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlayerUnlocksData {
    private ImmutableSet<ItemStack> unlockedItemKeys;

    public PlayerUnlocksData() {
        unlockedItemKeys = ImmutableSet.of();
    }

    public PlayerUnlocksData(ImmutableSet<ItemStack> unlockedItemKeys) {
        this.unlockedItemKeys = unlockedItemKeys;
    }

    public ImmutableSet<ItemStack> getUnlockedItemKeys() {
        return unlockedItemKeys;
    }

    public void setUnlockedItemKeys(ImmutableSet<ItemStack> itemKeys) {
        unlockedItemKeys = itemKeys;
    }

    public boolean unlockItem(ItemStack item) {
        if (isUnlocked(item, false)) { // We don't check the gamerule so that the item can be unlocked regardless
            return false;
        } else {
            ItemStack normalized = normalizeForUnlocks(item);
            this.unlockedItemKeys = ImmutableSet.<ItemStack>builder().addAll(this.unlockedItemKeys).add(normalized).build();
            return true;
        }
    }

    public boolean isUnlocked(ItemStack item, boolean researchItems) {
        ItemStack normalized = normalizeForUnlocks(item);
        AtomicBoolean equal = new AtomicBoolean(false);

        unlockedItemKeys.stream().iterator().forEachRemaining(stack -> {
            if (ItemStack.isSameItemSameComponents(stack, normalized)) equal.set(true);
        });

        if (researchItems) {
            ItemStack researchVessel = normalizeForUnlocks(new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(Journeycreative.MOD_ID, "research_vessel")), 1));
            ItemStack enderArchive = normalizeForUnlocks(new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(Journeycreative.MOD_ID, "ender_archive")), 1));
            if (ItemStack.isSameItemSameComponents(researchVessel, normalized)) equal.set(true);
            if (ItemStack.isSameItemSameComponents(enderArchive, normalized)) equal.set(true);
        }

        return equal.get();
    }

    private static ItemStack normalizeForUnlocks(ItemStack stack) {
        if (stack.isEmpty()) return ItemStack.EMPTY;

        ItemStack copy = stack.copy();
        copy.setCount(1);

        Set<DataComponentType<?>> keepComponents = Set.of(
                DataComponents.POTION_CONTENTS,
                DataComponents.POTION_DURATION_SCALE,
                DataComponents.STORED_ENCHANTMENTS,
                DataComponents.INSTRUMENT,
                DataComponents.FIREWORKS,
                DataComponents.SUSPICIOUS_STEW_EFFECTS,
                DataComponents.OMINOUS_BOTTLE_AMPLIFIER
        );

        copy.getComponents().forEach(component -> {
            DataComponentType<?> componentType = component.type();
            if (!keepComponents.contains(componentType)) {
                copy.remove(componentType);
            }
        });

        return copy;
    }



    public static final Codec<PlayerUnlocksData> PLAYER_UNLOCKS_CODEC = ItemStack.CODEC
            .listOf()
            .xmap(
                    ImmutableSet::copyOf,
                    ImmutableList::copyOf
            )
            .fieldOf("unlockedItems")
            .codec()
            .xmap(
                    PlayerUnlocksData::new,
                    PlayerUnlocksData::getUnlockedItemKeys
            );
}
