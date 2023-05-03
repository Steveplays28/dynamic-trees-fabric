package io.github.steveplays28.dynamictreesfabric.command;

import java.util.Optional;

import com.mojang.brigadier.builder.ArgumentBuilder;
import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.util.CommandHelper;
import io.github.steveplays28.dynamictreesfabric.worldgen.JoCode;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class GetTreeCommand extends SubCommand {

	private static final String CODE_RAW = "code_raw";

	@Override
	protected String getName() {
		return CommandConstants.GET_TREE;
	}

	@Override
	protected int getPermissionLevel() {
		return 0;
	}

	@Override
	public ArgumentBuilder<ServerCommandSource, ?> registerArgument() {
		return blockPosArgument().executes(context -> this.getTree(context.getSource(), blockPosArgument(context), false))
				.then(booleanArgument(CODE_RAW).executes(context -> this.getTree(context.getSource(), blockPosArgument(context),
						booleanArgument(context, CODE_RAW))));
	}

	private int getTree(final ServerCommandSource source, final BlockPos pos, final boolean codeRaw) {
		final World world = source.getWorld();

		return TreeHelper.getBestGuessSpecies(world, pos).ifValidElse(species -> {
					final Optional<JoCode> joCode = TreeHelper.getJoCode(world, pos);

					if (codeRaw) {
						sendSuccess(source, Text.literal(joCode.map(JoCode::toString).orElse("?")));
					} else {
						sendSuccess(source, Text.translatable("commands.dynamictrees.success.get_tree",
								species.getTextComponent(), joCode.map(JoCode::getTextComponent)
										.orElse(Text.literal("?"))));
					}
				}, () -> sendFailure(source, Text.translatable("commands.dynamictrees.error.get_tree",
						CommandHelper.posComponent(pos).copy().styled(style -> style.withColor(Formatting.DARK_RED))))
		) ? 1 : 0;
	}

}
