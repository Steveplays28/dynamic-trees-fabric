package com.ferreusveritas.dynamictrees.resources.treepack;

import net.minecraft.resources.*;

import java.io.File;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TreePackAssetsFinder implements IPackFinder {

    public static final TreePackAssetsFinder INSTANCE = new TreePackAssetsFinder();

    private TreePackAssetsFinder() {
    }

    @Override
    public void loadPacks(Consumer<ResourcePackInfo> infoConsumer, ResourcePackInfo.IFactory infoFactory) {
        for (File file : TreePackLoader.getTreePackFolders()) {
            String name = "file/" + file.getName();
            ResourcePackInfo info = ResourcePackInfo.create(name, true, this.createSupplier(file),
                    infoFactory, ResourcePackInfo.Priority.TOP, IPackNameDecorator.DEFAULT);
            if (info != null) {
                infoConsumer.accept(info);
            }
        }
    }

    private Supplier<IResourcePack> createSupplier(File file) {
        return file.isDirectory() ? () -> new FolderPack(file) : () -> new FilePack(file);
    }

}
