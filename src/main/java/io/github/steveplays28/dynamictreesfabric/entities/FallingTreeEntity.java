package io.github.steveplays28.dynamictreesfabric.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Iterables;
import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.blocks.branches.TrunkShellBlock;
import io.github.steveplays28.dynamictreesfabric.blocks.rootyblocks.RootyBlock;
import io.github.steveplays28.dynamictreesfabric.entities.animation.AnimationHandler;
import io.github.steveplays28.dynamictreesfabric.entities.animation.AnimationHandlers;
import io.github.steveplays28.dynamictreesfabric.entities.animation.DataAnimationHandler;
import io.github.steveplays28.dynamictreesfabric.init.DTConfigs;
import io.github.steveplays28.dynamictreesfabric.init.DTRegistries;
import io.github.steveplays28.dynamictreesfabric.models.FallingTreeEntityModelTrackerCache;
import io.github.steveplays28.dynamictreesfabric.models.ModelTracker;
import io.github.steveplays28.dynamictreesfabric.util.BlockBounds;
import io.github.steveplays28.dynamictreesfabric.util.BlockStates;
import io.github.steveplays28.dynamictreesfabric.util.BranchDestructionData;
import io.github.steveplays28.dynamictreesfabric.util.CoordUtils.Surround;
import net.minecraftforge.network.NetworkHooks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;

/**
 * @author ferreusveritas
 */
public class FallingTreeEntity extends Entity implements ModelTracker {

	public static final TrackedData<NbtCompound> voxelDataParameter = DataTracker.registerData(FallingTreeEntity.class, TrackedDataHandlerRegistry.NBT_COMPOUND);
	public static AnimationHandler AnimHandlerFall = AnimationHandlers.falloverAnimationHandler;
	public static AnimationHandler AnimHandlerDrop = AnimationHandlers.defaultAnimationHandler;
	public static AnimationHandler AnimHandlerBurn = AnimationHandlers.defaultAnimationHandler;
	public static AnimationHandler AnimHandlerFling = AnimationHandlers.defaultAnimationHandler;
	public static AnimationHandler AnimHandlerBlast = AnimationHandlers.blastAnimationHandler;
	public boolean landed = false;
	public DestroyType destroyType = DestroyType.HARVEST;
	public boolean onFire = false;
	public AnimationHandler currentAnimationHandler = AnimationHandlers.voidAnimationHandler;
	public DataAnimationHandler dataAnimationHandler = null;
	//Not needed in client
	protected List<ItemStack> payload = new ArrayList<>(0);
	//Needed in client and server
	protected BranchDestructionData destroyData = new BranchDestructionData();
	protected Vec3d geomCenter = Vec3d.ZERO;
	protected Vec3d massCenter = Vec3d.ZERO;
	protected Box normalBB = new Box(BlockPos.ORIGIN);
	protected Box cullingNormalBB = new Box(BlockPos.ORIGIN);
	protected boolean clientBuilt = false;
	protected boolean firstUpdate = true;
	protected Box cullingBB;

	//Stores color for tinted quads that aren't the leaves
//	protected Map<BakedQuad, Integer> quadTints = new HashMap<>();

	public FallingTreeEntity(World world) {
		super(DTRegistries.FALLING_TREE.get(), world);
	}

	public FallingTreeEntity(EntityType<? extends FallingTreeEntity> type, World world) {
		super(type, world);
	}

	/**
	 * Same style payload droppers that have always existed in Dynamic Trees.
	 * <p>
	 * Drops wood materials at the cut position Leaves drops fall from their original location
	 *
	 * @param entity The {@link FallingTreeEntity} object.
	 */
	public static void standardDropLogsPayload(FallingTreeEntity entity) {
		World world = entity.world;
		if (!world.isClient) {
			BlockPos cutPos = entity.getDestroyData().cutPos;
			entity.getPayload().forEach(i -> spawnItemAsEntity(world, cutPos, i));
		}
	}

	public static void standardDropLeavesPayLoad(FallingTreeEntity entity) {
		World world = entity.world;
		if (!world.isClient) {
			BlockPos cutPos = entity.getDestroyData().cutPos;
			entity.getDestroyData().leavesDrops.forEach(bis -> Block.dropStack(world, cutPos.add(bis.pos), bis.stack));
		}
	}

