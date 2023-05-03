package io.github.steveplays28.dynamictreesfabric.event;

import io.github.steveplays28.dynamictreesfabric.api.worldgen.PoissonDiscProvider;
import net.minecraftforge.event.level.LevelEvent;

import net.minecraft.world.WorldAccess;

public class PoissonDiscProviderCreateEvent extends LevelEvent {

	private PoissonDiscProvider poissonDiscProvider;

	public PoissonDiscProviderCreateEvent(WorldAccess world, PoissonDiscProvider poissonDiscProvider) {
		super(world);
		this.poissonDiscProvider = poissonDiscProvider;
	}

	public PoissonDiscProvider getPoissonDiscProvider() {
		return poissonDiscProvider;
	}

	public void setPoissonDiscProvider(PoissonDiscProvider poissonDiscProvider) {
		this.poissonDiscProvider = poissonDiscProvider;
	}

}
