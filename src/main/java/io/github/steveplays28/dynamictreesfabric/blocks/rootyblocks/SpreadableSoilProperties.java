package io.github.steveplays28.dynamictreesfabric.blocks.rootyblocks;

import io.github.steveplays28.dynamictreesfabric.api.registry.TypedRegistry;
import io.github.steveplays28.dynamictreesfabric.init.DTClient;
import java.util.*;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

/**
 * @author Max Hyper
 */
public class SpreadableSoilProperties extends SoilProperties {

    public static final TypedRegistry.EntryType<SoilProperties> TYPE = TypedRegistry.newType(SpreadableSoilProperties::new);

    private Integer required_light = null;
    private Item spread_item = null;
    private final List<SoilProperties> spreadable_soils = new LinkedList<>();

    public void setRequiredLight(Integer light) {
        this.required_light = light;
    }

    public void setSpreadItem(Item item) {
        this.spread_item = item;
    }

    public SpreadableSoilProperties(final Identifier registryName) {
        super(null, registryName);
    }

    @Override
    protected RootyBlock createBlock(AbstractBlock.Settings blockProperties) {
        return new SpreadableRootyBlock(this, blockProperties);
    }

    public void addSpreadableSoils(Block... blocks) {
        for (Block block : blocks) {
            SoilProperties props = SoilHelper.getProperties(block);
            if (props.isValid()) {
                spreadable_soils.add(props);
            }
        }
    }

    public void addSpreadableSoils(SoilProperties... props) {
        spreadable_soils.addAll(Arrays.asList(props));
    }

    public static class SpreadableRootyBlock extends RootyBlock {

        public SpreadableRootyBlock(SpreadableSoilProperties properties, Settings blockProperties) {
            super(properties, blockProperties);
        }

        @Override
        public SpreadableSoilProperties getSoilProperties() {
            return (SpreadableSoilProperties) super.getSoilProperties();
        }

        private Optional<RootyBlock> getRootyBlock(Block block) {
            return SoilHelper.getProperties(block).getBlock();
        }

        @Override
        public ActionResult onUse(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockHitResult hit) {
            SpreadableSoilProperties properties = getSoilProperties();
            if (properties.spread_item != null) {
                ItemStack handStack = player.getStackInHand(handIn);
                if (handStack.getItem().equals(properties.spread_item)) {
                    List<Block> foundBlocks = new LinkedList<>();

                    for (BlockPos blockpos : BlockPos.iterate(pos.add(-1, -1, -1), pos.add(1, 1, 1))) {
                        Block block = worldIn.getBlockState(blockpos).getBlock();
                        if (properties.spreadable_soils.stream().anyMatch(prop -> prop.getPrimitiveSoilBlock() == block)) {
                            foundBlocks.add(block);
                        }
                    }
                    if (foundBlocks.size() > 0) {
                        if (!worldIn.isClient()) {
                            int blockInt = worldIn.random.nextInt(foundBlocks.size());
                            this.getRootyBlock(foundBlocks.get(blockInt)).ifPresent(rootyBlock ->
                                    worldIn.setBlockState(pos, rootyBlock.getDefaultState(), 3)
                            );
                        }
                        if (!player.isCreative()) {
                            handStack.decrement(1);
                        }
                        DTClient.spawnParticles(worldIn, ParticleTypes.HAPPY_VILLAGER, pos.up(), 2 + worldIn.random.nextInt(5), worldIn.random);
                        return ActionResult.SUCCESS;
                    }
                }
            }
            return super.onUse(state, worldIn, pos, player, handIn, hit);
        }

        @Override
        public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
            super.randomTick(state, world, pos, random);
            SpreadableSoilProperties properties = getSoilProperties();
            //this is a similar behaviour to vanilla grass spreading but inverted to be handled by the dirt block
            if (!world.isClient && properties.required_light != null) {
                if (!world.isAreaLoaded(pos, 3)) {
                    return; // Forge: prevent loading unloaded chunks when checking neighbor's light and spreading
                }
                if (world.getLightLevel(pos.up()) >= properties.required_light) {
                    for (int i = 0; i < 4; ++i) {
                        BlockPos thatPos = pos.add(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);

                        if (thatPos.getY() >= 0 && thatPos.getY() < 256 && !world.isChunkLoaded(thatPos)) {
                            return;
                        }

                        BlockState thatStateUp = world.getBlockState(thatPos.up());
                        BlockState thatState = world.getBlockState(thatPos);

                        for (SoilProperties spreadable : properties.spreadable_soils) {
                            RootyBlock block = spreadable.getBlock().orElse(null);
                            if (block != null && (thatState.getBlock() == spreadable.getPrimitiveSoilBlock() || thatState.getBlock() == block) && world.getLightLevel(pos.up()) >= properties.required_light && thatStateUp.getOpacity(world, thatPos.up()) <= 2) {
                                if (state.contains(FERTILITY)) {
                                    world.setBlockState(pos, block.getDefaultState().with(FERTILITY, state.get(FERTILITY)));
                                }
                                return;
                            }
                        }
                    }
                }
            }

        }

    }


}
