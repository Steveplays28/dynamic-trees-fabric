package io.github.steveplays28.dynamictreesfabric.api.cells;

import net.minecraft.util.math.Direction;

public interface Cell {

    /**
     * @return The actual value of the cell.
     */
    int getValue();

    /**
     * Gets the value the cell returns for the given side.
     *
     * @param side The side's {@link Direction}.
     * @return The value for the given side.
     */
    int getValueFromSide(Direction side);

}
