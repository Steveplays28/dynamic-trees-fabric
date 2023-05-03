package io.github.steveplays28.dynamictreesfabric.api.worldgen;

public interface RadiusCoordinator {

	int getRadiusAtCoords(int x, int z);

	boolean runPass(int chunkX, int chunkZ, int pass);

}
