package io.github.steveplays28.dynamictreesfabric.worldgen;

import io.github.steveplays28.dynamictreesfabric.api.worldgen.GroundFinder;
import io.github.steveplays28.dynamictreesfabric.util.CoordUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;

import java.util.Collections;
import java.util.List;

/**
 * @author Harley O'Connor
 */
public final class OverworldGroundFinder implements GroundFinder {

    @Override
    public List<BlockPos> findGround(WorldGenLevel world, BlockPos start) {
        return Collections.singletonList(CoordUtils.findWorldSurface(world, start, true));
    }

}
