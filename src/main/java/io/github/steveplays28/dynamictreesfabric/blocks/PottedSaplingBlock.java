package io.github.steveplays28.dynamictreesfabric.blocks;

import io.github.steveplays28.dynamictreesfabric.tileentity.PottedSaplingTileEntity;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import io.github.steveplays28.dynamictreesfabric.util.BlockStates;
import io.github.steveplays28.dynamictreesfabric.util.ItemUtils;
import io.github.steveplays28.dynamictreesfabric.util.Null;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.block.Material;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.level.block.*;
import javax.annotation.Nullable;

@SuppressWarnings("deprecation")
public class PottedSaplingBlock extends BlockWithEntity {

    public static final Identifier REG_NAME = io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("potted_sapling");

    protected static final Box FLOWER_POT_AABB = new Box(0.3125D, 0.0D, 0.3125D, 0.6875D, 0.375D, 0.6875D);

    public PottedSaplingBlock() {
        super(Block.Properties.of(Material.DECORATION).breakInstantly().nonOpaque());
    }

    //////////////////////////////
    // Properties
    //////////////////////////////

    public Species getSpecies(BlockView world, BlockPos pos) {
        return Null.applyIfNonnull(this.getTileEntityPottedSapling(world, pos),
                PottedSaplingTileEntity::getSpecies, Species.NULL_SPECIES);
    }

    public boolean setSpecies(World world, BlockPos pos, BlockState state, Species species) {
        return Null.consumeIfNonnull(this.getTileEntityPottedSapling(world, pos),
                pottedSaplingTileEntity -> pottedSaplingTileEntity.setSpecies(species));
    }

    public BlockState getPotState(World world, BlockPos pos) {
        return Null.applyIfNonnull(this.getTileEntityPottedSapling(world, pos),
                PottedSaplingTileEntity::getPot, Blocks.FLOWER_POT.getDefaultState());
    }

    public boolean setPotState(World world, BlockState potState, BlockPos pos) {
        return Null.consumeIfNonnull(this.getTileEntityPottedSapling(world, pos),
                pottedSaplingTileEntity -> pottedSaplingTileEntity.setPot(potState));
    }


    ///////////////////////////////////////////
    // TILE ENTITY
    ///////////////////////////////////////////

    @Nullable
    private PottedSaplingTileEntity getTileEntityPottedSapling(BlockView world, BlockPos pos) {
        final BlockEntity tileEntity = world.getBlockEntity(pos);
        return tileEntity instanceof PottedSaplingTileEntity ? (PottedSaplingTileEntity) tileEntity : null;
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pPos, BlockState pState) {
        return new PottedSaplingTileEntity(pPos,pState);
    }


    ///////////////////////////////////////////
    // INTERACTION
    ///////////////////////////////////////////

    // Unlike a regular flower pot this is only used to eject the contents.
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        final ItemStack heldItem = player.getStackInHand(hand);
        final Species species = this.getSpecies(world, pos);

        if (!species.isValid()) {
            return ActionResult.FAIL;
        }

        final ItemStack seedStack = species.getSeedStack(1);

        // If they are holding the seed do not empty the pot.
        if (heldItem.getItem() == seedStack.getItem() || (hand == Hand.OFF_HAND &&
                player.getStackInHand(Hand.MAIN_HAND).getItem() == seedStack.getItem())) {
            return ActionResult.PASS;
        }

        if (heldItem.isEmpty()) {
            // If they're holding nothing, put it in their hand.
            player.setStackInHand(hand, seedStack);
            // Otherwise try to add it to their inventory.
        } else if (!player.giveItemStack(seedStack)) {
            // If their inventory is full, drop it instead.
            player.dropItem(seedStack, false);
        }

        // Set the block back to the original pot state.
        world.setBlockState(pos, this.getPotState(world, pos), 3);

        return ActionResult.success(world.isClient);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockView world, BlockPos pos, PlayerEntity player) {

        if (target.getType() == HitResult.Type.BLOCK && ((BlockHitResult) target).getSide() == Direction.UP) {
            final Species species = this.getSpecies(world, pos);
            if (species.isValid()) {
                return species.getSeedStack(1);
            }
        }

        final BlockState potState = Null.applyIfNonnull(this.getTileEntityPottedSapling(world, pos),
                PottedSaplingTileEntity::getPot, BlockStates.AIR);

        if (potState.getBlock() == Blocks.FLOWER_POT) {
            return new ItemStack(Items.FLOWER_POT);
        }

        if (potState.getBlock() instanceof FlowerPotBlock) {
            return new ItemStack(potState.getBlock(), 1);
        }

        return new ItemStack(Items.FLOWER_POT);
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!world.getBlockState(pos.down()).isSideSolidFullSquare(world, pos, Direction.UP)) {
            this.spawnDrops(world, pos);
            world.setBlockState(pos, BlockStates.AIR);
        }
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, FluidState fluid) {
        if (willHarvest) {
            return true; // If it will harvest, delay deletion of the block until after getDrops.
        }

        return super.onDestroyedByPlayer(state, world, pos, player, willHarvest, fluid);
    }

    @Override
    public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity te, ItemStack stack) {
        super.afterBreak(world, player, pos, state, te, stack);
        this.spawnDrops(world, pos);
        world.setBlockState(pos, Blocks.AIR.getDefaultState());
    }

    public void spawnDrops(World world, BlockPos pos) {
        ItemUtils.spawnItemStack(world, pos, new ItemStack(Blocks.FLOWER_POT), false);
        if (this.getSpecies(world, pos) != Species.NULL_SPECIES) { // Safety check in case for whatever reason the species was not set.
            ItemUtils.spawnItemStack(world, pos, this.getSpecies(world, pos).getSeedStack(1), false);
        }
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType pathType) {
        return false;
    }

    ///////////////////////////////////////////
    // PHYSICAL BOUNDS
    ///////////////////////////////////////////

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
        return VoxelShapes.cuboid(FLOWER_POT_AABB);
    }


    ///////////////////////////////////////////
    // RENDERING
    ///////////////////////////////////////////

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

}
