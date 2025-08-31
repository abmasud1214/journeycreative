package mod.journeycreative.screen;

import mod.journeycreative.Journeycreative;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class ModScreens {
    public static final ScreenHandlerType<ResearchVesselScreenHandler> RESEARCH_VESSEL_SCREEN_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, Identifier.of(Journeycreative.MOD_ID, "research_vessel"),
                    new ScreenHandlerType<>(ResearchVesselScreenHandler::new, FeatureSet.empty()));

    public static final ScreenHandlerType<EnderArchiveScreenHandler> ENDER_ARCHIVE_SCREEN_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, Identifier.of(Journeycreative.MOD_ID, "ender_archive"),
                    new ScreenHandlerType<>(EnderArchiveScreenHandler::new, FeatureSet.empty()));

    public static void initialize() {

    }
}
