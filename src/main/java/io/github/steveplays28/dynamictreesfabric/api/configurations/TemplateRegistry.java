package io.github.steveplays28.dynamictreesfabric.api.configurations;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import net.minecraft.util.Identifier;

/**
 * @author Harley O'Connor
 */
public class TemplateRegistry<C extends Configuration<C, ?>> {

    private final Map<Identifier, ConfigurationTemplate<C>>  templates = Maps.newHashMap();

    public void register(Identifier name, ConfigurationTemplate<C> template) {
        this.templates.put(name, template);
    }

    public Optional<ConfigurationTemplate<C>> get(Identifier name) {
        return Optional.ofNullable(this.templates.get(name));
    }

}
