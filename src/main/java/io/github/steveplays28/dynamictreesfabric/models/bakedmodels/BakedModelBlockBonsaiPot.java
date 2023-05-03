package io.github.steveplays28.dynamictreesfabric.models.bakedmodels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.steveplays28.dynamictreesfabric.client.QuadManipulator;
import io.github.steveplays28.dynamictreesfabric.tileentity.PottedSaplingTileEntity;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import net.minecraftforge.client.model.IDynamicBakedModel;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class BakedModelBlockBonsaiPot implements IDynamicBakedModel {

	protected BakedModel basePotModel;
	protected Map<Species, List<BakedQuad>> cachedSaplingQuads = new HashMap<>();

	public BakedModelBlockBonsaiPot(BakedModel basePotModel) {
		this.basePotModel = basePotModel;
	}

	@NotNull
	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull Random rand, @NotNull ModelData extraData, @Nullable RenderLayer renderType) {
		List<BakedQuad> quads = new ArrayList<>();

		if (side != null || state == null || !extraData.has(PottedSaplingTileEntity.SPECIES) || !extraData.has(PottedSaplingTileEntity.POT_MIMIC)) {
			return quads;
		}

		final Species species = extraData.get(PottedSaplingTileEntity.SPECIES);
		final BlockState potState = extraData.get(PottedSaplingTileEntity.POT_MIMIC);

		if (species == null || potState == null || !species.isValid() || !species.getSapling().isPresent()) {
			return quads;
		}

		final BlockState saplingState = species.getSapling().get().getDefaultState();

		BlockRenderManager dispatcher = MinecraftClient.getInstance().getBlockRenderManager();
		BakedModel potModel = dispatcher.getModel(potState);
		BakedModel saplingModel = dispatcher.getModel(saplingState);

		quads.addAll(potModel.getQuads(potState, side, rand, extraData, renderType));
		quads.addAll(cachedSaplingQuads.computeIfAbsent(species, s -> QuadManipulator.getQuads(saplingModel, saplingState, new Vec3d(0, 0.25, 0), rand, extraData)));

		return quads;
	}

	@Override
	public boolean useAmbientOcclusion() {
		return this.basePotModel.useAmbientOcclusion();
	}

	@Override
	public boolean isGui3d() {
		return true;
	}

	@Override
	public boolean isCustomRenderer() {
		return true;
	}

	@Override
	public boolean usesBlockLight() {
		return false;
	}

	@Override
	public Sprite getParticleIcon() {
		return this.basePotModel.getParticleSprite();
	}

	@Override
	public ModelOverrideList getOverrides() {
		return ModelOverrideList.EMPTY;
	}

}
