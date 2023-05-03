package io.github.steveplays28.dynamictreesfabric.systems.dropcreators.context;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import io.github.steveplays28.dynamictreesfabric.trees.Species;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

/**
 * Defines context variables about a specific drop.
 *
 * @author Harley O'Connor
 */
public class DropContext {

	private final World world;
	private final Random random;
	private final BlockPos pos;

	private final Species species;
	private final List<ItemStack> dropList;

	private final ItemStack tool;
	private final int fertility;
	private final int fortune;

	public DropContext(@Nullable World world, BlockPos pos, Species species, List<ItemStack> dropList) {
		this(world, pos, species, dropList, ItemStack.EMPTY, -1, 0);
	}

	public DropContext(World world, Random random, BlockPos pos, Species species, List<ItemStack> dropList, int fertility, int fortune) {
		this(world, pos, species, dropList, ItemStack.EMPTY, fertility, fortune);
	}

	public DropContext(@Nullable World world, BlockPos pos, Species species, List<ItemStack> dropList, ItemStack tool, int fertility, int fortune) {
		this.world = world;
		this.random = world == null ? Random.create() : world.random;
		this.pos = pos;
		this.species = species;
		this.dropList = dropList;
		this.tool = tool;
		this.fertility = fertility;
		this.fortune = fortune;
	}

	public World world() {
		return world;
	}

	public Random random() {
		return this.random;
	}

	public BlockPos pos() {
		return pos;
	}

	public Species species() {
		return species;
	}

	public List<ItemStack> drops() {
		return dropList;
	}

	public ItemStack tool() {
		return tool;
	}

	/**
	 * Returns the fertility of the relevant tree, or {@code -1} if it was not available.
	 *
	 * @return The fertility of the related tree, or {@code -1} if it was unavailable.
	 */
	public int fertility() {
		return fertility;
	}

	public int fortune() {
		return fortune;
	}

}
