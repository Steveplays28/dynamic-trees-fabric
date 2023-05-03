package io.github.steveplays28.dynamictreesfabric.worldgen;

import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.api.network.MapSignal;
import io.github.steveplays28.dynamictreesfabric.api.network.NodeInspector;
import io.github.steveplays28.dynamictreesfabric.blocks.branches.BranchBlock;
import io.github.steveplays28.dynamictreesfabric.blocks.leaves.DynamicLeavesBlock;
import io.github.steveplays28.dynamictreesfabric.blocks.leaves.LeavesProperties;
import io.github.steveplays28.dynamictreesfabric.cells.LeafClusters;
import io.github.steveplays28.dynamictreesfabric.data.DTBlockTags;
import io.github.steveplays28.dynamictreesfabric.event.SpeciesPostGenerationEvent;
import io.github.steveplays28.dynamictreesfabric.systems.genfeatures.context.PostGenerationContext;
import io.github.steveplays28.dynamictreesfabric.systems.nodemappers.CoderNode;
import io.github.steveplays28.dynamictreesfabric.systems.nodemappers.CollectorNode;
import io.github.steveplays28.dynamictreesfabric.systems.nodemappers.FindEndsNode;
import io.github.steveplays28.dynamictreesfabric.trees.Family;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import io.github.steveplays28.dynamictreesfabric.util.BlockStates;
import io.github.steveplays28.dynamictreesfabric.util.SafeChunkBounds;
import io.github.steveplays28.dynamictreesfabric.util.SimpleVoxmap;
import io.github.steveplays28.dynamictreesfabric.util.SimpleVoxmap.Cell;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.TreeFeature;

import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * So named because the base64 codes it generates almost always start with "JO"
 *
 * <p>This class provides methods for recreating tree shapes.</p>
 *
 * @author ferreusveritas
 */
public class JoCode {

	protected static final byte FORK_CODE = 6;
	protected static final byte RETURN_CODE = 7;
	private static final Logger LOGGER = LogManager.getLogger();
	private static final String BASE_64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
	/**
	 * A facing matrix for mapping instructions to different rotations
	 */
	private final byte[][] dirmap = {
			//  {D, U, N, S, W, E, F, R}
			{0, 1, 2, 3, 4, 5, 6, 7},//FACING DOWN:	 Same as NORTH
			{0, 1, 2, 3, 4, 5, 6, 7},//FACING UP:	 Same as NORTH
			{0, 1, 2, 3, 4, 5, 6, 7},//FACING NORTH: N->N S->S W->W E->E 0
			{0, 1, 3, 2, 5, 4, 6, 7},//FACING SOUTH: N->S S->N W->E E->W 180
			{0, 1, 5, 4, 2, 3, 6, 7},//FACING WEST:	 N->E S->W W->N E->S 90 CW
			{0, 1, 4, 5, 3, 2, 6, 7},//FACING EAST:	 N->W S->E W->S E->N 90 CCW
	};
	public byte[] instructions = new byte[0];
	protected boolean careful = false;//If true the code checks for surrounding branches while building to avoid making frankentrees.  Safer but slower.
	//"Pointers" to the current rotation direction.
	private byte[] facingMap = dirmap[2];//Default to NORTH(Effectively an identity matrix)
	private byte[] unfacingMap = dirmap[2];//Default to NORTH(Effectively an identity matrix)

	/**
	 * @param world   The world
	 * @param rootPos Block position of rootyDirt block
	 * @param facing  A final rotation applied to the code after creation
	 */
	public JoCode(World world, BlockPos rootPos, Direction facing) {
		Optional<BranchBlock> branch = TreeHelper.getBranchOpt(world.getBlockState(rootPos.up()));

		if (branch.isPresent()) {
			CoderNode coder = new CoderNode();
			//Warning!  This sends a RootyBlock BlockState into a branch for the kickstart of the analysis.
			branch.get().analyse(world.getBlockState(rootPos), world, rootPos, Direction.DOWN, new MapSignal(coder));
			instructions = coder.compile(this);
			rotate(facing);
		}
	}

	/**
	 * Build a JoCode instruction set from the tree found at pos.
	 *
	 * @param world The world
	 * @param pos   Position of the rooty dirt block
	 */
	public JoCode(World world, BlockPos pos) {
		this(world, pos, Direction.SOUTH);
	}

	public JoCode(String code) {
		instructions = decode(code);
	}

