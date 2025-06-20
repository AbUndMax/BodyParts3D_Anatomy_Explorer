package explorer.model;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;

public class KryoLoader {
    public static AnatomyNode loadPartOfTreeFromKryo() {
        Kryo kryo = new Kryo();
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());

        // Registrierungen wie beim Speichern (optional, aber empfohlen)
        kryo.register(AnatomyNode.class);
        kryo.register(LinkedList.class);
        kryo.register(String.class);

        try (Input input = new Input(new FileInputStream("src/main/resources/serializedTress/partOf_tree.kryo"))) {
            return kryo.readObject(input, AnatomyNode.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
