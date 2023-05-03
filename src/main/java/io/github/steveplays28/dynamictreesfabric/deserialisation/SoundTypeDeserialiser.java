package io.github.steveplays28.dynamictreesfabric.deserialisation;

import io.github.steveplays28.dynamictreesfabric.deserialisation.result.Result;
import com.google.gson.JsonElement;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

/**
 * @author Harley O'Connor
 */
public final class SoundTypeDeserialiser implements JsonDeserialiser<BlockSoundGroup> {

    private static final Map<Identifier, BlockSoundGroup> SOUND_TYPES =
            Util.make(new HashMap<>(), soundTypes -> {
                soundTypes.put(new Identifier("wood"), BlockSoundGroup.WOOD);
                soundTypes.put(new Identifier("gravel"), BlockSoundGroup.GRAVEL);
                soundTypes.put(new Identifier("grass"), BlockSoundGroup.GRASS);
                soundTypes.put(new Identifier("lily_pad"), BlockSoundGroup.LILY_PAD);
                soundTypes.put(new Identifier("stone"), BlockSoundGroup.STONE);
                soundTypes.put(new Identifier("metal"), BlockSoundGroup.METAL);
                soundTypes.put(new Identifier("glass"), BlockSoundGroup.GLASS);
                soundTypes.put(new Identifier("wool"), BlockSoundGroup.WOOL);
                soundTypes.put(new Identifier("sand"), BlockSoundGroup.SAND);
                soundTypes.put(new Identifier("snow"), BlockSoundGroup.SNOW);
                soundTypes.put(new Identifier("ladder"), BlockSoundGroup.LADDER);
                soundTypes.put(new Identifier("anvil"), BlockSoundGroup.ANVIL);
                soundTypes.put(new Identifier("slime_block"), BlockSoundGroup.SLIME);
                soundTypes.put(new Identifier("honey_block"), BlockSoundGroup.HONEY);
                soundTypes.put(new Identifier("wet_grass"), BlockSoundGroup.WET_GRASS);
                soundTypes.put(new Identifier("coral_block"), BlockSoundGroup.CORAL);
                soundTypes.put(new Identifier("bamboo"), BlockSoundGroup.BAMBOO);
                soundTypes.put(new Identifier("bamboo_sapling"), BlockSoundGroup.BAMBOO_SAPLING);
                soundTypes.put(new Identifier("scaffolding"), BlockSoundGroup.SCAFFOLDING);
                soundTypes.put(new Identifier("sweet_berry_bush"), BlockSoundGroup.SWEET_BERRY_BUSH);
                soundTypes.put(new Identifier("crop"), BlockSoundGroup.CROP);
                soundTypes.put(new Identifier("hard_crop"), BlockSoundGroup.STEM);
                soundTypes.put(new Identifier("vine"), BlockSoundGroup.VINE);
                soundTypes.put(new Identifier("nether_wart"), BlockSoundGroup.NETHER_WART);
                soundTypes.put(new Identifier("lantern"), BlockSoundGroup.LANTERN);
                soundTypes.put(new Identifier("stem"), BlockSoundGroup.NETHER_STEM);
                soundTypes.put(new Identifier("nylium"), BlockSoundGroup.NYLIUM);
                soundTypes.put(new Identifier("fungus"), BlockSoundGroup.FUNGUS);
                soundTypes.put(new Identifier("roots"), BlockSoundGroup.ROOTS);
                soundTypes.put(new Identifier("shroomlight"), BlockSoundGroup.SHROOMLIGHT);
                soundTypes.put(new Identifier("weeping_vines"), BlockSoundGroup.WEEPING_VINES);
                soundTypes.put(new Identifier("twisting_vines"), BlockSoundGroup.WEEPING_VINES_LOW_PITCH);
                soundTypes.put(new Identifier("soul_sand"), BlockSoundGroup.SOUL_SAND);
                soundTypes.put(new Identifier("soul_soil"), BlockSoundGroup.SOUL_SOIL);
                soundTypes.put(new Identifier("basalt"), BlockSoundGroup.BASALT);
                soundTypes.put(new Identifier("wart_block"), BlockSoundGroup.WART_BLOCK);
                soundTypes.put(new Identifier("netherrack"), BlockSoundGroup.NETHERRACK);
                soundTypes.put(new Identifier("nether_bricks"), BlockSoundGroup.NETHER_BRICKS);
                soundTypes.put(new Identifier("nether_sprouts"), BlockSoundGroup.NETHER_SPROUTS);
                soundTypes.put(new Identifier("nether_ore"), BlockSoundGroup.NETHER_ORE);
                soundTypes.put(new Identifier("bone_block"), BlockSoundGroup.BONE);
                soundTypes.put(new Identifier("netherite_block"), BlockSoundGroup.NETHERITE);
                soundTypes.put(new Identifier("ancient_debris"), BlockSoundGroup.ANCIENT_DEBRIS);
                soundTypes.put(new Identifier("lodestone"), BlockSoundGroup.LODESTONE);
                soundTypes.put(new Identifier("chain"), BlockSoundGroup.CHAIN);
                soundTypes.put(new Identifier("nether_gold_ore"), BlockSoundGroup.NETHER_GOLD_ORE);
                soundTypes.put(new Identifier("gilded_blackstone"), BlockSoundGroup.GILDED_BLACKSTONE);
            });

    /**
     * Registers given sound type under the given name, if that name is not already taken.
     *
     * @param name      the name to register the sound type under
     * @param soundType the sound type to register
     */
    public static void registerSoundType(Identifier name, BlockSoundGroup soundType) {
        SOUND_TYPES.putIfAbsent(name, soundType);
    }

    @Override
    public Result<BlockSoundGroup, JsonElement> deserialise(JsonElement input) {
        return JsonDeserialisers.RESOURCE_LOCATION.deserialise(input)
                .map(SOUND_TYPES::get, "Could not get sound type from \"{}\".");
    }
}
