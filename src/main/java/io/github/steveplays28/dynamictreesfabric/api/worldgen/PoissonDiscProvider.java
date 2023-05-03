package io.github.steveplays28.dynamictreesfabric.api.worldgen;


import java.util.List;

import io.github.steveplays28.dynamictreesfabric.systems.poissondisc.PoissonDisc;

public interface PoissonDiscProvider {

	List<PoissonDisc> getPoissonDiscs(int chunkX, int chunkY, int chunkZ);

	byte[] getChunkPoissonData(int chunkX, int chunkY, int chunkZ);

	void setChunkPoissonData(int chunkX, int chunkY, int chunkZ, byte[] circleData);

	void unloadChunkPoissonData(int chunkX, int chunkY, int chunkZ);

}
