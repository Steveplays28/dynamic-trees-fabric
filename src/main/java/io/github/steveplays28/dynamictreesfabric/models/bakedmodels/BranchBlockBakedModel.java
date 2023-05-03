package io.github.steveplays28.dynamictreesfabric.models.bakedmodels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.github.steveplays28.dynamictreesfabric.event.handlers.BakedModelEventHandler;
import net.minecraftforge.client.model.IDynamicBakedModel;

import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.util.Identifier;

/**
 * Holds general model data and sets up a {@link JsonUnbakedModel} for branch block baked models.
 *
 * <p>Main implementation is {@link BasicBranchBlockBakedModel}, which is the baked model
 * for dynamic branches with radius 1-8.</p>
 *
 * @author Harley O'Connor
 */
public abstract class BranchBlockBakedModel implements IDynamicBakedModel {

	/**
	 * A list of {@link BranchBlockBakedModel} instances, so that {@link #setupModels} can be called in {@link
	 * BakedModelEventHandler}.
	 */
	public static final List<BranchBlockBakedModel> INSTANCES = new ArrayList<>();

	protected final JsonUnbakedModel blockModel;

	protected final Identifier modelResLoc;
	protected final Identifier barkResLoc;
	protected final Identifier ringsResLoc;

	public BranchBlockBakedModel(Identifier modelResLoc, Identifier barkResLoc, Identifier ringsResLoc) {
		this.blockModel = new JsonUnbakedModel(null, new ArrayList<>(), new HashMap<>(), false, JsonUnbakedModel.GuiLight.ITEM, ModelTransformation.NONE, new ArrayList<>());

		this.modelResLoc = modelResLoc;
		this.barkResLoc = barkResLoc;
		this.ringsResLoc = ringsResLoc;

		INSTANCES.add(this);
	}

	/**
	 * BakedModelEventHandler#onModelBake(ModelBakeEvent)}, once the textures have been stitched and so can be baked
	 * onto models.
	 */
	public abstract void setupModels();

}
