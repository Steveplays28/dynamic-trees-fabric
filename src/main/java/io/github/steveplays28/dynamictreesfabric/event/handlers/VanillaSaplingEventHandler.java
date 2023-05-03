package io.github.steveplays28.dynamictreesfabric.event.handlers;

import io.github.steveplays28.dynamictreesfabric.api.TreeRegistry;
import io.github.steveplays28.dynamictreesfabric.blocks.DynamicSaplingBlock;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import io.github.steveplays28.dynamictreesfabric.util.ItemUtils;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.SaplingGrowTreeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class VanillaSaplingEventHandler {

    @SubscribeEvent
    public void onPlayerPlaceBlock(BlockEvent.EntityPlaceEvent event) {
        final BlockState state = event.getPlacedBlock();

        if (!(event.getLevel() instanceof World world) || !TreeRegistry.SAPLING_REPLACERS.containsKey(state)) {
            return;
        }

        final BlockPos pos = event.getPos();
        final Species targetSpecies = TreeRegistry.SAPLING_REPLACERS.get(state);

        // If we should be overriding for this location, then correct the species to the override.
        final Species species = targetSpecies.selfOrLocationOverride(world, pos);

        world.removeBlock(pos, false); // Remove the block so the plantTree function won't automatically fail.

        if (!species.plantSapling(world, pos, targetSpecies != species)) { // If it fails then give a seed back to the player.
            ItemUtils.spawnItemStack(world, pos, species.getSeedStack(1));
        }
    }

    @SubscribeEvent
    public void onSaplingGrowTree(SaplingGrowTreeEvent event) {
        final WorldAccess iWorld = event.getLevel();
        final BlockPos pos = event.getPos();
        final BlockState blockState = iWorld.getBlockState(pos);

        if (!(iWorld instanceof World) || !TreeRegistry.SAPLING_REPLACERS.containsKey(blockState)) {
            return;
        }

        final World world = ((World) iWorld);
        final Species species = TreeRegistry.SAPLING_REPLACERS.get(blockState)
                .selfOrLocationOverride(world, pos);

        world.removeBlock(pos, false); // Remove the block so the plantTree function won't automatically fail.
        event.setResult(Event.Result.DENY);

        if (species.isValid()) {
            if (DynamicSaplingBlock.canSaplingStay(world, species, pos)) {
                species.transitionToTree(world, pos);
            }
        }
    }

}