	/**
	 * Same as Block.spawnAsEntity only this arrests the entityItem's random motion. Useful for CC turtles to pick up
	 * the loot.
	 */
	public static void spawnItemAsEntity(World worldIn, BlockPos pos, ItemStack stack) {
		if (!worldIn.isClient && !stack.isEmpty() && worldIn.getGameRules().getBoolean(GameRules.DO_TILE_DROPS) && !worldIn.restoringBlockSnapshots) { // do not drop items while restoring blockstates, prevents item dupe
			ItemEntity entityitem = new ItemEntity(worldIn, (double) pos.getX() + 0.5F, (double) pos.getY() + 0.5F, (double) pos.getZ() + 0.5F, stack);
			entityitem.setVelocity(0, 0, 0);
			entityitem.setToDefaultPickupDelay();
			worldIn.spawnEntity(entityitem);
		}
	}

	public static FallingTreeEntity dropTree(World world, BranchDestructionData destroyData, List<ItemStack> woodDropList, DestroyType destroyType) {
		//Spawn the appropriate item entities into the world
		if (!world.isClient) {// Only spawn entities server side
			// Falling tree currently has severe rendering issues.
			FallingTreeEntity entity = new FallingTreeEntity(world).setData(destroyData, woodDropList, destroyType);
			if (entity.isAlive()) {
				world.spawnEntity(entity);
			}
			return entity;
		}

		return null;
	}

	public boolean isClientBuilt() {
		return clientBuilt;
	}

//	public Map<BakedQuad, Integer> getQuadTints (){
//		return quadTints;
//	}
//	public void addTintedQuad (int tint, BakedQuad quad){
//		quadTints.put(quad, tint);
//	}
//	public void addTintedQuads (int tint, BakedQuad... quads){
//		for (BakedQuad quad : quads)
//			addTintedQuad(tint, quad);
//	}

	/**
	 * This is only run by the server to set up the object data
	 *
	 * @param destroyData
	 * @param payload
	 */
	public FallingTreeEntity setData(BranchDestructionData destroyData, List<ItemStack> payload, DestroyType destroyType) {
		this.destroyData = destroyData;
		if (destroyData.getNumBranches() == 0) { //If the entity contains no branches there's no reason to create it at all
			System.err.println("Warning: Tried to create a EntityFallingTree with no branch blocks. This shouldn't be possible.");
			new Exception().printStackTrace();
			kill();
			return this;
		}
		BlockPos cutPos = destroyData.cutPos;
		this.payload = payload;
		this.destroyType = destroyType;
		this.onFire = destroyType == DestroyType.FIRE;

		this.setPos(cutPos.getX() + 0.5, cutPos.getY(), cutPos.getZ() + 0.5);

		int numBlocks = destroyData.getNumBranches();
		geomCenter = new Vec3d(0, 0, 0);
		double totalMass = 0;

		//Calculate center of geometry, center of mass and bounding box, remap to relative coordinates
		for (int index = 0; index < destroyData.getNumBranches(); index++) {
			BlockPos relPos = destroyData.getBranchRelPos(index);

			int radius = destroyData.getBranchRadius(index);
			float mass = (radius * radius * 64) / 4096f;//Assume full height cuboids for simplicity
			totalMass += mass;

			Vec3d relVec = new Vec3d(relPos.getX(), relPos.getY(), relPos.getZ());
			geomCenter = geomCenter.add(relVec);
			massCenter = massCenter.add(relVec.multiply(mass));
		}

		geomCenter = geomCenter.multiply(1.0 / numBlocks);
		massCenter = massCenter.multiply(1.0 / totalMass);

		setVoxelData(buildVoxelData(destroyData));

		return this;
	}

	public NbtCompound buildVoxelData(BranchDestructionData destroyData) {
		NbtCompound tag = new NbtCompound();
		destroyData.writeToNBT(tag);

		tag.putDouble("geomx", geomCenter.x);
		tag.putDouble("geomy", geomCenter.y);
		tag.putDouble("geomz", geomCenter.z);
		tag.putDouble("massx", massCenter.x);
		tag.putDouble("massy", massCenter.y);
		tag.putDouble("massz", massCenter.z);
		tag.putInt("destroytype", destroyType.ordinal());
		tag.putBoolean("onfire", onFire);

		return tag;
	}

	public void setupFromNBT(NbtCompound tag) {
		destroyData = new BranchDestructionData(tag);
		if (destroyData.getNumBranches() == 0) {
			kill();
		}
		destroyType = DestroyType.values()[tag.getInt("destroytype")];
		geomCenter = new Vec3d(tag.getDouble("geomx"), tag.getDouble("geomy"), tag.getDouble("geomz"));
		massCenter = new Vec3d(tag.getDouble("massx"), tag.getDouble("massy"), tag.getDouble("massz"));

		this.setBoundingBox(this.buildAABBFromDestroyData(this.destroyData).offset(this.getX(), this.getY(), this.getZ()));
		this.cullingBB = this.cullingNormalBB.offset(this.getX(), this.getY(), this.getZ());

		onFire = tag.getBoolean("onfire");
	}