	static public String encode(byte[] array) {

		//Convert byte array to ArrayList of Byte
		ArrayList<Byte> instructions = new ArrayList<>(array.length + (array.length & 1));
		for (byte b : array) {
			instructions.add(b);
		}

		if ((instructions.size() & 1) == 1) {//Check if odd
			instructions.add(RETURN_CODE);//Add a return code to even up the series
		}

		//Smallest Base64 encoder ever.
		StringBuilder code = new StringBuilder();
		for (int b = 0; b < instructions.size(); b += 2) {
			code.append(BASE_64.charAt(instructions.get(b) << 3 | instructions.get(b + 1)));
		}

		return code.toString();
	}

	static public byte[] decode(String code) {
		return new CodeCompiler(code).compile();
	}

	public JoCode setCareful(boolean c) {
		careful = c;
		return this;
	}

	/**
	 * Get the instruction at a locus. Automatically performs rotation based on what facing matrix is selected.
	 *
	 * @param pos
	 * @return
	 */
	protected int getCode(int pos) {
		return unfacingMap[instructions[pos]];
	}

	/**
	 * Sets the active facing matrix to a specific direction
	 *
	 * @param facing
	 * @return
	 */
	public JoCode setFacing(Direction facing) {
		int faceNum = facing.ordinal();
		facingMap = dirmap[faceNum];
		faceNum = (faceNum == 4) ? 5 : (faceNum == 5) ? 4 : faceNum;//Swap West and East
		unfacingMap = dirmap[faceNum];
		return this;
	}

	/**
	 * Rotates the JoCode such that the model's "north" faces a new direction.
	 *
	 * @param dir
	 * @return
	 */
	public JoCode rotate(Direction dir) {
		setFacing(dir);
		for (int c = 0; c < instructions.length; c++) {
			instructions[c] = facingMap[instructions[c]];
		}
		return this;
	}

