package explorer.model.treetools;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import explorer.model.AnatomyNode;
import org.objenesis.strategy.StdInstantiatorStrategy;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class KryoUtils {

    /**
     * Loads and deserializes the "part-of" AnatomyNode tree from a Kryo file.
     * The file must contain a previously serialized AnatomyNode tree.
     * Uses the same class registrations as during serialization to ensure compatibility.
     *
     * @return the deserialized AnatomyNode tree, or null if loading fails
     */
    public static AnatomyNode loadTreeFromKryo(String kryoFilePath) {
        Kryo kryo = new Kryo();
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());

        // Registrierungen wie beim Speichern (optional, aber empfohlen)
        kryo.register(AnatomyNode.class);
        kryo.register(ArrayList.class);
        kryo.register(String.class);

        try (Input input = new Input(new FileInputStream(kryoFilePath))) {
            return kryo.readObject(input, AnatomyNode.class);
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
    public static void freezeTree(AnatomyNode tree, String saveToPath) {
        // setting strategy
        Kryo kryo = new Kryo();
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());  // Optional, wenn du final-Felder hast

        // register classes
        kryo.register(AnatomyNode.class);
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
}
