package explorer.model.treeBuilder;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import explorer.model.AnatomyNode;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;

public class KryoFreezer {

    public static void freezeTree(AnatomyNode tree, String saveToPath) {
        // Freeze the tree
        Kryo kryo = new Kryo();
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());  // Optional, wenn du final-Felder hast

        // (Optional) Klassen registrieren, für bessere Performance
        kryo.register(AnatomyNode.class);
        kryo.register(LinkedList.class);
        kryo.register(String.class);

        // Speicherort
        try (Output output = new Output(new FileOutputStream(saveToPath))) {
            kryo.writeObject(output, tree);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
