package com.ferreusveritas.dynamictrees.systems.nodemappers;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.INodeInspector;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class NodeNetVolume implements INodeInspector {

	public static final class Volume {
		public static final int VOXELSPERLOG = 4096; //A log contains 4096 voxels of wood material(16x16x16 pixels)

		private float volume;

		public Volume() {
			this.volume = 0;
		}

		public Volume(float volume) {
			this.volume = volume * VOXELSPERLOG;
		}

		public void addVolume (float volume) {
			this.volume += volume;
		}

		public void multiplyVolume (double multiplier) {
			this.volume *= multiplier;
		}

		public float getVolume() {
			return volume / (float) VOXELSPERLOG;
		}

		public float getTotalVolume () {
			return this.getVolume();
		}

	}

	private final Volume volume = new Volume();//number of voxels(1x1x1 pixels) of wood accumulated from network analysis

	@Override
	public boolean run(BlockState state, IWorld world, BlockPos pos, Direction fromDir) {
		if(TreeHelper.isBranch(state)) {
			ITreePart treePart = TreeHelper.getTreePart(state);
			int radius = treePart.getRadius(state);
			volume.addVolume(radius * radius * 64);//Integrate volume of this tree part into the total volume calculation
		}
		return true;
	}
	
	@Override
	public boolean returnRun(BlockState blockState, IWorld world, BlockPos pos, Direction fromDir) {
		return false;
	}
	
	public Volume getVolume () {
		return volume;
	}

}