	public void buildClient() {

		NbtCompound tag = getVoxelData();

		if (tag.contains("species")) {
			setupFromNBT(tag);
			clientBuilt = true;
		} else {
			System.out.println("Error: No species tag has been set");
		}

		BlockBounds renderBounds = new BlockBounds(destroyData.cutPos);

		for (BlockPos absPos : Iterables.concat(destroyData.getPositions(BranchDestructionData.PosType.BRANCHES), destroyData.getPositions(BranchDestructionData.PosType.LEAVES))) {
			BlockState state = world.getBlockState(absPos);
			if (TreeHelper.isTreePart(state)) {
				world.setBlockState(absPos, BlockStates.AIR, 0);////The client needs to set it's blocks to air
				renderBounds.union(absPos);//Expand the re-render volume to include this block
			}
		}

		cleanupShellBlocks(destroyData);

		MinecraftClient.getInstance().worldRenderer.scheduleBlockRenders(renderBounds.getMin().getX(), renderBounds.getMin().getY(), renderBounds.getMin().getZ(), renderBounds.getMax().getX(), renderBounds.getMax().getY(), renderBounds.getMax().getZ());//This forces the client to rerender the chunks
	}

	protected void cleanupShellBlocks(BranchDestructionData destroyData) {
		BlockPos cutPos = destroyData.cutPos;
		for (int i = 0; i < destroyData.getNumBranches(); i++) {
			if (destroyData.getBranchRadius(i) > 8) {
				BlockPos pos = destroyData.getBranchRelPos(i).add(cutPos);
				for (Surround dir : Surround.values()) {
					BlockPos dPos = pos.add(dir.getOffset());
					if (world.getBlockState(dPos).getBlock() instanceof TrunkShellBlock) {
						world.removeBlock(dPos, false);
					}
				}
			}
		}
	}

	public Box buildAABBFromDestroyData(BranchDestructionData destroyData) {

		normalBB = new Box(BlockPos.ORIGIN);

		for (BlockPos relPos : destroyData.getPositions(BranchDestructionData.PosType.BRANCHES, false)) {
			normalBB = normalBB.union(new Box(relPos));
		}

		//Adjust the bounding box to account for the tree falling over
		double height = normalBB.maxY - normalBB.minY;
		double width = MathHelper.absMax(normalBB.maxX - normalBB.minX, normalBB.maxZ - normalBB.minZ);
		double grow = Math.max(0, height - (width / 2)) + 2;
		cullingNormalBB = normalBB.expand(grow + 4, 4, grow + 4);

		return normalBB;
	}

	@Override
	public Box getVisibilityBoundingBox() {
		return this.cullingBB;
	}

	public BranchDestructionData getDestroyData() {
		return destroyData;
	}

	public List<ItemStack> getPayload() {
		return payload;
	}

	public Vec3d getGeomCenter() {
		return geomCenter;
	}

	public Vec3d getMassCenter() {
		return massCenter;
	}

	@Override
	public void setPosition(double x, double y, double z) {
		//This comes to the client as a packet from the server. But it doesn't set up the bounding box correctly
		this.setPos(x, y, z);
		//This function is called by the Entity constructor during which normAABB hasn't yet been assigned.
		this.setBoundingBox(this.normalBB != null ? this.normalBB.offset(x, y, z) : new Box(BlockPos.ORIGIN));
		this.cullingBB = cullingNormalBB != null ? cullingNormalBB.offset(x, y, z) : new Box(BlockPos.ORIGIN);
	}

	@Override
	public void tick() {
		super.tick();

		if (this.world.isClient && !this.clientBuilt) {
			this.buildClient();
			if (!isAlive()) {
				return;
			}
		}

		if (!this.world.isClient && this.firstUpdate) {
			this.updateNeighbors();
		}

		this.handleMotion();

		this.setBoundingBox(this.normalBB.offset(this.getX(), this.getY(), this.getZ()));
		this.cullingBB = cullingNormalBB.offset(this.getX(), this.getY(), this.getZ());

		if (this.shouldDie()) {
			this.dropPayLoad();
			this.kill();
			this.modelCleanup();
		}

		this.firstUpdate = false;
	}

