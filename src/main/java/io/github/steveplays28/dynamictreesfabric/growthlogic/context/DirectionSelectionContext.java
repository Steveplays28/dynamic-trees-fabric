package io.github.steveplays28.dynamictreesfabric.growthlogic.context;

import io.github.steveplays28.dynamictreesfabric.blocks.branches.BranchBlock;
import io.github.steveplays28.dynamictreesfabric.systems.GrowSignal;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * @author Harley O'Connor
 */
public class DirectionSelectionContext extends PositionalSpeciesContext {

    private final BranchBlock branch;
    private final GrowSignal signal;

    public DirectionSelectionContext(World world, BlockPos pos, Species species, BranchBlock branch, GrowSignal signal) {
        super(world, pos, species);
        this.branch = branch;
        this.signal = signal;
    }

    public BranchBlock branch() {
        return branch;
    }

    public GrowSignal signal() {
        return signal;
    }

}
