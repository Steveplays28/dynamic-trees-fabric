package io.github.steveplays28.dynamictreesfabric.command;

import java.util.Collections;

import com.mojang.brigadier.builder.ArgumentBuilder;
import io.github.steveplays28.dynamictreesfabric.util.CoordUtils;

import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public final class SetCoordXorCommand extends SubCommand {

	private static final String XOR = "xor";

	@Override
	protected String getName() {
		return CommandConstants.SET_COORD_XOR;
	}

	@Override
	protected int getPermissionLevel() {
		return 2;
	}

	@Override
	public ArgumentBuilder<ServerCommandSource, ?> registerArgument() {
		return intArgument(XOR).suggests(((context, builder) -> CommandSource.suggestMatching(Collections.singletonList("0"), builder)))
				.executes(context -> executesSuccess(() -> this.setXor(context.getSource(), intArgument(context, XOR))));
	}

	private void setXor(final ServerCommandSource source, final int xor) {
		CoordUtils.coordXor = xor;
		sendSuccessAndLog(source, Text.translatable("commands.dynamictrees.success.set_xor", aqua(xor)));
	}

}
