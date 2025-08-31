package mod.journeycreative;

import mod.journeycreative.blocks.EnderArchiveEntityRenderer;
import mod.journeycreative.blocks.ModBlocks;
import mod.journeycreative.blocks.ModModelLayers;
import mod.journeycreative.blocks.ResearchVesselEntityRenderer;
import mod.journeycreative.items.ModItems;
import mod.journeycreative.items.ResearchCertificateItem;
import mod.journeycreative.networking.JourneyClientNetworking;
import mod.journeycreative.screen.ModScreensClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;

public class JourneycreativeClient implements ClientModInitializer {



	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		JourneyClientNetworking.RegisterClientPackets();

		ItemTooltipCallback.EVENT.register(((itemStack, tooltipContext, tooltipType, list) -> {
			if (!itemStack.isOf(ModItems.RESEARCH_CERTIFICATE)) {
				return;
			}
			ResearchCertificateItem.appendTooltip(itemStack, tooltipContext, list, tooltipType);
		}));

		ModScreensClient.initialize();
		ModModelLayers.initialize();

		BlockEntityRendererFactories.register(ModBlocks.RESEARCH_VESSEL_BLOCK_ENTITY, ResearchVesselEntityRenderer::new);
		BlockEntityRendererFactories.register(ModBlocks.ENDER_ARCHIVE_BLOCK_ENTITY, EnderArchiveEntityRenderer::new);
	}
}