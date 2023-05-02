package io.github.steveplays28.dynamictreesfabric.systems.dropcreators.context;

import io.github.steveplays28.dynamictreesfabric.systems.nodemappers.NetVolumeNode;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * @author Harley O'Connor
 */
public class LogDropContext extends DropContext {

    private final NetVolumeNode.Volume volume;

    public LogDropContext(Level world, BlockPos pos, Species species, List<ItemStack> dropList, NetVolumeNode.Volume volume, ItemStack tool) {
        super(world, pos, species, dropList, tool, -1, 0);
        this.volume = volume;
    }

    public NetVolumeNode.Volume volume() {
        return volume;
    }

}
