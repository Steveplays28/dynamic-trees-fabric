package io.github.steveplays28.dynamictreesfabric.systems.poissondisc;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.github.steveplays28.dynamictreesfabric.api.worldgen.PoissonDiscProvider;
import io.github.steveplays28.dynamictreesfabric.event.PoissonDiscProviderCreateEvent;
import io.github.steveplays28.dynamictreesfabric.worldgen.BiomeRadiusCoordinator;
import io.github.steveplays28.dynamictreesfabric.worldgen.TreeGenerator;
import net.minecraftforge.common.MinecraftForge;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldAccess;

public class UniversalPoissonDiscProvider {

	private final Map<Identifier, PoissonDiscProvider> providerMap = new ConcurrentHashMap<>();

	protected PoissonDiscProvider createCircleProvider(ServerWorld world, WorldAccess iWorld) {
		final BiomeRadiusCoordinator radiusCoordinator = new BiomeRadiusCoordinator(TreeGenerator.getTreeGenerator(),
				world.getRegistryKey().getValue(), iWorld);
		final PoissonDiscProviderCreateEvent poissonDiscProviderCreateEvent = new PoissonDiscProviderCreateEvent(world,
				new LevelPoissonDiscProvider(radiusCoordinator).setSeed(world.getSeed()));
		MinecraftForge.EVENT_BUS.post(poissonDiscProviderCreateEvent);
		return poissonDiscProviderCreateEvent.getPoissonDiscProvider();
	}

	public PoissonDiscProvider getProvider(ServerWorld world, WorldAccess iWorld) {
		return this.providerMap.computeIfAbsent(world.getRegistryKey().getValue(), k -> createCircleProvider(world, iWorld));
	}

	public List<PoissonDisc> getPoissonDiscs(ServerWorld world, WorldAccess iWorld, ChunkPos chunkPos) {
		final PoissonDiscProvider provider = getProvider(world, iWorld);
		return provider.getPoissonDiscs(chunkPos.x, 0, chunkPos.z);
	}

	public void unloadWorld(ServerWorld world) {
		this.providerMap.remove(world.getRegistryKey().getValue());
	}

	public void setChunkPoissonData(ServerWorld world, ChunkPos chunkPos, byte[] circleData) {
		this.getProvider(world, world).setChunkPoissonData(chunkPos.x, 0, chunkPos.z, circleData);
	}

	public byte[] getChunkPoissonData(ServerWorld world, ChunkPos chunkPos) {
		return this.getProvider(world, world).getChunkPoissonData(chunkPos.x, 0, chunkPos.z);
	}

	public void unloadChunkPoissonData(ServerWorld world, ChunkPos chunkPos) {
		this.getProvider(world, world).unloadChunkPoissonData(chunkPos.x, 0, chunkPos.z);
	}

}
