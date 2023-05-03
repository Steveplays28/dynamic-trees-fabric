package io.github.steveplays28.dynamictreesfabric.blocks.leaves;

import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/**
 * An extension of {@link DynamicLeavesBlock} which makes the block solid. This means that it can be landed on like
 * normal and gives fall damage, is a full cube, and isn't made passable when the config option is enabled.
 */
public class SolidDynamicLeavesBlock extends DynamicLeavesBlock {

	public SolidDynamicLeavesBlock(final LeavesProperties leavesProperties, final Settings properties) {
		super(leavesProperties, properties);
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
		return VoxelShapes.fullCube();
	}

	@Override
	public boolean addLandingEffects(BlockState state1, ServerWorld worldserver, BlockPos pos, BlockState state2, LivingEntity entity, int numberOfParticles) {
		return false;
	}

	@Override
	public void onLandedUpon(World world, BlockState blockstate, BlockPos pos, Entity entity, float fallDistance) {
		entity.handleFallDamage(fallDistance, 1.0F, DamageSource.FALLING_BLOCK);
	}

	@Override
	public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
	}

}
