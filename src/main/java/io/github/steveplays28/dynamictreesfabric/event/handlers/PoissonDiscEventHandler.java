package io.github.steveplays28.dynamictreesfabric.event.handlers;

import io.github.steveplays28.dynamictreesfabric.systems.poissondisc.UniversalPoissonDiscProvider;
import io.github.steveplays28.dynamictreesfabric.worldgen.TreeGenerator;
import net.minecraftforge.event.level.ChunkDataEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;

public class PoissonDiscEventHandler {

	// TODO: Check ServerWorld casts work in all dimensions and with modded dimensions.

	public static final String CIRCLE_DATA_ID = "GTCD"; // ID for "Growing Trees Circle Data" NBT tag.

	/** This piece of crap event will not fire until after PLENTY of chunks have already generated when creating a new world.  WHY!? */
	/*@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {}*/

	/**
	 * We'll use this instead because at least new chunks aren't created after the world is unloaded. I hope. >:(
	 */
	@SubscribeEvent
	public void onWorldUnload(LevelEvent.Unload event) {
		final WorldAccess world = event.getLevel();
		if (!world.isClient()) {
			TreeGenerator.getTreeGenerator().getCircleProvider().unloadWorld((ServerWorld) world);//clears the circles
		}
	}

	@SubscribeEvent
	public void onChunkDataLoad(ChunkDataEvent.Load event) {
		final WorldAccess world = event.getLevel();

		if (world == null || world.isClient()) {
			return;
		}

		final byte[] circleData = event.getData().getByteArray(CIRCLE_DATA_ID);
		final UniversalPoissonDiscProvider discProvider = TreeGenerator.getTreeGenerator().getCircleProvider();

		final ChunkPos chunkPos = event.getChunk().getPos();
		discProvider.setChunkPoissonData((ServerWorld) world, chunkPos, circleData);
	}

	@SubscribeEvent
	public void onChunkDataSave(ChunkDataEvent.Save event) {
		final ServerWorld world = (ServerWorld) event.getLevel();
		final UniversalPoissonDiscProvider discProvider = TreeGenerator.getTreeGenerator().getCircleProvider();
		final Chunk chunk = event.getChunk();
		final ChunkPos chunkPos = chunk.getPos();

		final byte[] circleData = discProvider.getChunkPoissonData(world, chunkPos);
		event.getData().putByteArray(CIRCLE_DATA_ID, circleData); // Set circle data.

		if (chunk instanceof WorldChunk && !((WorldChunk) chunk).loadedToWorld) {
			discProvider.unloadChunkPoissonData(world, chunkPos);
		}
	}

}
