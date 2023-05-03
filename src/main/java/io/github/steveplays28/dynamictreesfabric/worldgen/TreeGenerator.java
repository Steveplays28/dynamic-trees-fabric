package io.github.steveplays28.dynamictreesfabric.worldgen;

import java.util.Objects;

import io.github.steveplays28.dynamictreesfabric.api.worldgen.BiomePropertySelectors;
import io.github.steveplays28.dynamictreesfabric.api.worldgen.BiomePropertySelectors.Chance;
import io.github.steveplays28.dynamictreesfabric.api.worldgen.BiomePropertySelectors.SpeciesSelection;
import io.github.steveplays28.dynamictreesfabric.init.DTConfigs;
import io.github.steveplays28.dynamictreesfabric.systems.poissondisc.PoissonDisc;
import io.github.steveplays28.dynamictreesfabric.systems.poissondisc.UniversalPoissonDiscProvider;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import io.github.steveplays28.dynamictreesfabric.util.RandomXOR;
import io.github.steveplays28.dynamictreesfabric.util.SafeChunkBounds;
import io.github.steveplays28.dynamictreesfabric.worldgen.BiomeDatabase.Entry;
import net.minecraftforge.registries.ForgeRegistries;

import net.minecraft.block.BlockState;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.biome.Biome;

public class TreeGenerator {

	protected static TreeGenerator INSTANCE;

	protected final UniversalPoissonDiscProvider circleProvider;
	protected final RandomXOR random = new RandomXOR();

	public TreeGenerator() {
		INSTANCE = this; // Set this here in case the lines in the contructor lead to calls that use getTreeGenerator
		this.circleProvider = new UniversalPoissonDiscProvider();
	}

	public static void setup() {
		new TreeGenerator();
	}

	public static TreeGenerator getTreeGenerator() {
		return INSTANCE;
	}

	public UniversalPoissonDiscProvider getCircleProvider() {
		return circleProvider;
	}

	public void makeConcreteCircle(WorldAccess world, PoissonDisc circle, int h, GeneratorResult resultType, SafeChunkBounds safeBounds) {
		makeConcreteCircle(world, circle, h, resultType, safeBounds, 0);
	}

	private BlockState getConcreteByColor(DyeColor color) {
		return Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(new Identifier(color + "_concrete"))).defaultBlockState();
	}

	public void makeConcreteCircle(WorldAccess world, PoissonDisc circle, int h, GeneratorResult resultType, SafeChunkBounds safeBounds, int flags) {
		for (int ix = -circle.radius; ix <= circle.radius; ix++) {
			for (int iz = -circle.radius; iz <= circle.radius; iz++) {
				if (circle.isEdge(circle.x + ix, circle.z + iz)) {
					safeBounds.setBlockState(world, new BlockPos(circle.x + ix, h, circle.z + iz), this.getConcreteByColor(DyeColor.byId((circle.x ^ circle.z) & 0xF)), flags, true);
				}
			}
		}

		if (resultType != GeneratorResult.GENERATED) {
			final BlockPos pos = new BlockPos(circle.x, h, circle.z);
			final DyeColor color = resultType.getColor();
			safeBounds.setBlockState(world, pos, this.getConcreteByColor(color), true);
			safeBounds.setBlockState(world, pos.up(), this.getConcreteByColor(color), true);
		}
	}

	public void makeTrees(StructureWorldAccess world, BiomeDatabase biomeDataBase, PoissonDisc circle, SafeChunkBounds safeBounds) {
		circle.add(8, 8); // Move the circle into the "stage".
		// TODO: De-couple ground finder from biomes, now that they can vary based on height.
		BlockPos pos = new BlockPos(circle.x, world.getTopY(), circle.z);
		final Entry entry = biomeDataBase.getEntry(world.getBiome(pos));
		for (BlockPos groundPos : entry.getGroundFinder().findGround(world, pos)) {
			makeTree(world, entry, circle, groundPos, safeBounds);
		}
		circle.sub(8, 8); // Move the circle back to normal coords.
	}

	public GeneratorResult makeTree(StructureWorldAccess world, BiomeDatabase.Entry biomeEntry, PoissonDisc circle, BlockPos groundPos, SafeChunkBounds safeBounds) {

		final RegistryEntry<Biome> biome = world.getBiome(groundPos);

		if (biomeEntry.isBlacklisted()) {
			return GeneratorResult.UNHANDLED_BIOME;
		}

		if (groundPos == BlockPos.ORIGIN) {
			return GeneratorResult.NO_GROUND;
		}

		random.setXOR(groundPos);

		BlockState dirtState = world.getBlockState(groundPos);

		GeneratorResult result = GeneratorResult.GENERATED;

		final BiomePropertySelectors.SpeciesSelector speciesSelector = biomeEntry.getSpeciesSelector();
		final SpeciesSelection speciesSelection = speciesSelector.getSpecies(groundPos, dirtState, random);

		if (speciesSelection.isHandled()) {
			final Species species = speciesSelection.getSpecies();
			if (species.isValid()) {
//                BlockPos newGroundPos = species.moveGroundPosWorldgen(world, groundPos, dirtState);
//                if (!newGroundPos.equals(groundPos)) {
//                    groundPos = newGroundPos;
//                    dirtState = world.getBlockState(newGroundPos);
//                }
				if (species.isAcceptableSoilForWorldgen(world, groundPos, dirtState)) {
					if (biomeEntry.getChanceSelector().getChance(random, species, circle.radius) == Chance.OK) {
						if (!species.generate(world.toServerWorld(), world, groundPos, biome, random, circle.radius, safeBounds)) {
							result = GeneratorResult.FAIL_GENERATION;
						}
					} else {
						result = GeneratorResult.FAIL_CHANCE;
					}
				} else {
					result = GeneratorResult.FAIL_SOIL;
				}
			} else {
				result = GeneratorResult.NO_TREE;
			}
		} else {
			result = GeneratorResult.UNHANDLED_BIOME;
		}

		// Display concrete circles for testing the circle growing algorithm.
		if (DTConfigs.WORLD_GEN_DEBUG.get()) {
			this.makeConcreteCircle(world, circle, groundPos.getY(), result, safeBounds);
		}

		return result;
	}

	/**
	 * This is for world gen debugging. The colors signify the different tree spawn failure modes.
	 */
	public enum GeneratorResult {
		GENERATED(DyeColor.WHITE),
		NO_TREE(DyeColor.BLACK),
		UNHANDLED_BIOME(DyeColor.YELLOW),
		FAIL_SOIL(DyeColor.BROWN),
		FAIL_CHANCE(DyeColor.BLUE),
		FAIL_GENERATION(DyeColor.RED),
		NO_GROUND(DyeColor.PURPLE);

		private final DyeColor color;

		GeneratorResult(DyeColor color) {
			this.color = color;
		}

		public DyeColor getColor() {
			return this.color;
		}

	}

}
