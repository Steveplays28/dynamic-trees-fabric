package io.github.steveplays28.dynamictreesfabric.blocks.leaves;

import io.github.steveplays28.dynamictreesfabric.api.registry.TypedRegistry;
import io.github.steveplays28.dynamictreesfabric.blocks.branches.BranchBlock;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import io.github.steveplays28.dynamictreesfabric.util.CoordUtils;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;

public class PalmLeavesProperties extends LeavesProperties {

    public static final TypedRegistry.EntryType<LeavesProperties> TYPE = TypedRegistry.newType(PalmLeavesProperties::new);

    public PalmLeavesProperties(Identifier registryName) {
        super(registryName);
    }

    @Override
    protected DynamicLeavesBlock createDynamicLeaves(AbstractBlock.Settings properties) {
        return new DynamicPalmLeavesBlock(this, properties);
    }

    public static class DynamicPalmLeavesBlock extends DynamicLeavesBlock {

        public static final IntProperty DIRECTION = IntProperty.of("direction", 0, 8);

        public static final CoordUtils.Surround[][] hydroSurroundMap = new CoordUtils.Surround[][]{
                {}, //distance 0
                {CoordUtils.Surround.NE, CoordUtils.Surround.SE, CoordUtils.Surround.SW, CoordUtils.Surround.NW}, //distance 1
                {CoordUtils.Surround.N, CoordUtils.Surround.E, CoordUtils.Surround.S, CoordUtils.Surround.W}, //distance 2
                {}, //distance 3
                {}, //distance 4
                {}, //distance 5
                {}, //distance 6
                {}  //distance 7
        };

        @Override
        public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random rand) {
            if (state.getBlock() == this) {
                int dist = state.get(DISTANCE);
                if ((dist == 1 || dist == 2) && state.get(DIRECTION) == 0) {
                    world.setBlockState(pos, Blocks.AIR.getDefaultState());
                    return;
                }
            }
            super.randomTick(state, world, pos, rand);
        }

        public DynamicPalmLeavesBlock(LeavesProperties leavesProperties, Settings properties) {
            super(leavesProperties, properties);
            setDefaultState(getDefaultState().with(DIRECTION, 0));
        }

        @Override
        protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
            super.appendProperties(builder);
            builder.add(DIRECTION);
        }

        public static BlockState getDirectionState(BlockState state, CoordUtils.Surround surround) {
            if (state == null) {
                return null;
            }
            return state.with(DIRECTION, surround == null ? 0 : surround.ordinal() + 1);
        }

        @Override
        public int getRadiusForConnection(BlockState state, BlockView reader, BlockPos pos, BranchBlock from, Direction side, int fromRadius) {
            return side == Direction.UP && from.getFamily().isCompatibleDynamicLeaves(Species.NULL_SPECIES, state, reader, pos) ? fromRadius : 0;
        }

        @Override
        public int branchSupport(BlockState state, BlockView reader, BranchBlock branch, BlockPos pos, Direction dir, int radius) {
            return branch.getFamily() == getFamily(state, reader, pos) ? BranchBlock.setSupport(0, 1) : 0;
        }

        @Override
        public boolean appearanceChangesWithHydro(int oldHydro, int newHydro) {
            return true;
        }

        @Override
        public BlockState getLeavesBlockStateForPlacement(WorldAccess world, BlockPos pos, BlockState leavesStateWithHydro, int oldHydro, boolean worldGen) {
            for (CoordUtils.Surround surround : CoordUtils.Surround.values()) {
                BlockState offstate = world.getBlockState(pos.add(surround.getOffset()));
                if (offstate.getBlock() == this && offstate.get(DISTANCE) == 3) {
                    return getDirectionState(leavesStateWithHydro, surround);
                }
            }
            return leavesStateWithHydro;
        }

        @Override
        public VoxelShape getCullingShape(BlockState state, BlockView world, BlockPos pos) {
            Box base = super.getCullingShape(state, world, pos).getBoundingBox();
            base.expand(1, 0, 1);
            base.expand(-1, -0, -1);
            return VoxelShapes.cuboid(base);
        }

    }

}
