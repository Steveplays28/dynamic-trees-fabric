package io.github.steveplays28.dynamictreesfabric.command;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.registry.Registry;

/**
 * @author Harley O'Connor
 */
public final class DTArgumentTypes {

	public static final DeferredRegister<ArgumentSerializer<?, ?>> ARGUMENT_TYPES = DeferredRegister.create(Registry.COMMAND_ARGUMENT_TYPE_REGISTRY, io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.MOD_ID);

	public static final RegistryObject<ConstantArgumentSerializer<HexColorArgument>> HEX_COLOR = ARGUMENT_TYPES.register("hex_color", () -> ArgumentTypeInfos.registerByClass(HexColorArgument.class,
			SingletonArgumentInfo.contextFree(HexColorArgument::hex)));

}
