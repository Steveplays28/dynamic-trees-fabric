package io.github.steveplays28.dynamictreesfabric.systems.genfeatures;

import io.github.steveplays28.dynamictreesfabric.api.configurations.ConfigurationProperty;
import io.github.steveplays28.dynamictreesfabric.systems.genfeatures.context.FullGenerationContext;
import io.github.steveplays28.dynamictreesfabric.systems.genfeatures.context.PostGenerationContext;
import io.github.steveplays28.dynamictreesfabric.util.CoordUtils;
import java.util.Collections;
import java.util.Comparator;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldAccess;

/**
 * Used to add mushrooms under a tree canopy.  Currently used by dark oaks for roofed forests.
 *
 * @author ferreusveritas
 */
public class HugeMushroomsGenFeature extends HugeMushroomGenFeature {

    public static final ConfigurationProperty<Integer> MAX_MUSHROOMS = ConfigurationProperty.integer("max_mushrooms");
    public static final ConfigurationProperty<Integer> MAX_ATTEMPTS = ConfigurationProperty.integer("max_attempts");

    public HugeMushroomsGenFeature(Identifier registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
        super.registerProperties();
        this.register(MAX_MUSHROOMS, MAX_ATTEMPTS);
    }

    @Override
    protected GenFeatureConfiguration createDefaultConfiguration() {
        return super.createDefaultConfiguration()
                .with(MAX_MUSHROOMS, 2)
                .with(MAX_ATTEMPTS, 4);
    }

    @Override
    protected boolean postGenerate(GenFeatureConfiguration configuration, PostGenerationContext context) {
        if (context.endPoints().isEmpty() || !context.isWorldGen() || context.radius() < 5) {
            return false;
        }

        final WorldAccess world = context.world();
        final BlockPos rootPos = context.pos();
        final BlockPos lowest = Collections.min(context.endPoints(), Comparator.comparingInt(Vec3i::getY));
        final Random rand = context.random();

        int success = 0;

        for (int tries = 0; tries < configuration.get(MAX_ATTEMPTS); tries++) {

            float angle = (float) (rand.nextFloat() * Math.PI * 2);
            int xOff = (int) (MathHelper.sin(angle) * (context.radius() - 1));
            int zOff = (int) (MathHelper.cos(angle) * (context.radius() - 1));

            BlockPos mushPos = rootPos.add(xOff, 0, zOff);

            mushPos = CoordUtils.findWorldSurface(world, new BlockPos(mushPos), context.isWorldGen()).up();

            if (context.bounds().inBounds(mushPos, true)) {
                int maxHeight = lowest.getY() - mushPos.getY();
                if (maxHeight >= 2) {
                    int height = MathHelper.clamp(rand.nextInt(maxHeight) + 3, 3, maxHeight);

                    if (this.setHeight(height).generate(configuration, new FullGenerationContext(
                            context.world(),
                            context.pos(),
                            context.species(),
                            context.biome(),
                            context.radius(),
                            context.bounds()
                    ))) {
                        if (++success >= configuration.get(MAX_MUSHROOMS)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

}
