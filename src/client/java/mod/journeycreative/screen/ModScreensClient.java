package mod.journeycreative.screen;

import net.minecraft.client.gui.screens.MenuScreens;

public class ModScreensClient {
    public static void initialize() {
        MenuScreens.register(ModScreens.RESEARCH_VESSEL_SCREEN_HANDLER, ResearchVesselScreen::new);
        MenuScreens.register(ModScreens.ENDER_ARCHIVE_SCREEN_HANDLER, EnderArchiveScreen::new);
    }
}
