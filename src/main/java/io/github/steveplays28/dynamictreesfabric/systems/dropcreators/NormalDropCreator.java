package io.github.steveplays28.dynamictreesfabric.systems.dropcreators;

import io.github.steveplays28.dynamictreesfabric.api.configurations.ConfigurationProperty;
import io.github.steveplays28.dynamictreesfabric.deserialisation.JsonDeserialisers;
import io.github.steveplays28.dynamictreesfabric.deserialisation.MapDeserialiser;
import io.github.steveplays28.dynamictreesfabric.systems.dropcreators.context.DropContext;
import io.github.steveplays28.dynamictreesfabric.systems.dropcreators.context.LogDropContext;
import io.github.steveplays28.dynamictreesfabric.systems.dropcreators.drops.Drops;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.util.Identifier;

/**
 * @author Harley O'Connor
 */
public final class NormalDropCreator extends DropCreator {

    @SuppressWarnings("all")
    public static final ConfigurationProperty<Map<Type<DropContext>, Drops>> DROP_MAP = ConfigurationProperty.property("drops",
            MapDeserialiser.getMapClass(Type.getGenericClass(), Drops.class, HashMap::new));

    static {
        JsonDeserialisers.register(
                MapDeserialiser.getMapClass(Type.getGenericClass(), Drops.class),
                new MapDeserialiser<>(JsonDeserialisers.DROP_TYPE, JsonDeserialisers.DROPS)
        );
    }

    public NormalDropCreator(Identifier registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
        this.register(DROP_MAP);
    }

    @Override
    protected DropCreatorConfiguration createDefaultConfiguration() {
        return super.createDefaultConfiguration()
                .with(DROP_MAP, Maps.newHashMap());
    }

    @Override
    public void appendHarvestDrops(DropCreatorConfiguration configuration, DropContext context) {
        configuration.get(DROP_MAP).getOrDefault(Type.HARVEST, Drops.NONE)
                .appendDrops(context.drops(), context.random(), context.fortune());
    }

    @Override
    public void appendVoluntaryDrops(DropCreatorConfiguration configuration, DropContext context) {
        configuration.get(DROP_MAP).getOrDefault(Type.VOLUNTARY, Drops.NONE)
                .appendDrops(context.drops(), context.random(), 0);
    }

    @Override
    public void appendLeavesDrops(DropCreatorConfiguration configuration, DropContext context) {
        configuration.get(DROP_MAP).getOrDefault(Type.LEAVES, Drops.NONE)
                .appendDrops(context.drops(), context.random(), context.fortune());
    }

    @Override
    public void appendLogDrops(DropCreatorConfiguration configuration, LogDropContext context) {
        configuration.get(DROP_MAP).getOrDefault(Type.LOGS, Drops.NONE)
                .appendDrops(context.drops(), context.random(), 0);
    }

}
