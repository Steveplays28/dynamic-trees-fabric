package io.github.steveplays28.dynamictreesfabric.command;

import io.github.steveplays28.dynamictreesfabric.util.ChunkTreeHelper;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

/**
 * @author Harley O'Connor
 */
public final class ClearOrphanedCommand extends ChunkBasedCommand {

	@Override
	protected String getName() {
		return CommandConstants.CLEAR_ORPHANED;
	}

	@Override
	protected int getPermissionLevel() {
		return 0;
	}

	@Override
	protected void processChunk(ServerCommandSource source, World world, ChunkPos chunkPos, int radius) {
		sendSuccessAndLog(source, Text.translatable("commands.dynamictrees.success.clear_orphaned",
				aqua(ChunkTreeHelper.removeOrphanedBranchNodes(world, chunkPos, radius))));
	}

}