	/**
	 * This is run server side to update all of the neighbors
	 */
	protected void updateNeighbors() {
		HashSet<BlockPos> destroyed = new HashSet<>();
		HashSet<BlockPos> toUpdate = new HashSet<>();

		//Gather a set of all of the block positions that were recently destroyed
		Iterables.concat(destroyData.getPositions(BranchDestructionData.PosType.BRANCHES), destroyData.getPositions(BranchDestructionData.PosType.LEAVES)).forEach(destroyed::add);

		//Gather a list of all of the non-destroyed blocks surrounding each destroyed block
		for (BlockPos d : destroyed) {
			for (Direction dir : Direction.values()) {
				BlockPos dPos = d.offset(dir);
				if (!destroyed.contains(dPos)) {
					toUpdate.add(dPos);
				}
			}
		}

		//Update each of the blocks that need to be updated
		toUpdate.forEach(pos -> world.updateNeighbor(pos, Blocks.AIR, pos));
	}

	protected AnimationHandler selectAnimationHandler() {
		return DTConfigs.ENABLE_FALLING_TREES.get() ? destroyData.species.selectAnimationHandler(this) : AnimationHandlers.voidAnimationHandler;
	}

	public AnimationHandler defaultAnimationHandler() {
		if (destroyType == DestroyType.VOID || destroyType == DestroyType.ROOT) {
			return AnimationHandlers.voidAnimationHandler;
		}

		if (destroyType == DestroyType.BLAST) {
			return AnimHandlerBlast;
		}

		if (destroyType == DestroyType.FIRE) {
			return AnimHandlerBurn;
		}

		if (getDestroyData().cutDir == Direction.DOWN) {
			if (getMassCenter().y >= 1.0) {
				return AnimHandlerFall;
			} else {
				return AnimHandlerFling;
			}
		}

		return AnimHandlerDrop;
	}

	@Override

@Environment(EnvType.CLIENT)
	public void modelCleanup() {
		FallingTreeEntityModelTrackerCache.cleanupModels(world, this);
	}

	public void handleMotion() {
		if (firstUpdate) {
			currentAnimationHandler = selectAnimationHandler();
			currentAnimationHandler.initMotion(this);
		} else {
			currentAnimationHandler.handleMotion(this);
		}
	}

	public void dropPayLoad() {
		if (!world.isClient) {
			currentAnimationHandler.dropPayload(this);
		}
	}

	public boolean shouldDie() {
		return age > 20 && currentAnimationHandler.shouldDie(this); //Give the entity 20 ticks to receive it's data from the server.
	}

@Environment(EnvType.CLIENT)
	public boolean shouldRender() {
		return currentAnimationHandler.shouldRender(this);
	}

	@Override
	protected void initDataTracker() {
		getDataTracker().startTracking(voxelDataParameter, new NbtCompound());
	}

	public void cleanupRootyDirt() {
		// Force the Rooty Dirt to update if it's there.  Turning it back to dirt.
		if (!this.world.isClient) {
			final BlockPos rootPos = getDestroyData().cutPos.down();
			final BlockState belowState = this.world.getBlockState(rootPos);

			if (TreeHelper.isRooty(belowState)) {
				final RootyBlock rootyBlock = (RootyBlock) belowState.getBlock();
				rootyBlock.doDecay(this.world, rootPos, belowState, getDestroyData().species);
			}
		}
	}

	public NbtCompound getVoxelData() {
		return getDataTracker().get(voxelDataParameter);
	}

	//This is shipped off to the clients
	public void setVoxelData(NbtCompound tag) {
		this.setBoundingBox(this.buildAABBFromDestroyData(this.destroyData).offset(this.getX(), this.getY(), this.getZ()));
		this.cullingBB = this.cullingNormalBB.offset(this.getX(), this.getY(), this.getZ());
		getDataTracker().set(voxelDataParameter, tag);
	}

	@Override
	protected void readCustomDataFromNbt(NbtCompound compound) {
		NbtCompound vox = (NbtCompound) compound.get("vox");
		setupFromNBT(vox);
		setVoxelData(vox);

		if (compound.contains("payload")) {
			final NbtList nbtList = (NbtList) compound.get("payload");

			for (NbtElement tag : Objects.requireNonNull(nbtList)) {
				if (tag instanceof NbtCompound) {
					NbtCompound compTag = (NbtCompound) tag;
					this.payload.add(ItemStack.fromNbt(compTag));
				}
			}
		}

	}

	@Override
	protected void writeCustomDataToNbt(NbtCompound compound) {
		compound.put("vox", getVoxelData());

		if (!payload.isEmpty()) {
			NbtList list = new NbtList();

			for (ItemStack stack : payload) {
				list.add(stack.serializeNBT());
			}

			compound.put("payload", list);
		}
	}

	@NotNull
	@Override
	public Packet<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	public enum DestroyType {
		VOID,
		HARVEST,
		BLAST,
		FIRE,
		ROOT
	}

}
