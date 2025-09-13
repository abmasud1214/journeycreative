package mod.journeycreative;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.minecraft.client.MinecraftClient;
import mod.journeycreative.blocks.ModBlocks;
import mod.journeycreative.items.ModComponents;
import mod.journeycreative.items.ModItems;
import mod.journeycreative.items.ResearchCertificateItem;
import mod.journeycreative.networking.JourneyNetworking;
import mod.journeycreative.screen.ModScreens;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

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
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public Identifier getFabricId() {
				return Identifier.of("journeycreative", "research");
			}

			@Override
			public void reload(ResourceManager manager) {
				for (Identifier id : manager.findResources("research", path -> path.toString().endsWith(".json")).keySet()) {
					try(InputStream stream = manager.getResource(id).get().getInputStream()) {
						Reader reader = new InputStreamReader(stream);

						if (id.getPath().endsWith("research_amount.json")) {
							ResearchConfig.loadResearchAmounts(reader);
							LOGGER.info("Loaded research requirements from {}", id);
						} else if (id.getPath().endsWith("research_prerequisite.json")) {
							ResearchConfig.loadResearchPrerequisites(reader);
							LOGGER.info("Loaded research prerequisites from {}", id);
						} else if (id.getPath().endsWith("research_prohibited.json")) {
							ResearchConfig.loadResearchProhibited(reader);
							LOGGER.info("Loaded prohibited research items from {}", id);
						} else if (id.getPath().endsWith("research_certificate_blocked.json")) {
							ResearchConfig.loadResearchBlocked(reader);
							LOGGER.info("Loaded blocked research items from {}", id);
						}
					} catch(Exception e) {
						LOGGER.error("Error occured while loading resource json" + id.toString(), e);
					}
				}
			}
		});

		LOGGER.info("Hello Fabric world!");
		JourneyNetworking.registerServerPackets();

		ModItems.initialize();
		ModComponents.initialize();
		ModBlocks.initialize();
		ModScreens.initialize();
	}

}