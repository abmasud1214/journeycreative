package mod.journeycreative;

import mod.journeycreative.blocks.ModBlocks;
import mod.journeycreative.items.ModComponents;
import mod.journeycreative.items.ModItems;
import mod.journeycreative.items.ResearchCertificateItem;
import mod.journeycreative.networking.JourneyNetworking;
import mod.journeycreative.screen.ModScreens;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Journeycreative implements ModInitializer {
	public static final String MOD_ID = "journeycreative";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Fabric world!");
		JourneyNetworking.registerServerPackets();

		ModItems.initialize();
		ModComponents.initialize();
		ModBlocks.initialize();
		ModScreens.initialize();
	}
}