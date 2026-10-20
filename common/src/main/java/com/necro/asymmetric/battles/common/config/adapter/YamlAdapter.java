package com.necro.asymmetric.battles.common.config.adapter;

import java.util.function.Function;

public interface YamlAdapter<T> {
    Object serialize(T value);
    T deserialize(Object raw);

    static <T> YamlAdapter<T> of(Function<T, Object> serializer, Function<Object, T> deserializer) {
        return new YamlAdapter<>() {
            @Override
            public Object serialize(T value) {
                return serializer.apply(value);
            }

            @Override
            public T deserialize(Object raw) {
                return deserializer.apply(raw);
            }
        };
    }
}
