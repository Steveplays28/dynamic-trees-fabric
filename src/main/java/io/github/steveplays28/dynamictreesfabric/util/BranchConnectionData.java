package io.github.steveplays28.dynamictreesfabric.util;

import net.minecraft.block.BlockState;

public class BranchConnectionData {

    private final BlockState blockState;
    private final Connections connections;

    public BranchConnectionData(BlockState blockState, Connections connections) {
        this.blockState = blockState;
        this.connections = connections;
    }

    public BlockState getBlockState() {
        return blockState;
    }

    public Connections getConnections() {
        return connections;
    }

}
