package explorer.model;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.databind.ObjectMapper;
import explorer.model.treetools.ConceptNode;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.*;
import java.util.*;

public class KryoUtils {

    private static final Map<String, Object> cache = new HashMap<>();

    /**
     * Creates a new Kryo instance and registers the provided classes.
     *
     * @param toRegister Classes to register with Kryo.
     * @return Configured Kryo instance.
     */
    private static Kryo newKryo(Class<?>... toRegister) {
        Kryo kryo = new Kryo();
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
        for (Class<?> c : toRegister) kryo.register(c);
        return kryo;
    }

    /**
     * Serializes (freezes) an object to the specified file path using Kryo.
     *
     * @param obj        The object to serialize.
     * @param saveToPath The path to save the serialized object.
     * @param toRegister Classes to register with Kryo.
     * @param <T>        The type of the object.
     */
    public static <T> void freezeObject(T obj, String saveToPath, Class<?>... toRegister) {
        Kryo kryo = newKryo(toRegister);
        try (Output output = new Output(new FileOutputStream(saveToPath))) {
            kryo.writeObject(output, obj);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deserializes (thaws) an object from the specified resource path using Kryo.
     * The resource path should be relative to the classpath, e.g. "/requests/conceptTerms.kryo".
     *
     * @param resourceKryoPath   The resource path of the serialized file (relative to classpath).
     * @param clazz      The class of the object to deserialize.
     * @param toRegister Classes to register with Kryo.
     * @param <T>        The type of the object.
     * @return The deserialized object, or null if deserialization fails or resource is not found.
     */
    @SuppressWarnings("unchecked")
    public static <T> T thawObject(String resourceKryoPath, Class<T> clazz, Class<?>... toRegister) {
        Kryo kryo = newKryo(toRegister);
        try (InputStream stream = KryoUtils.class.getResourceAsStream(resourceKryoPath)) {
            if (stream == null) {
                System.err.println("Resource not found: " + resourceKryoPath);
                return null;
            }
            try (Input input = new Input(stream)) {
                return (T) kryo.readObject(input, clazz);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Serializes a ConceptNode tree to the specified file path.
     *
     * @param tree       The ConceptNode tree to serialize.
     * @param saveToPath The path to save the serialized tree.
     */
    public static void freezeTree(ConceptNode tree, String saveToPath) {
        freezeObject(tree, saveToPath, ConceptNode.class, ArrayList.class, String.class);
    }

    /**
     * Deserializes a ConceptNode tree from the specified Kryo resource.
     *
     * @param resourcePath The resource path of the Kryo file (relative to classpath).
     * @return The deserialized ConceptNode tree, or null if deserialization fails.
     */
    public static ConceptNode thawTreeFromKryo(String resourcePath) {
        return thawObject(resourcePath, ConceptNode.class, ConceptNode.class, ArrayList.class, String.class);
    }

    /**
     * Serializes a Map<Integer, Double> to the specified file path.
     *
     * @param map        The map to serialize.
     * @param saveToPath The path to save the serialized map.
     */
    public static void freezeIntegerMap(Map<Integer, Double> map, String saveToPath) {
        freezeObject(map, saveToPath, HashMap.class, Double.class, Integer.class);
    }

    /**
     * Deserializes a Map<Integer, Double> from the specified Kryo resource, with caching.
     *
     * @param resourcePath The resource path of the Kryo file (relative to classpath).
     * @return The deserialized map, or null if deserialization fails.
     */
    public static Map<Integer, Double> thawIntegerMapFromKryo(String resourcePath) {
        // Uses cache to avoid redundant deserialization
        if (cache.containsKey(resourcePath)) return (Map<Integer, Double>) cache.get(resourcePath);
        Map<Integer, Double> map = thawObject(resourcePath, HashMap.class, HashMap.class, Double.class, Integer.class);
        cache.put(resourcePath, map);
        return map;
    }

    /**
     * Reads the JSON file containing the concept terms and serializes it to a Kryo file.
     * The resulting file is written to "src/main/resources/requests/conceptTerms.kryo".
     */
    public static void freezeConceptTermsMap() {
        ObjectMapper mapper = new ObjectMapper();
        InputStream termStream = Objects.requireNonNull(
                KryoUtils.class.getResourceAsStream("/requests/conceptTerms.json"));

        Map<String, List<String>> conceptTerms;
        try {
            // Read the JSON as a LinkedHashMap
            conceptTerms = mapper.readValue(termStream, LinkedHashMap.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        freezeObject(conceptTerms, "src/main/resources/requests/conceptTerms.kryo",
                     LinkedHashMap.class, ArrayList.class, String.class);
        System.out.println("conceptTerms.kryo successfully generated!");
    }

    /**
     * Deserializes a Map<String, List<String>> from the specified Kryo resource.
     *
     * @param resourcePath The resource path of the Kryo file (relative to classpath).
     * @return The deserialized map, or null if deserialization fails.
     */
    public static Map<String, List<String>> thawStringMapFromKryo(String resourcePath) {
        return thawObject(resourcePath, LinkedHashMap.class, LinkedHashMap.class, ArrayList.class, String.class);
    }
}