package io.github.steveplays28.dynamictreesfabric.entities.animation;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.blocks.branches.BranchBlock;
import io.github.steveplays28.dynamictreesfabric.blocks.branches.TrunkShellBlock;
import io.github.steveplays28.dynamictreesfabric.entities.FallingTreeEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class PhysicsAnimationHandler implements AnimationHandler {
	@Override
	public String getName() {
		return "physics";
	}

	HandlerData getData(FallingTreeEntity entity) {
		return entity.dataAnimationHandler instanceof HandlerData ? (HandlerData) entity.dataAnimationHandler : new HandlerData();
	}

	@Override
	public void initMotion(FallingTreeEntity entity) {
		entity.dataAnimationHandler = new HandlerData();
		final BlockPos cutPos = entity.getDestroyData().cutPos;

		final long seed = entity.world.random.nextLong();
		final Random random = Random.create(seed ^ (((long) cutPos.getX()) << 32 | ((long) cutPos.getZ())));
		final float mass = entity.getDestroyData().woodVolume.getVolume();
		final float inertialMass = MathHelper.clamp(mass, 1, 3);
		entity.setVelocity(entity.getVelocity().x / inertialMass,
				entity.getVelocity().y / inertialMass, entity.getVelocity().z / inertialMass);

		this.getData(entity).rotPit = (random.nextFloat() - 0.5f) * 4 / inertialMass;
		this.getData(entity).rotYaw = (random.nextFloat() - 0.5f) * 4 / inertialMass;

		final double motionToAdd = entity.getDestroyData().cutDir.getOpposite().getOffsetX() * 0.1;
		entity.setVelocity(entity.getVelocity().add(motionToAdd, motionToAdd, motionToAdd));

		FallingTreeEntity.standardDropLeavesPayLoad(entity); // Seeds and stuff fall out of the tree before it falls over.
	}

	@Override
	public void handleMotion(FallingTreeEntity entity) {
		if (entity.landed) {
			return;
		}

		entity.setVelocity(entity.getVelocity().x, entity.getVelocity().y - AnimationConstants.TREE_GRAVITY, entity.getVelocity().z);

		// Create drag in air.
		entity.setVelocity(entity.getVelocity().x * 0.98f, entity.getVelocity().y * 0.98f,
				entity.getVelocity().z * 0.98f);
		this.getData(entity).rotYaw *= 0.98f;
		this.getData(entity).rotPit *= 0.98f;

		// Apply motion.
		entity.setPosition(entity.getX() + entity.getVelocity().x, entity.getY() + entity.getVelocity().y,
				entity.getZ() + entity.getVelocity().z);
		entity.setPitch(MathHelper.wrapDegrees(entity.getPitch() + getData(entity).rotPit));
		entity.setYaw(MathHelper.wrapDegrees(entity.getYaw() + getData(entity).rotYaw));

		int radius = 8;
		if (entity.getDestroyData().getNumBranches() <= 0) {
			return;
		}
		final BlockState state = entity.getDestroyData().getBranchBlockState(0);
		if (TreeHelper.isBranch(state)) {
			radius = ((BranchBlock) state.getBlock()).getRadius(state);
		}

		final World world = entity.world;
		final Box fallBox = new Box(entity.getX() - radius, entity.getY(), entity.getZ() - radius, entity.getX() + radius, entity.getY() + 1.0, entity.getZ() + radius);
		final BlockPos pos = new BlockPos(entity.getX(), entity.getY(), entity.getZ());
		final BlockState collState = world.getBlockState(pos);

		if (!TreeHelper.isLeaves(collState) && !TreeHelper.isBranch(collState) && !(collState.getBlock() instanceof TrunkShellBlock)) {
			if (collState.getBlock() instanceof FluidBlock) {
				// Undo the gravity.
				entity.setVelocity(entity.getVelocity().add(0, AnimationConstants.TREE_GRAVITY, 0));
				// Create drag in liquid.
				entity.setVelocity(entity.getVelocity().multiply(0.8f, 0.8f, 0.8f));
				this.getData(entity).rotYaw *= 0.8f;
				this.getData(entity).rotPit *= 0.8f;
				// Add a little buoyancy.
				entity.setVelocity(entity.getVelocity().add(0, 0.01, 0));
				entity.onFire = false;
			} else {
				final VoxelShape shape = collState.getSidesShape(world, pos);
				Box collBox = new Box(0, 0, 0, 0, 0, 0);
				if (!shape.isEmpty()) {
					collBox = collState.getSidesShape(world, pos).getBoundingBox();
				}

				collBox = collBox.offset(pos);
				if (fallBox.intersects(collBox)) {
					entity.setVelocity(entity.getVelocity().x, 0, entity.getVelocity().z);
					entity.setPosition(entity.getX(), collBox.maxY, entity.getZ());
					entity.prevY = entity.getY();
					entity.landed = true;
					entity.setOnGround(true);
					if (entity.onFire) {
						if (entity.world.isAir(pos.up())) {
							entity.world.setBlockState(pos.up(), Blocks.FIRE.getDefaultState());
						}
					}
				}
			}
		}

	}

	@Override
	public void dropPayload(FallingTreeEntity entity) {
		final World world = entity.world;
		entity.getPayload().forEach(i -> Block.dropStack(world, new BlockPos(entity.getX(), entity.getY(), entity.getZ()), i));
		entity.getDestroyData().leavesDrops.forEach(bis -> Block.dropStack(world, entity.getDestroyData().cutPos.add(bis.pos), bis.stack));
	}

	public boolean shouldDie(FallingTreeEntity entity) {
		final boolean dead = entity.landed || entity.age > 120;

		if (dead) {
			entity.cleanupRootyDirt();
		}

		return dead;
	}

	@Override

@Environment(EnvType.CLIENT)
	public void renderTransform(FallingTreeEntity entity, float entityYaw, float partialTicks, MatrixStack matrixStack) {
		final float yaw = MathHelper.wrapDegrees(io.github.steveplays28.dynamictreesfabric.util.MathHelper.angleDegreesInterpolate(entity.prevYaw, entity.getYaw(), partialTicks));
		final float pit = MathHelper.wrapDegrees(io.github.steveplays28.dynamictreesfabric.util.MathHelper.angleDegreesInterpolate(entity.prevPitch, entity.getPitch(), partialTicks));

		final Vec3d mc = entity.getMassCenter();
		matrixStack.translate(mc.x, mc.y, mc.z);
		matrixStack.multiply(new Quaternion(new Vector3f(0, 1, 0), -yaw, true));
		matrixStack.multiply(new Quaternion(new Vector3f(1, 0, 0), pit, true));
		matrixStack.translate(-mc.x - 0.5, -mc.y, -mc.z - 0.5);

	}

	@Override

@Environment(EnvType.CLIENT)
	public boolean shouldRender(FallingTreeEntity entity) {
		return true;
	}

	static class HandlerData extends DataAnimationHandler {
		float rotYaw = 0;
		float rotPit = 0;
	}
}
