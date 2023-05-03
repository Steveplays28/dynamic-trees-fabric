package io.github.steveplays28.dynamictreesfabric.tileentity;

import javax.annotation.Nonnull;
import org.jetbrains.annotations.Nullable;

import io.github.steveplays28.dynamictreesfabric.api.TreeRegistry;
import io.github.steveplays28.dynamictreesfabric.init.DTRegistries;
import io.github.steveplays28.dynamictreesfabric.trees.Species;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * A TileEntity that holds a species value.
 *
 * @author ferreusveritas
 */
public class SpeciesTileEntity extends BlockEntity {

	private Species species = Species.NULL_SPECIES;

	public SpeciesTileEntity(BlockPos pos, BlockState state) {
		super(DTRegistries.speciesTE, pos, state);
	}

	public Species getSpecies() {
		return species;
	}

	public void setSpecies(Species species) {
		this.species = species;
		this.markDirty();
	}

	@Override
	public void readNbt(NbtCompound tag) {
		if (tag.contains("species")) {
			Identifier speciesName = new Identifier(tag.getString("species"));
			species = TreeRegistry.findSpecies(speciesName);
		}
		super.readNbt(tag);
	}

	@Nonnull
	@Override
	public void writeNbt(NbtCompound tag) {
		tag.putString("species", species.getRegistryName().toString());
	}

	@Nullable
	public BlockEntityUpdateS2CPacket toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public void onDataPacket(ClientConnection net, BlockEntityUpdateS2CPacket pkt) {
		readNbt(pkt.getNbt());
	}

	@Override
	public NbtCompound toInitialChunkDataNbt() {
		NbtCompound tag = super.toInitialChunkDataNbt();
		this.writeNbt(tag);
		return tag;
	}

}
