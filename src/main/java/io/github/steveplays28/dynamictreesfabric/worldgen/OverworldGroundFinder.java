package io.github.steveplays28.dynamictreesfabric.worldgen;

import io.github.steveplays28.dynamictreesfabric.api.worldgen.GroundFinder;
import io.github.steveplays28.dynamictreesfabric.util.CoordUtils;
import java.util.Collections;
import java.util.List;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;

/**
 * @author Harley O'Connor
 */
public final class OverworldGroundFinder implements GroundFinder {

    @Override
    public List<BlockPos> findGround(StructureWorldAccess world, BlockPos start) {
        return Collections.singletonList(CoordUtils.findWorldSurface(world, start, true));
    }

}
