package io.github.steveplays28.dynamictreesfabric.tileentity;

import io.github.steveplays28.dynamictreesfabric.api.TreeRegistry;
import io.github.steveplays28.dynamictreesfabric.init.DTRegistries;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A TileEntity that holds a species value.
 *
 * @author ferreusveritas
 */
public class PottedSaplingTileEntity extends BlockEntity {

    private static final String POT_MIMIC_TAG = "pot_mimic";
    private static final String SPECIES_TAG = "species";

    public static final ModelProperty<BlockState> POT_MIMIC = new ModelProperty<>();
    public static final ModelProperty<Species> SPECIES = new ModelProperty<>();

    private BlockState potState = Blocks.FLOWER_POT.getDefaultState();
    private Species species = Species.NULL_SPECIES;

    public PottedSaplingTileEntity(BlockPos pos, BlockState state) {
        super(DTRegistries.bonsaiTE,pos,state);
    }

    public Species getSpecies() {
        return this.species;
    }

    public void setSpecies(Species species) {
        this.species = species;
        this.markDirty();
        world.updateListeners(pos, this.getCachedState(), this.getCachedState(), Block.NOTIFY_ALL);
    }

    public BlockState getPot() {
        return potState;
    }

    public void setPot(BlockState newPotState) {
        if (newPotState.getBlock() instanceof FlowerPotBlock) {
            this.potState = newPotState.getBlock().getDefaultState();
        } else {
            this.potState = Blocks.FLOWER_POT.getDefaultState();
        }
        this.markDirty();
        world.updateListeners(pos, this.getCachedState(), this.getCachedState(), Block.NOTIFY_ALL);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound tag = super.toInitialChunkDataNbt();
        this.writeNbt(tag);
        return tag;
    }

    @Nullable
    @Override
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public void onDataPacket(ClientConnection net, BlockEntityUpdateS2CPacket pkt) {
        BlockState oldPotState = potState;
        this.handleUpdateTag(pkt.getNbt());

        if (!oldPotState.equals(potState)) {
            world.getModelDataManager().requestRefresh(this);
            world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_ALL);
        }
    }

    @Override
    public void readNbt(NbtCompound tag) {
        if (tag.contains(POT_MIMIC_TAG)) {
            Block block = ForgeRegistries.BLOCKS.getValue(new Identifier(tag.getString(POT_MIMIC_TAG)));
            potState = block != Blocks.AIR ? block.getDefaultState() : Blocks.FLOWER_POT.getDefaultState();
        }
        if (tag.contains(SPECIES_TAG)) {
            this.species = TreeRegistry.findSpecies(tag.getString(SPECIES_TAG));
        }
        super.readNbt(tag);
    }

    @Override
    protected void writeNbt(NbtCompound tag) {
        tag.putString(POT_MIMIC_TAG, ForgeRegistries.BLOCKS.getKey(potState.getBlock()).toString());
        tag.putString(SPECIES_TAG, this.species.getRegistryName().toString());
    }

    @Nonnull
    @Override
    public ModelData getModelData() {
        return ModelData.builder().with(POT_MIMIC, potState).with(SPECIES, species).build();
    }

}
