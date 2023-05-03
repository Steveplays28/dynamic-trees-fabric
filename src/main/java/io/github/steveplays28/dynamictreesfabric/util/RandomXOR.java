package io.github.steveplays28.dynamictreesfabric.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.RandomSeed;

public class RandomXOR extends CheckedRandom {

    private static final long serialVersionUID = -3477272122511092632L;

    private int xor = 0;

    public RandomXOR() {
        this(RandomSeed.getSeed());
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
