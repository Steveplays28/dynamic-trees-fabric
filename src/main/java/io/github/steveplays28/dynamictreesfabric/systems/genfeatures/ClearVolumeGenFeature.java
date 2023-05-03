package io.github.steveplays28.dynamictreesfabric.systems.genfeatures;

import io.github.steveplays28.dynamictreesfabric.api.configurations.ConfigurationProperty;
import io.github.steveplays28.dynamictreesfabric.systems.genfeatures.context.PreGenerationContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public class ClearVolumeGenFeature extends GenFeature {

    public static final ConfigurationProperty<Integer> HEIGHT = ConfigurationProperty.integer("height");

    public ClearVolumeGenFeature(Identifier registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
        this.register(HEIGHT);
    }

    @Override
    public GenFeatureConfiguration createDefaultConfiguration() {
        return super.createDefaultConfiguration()
                .with(HEIGHT, 8);
    }

    @Override
    protected BlockPos preGenerate(GenFeatureConfiguration configuration, PreGenerationContext context) {
        final BlockPos rootPos = context.pos();

        // Erase a volume of blocks that could potentially get in the way.
        for (BlockPos pos : BlockPos.iterate(
                rootPos.add(new Vec3i(-1, 1, -1)),
                rootPos.add(new Vec3i(1, configuration.get(HEIGHT), 1))
        )) {
            context.world().removeBlock(pos, false);
        }

        return rootPos;
    }

}
