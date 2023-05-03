package io.github.steveplays28.dynamictreesfabric.entities;

import io.github.steveplays28.dynamictreesfabric.api.substances.SubstanceEffect;
import io.github.steveplays28.dynamictreesfabric.blocks.rootyblocks.RootyBlock;
import io.github.steveplays28.dynamictreesfabric.init.DTRegistries;
import io.github.steveplays28.dynamictreesfabric.systems.substances.LingeringSubstances;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

public class LingeringEffectorEntity extends Entity implements IEntityAdditionalSpawnData {

    private BlockPos blockPos;
    private SubstanceEffect effect;

    public LingeringEffectorEntity(EntityType<? extends LingeringEffectorEntity> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
        this.blockPos = BlockPos.ORIGIN;
    }

    @SuppressWarnings("unused")
    private LingeringEffectorEntity(World world) {
        super(DTRegistries.LINGERING_EFFECTOR.get(), world);
    }

    public LingeringEffectorEntity(World world, BlockPos pos, SubstanceEffect effect) {
        this(DTRegistries.LINGERING_EFFECTOR.get(), world);
        this.stepHeight = 1f;
        this.noClip = true;
        this.setBlockPos(pos);
        this.effect = effect;

        if (this.effect != null) {
            // Search for existing effectors with the same effect in the same place.
            for (final LingeringEffectorEntity effector : world.getNonSpectatingEntities(LingeringEffectorEntity.class, new Box(pos))) {
                if (effector.getEffect() != null && effector.getEffect().getName().equals(effect.getName())) {
                    effector.kill(); // Kill old effector if it's the same.
                }
            }
        }
    }

    public static boolean treeHasEffectorForEffect(WorldAccess world, BlockPos pos, SubstanceEffect effect) {
        for (final LingeringEffectorEntity effector : world.getNonSpectatingEntities(LingeringEffectorEntity.class, new Box(pos))) {
            if (effector.getEffect() != null && effector.getEffect().getName().equals(effect.getName())) {
                return true;
            }
        }
        return false;
    }

    public void setBlockPos(BlockPos pos) {
        this.blockPos = pos;
        setPosition(this.blockPos.getX() + 0.5, this.blockPos.getY(), this.blockPos.getZ() + 0.5);
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public SubstanceEffect getEffect() {
        return this.effect;
    }

    @Override
    protected void initDataTracker() {
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound compound) {
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound compound) {
    }

    private byte invalidTicks = 0;

    @Override
    public void tick() {
        super.tick();

        if (this.effect == null) {
            // If effect hasn't been set for 20 ticks then kill the entity.
            if (++this.invalidTicks > 20) {
                this.kill();
            }
            return;
        }

        final BlockState blockState = this.world.getBlockState(this.blockPos);

        if (blockState.getBlock() instanceof RootyBlock) {
            if (!this.effect.update(this.world, this.blockPos, this.age, blockState.get(RootyBlock.FERTILITY))) {
                this.kill();
            }
        } else {
            this.kill();
        }
    }

    @Override
    public Packet<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(PacketByteBuf buffer) {
        // We'll assume there aren't more than 128 lingering substance effects, so send a byte.
        buffer.writeByte(this.effect == null ? -1 : LingeringSubstances.indexOf(this.effect.getClass()));
    }

    @Override
    public void readSpawnData(PacketByteBuf additionalData) {
        // We'll assume there aren't more than 128 lingering substance effects, so send a byte.
        final byte index = additionalData.readByte();
        this.effect = index < 0 ? null : LingeringSubstances.fromIndex(index).get();

        if (this.effect != null && this.world != null) {
            this.effect.apply(this.world, this.blockPos);
        }
    }

}
