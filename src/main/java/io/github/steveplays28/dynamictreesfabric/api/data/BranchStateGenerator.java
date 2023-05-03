package io.github.steveplays28.dynamictreesfabric.api.data;

import io.github.steveplays28.dynamictreesfabric.blocks.branches.BranchBlock;
import io.github.steveplays28.dynamictreesfabric.data.provider.BranchLoaderBuilder;
import io.github.steveplays28.dynamictreesfabric.data.provider.DTBlockStateProvider;
import io.github.steveplays28.dynamictreesfabric.trees.Family;
import net.minecraft.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

/**
 * @author Harley O'Connor
 */
public class BranchStateGenerator implements Generator<DTBlockStateProvider, Family> {

    public static final DependencyKey<BranchBlock> BRANCH = new DependencyKey<>("branch");
    public static final DependencyKey<Block> PRIMITIVE_LOG = new DependencyKey<>("primitive_log");

    @Override
    public void generate(DTBlockStateProvider provider, Family input, Dependencies dependencies) {
        final BranchBlock branch = dependencies.get(BRANCH);
        final BranchLoaderBuilder builder = provider.models().getBuilder(
                Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(branch)).getPath()
        ).customLoader(branch.getFamily().getBranchLoaderConstructor());
        input.addBranchTextures(builder::texture, provider.block(ForgeRegistries.BLOCKS.getKey(dependencies.get(PRIMITIVE_LOG))));
        provider.simpleBlock(branch, builder.end());
    }

    @Override
    public Dependencies gatherDependencies(Family input) {
        return new Dependencies()
                .append(BRANCH, input.getBranch())
                .append(PRIMITIVE_LOG, input.getPrimitiveLog());
    }

}
