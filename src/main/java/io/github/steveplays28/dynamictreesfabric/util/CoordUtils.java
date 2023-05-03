package io.github.steveplays28.dynamictreesfabric.util;

import io.github.steveplays28.dynamictreesfabric.blocks.branches.BranchBlock;
import io.github.steveplays28.dynamictreesfabric.trees.Family;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import com.google.common.collect.AbstractIterator;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.ChunkCache;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.level.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class CoordUtils {

    // Used for devs to simulate tree growing in different location hashes.
    public static int coordXor = 0;

    public static final Direction[] HORIZONTALS = {Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST};

    public enum Surround implements StringIdentifiable {
        N("n", Direction.NORTH),
        NW("nw", Direction.NORTH, Direction.WEST),
        W("w", Direction.WEST),
        SW("sw", Direction.SOUTH, Direction.WEST),
        S("s", Direction.SOUTH),
        SE("se", Direction.SOUTH, Direction.EAST),
        E("e", Direction.EAST),
        NE("ne", Direction.NORTH, Direction.EAST);

        final private String name;
        final private Vec3i offset;

        Surround(String name, Direction... dirs) {
            this.name = name;
            BlockPos pos = BlockPos.ORIGIN;
            for (Direction d : dirs) {
                pos = pos.add(d.getVector());
            }
            this.offset = pos;
        }

        public String asString() {
            return name;
        }

        public Vec3i getOffset() {
            return offset;
        }

        public BlockPos getOffsetPos() {
            return new BlockPos(offset);
        }

        public Surround getOpposite() {
            return values()[(ordinal() + 4) & 7];
        }
    }

    public static boolean isSurroundedByLoadedChunks(World world, BlockPos pos) {
        for (Surround surr : CoordUtils.Surround.values()) {
            Vec3i dir = surr.getOffset();
            if (!((ServerWorld)world).shouldTickEntity(pos.add(dir))) {
                return false;
            }
        }

        return true;
    }

    @SuppressWarnings("deprecation")
    public static boolean canAccessStateSafely(BlockView blockReader, BlockPos pos) {
        if (blockReader instanceof WorldView) { // Handles most cases.
            return ((WorldView) blockReader).isChunkLoaded(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()));
        } else if (blockReader instanceof ChunkCache) { // Handles Region.
            return !(((ChunkCache) blockReader).getChunk(pos) instanceof EmptyChunk);
        }
        // Otherwise assume we can access state safely. In most cases this is true, and if not we know it is a
        // mod compatibility issue and a crash or logging will be more helpful in solving the problem.
        return true;
    }

    /**
     * Gets the {@link BlockState} object at the given position, or null if the block wasn't loaded. This is safer
     * because calling getBlockState on an unloaded block can cause a crash.
     *
     * @param blockReader The {@link BlockView} object.
     * @return The {@link BlockState} object, or null if it's not loaded.
     */
    @Nullable
    public static BlockState getStateSafe(BlockView blockReader, BlockPos blockPos) {
        return canAccessStateSafely(blockReader, blockPos) ? blockReader.getBlockState(blockPos) : null;
    }

    public static Direction getRandomHorizontalDir(Random rand) {
        return Direction.values()[2 + rand.nextInt(4)];//Return NSWE
    }

    /**
     * Find a suitable position for seed drops or fruit placement using ray tracing.
     *
     * @param world     The world
     * @param treePos   The block position of the {@link Family} trunk base.
     * @param branchPos The {@link BlockPos} of a {@link BranchBlock} selected as a fruit target
     * @return The {@link BlockPos} of a suitable location.  The block is always air if successful otherwise it is
     * BlockPos.ZERO
     */
    public static BlockPos getRayTraceFruitPos(WorldAccess world, Species species, BlockPos treePos, BlockPos branchPos, SafeChunkBounds safeBounds) {
        final HitResult result = branchRayTrace(world, species, treePos, branchPos, 45, 60, 4 + world.getRandom().nextInt(3), safeBounds);

        if (result != null) {
            BlockPos hitPos = new BlockPos(result.getPos());
            if (hitPos != BlockPos.ORIGIN) {
                do { // Run straight down until we hit a block that's non compatible leaves.
                    hitPos = hitPos.down();
                } while (species.getFamily().isCompatibleGenericLeaves(species, world.getBlockState(hitPos), world, hitPos));

                if (world.isAir(hitPos)) { // If that block is air then we have a winner.
                    return hitPos;
                }
            }
        }

        return BlockPos.ORIGIN;
    }

    @Nullable
    public static BlockHitResult branchRayTrace(WorldAccess world, Species species, BlockPos treePos, BlockPos branchPos, float spreadHor, float spreadVer, float distance, SafeChunkBounds safeBounds) {
        treePos = new BlockPos(treePos.getX(), branchPos.getY(), treePos.getZ()); // Make the tree pos level with the branch pos.

        Vec3d vOut = new Vec3d(branchPos.getX() - treePos.getX(), 0, branchPos.getZ() - treePos.getZ());

        if (vOut.equals(Vec3d.ZERO)) {
            vOut = new Vec3d(1, 0, 0);
            spreadHor = 180;
        }

        final float deltaYaw = (world.getRandom().nextFloat() * spreadHor * 2) - spreadHor;
        final float deltaPitch = (world.getRandom().nextFloat() * -spreadVer); // Must be greater than -90 degrees(and less than 90) for the tangent function.
        vOut = vOut.normalize(). // Normalize to unit vector.
                add(0, Math.tan(Math.toRadians(deltaPitch)), 0). // Pitch the angle downward by 0 to spreadVer degrees.
                normalize(). // Re-normalize to unit vector.
                rotateY((float) Math.toRadians(deltaYaw)). // Vary the yaw by +/- spreadHor.
                multiply(distance); // Vary the view distance.

        final Vec3d branchVec = new Vec3d(branchPos.getX(), branchPos.getY(), branchPos.getZ()).add(0.5, 0.5, 0.5); // Get the vector of the middle of the branch block.
        final Vec3d vantageVec = branchVec.add(vOut); // Make a vantage point to look at the branch.
        final BlockPos vantagePos = new BlockPos(vantageVec); // Convert Vector to BlockPos for testing.

        if (!safeBounds.inBounds(vantagePos, false) || world.isAir(vantagePos)) { // The observing block must be in free space.
            final BlockHitResult result = rayTraceBlocks(world, new CustomRayTraceContext(vantageVec, branchVec, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE), safeBounds);
            // Beyond here should be safe since the only blocks that can possibly be hit are in loaded chunks.
            final BlockPos hitPos = new BlockPos(result.getPos());
            if (result.getType() == HitResult.Type.BLOCK && !hitPos.equals(BlockPos.ORIGIN)) { // We found a block.
                if (species.getFamily().isCompatibleGenericLeaves(species, world.getBlockState(hitPos), world, hitPos)) { // Test if it's the right kind of leaves for the species.
                    return result;
                }
            }
        }

        return null;
    }

    /**
     * I had to import Minecraft's block ray trace algorithm to make it worldgen blocksafe. I honestly don't know much
     * about what's going on in here because I haven't studied it.
     * <p>
     * If an attempt is made to read a block in an unloaded chunk it will simply return AIR or the properties of AIR
     * where applicable.
     *
     * @param world
     * @param context
     * @return
     */
    public static BlockHitResult rayTraceBlocks(WorldAccess world, CustomRayTraceContext context, SafeChunkBounds safeBounds) {
        return getRayTraceVector(context, (fromContext, blockPos) -> {
            BlockState blockstate = safeBounds.inBounds(blockPos, false) ? world.getBlockState(blockPos) : Blocks.AIR.getDefaultState();
            FluidState fluidState = safeBounds.inBounds(blockPos, false) ? world.getFluidState(blockPos) : Fluids.EMPTY.getDefaultState();
            Vec3d startVec = fromContext.getStartVector();
            Vec3d endVec = fromContext.getEndVector();
            VoxelShape voxelshape = safeBounds.inBounds(blockPos, false) ? fromContext.getBlockShape(blockstate, world, blockPos) : VoxelShapes.empty();
            BlockHitResult blockraytraceresult = world.raycastBlock(startVec, endVec, blockPos, voxelshape, blockstate);
            VoxelShape voxelshape1 = safeBounds.inBounds(blockPos, false) ? fromContext.getFluidShape(fluidState, world, blockPos) : VoxelShapes.empty();
            BlockHitResult blockraytraceresult1 = voxelshape1.raycast(startVec, endVec, blockPos);
            double d0 = blockraytraceresult == null ? Double.MAX_VALUE : fromContext.getStartVector().squaredDistanceTo(blockraytraceresult.getPos());
            double d1 = blockraytraceresult1 == null ? Double.MAX_VALUE : fromContext.getStartVector().squaredDistanceTo(blockraytraceresult1.getPos());
            return d0 <= d1 ? blockraytraceresult : blockraytraceresult1;
        }, (context1) -> {
            Vec3d vec3d = context1.getStartVector().subtract(context1.getEndVector());
            return BlockHitResult.createMissed(context1.getEndVector(), Direction.getFacing(vec3d.x, vec3d.y, vec3d.z), new BlockPos(context1.getEndVector()));
        });
    }

    private static <T> T getRayTraceVector(CustomRayTraceContext context, BiFunction<CustomRayTraceContext, BlockPos, T> biFunction, Function<CustomRayTraceContext, T> function) {
        Vec3d startVec = context.getStartVector();
        Vec3d endVec = context.getEndVector();
        if (startVec.equals(endVec)) {
            return function.apply(context);
        } else {
            double vantX = MathHelper.lerp(-1.0E-7D, endVec.x, startVec.x);
            double vantY = MathHelper.lerp(-1.0E-7D, endVec.y, startVec.y);
            double vantZ = MathHelper.lerp(-1.0E-7D, endVec.z, startVec.z);
            double lookX = MathHelper.lerp(-1.0E-7D, startVec.x, endVec.x);
            double lookY = MathHelper.lerp(-1.0E-7D, startVec.y, endVec.y);
            double lookZ = MathHelper.lerp(-1.0E-7D, startVec.z, endVec.z);
            int i = MathHelper.floor(lookX);
            int j = MathHelper.floor(lookY);
            int k = MathHelper.floor(lookZ);
            BlockPos.Mutable blockpos$mutableblockpos = new BlockPos.Mutable(i, j, k);
            T t = biFunction.apply(context, blockpos$mutableblockpos);
            if (t != null) {
                return t;
            } else {
                double d6 = vantX - lookX;
                double d7 = vantY - lookY;
                double d8 = vantZ - lookZ;
                int l = MathHelper.sign(d6);
                int i1 = MathHelper.sign(d7);
                int j1 = MathHelper.sign(d8);
                double d9 = l == 0 ? Double.MAX_VALUE : (double) l / d6;
                double d10 = i1 == 0 ? Double.MAX_VALUE : (double) i1 / d7;
                double d11 = j1 == 0 ? Double.MAX_VALUE : (double) j1 / d8;
                double d12 = d9 * (l > 0 ? 1.0D - MathHelper.fractionalPart(lookX) : MathHelper.fractionalPart(lookX));
                double d13 = d10 * (i1 > 0 ? 1.0D - MathHelper.fractionalPart(lookY) : MathHelper.fractionalPart(lookY));
                double d14 = d11 * (j1 > 0 ? 1.0D - MathHelper.fractionalPart(lookZ) : MathHelper.fractionalPart(lookZ));

                while (d12 <= 1.0D || d13 <= 1.0D || d14 <= 1.0D) {
                    if (d12 < d13) {
                        if (d12 < d14) {
                            i += l;
                            d12 += d9;
                        } else {
                            k += j1;
                            d14 += d11;
                        }
                    } else if (d13 < d14) {
                        j += i1;
                        d13 += d10;
                    } else {
                        k += j1;
                        d14 += d11;
                    }

                    T t1 = biFunction.apply(context, blockpos$mutableblockpos.set(i, j, k));
                    if (t1 != null) {
                        return t1;
                    }
                }

                return function.apply(context);
            }
        }
    }

    /**
     * We make a custom ray trace context since vanilla's ray trace context requires an entity (for no reason '-_-)
     */
    private static class CustomRayTraceContext {
        private final Vec3d startVec;
        private final Vec3d endVec;
        private final net.minecraft.world.RaycastContext.ShapeType blockMode;
        private final net.minecraft.world.RaycastContext.FluidHandling fluidMode;

        public CustomRayTraceContext(Vec3d startVecIn, Vec3d endVecIn, net.minecraft.world.RaycastContext.ShapeType blockModeIn, net.minecraft.world.RaycastContext.FluidHandling fluidModeIn) {
            this.startVec = startVecIn;
            this.endVec = endVecIn;
            this.blockMode = blockModeIn;
            this.fluidMode = fluidModeIn;
        }

        public Vec3d getEndVector() {
            return this.endVec;
        }

        public Vec3d getStartVector() {
            return this.startVec;
        }

        public VoxelShape getBlockShape(BlockState state, BlockView world, BlockPos pos) {
            return this.blockMode.get(state, world, pos, ShapeContext.absent());
        }

        public VoxelShape getFluidShape(FluidState state, BlockView world, BlockPos pos) {
            return this.fluidMode.handled(state) ? state.getShape(world, pos) : VoxelShapes.empty();
        }
    }

    /**
     * @param world    The world
     * @param startPos The starting position
     * @return The position of the top solid block
     */
    public static BlockPos findWorldSurface(WorldAccess world, BlockPos startPos, boolean worldGen) {
        return new BlockPos(
                startPos.getX(),
                world.getTopY(worldGen ? Heightmap.Type.WORLD_SURFACE_WG : Heightmap.Type.WORLD_SURFACE,
                        startPos.getX(), startPos.getZ()) - 1,
                startPos.getZ()
        );

//		BlockPos.Mutable pos = new BlockPos.Mutable(startPos.getX(), startPos.getY(), startPos.getZ());
//
//		//Rise up until we are no longer in a solid block
//		while(world.getBlockState(pos).canOcclude()) {
//			pos.set(pos.getX(), pos.getY() + 1, pos.getZ());
//		}
//		//Dive down until we are again
//		while(!world.getBlockState(pos).canOcclude() && pos.getY() > 50) {
//			pos.set(pos.getX(), pos.getY() - 1, pos.getZ());
//		}
//		return pos;
    }

    //Some ready made not terrible prime hash factors
    private static final int[][] coordHashMap = {
            {4111, 271, 3067},
            {7933711, 6144389, 9538033},
            {9973, 8287, 9721},
            {7211, 5437, 9613}
    };

    public static int coordHashCode(BlockPos pos, int a, int b, int c) {
        int hash = (pos.getX() * a ^ pos.getY() * b ^ pos.getZ() * c) >> 1;
        return (hash ^ coordXor) & 0xFFFF;
    }

    public static int coordHashCode(BlockPos pos, int readyMade) {
        int[] factors = coordHashMap[readyMade & 3];
        return coordHashCode(pos, factors[0], factors[1], factors[2]);
    }

    public static Iterable<BlockPos> goHorSides(BlockPos pos) {
        return goHorSides(pos, null);
    }

    public static Iterable<BlockPos> goHorSides(final BlockPos pos, @Nullable final Direction ignore) {
        return new Iterable<BlockPos>() {
            @Nonnull
            @Override
            public Iterator<BlockPos> iterator() {
                return new AbstractIterator<BlockPos>() {
                    private int currentDir = 0;

                    @Override
                    protected BlockPos computeNext() {
                        while (true) {
                            if (currentDir < HORIZONTALS.length) {
                                Direction face = HORIZONTALS[currentDir++];
                                if (face != ignore) {
                                    return pos.offset(face);
                                }
                            } else {
                                return this.endOfData();
                            }
                        }
                    }
                };
            }
        };
    }

}
