package net.mcaurora.serialization;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.massivecraft.factions.Faction;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import sun.reflect.ReflectionFactory;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@UtilityClass
public class DeepObjectSerializer {
    private final Pattern JAVA_CLASS_REGEX = Pattern.compile("java\\.lang\\.[a-zA-Z0-9_]*");

    @SneakyThrows
    private void setToType(Field f, Object o, JsonElement e) {
        f.set(o, switch (f.getType().getName()) {
            case "byte", "java.lang.Byte" -> e.getAsByte();
            case "short", "java.lang.Short" -> e.getAsShort();
            case "int", "java.lang.Integer" -> e.getAsInt();
            case "long", "java.lang.Long" -> e.getAsBigInteger();
            case "float", "java.lang.Float" -> e.getAsFloat();
            case "double", "java.lang.Double" -> e.getAsDouble();
            case "boolean", "java.lang.Boolean" -> e.getAsBoolean();
            case "char", "java.lang.Character" -> e.getAsString().toCharArray()[0];
            case "java.lang.String" -> e.getAsString();
            default -> e.getAsJsonNull();
        });
    }

    @SuppressWarnings("unchecked")
    private <T> JsonArray serializeArray(Object array, Class<T> componentType) {
        JsonArray jsonArray = new JsonArray();
        for (int i = 0; i < Array.getLength(array); i++) {
            jsonArray.add(getFromArray((T) Array.get(array, i), componentType, new JsonObject()));
        }
        return jsonArray;
    }

    private <T> JsonObject getFromArray(T o, Class<T> t, JsonObject jsonObject) {
        if (o == null) {
            jsonObject.addProperty("", "null");
        } else if (t.isPrimitive() || JAVA_CLASS_REGEX.matcher(t.getName()).matches()) {
            jsonObject.addProperty("", o.toString());
        } else if (t.isArray()) {
            jsonObject.add("", serializeArray(o, t.componentType()));
        } else {
            put(o, "", jsonObject);
        }
        return jsonObject;
    }

    @SuppressWarnings("unchecked")
    private <T> T[] deserializeArray(JsonArray array, Class<T> type) {
        List<T> elements = new ArrayList<>();
        for (JsonElement jsonElement : array) {
            elements.add(load("", type, new JsonObject()));
        }
        return (T[]) elements.toArray();
    }

    @SneakyThrows
    private JsonObject put(Object object, String key, JsonObject jsonObject) {
        for (Field f : object.getClass().getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers())) continue;
            f.setAccessible(true);
            String nextKey = key + "." + f.getName();
            if (f.get(object) == null) {
                jsonObject.add(nextKey, null);
            } else if (f.getType().isPrimitive() || JAVA_CLASS_REGEX.matcher(f.getType().getName()).matches()) {
                jsonObject.addProperty(nextKey, f.get(object).toString());
            } else if (f.getType().isArray()) {
                jsonObject.add(nextKey, serializeArray(f.get(object), f.getType().componentType()));
            } else if (f.getType().isEnum()) {
                jsonObject.addProperty(nextKey, f.get(object).toString());
            } else {
                put(f.get(object), nextKey, jsonObject);
            }
        }
        return jsonObject;
    }

    @SuppressWarnings("rawtypes unchecked")
    @SneakyThrows
    private <T> T load(String key, Class<T> type, JsonObject jsonObject) {
        ReflectionFactory rf = ReflectionFactory.getReflectionFactory();
        Constructor constructor = rf.newConstructorForSerialization(type, Object.class.getDeclaredConstructor());
        T tr = type.cast(constructor.newInstance());
        for (Field f : type.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers())) continue;
            f.setAccessible(true);
            String nextKey = key + "." + f.getName();
            if (jsonObject.has(nextKey) && jsonObject.get(nextKey).isJsonNull()) {
                f.set(tr, null);
                return tr;
            }
            if (f.getType().isPrimitive() || JAVA_CLASS_REGEX.matcher(f.getType().getName()).matches()) {
                setToType(f, tr, jsonObject.get(nextKey));
            } else if (f.getType().isArray()) {
                f.set(tr, deserializeArray(jsonObject.getAsJsonArray(nextKey), f.getType().componentType()));
            } else if (f.getType().isEnum()) {
                f.set(tr, Enum.valueOf((Class<Enum>) f.getType(), jsonObject.get(nextKey).getAsString()));
            } else {
                f.set(tr, load(nextKey, f.getType(), jsonObject));
            }
        }
        return tr;
    }

    public void putObject(Faction faction, Object object, String key) {
        faction.setAddonItem(key, put(object, key, new JsonObject()).toString());
    }

    public <T> T loadObject(Faction faction, String key, Class<T> type) {
        if (!faction.hasAddonItem(key)) return null;
        JsonObject jsonObject = new Gson().fromJson((String) faction.getAddonItem(key), JsonObject.class);
        return load(key, type, jsonObject);
    }

    public void putList(Faction faction, List<?> objects, String key) {
        JsonArray jsonArray = new JsonArray();
        for (Object object : objects) {
            jsonArray.add(put(object, "", new JsonObject()));
        }
        faction.setAddonItem(key, jsonArray.toString());
    }

    public <T> List<T> loadList(Faction faction, String key, Class<T> type) {
        List<T> list = new ArrayList<>();
        if (!faction.hasAddonItem(key)) return list;
        JsonArray jsonArray = new Gson().fromJson((String) faction.getAddonItem(key), JsonArray.class);
        for (JsonElement jsonElement : jsonArray) {
            if (jsonElement.isJsonPrimitive()) continue;
            list.add(load("", type, jsonElement.getAsJsonObject()));
        }
        return list;
    }
}
