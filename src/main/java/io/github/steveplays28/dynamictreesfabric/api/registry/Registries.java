package io.github.steveplays28.dynamictreesfabric.api.registry;

import io.github.steveplays28.dynamictreesfabric.api.cells.CellKit;
import io.github.steveplays28.dynamictreesfabric.blocks.leaves.LeavesProperties;
import io.github.steveplays28.dynamictreesfabric.blocks.rootyblocks.SoilProperties;
import io.github.steveplays28.dynamictreesfabric.growthlogic.GrowthLogicKit;
import io.github.steveplays28.dynamictreesfabric.systems.dropcreators.DropCreator;
import io.github.steveplays28.dynamictreesfabric.systems.genfeatures.GenFeature;
import io.github.steveplays28.dynamictreesfabric.trees.Family;
import io.github.steveplays28.dynamictreesfabric.trees.Species;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Holds all registries in an ordered list.
 *
 * @author Harley O'Connor
 */
public final class Registries {

    public static final List<Registry<?>> REGISTRIES = new ArrayList<>(
            Arrays.asList(
                    RegistryHandler.REGISTRY,
                    CellKit.REGISTRY,
                    LeavesProperties.REGISTRY,
                    GrowthLogicKit.REGISTRY,
                    Family.REGISTRY,
                    GenFeature.REGISTRY,
                    DropCreator.REGISTRY,
                    Species.REGISTRY,
                    SoilProperties.REGISTRY
            )
    );

}
