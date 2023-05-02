package io.github.steveplays28.dynamictreesfabric.command;

import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.Registry;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * @author Harley O'Connor
 */
public final class DTArgumentTypes {

    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENT_TYPES = DeferredRegister.create(Registry.COMMAND_ARGUMENT_TYPE_REGISTRY, io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.MOD_ID);

    public static final RegistryObject<SingletonArgumentInfo<HexColorArgument>> HEX_COLOR = ARGUMENT_TYPES.register("hex_color", () -> ArgumentTypeInfos.registerByClass(HexColorArgument.class,
            SingletonArgumentInfo.contextFree(HexColorArgument::hex)));

}