	/**
	 * Generate a tree from this {@link JoCode} instruction list.
	 *
	 * @param world             The {@link World} instance.
	 * @param rootPosIn         The position of what will become the {@link io.github.steveplays28.dynamictreesfabric.blocks.rootyblocks.RootyBlock}.
	 * @param biome             The {@link Biome} at {@code rootPosIn}.
	 * @param facing            The {@link Direction} of the tree.
	 * @param radius            The radius constraint.
	 * @param secondChanceRegen Ensures second chance regen doesn't recurse too far.
	 */
	public void generate(World worldObj, WorldAccess world, Species species, BlockPos rootPosIn, RegistryEntry<Biome> biome, Direction facing, int radius, SafeChunkBounds safeBounds, boolean secondChanceRegen) {
		final boolean worldGen = safeBounds != SafeChunkBounds.ANY;

		// A Tree generation boundary radius is at least 2 and at most 8.
		radius = MathHelper.clamp(radius, 2, 8);

		this.setFacing(facing);
		final BlockPos rootPos = species.preGeneration(world, rootPosIn, radius, facing, safeBounds, this);

		if (rootPos == BlockPos.ORIGIN) {
			return;
		}

		final BlockState initialDirtState = world.getBlockState(rootPos); // Save the initial state of the dirt in case this fails.
		species.placeRootyDirtBlock(world, rootPos, 0); // Set to unfertilized rooty dirt.

		// Make the tree branch structure.
		this.generateFork(world, species, 0, rootPos, false);

		// Establish a position for the bottom block of the trunk.
		final BlockPos treePos = rootPos.up();

		// Fix branch thicknesses and map out leaf locations.
		final BlockState treeState = world.getBlockState(treePos);
		final BranchBlock firstBranch = TreeHelper.getBranch(treeState);

		// If a branch doesn't exist the growth failed.. turn the soil back to what it was.
		if (firstBranch == null) {
			world.setBlockState(rootPos, initialDirtState, this.careful ? 3 : 2);
			return;
		}

		// If a branch exists then the growth was successful.

		final LeavesProperties leavesProperties = species.getLeavesProperties();
		final SimpleVoxmap leafMap = new SimpleVoxmap(radius * 2 + 1, species.getWorldGenLeafMapHeight(), radius * 2 + 1).setMapAndCenter(treePos, new BlockPos(radius, 0, radius));
		final NodeInspector inflator = species.getNodeInflator(leafMap); // This is responsible for thickening the branches.
		final FindEndsNode endFinder = new FindEndsNode(); // This is responsible for gathering a list of branch end points.
		final MapSignal signal = new MapSignal(inflator, endFinder); // The inflator signal will "paint" a temporary voxmap of all of the leaves and branches.
		signal.destroyLoopedNodes = this.careful;

		firstBranch.analyse(treeState, world, treePos, Direction.DOWN, signal);

		if (signal.foundRoot || signal.overflow) { // Something went terribly wrong.
			this.tryGenerateAgain(worldObj, world, species, rootPosIn, biome, facing, radius, safeBounds, worldGen, treePos, treeState, endFinder, secondChanceRegen);
			return;
		}

		final List<BlockPos> endPoints = endFinder.getEnds();

		this.smother(leafMap, leavesProperties); // Use the voxmap to precompute leaf smothering so we don't have to age it as many times.

		// Place Growing Leaves Blocks from voxmap.
		for (final Cell cell : leafMap.getAllNonZeroCells((byte) 0x0F)) { // Iterate through all of the cells that are leaves (not air or branches).
			final BlockPos.Mutable cellPos = cell.getPos();

			if (safeBounds.inBounds(cellPos, false)) {
				final BlockState testBlockState = world.getBlockState(cellPos);
				if (testBlockState.isAir() || testBlockState.isIn(BlockTags.LEAVES)) {
					world.setBlockState(cellPos, leavesProperties.getDynamicLeavesState(cell.getValue()), worldGen ? 16 : 2); // Flag 16 to prevent observers from causing cascading lag.
				}
			} else {
				leafMap.setVoxel(cellPos, (byte) 0);
			}
		}

		// Shrink the leafMap down by the safeBounds object so that the aging process won't look for neighbors outside of the bounds.
		for (final Cell cell : leafMap.getAllNonZeroCells()) {
			final BlockPos.Mutable cellPos = cell.getPos();
			if (!safeBounds.inBounds(cellPos, true)) {
				leafMap.setVoxel(cellPos, (byte) 0);
			}
		}

		// Age volume for 3 cycles using a leafmap.
		TreeHelper.ageVolume(world, leafMap, species.getWorldGenAgeIterations(), safeBounds);

		// Rot the unsupported branches.
		if (species.handleRot(world, endPoints, rootPos, treePos, 0, safeBounds)) {
			return; // The entire tree rotted away before it had a chance.
		}

		// Allow for special decorations by the tree itself.
		//species.postGeneration(new PostGenerationContext(world, rootPos, species, biome, radius, endPoints,
		//		safeBounds, initialDirtState, SeasonHelper.getSeasonValue(worldObj, rootPos),
		//		species.seasonalFruitProductionFactor(worldObj, rootPos)));
		//MinecraftForge.EVENT_BUS.post(new SpeciesPostGenerationEvent(world, species, rootPos, endPoints, safeBounds, initialDirtState));

		// Add snow to parts of the tree in chunks where snow was already placed.
		this.addSnow(leafMap, world, rootPos, biome);
	}

	private void tryGenerateAgain(World worldObj, WorldAccess world, Species species, BlockPos rootPosIn, RegistryEntry<Biome> biome, Direction facing, int radius, SafeChunkBounds safeBounds, boolean worldGen, BlockPos treePos, BlockState treeState, FindEndsNode endFinder, boolean secondChanceRegen) {
		// Don't log the error if it didn't happen during world gen (so we don't fill the logs if players spam the staff in cramped positions).
		if (worldGen) {
			if (!secondChanceRegen) {
				LOGGER.debug("Non-viable branch network detected during world generation @ {}", treePos);
				LOGGER.debug("Species: {}", species);
				LOGGER.debug("Radius: {}", radius);
				LOGGER.debug("JoCode: {}", this);
			} else {
				LOGGER.debug("Second attempt for code {} has also failed", this);
			}

		}

		// Completely blow away any improperly defined network nodes.
		this.cleanupFrankentree(world, treePos, treeState, endFinder.getEnds(), safeBounds);

		// Now that everything is clear we may as well regenerate the tree that screwed everything up.
		if (!secondChanceRegen) {
			this.generate(worldObj, world, species, rootPosIn, biome, facing, radius, safeBounds, true);
		}
	}

