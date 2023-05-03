package io.github.steveplays28.dynamictreesfabric.growthlogic.context;

import io.github.steveplays28.dynamictreesfabric.blocks.branches.BranchBlock;
import io.github.steveplays28.dynamictreesfabric.systems.GrowSignal;
import io.github.steveplays28.dynamictreesfabric.trees.Species;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * @author Harley O'Connor
 */
public class DirectionManipulationContext extends DirectionSelectionContext {
	private final int radius;
	private int[] probMap;

	public DirectionManipulationContext(World world, BlockPos pos, Species species,
	                                    BranchBlock branch,
	                                    GrowSignal signal, int radius, int[] probMap) {
		super(world, pos, species, branch, signal);
		this.radius = radius;
		this.probMap = probMap;
	}

	public int radius() {
		return radius;
	}

	public int[] probMap() {
		return probMap;
	}

	public void probMap(int[] probMap) {
		this.probMap = probMap;
	}
}
