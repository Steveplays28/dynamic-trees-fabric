package com.ferreusveritas.dynamictrees.resources;

import com.ferreusveritas.dynamictrees.api.resource.ResourceAccessor;
import com.ferreusveritas.dynamictrees.api.resource.ResourceManager;
import com.ferreusveritas.dynamictrees.api.resource.TreeResourcePack;
import com.ferreusveritas.dynamictrees.api.resource.loading.ApplierResourceLoader;
import com.ferreusveritas.dynamictrees.api.resource.loading.ResourceLoader;
import com.ferreusveritas.dynamictrees.resources.treepack.ModTreeResourcePack;
import com.ferreusveritas.dynamictrees.resources.treepack.RequiredFolderTreeResourcePack;
import com.ferreusveritas.dynamictrees.resources.treepack.TreePackLoader;
import com.ferreusveritas.dynamictrees.util.CommonCollectors;
import com.google.common.collect.Lists;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IResourcePack;
import net.minecraft.resources.SimpleResource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import net.minecraftforge.forgespi.locating.IModFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Harley O'Connor
 */
public final class TreesResourceManager implements IResourceManager, ResourceManager {

    private final List<TreeResourcePack> resourcePacks = Lists.newArrayList();
    private final List<ResourceLoader<?>> resourceLoaders = Lists.newArrayList();

    public TreesResourceManager() {
        addModTreePacks();
        addFlatTreePack();
        addTreePacks();
    }

    private void addModTreePacks() {
        // Register all mod tree packs. Gets the mods in an ordered list so that add-ons will come after DT.
        // This means that add-ons will take priority over DT.
        ModList.get().getMods().forEach(this::addModResourcePack);
    }

    private void addModResourcePack(ModInfo modInfo) {
        final IModFile modFile = ModList.get().getModFileById(modInfo.getModId()).getFile();
        if (modFile.getLocator().isValid(modFile)) {
            addModResourcePack(modFile);
        }
    }

    private void addModResourcePack(IModFile modFile) {
        final Path treesPath = modFile.getLocator()
                .findPath(modFile, Resources.TREES)
                .toAbsolutePath();

        if (Files.exists(treesPath)) {
            addPack(new ModTreeResourcePack(treesPath, modFile));
        }
    }

    private void addFlatTreePack() {
        final File mainTreeFolder = getFlatTreePackFolder();
        addPack(new RequiredFolderTreeResourcePack(mainTreeFolder.toPath().toAbsolutePath()));
    }

    private File getFlatTreePackFolder() {
        final File flatTreePackFolder = new File(Resources.TREES + "/");
        // Create the trees folder if it doesn't already exist, crash if failed.
        if (!flatTreePackFolder.exists() && !flatTreePackFolder.mkdir()) {
            throw new RuntimeException("Failed to create \"trees\" folder in your Minecraft directory.");
        }
        return flatTreePackFolder;
    }

    private void addTreePacks() {
        addPacks(TreePackLoader.loadTreePacks());
    }

    private void addPack(TreeResourcePack pack) {
        this.resourcePacks.add(pack);
    }

    private void addPacks(Collection<TreeResourcePack> packs) {
        this.resourcePacks.addAll(packs);
    }

    @Override
    public void addLoader(ResourceLoader<?> loader) {
        this.resourceLoaders.add(loader);
    }

    @Override
    public void addLoaders(ResourceLoader<?>... loaders) {
        this.resourceLoaders.addAll(Arrays.asList(loaders));
    }

    @Override
    public void addLoaderBefore(ResourceLoader<?> loader, ResourceLoader<?> existing) {
        this.resourceLoaders.add(this.resourceLoaders.indexOf(existing), loader);
    }

    @Override
    public void addLoaderAfter(ResourceLoader<?> loader, ResourceLoader<?> existing) {
        this.resourceLoaders.add(this.resourceLoaders.indexOf(existing) + 1, loader);
    }

