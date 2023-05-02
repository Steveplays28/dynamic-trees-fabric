package io.github.steveplays28.dynamictreesfabric.growthlogic;

import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.api.configurations.ConfigurableRegistry;
import io.github.steveplays28.dynamictreesfabric.api.configurations.ConfigurableRegistryEntry;
import io.github.steveplays28.dynamictreesfabric.api.configurations.ConfigurationProperty;
import io.github.steveplays28.dynamictreesfabric.growthlogic.context.DirectionManipulationContext;
import io.github.steveplays28.dynamictreesfabric.growthlogic.context.DirectionSelectionContext;
import io.github.steveplays28.dynamictreesfabric.growthlogic.context.PositionalSpeciesContext;
import io.github.steveplays28.dynamictreesfabric.init.DTTrees;
import io.github.steveplays28.dynamictreesfabric.systems.GrowSignal;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import io.github.steveplays28.dynamictreesfabric.util.MathHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A growth logic kit defines how/in what shape a tree should grow.
 */
public abstract class GrowthLogicKit extends ConfigurableRegistryEntry<GrowthLogicKit, GrowthLogicKitConfiguration> {

    /**
     * Sets the amount of psuedorandom height variation added to a tree. Helpful to prevent all trees from turning out
     * the same height.
     */
    public static final ConfigurationProperty<Integer> HEIGHT_VARIATION =
            ConfigurationProperty.integer("height_variation");

    public static final GrowthLogicKit DEFAULT = new GrowthLogicKit(DTTrees.NULL) {
        @Override
        public GrowthLogicKitConfiguration getDefaultConfiguration() {
            return this.defaultConfiguration;
        }
    };

    /**
     * Central registry for all {@link GrowthLogicKit} objects.
     */
    public static final ConfigurableRegistry<GrowthLogicKit, GrowthLogicKitConfiguration> REGISTRY =
            new ConfigurableRegistry<>(GrowthLogicKit.class, DEFAULT, GrowthLogicKitConfiguration.TEMPLATES);

    public GrowthLogicKit(final ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected GrowthLogicKitConfiguration createDefaultConfiguration() {
        return new GrowthLogicKitConfiguration(this);
    }

    @Override
    protected void registerProperties() {
    }

    /**
     * Selects and returns a new direction for the branch {@linkplain GrowSignal signal} to turn to.
     * <p>
     * This function uses a probability map to make the decision, populating it via {@link
     * #populateDirectionProbabilityMap(GrowthLogicKitConfiguration, DirectionManipulationContext)}. See that method for
     * more information on this map.
     *
     * @param configuration the configuration
     * @param context       the context
     * @return the direction for the signal to turn to
     */
    public Direction selectNewDirection(GrowthLogicKitConfiguration configuration, DirectionSelectionContext context) {
        // Prevent branches growing on the ground.
        if (context.signal().numSteps + 1 <= configuration.getLowestBranchHeight(
                new PositionalSpeciesContext(context.world(), context.signal().rootPos, context.species())
        ) && !context.signal().getSpecies().getLeavesProperties().canGrowOnGround()) {
            return Direction.UP;
        }

        // Populate the direction probability map.
        final int[] probMap = configuration.populateDirectionProbabilityMap(
                new DirectionManipulationContext(context.world(), context.pos(), context.species(), context.branch(),
                        context.signal(), context.branch().getRadius(context.world().getBlockState(context.pos())),
                        new int[6])
        );

        // Select a direction from the probability map.
        final int choice = MathHelper.selectRandomFromDistribution(context.signal().rand, probMap);
        return Direction.values()[choice != -1 ? choice : 1]; // Default to up if it failed.
    }

    /**
     * Populates the direction probability map as specified by the given {@code context}'s {@link
     * DirectionManipulationContext#probMap()}. This is effectively a weighted map used to make the decision of which
     * direction to turn. The index is equivalent to the index of the corresponding {@link Direction} whose probability
     * is being defined.
     * <p>
     * The default implementation uses the {@linkplain Species#getUpProbability() species' up probability} for the up
     * direction, the {@linkplain Species#getProbabilityForCurrentDir() current direction probability reinforcer} to
     *
     * @param configuration the configuration
     * @param context       the context
     * @return the populated probability map
     */
    public int[] populateDirectionProbabilityMap(GrowthLogicKitConfiguration configuration,
                                                 DirectionManipulationContext context) {
        final int[] probMap = context.probMap();
        final Direction originDir = context.signal().dir.getOpposite();

        // Use the up probability of the species, as long as the current direction is not down.
        probMap[Direction.UP.ordinal()] = context.signal().dir != Direction.DOWN ?
                context.species().getUpProbability() : 0;
        // Favour the current direction of travel as defined by the species.
        probMap[context.signal().dir.ordinal()] += context.species().getProbabilityForCurrentDir();

        for (Direction dir : Direction.values()) {
            if (!dir.equals(originDir)) {
                final BlockPos deltaPos = context.pos().relative(dir);
                // Check probability for surrounding blocks.
                // Typically, Air: 1, Leaves: 2, Branches: 2 + radius
                final BlockState deltaBlockState = context.world().getBlockState(deltaPos);
                probMap[dir.get3DDataValue()] += TreeHelper.getTreePart(deltaBlockState)
                        .probabilityForBlock(deltaBlockState, context.world(), deltaPos, context.branch());
            }
        }

        return probMap;
    }

    /**
     * Returns the energy for the tree. This effectively determines how high the branches can grow from the root.
     * Defaults to the {@linkplain Species#getSignalEnergy() species' signal energy}.
     * <p>
     * Note that the {@linkplain PositionalSpeciesContext#pos() position} in the specified {@code context} is the tree's
     * root position.
     *
     * @param configuration the configuration
     * @param context       the context
     * @return the energy for the current branch
     */
    public float getEnergy(GrowthLogicKitConfiguration configuration, PositionalSpeciesContext context) {
        return context.species().getSignalEnergy();
    }

    /**
     * Returns the lowest branch height for the tree.
     * <p>
     * Note that the {@linkplain PositionalSpeciesContext#pos() position} in the specified {@code context} is the tree's
     * root position.
     *
     * @param configuration the configuration
     * @param context       the context
     * @return the lowest branch height for the tree
     */
    public int getLowestBranchHeight(GrowthLogicKitConfiguration configuration, PositionalSpeciesContext context) {
        return context.species().getLowestBranchHeight();
    }

}
