package mod.journeycreative;

import mod.journeycreative.networking.JourneyClientNetworking;
import mod.journeycreative.networking.PlayerUnlocksData;
import net.fabricmc.api.ClientModInitializer;

public class JourneycreativeClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		JourneyClientNetworking.RegisterClientPackets();
	}
}