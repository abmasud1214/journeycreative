package mod.journeycreative.screen;

import mod.journeycreative.Journeycreative;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.MenuType;

public class ModScreens {
    public static final MenuType<ResearchVesselScreenHandler> RESEARCH_VESSEL_SCREEN_HANDLER =
            Registry.register(BuiltInRegistries.MENU, Identifier.fromNamespaceAndPath(Journeycreative.MOD_ID, "research_vessel"),
                    new MenuType<>(ResearchVesselScreenHandler::new, FeatureFlagSet.of()));

    public static final MenuType<EnderArchiveScreenHandler> ENDER_ARCHIVE_SCREEN_HANDLER =
            Registry.register(BuiltInRegistries.MENU, Identifier.fromNamespaceAndPath(Journeycreative.MOD_ID, "ender_archive"),
                    new MenuType<>(EnderArchiveScreenHandler::new, FeatureFlagSet.of()));

    public static void initialize() {

    }
}
