package io.github.steveplays28.dynamictreesfabric.cells;

import io.github.steveplays28.dynamictreesfabric.api.cells.Cell;
import net.minecraft.util.math.Direction;

/**
 * Cell that simply returns it's value
 *
 * @author ferreusveritas
 */
public class NormalCell implements Cell {

    private final int value;

    public NormalCell(int value) {
        this.value = value;
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public int getValueFromSide(Direction side) {
        return value;
    }

}
