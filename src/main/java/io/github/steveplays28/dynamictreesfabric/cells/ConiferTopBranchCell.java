package io.github.steveplays28.dynamictreesfabric.cells;

import io.github.steveplays28.dynamictreesfabric.api.cells.Cell;
import net.minecraft.util.math.Direction;

public class ConiferTopBranchCell implements Cell {

    @Override
    public int getValue() {
        return 5;
    }

    static final int[] map = {2, 5, 3, 3, 3, 3};

    //Used for giving more hydration if the below block is also a branch
    @Override
    public int getValueFromSide(Direction side) {
        return map[side.ordinal()];
    }

}
