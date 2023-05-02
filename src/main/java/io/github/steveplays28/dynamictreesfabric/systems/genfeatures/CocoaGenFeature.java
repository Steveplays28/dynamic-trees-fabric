package io.github.steveplays28.dynamictreesfabric.systems.genfeatures;

import io.github.steveplays28.dynamictreesfabric.api.GeneratesFruit;
import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.api.network.MapSignal;
import io.github.steveplays28.dynamictreesfabric.systems.genfeatures.context.PostGenerationContext;
import io.github.steveplays28.dynamictreesfabric.systems.genfeatures.context.PostGrowContext;
import io.github.steveplays28.dynamictreesfabric.systems.nodemappers.CocoaFruitNode;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

@GeneratesFruit
public class CocoaGenFeature extends GenFeature {

    public CocoaGenFeature(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
    }

    @Override
    protected boolean postGrow(GenFeatureConfiguration configuration, PostGrowContext context) {
        if (context.fertility() == 0 && context.random().nextInt() % 16 == 0) {
            final Level world = context.world();
            if (context.species().seasonalFruitProductionFactor(world, context.treePos()) > context.random().nextFloat()) {
                this.addCocoa(world, context.pos(), false);
            }
        }
        return false;
    }

    @Override
    protected boolean postGenerate(GenFeatureConfiguration configuration, PostGenerationContext context) {
        if (context.random().nextInt() % 8 == 0) {
            this.addCocoa(context.world(), context.pos(), true);
            return true;
        }
        return false;
    }

    private void addCocoa(LevelAccessor world, BlockPos rootPos, boolean worldGen) {
        TreeHelper.startAnalysisFromRoot(world, rootPos, new MapSignal(new CocoaFruitNode().setWorldGen(worldGen)));
    }

}
