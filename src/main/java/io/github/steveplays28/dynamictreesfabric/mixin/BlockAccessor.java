package io.github.steveplays28.dynamictreesfabric.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;

@Mixin(Block.class)
public interface BlockAccessor {
	@Accessor("stateManager")
	void setStateManager(StateManager<Block, BlockState> stateManager);
}
