package io.github.steveplays28.dynamictreesfabric.entities.animation;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.blocks.branches.BranchBlock;
import io.github.steveplays28.dynamictreesfabric.entities.FallingTreeEntity;
import io.github.steveplays28.dynamictreesfabric.init.DTConfigs;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;

public class FalloverAnimationHandler implements AnimationHandler {

	/**
	 *
	 */
	public static boolean intersects(Box axisAlignedBB, Vec3d vec3d, Vec3d otherVec3d) {
		return axisAlignedBB.intersects(Math.min(vec3d.x, otherVec3d.x), Math.min(vec3d.y, otherVec3d.y), Math.min(vec3d.z, otherVec3d.z), Math.max(vec3d.x, otherVec3d.x), Math.max(vec3d.y, otherVec3d.y), Math.max(vec3d.z, otherVec3d.z));
	}

	@Override
	public String getName() {
		return "fallover";
	}

	HandlerData getData(FallingTreeEntity entity) {
		return entity.dataAnimationHandler != null ? (HandlerData) entity.dataAnimationHandler : new HandlerData();
	}

	@Override
	public void initMotion(FallingTreeEntity entity) {
		entity.dataAnimationHandler = new HandlerData();
		FallingTreeEntity.standardDropLeavesPayLoad(entity);//Seeds and stuff fall out of the tree before it falls over

		BlockPos belowBlock = entity.getDestroyData().cutPos.down();
		if (entity.world.getBlockState(belowBlock).isSideSolidFullSquare(entity.world, belowBlock, Direction.UP)) {
			entity.setOnGround(true);
			return;
		}
	}

	@Override
	public void handleMotion(FallingTreeEntity entity) {

		float fallSpeed = getData(entity).fallSpeed;

		if (entity.isOnGround()) {
			float height = (float) entity.getMassCenter().y * 2;
			fallSpeed += (0.2 / height);
			addRotation(entity, fallSpeed);
		}

		entity.setVelocity(entity.getVelocity().x, entity.getVelocity().y - AnimationConstants.TREE_GRAVITY, entity.getVelocity().z);
		entity.setPosition(entity.getX(), entity.getY() + entity.getVelocity().y, entity.getZ());

		{//Handle entire entity falling and collisions with it's base and the ground
			World world = entity.world;
			int radius = 8;
			BlockState state = entity.getDestroyData().getBranchBlockState(0);
			if (TreeHelper.isBranch(state)) {
				radius = ((BranchBlock) state.getBlock()).getRadius(state);
			}
			Box fallBox = new Box(entity.getX() - radius, entity.getY(), entity.getZ() - radius, entity.getX() + radius, entity.getY() + 1.0, entity.getZ() + radius);
			BlockPos pos = new BlockPos(entity.getX(), entity.getY(), entity.getZ());
			BlockState collState = world.getBlockState(pos);

			VoxelShape shape = collState.getSidesShape(world, pos);
			Box collBox = new Box(0, 0, 0, 0, 0, 0);
			if (!shape.isEmpty()) {
				collBox = collState.getSidesShape(world, pos).getBoundingBox();
			}

			collBox = collBox.offset(pos);
			if (fallBox.intersects(collBox)) {
				entity.setVelocity(entity.getVelocity().x, 0, entity.getVelocity().z);
				entity.setPosition(entity.getX(), collBox.maxY, entity.getZ());
				entity.prevY = entity.getY();
				entity.setOnGround(true);
			}
		}

		if (fallSpeed > 0 && testCollision(entity)) {
			addRotation(entity, -fallSpeed);//pull back to before the collision
			getData(entity).bounces++;
			fallSpeed *= -AnimationConstants.TREE_ELASTICITY;//bounce with elasticity
			entity.landed = Math.abs(fallSpeed) < 0.02f;//The entity has landed if after a bounce it has little velocity
		}

		//Crush living things with clumsy dead trees
		World world = entity.world;
		if (DTConfigs.ENABLE_FALLING_TREE_DAMAGE.get() && !world.isClient) {
			List<LivingEntity> elist = testEntityCollision(entity);
			for (LivingEntity living : elist) {
				if (!getData(entity).entitiesHit.contains(living)) {
					getData(entity).entitiesHit.add(living);
					float damage = entity.getDestroyData().woodVolume.getVolume() * Math.abs(fallSpeed) * 3f;
					if (getData(entity).bounces == 0 && damage > 2) {
						//System.out.println("damage: " + damage);
						living.setVelocity(
								living.getVelocity().x + (world.random.nextFloat() * entity.getDestroyData().toolDir.getOpposite().getOffsetX() * damage * 0.2f),
								living.getVelocity().y + (world.random.nextFloat() * fallSpeed * 0.25f),
								living.getVelocity().z + (world.random.nextFloat() * entity.getDestroyData().toolDir.getOpposite().getOffsetZ() * damage * 0.2f));
						living.setVelocity(living.getVelocity().x + (world.random.nextFloat() - 0.5), living.getVelocity().y, living.getVelocity().z + (world.random.nextFloat() - 0.5));
						damage *= DTConfigs.FALLING_TREE_DAMAGE_MULTIPLIER.get();
						//System.out.println("Tree Falling Damage: " + damage + "/" + living.getHealth());
						living.damage(AnimationConstants.TREE_DAMAGE, damage);
					}
				}
			}
		}

		getData(entity).fallSpeed = fallSpeed;
	}

