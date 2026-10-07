package com.necro.asymmetric.battles.common.api.spawning;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class BattleSpawnDetailListAdapter implements JsonDeserializer<List<BattleSpawnDetail>> {
    @Override
    public List<BattleSpawnDetail> deserialize(JsonElement json, Type typeOf, JsonDeserializationContext context) {
        List<BattleSpawnDetail> result = new ArrayList<>();
        if (!json.isJsonArray()) throw new JsonParseException("Expected a JSON array for \"spawns\"");

        for (JsonElement element : json.getAsJsonArray()) {
            try {
                BattleSpawnDetail detail = context.deserialize(element, BattleSpawnDetail.class);
                if (detail.isValid()) result.add(detail);
            }
            catch (JsonParseException ignored) {}
        }

        return result;
    }
}