    @Override
    public void registerAppliers() {
        this.resourceLoaders.stream()
                .filter(ApplierResourceLoader.class::isInstance)
                .map(ApplierResourceLoader.class::cast)
                .forEach(ApplierResourceLoader::registerAppliers);
    }

    @Override
    public void load() {
        this.resourceLoaders.forEach(loader -> loader.load(this).join());
    }

    @Override
    public void gatherData() {
        this.resourceLoaders.forEach(loader -> loader.gatherData(this).join());
    }

    @Override
    public void setup() {
        this.resourceLoaders.forEach(loader -> loader.setup(this).join());
    }

    @Override
    public CompletableFuture<?>[] prepareReload(final Executor gameExecutor, final Executor backgroundExecutor) {
        return this.resourceLoaders.stream()
                .map(loader -> loader.prepareReload(this))
                .toArray(CompletableFuture<?>[]::new);
    }

    /**
     * Reloads the given {@link CompletableFuture}s. These <b>must</b> be given in the same order as returned from
     * {@link #prepareReload(Executor, Executor)}.
     *
     * @param futures the futures returned from {@link #prepareReload(Executor, Executor)}
     */
    @Override
    public void reload(final CompletableFuture<?>[] futures) {
        reloadTreePacks();
        for (int i = 0; i < futures.length; i++) {
            this.reload(this.resourceLoaders.get(i), futures[i]);
        }
    }

    private void reloadTreePacks() {
        resourcePacks.removeIf(pack -> !pack.isRequired());
        addTreePacks();
    }

    @SuppressWarnings("unchecked")
    private <R> void reload(final ResourceLoader<R> loader, final CompletableFuture<?> future) {
        loader.reload((CompletableFuture<ResourceAccessor<R>>) future, this);
    }

    @Override
    public Set<String> getNamespaces() {
        return this.resourcePacks.stream()
                .map(treeResourcePack -> treeResourcePack.getNamespaces(null))
                .flatMap(Collection::stream)
                .collect(CommonCollectors.toLinkedSet());
    }

    @Override
    public IResource getResource(final ResourceLocation location) throws IOException {
        final List<IResource> resources = this.getResources(location);

        if (resources.isEmpty()) {
            throw new FileNotFoundException("Could not find path '" + location + "' in any tree packs.");
        }

        return resources.get(resources.size() - 1);
    }

    @Override
    public boolean hasResource(ResourceLocation path) {
        try {
            return !this.getResources(path).isEmpty();
        } catch (IOException e) {
            return false;
        }
    }

    private static final IResource NULL_RESOURCE =
            new SimpleResource(null, null, null, null);

    @Override
    public List<IResource> getResources(ResourceLocation path) throws IOException {
        return this.resourcePacks.stream()
                .map(resourcePack -> getResource(path, resourcePack))
                .filter(resourcePack -> resourcePack != NULL_RESOURCE)
                .collect(Collectors.toList());
    }

    private IResource getResource(ResourceLocation path, TreeResourcePack resourcePack) {
        final InputStream stream;

        try {
            stream = resourcePack.getResource(path);
        } catch (final IOException e) {
            return NULL_RESOURCE;
        }

        return new SimpleResource(resourcePack.getName(), path, stream, null);
    }

    private Stream<ResourceLocation> resourceLocationStream(String path, Predicate<String> filter) {
        return this.resourcePacks.stream()
                .map(
                        resourcePack -> resourcePack.getNamespaces().stream()
                                .map(namespace -> resourcePack.getResources(namespace, path,
                                        Integer.MAX_VALUE, filter))
                                .flatMap(Collection::stream)
                                .collect(CommonCollectors.toLinkedSet())
                ).flatMap(Collection::stream);
    }

    @Override
    public Collection<ResourceLocation> listResources(String path, Predicate<String> filter) {
        return this.resourceLocationStream(path, filter).collect(CommonCollectors.toAlternateLinkedSet());
    }

    @Override
    public Stream<IResourcePack> listPacks() {
        return this.resourcePacks.stream().map(treeResourcePack -> treeResourcePack);
    }

}
