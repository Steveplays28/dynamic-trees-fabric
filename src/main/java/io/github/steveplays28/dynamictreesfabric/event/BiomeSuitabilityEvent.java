package io.github.steveplays28.dynamictreesfabric.event;

import io.github.steveplays28.dynamictreesfabric.trees.Species;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

@Cancelable
public class BiomeSuitabilityEvent extends Event {

	protected World world;
	protected Biome biome;
	protected Species species;
	protected BlockPos pos;
	protected float suitability = 1.0f;
	protected boolean handled = false;

	public BiomeSuitabilityEvent(World world, Biome biome, Species species, BlockPos pos) {
		this.world = world;
		this.biome = biome;
		this.species = species;
		this.pos = pos;
	}

	public World getWorld() {
		return world;
	}

	public Biome getBiome() {
		return biome;
	}

	public Species getSpecies() {
		return species;
	}

	public BlockPos getPos() {
		return pos;
	}

	public float getSuitability() {
		return suitability;
	}

	public void setSuitability(float suitability) {
		this.suitability = suitability;
		handled = true;
	}

	public boolean isHandled() {
		return handled;
	}

}
