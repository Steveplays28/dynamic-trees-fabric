package io.github.steveplays28.dynamictreesfabric.models.loaders;

import io.github.steveplays28.dynamictreesfabric.models.geometry.BranchBlockModelGeometry;
import net.minecraft.util.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

/**
 * @author Harley O'Connor
 */
@OnlyIn(Dist.CLIENT)
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
