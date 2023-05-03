package io.github.steveplays28.dynamictreesfabric.event.handlers;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.MissingMappingsEvent;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

@Mod.EventBusSubscriber
public class MissingEventHandler {

	///////////////////////////////////////////
	// MISSING REMAPPING
	///////////////////////////////////////////

	/**
	 * Here we'll simply remap the old "growingtrees" modid to the new modid for old blocks and items.
	 *
	 * @param event
	 */
	@SubscribeEvent
	public void missingMappings(MissingMappingsEvent event) {
		for (MissingMappingsEvent.Mapping<Block> missing : event.getAllMappings(ForgeRegistries.Keys.BLOCKS)) {
			Identifier resLoc = missing.getKey();
			String domain = resLoc.getNamespace();
			String path = resLoc.getPath();
			if (domain.equals("growingtrees")) {
				Logger.getLogger(io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.MOD_ID).log(Level.CONFIG, "Remapping Missing Block: " + path);
				Block mappedBlock = ForgeRegistries.BLOCKS.getValue(new Identifier(io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.MOD_ID, path));
				if (mappedBlock != Blocks.AIR) { // Air is what you get when you don't get what you're looking for.
					// assert mappedBlock != null;
					missing.remap(mappedBlock);
				}
			}
		}

		for (MissingMappingsEvent.Mapping<Item> missing : event.getAllMappings(ForgeRegistries.Keys.ITEMS)) {
			Identifier resLoc = missing.getKey();
			String domain = resLoc.getNamespace();
			String path = resLoc.getPath();
			if (domain.equals("growingtrees")) {
				Logger.getLogger(io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.MOD_ID).log(Level.CONFIG, "Remapping Missing Item: " + path);
				Item mappedItem = ForgeRegistries.ITEMS.getValue(new Identifier(io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.MOD_ID, path));
				if (mappedItem != null) { // Null is what you get when you don't get what you're looking for.
					missing.remap(mappedItem);
				}
			}
		}
	}

}
