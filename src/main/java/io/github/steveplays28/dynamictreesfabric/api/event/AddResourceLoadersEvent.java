package io.github.steveplays28.dynamictreesfabric.api.event;

import io.github.steveplays28.dynamictreesfabric.api.resource.TreeResourceManager;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;

/**
 * @author Harley O'Connor
 */
public final class AddResourceLoadersEvent extends Event implements IModBusEvent {

	private final TreeResourceManager resourceManager;

	public AddResourceLoadersEvent(TreeResourceManager resourceManager) {
		this.resourceManager = resourceManager;
	}

	public TreeResourceManager getResourceManager() {
		return resourceManager;
	}

}
