package com.necro.asymmetric.battles.common.util;

import com.cobblemon.mod.common.api.pokemon.PokemonPropertyExtractor;

import java.util.List;

public class PropertyExtractors {
    private static final PokemonPropertyExtractor ALPHA = PokemonPropertyExtractor.Companion.add((pokemon, properties) ->
        properties.setAlpha(pokemon.isAlpha())
    );

    public static final List<PokemonPropertyExtractor> LONG_EXTRACTOR = List.of(
        PokemonPropertyExtractor.SPECIES,
        PokemonPropertyExtractor.ASPECTS,
        PokemonPropertyExtractor.SHINY,
        PokemonPropertyExtractor.FORM,
        PokemonPropertyExtractor.GENDER,
        ALPHA
    );

    public static final List<PokemonPropertyExtractor> SHORT_EXTRACTOR = List.of(
        PokemonPropertyExtractor.SPECIES,
        PokemonPropertyExtractor.FORM
    );
}
