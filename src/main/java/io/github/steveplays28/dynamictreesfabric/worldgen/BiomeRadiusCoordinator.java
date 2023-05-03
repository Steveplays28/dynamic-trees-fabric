package io.github.steveplays28.dynamictreesfabric.worldgen;

import io.github.steveplays28.dynamictreesfabric.api.worldgen.RadiusCoordinator;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.OctaveSimplexNoiseSampler;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.biome.Biome;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.IntUnaryOperator;

public class BiomeRadiusCoordinator implements RadiusCoordinator {

	protected final TreeGenerator treeGenerator;
	protected final WorldAccess world;
	protected final Identifier dimRegName;
	public OctaveSimplexNoiseSampler noiseGenerator;
	protected int pass;
	protected IntUnaryOperator chunkMultipass;

	public BiomeRadiusCoordinator(TreeGenerator treeGenerator, Identifier dimRegName, WorldAccess world) {
		this.noiseGenerator = new OctaveSimplexNoiseSampler(new ChunkRandom(ChunkRandom.RandomProvider.LEGACY.create(96)), new ArrayList<>(Collections.singletonList(1)));
		this.world = world;
		this.dimRegName = dimRegName;
		this.treeGenerator = treeGenerator;
	}

	@Override
	public int getRadiusAtCoords(int x, int z) {
		int rad = this.chunkMultipass.applyAsInt(pass);
		if (rad >= 2 && rad <= 8) {
			return rad;
		}

		final double scale = 128; // Effectively scales up the noisemap
		final RegistryEntry<Biome> biome = this.world.getGeneratorStoredBiome((x + 8) >> 2, world.getTopY() >> 2, (z + 8) >> 2); // Placement is offset by +8,+8

		final double noiseDensity = (this.noiseGenerator.sample(x / scale, z / scale, false) + 1D) / 2.0D; // Gives 0.0 to 1.0
		final double density = BiomeDatabases.getDimensionalOrDefault(this.dimRegName)
				.getDensitySelector(biome).getDensity(this.world.getRandom(), noiseDensity);
		final double size = ((1.0 - density) * 9); // Size is the inverse of density (gives 0 to 9)

		// Oh Joy. Random can potentially start with the same number for each chunk. Let's just
		// throw this large prime xor hack in there to get it to at least look like it's random.
		int kindaRandom = ((x * 674365771) ^ (z * 254326997)) >> 4;
		int shakelow = (kindaRandom & 0x3) % 3; // Produces 0,0,1 or 2
		int shakehigh = (kindaRandom & 0xc) % 3; // Produces 0,0,1 or 2

		return MathHelper.clamp((int) size, 2 + shakelow, 8 - shakehigh); // Clamp to tree volume radius range
	}

	@Override
	public boolean runPass(int chunkX, int chunkZ, int pass) {
		this.pass = pass;

		if (pass == 0) {
			final RegistryEntry<Biome> biome = this.world.getGeneratorStoredBiome(((chunkX << 4) + 8) >> 2, world.getTopY() >> 2, ((chunkZ << 4) + 8) >> 2); // Aim at center of chunk
			this.chunkMultipass = BiomeDatabases.getDimensionalOrDefault(this.dimRegName).getMultipass(biome);
		}

		return this.chunkMultipass.applyAsInt(pass) >= 0;
	}

}
