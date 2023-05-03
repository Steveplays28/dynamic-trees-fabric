package io.github.steveplays28.dynamictreesfabric.systems.genfeatures;

import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.api.configurations.ConfigurationProperty;
import io.github.steveplays28.dynamictreesfabric.blocks.branches.BranchBlock;
import io.github.steveplays28.dynamictreesfabric.systems.genfeatures.context.GenerationContext;
import io.github.steveplays28.dynamictreesfabric.systems.genfeatures.context.PostGenerationContext;
import io.github.steveplays28.dynamictreesfabric.systems.genfeatures.context.PreGenerationContext;
import io.github.steveplays28.dynamictreesfabric.util.CoordUtils.Surround;
import io.github.steveplays28.dynamictreesfabric.util.SimpleVoxmap;
import io.github.steveplays28.dynamictreesfabric.util.SimpleVoxmap.Cell;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.biome.Biome;

public class MoundGenFeature extends GenFeature {

    private static final SimpleVoxmap moundMap = new SimpleVoxmap(5, 4, 5, new byte[]{
            0, 0, 0, 0, 0, 0, 2, 2, 2, 0, 0, 2, 2, 2, 0, 0, 2, 2, 2, 0, 0, 0, 0, 0, 0,
            0, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 2, 2, 2, 0,
            0, 1, 1, 1, 0, 1, 2, 2, 2, 1, 1, 2, 2, 2, 1, 1, 2, 2, 2, 1, 0, 1, 1, 1, 0,
            0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 1, 1, 1, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0
    }).setCenter(new BlockPos(2, 3, 2));

    public static final ConfigurationProperty<Integer> MOUND_CUTOFF_RADIUS = ConfigurationProperty.integer("mound_cutoff_radius");

    public MoundGenFeature(Identifier registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
        this.register(MOUND_CUTOFF_RADIUS);
    }

    @Override
    protected GenFeatureConfiguration createDefaultConfiguration() {
        return super.createDefaultConfiguration()
                .with(MOUND_CUTOFF_RADIUS, 5);
    }

    /**
     * Used to create a 5x4x5 rounded mound that is one block higher than the ground surface. This is meant to replicate
     * the appearance of a root hill and gives generated surface roots a better appearance.
     *
     * @param configuration                The {@link GenFeatureConfiguration} instance.
     * @param context       The {@link GenerationContext}.
     * @return The modified {@link BlockPos} of the rooty dirt that is one block higher.
     */
    @Override
    protected BlockPos preGenerate(GenFeatureConfiguration configuration, PreGenerationContext context) {
        final WorldAccess world = context.world();
        BlockPos rootPos = context.pos();

        if (context.radius() >= configuration.get(MOUND_CUTOFF_RADIUS) && context.isWorldGen()) {
            BlockState initialDirtState = world.getBlockState(rootPos);
            BlockState initialUnderState = world.getBlockState(rootPos.down());

            if (initialUnderState.getMaterial() == Material.AIR ||
                    (initialUnderState.getMaterial() != Material.SOIL && initialUnderState.getMaterial() != Material.STONE)
            ) {
                final Biome biome = world.getGeneratorStoredBiome(
                        rootPos.getX() >> 2,
                        rootPos.getY() >> 2,
                        rootPos.getZ() >> 2
                ).value();
                //todo: figure out if needs replacement
//                initialUnderState = biome.getGenerationSettings().getSurfaceBuilderConfig().getTopMaterial();
            }

            rootPos = rootPos.up();

            for (Cell cell : moundMap.getAllNonZeroCells()) {
                final BlockState placeState = cell.getValue() == 1 ? initialDirtState : initialUnderState;
                world.setBlockState(rootPos.add(cell.getPos()), placeState, 3);
            }
        }

        return rootPos;
    }

    /**
     * Creates a 3x2x3 cube of dirt around the base of the tree using blocks derived from the environment.  This is used
     * to cleanup the overhanging trunk that happens when a thick tree is generated next to a drop off.  Only runs when
     * the radius is greater than 8.
     */
    @Override
    protected boolean postGenerate(GenFeatureConfiguration configuration, PostGenerationContext context) {
        // A mound was already generated in preGen and worldgen test
        if (context.radius() >= configuration.get(MOUND_CUTOFF_RADIUS) || !context.isWorldGen()) {
            return false;
        }

        final WorldAccess world = context.world();
        final BlockPos rootPos = context.pos();
        final BlockPos treePos = rootPos.up();
        final BlockState belowState = world.getBlockState(rootPos.down());

        // Place dirt blocks around rooty dirt block if tree has a > 8 radius.
        final BlockState branchState = world.getBlockState(treePos);
        if (TreeHelper.getTreePart(branchState).getRadius(branchState) > BranchBlock.MAX_RADIUS) {
            for (Surround dir : Surround.values()) {
                BlockPos dPos = rootPos.add(dir.getOffset());
                world.setBlockState(dPos, context.initialDirtState(), 3);
                world.setBlockState(dPos.down(), belowState, 3);
            }
            return true;
        }

        return false;
    }
}
