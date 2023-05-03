package io.github.steveplays28.dynamictreesfabric.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

/**
 * @author Harley O'Connor
 */
public abstract class ChunkBasedCommand extends SubCommand {

	private static final String RADIUS = "radius";

	private static final int DEFAULT_RADIUS = 1;

	@Override
	public ArgumentBuilder<ServerCommandSource, ?> register() {
		return super.register().executes(context -> executesSuccess(() -> this.processChunk(context.getSource(),
				context.getSource().getWorld(), this.getChunkPos(context.getSource()), DEFAULT_RADIUS)));
	}

	private ChunkPos getChunkPos(final ServerCommandSource source) {
		return new ChunkPos(new BlockPos(source.getPosition().x, source.getPosition().y, source.getPosition().z));
	}

	@Override
	public ArgumentBuilder<ServerCommandSource, ?> registerArgument() {
		return blockPosArgument().executes(context -> executesSuccess(() -> this.processChunk(context.getSource(),
						context.getSource().getWorld(), new ChunkPos(blockPosArgument(context)), DEFAULT_RADIUS)))
				.then(CommandManager.argument(RADIUS, IntegerArgumentType.integer(1))
						.executes(context -> executesSuccess(() -> this.processChunk(context.getSource(),
								context.getSource().getWorld(), new ChunkPos(blockPosArgument(context)), intArgument(context, RADIUS)))));
	}

	protected abstract void processChunk(ServerCommandSource source, World world, ChunkPos chunkPos, int radius);

}
