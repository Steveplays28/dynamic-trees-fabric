/*
package io.github.steveplays28.dynamictreesfabric.compat.waila;

import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.api.TreeRegistry;
import io.github.steveplays28.dynamictreesfabric.api.network.MapSignal;
import io.github.steveplays28.dynamictreesfabric.blocks.branches.BranchBlock;
import io.github.steveplays28.dynamictreesfabric.blocks.branches.TrunkShellBlock;
import io.github.steveplays28.dynamictreesfabric.blocks.branches.TrunkShellBlock.ShellMuse;
import io.github.steveplays28.dynamictreesfabric.init.DTConfigs;
import io.github.steveplays28.dynamictreesfabric.systems.nodemappers.NetVolumeNode;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import io.github.steveplays28.dynamictreesfabric.trees.Species.LogsAndSticks;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.RenderableTextComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedList;
import java.util.List;

public class WailaBranchHandler implements IComponentProvider {

    private BlockPos lastPos = BlockPos.ZERO;
    private Species lastSpecies = Species.NULL_SPECIES;
    private NetVolumeNode.Volume lastVolume = new NetVolumeNode.Volume();



    @Override
    public void appendBody(List<TextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
        if (WailaOther.invalid) {
            lastPos = BlockPos.ZERO;
            lastSpecies = Species.NULL_SPECIES;
            lastVolume = new NetVolumeNode.Volume();

            WailaOther.invalid = false;
        }

        CompoundTag nbtData = accessor.getServerData();
        BlockPos pos = accessor.getPosition();
        Species species = Species.NULL_SPECIES;

        //Attempt to get species from server via NBT data
        if (nbtData.contains("species")) {
            species = TreeRegistry.findSpecies(new ResourceLocation(nbtData.getString("species")));
        }

        //Attempt to get species by checking if we're still looking at the same block
        if (species == Species.NULL_SPECIES && lastPos.equals(pos)) {
            species = lastSpecies;
        }

        //Attempt to get species from the world as a last resort as the operation can be rather expensive
        if (species == Species.NULL_SPECIES) {
            species = getWailaSpecies(accessor.getWorld(), pos);
        }

        if (!species.useDefaultWailaBody()) {
            return;
        }

        if (!lastPos.equals(pos)) {
            lastVolume = getTreeVolume(accessor.getWorld(), pos);
        }

        //Update the cached species and position
        lastSpecies = species;
        lastPos = pos;

        if (species != Species.NULL_SPECIES) {
            if (species.showSpeciesOnWaila()) {
                tooltip.add(Component.translatable("tooltip.dynamictrees.species", species.getTextComponent()));
            }

            if (Minecraft.getInstance().options.advancedItemTooltips) {
                tooltip.add(Component.literal(ChatFormatting.DARK_GRAY + species.getRegistryName().toString()));
            }

            ItemStack seedStack = species.getSeedStack(1);

            List<RenderableTextComponent> renderers = new LinkedList<>();
            renderers.add(getRenderable(seedStack)); //adds seed;

            if (lastVolume.getVolume() > 0) {
                LogsAndSticks las = species.getLogsAndSticks(lastVolume);
                List<ItemStack> logStacks = las.logs;
                if (!logStacks.isEmpty()) {
                    for (ItemStack logStack : logStacks) {
                        renderers.add(getRenderable(logStack));
                    }
                }
                if (las.sticks > 0) {
                    ItemStack stickStack = species.getFamily().getStick(las.sticks);
                    if (!stickStack.isEmpty()) {
                        renderers.add(getRenderable(stickStack));
                    }
                }
            }

            RenderableTextComponent renderables = new RenderableTextComponent(renderers.toArray(new RenderableTextComponent[]{}));

            tooltip.add(renderables);
        }
    }

    private NetVolumeNode.Volume getTreeVolume(Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        //Dereference proxy trunk shell block
        if (block instanceof TrunkShellBlock) {
            ShellMuse muse = ((TrunkShellBlock) block).getMuse(world, pos);
            if (muse != null) {
                state = muse.state;
                block = state.getBlock();
                pos = muse.pos;
            }
        }

        if (block instanceof BranchBlock) {
            BranchBlock branch = (BranchBlock) block;

            // Analyze only part of the tree beyond the break point and calculate it's volume
            NetVolumeNode volumeSum = new NetVolumeNode();
            branch.analyse(state, world, pos, null, new MapSignal(volumeSum));

            NetVolumeNode.Volume volume = volumeSum.getVolume();
            volume.multiplyVolume(DTConfigs.TREE_HARVEST_MULTIPLIER.get());

            return volume;
        }

        return new NetVolumeNode.Volume();
    }

//	@Override
//	public void appendServerData(CompoundNBT compoundNBT, ServerPlayerEntity serverPlayerEntity, World world, String string) {
//		compoundNBT.putString("species", string);
//	}

    private Species getWailaSpecies(Level world, BlockPos pos) {
        return TreeHelper.getBestGuessSpecies(world, pos);
    }

    private static RenderableTextComponent getRenderable(ItemStack stack) {
        CompoundTag tag = new CompoundTag();
        if (!stack.isEmpty()) {
            tag.putString("id", stack.getItem().getRegistryName().toString());
            tag.putInt("count", stack.getCount());
            if (stack.hasTag()) {
                tag.putString("nbt", stack.getTag().toString());
            }
            return new RenderableTextComponent(new ResourceLocation("item"), tag);
        } else {
            tag.putInt("width", 0);
            return new RenderableTextComponent(new ResourceLocation("spacer"), tag);
        }
    }
}
*/
