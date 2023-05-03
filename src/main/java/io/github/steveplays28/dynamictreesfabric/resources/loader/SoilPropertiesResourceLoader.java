package io.github.steveplays28.dynamictreesfabric.resources.loader;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.steveplays28.dynamictreesfabric.api.resource.loading.preparation.JsonRegistryResourceLoader;
import io.github.steveplays28.dynamictreesfabric.api.treepacks.ApplierRegistryEvent;
import io.github.steveplays28.dynamictreesfabric.blocks.rootyblocks.RootyBlock;
import io.github.steveplays28.dynamictreesfabric.blocks.rootyblocks.SoilHelper;
import io.github.steveplays28.dynamictreesfabric.blocks.rootyblocks.SoilProperties;
import io.github.steveplays28.dynamictreesfabric.blocks.rootyblocks.SpreadableSoilProperties;
import io.github.steveplays28.dynamictreesfabric.deserialisation.JsonHelper;
import io.github.steveplays28.dynamictreesfabric.deserialisation.ResourceLocationDeserialiser;
import io.github.steveplays28.dynamictreesfabric.deserialisation.result.JsonResult;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolderRegistry;

/**
 * @author Max Hyper and Harley O'Connor
 */
public final class SoilPropertiesResourceLoader extends JsonRegistryResourceLoader<SoilProperties> {

	public SoilPropertiesResourceLoader() {
		super(SoilProperties.REGISTRY, ApplierRegistryEvent.SOIL_PROPERTIES);
	}

	@Override
	public void registerAppliers() {
		this.loadAppliers.register("substitute_soil", String.class, (soilProperties, substitute) ->
				soilProperties.setHasSubstitute(true)
		);

		// Primitive soil is needed before gathering data.
		this.gatherDataAppliers.register("primitive_soil", Block.class, SoilProperties::setPrimitiveSoilBlock);

		this.setupAppliers.register("primitive_soil", Block.class, SoilProperties::setPrimitiveSoilBlock);

		this.registerSpreadableAppliers();

		this.commonAppliers.registerArrayApplier("acceptable_soils", String.class, this::registerAcceptableSoil)
				.register("worldgen_only", Boolean.class, SoilProperties::setWorldGenOnly);

		super.registerAppliers();
	}

	private void registerAcceptableSoil(SoilProperties soilProperties, String acceptableSoil) {
		if (SoilHelper.getSoilFlags(acceptableSoil) == 0) {
			SoilHelper.createNewAdjective(acceptableSoil);
		}
		SoilHelper.registerSoil(soilProperties, acceptableSoil);
	}

	private void registerSpreadableAppliers() {
		this.reloadAppliers
				.register("required_light", SpreadableSoilProperties.class, Integer.class,
						SpreadableSoilProperties::setRequiredLight)
				.register("spread_item", SpreadableSoilProperties.class, Item.class,
						SpreadableSoilProperties::setSpreadItem)
				.registerArrayApplier("spreadable_soils", SpreadableSoilProperties.class, SoilProperties.class,
						(properties, soil) -> SoilProperties.REGISTRY.runOnNextLock(
								() -> properties.addSpreadableSoils(soil)
						));
	}

	@Override
	protected void applyLoadAppliers(LoadData loadData, JsonObject json) {
		this.readCustomBlockRegistryName(loadData.getResource(), json);
		super.applyLoadAppliers(loadData, json);
	}

	private void readCustomBlockRegistryName(SoilProperties soilProperties, JsonObject json) {
		JsonResult.forInput(json)
				.mapIfContains("block_registry_name", JsonElement.class, input ->
						ResourceLocationDeserialiser.create(soilProperties.getRegistryName().getNamespace())
								.deserialise(input).orElseThrow(), soilProperties.getBlockRegistryName()
				).ifSuccessOrElse(
						soilProperties::setBlockRegistryName,
						error -> this.logError(soilProperties.getRegistryName(), error),
						warning -> this.logWarning(soilProperties.getRegistryName(), warning)
				);
	}

	@Override
	protected void postLoadOnLoad(LoadData loadData, JsonObject json) {
		super.postLoadOnLoad(loadData, json);
		this.setSubstituteSoil(loadData.getResource(), json);
	}

	private void setSubstituteSoil(SoilProperties soilProperties, JsonObject json) {
		SoilProperties substitute = JsonHelper.getOrDefault(json, "substitute_soil", SoilProperties.class,
				SoilProperties.NULL_SOIL_PROPERTIES);

		if (substitute.isValid()) {
			this.useSubstituteSoilBlock(soilProperties, substitute);
		} else {
			this.generateSoilBlock(soilProperties, json);
		}
	}

	private void useSubstituteSoilBlock(SoilProperties soilProperties, SoilProperties substitute) {
		ObjectHolderRegistry.addHandler(registryNameFilter -> {
			if (registryNameFilter.test(ForgeRegistries.Keys.BLOCKS.location())) {
				if (ForgeRegistries.BLOCKS.getValue(substitute.getBlockRegistryName()) instanceof RootyBlock rootyBlock) {
					soilProperties.setBlock(rootyBlock);
				}
			}
		});
	}

	private void generateSoilBlock(SoilProperties soilProperties, JsonObject json) {
		soilProperties.generateBlock(JsonHelper.getBlockProperties(
				json,
				soilProperties.getDefaultMaterial(),
				soilProperties.getDefaultMaterial().getColor(),
				soilProperties::getDefaultBlockProperties,
				error -> this.logError(soilProperties.getRegistryName(), error),
				warning -> this.logWarning(soilProperties.getRegistryName(), warning)
		));
	}

}
