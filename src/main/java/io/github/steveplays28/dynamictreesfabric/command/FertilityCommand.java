package io.github.steveplays28.dynamictreesfabric.command;

import static io.github.steveplays28.dynamictreesfabric.command.CommandConstants.FERTILITY_SUGGESTIONS;
import static io.github.steveplays28.dynamictreesfabric.command.CommandConstants.RAW;

import java.util.Objects;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.util.CommandHelper;

import net.minecraft.block.BlockState;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

public final class FertilityCommand extends SubCommand {

	private static final String FERTILITY = CommandConstants.FERTILITY;

	@Override
	protected String getName() {
		return FERTILITY;
	}

	@Override
	protected int getPermissionLevel() {
		return 0;
	}

	@Override
	public ArgumentBuilder<ServerCommandSource, ?> registerArgument() {
		return blockPosArgument().executes(context -> executesSuccess(() -> this.getFertility(context.getSource(),
						rootPosArgument(context), false)))
				.then(booleanArgument(RAW).executes(context -> executesSuccess(() -> this.getFertility(context.getSource(),
						rootPosArgument(context), booleanArgument(context, RAW)))))
				.then(CommandManager.argument(FERTILITY, IntegerArgumentType.integer(0, 15)).suggests(FERTILITY_SUGGESTIONS)
						.requires(commandSource -> commandSource.hasPermissionLevel(2)) // Setting fertility requires higher permission level.
						.executes(context -> executesSuccess(() -> this.setFertility(context.getSource(), rootPosArgument(context),
								intArgument(context, FERTILITY)))));
	}

	private void getFertility(final ServerCommandSource source, final BlockPos rootPos, final boolean raw) {
		final BlockState state = source.getWorld().getBlockState(rootPos);
		final int fertility = Objects.requireNonNull(TreeHelper.getRooty(state)).getFertility(state, source.getWorld(), rootPos);

		if (raw) {
			sendSuccess(source, Text.literal(String.valueOf(fertility)));
			return;
		}

		sendSuccess(source, Text.translatable("commands.dynamictrees.success.get_fertility",
				CommandHelper.posComponent(rootPos, Formatting.AQUA),
				CommandHelper.colour(String.valueOf(fertility), Formatting.AQUA)));
	}

	private void setFertility(final ServerCommandSource source, final BlockPos rootPos, final int fertility) {
		final BlockState state = source.getWorld().getBlockState(rootPos);
		Objects.requireNonNull(TreeHelper.getRooty(state)).setFertility(source.getWorld(), rootPos, fertility);

		sendSuccessAndLog(source, Text.translatable("commands.dynamictrees.success.set_fertility",
				CommandHelper.posComponent(rootPos, Formatting.AQUA),
				CommandHelper.colour(String.valueOf(fertility), Formatting.AQUA)));
	}

}
