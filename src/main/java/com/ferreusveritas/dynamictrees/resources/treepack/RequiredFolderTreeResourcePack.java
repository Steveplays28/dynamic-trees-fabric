package com.ferreusveritas.dynamictrees.resources.treepack;

import com.ferreusveritas.dynamictrees.resources.treepack.FolderTreeResourcePack;

import java.nio.file.Path;

public class RequiredFolderTreeResourcePack extends FolderTreeResourcePack {

    public RequiredFolderTreeResourcePack(Path path) {
        super(path);
    }

    @Override
    public boolean isRequired() {
        return true;
    }

}
