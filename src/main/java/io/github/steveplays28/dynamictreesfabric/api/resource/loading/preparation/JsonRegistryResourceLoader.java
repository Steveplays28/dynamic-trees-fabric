package io.github.steveplays28.dynamictreesfabric.api.resource.loading.preparation;

import io.github.steveplays28.dynamictreesfabric.api.registry.RegistryEntry;
import io.github.steveplays28.dynamictreesfabric.api.registry.TypedRegistry;
import io.github.steveplays28.dynamictreesfabric.api.resource.DTResource;
import io.github.steveplays28.dynamictreesfabric.api.resource.ResourceAccessor;
import io.github.steveplays28.dynamictreesfabric.api.resource.loading.ApplicationException;
import io.github.steveplays28.dynamictreesfabric.api.resource.loading.StagedApplierResourceLoader;
import io.github.steveplays28.dynamictreesfabric.deserialisation.JsonHelper;
import io.github.steveplays28.dynamictreesfabric.deserialisation.JsonPropertyAppliers;
import io.github.steveplays28.dynamictreesfabric.trees.Resettable;
import io.github.steveplays28.dynamictreesfabric.util.IgnoreThrowable;
import io.github.steveplays28.dynamictreesfabric.util.JsonMapWrapper;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static io.github.steveplays28.dynamictreesfabric.deserialisation.JsonHelper.throwIfNotJsonObject;
import static io.github.steveplays28.dynamictreesfabric.deserialisation.JsonHelper.throwIfShouldNotLoad;

/**
 * @author Harley O'Connor
 */
