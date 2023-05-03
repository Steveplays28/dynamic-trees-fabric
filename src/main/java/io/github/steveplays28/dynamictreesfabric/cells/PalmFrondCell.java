package io.github.steveplays28.dynamictreesfabric.cells;

public class PalmFrondCell extends MatrixCell {

	static final byte[] valMap = {
			0, 0, 0, 0, 0, 0, 0, 0, //D Maps * -> 0
			0, 0, 0, 0, 4, 0, 0, 0, //U Maps 4 -> 4, * -> 0
			0, 1, 2, 3, 0, 0, 0, 0, //N Maps 4 -> 0, * -> *
			0, 1, 2, 3, 0, 0, 0, 0, //S Maps 4 -> 0, * -> *
			0, 1, 2, 3, 0, 0, 0, 0, //W Maps 4 -> 0, * -> *
			0, 1, 2, 3, 0, 0, 0, 0  //E Maps 4 -> 0, * -> *
	};

	public PalmFrondCell(int value) {
		super(value, valMap);
	}

}
