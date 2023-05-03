package io.github.steveplays28.dynamictreesfabric.command;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.mojang.brigadier.builder.ArgumentBuilder;
import io.github.steveplays28.dynamictreesfabric.api.registry.Registries;

import net.minecraft.server.command.ServerCommandSource;

/**
 * @author Harley O'Connor
 */
public final class RegistryCommand extends SubCommand {

	private final List<RegistrySubCommand<?>> subCommands = Lists.newArrayList();

	public RegistryCommand() {
		Registries.REGISTRIES.forEach(registry -> subCommands.add(new RegistrySubCommand<>(registry)));
	}

	@Override
	protected String getName() {
		return "registry";
	}

	@Override
	protected int getPermissionLevel() {
		return 0;
	}

	@Override
	protected List<ArgumentBuilder<ServerCommandSource, ?>> registerArguments() {
		return this.subCommands.stream().map(SubCommand::register)
				.collect(Collectors.toList());
	}

	@Override
	public ArgumentBuilder<ServerCommandSource, ?> registerArgument() {
		return stringArgument("null");
	}

}
