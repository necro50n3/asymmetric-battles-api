package com.necro.asymmetric.battles.common.config.serializer;

import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.api.DeserializationException;

public interface YamlSerializer {
    Object serialize();

    Object deserialize(Object raw) throws DeserializationException;
}
