package io.github.steveplays28.dynamictreesfabric.api.worldgen;

import io.github.steveplays28.dynamictreesfabric.worldgen.OverworldGroundFinder;
import io.github.steveplays28.dynamictreesfabric.worldgen.SubterraneanGroundFinder;
import java.util.List;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;

/**
 * Implementations will find a suitable area to generate a tree on the ground.
 */
@FunctionalInterface
public interface GroundFinder {

    GroundFinder OVERWORLD = new OverworldGroundFinder();
    GroundFinder SUBTERRANEAN = new SubterraneanGroundFinder();

    /**
     * Finds the {@link BlockPos} of the first ground block for the y-column of the start {@link BlockPos} given.
     *
     * @param world The {@link ISeedReader} world object.
     * @param start The {@link BlockPos} to start from.
     * @return The {@link BlockPos} of the first ground block.
     */
    List<BlockPos> findGround(StructureWorldAccess world, BlockPos start);

}
