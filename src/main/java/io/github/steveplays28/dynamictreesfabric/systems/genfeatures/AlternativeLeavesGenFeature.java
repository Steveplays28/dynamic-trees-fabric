package io.github.steveplays28.dynamictreesfabric.systems.genfeatures;

import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.api.configurations.ConfigurationProperty;
import io.github.steveplays28.dynamictreesfabric.api.network.MapSignal;
import io.github.steveplays28.dynamictreesfabric.blocks.leaves.DynamicLeavesBlock;
import io.github.steveplays28.dynamictreesfabric.blocks.leaves.LeavesProperties;
import io.github.steveplays28.dynamictreesfabric.systems.genfeatures.context.PostGenerationContext;
import io.github.steveplays28.dynamictreesfabric.systems.genfeatures.context.PostGrowContext;
import io.github.steveplays28.dynamictreesfabric.systems.nodemappers.FindEndsNode;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import io.github.steveplays28.dynamictreesfabric.util.BlockBounds;
import io.github.steveplays28.dynamictreesfabric.util.SafeChunkBounds;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class AlternativeLeavesGenFeature extends GenFeature {

    public static final ConfigurationProperty<LeavesProperties> ALT_LEAVES = ConfigurationProperty.property("alternative_leaves", LeavesProperties.class);
    public static final ConfigurationProperty<Block> ALT_LEAVES_BLOCK = ConfigurationProperty.block("alternative_leaves_block");

    public AlternativeLeavesGenFeature(Identifier registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
        this.register(ALT_LEAVES, ALT_LEAVES_BLOCK, PLACE_CHANCE, QUANTITY);
    }

    public GenFeatureConfiguration createDefaultConfiguration() {
        return super.createDefaultConfiguration().with(ALT_LEAVES, LeavesProperties.NULL_PROPERTIES).with(ALT_LEAVES_BLOCK, Blocks.AIR)
                .with(PLACE_CHANCE, 0.5f).with(QUANTITY, 5);
    }

    @Override
    public boolean shouldApply(Species species, GenFeatureConfiguration configuration) {
        configuration.get(ALT_LEAVES).ifValid(properties -> {
            properties.setFamily(species.getFamily());
            species.addValidLeafBlocks(properties);
        });
        return true;
    }

    @Override
    protected boolean postGenerate(GenFeatureConfiguration configuration, PostGenerationContext context) {
        final BlockBounds bounds = context.species().getFamily().expandLeavesBlockBounds(new BlockBounds(context.endPoints()));
        return this.setAltLeaves(configuration, context.world(), bounds, context.bounds(), context.species());
    }

    @Override
    protected boolean postGrow(GenFeatureConfiguration configuration, PostGrowContext context) {
        if (context.fertility() == 0) {
            return false;
        }

        final World world = context.world();
        final Species species = context.species();

        final FindEndsNode endFinder = new FindEndsNode();
        TreeHelper.startAnalysisFromRoot(world, context.pos(), new MapSignal(endFinder));
        final List<BlockPos> endPoints = endFinder.getEnds();
        if (endPoints.isEmpty()) {
            return false;
        }

        final BlockPos chosenEndPoint = endPoints.get(world.getRandom().nextInt(endPoints.size()));
        final BlockBounds bounds = species.getFamily().expandLeavesBlockBounds(new BlockBounds(chosenEndPoint));

        return setAltLeaves(configuration, world, bounds, SafeChunkBounds.ANY, species);
    }

    private Block getAltLeavesBlock(GenFeatureConfiguration conifuration) {
        LeavesProperties properties = conifuration.get(ALT_LEAVES);
        if (!properties.isValid() || !properties.getDynamicLeavesBlock().isPresent()) {
            return conifuration.get(ALT_LEAVES_BLOCK);
        }
        return properties.getDynamicLeavesBlock().get();
    }

    private BlockState getSwapBlockState(GenFeatureConfiguration configuration, WorldAccess world, Species species, BlockState state, boolean worldgen) {
        DynamicLeavesBlock originalLeaves = species.getLeavesBlock().orElse(null);
        Block alt = getAltLeavesBlock(configuration);
        DynamicLeavesBlock altLeaves = alt instanceof DynamicLeavesBlock ? (DynamicLeavesBlock) alt : null;
        if (originalLeaves != null && altLeaves != null) {
            if (worldgen || world.getRandom().nextFloat() < configuration.get(PLACE_CHANCE)) {
                if (state.getBlock() == originalLeaves) {
                    return altLeaves.properties.getDynamicLeavesState(state.get(LeavesBlock.DISTANCE));
                }
            } else {
                if (state.getBlock() == altLeaves) {
                    return originalLeaves.properties.getDynamicLeavesState(state.get(LeavesBlock.DISTANCE));
                }
            }
        }
        return state;
    }

    private boolean setAltLeaves(GenFeatureConfiguration configuration, WorldAccess world, BlockBounds leafPositions, SafeChunkBounds safeBounds, Species species) {
        boolean worldGen = safeBounds != SafeChunkBounds.ANY;

        if (worldGen) {
            AtomicBoolean isSet = new AtomicBoolean(false);
            leafPositions.iterator().forEachRemaining((pos) -> {
                if (safeBounds.inBounds(pos, true) && world.getRandom().nextFloat() < configuration.get(PLACE_CHANCE)) {
                    if (world.setBlockState(pos, getSwapBlockState(configuration, world, species, world.getBlockState(pos), true), 2)) {
                        isSet.set(true);
                    }
                }
            });
            return isSet.get();
        } else {
            boolean isSet = false;
            List<BlockPos> posList = new LinkedList<>();
            for (BlockPos leafPosition : leafPositions) {
                posList.add(new BlockPos(leafPosition));
            }
            if (posList.isEmpty()) {
                return false;
            }
            for (int i = 0; i < configuration.get(QUANTITY); i++) {
                BlockPos pos = posList.get(world.getRandom().nextInt(posList.size()));
                if (world.setBlockState(pos, getSwapBlockState(configuration, world, species, world.getBlockState(pos), false), 2)) {
                    isSet = true;
                }
            }
            return isSet;
        }
    }
}
