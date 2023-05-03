package io.github.steveplays28.dynamictreesfabric.api.data;

import io.github.steveplays28.dynamictreesfabric.trees.Family;

/**
 * @author Harley O'Connor
 */
public class StrippedBranchStateGenerator extends BranchStateGenerator {

	@Override
	public Dependencies gatherDependencies(Family input) {
		return new Dependencies()
				.append(BRANCH, input.getStrippedBranch())
				.append(PRIMITIVE_LOG, input.getPrimitiveStrippedLog());
	}

}
