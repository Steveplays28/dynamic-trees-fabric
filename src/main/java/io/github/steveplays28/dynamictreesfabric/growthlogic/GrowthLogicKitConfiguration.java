package io.github.steveplays28.dynamictreesfabric.growthlogic;

import io.github.steveplays28.dynamictreesfabric.api.configurations.Configuration;
import io.github.steveplays28.dynamictreesfabric.api.configurations.TemplateRegistry;
import io.github.steveplays28.dynamictreesfabric.growthlogic.context.DirectionManipulationContext;
import io.github.steveplays28.dynamictreesfabric.growthlogic.context.DirectionSelectionContext;
import io.github.steveplays28.dynamictreesfabric.growthlogic.context.PositionalSpeciesContext;

import net.minecraft.util.math.Direction;

/**
 * @author Harley O'Connor
 */
public final class GrowthLogicKitConfiguration extends Configuration<GrowthLogicKitConfiguration, GrowthLogicKit> {

	public static final TemplateRegistry<GrowthLogicKitConfiguration> TEMPLATES = new TemplateRegistry<>();

	public GrowthLogicKitConfiguration(GrowthLogicKit configurable) {
		super(configurable);
	}

	public static GrowthLogicKitConfiguration getDefault() {
		return GrowthLogicKit.DEFAULT.getDefaultConfiguration();
	}

	@Override
	public GrowthLogicKitConfiguration copy() {
		final GrowthLogicKitConfiguration duplicateLogicKit = new GrowthLogicKitConfiguration(this.configurable);
		duplicateLogicKit.properties.putAll(this.properties);
		return duplicateLogicKit;
	}

	/**
	 * Invokes {@link GrowthLogicKit#selectNewDirection(GrowthLogicKitConfiguration, DirectionSelectionContext)} for this
	 * configured kit's growth logic kit.
	 *
	 * @param context the context
	 * @return the direction for the signal to turn to
	 * @see GrowthLogicKit#selectNewDirection(GrowthLogicKitConfiguration, DirectionSelectionContext)
	 */
	public Direction selectNewDirection(DirectionSelectionContext context) {
		return this.configurable.selectNewDirection(this, context);
	}

	/**
	 * Invokes {@link GrowthLogicKit#populateDirectionProbabilityMap(GrowthLogicKitConfiguration,
	 * DirectionManipulationContext)} for this configured kit's growth logic kit.
	 *
	 * @param context the context
	 * @return the direction for the signal to turn to
	 * @see GrowthLogicKit#populateDirectionProbabilityMap(GrowthLogicKitConfiguration, DirectionManipulationContext)
	 */
	public int[] populateDirectionProbabilityMap(DirectionManipulationContext context) {
		return this.configurable.populateDirectionProbabilityMap(this, context);
	}

	/**
	 * Invokes {@link GrowthLogicKit#getEnergy(GrowthLogicKitConfiguration, PositionalSpeciesContext)} for this configured
	 * kit's growth logic kit.
	 *
	 * @param context the context
	 * @return the direction for the signal to turn to
	 * @see GrowthLogicKit#getEnergy(GrowthLogicKitConfiguration, PositionalSpeciesContext)
	 */
	public float getEnergy(PositionalSpeciesContext context) {
		return this.configurable.getEnergy(this, context);
	}

	/**
	 * Invokes {@link GrowthLogicKit#getLowestBranchHeight(GrowthLogicKitConfiguration, PositionalSpeciesContext)} for this
	 * configured kit's growth logic kit.
	 *
	 * @param context the context
	 * @return the direction for the signal to turn to
	 * @see GrowthLogicKit#getLowestBranchHeight(GrowthLogicKitConfiguration, PositionalSpeciesContext)
	 */
	public int getLowestBranchHeight(PositionalSpeciesContext context) {
		return this.configurable.getLowestBranchHeight(this, context);
	}

}
