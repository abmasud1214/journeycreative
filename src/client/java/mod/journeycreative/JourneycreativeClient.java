package mod.journeycreative;

import mod.journeycreative.blocks.EnderArchiveEntityRenderer;
import mod.journeycreative.blocks.ModBlocks;
import mod.journeycreative.blocks.ModModelLayers;
import mod.journeycreative.blocks.ResearchVesselEntityRenderer;
import mod.journeycreative.items.EnderArchiveBlockItem;
import mod.journeycreative.items.ModItems;
import mod.journeycreative.items.ResearchCertificateItem;
import mod.journeycreative.items.ResearchVesselBlockItem;
import mod.journeycreative.keybinds.ModKeyBindings;
import mod.journeycreative.networking.JourneyClientNetworking;
import mod.journeycreative.screen.ModScreensClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

public class JourneycreativeClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		JourneyClientNetworking.RegisterClientPackets();

		ItemTooltipCallback.EVENT.register(((itemStack, tooltipContext, tooltipType, list) -> {
			if (itemStack.isOf(ModItems.RESEARCH_CERTIFICATE)) {
				ResearchCertificateItem.appendTooltip(itemStack, tooltipContext, list, tooltipType);
			} else if (itemStack.isOf(ModBlocks.RESEARCH_VESSEL_BLOCK_ITEM)) {
				ResearchVesselBlockItem.appendTooltip(itemStack, tooltipContext, list, tooltipType);
			} else if (itemStack.isOf(ModBlocks.ENDER_ARCHIVE_BLOCK_ITEM)) {
				EnderArchiveBlockItem.appendTooltip(itemStack, tooltipContext, list, tooltipType);
			}
		}));

		ModScreensClient.initialize();
		ModModelLayers.initialize();
		ModKeyBindings.register();

		BlockEntityRendererFactories.register(ModBlocks.RESEARCH_VESSEL_BLOCK_ENTITY, ResearchVesselEntityRenderer::new);
		BlockEntityRendererFactories.register(ModBlocks.ENDER_ARCHIVE_BLOCK_ENTITY, EnderArchiveEntityRenderer::new);
	}
}