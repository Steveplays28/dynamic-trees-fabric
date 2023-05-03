package io.github.steveplays28.dynamictreesfabric.systems.genfeatures;

import static net.minecraft.block.MushroomBlock.*;

import com.google.common.collect.Iterables;
import io.github.steveplays28.dynamictreesfabric.api.configurations.ConfigurationProperty;
import io.github.steveplays28.dynamictreesfabric.systems.genfeatures.context.FullGenerationContext;
import io.github.steveplays28.dynamictreesfabric.util.BlockBounds;
import io.github.steveplays28.dynamictreesfabric.util.SafeChunkBounds;
import io.github.steveplays28.dynamictreesfabric.util.SimpleVoxmap;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.biome.Biome;

/**
 * Generates a singular huge mushroom
 *
 * @author ferreusveritas
 */
public class HugeMushroomGenFeature extends GenFeature {

	public static final ConfigurationProperty<Block> MUSHROOM_BLOCK = ConfigurationProperty.block("mushroom");
	public static final ConfigurationProperty<Block> STEM_BLOCK = ConfigurationProperty.block("stem");
	static final SimpleVoxmap BROWN_CAP;
	static final SimpleVoxmap BROWN_CAP_MEDIUM;
	static final SimpleVoxmap BROWN_CAP_SMALL;
	static final SimpleVoxmap RED_CAP;
	static final SimpleVoxmap RED_CAP_SHORT;
	static final SimpleVoxmap RED_CAP_SMALL;

	static {
		BROWN_CAP = new SimpleVoxmap(7, 1, 7, new byte[]{
				0, 1, 2, 2, 2, 3, 0,
				1, 5, 5, 5, 5, 5, 3,
				4, 5, 5, 5, 5, 5, 6,
				4, 5, 5, 5, 5, 5, 6,
				4, 5, 5, 5, 5, 5, 6,
				7, 5, 5, 5, 5, 5, 9,
				0, 7, 8, 8, 8, 9, 0
		}).setCenter(new BlockPos(3, 0, 3));

		BROWN_CAP_MEDIUM = new SimpleVoxmap(5, 1, 5, new byte[]{
				0, 1, 2, 3, 0,
				1, 5, 5, 5, 3,
				4, 5, 5, 5, 6,
				7, 5, 5, 5, 9,
				0, 7, 8, 9, 0
		}).setCenter(new BlockPos(2, 0, 2));

		BROWN_CAP_SMALL = new SimpleVoxmap(3, 1, 3, new byte[]{
				1, 2, 3,
				4, 5, 6,
				7, 8, 9
		}).setCenter(new BlockPos(1, 0, 1));

		RED_CAP = new SimpleVoxmap(5, 4, 5, new byte[]{
				0, 1, 2, 3, 0,
				1, 0, 0, 0, 3,
				4, 0, 10, 0, 6,
				7, 0, 0, 0, 9,
				0, 7, 8, 9, 0, // Bottom

				0, 1, 2, 3, 0,
				1, 0, 0, 0, 3,
				4, 0, 10, 0, 6,
				7, 0, 0, 0, 9,
				0, 7, 8, 9, 0,

				0, 1, 2, 3, 0,
				1, 0, 0, 0, 3,
				4, 0, 10, 0, 6,
				7, 0, 0, 0, 9,
				0, 7, 8, 9, 0,

				0, 0, 0, 0, 0,
				0, 1, 2, 3, 0,
				0, 4, 5, 6, 0,
				0, 7, 8, 9, 0,
				0, 0, 0, 0, 0 // Top
		}).setCenter(new BlockPos(2, 3, 2));

		RED_CAP_SHORT = new SimpleVoxmap(5, 3, 5, new byte[]{
				0, 1, 2, 3, 0,
				1, 0, 0, 0, 3,
				4, 0, 10, 0, 6,
				7, 0, 0, 0, 9,
				0, 7, 8, 9, 0, // Bottom

				0, 1, 2, 3, 0,
				1, 0, 0, 0, 3,
				4, 0, 10, 0, 6,
				7, 0, 0, 0, 9,
				0, 7, 8, 9, 0,

				0, 0, 0, 0, 0,
				0, 1, 2, 3, 0,
				0, 4, 5, 6, 0,
				0, 7, 8, 9, 0,
				0, 0, 0, 0, 0 // Top
		}).setCenter(new BlockPos(2, 2, 2));

		RED_CAP_SMALL = new SimpleVoxmap(3, 2, 3, new byte[]{
				1, 2, 3,
				4, 10, 6,
				7, 8, 9, // Bottom

				1, 2, 3,
				4, 5, 6,
				7, 8, 9 // Top
		}).setCenter(new BlockPos(1, 1, 1));
	}

