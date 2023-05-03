package io.github.steveplays28.dynamictreesfabric.systems.genfeatures.context;

import io.github.steveplays28.dynamictreesfabric.trees.Species;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldAccess;

/**
 * @author Harley O'Connor
 */
public abstract class GenerationContext<W extends WorldAccess> {

	private final W world;
	private final BlockPos pos;
	private final Species species;

	public GenerationContext(W world, BlockPos pos, Species species) {
		this.world = world;
		this.pos = pos;
		this.species = species;
	}

	public W world() {
		return world;
	}

	public BlockPos pos() {
		return pos;
	}

	public Species species() {
		return species;
	}

	public final Random random() {
		return this.world.getRandom();
	}

}
