package io.github.steveplays28.dynamictreesfabric.api.resource.loading.preparation;

import com.google.gson.JsonElement;
import io.github.steveplays28.dynamictreesfabric.api.resource.DTResource;
import io.github.steveplays28.dynamictreesfabric.api.resource.ResourceCollector;

import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Harley O'Connor
 */
public final class MultiJsonResourcePreparer extends
		AbstractResourcePreparer<Iterable<JsonElement>> {

	private static final String JSON_EXTENSION = ".json";

	public MultiJsonResourcePreparer(String folderName) {
		this(folderName, ResourceCollector.ordered());
	}

	public MultiJsonResourcePreparer(String folderName, ResourceCollector<Iterable<JsonElement>> resourceCollector) {
		super(folderName, JSON_EXTENSION, resourceCollector);
	}

	@Override
	protected void readAndPutResources(ResourceManager resourceManager, Map<Identifier, Resource> resourceMap) {
		resourceMap.forEach((location, resource) -> {
			final Identifier resourceName = this.getResourceName(location);
			this.tryReadAndPutResource(resourceManager, location, resourceName);
		});
	}

	private void tryReadAndPutResource(ResourceManager resourceManager, Identifier location,
	                                   Identifier resourceName) {
		try {
			this.readAndPutResource(resourceManager, location, resourceName);
		} catch (PreparationException | IOException e) {
			this.logError(location, e);
		}
	}

	@Override
	protected void readAndPutResource(Resource resource, Identifier resourceName)
			throws PreparationException, IOException {

	}

	private void readAndPutResource(ResourceManager resourceManager, Identifier location,
	                                Identifier resourceName) throws PreparationException, IOException {
		this.computeResourceListIfAbsent(resourceName)
				.addAll(this.collectResources(resourceManager, location));
	}

	private List<JsonElement> computeResourceListIfAbsent(Identifier resourceName) {
		return (List<JsonElement>)
				this.resourceCollector.computeIfAbsent(resourceName,
						() -> new DTResource<>(resourceName, new LinkedList<>())
				).getResource();
	}

	private List<JsonElement> collectResources(ResourceManager resourceManager, Identifier location)
			throws IOException, PreparationException {
		final List<JsonElement> resources = new LinkedList<>();
		for (Resource resource : resourceManager.getAllResources(location)) {
			resources.add(JsonResourcePreparer.readResource(resource));
		}
		return resources;
	}

}
