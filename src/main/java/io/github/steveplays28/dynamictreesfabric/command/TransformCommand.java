package io.github.steveplays28.dynamictreesfabric.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.api.network.MapSignal;
import io.github.steveplays28.dynamictreesfabric.blocks.rootyblocks.RootyBlock;
import io.github.steveplays28.dynamictreesfabric.compat.waila.WailaOther;
import io.github.steveplays28.dynamictreesfabric.systems.nodemappers.TransformNode;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import io.github.steveplays28.dynamictreesfabric.util.CommandHelper;

import net.minecraft.block.BlockState;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * @author Harley O'Connor
 */
public final class TransformCommand extends SubCommand {

	private static final Dynamic2CommandExceptionType SPECIES_EQUAL = new Dynamic2CommandExceptionType((toSpecies, fromSpecies) -> Text.translatable("commands.dynamictrees.error.species_equal", darkRed(toSpecies), darkRed(fromSpecies)));

	@Override
	protected String getName() {
		return CommandConstants.TRANSFORM;
	}

	@Override
	protected int getPermissionLevel() {
		return 2;
	}

	@Override
	public ArgumentBuilder<ServerCommandSource, ?> registerArgument() {
		return blockPosArgument().then(transformableSpeciesArgument().executes(context -> executesSuccess(() ->
				this.transformSpecies(context.getSource(), rootPosArgument(context), speciesArgument(context)))));
	}

	private void transformSpecies(final ServerCommandSource source, final BlockPos rootPos, final Species toSpecies) throws CommandSyntaxException {
		final World world = source.getWorld();

		final Species fromSpecies = TreeHelper.getExactSpecies(world, rootPos);

		if (toSpecies == fromSpecies) {
			throw SPECIES_EQUAL.create(toSpecies.getTextComponent(), fromSpecies.getTextComponent());
		}

		if (!toSpecies.isTransformable() || !fromSpecies.isTransformable()) {
			throw SPECIES_NOT_TRANSFORMABLE.create(!toSpecies.isTransformable() ? toSpecies.getTextComponent() : fromSpecies.getTextComponent());
		}

		final BlockState rootyState = world.getBlockState(rootPos);
		final RootyBlock rootyBlock = ((RootyBlock) rootyState.getBlock());

		// Transform tree.
		rootyBlock.startAnalysis(world, rootPos, new MapSignal(new TransformNode(fromSpecies, toSpecies)));

		if (rootyBlock.getSpecies(rootyState, world, rootPos) != toSpecies) {
			// Place new rooty dirt block if transforming to species that requires tile entity.
			toSpecies.placeRootyDirtBlock(world, rootPos, rootyBlock.getFertility(rootyState, world, rootPos));
		}

		sendSuccessAndLog(source, Text.translatable("commands.dynamictrees.success.transform",
				fromSpecies.getTextComponent(), CommandHelper.posComponent(rootPos, Formatting.AQUA),
				toSpecies.getTextComponent()));

		WailaOther.invalidateWailaPosition();
	}
}
