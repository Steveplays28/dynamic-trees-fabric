package io.github.steveplays28.dynamictreesfabric.command;

import io.github.steveplays28.dynamictreesfabric.util.ChunkTreeHelper;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

/**
 * @author Harley O'Connor
 */
public final class PurgeTreesCommand extends ChunkBasedCommand {

	@Override
	protected String getName() {
		return CommandConstants.PURGE_TREES;
	}

	@Override
	protected int getPermissionLevel() {
		return 2;
	}

	@Override
	protected void processChunk(ServerCommandSource source, World world, ChunkPos chunkPos, int radius) {
		sendSuccessAndLog(source, Text.translatable("commands.dynamictrees.success.purge_trees",
				aqua(ChunkTreeHelper.removeAllBranchesFromChunk(world, chunkPos, radius))));
	}

}
