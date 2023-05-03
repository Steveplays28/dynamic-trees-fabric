package io.github.steveplays28.dynamictreesfabric.cells;

import io.github.steveplays28.dynamictreesfabric.api.cells.Cell;

import net.minecraft.util.math.Direction;

public class ConiferBranchCell implements Cell {

	static final int[] map = {2, 2, 3, 3, 3, 3};

	@Override
	public int getValue() {
		return 5;
	}

	@Override
	public int getValueFromSide(Direction side) {
		return map[side.ordinal()];
	}

}
