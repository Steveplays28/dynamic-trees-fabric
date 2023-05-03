package io.github.steveplays28.dynamictreesfabric.blocks;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import io.github.steveplays28.dynamictreesfabric.mixin.BlockAccessor;
import io.github.steveplays28.dynamictreesfabric.blocks.branches.BasicBranchBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

/**
 * An abstract class to allow for Blocks with dynamic hardness.
 * <p>
 * The main use of this class is {@link BasicBranchBlock}, as its hardness depends on the radius of the branch.
 *
 * @author Harley O'Connor
 */
public abstract class BlockWithDynamicHardness extends Block {

	public BlockWithDynamicHardness(Settings properties) {
		super(properties.hardness(2.0f));

		// Create and fill a new state container.
		final StateManager.Builder<Block, BlockState> builder = new StateManager.Builder<>(this);
		this.appendProperties(builder);

		// Set the state container to use our custom BlockState class.
		((BlockAccessor) this).setStateManager(builder.build(Block::getDefaultState, DynamicHardnessBlockState::new));

		// Sets the default state to the current default state, but with our new BlockState class.
		this.setDefaultState(this.stateManager.getDefaultState());
	}

	/**
	 * Sub-classes can override this method to return a hardness value that could, for example, depend on the {@link
	 * BlockState}.
	 *
	 * @param world An {@link BlockView} instance.
	 * @param pos   The {@link BlockPos}.
	 * @return The hardness value.
	 */
	public float getHardness(BlockState state, final BlockView world, final BlockPos pos) {
		return 2.0f;
	}

	/**
	 * Custom extension of {@link BlockState} to allow for dynamic hardness.
	 */
	protected final class DynamicHardnessBlockState extends BlockState {

		public DynamicHardnessBlockState(Block block, ImmutableMap<Property<?>, Comparable<?>> propertiesToValueMap, MapCodec<BlockState> codec) {
			super(block, propertiesToValueMap, codec);
		}

		@Override
		public float getHardness(BlockView worldIn, BlockPos pos) {
			return getHardness(this, worldIn, pos);
		}

	}

}
