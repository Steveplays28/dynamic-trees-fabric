package com.ferreusveritas.dynamictrees.blocks.rootyblocks;

import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.init.DTClient;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.*;

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

    public SpreadableSoilProperties(final ResourceLocation registryName) {
        super(null, registryName);
    }

    @Override
    protected RootyBlock createBlock(AbstractBlock.Properties blockProperties) {
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

        public SpreadableRootyBlock(SpreadableSoilProperties properties, Properties blockProperties) {
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
        public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
            SpreadableSoilProperties properties = getSoilProperties();
            if (properties.spread_item != null) {
                ItemStack handStack = player.getItemInHand(hand);
                if (handStack.getItem().equals(properties.spread_item)) {
                    List<Block> foundBlocks = new LinkedList<>();

                    for (BlockPos blockpos : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
                        Block block = world.getBlockState(blockpos).getBlock();
                        if (properties.spreadable_soils.stream().anyMatch(prop -> prop.getPrimitiveSoilBlock() == block)) {
                            foundBlocks.add(block);
                        }
                    }
                    if (foundBlocks.size() > 0) {
                        if (!world.isClientSide()) {
                            int blockInt = world.random.nextInt(foundBlocks.size());
                            this.getRootyBlock(foundBlocks.get(blockInt)).ifPresent(rootyBlock ->
                                    world.setBlock(pos, rootyBlock.defaultBlockState(), 3)
                            );
                        }
                        if (!player.isCreative()) {
                            handStack.shrink(1);
                        }
                        DTClient.spawnParticles(world, ParticleTypes.HAPPY_VILLAGER, pos.above(), 2 + world.random.nextInt(5), world.random);
                        return ActionResultType.SUCCESS;
                    }
                }
            }
            return super.use(state, world, pos, player, hand, hit);
        }

        @Override
        public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
            super.randomTick(state, world, pos, random);
            SpreadableSoilProperties properties = getSoilProperties();
            //this is a similar behaviour to vanilla grass spreading but inverted to be handled by the dirt block
            if (!world.isClientSide && properties.required_light != null) {
                if (!world.isAreaLoaded(pos, 3)) {
                    return; // Forge: prevent loading unloaded chunks when checking neighbor's light and spreading
                }
                if (world.getMaxLocalRawBrightness(pos.above()) >= properties.required_light) {
                    for (int i = 0; i < 4; ++i) {
                        BlockPos thatPos = pos.offset(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);

                        if (thatPos.getY() >= 0 && thatPos.getY() < 256 && !world.hasChunkAt(thatPos)) {
                            return;
                        }

                        BlockState thatStateUp = world.getBlockState(thatPos.above());
                        BlockState thatState = world.getBlockState(thatPos);

                        for (SoilProperties spreadable : properties.spreadable_soils) {
                            RootyBlock block = spreadable.getBlock().orElse(null);
                            if (block != null && (thatState.getBlock() == spreadable.getPrimitiveSoilBlock() || thatState.getBlock() == block) && world.getMaxLocalRawBrightness(pos.above()) >= properties.required_light && thatStateUp.getLightBlock(world, thatPos.above()) <= 2) {
                                if (state.hasProperty(FERTILITY)) {
                                    world.setBlockAndUpdate(pos, block.defaultBlockState().setValue(FERTILITY, state.getValue(FERTILITY)));
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
