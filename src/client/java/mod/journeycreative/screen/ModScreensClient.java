package mod.journeycreative.screen;

import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class ModScreensClient {
    public static void initialize() {
        HandledScreens.register(ModScreens.RESEARCH_VESSEL_SCREEN_HANDLER, ResearchVesselScreen::new);
    }
}
