package io.github.steveplays28.dynamictreesfabric.api.data;

import io.github.steveplays28.dynamictreesfabric.blocks.rootyblocks.SoilProperties;
import io.github.steveplays28.dynamictreesfabric.data.provider.DTBlockStateProvider;

/**
 * @author Harley O'Connor
 */
public final class WaterRootGenerator extends SoilStateGenerator {

	@Override
	public void generate(DTBlockStateProvider provider, SoilProperties input, Dependencies dependencies) {
		// TODO: Smart model for water roots.
		provider.simpleBlock(
				dependencies.get(SOIL),
				provider.models().getExistingFile(io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("block/roots_water"))
		);
	}

	@Override
	public Dependencies gatherDependencies(SoilProperties input) {
		return new Dependencies()
				.append(SOIL, input.getBlock());
	}

}
