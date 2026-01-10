package mod.journeycreative;

import mod.journeycreative.blocks.EnderArchiveEntityRenderer;
import mod.journeycreative.blocks.ModBlocks;
import mod.journeycreative.blocks.ModModelLayers;
import mod.journeycreative.blocks.ResearchVesselEntityRenderer;
import mod.journeycreative.keybinds.ModKeyBindings;
import mod.journeycreative.networking.JourneyClientNetworking;
import mod.journeycreative.screen.ModScreensClient;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

public class JourneycreativeClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		JourneyClientNetworking.RegisterClientPackets();

		ModScreensClient.initialize();
		ModModelLayers.initialize();
		ModKeyBindings.register();

		BlockEntityRendererFactories.register(ModBlocks.RESEARCH_VESSEL_BLOCK_ENTITY, ResearchVesselEntityRenderer::new);
		BlockEntityRendererFactories.register(ModBlocks.ENDER_ARCHIVE_BLOCK_ENTITY, EnderArchiveEntityRenderer::new);
	}
}