package io.github.steveplays28.dynamictreesfabric.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * @author Harley O'Connor
 */
public final class ItemUtils {

    /**
     * Spawns an {@link ItemStack} as an {@link ItemEntity} in the {@link World} at the {@link BlockPos} given.
     *
     * @param world The {@link World} object to spawn the item in.
     * @param pos   The {@link BlockPos} object to spawn the item at.
     * @param stack The {@link ItemStack} to spawn.
     */
    public static void spawnItemStack(Level world, BlockPos pos, ItemStack stack) {
        spawnItemStack(world, pos, stack, false);
    }

    /**
     * Spawns an {@link ItemStack} as an {@link ItemEntity} in the {@link World} at the {@link BlockPos} given.
     *
     * @param world        The {@link World} object to spawn the item in.
     * @param pos          The {@link BlockPos} object to spawn the item at.
     * @param stack        The {@link ItemStack} to spawn.
     * @param searchForAir If true, searches for air for the item to spawn in.
     */
    public static void spawnItemStack(Level world, BlockPos pos, ItemStack stack, boolean searchForAir) {
        if (searchForAir) {
            // Goes up one block at a time until an air block to spawn on is found.
            while (!world.isEmptyBlock(pos)) {
                pos = pos.above();
            }
        }

        // Create the item entity, spawning it in the centre of the position given.
        final ItemEntity itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
        // Make sure the item entity has no motion.
        itemEntity.setDeltaMovement(0, 0, 0);
        // Add (spawn) the item to the world.
        world.addFreshEntity(itemEntity);
    }

}
