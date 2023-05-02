package io.github.steveplays28.dynamictreesfabric.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomSupport;

public class RandomXOR extends LegacyRandomSource {

    private static final long serialVersionUID = -3477272122511092632L;

    private int xor = 0;

    public RandomXOR() {
        this(RandomSupport.generateUniqueSeed());
    }

    public RandomXOR(long seed) {
        super(seed);
    }

    public void setXOR(BlockPos pos) {
        setXOR(((pos.getX() * 674365771) ^ (pos.getZ() * 254326997)) >> 4);
    }

    public void setXOR(int xor) {
        this.xor = xor;
    }

    @Override
    public int next(int bits) {
        return super.next(bits) ^ (xor & ((1 << bits) - 1));
    }

}
