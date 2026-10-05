package com.necro.asymmetric.battles.common.config.serializer;

import com.mojang.logging.LogUtils;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.serializer.ConfigSerializer;
import me.shedaniel.autoconfig.util.Utils;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import me.shedaniel.cloth.clothconfig.shadowed.org.yaml.snakeyaml.DumperOptions;
import me.shedaniel.cloth.clothconfig.shadowed.org.yaml.snakeyaml.LoaderOptions;
import me.shedaniel.cloth.clothconfig.shadowed.org.yaml.snakeyaml.Yaml;
import me.shedaniel.cloth.clothconfig.shadowed.org.yaml.snakeyaml.constructor.SafeConstructor;
import me.shedaniel.cloth.clothconfig.shadowed.org.yaml.snakeyaml.representer.Representer;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class PrimitiveYamlConfigSerializer<T extends ConfigData> implements ConfigSerializer<T> {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Config definition;
    private final Class<T> configClass;
    private final Yaml yaml;

    private PrimitiveYamlConfigSerializer(Config definition, Class<T> configClass, Yaml yaml) {
        this.definition = definition;
        this.configClass = configClass;
        this.yaml = yaml;
    }

    public PrimitiveYamlConfigSerializer(Config definition, Class<T> configClass) {
        this(definition, configClass, createYaml());
    }

    private static Yaml createYaml() {
        DumperOptions dump = new DumperOptions();
        dump.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dump.setIndent(4);
        dump.setIndicatorIndent(2);
        dump.setIndentWithIndicator(true);
        LoaderOptions load = new LoaderOptions();
        return new Yaml(new SafeConstructor(load), new Representer(dump), dump, load);
    }

    private Path configPath() {
        return Utils.getConfigFolder().resolve(this.definition.name() + ".yaml");
    }

    @Override
    public void serialize(T config) throws SerializationException {
        try {
            Path path = this.configPath();
            Files.createDirectories(path.getParent());

            StringBuilder builder = new StringBuilder();
            serializeObject(config, 0, builder, Collections.newSetFromMap(new IdentityHashMap<>()));

            Files.writeString(path, builder.toString());
        }
        catch (IOException | IllegalAccessException e) {
            throw new SerializationException(e);
        }
    }

    private void serializeObject(Object object, int indent, StringBuilder builder, Set<Object> seen) throws IllegalAccessException {
        if (!seen.add(object)) throw new IllegalStateException("Cyclic reference in config: " + object.getClass());
        Indent spacing = object.getClass().getAnnotation(Indent.class);
        boolean first = true;
        boolean prevWasBlock = false;
        try {
            for (Field field : fieldsOf(object.getClass())) {
                Object value = field.get(object);
                String key = keyName(field);
                Comment comment = getComment(field);

                boolean nested = isCustomObject(value) && !fieldsOf(value.getClass()).isEmpty();
                String dumped = null;
                if (!nested) {
                    Object plain = toPlain(value, seen);
                    if (plain == null) continue;
                    Map<String, Object> single = new LinkedHashMap<>();
                    single.put(key, plain);
                    dumped = this.yaml.dump(single);
                }
                boolean block = nested || dumped.stripTrailing().contains("\n");

                if (!first && needsGap(spacing, comment != null, block, prevWasBlock)) builder.append('\n');
                writeComment(builder, comment, indent);

                if (nested) {
                    builder.repeat(" ", indent).append(key).append(":\n");
                    serializeObject(value, indent + 4, builder, seen);
                } else if (indent == 0) {
                    builder.append(dumped);
                } else {
                    String indentStr = " ".repeat(indent);
                    for (String line : dumped.split("\n")) {
                        if (!line.isBlank()) builder.append(indentStr).append(line).append('\n');
                    }
                }

                first = false;
                prevWasBlock = block;
            }
        } finally {
            seen.remove(object);
        }
    }

    private static boolean needsGap(Indent spacing, boolean hasComment, boolean block, boolean prevWasBlock) {
        if (spacing != null) return spacing.value();
        return hasComment || block || prevWasBlock;
    }

    private static boolean isCustomObject(Object object) {
        if (object == null) return false;
        else if (object instanceof String || object instanceof Number || object instanceof Boolean || object instanceof Character) return false;
        else if (object instanceof Enum<?>) return false;
        else if (object instanceof YamlSerializer) return false;
        else if (object instanceof Collection<?>) return false;
        else if (object instanceof Map<?, ?>) return false;
        else if (object.getClass().isArray()) return false;
        return true;
    }

    private static Comment getComment(Field field) {
        Comment comment = field.getAnnotation(Comment.class);
        if (comment != null) return comment;
        if (field.getDeclaringClass().isRecord()) {
            for (RecordComponent comp : field.getDeclaringClass().getRecordComponents()) {
                if (comp.getName().equals(field.getName())) {
                    return comp.getAnnotation(Comment.class);
                }
            }
        }
        return null;
    }

    private static void writeComment(StringBuilder builder, Comment comment, int indent) {
        if (comment == null) return;
        for (String line : comment.value().split("\n")) {
            builder.repeat(" ", indent).append("# ").append(line).append('\n');
        }
    }

    private static Object toPlain(Object object, Set<Object> seen) {
        if (object == null || object instanceof String || object instanceof Number || object instanceof Boolean) return object;
        if (object instanceof Enum<?> e) return e.name();
        if (object instanceof Character) return object.toString();
        if (!seen.add(object)) throw new IllegalStateException("Cyclic reference in config: " + object.getClass());
        try {
            if (object instanceof YamlSerializer custom) return toPlain(custom.serialize(), seen);
            else if (object instanceof Collection<?> collection) {
                List<Object> result = new ArrayList<>();
                for (Object obj : collection) result.add(toPlain(obj, seen));
                return result;
            }
            else if (object instanceof Map<?, ?> map) {
                Map<Object, Object> result = new LinkedHashMap<>();
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    Object val = toPlain(entry.getValue(), seen);
                    if (val != null) result.put(plainKey(entry.getKey()), val);
                }
                return result;
            }
            else if (object.getClass().isArray()) {
                List<Object> result = new ArrayList<>();
                for (int i = 0; i < Array.getLength(object); i++) result.add(toPlain(Array.get(object, i), seen));
                return result;
            }
            Map<String, Object> result = new LinkedHashMap<>();
            for (Field field : fieldsOf(object.getClass())) {
                String key = keyName(field);
                try {
                    Object val = toPlain(field.get(object), seen);
                    if (val != null) result.put(key, val);
                }
                catch (IllegalAccessException e) {
                    logInvalidKey(key, e);
                }
            }
            return result;
        } finally {
            seen.remove(object);
        }
    }

    private static Object plainKey(Object key) {
        if (key == null || key instanceof String || key instanceof Number || key instanceof Boolean) return key;
        else if (key instanceof YamlSerializer custom) return custom.serialize();
        else if (key instanceof Enum<?> e) return e.name();
        return String.valueOf(key);
    }

    @Override
    public T deserialize() throws SerializationException {
        Path path = this.configPath();
        T instance = createDefault();
        if (!Files.exists(path)) return instance;
        try (InputStream inputStream = Files.newInputStream(path)) {
            Object raw = this.yaml.load(inputStream);
            @SuppressWarnings("unchecked")
            T result = (T) fromPlain(raw, this.configClass, instance);
            return result;
        }
        catch (Exception e) {
            throw new SerializationException(e);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object fromPlain(Object raw, Type type, Object existing) throws Exception {
        if (raw == null) return existing;
        Class<?> cls = rawClass(type);

        if (YamlSerializer.class.isAssignableFrom(cls)) {
            YamlSerializer target = existing instanceof YamlSerializer y ? y : (YamlSerializer) newInstance(cls);
            return target.deserialize(raw);
        }

        Class<?> wrapped = wrap(cls);

        if (cls == String.class) return String.valueOf(raw);
        if (wrapped == Boolean.class) return raw instanceof Boolean ? raw : Boolean.parseBoolean(raw.toString());
        if (wrapped == Character.class) return raw.toString().charAt(0);
        if (Number.class.isAssignableFrom(wrapped) && (raw instanceof Number || raw instanceof String)) {
            return toNumber(raw, wrapped);
        }
        if (cls.isEnum()) return Enum.valueOf((Class<? extends Enum>) cls, raw.toString());

        if (raw instanceof List<?> list) {
            if (cls.isArray()) {
                Class<?> comp = cls.getComponentType();
                Object array = Array.newInstance(comp, list.size());
                for (int i = 0; i < list.size(); i++) Array.set(array, i, fromPlain(list.get(i), comp, null));
                return array;
            }
            Type t = type instanceof ParameterizedType pt ? pt.getActualTypeArguments()[0] : Object.class;
            Collection<Object> result = Set.class.isAssignableFrom(cls) ? new LinkedHashSet<>() : new ArrayList<>();
            for (Object object : list) result.add(fromPlain(object, t, null));
            return result;
        }

        if (raw instanceof Map<?, ?> map) {
            if (Map.class.isAssignableFrom(cls)) {
                Type keyType = Object.class, valType = Object.class;
                if (type instanceof ParameterizedType pt && pt.getActualTypeArguments().length == 2) {
                    keyType = pt.getActualTypeArguments()[0];
                    valType = pt.getActualTypeArguments()[1];
                }
                Map<Object, Object> result = newMap(cls);
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    result.put(fromPlain(entry.getKey(), keyType, null), fromPlain(entry.getValue(), valType, null));
                }
                return result;
            }

            if (cls.isRecord()) {
                RecordComponent[] comps = cls.getRecordComponents();
                Class<?>[] types = new Class<?>[comps.length];
                Object[] args = new Object[comps.length];
                for (int i = 0; i < comps.length; i++) {
                    types[i] = comps[i].getType();
                    Field field = cls.getDeclaredField(comps[i].getName());
                    String key = lookupKey(map, field);
                    Object object = key != null ? fromPlain(map.get(key), comps[i].getGenericType(), null) : null;
                    if (object == null && types[i].isPrimitive()) {
                        object = Array.get(Array.newInstance(types[i], 1), 0);
                    }
                    args[i] = object;
                }
                Constructor<?> constructor = cls.getDeclaredConstructor(types);
                constructor.setAccessible(true);
                return constructor.newInstance(args);
            }

            Object bean = existing;
            if (bean == null) {
                Constructor<?> constructor = cls.getDeclaredConstructor();
                constructor.setAccessible(true);
                bean = constructor.newInstance();
            }
            for (Field field : fieldsOf(bean.getClass())) {
                String key = lookupKey(map, field);
                if (key == null) continue;
                try {
                    field.set(bean, fromPlain(map.get(key), field.getGenericType(), field.get(bean)));
                }
                catch (Exception e) {
                    logInvalidKey(key, e);
                }
            }
            return bean;
        }
        return raw;
    }

    private static Object toNumber(Object raw, Class<?> boxed) {
        Number n = raw instanceof Number num ? num : new java.math.BigDecimal(raw.toString().trim());
        if (boxed == Integer.class) return n.intValue();
        if (boxed == Long.class) return n.longValue();
        if (boxed == Double.class) return n.doubleValue();
        if (boxed == Float.class) return n.floatValue();
        if (boxed == Short.class) return n.shortValue();
        if (boxed == Byte.class) return n.byteValue();
        return n;
    }

    @SuppressWarnings("unchecked")
    private static Map<Object, Object> newMap(Class<?> cls) {
        if (!cls.isInterface() && !Modifier.isAbstract(cls.getModifiers())) {
            try {
                Constructor<?> constructor = cls.getDeclaredConstructor();
                constructor.setAccessible(true);
                return (Map<Object, Object>) constructor.newInstance();
            }
            catch (ReflectiveOperationException ignored) {}
        }
        return new LinkedHashMap<>();
    }

    private static Object newInstance(Class<?> cls) throws Exception {
        try {
            Constructor<?> constructor = cls.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (NoSuchMethodException e) {
            return Utils.constructUnsafely(cls);
        }
    }

    private static List<Field> fieldsOf(Class<?> cls) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> c = cls; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                int modifier = field.getModifiers();
                if (Modifier.isStatic(modifier) || Modifier.isTransient(modifier) || field.isSynthetic()) continue;
                field.setAccessible(true);
                fields.add(field);
            }
        }
        return fields;
    }

    private static Class<?> rawClass(Type type) {
        if (type instanceof Class<?> cls) return cls;
        if (type instanceof ParameterizedType param) return (Class<?>) param.getRawType();
        return Object.class;
    }

    private static Class<?> wrap(Class<?> cls) {
        if (cls == int.class) return Integer.class;
        if (cls == long.class) return Long.class;
        if (cls == double.class) return Double.class;
        if (cls == float.class) return Float.class;
        if (cls == short.class) return Short.class;
        if (cls == byte.class) return Byte.class;
        if (cls == boolean.class) return Boolean.class;
        if (cls == char.class) return Character.class;
        return cls;
    }

    private static String keyName(Field field) {
        YamlKey key = field.getAnnotation(YamlKey.class);
        if (key != null) return key.value();
        return field.getName();
    }

    private static String lookupKey(Map<?, ?> map, Field field) {
        String key = keyName(field);
        if (map.containsKey(key)) return key;
        if (map.containsKey(field.getName())) return field.getName();
        return null;
    }

    private static void logInvalidKey(String key, Exception e) {
        LOGGER.warn("Ignoring invalid value for '{}': {}", key, e.toString());
    }

    @Override
    public T createDefault() {
        return Utils.constructUnsafely(this.configClass);
    }
}