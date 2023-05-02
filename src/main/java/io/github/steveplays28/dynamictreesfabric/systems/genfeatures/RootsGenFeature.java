package io.github.steveplays28.dynamictreesfabric.systems.genfeatures;

import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.api.configurations.ConfigurationProperty;
import io.github.steveplays28.dynamictreesfabric.blocks.branches.SurfaceRootBlock;
import io.github.steveplays28.dynamictreesfabric.blocks.branches.TrunkShellBlock;
import io.github.steveplays28.dynamictreesfabric.init.DTRegistries;
import io.github.steveplays28.dynamictreesfabric.systems.genfeatures.context.PostGenerationContext;
import io.github.steveplays28.dynamictreesfabric.systems.genfeatures.context.PostGrowContext;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import io.github.steveplays28.dynamictreesfabric.util.CoordUtils;
import io.github.steveplays28.dynamictreesfabric.util.CoordUtils.Surround;
import io.github.steveplays28.dynamictreesfabric.util.SimpleVoxmap;
import io.github.steveplays28.dynamictreesfabric.util.function.TetraFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

public class RootsGenFeature extends GenFeature {

    public static final ConfigurationProperty<Integer> MIN_TRUNK_RADIUS = ConfigurationProperty.integer("min_trunk_radius");
    public static final ConfigurationProperty<Integer> LEVEL_LIMIT = ConfigurationProperty.integer("level_limit");
    public static final ConfigurationProperty<Float> SCALE_FACTOR = ConfigurationProperty.floatProperty("scale_factor");

    private TetraFunction<Integer, Integer, Integer, Float, Integer> scaler = (inRadius, trunkRadius, minTrunkRadius, scaleFactor) -> {
        float scale = Mth.clamp(trunkRadius >= minTrunkRadius ? (trunkRadius / scaleFactor) : 0, 0, 1);
        return (int) (inRadius * scale);
    };

    private final SimpleVoxmap[] rootMaps;

    public RootsGenFeature(ResourceLocation registryName) {
        super(registryName);

        this.rootMaps = createRootMaps();
    }

    @Override
    protected void registerProperties() {
        this.register(MIN_TRUNK_RADIUS, LEVEL_LIMIT, SCALE_FACTOR);
    }

    @Override
    protected GenFeatureConfiguration createDefaultConfiguration() {
        return super.createDefaultConfiguration()
                .with(MIN_TRUNK_RADIUS, 13)
                .with(LEVEL_LIMIT, 2)
                .with(SCALE_FACTOR, 24f);
    }

    protected SimpleVoxmap[] createRootMaps() {
        //These are basically bitmaps of the root structures
        byte[][] rootData = new byte[][]{
                {0, 3, 0, 0, 0, 0, 0, 0, 5, 6, 7, 0, 3, 2, 0, 0, 0, 8, 0, 5, 0, 0, 6, 8, 0, 8, 7, 0, 0, 0, 0, 7, 0, 0, 0, 0, 3, 4, 6, 4, 0, 0, 0, 2, 0, 0, 3, 2, 1},
                {0, 3, 0, 0, 0, 0, 0, 0, 5, 6, 7, 0, 3, 2, 0, 0, 0, 8, 0, 5, 0, 0, 6, 8, 0, 8, 7, 0, 0, 0, 0, 7, 0, 0, 0, 0, 3, 4, 6, 4, 0, 0, 0, 2, 0, 0, 3, 2, 1},
                {0, 0, 2, 0, 0, 0, 0, 3, 4, 6, 0, 0, 0, 0, 1, 0, 7, 8, 0, 0, 0, 0, 0, 0, 0, 7, 6, 0, 0, 0, 0, 8, 0, 5, 4, 0, 5, 6, 7, 0, 0, 2, 2, 4, 0, 0, 0, 0, 0},
                {0, 4, 0, 0, 0, 0, 0, 0, 5, 6, 0, 0, 1, 0, 0, 0, 7, 0, 0, 3, 0, 0, 0, 8, 0, 8, 7, 0, 0, 0, 0, 8, 0, 5, 4, 0, 0, 6, 7, 3, 0, 2, 0, 4, 5, 0, 0, 0, 0},
                {3, 4, 5, 0, 0, 0, 0, 2, 0, 6, 0, 0, 0, 0, 0, 0, 7, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 7, 0, 0, 0, 0, 0, 0, 6, 0, 0, 0, 0, 2, 3, 5, 2, 0},
                {0, 0, 4, 0, 0, 0, 0, 0, 0, 6, 7, 0, 2, 0, 0, 0, 0, 8, 0, 3, 0, 5, 7, 8, 0, 6, 5, 0, 3, 0, 0, 8, 0, 2, 1, 0, 3, 0, 7, 0, 0, 0, 0, 4, 5, 6, 0, 0, 0}
        };

        SimpleVoxmap[] maps = new SimpleVoxmap[rootData.length];

        for (int i = 0; i < maps.length; i++) {
            maps[i] = new SimpleVoxmap(7, 1, 7, rootData[i]).setCenter(new BlockPos(3, 0, 3));
        }

        return maps;
    }

