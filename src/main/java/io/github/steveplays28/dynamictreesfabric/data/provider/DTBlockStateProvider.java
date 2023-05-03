package io.github.steveplays28.dynamictreesfabric.data.provider;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;
import io.github.steveplays28.dynamictreesfabric.api.registry.Registry;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import net.minecraft.data.DataGenerator;

/**
 * @author Harley O'Connor
 */
public class DTBlockStateProvider extends BlockStateProvider implements DTDataProvider {

	private final String modId;
	private final List<Registry<?>> registries;

	public DTBlockStateProvider(DataGenerator generator, String modId, ExistingFileHelper existingFileHelper,
	                            Collection<Registry<?>> registries) {
		super(generator, modId, existingFileHelper);
		this.modId = modId;
		this.registries = ImmutableList.copyOf(registries);
	}

	@Override
	protected void registerStatesAndModels() {
		this.registries.forEach(registry ->
				registry.dataGenerationStream(this.modId).forEach(entry ->
						entry.generateStateData(this)
				)
		);
	}

}