	/**
	 * Attempt to clean up fused trees that have multiple root blocks by simply destroying them both messily
	 */
	protected void cleanupFrankentree(WorldAccess world, BlockPos treePos, BlockState treeState, List<BlockPos> endPoints, SafeChunkBounds safeBounds) {
		final Set<BlockPos> blocksToDestroy = new HashSet<>();
		final BranchBlock branch = TreeHelper.getBranch(treeState);
		final MapSignal signal = new MapSignal(new CollectorNode(blocksToDestroy));

		signal.destroyLoopedNodes = false;
		signal.trackVisited = true;

		assert branch != null;

		branch.analyse(treeState, world, treePos, null, signal);
		BranchBlock.destroyMode = io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.DestroyMode.IGNORE;

		for (BlockPos pos : blocksToDestroy) {
			if (safeBounds.inBounds(pos, false)) {
				final BlockState branchState = world.getBlockState(pos);
				final Optional<BranchBlock> branchBlock = TreeHelper.getBranchOpt(branchState);

				if (!branchBlock.isPresent()) {
					continue;
				}

				int radius = branchBlock.get().getRadius(branchState);
				final Family family = branchBlock.get().getFamily();
				final Species species = family.getCommonSpecies();

				if (family.getPrimaryThickness() == radius) {
					species.getLeavesProperties().ifValid(leavesProperties -> {
						final SimpleVoxmap leafCluster = leavesProperties.getCellKit().getLeafCluster();

						if (leafCluster != LeafClusters.NULL_MAP) {
							for (Cell cell : leafCluster.getAllNonZeroCells()) {
								final BlockPos delPos = pos.add(cell.getPos());
								if (safeBounds.inBounds(delPos, false)) {
									final BlockState leavesState = world.getBlockState(delPos);
									if (TreeHelper.isLeaves(leavesState)) {
										final DynamicLeavesBlock leavesBlock = (DynamicLeavesBlock) leavesState.getBlock();
										if (leavesProperties.getFamily() == leavesBlock.getProperties(leavesState).getFamily()) {
											world.setBlockState(delPos, BlockStates.AIR, 2);
										}
									}
								}
							}
						}
					});
				}

				world.setBlockState(pos, BlockStates.AIR, 2);
			}
		}

		BranchBlock.destroyMode = io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.DestroyMode.HARVEST;

		// Now wreck out all surrounding leaves. Let them grow back naturally.
 		/*if (!endPoints.isEmpty()) {
 			final BlockBounds bounds = new BlockBounds(endPoints).expand(3);

 			for (BlockPos pos : bounds) {
 				if (safeBounds.inBounds(pos, false) && TreeHelper.isLeaves(world.getBlockState(pos))) {
					world.setBlock(pos, DTRegistries.BLOCK_STATES.AIR, 2);
				}
 			}
 		}*/
	}

	/**
	 * Recursive function that "draws" a branch of a tree
	 *
	 * @param world
	 * @param species
	 * @param codePos
	 * @param pos
	 * @param disabled
	 * @return
	 */
	protected int generateFork(WorldAccess world, Species species, int codePos, BlockPos pos, boolean disabled) {
		while (codePos < instructions.length) {
			final int code = this.getCode(codePos);

			switch (code) {
				case FORK_CODE:
					codePos = this.generateFork(world, species, codePos + 1, pos, disabled);
					break;
				case RETURN_CODE:
					return codePos + 1;
				default:
					final Direction dir = Direction.byId(code);
					pos = pos.offset(dir);
					if (!disabled) {
						disabled = this.setBlockForGeneration(world, species, pos, dir, careful, codePos + 1 == instructions.length);
					}
					codePos++;
					break;
			}
		}

		return codePos;
	}

	protected boolean setBlockForGeneration(WorldAccess world, Species species, BlockPos pos, Direction dir, boolean careful, @SuppressWarnings("unused") boolean isLast) {
		if (isFreeToSetBlock(world, pos) && (!careful || this.isClearOfNearbyBranches(world, pos, dir.getOpposite()))) {
			species.getFamily().getBranchForPlacement(world, species, pos).ifPresent(branch ->
					branch.setRadius(world, pos, species.getFamily().getPrimaryThickness(), null, careful ? 3 : 2)
			);
			return false;
		}
		return true;
	}

	protected boolean isFreeToSetBlock(WorldAccess level, BlockPos pos) {
		if (TreeFeature.canReplace(level, pos) || level.testBlockState(pos, state -> state.isIn(BlockTags.LOGS)))
			return true;

		BlockState blockState = level.getBlockState(pos);
		return blockState.getMaterial().isReplaceable() && blockState.getMaterial().isLiquid() || blockState.isIn(DTBlockTags.FOLIAGE) || blockState.isIn(BlockTags.FLOWERS);
	}