    @Override
    protected boolean postGenerate(GenFeatureConfiguration configuration, PostGenerationContext context) {
        final BlockPos treePos = context.pos().above();
        final int trunkRadius = TreeHelper.getRadius(context.world(), treePos);
        return trunkRadius >= configuration.get(MIN_TRUNK_RADIUS) &&
                this.startRoots(configuration, context.world(), treePos, context.species(), trunkRadius);
    }

    @Override
    protected boolean postGrow(GenFeatureConfiguration configuration, PostGrowContext context) {
        final Level world = context.world();
        final BlockPos treePos = context.treePos();
        final int trunkRadius = TreeHelper.getRadius(world, treePos);

        if (context.fertility() > 0 && trunkRadius >= configuration.get(MIN_TRUNK_RADIUS)) {
            final Surround surr = Surround.values()[world.random.nextInt(8)];
            final BlockPos dPos = treePos.offset(surr.getOffset());
            if (world.getBlockState(dPos).getBlock() instanceof SurfaceRootBlock) {
                world.setBlockAndUpdate(dPos, DTRegistries.TRUNK_SHELL.get().defaultBlockState().setValue(TrunkShellBlock.CORE_DIR, surr.getOpposite()));
            }

            this.startRoots(configuration, world, treePos, context.species(), trunkRadius);
        }

        return true;
    }

    public boolean startRoots(GenFeatureConfiguration configuration, LevelAccessor world, BlockPos treePos, Species species, int trunkRadius) {
        int hash = CoordUtils.coordHashCode(treePos, 2);
        SimpleVoxmap rootMap = rootMaps[hash % rootMaps.length];
        this.nextRoot(world, rootMap, treePos, species, trunkRadius, configuration.get(MIN_TRUNK_RADIUS), configuration.get(SCALE_FACTOR), BlockPos.ZERO, 0,
                -1, null, 0, configuration.get(LEVEL_LIMIT));
        return true;
    }

    protected void nextRoot(LevelAccessor world, SimpleVoxmap rootMap, BlockPos trunkPos, Species species, int trunkRadius, int minTrunkRadius, float scaleFactor, BlockPos pos, int height, int levelCount, Direction fromDir, int radius, int levelLimit) {

        for (int depth = 0; depth < 2; depth++) {
            BlockPos currPos = trunkPos.offset(pos).above(height - depth);
            BlockState placeState = world.getBlockState(currPos);
            BlockState belowState = world.getBlockState(currPos.below());

            boolean onNormalCube = belowState.isRedstoneConductor(world, currPos.below());

            if (pos == BlockPos.ZERO || isReplaceableWithRoots(world, placeState, currPos) && (depth == 1 || onNormalCube)) {
                if (radius > 0) {
                    species.getFamily().getSurfaceRoot().ifPresent(root ->
                            root.setRadius(world, currPos, radius, 3)
                    );
                }
                if (onNormalCube) {
                    for (Direction dir : CoordUtils.HORIZONTALS) {
                        if (dir != fromDir) {
                            BlockPos dPos = pos.relative(dir);
                            int nextRad = this.scaler.apply((int) rootMap.getVoxel(dPos), trunkRadius, minTrunkRadius, scaleFactor);
                            if (pos != BlockPos.ZERO && nextRad >= radius) {
                                nextRad = radius - 1;
                            }
                            int thisLevelCount = depth == 1 ? 1 : levelCount + 1;
                            if (nextRad > 0 && thisLevelCount <= levelLimit) {//Don't go longer than 2 adjacent blocks on a single level
                                nextRoot(world, rootMap, trunkPos, species, trunkRadius, minTrunkRadius, scaleFactor, dPos, height - depth, thisLevelCount, dir.getOpposite(), nextRad, levelLimit);//Recurse here
                            }
                        }
                    }
                }
                break;
            }
        }

    }

    protected boolean isReplaceableWithRoots(LevelAccessor world, BlockState placeState, BlockPos pos) {
        if (world.isEmptyBlock(pos) || placeState.getBlock() instanceof TrunkShellBlock) {
            return true;
        }

        Material material = placeState.getMaterial();
        return material.isReplaceable() && material != Material.LAVA;
    }

    public RootsGenFeature setScaler(TetraFunction<Integer, Integer, Integer, Float, Integer> scaler) {
        this.scaler = scaler;
        return this;
    }

}
