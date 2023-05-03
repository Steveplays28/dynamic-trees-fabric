package io.github.steveplays28.dynamictreesfabric.command;

import static io.github.steveplays28.dynamictreesfabric.command.CommandConstants.*;

import java.util.stream.Collectors;

import com.mojang.brigadier.builder.ArgumentBuilder;
import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import io.github.steveplays28.dynamictreesfabric.util.CommandHelper;
import io.github.steveplays28.dynamictreesfabric.util.Null;
import io.github.steveplays28.dynamictreesfabric.util.SafeChunkBounds;
import io.github.steveplays28.dynamictreesfabric.worldgen.JoCode;

import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public final class SetTreeCommand extends SubCommand {

	private static final int DEFAULT_FERTILITY = 0;

	@Override
	protected String getName() {
		return CommandConstants.SET_TREE;
	}

	@Override
	protected int getPermissionLevel() {
		return 2;
	}

	@Override
	public ArgumentBuilder<ServerCommandSource, ?> registerArgument() {
		return blockPosArgument().then(speciesArgument().executes(context -> this.setTree(context.getSource(), blockPosArgument(context),
						speciesArgument(context), JO_CODE, DEFAULT_TURNS, DEFAULT_FERTILITY))
				.then(stringArgument(JO_CODE).suggests(((context, builder) -> CommandSource.suggestMatching(speciesArgument(context).getJoCodes()
								.stream().map(JoCode::toString).collect(Collectors.toList()), builder)))
						.executes(context -> this.setTree(context.getSource(), blockPosArgument(context), speciesArgument(context),
								stringArgument(context, JO_CODE), DEFAULT_TURNS, DEFAULT_FERTILITY))
						.then(intArgument(TURNS).suggests(TURNS_SUGGESTIONS)
								.executes(context -> this.setTree(context.getSource(), blockPosArgument(context), speciesArgument(context),
										stringArgument(context, JO_CODE), intArgument(context, TURNS), DEFAULT_FERTILITY))
								.then(intArgument(FERTILITY).suggests(FERTILITY_SUGGESTIONS)
										.executes(context -> this.setTree(context.getSource(), blockPosArgument(context), speciesArgument(context),
												stringArgument(context, JO_CODE), intArgument(context, TURNS), intArgument(context, FERTILITY)))))));
	}

	private int setTree(final ServerCommandSource source, final BlockPos rootPos, final Species species, final String codeString, final int turns, final int fertility) {
		final ServerWorld world = source.getWorld();
		final JoCode joCode = species.getJoCode(codeString).rotate(Direction.fromHorizontal((3 - (turns % 4)) + 3)).setCareful(true);

		sendSuccessAndLog(source, Text.translatable("commands.dynamictrees.success.set_tree", CommandHelper.posComponent(rootPos),
				species.getTextComponent(), joCode.getTextComponent()));
		joCode.generate(world, world, species, rootPos, source.getWorld().getBiome(rootPos),
				Direction.SOUTH, 8, SafeChunkBounds.ANY, false);

		// Try to set the fertility.
		Null.consumeIfNonnull(TreeHelper.getRooty(world.getBlockState(rootPos)),
				rootyBlock -> rootyBlock.setFertility(world, rootPos, fertility));

		return 1;
	}

}
