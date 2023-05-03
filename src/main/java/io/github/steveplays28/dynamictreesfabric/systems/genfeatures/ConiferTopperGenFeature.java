package io.github.steveplays28.dynamictreesfabric.systems.genfeatures;

import java.util.Collections;
import java.util.Comparator;

import io.github.steveplays28.dynamictreesfabric.api.configurations.ConfigurationProperty;
import io.github.steveplays28.dynamictreesfabric.blocks.leaves.LeavesProperties;
import io.github.steveplays28.dynamictreesfabric.systems.genfeatures.context.PostGenerationContext;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.WorldAccess;

public class ConiferTopperGenFeature extends GenFeature {

	public static final ConfigurationProperty<LeavesProperties> LEAVES_PROPERTIES = ConfigurationProperty.property("leaves_properties", LeavesProperties.class);

	public ConiferTopperGenFeature(Identifier registryName) {
		super(registryName);
	}

	@Override
	protected void registerProperties() {
		this.register(LEAVES_PROPERTIES);
	}

	@Override
	public GenFeatureConfiguration createDefaultConfiguration() {
		return super.createDefaultConfiguration()
				.with(LEAVES_PROPERTIES, LeavesProperties.NULL_PROPERTIES);
	}

	@Override
	protected boolean postGenerate(GenFeatureConfiguration configuration, PostGenerationContext context) {
		if (context.endPoints().isEmpty()) {
			return false;
		}

		final WorldAccess world = context.world();

		// Find the highest end point.
		final BlockPos highest = Collections.max(context.endPoints(), Comparator.comparingInt(Vec3i::getY));
		// Fetch leaves properties property set or the default for the Species.
		final LeavesProperties leavesProperties = configuration.get(LEAVES_PROPERTIES)
				.elseIfInvalid(context.species().getLeavesProperties());

		// Manually place the highest few blocks of the conifer since the LeafCluster voxmap won't handle it.
		world.setBlockState(highest.up(1), leavesProperties.getDynamicLeavesState(4), 3);
		world.setBlockState(highest.up(2), leavesProperties.getDynamicLeavesState(3), 3);
		world.setBlockState(highest.up(3), leavesProperties.getDynamicLeavesState(1), 3);

		return true;
	}

}
