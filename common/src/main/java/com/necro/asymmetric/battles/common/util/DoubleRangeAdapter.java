package com.necro.asymmetric.battles.common.util;

import com.cobblemon.mod.common.util.math.DoubleRange;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DoubleRangeAdapter implements JsonSerializer<DoubleRange>, JsonDeserializer<DoubleRange> {
    public static final DoubleRangeAdapter INSTANCE = new DoubleRangeAdapter();
    private static final String NUMBER = "-?\\d+(?:\\.\\d+)?(?:[eE][+-]?\\d+)?";
    private static final Pattern PATTERN = Pattern.compile("(" + NUMBER + ")-?(" + NUMBER + ")?");

    @Override
    public JsonElement serialize(DoubleRange range, Type type, JsonSerializationContext ctx) {
        return new JsonPrimitive(serialize(range));
    }

    public String serialize(DoubleRange range) {
        if (range.getStart().equals(range.getEndInclusive())) return Double.toString(range.getStart());
        else return range.getStart() + "-" + range.getEndInclusive();
    }

    @Override
    public DoubleRange deserialize(JsonElement json, Type type, JsonDeserializationContext context) {
        return deserialize(json.getAsString());
    }

    public DoubleRange deserialize(String string) {
        Matcher matcher = PATTERN.matcher(string);
        if (!matcher.find()) throw new IllegalArgumentException("Invalid double range: " + string);

        String start = matcher.group(1);
        String end = matcher.group(2);

        double startValue = Double.parseDouble(start);
        if (end == null || end.isEmpty()) return new DoubleRange(startValue, startValue);
        else return new DoubleRange(startValue, Double.parseDouble(end));
    }
}
