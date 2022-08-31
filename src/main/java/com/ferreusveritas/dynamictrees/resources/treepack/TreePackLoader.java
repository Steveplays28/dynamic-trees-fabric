package com.ferreusveritas.dynamictrees.resources.treepack;

import com.ferreusveritas.dynamictrees.api.resource.TreeResourcePack;
import com.ferreusveritas.dynamictrees.resources.treepack.FolderTreeResourcePack;
import com.ferreusveritas.dynamictrees.resources.treepack.TreePackAssetsFinder;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourcePackList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TreePackLoader {

    private static final File TREE_PACKS_FOLDER = new File("treepacks/");

    private static final FileFilter TREE_PACK_FILTER = (folder) ->
            folder.isDirectory() && (new File(folder, "pack.mcmeta")).isFile();

    private static File getTreePacksFolder() {
        if (!TREE_PACKS_FOLDER.exists()) {
            TREE_PACKS_FOLDER.mkdir();
        } else if (!TREE_PACKS_FOLDER.isDirectory()) {
            LogManager.getLogger().error("`treepacks` in Minecraft directory must be a folder! " +
                    "Tree packs cannot be loaded.");
            return null;
        }
        return TREE_PACKS_FOLDER;
    }

    public static List<File> getTreePackFolders() {
        File folder = getTreePacksFolder();
        if (folder == null) {
            return Collections.emptyList();
        }
        File[] folders = folder.listFiles(TREE_PACK_FILTER);
        if (folders == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(folders);
    }

    public static List<TreeResourcePack> loadTreePacks() {
        return getTreePackFolders().stream()
                .filter(folder -> new File(folder, "trees/").isDirectory())
                .map(folder -> new FolderTreeResourcePack(new File(folder, "trees/").toPath().toAbsolutePath()))
                .collect(Collectors.toList());
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerTreePackAssetsFinder() {
        ResourcePackList resourcePackRepository = Minecraft.getInstance().getResourcePackRepository();
        resourcePackRepository.sources.add(TreePackAssetsFinder.INSTANCE);
        resourcePackRepository.reload();
    }

}
