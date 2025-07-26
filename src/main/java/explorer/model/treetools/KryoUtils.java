package explorer.model.treetools;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class KryoUtils {

    private static Map<String, Map<Integer, Double>> lazyLoadedMap = new HashMap<String, Map<Integer, Double>>();

    /**
     * Loads and deserializes the "part-of" AnatomyNode tree from a Kryo file.
     * The file must contain a previously serialized AnatomyNode tree.
     * Uses the same class registrations as during serialization to ensure compatibility.
     *
     * @return the deserialized AnatomyNode tree, or null if loading fails
     */
    public static ConceptNode thawTreeFromKryo(String resourceKryoPath) {
        Kryo kryo = new Kryo();
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());

        kryo.register(ConceptNode.class);
        kryo.register(ArrayList.class);
        kryo.register(String.class);

        try (InputStream input = KryoUtils.class.getResourceAsStream(resourceKryoPath)) {
            // TODO: need better logging
            if (input == null) throw new FileNotFoundException("Resource not found: " + resourceKryoPath);

            Input inputKryo = new Input(input);
            return kryo.readObject(inputKryo, ConceptNode.class);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Serializes the given AnatomyNode tree to a file using Kryo for efficient deserialization later.
     *
     * @param tree the AnatomyNode tree to serialize
     * @param saveToPath the path where the Kryo file will be saved
     */
    public static void freezeTree(ConceptNode tree, String saveToPath) {
        // setting strategy
        Kryo kryo = new Kryo();
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());

        // register classes
        kryo.register(ConceptNode.class);
        kryo.register(ArrayList.class);
        kryo.register(String.class);

        // save to file
        try (Output output = new Output(new FileOutputStream(saveToPath))) {
            kryo.writeObject(output, tree);
            System.out.println("Kryo file successfully generated!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<Integer, Double> thawIntegerMapFromKryo(String resourceKryoPath) {
        if (lazyLoadedMap.containsKey(resourceKryoPath)) {
            return lazyLoadedMap.get(resourceKryoPath);
        }

        Kryo kryo = new Kryo();
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());

        kryo.register(java.util.HashMap.class);
        kryo.register(Double.class);
        kryo.register(Integer.class);

        try (InputStream input = KryoUtils.class.getResourceAsStream(resourceKryoPath)) {
            if (input == null) throw new FileNotFoundException("Resource not found: " + resourceKryoPath);
            Input inputKryo = new Input(input);
            Map<Integer, Double> loadedMap = kryo.readObject(inputKryo, HashMap.class);
            lazyLoadedMap.put(resourceKryoPath, loadedMap);
            return loadedMap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void freezeIntegerMap(Map<Integer, Double> map, String saveToPath) {

        Kryo kryo = new Kryo();
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());

        kryo.register(java.util.HashMap.class);
        kryo.register(Double.class);
        kryo.register(Integer.class);

        try (Output output = new Output(new FileOutputStream(saveToPath))) {
            kryo.writeObject(output, map);
            System.out.println("Node degree map successfully serialized with Kryo.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