	private int height = -1;
	public HugeMushroomGenFeature(Identifier registryName) {
		super(registryName);
	}

	@Override
	protected void registerProperties() {
		this.register(MUSHROOM_BLOCK, STEM_BLOCK);
	}

	@Override
	protected GenFeatureConfiguration createDefaultConfiguration() {
		return super.createDefaultConfiguration()
				.with(MUSHROOM_BLOCK, Blocks.RED_MUSHROOM_BLOCK)
				.with(STEM_BLOCK, Blocks.MUSHROOM_STEM);
	}

	public HugeMushroomGenFeature setHeight(int height) {
		this.height = height;
		return this;
	}

	/**
	 * Select the appropriate sized cap for a huge mushroom type
	 *
	 * @param mushroomBlock Red or Brown mushroom block
	 * @param height        The height of the huge mushroom
	 * @return a voxmap of the cap to create
	 */
	protected SimpleVoxmap getCapForHeight(Block mushroomBlock, int height) {

		// Brown Cap mushroom
		if (mushroomBlock == Blocks.BROWN_MUSHROOM_BLOCK) {
			switch (height) {
				case 2:
				case 3:
					return BROWN_CAP_SMALL;
				case 4:
				case 5:
					return BROWN_CAP_MEDIUM;
				default:
					return BROWN_CAP;
			}
		}

		// Red Cap mushroom
		switch (height) {
			case 2:
				return BROWN_CAP_SMALL;
			case 3:
				return RED_CAP_SMALL;
			case 4:
				return RED_CAP_SHORT;
			default:
				return RED_CAP;
		}
	}

	//Override this for custom mushroom heights
	protected int getMushroomHeight(WorldAccess world, BlockPos rootPos, RegistryEntry<Biome> biome, Random random, int radius, SafeChunkBounds safeBounds) {
		return this.height > 0 ? this.height : random.nextInt(9) + 2;
	}

	@Override
	protected boolean generate(GenFeatureConfiguration configuration, FullGenerationContext context) {
		final WorldAccess world = context.world();
		final BlockPos rootPos = context.pos();

		final BlockPos genPos = rootPos.up();
		final int height = this.getMushroomHeight(world, rootPos, context.biome(), context.random(), context.radius(), context.bounds());
		final BlockState soilState = world.getBlockState(rootPos);

		if (context.species().isAcceptableSoilForWorldgen(world, rootPos, soilState)) {
			Block mushroomBlock = configuration.get(MUSHROOM_BLOCK);

			if (mushroomBlock == null) {
				mushroomBlock = context.random().nextBoolean() ? Blocks.BROWN_MUSHROOM_BLOCK : Blocks.RED_MUSHROOM_BLOCK;
			}

			final SimpleVoxmap capMap = this.getCapForHeight(mushroomBlock, height);

			final BlockPos capPos = genPos.up(height - 1); // Determine the cap position(top block of mushroom cap)
			final BlockBounds capBounds = capMap.getBounds().move(capPos); // Get a bounding box for the entire cap

			if (context.bounds().inBounds(capBounds, true)) {//Check to see if the cap can be generated in safeBounds

				// Check there's room for a mushroom cap and stem.
				for (BlockPos mutPos : Iterables.concat(BlockPos.iterate(BlockPos.ORIGIN.down(capMap.getLenY()), BlockPos.ORIGIN.down(height - 1)), capMap.getAllNonZero())) {
					final BlockPos dPos = mutPos.add(capPos);
					final BlockState state = world.getBlockState(dPos);
					if (!state.getMaterial().isReplaceable()) {
						return true;
					}
				}

				final BlockState stemState = configuration.get(STEM_BLOCK).getDefaultState();

				// Construct the mushroom cap from the voxel map.
				for (SimpleVoxmap.Cell cell : capMap.getAllNonZeroCells()) {
					world.setBlockState(capPos.add(cell.getPos()), this.getMushroomStateForValue(mushroomBlock, stemState, cell.getValue(), cell.getPos().getY()), 2);
				}

				// Construct the stem.
				final int stemLen = height - capMap.getLenY();
				for (int y = 0; y < stemLen; y++) {
					world.setBlockState(genPos.up(y), stemState, 2);
				}

				return true;
			}
		}

		return true;
	}

	protected BlockState getMushroomStateForValue(Block mushroomBlock, BlockState stemBlock, int value, int y) {
		if (value == 10) {
			return stemBlock;
		}

		return mushroomBlock.getDefaultState()
				.with(UP, y >= -1)
				.with(DOWN, false)
				.with(NORTH, value >= 1 && value <= 3)
				.with(SOUTH, value >= 7 && value <= 9)
				.with(WEST, value == 1 || value == 4 || value == 7)
				.with(EAST, value % 3 == 0);
	}

}
