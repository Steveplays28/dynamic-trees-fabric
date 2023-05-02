package io.github.steveplays28.dynamictreesfabric.systems.genfeatures;

import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.api.configurations.ConfigurationProperty;
import io.github.steveplays28.dynamictreesfabric.api.network.MapSignal;
import io.github.steveplays28.dynamictreesfabric.blocks.branches.BranchBlock;
import io.github.steveplays28.dynamictreesfabric.blocks.leaves.DynamicLeavesBlock;
import io.github.steveplays28.dynamictreesfabric.systems.genfeatures.context.PostGenerationContext;
import io.github.steveplays28.dynamictreesfabric.systems.genfeatures.context.PostGrowContext;
import io.github.steveplays28.dynamictreesfabric.systems.nodemappers.FindEndsNode;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import io.github.steveplays28.dynamictreesfabric.util.CoordUtils;
import io.github.steveplays28.dynamictreesfabric.util.SafeChunkBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;
import java.util.List;

public class VinesGenFeature extends GenFeature {

    /**
     * Vine Type - this tells the generator which side the vines generate on - ceiling for vines that grow from the
     * ceiling downward like weeping vines, floor for vines that grow from the ground upward like twisting vines, and
     * side for vines that grow on the side of blocks likes regular vines.
     */
    public enum VineType {
        CEILING,
        FLOOR, // This has not been tested properly and may need changes.
        SIDE // Note that side vines will assume the vine uses the same blockstates as a vanilla vine.
    }

    protected final BooleanProperty[] sideVineStates = new BooleanProperty[]{null, null, VineBlock.NORTH, VineBlock.SOUTH, VineBlock.WEST, VineBlock.EAST};

    public static final ConfigurationProperty<Integer> MAX_LENGTH = ConfigurationProperty.integer("max_length");
    public static final ConfigurationProperty<Block> BLOCK = ConfigurationProperty.block("block");
    public static final ConfigurationProperty<Block> TIP_BLOCK = ConfigurationProperty.block("tip_block");
    public static final ConfigurationProperty<VineType> VINE_TYPE = ConfigurationProperty.property("vine_type", VineType.class);

    public VinesGenFeature(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
        this.register(QUANTITY, MAX_LENGTH, VERTICAL_SPREAD, RAY_DISTANCE, BLOCK, TIP_BLOCK, VINE_TYPE, FRUITING_RADIUS);
    }

    @Override
    public GenFeatureConfiguration createDefaultConfiguration() {
        return super.createDefaultConfiguration()
                .with(QUANTITY, 4)
                .with(MAX_LENGTH, 8)
                .with(VERTICAL_SPREAD, 60f)
                .with(RAY_DISTANCE, 5f)
                .with(BLOCK, Blocks.VINE)
                .with(TIP_BLOCK, null)
                .with(VINE_TYPE, VineType.SIDE)
                .with(FRUITING_RADIUS, -1);
    }

    @Override
    protected boolean postGenerate(GenFeatureConfiguration configuration, PostGenerationContext context) {
        if (!context.isWorldGen() || context.endPoints().isEmpty()) {
            return false;
        }

        final VineType vineType = configuration.get(VINE_TYPE);
        final int quantity = configuration.get(QUANTITY);

        for (int i = 0; i < quantity; i++) {
            final BlockPos endPoint = context.endPoints().get(context.random().nextInt(context.endPoints().size()));

            switch (vineType) {
                case SIDE:
                    this.addSideVines(configuration, context.world(), context.species(), context.pos(), endPoint, context.bounds(), true);
                    break;
                case CEILING:
                case FLOOR:
                    this.addVerticalVines(configuration, context.world(), context.species(), context.pos(), endPoint, context.bounds(), true);
                    break;
            }
        }

        return true;
    }

    @Override
    protected boolean postGrow(GenFeatureConfiguration configuration, PostGrowContext context) {
        final Level world = context.world();
        final BlockPos rootPos = context.pos();
        final Species species = context.species();
        final int fruitingRadius = configuration.get(FRUITING_RADIUS);

        if (fruitingRadius < 0 || context.fertility() < 1) {
            return false;
        }

        final BlockState blockState = world.getBlockState(context.treePos());
        final BranchBlock branch = TreeHelper.getBranch(blockState);

        if (branch != null && branch.getRadius(blockState) >= fruitingRadius && context.natural()) {
            if (species.seasonalFruitProductionFactor(world, rootPos) > world.random.nextFloat()) {
                final FindEndsNode endFinder = new FindEndsNode();
                TreeHelper.startAnalysisFromRoot(world, rootPos, new MapSignal(endFinder));
                final List<BlockPos> endPoints = endFinder.getEnds();
                final int qty = configuration.get(QUANTITY);

                if (!endPoints.isEmpty()) {
                    for (int i = 0; i < qty; i++) {
                        BlockPos endPoint = endPoints.get(world.getRandom().nextInt(endPoints.size()));
                        if (configuration.get(VINE_TYPE) == VineType.SIDE) {
                            this.addSideVines(configuration, world, species, rootPos, endPoint, SafeChunkBounds.ANY, false);
                        } else {
                            this.addVerticalVines(configuration, world, species, rootPos, endPoint, SafeChunkBounds.ANY, false);
                        }
                    }
                    return true;
                }
            }
        }

        return true;
    }

