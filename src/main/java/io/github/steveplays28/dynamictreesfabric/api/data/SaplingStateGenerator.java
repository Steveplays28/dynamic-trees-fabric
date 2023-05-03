package io.github.steveplays28.dynamictreesfabric.api.data;

import io.github.steveplays28.dynamictreesfabric.blocks.DynamicSaplingBlock;
import io.github.steveplays28.dynamictreesfabric.data.provider.DTBlockStateProvider;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Harley O'Connor
 */
public class SaplingStateGenerator implements Generator<DTBlockStateProvider, Species> {

    public static final DependencyKey<DynamicSaplingBlock> SAPLING = new DependencyKey<>("sapling");
    public static final DependencyKey<Block> PRIMITIVE_LOG = new DependencyKey<>("primitive_log");
    public static final DependencyKey<Block> PRIMITIVE_LEAVES = new DependencyKey<>("primitive_leaves", true);

    @Override
    public void generate(DTBlockStateProvider provider, Species input, Dependencies dependencies) {
        final Optional<Identifier> leavesTextureLocation = dependencies.getOptional(PRIMITIVE_LEAVES)
                .map(primitiveLeaves -> provider.block(Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(primitiveLeaves))));
        final Identifier primitiveLogLocation = Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(dependencies.get(PRIMITIVE_LOG)));

        final BlockModelBuilder builder = provider.models().getBuilder("block/saplings/" + input.getRegistryName().getPath())
                .parent(provider.models().getExistingFile(input.getSaplingSmartModelLocation()))
                .renderType("cutout_mipped");
        input.addSaplingTextures(builder::texture, leavesTextureLocation.orElse(primitiveLogLocation), provider.block(primitiveLogLocation));
        provider.simpleBlock(dependencies.get(SAPLING), builder);
    }

    @Override
    public Dependencies gatherDependencies(Species input) {
        return new Dependencies()
                .append(SAPLING, input.getSapling())
                .append(PRIMITIVE_LOG, input.getFamily().getPrimitiveLog())
                .append(PRIMITIVE_LEAVES, input.getPrimitiveLeaves());
    }

}
