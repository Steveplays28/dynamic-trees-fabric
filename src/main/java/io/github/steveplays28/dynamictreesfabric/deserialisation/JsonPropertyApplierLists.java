package io.github.steveplays28.dynamictreesfabric.deserialisation;

import net.minecraft.block.AbstractBlock;
import net.minecraft.sound.BlockSoundGroup;

/**
 * Holds common {@link JsonPropertyAppliers} objects.
 *
 * @author Harley O'Connor
 */
public final class JsonPropertyApplierLists {

	public static final JsonPropertyAppliers<AbstractBlock.Settings> PROPERTIES = new JsonPropertyAppliers<>(AbstractBlock.Settings.class)
			.registerIfTrueApplier("does_not_block_movement", AbstractBlock.Settings::noCollision)
			.registerIfTrueApplier("not_solid", AbstractBlock.Settings::nonOpaque)
//            .register("harvest_level", Integer.class, BlockBehaviour.Properties::harvestLevel)
//            .register("harvest_tool", ToolType.class, BlockBehaviour.Properties::harvestTool)
			.register("slipperiness", Float.class, AbstractBlock.Settings::slipperiness)
			.register("speed_factor", Float.class, AbstractBlock.Settings::velocityMultiplier)
			.register("jump_factor", Float.class, AbstractBlock.Settings::jumpVelocityMultiplier)
			.register("sound", BlockSoundGroup.class, AbstractBlock.Settings::sounds)
			.register("hardness", Float.class, (properties, hardness) -> properties.strength(hardness, properties.resistance))
			.register("resistance", Float.class, (properties, resistance) -> properties.strength(properties.hardness, resistance))
			.registerIfTrueApplier("zero_hardness_and_resistance", AbstractBlock.Settings::breakInstantly)
			.register("hardness_and_resistance", Float.class, AbstractBlock.Settings::strength)
			.register("light", Integer.class, (properties, light) -> properties.luminance(state -> light))
			.registerIfTrueApplier("tick_randomly", AbstractBlock.Settings::ticksRandomly)
			.registerIfTrueApplier("variable_opacity", AbstractBlock.Settings::dynamicBounds)
			.registerIfTrueApplier("no_drops", AbstractBlock.Settings::dropsNothing)
			.registerIfTrueApplier("air", AbstractBlock.Settings::air)
			.registerIfTrueApplier("requires_tool", AbstractBlock.Settings::requiresTool);

}
