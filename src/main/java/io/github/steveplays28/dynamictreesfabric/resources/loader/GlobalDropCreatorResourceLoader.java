package io.github.steveplays28.dynamictreesfabric.resources.loader;

import io.github.steveplays28.dynamictreesfabric.api.resource.DTResource;
import io.github.steveplays28.dynamictreesfabric.api.resource.ResourceAccessor;
import io.github.steveplays28.dynamictreesfabric.api.resource.loading.AbstractResourceLoader;
import io.github.steveplays28.dynamictreesfabric.api.resource.loading.ApplicationException;
import io.github.steveplays28.dynamictreesfabric.api.resource.loading.preparation.JsonResourcePreparer;
import io.github.steveplays28.dynamictreesfabric.deserialisation.JsonDeserialisers;
import io.github.steveplays28.dynamictreesfabric.systems.dropcreators.GlobalDropCreators;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static io.github.steveplays28.dynamictreesfabric.deserialisation.JsonHelper.throwIfNotJsonObject;

/**
 * @author Harley O'Connor
 */
public final class GlobalDropCreatorResourceLoader extends AbstractResourceLoader<JsonElement> {

    private static final Logger LOGGER = LogManager.getLogger();

    public GlobalDropCreatorResourceLoader() {
        super(new JsonResourcePreparer("drop_creators/global"));
    }

    @Override
    public void applyOnReload(ResourceAccessor<JsonElement> resourceAccessor, ResourceManager resourceManager) {
        resourceAccessor.forEach(this::tryReadEntry);
    }

    private void tryReadEntry(DTResource<JsonElement> resource) {
        try {
            this.readEntry(resource);
        } catch (ApplicationException e) {
            LOGGER.error("Error loading global drop creator \"{}\": {}",
                    resource.getLocation(), e.getMessage());
        }
    }

    private void readEntry(DTResource<JsonElement> resource) throws ApplicationException {
        throwIfNotJsonObject(resource.getResource(), () -> new ApplicationException("Root element is not a Json object."));
        this.deserialiseAndPutEntry(resource.getLocation(), resource.getResource().getAsJsonObject());
    }

    private void deserialiseAndPutEntry(Identifier name, JsonObject json) {
        JsonDeserialisers.CONFIGURED_DROP_CREATOR.deserialise(json)
                .ifSuccessOrElse(
                        result -> GlobalDropCreators.put(name, result),
                        error -> LOGGER.error("Error loading global drop creator \"{}\": {}", name, error),
                        warning -> LOGGER.warn("Warning whilst loading global drop creator \"{}\": {}", name, warning)
                );
    }
}
