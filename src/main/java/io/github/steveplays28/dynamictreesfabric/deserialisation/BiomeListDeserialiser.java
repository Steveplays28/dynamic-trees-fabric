package io.github.steveplays28.dynamictreesfabric.deserialisation;

import io.github.steveplays28.dynamictreesfabric.api.treepacks.Applier;
import io.github.steveplays28.dynamictreesfabric.api.treepacks.PropertyApplierResult;
import io.github.steveplays28.dynamictreesfabric.api.treepacks.VoidApplier;
import io.github.steveplays28.dynamictreesfabric.deserialisation.result.JsonResult;
import io.github.steveplays28.dynamictreesfabric.deserialisation.result.Result;
import io.github.steveplays28.dynamictreesfabric.util.JsonMapWrapper;
import io.github.steveplays28.dynamictreesfabric.util.holderset.DTBiomeHolderSet;
import io.github.steveplays28.dynamictreesfabric.util.holderset.DelayedTagEntriesHolderSet;
import io.github.steveplays28.dynamictreesfabric.util.holderset.NameRegexMatchHolderSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.registries.holdersets.OrHolderSet;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

/**
 * @author Harley O'Connor
 */
public final class BiomeListDeserialiser implements JsonDeserialiser<DTBiomeHolderSet> {

    public static final Supplier<Registry<Biome>> DELAYED_BIOME_REGISTRY = () -> ServerLifecycleHooks.getCurrentServer().registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);

    private static final Applier<DTBiomeHolderSet, String> TAG_APPLIER = (biomeList, tagString) -> {
        tagString = tagString.toLowerCase();
        final boolean notOperator = usingNotOperator(tagString);
        if (notOperator)
            tagString = tagString.substring(1);
        if (tagString.charAt(0) == '#')
            tagString = tagString.substring(1);

        try {
            Identifier tagLocation = new Identifier(tagString);
            TagKey<Biome> tagKey = TagKey.of(Registry.BIOME_REGISTRY, tagLocation);

            (notOperator ? biomeList.getExcludeComponents() : biomeList.getIncludeComponents()).add(new DelayedTagEntriesHolderSet<>(DELAYED_BIOME_REGISTRY, tagKey));
        } catch (InvalidIdentifierException e) {
            return PropertyApplierResult.failure(e.getMessage());
        }

        return PropertyApplierResult.success();
    };

    private static final VoidApplier<DTBiomeHolderSet, String> NAME_APPLIER = (biomeList, nameRegex) -> {
        nameRegex = nameRegex.toLowerCase();
        final boolean notOperator = usingNotOperator(nameRegex);
        if (notOperator)
            nameRegex = nameRegex.substring(1);

        (notOperator ? biomeList.getExcludeComponents() : biomeList.getIncludeComponents()).add(new NameRegexMatchHolderSet<>(DELAYED_BIOME_REGISTRY, nameRegex));
    };

    private static boolean usingNotOperator(String categoryString) {
        return categoryString.charAt(0) == '!';
    }

    private static final VoidApplier<DTBiomeHolderSet, JsonArray> NAMES_OR_APPLIER = (biomeList, json) -> {
        final List<String> nameRegexes = JsonResult.forInput(json)
                .mapEachIfArray(String.class, (Result.SimpleMapper<String, String>) String::toLowerCase)
                .orElse(Collections.emptyList(), LogManager.getLogger()::error, LogManager.getLogger()::warn);

        List<RegistryEntryList<Biome>> orIncludes = new ArrayList<>();
        List<RegistryEntryList<Biome>> orExcludes = new ArrayList<>();
        nameRegexes.forEach(nameRegex -> {
            nameRegex = nameRegex.toLowerCase();
            final boolean notOperator = usingNotOperator(nameRegex);
            if (notOperator)
                nameRegex = nameRegex.substring(1);

            (notOperator ? orExcludes : orIncludes).add(new NameRegexMatchHolderSet<>(DELAYED_BIOME_REGISTRY, nameRegex));
        });

        if (!orIncludes.isEmpty())
            biomeList.getIncludeComponents().add(new OrHolderSet<>(orIncludes));
        if (!orExcludes.isEmpty())
            biomeList.getExcludeComponents().add(new OrHolderSet<>(orExcludes));
    };

    private final VoidApplier<DTBiomeHolderSet, JsonObject> andOperator =
            (biomes, jsonObject) -> applyAllAppliers(jsonObject, biomes);

    private final VoidApplier<DTBiomeHolderSet, JsonArray> orOperator = (biomeList, json) -> {
        JsonResult.forInput(json)
                .mapEachIfArray(JsonObject.class, object -> {
                    DTBiomeHolderSet subList = new DTBiomeHolderSet();
                    applyAllAppliers(object, subList);
                    biomeList.getIncludeComponents().add(subList);
                    return object;
                })
                .orElse(null, LogManager.getLogger()::error, LogManager.getLogger()::warn);
    };

    private final VoidApplier<DTBiomeHolderSet, JsonObject> notOperator = (biomeList, jsonObject) -> {
        final DTBiomeHolderSet notBiomeList = new DTBiomeHolderSet();
        applyAllAppliers(jsonObject, notBiomeList);
        biomeList.getExcludeComponents().add(notBiomeList);
    };

    private final JsonPropertyAppliers<DTBiomeHolderSet> appliers = new JsonPropertyAppliers<>(DTBiomeHolderSet.class);

    public BiomeListDeserialiser() {
        registerAppliers();
    }

    private void registerAppliers() {
        this.appliers
                .register("tag", String.class, TAG_APPLIER)
                .registerArrayApplier("tags", String.class, TAG_APPLIER)
                .register("name", String.class, NAME_APPLIER)
                .registerArrayApplier("names", String.class, NAME_APPLIER)
                .register("names_or", JsonArray.class, NAMES_OR_APPLIER)
                .registerArrayApplier("AND", JsonObject.class, andOperator)
                .register("OR", JsonArray.class, orOperator)
                .register("NOT", JsonObject.class, notOperator);
    }

    private void applyAllAppliers(JsonObject json, DTBiomeHolderSet biomes) {
        appliers.applyAll(new JsonMapWrapper(json), biomes);
    }

    @Override
    public Result<DTBiomeHolderSet, JsonElement> deserialise(final JsonElement input) {
        return JsonResult.forInput(input)
                .mapIfType(String.class, biomeName -> {
                    DTBiomeHolderSet biomes = new DTBiomeHolderSet();
                    biomes.getIncludeComponents().add(new NameRegexMatchHolderSet<>(DELAYED_BIOME_REGISTRY, biomeName.toLowerCase(Locale.ROOT)));
                    return biomes;
                })
                .elseMapIfType(JsonObject.class, selectorObject -> {
                    final DTBiomeHolderSet biomes = new DTBiomeHolderSet();
                    // Apply from all appliers
                    applyAllAppliers(selectorObject, biomes);
                    return biomes;
                }).elseTypeError();
    }

}
