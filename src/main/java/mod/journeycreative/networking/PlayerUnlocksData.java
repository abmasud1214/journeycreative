package mod.journeycreative.networking;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import mod.journeycreative.Journeycreative;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

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
            if (ItemStack.areItemsAndComponentsEqual(stack, normalized)) equal.set(true);
        });

        if (researchItems) {
            ItemStack researchVessel = normalizeForUnlocks(new ItemStack(Registries.ITEM.get(Identifier.of(Journeycreative.MOD_ID, "research_vessel")), 1));
            ItemStack enderArchive = normalizeForUnlocks(new ItemStack(Registries.ITEM.get(Identifier.of(Journeycreative.MOD_ID, "ender_archive")), 1));
            if (ItemStack.areItemsAndComponentsEqual(researchVessel, normalized)) equal.set(true);
            if (ItemStack.areItemsAndComponentsEqual(enderArchive, normalized)) equal.set(true);
        }

        return equal.get();
    }

    private static ItemStack normalizeForUnlocks(ItemStack stack) {
        if (stack.isEmpty()) return ItemStack.EMPTY;

        ItemStack copy = stack.copy();
        copy.setCount(1);

        Set<ComponentType<?>> keepComponents = Set.of(
                DataComponentTypes.POTION_CONTENTS,
                DataComponentTypes.POTION_DURATION_SCALE,
                DataComponentTypes.STORED_ENCHANTMENTS,
                DataComponentTypes.INSTRUMENT,
                DataComponentTypes.FIREWORKS,
                DataComponentTypes.SUSPICIOUS_STEW_EFFECTS,
                DataComponentTypes.OMINOUS_BOTTLE_AMPLIFIER
        );

        copy.getComponents().forEach(component -> {
            ComponentType<?> componentType = component.type();
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