    protected void addSideVines(GenFeatureConfiguration configuration, LevelAccessor world, Species species, BlockPos rootPos, BlockPos branchPos, SafeChunkBounds safeBounds, boolean worldgen) {
        // Uses branch ray tracing to find a place on the side of the tree to begin generating vines.
        final BlockHitResult result = CoordUtils.branchRayTrace(world, species, rootPos, branchPos, 90, configuration.get(VERTICAL_SPREAD), configuration.get(RAY_DISTANCE), safeBounds);

        if (result == null) {
            return;
        }

        final BlockPos vinePos = result.getBlockPos().relative(result.getDirection());
        if (vinePos == BlockPos.ZERO || !safeBounds.inBounds(vinePos, true)) {
            return;
        }

        final BooleanProperty vineSide = this.sideVineStates[result.getDirection().getOpposite().ordinal()];
        if (vineSide == null) {
            return;
        }

        final BlockState vineState = configuration.get(BLOCK).defaultBlockState().setValue(vineSide, true);
        this.placeVines(world, vinePos, vineState, configuration.get(MAX_LENGTH), null, configuration.get(VINE_TYPE), worldgen);
    }

    protected void addVerticalVines(GenFeatureConfiguration configuration, LevelAccessor world, Species species, BlockPos rootPos, BlockPos branchPos, SafeChunkBounds safeBounds, boolean worldgen) {
        // Uses fruit ray trace method to grab a position under the tree's leaves.
        BlockPos vinePos = CoordUtils.getRayTraceFruitPos(world, species, rootPos, branchPos, safeBounds);

        if (!safeBounds.inBounds(vinePos, true)) {
            return;
        }

        if (configuration.get(VINE_TYPE) == VineType.FLOOR) {
            vinePos = this.findGround(world, vinePos);
        }

        if (vinePos == BlockPos.ZERO) {
            return;
        }

        this.placeVines(world, vinePos, configuration.get(BLOCK).defaultBlockState(),
                configuration.get(MAX_LENGTH),
                configuration.getAsOptional(TIP_BLOCK)
                        .map(block -> block.defaultBlockState().setValue(GrowingPlantHeadBlock.AGE, worldgen ? 25 : 0))
                        .orElse(null),
                configuration.get(VINE_TYPE), worldgen);
    }

    // This is WIP (and isn't needed in the base mod anyway, as well as the fact that there's almost certainly a better way of doing this).
    private BlockPos findGround(LevelAccessor world, BlockPos vinePos) {
        BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos(vinePos.getX(), vinePos.getY(), vinePos.getZ());
        do {
            mPos.move(Direction.DOWN);
            if (mPos.getY() <= 0) {
                return BlockPos.ZERO;
            }
        } while (world.isEmptyBlock(vinePos) || world.getBlockState(vinePos).getBlock() instanceof DynamicLeavesBlock);

        return mPos.above();
    }

    protected void placeVines(LevelAccessor world, BlockPos vinePos, BlockState vinesState, int maxLength, @Nullable BlockState tipState, VineType vineType, boolean worldgen) {
        // Generate a random length for the vine.
        final int len = worldgen ? Mth.clamp(world.getRandom().nextInt(maxLength) + 3, 3, maxLength) : 1;
        final BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos(vinePos.getX(), vinePos.getY(), vinePos.getZ());

        tipState = tipState == null ? vinesState : tipState;

        for (int i = 0; i < len; i++) {
            if (world.isEmptyBlock(mPos)) {
                // Set the current block either to a vine block or a tip block if it's set.
                world.setBlock(mPos, (i == len - 1) ? tipState : vinesState, 3);
                // Move current position down/up depending on vine type.
                mPos.setY(mPos.getY() + (vineType == VineType.FLOOR ? 1 : -1));
            } else {
                if (i > 0 && vineType != VineType.SIDE) {
                    mPos.setY(mPos.getY() + (vineType == VineType.FLOOR ? -1 : 1)); //if the vine is cut short set the tip on the last block
                    world.setBlock(mPos, tipState, 3);
                }
                break;
            }
        }
    }

}
