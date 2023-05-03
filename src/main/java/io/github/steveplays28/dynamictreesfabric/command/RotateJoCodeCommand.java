package io.github.steveplays28.dynamictreesfabric.command;

import static io.github.steveplays28.dynamictreesfabric.command.CommandConstants.*;

import java.util.Collections;

import com.mojang.brigadier.builder.ArgumentBuilder;
import io.github.steveplays28.dynamictreesfabric.worldgen.JoCode;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;

public final class RotateJoCodeCommand extends SubCommand {

	@Override
	protected String getName() {
		return CommandConstants.ROTATE_JO_CODE;
	}

	@Override
	protected int getPermissionLevel() {
		return 0;
	}

	@Override
	public ArgumentBuilder<ServerCommandSource, ?> registerArgument() {
		return stringArgument(JO_CODE, Collections.singleton(DEFAULT_JO_CODE)).then(intArgument(TURNS).suggests(TURNS_SUGGESTIONS))
				.executes(context -> executesSuccess(() ->
						this.rotateJoCode(context.getSource(), stringArgument(context, JO_CODE), intArgument(context, TURNS))));
	}

	private void rotateJoCode(final ServerCommandSource source, final String code, final int turns) {
		sendSuccess(source, Text.translatable("commands.dynamictrees.success.rotate_jo_code",
				new JoCode(code).rotate(Direction.fromHorizontal((3 - (turns % 4)) + 3)).getTextComponent()));
	}

}
