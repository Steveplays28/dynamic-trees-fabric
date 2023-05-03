package io.github.steveplays28.dynamictreesfabric.models.loaders;

import javax.annotation.Nullable;

import io.github.steveplays28.dynamictreesfabric.models.geometry.BranchBlockModelGeometry;

import net.minecraft.util.Identifier;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ThickBranchBlockModelLoader extends BranchBlockModelLoader {

	@Override
	protected BranchBlockModelGeometry getModelGeometry(Identifier barkResLoc, Identifier ringsResLoc, @Nullable Identifier familyResLoc) {
		return new BranchBlockModelGeometry(barkResLoc, ringsResLoc, familyResLoc, true);
	}

	@Override
	protected String getModelTypeName() {
		return "Thick Branch";
	}

}
