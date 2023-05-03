package io.github.steveplays28.dynamictreesfabric.event.handlers;

import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;

//This has been put in place to counteract the effects of the FastLeafDecay mod
public class LeafUpdateEventHandler {

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void UpdateNeighbour(BlockEvent.NeighborNotifyEvent event) {
		WorldAccess world = event.getLevel();
		for (Direction facing : event.getNotifiedSides()) {
			BlockPos blockPos = event.getPos().relative(facing);
			if (TreeHelper.isLeaves(world.getBlockState(blockPos))) {
				event.setCanceled(true);
			}
		}
	}

}