	/**
	 * This tests a bounding box cube for each block of the trunk. Processing is approximately equivalent to the same
	 * number of {@link net.minecraft.entity.ItemEntity}s in the world.
	 *
	 * @param entity
	 * @return true if collision is detected
	 */
	private boolean testCollision(FallingTreeEntity entity) {
		Direction toolDir = entity.getDestroyData().toolDir;

		float actingAngle = toolDir.getAxis() == Direction.Axis.X ? entity.getYaw() : entity.getPitch();

		int offsetX = toolDir.getOffsetX();
		int offsetZ = toolDir.getOffsetZ();
		float h = MathHelper.sin((float) Math.toRadians(actingAngle)) * (offsetX | offsetZ);
		float v = MathHelper.cos((float) Math.toRadians(actingAngle));
		float xbase = (float) (entity.getX() + offsetX * (-(0.5f) + (v * 0.5f) + (h * 0.5f)));
		float ybase = (float) (entity.getY() - (h * 0.5f) + (v * 0.5f));
		float zbase = (float) (entity.getZ() + offsetZ * (-(0.5f) + (v * 0.5f) + (h * 0.5f)));

		int trunkHeight = entity.getDestroyData().trunkHeight;
		float maxRadius = entity.getDestroyData().getBranchRadius(0) / 16.0f;

		trunkHeight = Math.min(trunkHeight, 24);

		for (int segment = 0; segment < trunkHeight; segment++) {
			float segX = xbase + h * segment * offsetX;
			float segY = ybase + v * segment;
			float segZ = zbase + h * segment * offsetZ;
			float tex = 0.0625f;
			float half = MathHelper.clamp(tex * (segment + 1) * 2, tex, maxRadius);
			Box testBB = new Box(segX - half, segY - half, segZ - half, segX + half, segY + half, segZ + half);

			if (!entity.world.isSpaceEmpty(entity, testBB)) {
				return true;
			}
		}

		return false;
	}

	private void addRotation(FallingTreeEntity entity, float delta) {
		Direction toolDir = entity.getDestroyData().toolDir;

		switch (toolDir) {
			case NORTH:
				entity.setPitch(entity.getPitch() + delta);
				break;
			case SOUTH:
				entity.setPitch(entity.getPitch() - delta);
				break;
			case WEST:
				entity.setYaw(entity.getYaw() + delta);
				break;
			case EAST:
				entity.setYaw(entity.getYaw() - delta);
				break;
			default:
				break;
		}

		entity.setPitch(MathHelper.wrapDegrees(entity.getPitch()));
		entity.setYaw(MathHelper.wrapDegrees(entity.getYaw()));
	}

