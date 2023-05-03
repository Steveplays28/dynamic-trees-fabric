package io.github.steveplays28.dynamictreesfabric.blocks;

import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.blocks.branches.BranchBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CocoaBlock;
import net.minecraft.block.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;

public class DynamicCocoaBlock extends CocoaBlock {

    public DynamicCocoaBlock() {
        super(Block.Properties.of(Material.PLANT)
                .ticksRandomly()
                .strength(0.2F, 3.0F)
                .sounds(BlockSoundGroup.WOOD));
    }

    /**
     * Can this block stay at this position.  Similar to canPlaceBlockAt except gets checked often with plants.
     */
    public boolean canPlaceAt(BlockState state, WorldView worldIn, BlockPos pos) {
        final BlockState logState = worldIn.getBlockState(pos.offset(state.get(FACING)));
        final BranchBlock branch = TreeHelper.getBranch(logState);
        return branch != null && branch.getRadius(logState) == 8 && branch.getFamily().canSupportCocoa;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockView world, BlockPos pos, PlayerEntity player) {
        return new ItemStack(Items.COCOA_BEANS);
    }

}