public abstract class JsonRegistryResourceLoader<R extends RegistryEntry<R> & Resettable<R>> extends
        StagedApplierResourceLoader<JsonElement, R> {

    private static final Logger LOGGER = LogManager.getLogger();

    private final TypedRegistry<R> registry;
    private final String registryName;

    public JsonRegistryResourceLoader(TypedRegistry<R> registry, String folderName) {
        this(registry, folderName, folderName);
    }

    public JsonRegistryResourceLoader(TypedRegistry<R> registry, String folderName, String appliersIdentifier) {
        super(new JsonResourcePreparer(folderName), registry.getType(), JsonPropertyAppliers::new, appliersIdentifier);
        this.registry = registry;
        this.registryName = registry.getName();
    }

    //////////////////////////////
    // LOAD
    //////////////////////////////

    @Override
    public void applyOnLoad(ResourceAccessor<JsonElement> resourceAccessor, ResourceManager resourceManager) {
        resourceAccessor.forEach(resource -> {
            try {
                final JsonObject object = this.prepareJson(resource);
                final LoadData loadData = this.loadResourceOnLoad(resource.getLocation(), object);
                this.postLoadOnLoad(loadData, object);
            } catch (ApplicationException e) {
                this.logException(resource.getLocation(), e);
            } catch (IgnoreThrowable ignored) {}
        });
    }

    protected LoadData loadResourceOnLoad(Identifier name, JsonObject json) throws IgnoreThrowable {
        final LoadData loadData = new LoadData(this.loadNewResource(name, json));
        this.applyLoadAppliers(loadData, json);
        return loadData;
    }

    protected void postLoadOnLoad(LoadData loadData, JsonObject json) {
        this.applyCommonAppliers(loadData, json);
        this.postLoad(loadData);
    }

    protected void applyLoadAppliers(LoadData loadData, JsonObject json) {
        final Identifier resourceName = loadData.getResourceName();
        this.loadAppliers.applyAll(new JsonMapWrapper(json), loadData.resource)
                .forEachError(error -> this.logError(resourceName, error))
                .forEachWarning(warning -> this.logWarning(resourceName, warning));
    }

    //////////////////////////////
    // GATHER DATA
    //////////////////////////////

    @Override
    public void applyOnGatherData(ResourceAccessor<JsonElement> resourceAccessor,
                                  ResourceManager resourceManager) {
        resourceAccessor.forEach(resource -> {
            try {
                final JsonObject object = this.prepareJson(resource);
                final LoadData loadData = this.loadResource(resource.getLocation(), object);
                this.postLoadOnGatherData(loadData, object);
            } catch (ApplicationException e) {
                this.logException(resource.getLocation(), e);
            } catch (IgnoreThrowable ignored) {}
        });
    }

    private void postLoadOnGatherData(LoadData loadData, JsonObject json) {
        this.applyGatherDataAppliers(loadData, json);
        loadData.resource.setGenerateData(
                JsonHelper.getOrDefault(json, "generate_data", Boolean.class, true)
        );
        this.postLoad(loadData);
    }

    protected void applyGatherDataAppliers(LoadData loadData, JsonObject json) {
        final Identifier resourceName = loadData.getResourceName();
        this.gatherDataAppliers.applyAll(new JsonMapWrapper(json), loadData.resource)
                .forEachError(error -> this.logError(resourceName, error))
                .forEachWarning(warning -> this.logWarning(resourceName, warning));
    }

    //////////////////////////////
    // SETUP
    //////////////////////////////

    @Override
    public void applyOnSetup(ResourceAccessor<JsonElement> resourceAccessor, ResourceManager resourceManager) {
        resourceAccessor.forEach(resource -> {
            try {
                final JsonObject object = this.prepareJson(resource);
                final LoadData loadData = this.loadResourceOnSetup(resource.getLocation());
                this.applySetupAppliers(object, loadData);
            } catch (ApplicationException e) {
                this.logException(resource.getLocation(), e);
            } catch (IgnoreThrowable ignored) {}
        });
    }

    private LoadData loadResourceOnSetup(Identifier name) throws IgnoreThrowable {
        final LoadData loadData = new LoadData();
        loadData.wasAlreadyRegistered = this.registry.has(name);
        if (!loadData.wasAlreadyRegistered) {
            throw IgnoreThrowable.INSTANCE;
        }
        loadData.resource = this.registry.get(name);
        return loadData;
    }

    protected void applySetupAppliers(JsonObject json, LoadData loadData) {
        final Identifier resourceName = loadData.getResourceName();
        this.setupAppliers.applyAll(new JsonMapWrapper(json), loadData.resource)
                .forEachError(error -> this.logError(resourceName, error))
                .forEachWarning(warning -> this.logWarning(resourceName, warning));
    }

    //////////////////////////////
    // RELOAD
    //////////////////////////////

    @Override
    public void applyOnReload(ResourceAccessor<JsonElement> resourceAccessor, ResourceManager resourceManager) {
        this.registry.unlock();
        resourceAccessor.forEach(resource -> {
            try {
                final JsonObject object = this.prepareJson(resource);
                final LoadData loadData = this.loadResourceOnReload(resource.getLocation(), object);
                this.postLoadOnReload(loadData, object);
            } catch (ApplicationException e) {
                this.logException(resource.getLocation(), e);
            } catch (IgnoreThrowable ignored) {}
        });
        this.registry.lock();
    }

    private LoadData loadResourceOnReload(Identifier name, JsonObject json) throws IgnoreThrowable {
        final LoadData loadData = this.loadResource(name, json);
        if (loadData.wasAlreadyRegistered) {
            loadData.resource.reset().setPreReloadDefaults();
        } else {
            loadData.resource.setPreReloadDefaults();
        }
        return loadData;
    }

    protected void postLoadOnReload(LoadData loadData, JsonObject json) {
        this.applyReloadAppliers(loadData, json);
        this.applyCommonAppliers(loadData, json);
        loadData.resource.setPostReloadDefaults();
        this.postLoad(loadData);
    }

    private void applyReloadAppliers(LoadData loadData, JsonObject json) {
        final Identifier resourceName = loadData.getResourceName();
        this.reloadAppliers.applyAll(new JsonMapWrapper(json), loadData.resource)
                .forEachError(error -> this.logError(resourceName, error))
                .forEachWarning(warning -> this.logWarning(resourceName, warning));
    }

    //////////////////////////////
    // COMMON
    //////////////////////////////

    private JsonObject prepareJson(DTResource<JsonElement> resource)
            throws ApplicationException, IgnoreThrowable {
        throwIfNotJsonObject(resource.getResource(),
                () -> new ApplicationException("Root element is not a Json object."));
        final JsonObject object = TypedRegistry.putJsonRegistryName(resource.getResource().getAsJsonObject(),
                resource.getLocation());
        throwIfShouldNotLoad(object);
        return object;
    }

    private LoadData loadResource(Identifier name, JsonObject json) throws IgnoreThrowable {
        final LoadData loadData = new LoadData();
        loadData.wasAlreadyRegistered = this.registry.has(name);

        if (loadData.wasAlreadyRegistered) {
            loadData.resource = this.registry.get(name);
        } else {
            loadData.resource = this.loadNewResource(name, json);
        }
        return loadData;
    }

    private R loadNewResource(Identifier name, JsonObject json) throws IgnoreThrowable {
        final R resource = this.registry.getType(json, name).decode(json);
        // Stop loading this entry (error should have been logged already).
        if (resource == null) {
            throw IgnoreThrowable.INSTANCE;
        }
        return resource;
    }

    private void postLoad(LoadData loadData) {
        if (loadData.wasAlreadyRegistered) {
            LOGGER.debug("Loaded type \"{}\" data: {}.", this.registryName, loadData.resource.toReloadDataString());
        } else {
            this.registry.register(loadData.resource);
            LOGGER.debug("Loaded and registered type \"{}\": {}.", this.registryName, loadData.resource.toLoadDataString());
        }
    }

    private void logException(Identifier name, ApplicationException e) {
        LOGGER.error("Error whilst loading type \"" + this.registryName + "\" with name \"" + name + "\".", e);
    }

    protected void applyCommonAppliers(LoadData loadData, JsonObject json) {
        final Identifier resourceName = loadData.getResourceName();
        this.commonAppliers.applyAll(new JsonMapWrapper(json), loadData.resource)
                .forEachError(error -> this.logError(resourceName, error))
                .forEachWarning(warning -> this.logWarning(resourceName, warning));
    }

    protected void logError(Identifier name, String error) {
        LOGGER.error("Error whilst loading type \"" + this.registryName + "\" with name \"" + name + "\": {}", error);
    }

    protected void logWarning(Identifier name, String warning) {
        LOGGER.warn("Warning whilst loading type \"" + this.registryName + "\" with name \"" + name + "\": {}", warning);
    }

    public class LoadData {
        private R resource;
        private boolean wasAlreadyRegistered;

        public LoadData() {
        }

        public LoadData(R resource) {
            this.resource = resource;
        }

        public Identifier getResourceName() {
            return this.resource.getRegistryName();
        }

        public R getResource() {
            return resource;
        }

        public boolean wasAlreadyRegistered() {
            return wasAlreadyRegistered;
        }
    }

}