	/**
	 * Precompute leaf smothering before applying to the world.
	 *
	 * @param leafMap
	 * @param leavesProperties
	 */
	protected void smother(SimpleVoxmap leafMap, LeavesProperties leavesProperties) {
		final int smotherMax = leavesProperties.getSmotherLeavesMax();

		// Smothering is disabled if set to 0.
		if (smotherMax == 0) {
			return;
		}

		final BlockPos saveCenter = leafMap.getCenter();
		leafMap.setCenter(new BlockPos(0, 0, 0));

		int startY;

		// Find topmost block in build volume.
		for (startY = leafMap.getLenY() - 1; startY >= 0; startY--) {
			if (leafMap.isYTouched(startY)) {
				break;
			}
		}

		// Precompute smothering.
		for (int iz = 0; iz < leafMap.getLenZ(); iz++) {
			for (int ix = 0; ix < leafMap.getLenX(); ix++) {
				int count = 0;
				for (int iy = startY; iy >= 0; iy--) {
					final int v = leafMap.getVoxel(new BlockPos(ix, iy, iz));
					if (v == 0) { // Air
						count = 0; // Reset the count
					} else if ((v & 0x0F) != 0) { // Leaves
						count++;
						if (count > smotherMax) { // Smother value
							leafMap.setVoxel(new BlockPos(ix, iy, iz), (byte) 0);
						}
					} else if ((v & 0x10) != 0) { // Twig
						count++;
						leafMap.setVoxel(new BlockPos(ix, iy + 1, iz), (byte) 4);
					}
				}
			}
		}

		leafMap.setCenter(saveCenter);
	}

	protected boolean isClearOfNearbyBranches(WorldAccess world, BlockPos pos, Direction except) {
		for (Direction dir : Direction.values()) {
			if (dir != except && TreeHelper.getBranch(world.getBlockState(pos.offset(dir))) != null) {
				return false;
			}
		}

		return true;
	}

	protected void addSnow(SimpleVoxmap leafMap, WorldAccess world, BlockPos rootPos, RegistryEntry<Biome> biome) {
		if (biome.value().getTemperature() >= 0.4f) {
			return;
		}

		for (BlockPos.Mutable top : leafMap.getTops()) {
			if (world.getGeneratorStoredBiome(rootPos.getX() >> 2, rootPos.getY() >> 2, rootPos.getZ() >> 2).value().canSetSnow(world, rootPos)) {
				final BlockPos.Mutable iPos = new BlockPos.Mutable(top.getX(), top.getY(), top.getZ());
				int yOffset = 0;

				do {
					final BlockState state = world.getBlockState(iPos);
					if (state.getMaterial() == Material.AIR) {
						world.setBlockState(iPos, Blocks.SNOW.getDefaultState(), 2);
						break;
					} else if (state.getBlock() == Blocks.SNOW) {
						break;
					}
					iPos.setY(iPos.getY() + 1);
				} while (yOffset++ < 4);
			}
		}
	}

	@Override
	public String toString() {
		return encode(instructions);
	}

	public Text getTextComponent() {
		return Text.literal(this.toString()).styled(style ->
				style.withColor(Formatting.AQUA).withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, this.toString()))
						.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("chat.copy.click")))
		);
	}

	/**
	 * A tidy class for handling byte code adding and conversion to byte array
	 */
	public static class CodeCompiler {

		final ArrayList<Byte> instructions;

		public CodeCompiler() {
			instructions = new ArrayList<>();
		}

		public CodeCompiler(int size) {
			instructions = new ArrayList<>(size);
		}

		public CodeCompiler(String code) {
			instructions = new ArrayList<>(code.length() * 2);

			//Smallest Base64 decoder ever.
			for (int i = 0; i < code.length(); i++) {
				int sixbits = BASE_64.indexOf(code.charAt(i));
				if (sixbits != -1) {
					addInstruction((byte) (sixbits >> 3));
					addInstruction((byte) (sixbits & 7));
				}
			}
		}

		public void addDirection(byte dir) {
			if (dir >= 0) {
				instructions.add((byte) (dir & 7));
			}
		}

		public void addInstruction(byte instruction) {
			instructions.add(instruction);
		}

		public void addReturn() {
			instructions.add(RETURN_CODE);
		}

		public void addFork() {
			instructions.add(FORK_CODE);
		}

		public byte[] compile() {
			byte[] array = new byte[instructions.size()];
			Iterator<Byte> i = instructions.iterator();

			int pos = 0;
			while (i.hasNext()) {
				array[pos++] = i.next();
			}

			return array;
		}
	}

}
