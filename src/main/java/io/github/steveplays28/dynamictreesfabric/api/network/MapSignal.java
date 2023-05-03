package io.github.steveplays28.dynamictreesfabric.api.network;

import io.github.steveplays28.dynamictreesfabric.systems.nodemappers.CollectorNode;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;

public class MapSignal {

	public BlockPos root;
	public int depth;
	public boolean multiroot = false;
	public boolean destroyLoopedNodes = true;
	public boolean trackVisited = false;
	public Direction localRootDir;
	public boolean overflow;
	public boolean foundRoot;
	protected ArrayList<NodeInspector> nodeInspectors;

	public MapSignal() {
		localRootDir = null;
		nodeInspectors = new ArrayList<>();
	}

	public MapSignal(NodeInspector... nis) {
		this();

		nodeInspectors.addAll(Arrays.asList(nis));
	}

	public boolean run(BlockState blockState, WorldAccess world, BlockPos pos, @Nullable Direction fromDir) {
		for (NodeInspector inspector : nodeInspectors) {
			inspector.run(blockState, world, pos, fromDir);
		}
		return false;
	}

	public boolean returnRun(BlockState blockState, WorldAccess world, BlockPos pos, Direction fromDir) {
		for (NodeInspector inspector : nodeInspectors) {
			inspector.returnRun(blockState, world, pos, fromDir);
		}
		return false;
	}

	public ArrayList<NodeInspector> getInspectors() {
		return nodeInspectors;
	}

	public boolean doTrackingVisited(BlockPos pos) {
		if (nodeInspectors.size() > 0) {
			final NodeInspector inspector = nodeInspectors.get(0);

			if (inspector instanceof CollectorNode) {
				return ((CollectorNode) inspector).contains(pos);
			}
		}
		return false;
	}

}
