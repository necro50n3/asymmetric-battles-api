package com.necro.asymmetric.battles.common.util;

import com.cobblemon.mod.common.api.pokemon.PokemonPropertyExtractor;

import java.util.List;

public class PropertyExtractors {
    public static final List<PokemonPropertyExtractor> LONG_EXTRACTOR = List.of(
        PokemonPropertyExtractor.SPECIES,
        PokemonPropertyExtractor.ASPECTS,
        PokemonPropertyExtractor.SHINY,
        PokemonPropertyExtractor.FORM,
        PokemonPropertyExtractor.GENDER
    );

    public static final List<PokemonPropertyExtractor> SHORT_EXTRACTOR = List.of(
        PokemonPropertyExtractor.SPECIES,
        PokemonPropertyExtractor.FORM
    );
}