	public List<LivingEntity> testEntityCollision(FallingTreeEntity entity) {

		World world = entity.world;

		Direction toolDir = entity.getDestroyData().toolDir;

		float actingAngle = toolDir.getAxis() == Direction.Axis.X ? entity.getYaw() : entity.getPitch();

		int offsetX = toolDir.getOffsetX();
		int offsetZ = toolDir.getOffsetZ();
		float h = MathHelper.sin((float) Math.toRadians(actingAngle)) * (offsetX | offsetZ);
		float v = MathHelper.cos((float) Math.toRadians(actingAngle));
		float xbase = (float) (entity.getX() + offsetX * (-(0.5f) + (v * 0.5f) + (h * 0.5f)));
		float ybase = (float) (entity.getY() - (h * 0.5f) + (v * 0.5f));
		float zbase = (float) (entity.getZ() + offsetZ * (-(0.5f) + (v * 0.5f) + (h * 0.5f)));
		int trunkHeight = entity.getDestroyData().trunkHeight;
		float segX = xbase + h * (trunkHeight - 1) * offsetX;
		float segY = ybase + v * (trunkHeight - 1);
		float segZ = zbase + h * (trunkHeight - 1) * offsetZ;

		float maxRadius = entity.getDestroyData().getBranchRadius(0) / 16.0f;

		Vec3d vec3d1 = new Vec3d(xbase, ybase, zbase);
		Vec3d vec3d2 = new Vec3d(segX, segY, segZ);

		return world.getOtherEntities(entity, new Box(vec3d1.x, vec3d1.y, vec3d1.z, vec3d2.x, vec3d2.y, vec3d2.z),
				entity1 -> {
					if (entity1 instanceof LivingEntity && entity1.canHit()) {
						Box axisalignedbb = entity1.getBoundingBox().expand(maxRadius);
						return axisalignedbb.contains(vec3d1) || intersects(axisalignedbb, vec3d1, vec3d2);
					}
					return false;
				}
		).stream().map(a -> (LivingEntity) a).collect(Collectors.toList());

	}

	@Override
	public void dropPayload(FallingTreeEntity entity) {
		World world = entity.world;
		BlockPos cutPos = entity.getDestroyData().cutPos;
		entity.getPayload().forEach(i -> Block.dropStack(world, cutPos, i));
	}

	@Override
	public boolean shouldDie(FallingTreeEntity entity) {

		boolean dead =
				Math.abs(entity.getPitch()) >= 160 ||
						Math.abs(entity.getYaw()) >= 160 ||
						entity.landed ||
						entity.age > 120 + (entity.getDestroyData().trunkHeight);

		//Force the Rooty Dirt to update if it's there.  Turning it back to dirt.
		if (dead) {
			entity.cleanupRootyDirt();
		}

		return dead;
	}

	@Override
	import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
	public void renderTransform(FallingTreeEntity entity, float entityYaw, float partialTicks, MatrixStack matrixStack) {

		float yaw = MathHelper.wrapDegrees(io.github.steveplays28.dynamictreesfabric.util.MathHelper.angleDegreesInterpolate(entity.prevYaw, entity.getYaw(), partialTicks));
		float pit = MathHelper.wrapDegrees(io.github.steveplays28.dynamictreesfabric.util.MathHelper.angleDegreesInterpolate(entity.prevPitch, entity.getPitch(), partialTicks));

		//Vec3d mc = entity.getMassCenter();

		int radius = entity.getDestroyData().getBranchRadius(0);

		Direction toolDir = entity.getDestroyData().toolDir;
		Vec3d toolVec = new Vec3d(toolDir.getOffsetX(), toolDir.getOffsetY(), toolDir.getOffsetZ()).multiply(radius / 16.0f);

		matrixStack.translate(-toolVec.x, -toolVec.y, -toolVec.z);
		matrixStack.multiply(new Quaternion(new Vector3f(0, 0, 1), -yaw, true));
		matrixStack.multiply(new Quaternion(new Vector3f(1, 0, 0), pit, true));
		matrixStack.translate(toolVec.x, toolVec.y, toolVec.z);

		matrixStack.translate(-0.5, 0, -0.5);

	}

	@Override
	import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
	public boolean shouldRender(FallingTreeEntity entity) {
		return true;
	}

	class HandlerData extends DataAnimationHandler {
		float fallSpeed = 0;
		int bounces = 0;
		HashSet<LivingEntity> entitiesHit = new HashSet<>();//A record of the entities that have taken damage to ensure they are only damaged a single time
	}

}
