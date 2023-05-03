package io.github.steveplays28.dynamictreesfabric.blocks.rootyblocks;

import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.api.data.WaterRootGenerator;
import io.github.steveplays28.dynamictreesfabric.api.registry.TypedRegistry;
import io.github.steveplays28.dynamictreesfabric.blocks.branches.BranchBlock;
import io.github.steveplays28.dynamictreesfabric.init.DTConfigs;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

/**
 * @author Max Hyper
 */
public class WaterSoilProperties extends SoilProperties {

    public static final TypedRegistry.EntryType<SoilProperties> TYPE = TypedRegistry.newType(WaterSoilProperties::new);

    public WaterSoilProperties(final Identifier registryName) {
        super(null, registryName);

        this.soilStateGenerator.reset(WaterRootGenerator::new);
    }

    @Override
    protected RootyBlock createBlock(AbstractBlock.Settings blockProperties) {
        return new RootyWaterBlock(this, blockProperties);
    }

    @Override
    public Material getDefaultMaterial() {
        return Material.WATER;
    }

    @Override
    public AbstractBlock.Settings getDefaultBlockProperties(Material material, MapColor materialColor) {
        return AbstractBlock.Settings.copy(Blocks.WATER);
    }

    public static class RootyWaterBlock extends RootyBlock implements Waterloggable {

        protected static final Box WATER_ROOTS_AABB = new Box(0.1, 0.0, 0.1, 0.9, 1.0, 0.9);
        public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

        public RootyWaterBlock(SoilProperties properties, Settings blockProperties) {
            super(properties, blockProperties);
            setDefaultState(getDefaultState().with(WATERLOGGED, true));
        }

        @Override
        protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
            super.appendProperties(builder.add(WATERLOGGED));
        }

        @Override
        public int getRadiusForConnection(BlockState state, BlockView reader, BlockPos pos, BranchBlock from, Direction side, int fromRadius) {
            return 1;
        }

        @Override
        public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockView world, BlockPos pos, PlayerEntity player) {
            BlockState upState = world.getBlockState(pos.up());
            if (TreeHelper.isBranch(upState)) {
                return TreeHelper.getBranch(upState).getFamily().getBranchItem()
                        .map(ItemStack::new)
                        .orElse(ItemStack.EMPTY);
            }
            return ItemStack.EMPTY;
        }

        @Override
        public float getHardness(BlockState state, BlockView worldIn, BlockPos pos) {
            return (float) (0.5 * DTConfigs.ROOTY_BLOCK_HARDNESS_MULTIPLIER.get());
        }

        @Override
        public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
            return VoxelShapes.cuboid(WATER_ROOTS_AABB);
        }

        @Override
        public VoxelShape getSidesShape(BlockState state, BlockView reader, BlockPos pos) {
            return VoxelShapes.empty();
        }

        @Override
        public boolean canReplace(BlockState state, ItemPlacementContext useContext) {
            return false;
        }

        @Override
        public FluidState getFluidState(BlockState state) {
            return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
        }

        @Override
        public BlockState getStateForNeighborUpdate(BlockState stateIn, Direction facing, BlockState facingState, WorldAccess worldIn, BlockPos currentPos, BlockPos facingPos) {
            if (stateIn.get(WATERLOGGED)) {
                worldIn.scheduleFluidTick(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
            }
            return super.getStateForNeighborUpdate(stateIn, facing, facingState, worldIn, currentPos, facingPos);
        }

        @Override
        public BlockState getDecayBlockState(BlockState state, BlockView access, BlockPos pos) {
            if (state.contains(WATERLOGGED) && !state.get(WATERLOGGED)) {
                return Blocks.AIR.getDefaultState();
            }
            return super.getDecayBlockState(state, access, pos);
        }

        ///////////////////////////////////////////
        // RENDERING
        ///////////////////////////////////////////

        @Override
        public boolean getColorFromBark() {
            return true;
        }

        public boolean fallWithTree(BlockState state, World world, BlockPos pos) {
            //The block is removed when this is checked because it means it got attached to a tree
            world.setBlockState(pos, getDecayBlockState(state, world, pos));
            return true;
        }

    }

}
